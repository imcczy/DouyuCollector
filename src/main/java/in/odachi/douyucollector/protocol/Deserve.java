package in.odachi.douyucollector.protocol;

import in.odachi.douyucollector.common.util.NumberUtil;

import java.util.Map;

/**
 * 用户赠送酬勤通知消息
 */
public class Deserve extends Message {

    /**
     * 房间Id
     */
    private Integer rid;

    /**
     * 弹幕组Id
     */
    private String gid;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 赠送数量
     */
    private Integer cnt;

    /**
     * 赠送连击次数
     */
    private Integer hits;

    /**
     * 酬勤等级
     */
    private Integer lev;

    /**
     * 用户信息序列化字符串，详见下文。注意，此处为嵌套序列化，需注意符号的转义变换。
     */
    private Object sui;

    public Deserve(Message message, Map<String, String> obj) {
        super(message);
        this.type = obj.get("type");
        this.rid = NumberUtil.parseInt(obj.get("rid"));
        this.gid = obj.get("gid");
        this.level = NumberUtil.parseInt(obj.get("level"));
        this.cnt = NumberUtil.parseInt(obj.get("cnt"));
        this.hits = NumberUtil.parseInt(obj.get("hits"));
        this.lev = NumberUtil.parseInt(obj.get("lev"));
        this.sui = obj.get("sui");
    }

    public Integer getRid() {
        return rid;
    }

    public String getGid() {
        return gid;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getCnt() {
        return cnt;
    }

    public Integer getHits() {
        return hits;
    }

    public Integer getLev() {
        return lev;
    }

    public Object getSui() {
        return sui;
    }
}
