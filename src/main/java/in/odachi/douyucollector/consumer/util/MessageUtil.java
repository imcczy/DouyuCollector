package in.odachi.douyucollector.consumer.util;

import in.odachi.douyucollector.protocol.*;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    /**
     * 解析消息
     */
    public static Message parseMessage(Message message) {
        Map<String, String> messageObj = parseMessage(message.getMessage());
        switch (messageObj.get("type")) {
            case "chatmsg":
                return new Chat(message, messageObj);
            case "bc_buy_deserve":
                return new Deserve(message, messageObj);
            case "dgb":
                return new Dgb(message, messageObj);
            case "rss":
                return new Rss(message, messageObj);
            case "ssd":
                return new Ssd(message, messageObj);
            default:
                return message;
        }
    }

    /**
     * 解析消息
     */
    private static Map<String, String> parseMessage(String messageStr) {
        Map<String, String> messageObj = new HashMap<>();
        // 处理数据字符串末尾的'/0字符'
        messageStr = StringUtils.substringBeforeLast(messageStr, "/");
        String[] buffList = messageStr.split("/");
        for (String buff : buffList) {
            String key = StringUtils.substringBefore(buff, "@=");
            String value = StringUtils.substringAfter(buff, "@=");
//            if (StringUtils.contains((String) value, "@A")) {
//                value = ((String) value).replaceAll("@S", "/").replaceAll("@A", "@");
//                value = parseMessage((String) value);
//            }
            messageObj.put(key, value);
        }
        return messageObj;
    }
}
