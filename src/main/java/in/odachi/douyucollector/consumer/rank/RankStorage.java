package in.odachi.douyucollector.consumer.rank;

import in.odachi.douyucollector.LCache;
import in.odachi.douyucollector.RedissonStorage;
import in.odachi.douyucollector.common.constant.Constants;
import in.odachi.douyucollector.database.entity.Gift;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public enum RankStorage {
    // 单例模式
    instance;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN);

    // 5分钟排行榜：弹幕数量
    private RankSlot mcm5 = new RankSlot(5);

    // 60分钟排行榜：弹幕数量
    private RankSlot mch1 = new RankSlot(60);

    // 5分钟排行榜：弹幕人次
    private RankSlot mum5 = new RankSlot(5);

    // 60分钟排行榜：弹幕人次
    private RankSlot muh1 = new RankSlot(60);

    // 5分钟排行榜：礼物数量
    private RankSlot gcm5 = new RankSlot(5);

    // 60分钟排行榜：礼物数量
    private RankSlot gch1 = new RankSlot(60);

    // 5分钟排行榜：礼物人次
    private RankSlot gum5 = new RankSlot(5);

    // 60分钟排行榜：礼物人次
    private RankSlot guh1 = new RankSlot(60);

    // 5分钟排行榜：礼物价值
    private RankSlot gpm5 = new RankSlot(5);

    // 60分钟排行榜：礼物价值
    private RankSlot gph1 = new RankSlot(60);

    /**
     * 统计某个时间段的消息数量
     */
    public void computeMsgCount(Map<Integer, Double> mcm) {
        Map<Integer, Double> map = new HashMap<>();
        mcm.keySet().forEach(roomId -> map.put(roomId, mcm.replace(roomId, 0d)));
        mcm5.putIntoSlots(map);
        mch1.putIntoSlots(map);
        top(gpm5, "chat", "count", "min5");
        top(gph1, "chat", "count", "hour1");
        topAndDaily(map, "chat", "count");
    }

    /**
     * 统计某个时间段的消息人次
     */
    public void computeMsgUser(Map<Integer, Set<Integer>> mum) {
        Map<Integer, Double> map = new HashMap<>();
        mum.keySet().forEach(roomId -> map.put(roomId, (double) mum.replace(roomId, new HashSet<>()).size()));
        mum5.putIntoSlots(map);
        muh1.putIntoSlots(map);
        top(gpm5, "chat", "user", "min5");
        top(gph1, "chat", "user", "hour1");
        topAndDaily(map, "chat", "user");
    }

    /**
     * 统计某个时间段的礼物数量
     */
    public void computeGiftCount(Map<Integer, Double> gcm) {
        Map<Integer, Double> map = new HashMap<>();
        gcm.keySet().forEach(roomId -> map.put(roomId, gcm.replace(roomId, 0d)));
        gcm5.putIntoSlots(map);
        gch1.putIntoSlots(map);
        top(gpm5, "gift", "count", "min5");
        top(gph1, "gift", "count", "hour1");
        topAndDaily(map, "gift", "count");
    }

    /**
     * 统计某个时间段的礼物人次
     */
    public void computeGiftUser(Map<Integer, Set<Integer>> gum) {
        Map<Integer, Double> map = new HashMap<>();
        gum.keySet().forEach(roomId -> map.put(roomId, (double) gum.replace(roomId, new HashSet<>()).size()));
        gum5.putIntoSlots(map);
        guh1.putIntoSlots(map);
        top(gpm5, "gift", "user", "min5");
        top(gph1, "gift", "user", "hour1");
        topAndDaily(map, "gift", "user");
    }

    /**
     * 统计某个时间段的礼物价值
     */
    public void computeGiftPrice(Map<Integer, Map<Integer, Integer>> gtm) {
        String today = LocalDate.now().format(dateFormatter);
        Map<Integer, Double> map = new HashMap<>();
        gtm.keySet().forEach(roomId -> {
            Map<Integer, Integer> gts = gtm.replace(roomId, new ConcurrentHashMap<>());
            RMap<Integer, Integer> gds = RedissonStorage.instance.giftDetail(roomId, today);
            gds.expire(Constants.REDIS_DATA_KEEP_DAYS, TimeUnit.DAYS);
            gts.forEach((gid, count) -> {
                gds.compute(gid, (k, v) -> v == null ? count : v + count);
                Gift gift = LCache.instance.getGift(gid);
                if (gift == null) {
                    if (LCache.instance.setGiftNotFound(gid)) {
                        logger.error("Gift {} NOT found, roomId: {}", gid, roomId);
                    }
                } else {
                    Double price = gift.getPc() * (("2".equals(gift.getType())) ? 1 : 0.001);
                    map.compute(roomId, (k, v) -> v == null ? price : v + price);
                }
            });
        });
        gpm5.putIntoSlots(map);
        gph1.putIntoSlots(map);
        top(gpm5, "gift", "price", "min5");
        top(gph1, "gift", "price", "hour1");
        topAndDaily(map, "gift", "price");
    }

    /**
     * 缓存topN结果
     */
    private void top(RankSlot rankSlot, String msg, String type, String date) {
        RScoredSortedSet<Integer> rankSet = RedissonStorage.instance.roomRank(msg, type, date);
        rankSet.clear();
        rankSlot.querySlotSum().forEach((roomId, score) -> rankSet.add(score, roomId));
    }

    /**
     * 缓存topN结果和当天周期数据
     */
    private void topAndDaily(Map<Integer, Double> map, String msg, String type) {
        String today = LocalDate.now().format(dateFormatter);
        RScoredSortedSet<Integer> rankSet = RedissonStorage.instance.roomRank(msg, type, today);
        rankSet.clear();
        map.keySet().forEach(roomId -> {
            RBucket<Double> bucket = RedissonStorage.instance.roomDetail(msg, type, roomId, today);
            Double old = bucket.get();
            Double value = (old == null ? map.get(roomId) : old + map.get(roomId));
            rankSet.add(value, roomId);
            bucket.set(value, Constants.REDIS_DATA_KEEP_DAYS, TimeUnit.DAYS);
        });
    }
}
