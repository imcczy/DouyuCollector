package in.odachi.douyucollector.protocol;

import in.odachi.douyucollector.common.util.NumberUtil;

import java.util.Map;

/**
 * 超级弹幕消息
 */
public class Ssd extends Message {

    /**
     * 房间Id
     */
    private Integer rid;

    /**
     * 弹幕组Id
     */
    private String gid;

    /**
     * 超级弹幕Id
     */
    private String sdid;

    /**
     * 跳转房间Id
     */
    private Integer trid;

    /**
     * 超级弹幕的内容
     */
    private String content;

    public Ssd(Message message, Map<String, String> obj) {
        super(message);
        this.type = obj.get("type");
        this.rid = NumberUtil.parseInt(obj.get("rid"));
        this.gid = obj.get("gid");
        this.sdid = obj.get("sdid");
        this.trid = NumberUtil.parseInt(obj.get("trid"));
        this.content = obj.get("content");
    }

    public Integer getRid() {
        return rid;
    }

    public String getGid() {
        return gid;
    }

    public String getSdid() {
        return sdid;
    }

    public Integer getTrid() {
        return trid;
    }

    public String getContent() {
        return content;
    }
}
