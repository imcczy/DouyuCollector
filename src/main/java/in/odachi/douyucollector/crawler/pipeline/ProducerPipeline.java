package in.odachi.douyucollector.crawler.pipeline;

import in.odachi.douyucollector.ChannelTask;
import in.odachi.douyucollector.RedissonStorage;
import in.odachi.douyucollector.common.constant.Constants;
import in.odachi.douyucollector.common.util.ConfigUtil;
import in.odachi.douyucollector.crawler.Item;
import in.odachi.douyucollector.database.Log2DB;
import in.odachi.douyucollector.database.entity.Log;
import in.odachi.douyucollector.database.entity.Room;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProducerPipeline implements Pipeline {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Log2DB log2DB = Log2DB.getLog();

    private final BlockingQueue<ChannelTask> channelTasks;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN);

    private long totalCount;
    //private final Set<Integer> rooms = ConfigUtil.getRoomSet();

    public ProducerPipeline(BlockingQueue<ChannelTask> channelTasks) {
        this.channelTasks = channelTasks;
    }

    @Override
    public boolean process(Item item) {
        item.getFields().forEach((i) -> {
            if (i instanceof Room) {
                Room r = (Room) i;
                Integer roomId = r.getRoomId();
                // 推送任务到生产者
                totalCount++;
                //if (r.getFansNum() > ConfigUtil.getFansMinimum()) {
                    if (!channelTasks.offer(new ChannelTask(roomId))) {
                        logger.error("Offer room into channel task queue error: {}", roomId);
                    }
                //}
                // 统计人气峰值
                String today = LocalDateTime.now().format(dateFormatter);
                RBucket<Integer> onlinePeak = RedissonStorage.instance.onlinePeak(roomId, today);
                Integer old = onlinePeak.get();
                onlinePeak.set(Math.max(old == null ? 0 : old, r.getOnline()),
                        Constants.REDIS_DATA_KEEP_DAYS, TimeUnit.DAYS);
                // 统计在线时长
                if ("1".equals(r.getRoomStatus())) {
                    RBucket<Double> onlineTotal = RedissonStorage.instance.onlineTotal(roomId, today);
                    Double oldValue = onlineTotal.get();
                    // 如果每6分钟抓取一次房间，此处+0.1；如果每12分钟抓取一次房间，此处+0.2；以此类推
                    Double value = oldValue == null ? 0.2 : oldValue + 0.2;
                    onlineTotal.set(value, Constants.REDIS_DATA_KEEP_DAYS, TimeUnit.DAYS);
                } else {
                    logger.debug("The room is not online: " + roomId);
                    log2DB.log(new Log().crawler().debug().rid(r.getRoomId().toString())
                            .message("The room is not online."));
                }
            }
        });
        return true;
    }

    @Override
    public void close() {
        logger.info("Total room count: {}", totalCount);
    }
}
