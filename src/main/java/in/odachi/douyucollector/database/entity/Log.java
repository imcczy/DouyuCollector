package in.odachi.douyucollector.database.entity;

public class Log implements Entity {

    private static abstract class Module {
        /**
         * 爬虫模块
         */
        private static final String CRAWLER = "crawler";
        /**
         * 生产者模块
         */
        private static final String PRODUCER = "producer";
        /**
         * 消费者模块
         */
        private static final String CONSUMER = "consumer";
    }

    private static abstract class Level {

        private static final String DEBUG = "debug";

        private static final String INFO = "info";

        private static final String WARN = "warn";

        private static final String ERROR = "error";
    }

    private Integer id;

    private String time;

    private String module = Module.CRAWLER;

    private String level = Level.DEBUG;

    private String rid;

    private String message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // builder
    public Log crawler() {
        this.module = Module.CRAWLER;
        return this;
    }

    public Log producer() {
        this.module = Module.PRODUCER;
        return this;
    }

    public Log consumer() {
        this.module = Module.CONSUMER;
        return this;
    }

    public Log debug() {
        this.level = Level.DEBUG;
        return this;
    }

    public Log info() {
        this.level = Level.INFO;
        return this;
    }

    public Log warn() {
        this.level = Level.WARN;
        return this;
    }

    public Log error() {
        this.level = Level.ERROR;
        return this;
    }

    public Log rid(String rid) {
        this.rid = rid;
        return this;
    }

    public Log message(String message) {
        this.message = message;
        return this;
    }
}
