package in.odachi.douyucollector.database;

abstract class DBStatement {

    /**
     * 房间插入
     */
    static final String INSERT_ROOM = "insert ignore into room (`room_id`,`cate_id`,`room_name`," +
            "`owner_name`,`owner_weight`,`fans_num`,`room_thumb`,`update_time`) values(?,?,?,?,?,?,?,?) " +
            "on duplicate key update `room_name`=?, `owner_weight`=?, `fans_num`=?, `room_thumb`=?, `update_time`=?";

    /**
     * 礼物插入
     */
    static final String INSERT_GIFT = "insert ignore into gift (`id`,`name`,`type`,`pc`," +
            "`gx`,`desc`,`intro`,`mimg`,`himg`,`update_time`) values(?,?,?,?,?,?,?,?,?,?)";

    /**
     * 目录插入
     */
    static final String INSERT_CATEGORY = "insert ignore into category (`cate1_id`,`cate1_name`,`cate1_short_name`," +
            "`cate2_id`,`cate2_name`,`cate2_short_name`,`pic`,`icon`,`small_icon`,`count`,`update_time`) " +
            "values(?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * 礼物查询
     */
    static final String QUERY_GIFT_BY_ID = "select * from gift where `id`=?";

    /**
     * 日志插入
     */
    static final String INSERT_LOG = "insert into log (`time`,`module`,`level`,`rid`,`message`) values(?,?,?,?,?)";
    /**
     * 弹幕插入
     */
    static final String INSERT_DANMU = "insert into danmu (`room_id`,`user_id`,`time`,`user_name`,`level`,`text`) values(?,?,?,?,?,?)";

}
