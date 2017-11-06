package in.odachi.douyucollector.protocol;

import in.odachi.douyucollector.common.util.NumberUtil;

import java.util.Map;

/**
 * 房间开关播提醒
 */
public class Rss extends Message {

    /**
     * 房间Id
     */
    private Integer rid;

    /**
     * 弹幕组Id
     */
    private String gid;

    /**
     * 直播状态，0-没有直播，1-正在直播
     */
    private String ss;

    /**
     * 类型
     */
    private String code;

    /**
     * 开关播原因：0-主播开关播，其他值-其他原因
     */
    private String rt;

    /**
     * 通知类型
     */
    private String notify;

    /**
     * 关播时间（仅关播时有效）
     */
    private String endtime;

    public Rss(Message message, Map<String, String> obj) {
        super(message);
        this.type = obj.get("type");
        this.rid = NumberUtil.parseInt(obj.get("rid"));
        this.gid = obj.get("gid");
        this.ss = obj.get("ss");
        this.code = obj.get("code");
        this.rt = obj.get("rt");
        this.notify = obj.get("notify");
        this.endtime = obj.get("endtime");
    }

    public Integer getRid() {
        return rid;
    }

    public String getGid() {
        return gid;
    }

    public String getSs() {
        return ss;
    }

    public String getCode() {
        return code;
    }

    public String getRt() {
        return rt;
    }

    public String getNotify() {
        return notify;
    }

    public String getEndtime() {
        return endtime;
    }
}
