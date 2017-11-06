package in.odachi.douyucollector.protocol;

import in.odachi.douyucollector.common.util.NumberUtil;

import java.util.Map;

/**
 * 赠送礼物消息
 */
public class Dgb extends Message {

    /**
     * 房间Id
     */
    private Integer rid;

    /**
     * 弹幕分组Id
     */
    private String gid;

    /**
     * 礼物Id
     */
    private Integer gfid;

    /**
     * 礼物显示样式
     */
    private String gs;

    /**
     * 用户Id
     */
    private Integer uid;

    /**
     * 用户昵称
     */
    private String nn;

    /**
     * 用户战斗力
     */
    private String str;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 主播体重
     */
    private String dw;

    /**
     * 礼物个数：默认值 1（表示 1 个礼物）
     */
    private Integer gfcnt;

    /**
     * 礼物连击次数：默认值 1（表示 1 连击）
     */
    private Integer hits;

    /**
     * 酬勤头衔：默认值 0（表示没有酬勤）
     */
    private String dlv;

    /**
     * 酬勤个数：默认值 0（表示没有酬勤数量）
     */
    private Integer dc;

    /**
     * 全站最高酬勤等级：默认值 0（表示全站都没有酬勤）
     */
    private Integer bdl;

    /**
     * 房间身份组：默认值 1（表示普通权限用户）
     */
    private String rg;

    /**
     * 平台身份组：默认值 1（表示普通权限用户）
     */
    private String pg;

    /**
     * 红包 id：默认值 0（表示没有红包）
     */
    private String rpid;

    /**
     * 红包开启剩余时间：默认值 0（表示没有红包）
     */
    private String slt;

    /**
     * 红包销毁剩余时间：默认值 0（表示没有红包）
     */
    private String elt;

    public Dgb(Message message, Map<String, String> obj) {
        super(message);
        this.type = obj.get("type");
        this.rid = NumberUtil.parseInt(obj.get("rid"));
        this.gid = obj.get("gid");
        this.gfid = NumberUtil.parseInt(obj.get("gfid"));
        this.gs = obj.get("gs");
        this.uid = NumberUtil.parseInt(obj.get("uid"));
        this.nn = obj.get("nn");
        this.str = obj.get("str");
        this.level = NumberUtil.parseInt(obj.get("level"));
        this.dw = obj.get("dw");
        this.gfcnt = NumberUtil.parseInt(obj.get("gfcnt"));
        this.hits = NumberUtil.parseInt(obj.get("hits"));
        this.dlv = obj.get("dlv");
        this.dc = NumberUtil.parseInt(obj.get("dc"));
        this.bdl = NumberUtil.parseInt(obj.get("bdl"));
        this.rg = obj.get("rg");
        this.pg = obj.get("pg");
        this.rpid = obj.get("rpid");
        this.slt = obj.get("slt");
        this.elt = obj.get("elt");
    }

    public Integer getRid() {
        return rid;
    }

    public String getGid() {
        return gid;
    }

    public Integer getGfid() {
        return gfid;
    }

    public String getGs() {
        return gs;
    }

    public Integer getUid() {
        return uid;
    }

    public String getNn() {
        return nn;
    }

    public String getStr() {
        return str;
    }

    public Integer getLevel() {
        return level;
    }

    public String getDw() {
        return dw;
    }

    public Integer getGfcnt() {
        return gfcnt;
    }

    public Integer getHits() {
        return hits;
    }

    public String getDlv() {
        return dlv;
    }

    public Integer getDc() {
        return dc;
    }

    public Integer getBdl() {
        return bdl;
    }

    public String getRg() {
        return rg;
    }

    public String getPg() {
        return pg;
    }

    public String getRpid() {
        return rpid;
    }

    public String getSlt() {
        return slt;
    }

    public String getElt() {
        return elt;
    }
}
