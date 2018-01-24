package in.odachi.douyucollector.database;

import in.odachi.douyucollector.database.entity.Log;
import in.odachi.douyucollector.protocol.Chat;

public class Log2DB {

    // private static final int BATCH_SIZE = 100;

    private static Log2DB instance = new Log2DB();

    private DBUtility dbUtility = DBUtility.instance;

    // private ExecutorService invoker = Executors.newFixedThreadPool(1);

    private Log2DB() {
    }

    public static Log2DB getLog() {
        return instance;
    }

    public void log(Log log) {
        dbUtility.log(log);
    }
    public void danmu(Chat c){dbUtility.danmu(c);}
}
