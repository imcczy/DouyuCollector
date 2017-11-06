package in.odachi.douyucollector.consumer;

import in.odachi.douyucollector.protocol.Message;

public interface MessageProcessor {

    /**
     * 处理消息
     */
    void process(Message message);
}
