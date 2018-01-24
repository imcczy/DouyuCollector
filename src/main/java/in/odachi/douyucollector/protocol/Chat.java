package in.odachi.douyucollector.protocol;

import in.odachi.douyucollector.common.util.NumberUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 弹幕消息
 */
public class Chat extends Message {

    /**
     * 弹幕组Id
     */
    private String gid;

    /**
     * 房间Id
     */
    private Integer rid;

    /**
     * 发送者Id
     */
    private Integer uid;

    /**
     * 发送者昵称
     */
    private String nn;

    /**
     * 弹幕文本内容
     */
    private String txt;

    /**
     * 弹幕唯一Id
     */
    private String cid;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 礼物头衔：默认值 0（表示没有头衔）
     */
    private String gt;

    /**
     * 颜色：默认值 0（表示默认颜色弹幕）
     */
    private String col;

    /**
     * 客户端类型：默认值 0（表示 web 用户）
     */
    private String ct;

    /**
     * 房间权限组：默认值 1（表示普通权限用户）
     */
    private String rg;

    /**
     * 平台权限组：默认值 1（表示普通权限用户）
     */
    private String pg;

    /**
     * 酬勤等级：默认值 0（表示没有酬勤）
     */
    private Integer dlv;

    /**
     * 酬勤数量：默认值 0（表示没有酬勤数量）
     */
    private Integer dc;

    /**
     * 最高酬勤等级：默认值 0（表示全站都没有酬勤）
     */
    private Integer bdlv;

    public Chat(Message message, Map<String, String> obj) {
        super(message);
        this.type = obj.get("type");
        this.gid = obj.get("gid");
        this.rid = NumberUtil.parseInt(obj.get("rid"));
        this.uid = NumberUtil.parseInt(obj.get("uid"));
        this.nn = obj.get("nn");
        this.txt = obj.get("txt");
        this.cid = obj.get("cid");
        this.level = NumberUtil.parseInt(obj.get("level"));
        this.gt = obj.get("gt");
        this.col = obj.get("col");
        this.ct = obj.get("ct");
        this.rg = obj.get("rg");
        this.ct = obj.get("ct");
        this.pg = obj.get("pg");
        this.dlv = NumberUtil.parseInt(obj.get("dlv"));
        this.dc = NumberUtil.parseInt(obj.get("dc"));
        this.bdlv = NumberUtil.parseInt(obj.get("bdlv"));
    }

    public String getGid() {
        return gid;
    }

    public Integer getRid() {
        return rid;
    }

    public Integer getUid() {
        return uid;
    }

    public String getNn() {
        return nn;
    }

    public String getTxt() {
        return txt;
    }

    public String getCid() {
        return cid;
    }

    public Integer getLevel() {
        return level;
    }

    public String getGt() {
        return gt;
    }

    public String getCol() {
        return col;
    }

    public String getCt() {
        return ct;
    }

    public String getRg() {
        return rg;
    }

    public String getPg() {
        return pg;
    }

    public Integer getDlv() {
        return dlv;
    }

    public Integer getDc() {
        return dc;
    }

    public Integer getBdlv() {
        return bdlv;
    }

    @Override
    public String toString() {
        return this.rid + "," +
                this.uid + "," +
                LocalDateTime.now() + "," +
                this.nn + "," +
                this.level + "," +
                this.txt;
    }
}
