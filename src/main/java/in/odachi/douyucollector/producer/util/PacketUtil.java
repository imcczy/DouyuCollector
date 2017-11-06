package in.odachi.douyucollector.producer.util;

import in.odachi.douyucollector.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 斗鱼弹幕协议信息封装类
 */
public class PacketUtil {

    /**
     * 生成登录请求数据包
     */
    public static byte[] generateLoginRequestPacket(int roomId) {
        DyPacket packet = new DyPacket()
                .addItem("type", "loginreq")
                .addItem("roomid", roomId);
        return packet.format();
    }

    /**
     * 生成加入弹幕分组池数据包
     */
    public static byte[] generateJoinGroupPacket(int roomId, int groupId) {
        DyPacket packet = new DyPacket()
                .addItem("type", "joingroup")
                .addItem("rid", roomId)
                .addItem("gid", groupId);
        return packet.format();
    }

    /**
     * 生成心跳协议数据包
     * v1.6.2 type@=mrkl/
     */
    public static byte[] generateKeepAlivePacket() {
        DyPacket packet = new DyPacket()
                .addItem("type", "mrkl");
        return packet.format();
    }

    public static byte[] toLH(int n) {
        final byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static int LHtoI(byte[] b) {
        return b[0] & 0xFF | (b[1] & 0xFF) << 8
                | (b[2] & 0xFF) << 16
                | (b[3] & 0xFF) << 24;
    }

    // 数据包构造类
    private static class DyPacket {

        private final StringBuilder builder = new StringBuilder();
        private final Logger logger = LoggerFactory.getLogger(getClass());

        /**
         * 返回弹幕协议格式化后的结果
         */
        private byte[] format() {
            // 数据包末尾必须以'\0'结尾
            String data = builder.append('\0').toString();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                byteArrayOutputStream.reset();
                // 加上消息头
                // 4 bytes packet length
                outputStream.write(toLH(data.length() + 8), 0, 4);
                // 4 bytes packet length
                outputStream.write(toLH(data.length() + 8), 0, 4);
                // 2 bytes message type
                outputStream.write(toLH(Constants.MESSAGE_TYPE_CLIENT), 0, 2);
                // 1 bytes encrypt
                outputStream.writeByte(0);
                // 1 bytes reserve
                outputStream.writeByte(0);
                // append data
                outputStream.writeBytes(data);
            } catch (RuntimeException | IOException e) {
                logger.error(e.getLocalizedMessage());
            }
            return byteArrayOutputStream.toByteArray();
        }

        /**
         * 根据斗鱼弹幕协议进行相应的编码处理
         */
        private DyPacket addItem(String key, Object value) {
            builder.append(key.replaceAll("/", "@S").replaceAll("@", "@A"));
            builder.append("@=");
            if (value instanceof String) {
                builder.append(((String) value).replaceAll("/", "@S").replaceAll("@", "@A"));
            } else if (value instanceof Integer) {
                builder.append(value);
            }
            builder.append("/");
            return this;
        }
    }
}
