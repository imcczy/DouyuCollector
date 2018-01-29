package in.odachi.douyucollector.database;

import in.odachi.douyucollector.common.util.ConfigUtil;
import in.odachi.douyucollector.database.entity.Category;
import in.odachi.douyucollector.database.entity.Gift;
import in.odachi.douyucollector.database.entity.Log;
import in.odachi.douyucollector.database.entity.Room;
import in.odachi.douyucollector.protocol.Chat;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public enum DBUtility {
    // 单例模式
    instance;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BasicDataSource dataSource;

    DBUtility() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(ConfigUtil.getMysqlJdbcUrl());
        ds.setUsername(ConfigUtil.getMysqlUsername());
        ds.setPassword(ConfigUtil.getMysqlPassword());
        ds.setMinIdle(1);
        ds.setMaxIdle(20);
        ds.setMaxOpenPreparedStatements(100);
        dataSource = ds;
    }

    public static void main(String[] args) {
    }

    public Gift queryGift(Integer id) {
        try {
            QueryRunner run = new QueryRunner(dataSource);
            RowProcessor processor = new BasicRowProcessor(new GenerousBeanProcessor());
            return run.query(DBStatement.QUERY_GIFT_BY_ID, new BeanHandler<>(Gift.class, processor), id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    public void insertGift(Gift g) {
        try {
            QueryRunner run = new QueryRunner(dataSource);
            run.update(DBStatement.INSERT_GIFT,
                    g.getId(),
                    g.getName(),
                    g.getType(),
                    g.getPc(),
                    g.getGx(),
                    g.getDesc(),
                    g.getIntro(),
                    g.getMimg(),
                    g.getHimg(),
                    LocalDateTime.now()
            );
            logger.debug("Database insert: " + g);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void insertRoom(Room r) {
        try {
            QueryRunner run = new QueryRunner(dataSource);
            run.update(DBStatement.INSERT_ROOM,
                    r.getRoomId(),
                    r.getCateId(),
                    r.getRoomName(),
                    r.getOwnerName(),
                    r.getOwnerWeight(),
                    r.getFansNum(),
                    r.getRoomThumb(),
                    LocalDateTime.now(),
                    r.getRoomName(),
                    r.getOwnerWeight(),
                    r.getFansNum(),
                    r.getRoomThumb(),
                    LocalDateTime.now()
            );
            logger.debug("Database insert: " + r);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void insertCategory(Category c) {
        try {
            QueryRunner run = new QueryRunner(dataSource);
            run.update(DBStatement.INSERT_CATEGORY,
                    c.getCate1Id(),
                    c.getCate1Name(),
                    c.getCate1ShortName(),
                    c.getCate2Id(),
                    c.getCate2Name(),
                    c.getCate2ShortName(),
                    c.getPic(),
                    c.getIcon(),
                    c.getSmallIcon(),
                    c.getCount(),
                    LocalDateTime.now()
            );
            logger.debug("Database insert: " + c);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void log(List<Log> logList) {
        final int ENTITY_SIZE = 4;
        Object[][] params = new Object[logList.size()][ENTITY_SIZE];
        for (int i = 0; i < logList.size(); i++) {
            Log log = logList.get(i);
            params[i][0] = LocalDateTime.now();
            params[i][1] = log.getModule();
            params[i][2] = log.getLevel();
            params[i][3] = log.getRid();
            params[i][4] = log.getMessage();
        }
        try {
            QueryRunner run = new QueryRunner(dataSource);
            run.batch(DBStatement.INSERT_LOG, params);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void log(Log log) {
        try {
            QueryRunner run = new QueryRunner(dataSource);
            run.update(DBStatement.INSERT_LOG,
                    LocalDateTime.now(),
                    log.getModule(),
                    log.getLevel(),
                    log.getRid(),
                    log.getMessage());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
    public void danmu(Chat chat){
        try {
            QueryRunner run = new QueryRunner(dataSource);
            run.update(DBStatement.INSERT_DANMU,
                    chat.getRid(),
                    chat.getUid(),
                    LocalDateTime.now(),
                    chat.getNn(),
                    chat.getLevel(),
                    chat.getTxt()
                    );
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}
