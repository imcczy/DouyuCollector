package in.odachi.douyucollector.common.constant;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

/**
 * 常量类
 */
public class Constants {

    // 获取在播房间
    public static final String HTTPS_M_DOUYU_COM_ROOMLISTS = "https://m.douyu.com/roomlists";

    // 获取所有分类
    public static final String HTTPS_M_DOUYU_COM_CATEGORY = "https://m.douyu.com/category";

    // 第三方查询API：获取直播房间详情信息
    public static final String HTTP_OPEN_DOUYUCDN_CN_API_ROOM_API_ROOM = "http://open.douyucdn.cn/api/RoomApi/room/";

    // 弹幕池分组号，海量模式使用-9999
    public static final int GROUP_ID = -9999;

    // 弹幕客户端类型设置
    public final static int MESSAGE_TYPE_CLIENT = 689;

    // 分隔符
    public static final String REGEX = ",";

    // 每个端口限制连接数
    public static final int CONNECTION_LIMIT_PER_HOST = 200;

    // 统计数据保留的历史天数
    public static final int REDIS_DATA_KEEP_DAYS = 32;

    // 消息实时处理速率数据保留分钟数
    public static final int PROCESSED_RATE_KEEP_SIZE = 576;

    // 消息实时处理速率间隔
    public static final int PROCESSED_RATE_REPORT_GAP = 5;

    // 统计线程休息时间
    public static final long WATCHER_SLEEP_TIME = 60 * 1000L;

    // 心跳线程休息时间
    public static final long KEEP_ALIVE_SLEEP_TIME = 45 * 1000L;

    // 配置文件路径
    public static final String CONF_DOUYU_PROPERTIES = "conf/douyu.properties";

    // 日期格式
    public static final String DATE_PATTERN = "yyyyMMdd";

    // 时间格式
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // 分钟格式
    public static final String MINUTE_PATTERN = "yyyy-MM-dd HH:mm";

    // HTTP相关
    public static abstract class Encoding {

        public static final String UTF_8 = "UTF-8";
    }

    public static abstract class Method {

        public static final String GET = HttpGet.METHOD_NAME;

        public static final String POST = HttpPost.METHOD_NAME;
    }

    public static abstract class ContentType {

        public static final String JSON = "application/json";

        public static final String XML = "text/xml";

        public static final String FORM = "application/x-www-form-urlencoded";

        public static final String MULTIPART = "multipart/form-data";
    }

    public static abstract class Header {

        public static final String USER_AGENT = "User-Agent";

        public static final String X_REQUESTED_WITH = "X-Requested-With";

        public static final String USER_AGENT_PAD = "Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 " +
                "(KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
    }
}