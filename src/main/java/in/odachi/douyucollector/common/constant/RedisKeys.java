package in.odachi.douyucollector.common.constant;

/**
 * 常量类
 */
public abstract class RedisKeys {

    /**
     * 待监听的房间
     */
    public static final String SYSTEM_ROOM_QUEUE = "douyu:system:room:queue";

    /**
     * 消息队列
     */
    public static final String SYSTEM_MESSAGE_QUEUE = "douyu:system:message:queue";

    /**
     * 消息处理速率
     */
    public static final String SYSTEM_MESSAGE_PROCESSED_RATE = "douyu:system:message:processed:rate";

    /**
     * 房间在线人气峰值
     * {0} -> roomId
     * %s -> date
     */
    public static final String ONLINE_PEAK = "douyu:online:peak:%s:%s";

    /**
     * 房间在播累计时长
     * %s -> roomId
     * %s -> date
     */
    public static final String ONLINE_TOTAL = "douyu:online:total:%s:%s";

    /**
     * 主播详细数据：弹幕/礼物数量，人次，价值等
     * %s -> chat/gift
     * %s -> count/user/price
     * %s -> roomId
     * %s -> min5/hour1/date
     */
    public static final String ROOM_DETAIL = "douyu:room:detail:%s:%s:%s:%s";

    /**
     * 房间的礼物详情
     * %s -> roomId
     * %s -> date
     */
    public static final String GIFT_DETAIL = "douyu:gift:detail:%s:%s";

    /**
     * 主播排行榜：弹幕/礼物数量，人次，价值等
     * %s -> chat/gift
     * %s -> count/user/price
     * %s -> min5/hour1/date
     */
    public static final String ROOM_RANK = "douyu:room:rank:%s:%s:%s";
}