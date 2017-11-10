package in.odachi.douyucollector;

import in.odachi.douyucollector.common.constant.RedisKeys;
import in.odachi.douyucollector.common.util.ConfigUtil;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;

public enum RedissonStorage {
    // 单例模式
    instance;

    // Redisson协议存储
    private final RedissonClient client;

    RedissonStorage() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(ConfigUtil.getRedisAddress())
                .setPassword(ConfigUtil.getRedisPassword())
                .setDatabase(ConfigUtil.getRedisDb());
        client = Redisson.create(config);
    }

    public RList<Number[]> messageProcessedRateList() {
        return client.getList(RedisKeys.SYSTEM_MESSAGE_PROCESSED_RATE);
    }

    public RBucket<Integer> onlinePeak(Integer roomId, String date) {
        return client.getBucket(String.format(RedisKeys.ONLINE_PEAK, roomId, date));
    }

    public RBucket<Double> onlineTotal(Integer roomId, String date) {
        return client.getBucket(String.format(RedisKeys.ONLINE_TOTAL, roomId, date));
    }

    public RBucket<Double> roomDetail(String msg, String type, Integer roomId, String date) {
        return client.getBucket(String.format(RedisKeys.ROOM_DETAIL, msg, type, roomId, date));
    }

    public RMap<Integer, Integer> giftDetail(Integer roomId, String date) {
        return client.getMap(String.format(RedisKeys.GIFT_DETAIL, roomId, date));
    }

    public RScoredSortedSet<Integer> roomRank(String msg, String type, String date) {
        return client.getScoredSortedSet(String.format(RedisKeys.ROOM_RANK, msg, type, date));
    }
}
