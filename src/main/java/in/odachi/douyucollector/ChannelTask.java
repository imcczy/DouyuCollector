package in.odachi.douyucollector;

public class ChannelTask {

    private Integer roomId;
    private int failedTimes;

    public ChannelTask(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public int addFailedTimes() {
        return ++failedTimes;
    }

    public int getFailedTimes() {
        return failedTimes;
    }
}
