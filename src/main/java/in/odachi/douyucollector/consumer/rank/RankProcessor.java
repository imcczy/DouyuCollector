package in.odachi.douyucollector.consumer.rank;

import in.odachi.douyucollector.consumer.MessageProcessor;
import in.odachi.douyucollector.database.Log2DB;
import in.odachi.douyucollector.protocol.Chat;
import in.odachi.douyucollector.protocol.Deserve;
import in.odachi.douyucollector.protocol.Dgb;
import in.odachi.douyucollector.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实时排名
 */
public class RankProcessor implements MessageProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Integer, Double> mcm = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> mum = new ConcurrentHashMap<>();
    private final Map<Integer, Double> gcm = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> gum = new ConcurrentHashMap<>();
    private final Map<Integer, Map<Integer, Integer>> gtm = new ConcurrentHashMap<>();

    public RankProcessor() {
        // 设置定时任务，默认每分钟执行一次统计
        LocalTime now = LocalTime.now();
        LocalTime next = LocalTime.of(now.getHour(), now.getMinute() + 1);
        long delay = now.until(next, ChronoUnit.MILLIS);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            logger.info("RankProcessor starts.");
            long start = System.currentTimeMillis();
            // 实时统计弹幕数量
            RankStorage.instance.computeMsgCount(mcm);
            // 实时统计弹幕人次
            RankStorage.instance.computeMsgUser(mum);
            // 实时统计礼物数量
            RankStorage.instance.computeGiftCount(gcm);
            // 实时统计礼物人次
            RankStorage.instance.computeGiftUser(gum);
            // 实时统计礼物价值
            RankStorage.instance.computeGiftPrice(gtm);
            logger.info("RankProcessor ends, cost {} milliseconds.", System.currentTimeMillis() - start);
        }, delay, 60 * 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void process(Message m) {
        if (m instanceof Chat) {
            Chat c = ((Chat) m);
            // 弹幕数量
            incrementMsgCount(c.getRid());
            // 弹幕人次
            incrementMsgUser(c.getRid(), c.getUid());
        } else if (m instanceof Dgb) {
            Dgb d = ((Dgb) m);
            // 礼物数量
            incrementGiftCount(d.getRid());
            // 礼物人次
            incrementGiftUser(d.getRid(), d.getUid());
            // 礼物类型
            incrementGiftType(d.getRid(), d.getGfid());
        } else if (m instanceof Deserve) {
            Deserve d = ((Deserve) m);
        }
    }

    /**
     * 新增一个消息
     */
    private void incrementMsgCount(Integer roomId) {
        mcm.compute(roomId, (k, v) -> v == null ? 1 : v + 1);
    }

    /**
     * 新增一个消息
     */
    private void incrementMsgUser(Integer roomId, Integer userId) {
        mum.compute(roomId, (k, v) -> {
            if (v == null) {
                Set<Integer> userSet = new HashSet<>();
                userSet.add(userId);
                return userSet;
            } else {
                v.add(userId);
                return v;
            }
        });
    }

    /**
     * 新增一个礼物
     */
    private void incrementGiftCount(Integer roomId) {
        gcm.compute(roomId, (k, v) -> v == null ? 1 : v + 1);
    }

    /**
     * 新增一个礼物
     */
    private void incrementGiftUser(Integer roomId, Integer userId) {
        gum.compute(roomId, (k, v) -> {
            if (v == null) {
                Set<Integer> userSet = new HashSet<>();
                userSet.add(userId);
                return userSet;
            } else {
                v.add(userId);
                return v;
            }
        });
    }

    /**
     * 新增一个礼物
     */
    private void incrementGiftType(Integer roomId, Integer giftId) {
        gtm.compute(roomId, (k, v) -> {
            if (v == null) {
                Map<Integer, Integer> types = new ConcurrentHashMap<>();
                types.compute(giftId, (k0, v0) -> v0 == null ? 1 : v0 + 1);
                return types;
            } else {
                v.compute(giftId, (k0, v0) -> v0 == null ? 1 : v0 + 1);
                return v;
            }
        });
    }
}