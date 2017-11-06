package in.odachi.douyucollector.producer.reactor;

import in.odachi.douyucollector.ChannelTask;
import in.odachi.douyucollector.common.constant.Constants;
import in.odachi.douyucollector.producer.util.PacketUtil;
import in.odachi.douyucollector.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reactor管理类
 */
public class ThreadedSelector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 自增编号
    private static long index = 0;

    private final Args args;
    private final Set<BossThread> bossThreads = new HashSet<>();
    private final Set<SelectorThread> selectorThreads = new HashSet<>();

    // 私有构造器
    public ThreadedSelector(Args args) {
        args.validate();
        this.args = args;
        // 启动相关线程
        startThreads();
        startKeepAliveThread();
        startWatcherThread();
    }

    /**
     * 启动selector线程和boss线程
     */
    private void startThreads() {
        for (int i = 0; i < args.selectorThreads; ++i) {
            selectorThreads.add(new SelectorThread());
        }
        for (SelectorThread thread : selectorThreads) {
            thread.start();
        }
        SelectorThreadLoadBalancer balancer = new SelectorThreadLoadBalancer(selectorThreads);
        for (String host : args.hosts) {
            bossThreads.add(new BossThread(host, balancer));
        }
        for (BossThread bossThread : bossThreads) {
            bossThread.start();
        }
    }

    /**
     * 创建心跳保持守护线程，定时发送心跳消息
     */
    private void startKeepAliveThread() {
        Thread keepAliveThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 通知轮训器注册写操作
                    selectorThreads.forEach((thread) -> {
                        thread.interestWriteOpsAll();
                        logger.info(thread.getName() + ", Keep alive packet is sent, selectionKey size: {}",
                                thread.getSelectionKeySize());
                    });
                    Thread.sleep(Constants.KEEP_ALIVE_SLEEP_TIME);
                } catch (RuntimeException e) {
                    logger.error(e.getLocalizedMessage());
                } catch (InterruptedException e) {
                    break;
                }
            }
            logger.error("{} has exited.", Thread.currentThread().getName());
        }, "KeepAlive");
        // 心跳保持线程
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    /**
     * 创建数据监控守护线程
     */
    private void startWatcherThread() {
        Thread watcherThread = new Thread(() -> {
            final int EMPTY_MESSAGE_TIMES = 5;
            int failedTime = 0;
            while (!Thread.currentThread().isInterrupted()) {
                String processedTotalRate = String.format("%.0f", EventHandler.processedTotalCount.getAndSet(0) /
                        (double) Constants.WATCHER_SLEEP_TIME * 60 * 1000);
                // 拼接输出字符串
                String builder = "Statistics, " + "processed msg rate: " + processedTotalRate + "/minute";
                if (Double.parseDouble(processedTotalRate) <= 0) {
                    // 连续数次采集数据为空
                    failedTime++;
                    if (failedTime >= EMPTY_MESSAGE_TIMES) {
                        failedTime = 0;
                        logger.error(builder);
                    }
                } else {
                    failedTime = 0;
                    logger.info(builder);
                }

                for (BossThread bossThread : bossThreads) {
                    // 输出bossThread统计
                    logger.info(bossThread.getStatistics());
                }
                for (BossThread bossThread : bossThreads) {
                    // 检查Host健康度
                    bossThread.checkHost();
                }
                try {
                    Thread.sleep(Constants.WATCHER_SLEEP_TIME);
                } catch (InterruptedException ignored) {
                }
            }
            logger.error("{} has exited.", Thread.currentThread().getName());
        }, "ReactorWatcher");
        // 设置为守护线程
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    /**
     * 向所有选择器线程发送中断信号
     */
    public void stop() {
        // 中断所有选择器
        bossThreads.forEach(Thread::interrupt);
        selectorThreads.forEach(SelectorThread::interestWriteOpsAll);
    }

    /**
     * 所有选择器线程是否已经终止
     */
    public boolean isStopped() {
        for (SelectorThread thread : selectorThreads) {
            if (thread.getState() == Thread.State.TERMINATED) {
                for (BossThread bossThread : bossThreads) {
                    if (bossThread.getState() != Thread.State.TERMINATED) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static class Args {

        // The number of threads for selecting on already-accepted connections
        private int selectorThreads = 2;
        private String[] hosts;
        private BlockingQueue<ChannelTask> taskQueue;
        private BlockingQueue<Message> messageQueue;

        public Args() {
        }

        public Args selectorThreads(int n) {
            this.selectorThreads = n;
            return this;
        }

        public Args hosts(String[] hosts) {
            this.hosts = hosts;
            return this;
        }

        public Args channelTaskQueue(BlockingQueue<ChannelTask> taskQueue) {
            this.taskQueue = taskQueue;
            return this;
        }

        public Args messageQueue(BlockingQueue<Message> messageQueue) {
            this.messageQueue = messageQueue;
            return this;
        }

        public void validate() {
            if (selectorThreads <= 0) {
                throw new IllegalArgumentException("selectorThreads must be positive.");
            }
            if (hosts == null || hosts.length <= 0) {
                throw new IllegalArgumentException("hosts must be non-empty.");
            }
            if (taskQueue == null) {
                throw new IllegalArgumentException("taskQueue must be not-null.");
            }
            if (messageQueue == null) {
                throw new IllegalArgumentException("messageQueue must be not-null.");
            }
        }
    }

    /**
     * A round robin load balancer for choosing selector threads for new
     * connections.
     */
    protected static class SelectorThreadLoadBalancer {
        private final Collection<? extends SelectorThread> threads;
        private Iterator<? extends SelectorThread> nextThreadIterator;

        public <T extends SelectorThread> SelectorThreadLoadBalancer(Collection<T> threads) {
            if (threads.isEmpty()) {
                throw new IllegalArgumentException("At least one selector thread is required");
            }
            this.threads = Collections.unmodifiableList(new ArrayList<>(threads));
            nextThreadIterator = this.threads.iterator();
        }

        public SelectorThread nextThread() {
            // Choose a selector thread (round robin)
            if (!nextThreadIterator.hasNext()) {
                nextThreadIterator = threads.iterator();
            }
            return nextThreadIterator.next();
        }
    }

    /**
     * 连接服务器线程
     */
    class BossThread extends Thread {

        private static final int MAX_FAILED_TIMES = 3;

        // 可用端口：8601,8602,12601,12602,12603,12604
        private final Integer[] ports = new Integer[]{8601, 8602, 12601, 12602, 12603, 12604};
        private final SelectorThreadLoadBalancer threadChooser;
        private final Set<Integer> roomListenedSet;
        private final Logger logger = LoggerFactory.getLogger(getClass());

        // 目前斗鱼服务器每个客户端IP限制200（约）个连接
        private BlockingQueue<InetSocketAddress> socketPermit = new LinkedBlockingQueue<>();
        private String host;

        // 统计连接质量
        private long costMax = Long.MIN_VALUE;
        private long costMin = Long.MAX_VALUE;
        private long costAvg;
        private long tryConnect = 0;
        private long failedConnect = 0;
        private long tryConnectLast = 0;
        private long failedConnectLast = 0;

        // 是否停用
        private volatile boolean stopped = false;

        /**
         * 连接器初始化
         */
        BossThread(String host, SelectorThreadLoadBalancer threadChooser) {
            setName(getClass().getSimpleName() + "-" + host.replaceAll("\\.", "-"));
            this.host = host;
            this.threadChooser = threadChooser;
            this.roomListenedSet = new HashSet<>();
            for (Integer port : ports) {
                for (int i = 0; i < Constants.CONNECTION_LIMIT_PER_HOST; i++) {
                    socketPermit.add(new InetSocketAddress(host, port));
                }
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (stopped) {
                        // 如果该Host被禁用，则等待2小时
                        long sleepTime = 2 * 60 * 60 * 1000;
                        logger.error("Host {} will sleep {} seconds!", host, sleepTime / 1000);
                        Thread.sleep(sleepTime);
                        stopped = false;
                    }
                    InetSocketAddress socketAddress = acquirePermit();
                    ChannelTask task = args.taskQueue.take();
                    if (contains(task.getRoomId())) {
                        logger.trace("Room {} is under listening!", task.getRoomId());
                        releasePermit(socketAddress);
                        continue;
                    }
                    if (!addChannel(task.getRoomId(), socketAddress)) {
                        task.addFailedTimes();
                        if (task.getFailedTimes() < MAX_FAILED_TIMES) {
                            args.taskQueue.add(task);
                        }
                        logger.error("Add channel FAILED: {}/{}/{}", socketAddress, task.getRoomId(), task.getFailedTimes());
                        // 休息20秒
                        Thread.sleep(20 * 1000);
                        releasePermit(socketAddress);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        /**
         * 创建连接
         */
        boolean addChannel(Integer roomId, InetSocketAddress socketAddress) {
            SocketChannel channel = null;
            long start = System.currentTimeMillis();
            try {
                channel = SocketChannel.open();
                EventHandler eventHandler = createEventHandler(roomId, socketAddress, channel, BossThread.this);
                channel.connect(socketAddress);
                // try login
                if (!login(roomId, eventHandler, channel)) {
                    throw new IOException(String.format("Channel login FAILED, %s/%s", socketAddress, roomId));
                }
                // 设为非阻塞模式
                channel.configureBlocking(false);
                registerChannel(eventHandler);
                synchronized (roomListenedSet) {
                    roomListenedSet.add(roomId);
                }
            } catch (RuntimeException | IOException e) {
                failedConnect++;
                // 只有出异常才关闭channel
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ignored) {
                    }
                }
                releasePermit(socketAddress);
                return false;
            } finally {
                long cost = System.currentTimeMillis() - start;
                logger.trace("Channel connect cost: {} ms, {}/{}" + cost, socketAddress, roomId);
                // 统计最大值，最小值，平均值
                if (cost > costMax) {
                    costMax = cost;
                }
                if (cost < costMin) {
                    costMin = cost;
                }
                costAvg = (costAvg * tryConnect + cost) / (tryConnect + 1);
                tryConnect++;
            }
            return true;
        }

        private EventHandler createEventHandler(Integer roomId, InetSocketAddress socketAddress,
                                                SocketChannel channel, BossThread bossThread) {
            return new EventHandler(roomId, args.messageQueue, socketAddress, channel, bossThread);
        }

        private boolean login(Integer roomId, EventHandler eventHandler, SocketChannel channel) throws IOException {
            final int BUF_SIZE = 1024;
            final int SKIP_SIZE = 12;

            InputStream inputStream = Channels.newInputStream(channel);
            OutputStream outputStream = Channels.newOutputStream(channel);
            // login
            outputStream.write(PacketUtil.generateLoginRequestPacket(roomId));
            outputStream.flush();
            // check login
            byte buf[] = new byte[BUF_SIZE];
            int count = inputStream.read(buf);
            String loginRet = null;

            if (count > SKIP_SIZE) {
                // login success
                byte[] dataBody = new byte[count - SKIP_SIZE];
                System.arraycopy(buf, SKIP_SIZE, dataBody, 0, count - SKIP_SIZE);
                loginRet = new String(dataBody);
            }
            if (loginRet == null || !loginRet.contains("type@=loginres/")) {
                // login failed
                logger.error("Login FAILED, channel is closing: {}", eventHandler);
                return false;
            }
            logger.debug("Login SUCCESS packet received: {}", eventHandler);
            // join group
            outputStream.write(PacketUtil.generateJoinGroupPacket(roomId, Constants.GROUP_ID));
            outputStream.flush();
            return true;
        }

        /**
         * 删除连接，同时归还闲置域名:端口
         */
        public void cleanupChannel(EventHandler eventHandler) {
            synchronized (roomListenedSet) {
                roomListenedSet.remove(eventHandler.getRoomId());
            }
            releasePermit(eventHandler.getSocketAddress());
            logger.debug("SelectionKey canceled: {}", eventHandler);
        }

        /**
         * 检查某房间是否在监听
         */
        public boolean contains(Integer roomId) {
            return roomListenedSet.contains(roomId);
        }

        /**
         * 注册连接
         */
        private void registerChannel(EventHandler eventHandler) {
            final SelectorThread targetThread = threadChooser.nextThread();
            targetThread.registerChannel(eventHandler);
        }

        /**
         * 申请连接许可
         */
        private InetSocketAddress acquirePermit() throws InterruptedException {
            return socketPermit.take();
        }

        /**
         * 归还连接许可
         */
        private void releasePermit(InetSocketAddress socketAddress) {
            socketPermit.add(socketAddress);
        }

        /**
         * 输出统计信息
         */
        public String getStatistics() {
            return "Host [" + host + "], tryConnect/" + tryConnect + ", failedConnect/" + failedConnect +
                    ", max/" + costMax + ", min/" + costMin + ", avg/" + costAvg +
                    ", used/" + (Constants.CONNECTION_LIMIT_PER_HOST * ports.length - socketPermit.size() - 1) +
                    ", left/" + (socketPermit.size() + 1) +
                    ", stopped/" + stopped;
        }

        /**
         * 判断是否应该停用该Host
         * 如果失败率大于30%，则将该IP停用数小时
         */
        public void checkHost() {
            long total = tryConnect - tryConnectLast;
            long failed = failedConnect - failedConnectLast;
            if (total > 0 && failed > 0 && (double) failed / total >= 0.3) {
                stopped = true;
            }
            tryConnectLast = tryConnect;
            failedConnectLast = failedConnect;
        }
    }

    /**
     * 消息处理线程
     */
    class SelectorThread extends Thread {

        private final int SELECT_WAIT_TIME = 10 * 1000;
        private final int EMPTY_SELECT_MIN_COUNT = 512;
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private volatile Selector selector;
        private int selectReturnsImmediately = 0;

        // 待办操作列表
        private ConcurrentLinkedQueue<OpsRequest> opsRequests = new ConcurrentLinkedQueue<>();

        /**
         * 客户端初始化
         */
        SelectorThread() {
            setName(getClass().getSimpleName() + "-" + index++);
            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // 遍历并处理全部事件
                    select();
                    handleOpsRequests();
                }
                for (SelectionKey selectionKey : selector.keys()) {
                    cleanupSelectionKey(selectionKey);
                }
            } catch (Throwable t) {
                logger.error("run() on SelectorThread exiting due to uncaught error, {}", t);
            } finally {
                try {
                    selector.close();
                } catch (IOException e) {
                    logger.error("Got an IOException while closing selector, {}", e);
                }
            }
            logger.error("{} has exited.", Thread.currentThread().getName());
        }

        private void select() {
            doSelect();

            // process the io events we received
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                EventHandler eventHandler = (EventHandler) key.attachment();
                // 清除已处理的通道
                keyIterator.remove();

                if (!key.isValid()) {
                    cleanupSelectionKey(key);
                    continue;
                }
                try {
                    int oldOps = key.interestOps();
                    int ops = oldOps;
                    if (key.isReadable()) {
                        // 读事件处理方法
                        if (!eventHandler.read(key)) {
                            cleanupSelectionKey(key);
                        }
                    } else if (key.isWritable()) {
                        // 写事件处理方法
                        if (!eventHandler.write(key)) {
                            cleanupSelectionKey(key);
                        }
                        // 注册读事件
                        ops = SelectionKey.OP_READ;
                    }
                    if (ops != oldOps) {
                        interestOps(key, ops);
                    }
                } catch (RuntimeException e) {
                    logger.error("RuntimeException occurred: {}, {}", eventHandler, e);
                    cleanupSelectionKey(key);
                }
            }
        }

        private void doSelect() {
            Selector selector = this.selector;
            try {
                long beforeSelect = System.currentTimeMillis();
                int selectedKeys = selector.select(SELECT_WAIT_TIME);

                if (selectedKeys > 0) {
                    // - Selected something,
                    selectReturnsImmediately = 1;
                    return;
                }

                long timeBlocked = System.currentTimeMillis() - beforeSelect;
                if (timeBlocked < SELECT_WAIT_TIME) {
                    // returned before the SELECT_WAIT_TIME elapsed with nothing select.
                    // this may be the cause of the jdk epoll(..) bug, so increment the counter
                    // which we use later to see if its really the jdk bug.
                    selectReturnsImmediately++;
                } else if (selectReturnsImmediately >= EMPTY_SELECT_MIN_COUNT) {
                    // The selector returned immediately for EMPTY_SELECT_MIN_COUNT times in a row,
                    // so recreate one selector as it seems like we hit the
                    // famous epoll(..) jdk bug.
                    rebuildSelector();
                    selector = this.selector;

                    // try to select again
                    selector.selectNow();
                    selectReturnsImmediately = 1;
                }
            } catch (CancelledKeyException e) {
                // Harmless exception - log anyway
                logger.error("CancelledKeyException raised by a Selector - JDK bug?, {}", e);
            } catch (IOException e) {
                logger.error("Got an IOException while selecting, {}", e);
            }
        }

        private void handleOpsRequests() {
            // 遍历并处理全部事件
            OpsRequest request;
            while ((request = opsRequests.poll()) != null) {
                switch (request.type) {
                    case OpsRequest.CHG:
                        // 变更
                        SelectionKey key0 = request.channel.keyFor(selector);
                        if (key0.isValid()) {
                            key0.interestOps(request.ops);
                        }
                        break;
                    case OpsRequest.REG:
                        // 注册
                        try {
                            request.channel.register(selector, request.ops, request.eventHandler);
                        } catch (ClosedChannelException e) {
                            logger.error(e.getLocalizedMessage());
                        }
                        break;
                }
            }
        }

        /**
         * Create a new selector and "transfer" all channels from the old
         * selector to the new one
         */
        private void rebuildSelector() throws IOException {
            final Selector oldSelector = this.selector;
            final Selector newSelector = Selector.open();
            if (oldSelector == null) {
                return;
            }

            // loop over all the keys that are registered with the old Selector
            // and register them with the new one
            int nChannels = 0;
            for (; ; ) {
                try {
                    for (SelectionKey key : oldSelector.keys()) {
                        if (!key.isValid() || key.channel().keyFor(newSelector) != null) {
                            return;
                        }
                        // cancel the old key
                        EventHandler eventHandler = (EventHandler) key.attachment();
                        try {
                            int interestOps = key.interestOps();
                            key.cancel();
                            key.channel().register(newSelector, interestOps, eventHandler);
                            nChannels++;
                        } catch (Exception e) {
                            logger.error("Failed to re-register a Channel to the new Selector, {}", e);
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    // Probably due to concurrent modification of the key set.
                    continue;
                }
                break;
            }

            this.selector = newSelector;
            try {
                oldSelector.close();
            } catch (Exception t) {
                logger.error("FAILED to close the old selector, {}", t);
            }

            logger.error("Migrated " + nChannels + " channel(s) to the new Selector.");
            logger.error("Clazz - oldSelector: {}, newSelector: {}", oldSelector, newSelector);
            logger.error("Recreated Selector because of possible jdk epoll(..) bug.");
        }

        /**
         * 注册事件
         */
        private void interestOps(SelectionKey key, int ops) {
            opsRequests.offer(new OpsRequest((SocketChannel) key.channel(), OpsRequest.CHG, ops, null));
        }

        private void cleanupSelectionKey(SelectionKey key) {
            try {
                key.cancel();
                key.channel().close();
            } catch (IOException ignored) {
            }
            EventHandler eventHandler = (EventHandler) key.attachment();
            eventHandler.close();
        }

        /**
         * 获取当前SelectionKey数量
         */
        int getSelectionKeySize() {
            return selector.keys().size();
        }

        /**
         * 注册连接
         */
        void registerChannel(EventHandler eventHandler) {
            // 提交到队列
            opsRequests.offer(new OpsRequest(eventHandler.getSocketChannel(), OpsRequest.REG,
                    SelectionKey.OP_READ, eventHandler));
        }

        /**
         * 通知轮训器注册写操作
         */
        void interestWriteOpsAll() {
            if (selector.isOpen()) {
                selector.keys().forEach(key -> interestOps(key, SelectionKey.OP_WRITE));
            }
        }

        /**
         * 连接注册事件
         */
        final class OpsRequest {
            /**
             * 注册事件
             */
            static final int REG = 1;

            /**
             * 改变事件
             */
            static final int CHG = 2;

            /**
             * 操作类型（注册/修改）
             */
            final int type;

            /**
             * 注册事件：读/写
             */
            final int ops;

            /**
             * 连接
             */
            final SocketChannel channel;

            /**
             * 附加信息
             */
            EventHandler eventHandler;

            OpsRequest(SocketChannel channel, int type, int ops, EventHandler eventHandler) {
                this.channel = channel;
                this.type = type;
                this.ops = ops;
                this.eventHandler = eventHandler;
            }
        }
    }
}
