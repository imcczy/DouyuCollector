package in.odachi.douyucollector.producer.reactor;

import in.odachi.douyucollector.producer.util.PacketUtil;
import in.odachi.douyucollector.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息收发
 */
class EventHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 连续接收到海量RSS消息问题
    private static final int MAX_NUMBER_OF_RSS_MSG = 10;
    private static final int HEADER_SIZE = 4;

    static AtomicLong processedTotalCount = new AtomicLong();

    private final Integer roomId;
    private final BlockingQueue<Message> queue;
    private final InetSocketAddress socketAddress;
    private final SocketChannel socketChannel;
    private final ThreadedSelector.BossThread bossThread;

    private ByteBuffer readBuf = null;
    private int continuousRssCount = 0;

    /**
     * 客户端初始化
     */
    EventHandler(Integer roomId, BlockingQueue<Message> queue, InetSocketAddress socketAddress,
                 SocketChannel socketChannel, ThreadedSelector.BossThread bossThread) {
        this.roomId = roomId;
        this.queue = queue;
        this.socketAddress = socketAddress;
        this.socketChannel = socketChannel;
        this.bossThread = bossThread;
    }

    /**
     * 处理读事件
     * 斗鱼弹幕服务器采用TCP长连接通信，会频繁出现粘包或断包现象。
     * 根据协议，每个数据包会在首8个字节发送消息长度，并且以'\0'结尾。
     * 这里选择根据消息长度采用ByteBuffer解决粘包问题。
     * 先读取头部信息，转换成消息长度，再读取相应长度的报文。
     * 同时斗鱼服务端疑似限制同IP最大连接数为200（约），我们这里监听channel.read()返回值用于判断连接是否被强制关闭了。
     */
    boolean read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        // inputBuf为空，表示开始读取一个新的消息包
        if (readBuf == null || readBuf.capacity() == HEADER_SIZE) {
            // 先读取头部信息，判断消息长度
            if (readBuf == null) {
                readBuf = ByteBuffer.allocate(HEADER_SIZE);
            }

            // if the peer closes the connection, OP_READ will fire and a read will return -1.
            try {
                if (channel.read(readBuf) == -1) {
                    logger.error("Channel is closed by the peer in step 1: {}", toString());
                    return false;
                }
            } catch (IOException e) {
                logger.error("Got an IOException during read, {}", e);
                return false;
            }
            // 没有将头部4字节读取完，等待继续读取
            if (readBuf.hasRemaining()) {
                return true;
            }

            // 包头部分已经接受完毕
            final int count = PacketUtil.LHtoI(readBuf.array());
            // 消息体长度应不小于8（此处可以不判断，但防止网络抖动。。）
            if (count <= HEADER_SIZE + 4) {
                logger.error("Packet headerBuf got count byte: {}, channel is closing: {}", count, toString());
                return false;
            } else {
                // 读count长度的消息
                readBuf = ByteBuffer.allocate(count);
            }
            return true;
        }

        // 尝试读取数据区域
        try {
            if (channel.read(readBuf) == -1) {
                logger.error("Channel is closed by the peer in step 2: {}", toString());
                return false;
            }
        } catch (IOException e) {
            logger.error("Got an IOException during read, {}", e);
            return false;
        }
        // 数据还没有填充满，继续接受数据
        if (readBuf.hasRemaining()) {
            return true;
        }
        byte[] data = readBuf.array();
        // 数据已经接受完，处理消息并继续接受新数据
        readBuf = null;
        return process(data);
    }

    /**
     * 处理消息
     */
    private boolean process(byte[] data) {
        // 消息体长度要剪掉8个字节，4个字节是消息长度，2字节小端整数表示消息类型，1个字节加密字段，1个字节保留字段。
        // 这里不二次校验消息长度是否相同，因为即使校验出不相同也没啥办法。。
        final int dataBodyLen = data.length - HEADER_SIZE - 4;
        final byte[] dataBody = new byte[dataBodyLen];

        // 消息体跳过前8个字节
        System.arraycopy(data, HEADER_SIZE + 4, dataBody, 0, dataBodyLen);
        String dataBodyStr = new String(dataBody);
        // 房间开关播提醒
        if (dataBodyStr.startsWith("type@=rss/")) {
            // 如果是rss消息，计数器+1
            continuousRssCount++;
            // 有时会连续收到无数个rss消息，
            // 如果检测超过MAX_NUMBER_OF_RSS_MSG个，则认为出现bug，关闭该链接
            if (continuousRssCount >= MAX_NUMBER_OF_RSS_MSG) {
                logger.warn("Received too many RSS packet in a row, channel is closing: {}", toString());
                return false;
            }
        } else {
            // 如果接下来的消息不是rss消息，则计数器归零
            continuousRssCount = 0;
        }

        // 解析其他信息
        queue.add(new Message(dataBodyStr));
        processedTotalCount.incrementAndGet();
        return true;
    }

    /**
     * 处理写事件
     * 发送登陆消息、注册消息和心跳保持消息，每次只发送一个类型消息
     */
    boolean write(SelectionKey key) {
        return keepAlive(key);
    }

    /**
     * 发送心跳信息
     */
    private boolean keepAlive(SelectionKey key) {
        byte[] keepAliveRequest = PacketUtil.generateKeepAlivePacket();
        ByteBuffer writeBuf = ByteBuffer.allocate(keepAliveRequest.length);
        writeBuf.put(keepAliveRequest);
        writeBuf.flip();

        SocketChannel channel = (SocketChannel) key.channel();
        try {
            while (writeBuf.hasRemaining()) {
                if (channel.write(writeBuf) <= 0) {
                    // 发送失败关闭链接
                    logger.error("Send keep alive request error, channel is closing: {}", toString());
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("Got an IOException during write: ", e);
            return false;
        }
        logger.debug("Send keep alive request success: {}", toString());
        return true;
    }

    /**
     * 关闭连接
     */
    public void close() {
        bossThread.cleanupChannel(this);
    }

    // Getter and setter
    public Integer getRoomId() {
        return roomId;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public String toString() {
        return roomId + " (" + socketAddress + ")";
    }
}
