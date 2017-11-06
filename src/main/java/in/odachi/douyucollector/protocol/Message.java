package in.odachi.douyucollector.protocol;

import java.time.LocalDateTime;

/**
 * 消息实体
 */
public class Message {

    protected String message;

    protected LocalDateTime dateTime;

    protected String type;

    private Message() {
    }

    public Message(String messageStr) {
        this.message = messageStr;
        this.dateTime = LocalDateTime.now();
    }

    public Message(Message message) {
        this.message = message.message;
        this.dateTime = message.dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "rtime@=" + dateTime + "/" + message;
    }
}
