package in.odachi.douyucollector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import in.odachi.douyucollector.database.DBUtility;
import in.odachi.douyucollector.database.entity.Gift;
import in.odachi.douyucollector.database.entity.Log;
import in.odachi.douyucollector.database.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public enum LCache {
    // 单例模式
    instance;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DBUtility dbUtility = DBUtility.instance;

    /**
     * 未找到的礼物
     */
    private final Set<Integer> giftNotFound = new HashSet<>();

    private Map<Integer, Gift> gc = new ConcurrentHashMap<>();

    private Cache<Integer, Boolean> rc = CacheBuilder.newBuilder().expireAfterWrite(6, TimeUnit.HOURS).build();

    public Gift getGift(Integer id) {
        return gc.computeIfAbsent(id, dbUtility::queryGift);
    }

    /**
     * 当缓存中没有的时候更新数据库
     */
    public void putGift(Gift g) {
        if (!gc.containsKey(g.getId())) {
            dbUtility.insertGift(g);
            gc.put(g.getId(), g);
            synchronized (giftNotFound) {
                if (giftNotFound.remove(g.getId())) {
                    logger.info("Gift removed from giftNotFound set: {}", g.getId());
                }
            }
        }
    }

    /**
     * 当缓存中没有的时候更新数据库
     * 房间信息默认每6小时更新一次
     */
    public void putRoom(Room r) {
        if (rc.getIfPresent(r.getRoomId()) == null) {
            dbUtility.insertRoom(r);
            rc.put(r.getRoomId(), Boolean.TRUE);
        }
    }

    /**
     * 礼物未找到
     */
    public boolean setGiftNotFound(Integer gid) {
        synchronized (giftNotFound) {
            return giftNotFound.add(gid);
        }
    }
}
