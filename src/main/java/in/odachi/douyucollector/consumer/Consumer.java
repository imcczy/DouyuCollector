package in.odachi.douyucollector.consumer;

import in.odachi.douyucollector.RedissonStorage;
import in.odachi.douyucollector.common.constant.Constants;
import in.odachi.douyucollector.consumer.util.MessageUtil;
import in.odachi.douyucollector.protocol.Chat;
import in.odachi.douyucollector.protocol.Deserve;
import in.odachi.douyucollector.protocol.Dgb;
import in.odachi.douyucollector.protocol.Message;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息队列
 */
public class Consumer extends Thread {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlockingQueue<Message> queue;
    private final List<MessageProcessor> messageProcessors = new LinkedList<>();

    /**
     * 私有构造器
     */
    public Consumer(BlockingQueue<Message> queue) {
        setName(getClass().getSimpleName());
        this.queue = queue;
        // 默认有一个监视器
        messageProcessors.add(new MonitorProcessor());
    }

    public Consumer messageProcessor(MessageProcessor processor) {
        messageProcessors.add(processor);
        return this;
    }

    /**
     * 启动消费者线程疯狂消费弹幕消息
     */
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final Message m = MessageUtil.parseMessage(queue.take());
                logger.trace(m.toString());
                messageProcessors.forEach((processor -> processor.process(m)));
            } catch (InterruptedException e) {
                break;
            } catch (RuntimeException e) {
                logger.error(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        logger.info("{} has exited.", Thread.currentThread().getName());
    }

    /**
     * 消息数量监视处理器
     */
    public class MonitorProcessor implements MessageProcessor {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        /**
         * 总消息处理速率
         */
        private AtomicLong processedTotalCount = new AtomicLong();

        /**
         * 相关消息处理速率
         * 目前，弹幕、礼物和酬勤是相关消息
         */
        private AtomicLong processedRelevantCount = new AtomicLong();

        public MonitorProcessor() {
            startWatcherThread();
        }

        @Override
        public void process(Message m) {
            processedTotalCount.incrementAndGet();
            if (m instanceof Chat) {
                processedRelevantCount.incrementAndGet();
            } else if (m instanceof Dgb) {
                processedRelevantCount.incrementAndGet();
            } else if (m instanceof Deserve) {
                processedRelevantCount.incrementAndGet();
            }
        }

        /**
         * 启动监视线程
         */
        private void startWatcherThread() {
            Thread watcherThread = new Thread(() -> {
                RList<Pair<Long, Integer>> rateList = RedissonStorage.instance.messageProcessedRateMap();
                int modIndex = 0;
                double reportRate = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    long start = System.currentTimeMillis();
                    double processedTotalRate = processedTotalCount.getAndSet(0) /
                            (double) Constants.WATCHER_SLEEP_TIME * 60 * 1000;
                    double processedRelevantRate = processedRelevantCount.getAndSet(0) /
                            (double) Constants.WATCHER_SLEEP_TIME * 60 * 1000;
                    String processedTotalRateStr = String.format("%.0f", processedTotalRate);
                    String processedRelevantCountStr = String.format("%.0f", processedRelevantRate);

                    // 计算平均速率
                    reportRate += processedTotalRate;
                    if (modIndex++ % Constants.PROCESSED_RATE_REPORT_GAP == 0) {
                        long timeMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        rateList.add(new ImmutablePair<>(timeMillis, new Double(reportRate / Constants.PROCESSED_RATE_REPORT_GAP).intValue()));
                        // 保留最近的PROCESSED_RATE_KEEP_SIZE个数据
                        while (rateList.size() > Constants.PROCESSED_RATE_KEEP_SIZE) {
                            rateList.remove(0);
                        }
                        reportRate = 0;
                    }

                    // 拼接输出字符串
                    logger.info("Statistics, barrage queue: {}, processed total rate: {}/minute, processed relevant rate: {}/minute",
                            queue.size(), processedTotalRateStr, processedRelevantCountStr);
                    try {
                        long sleepTime = Constants.WATCHER_SLEEP_TIME - ((System.currentTimeMillis() - start));
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                logger.error("{} has exited.", Thread.currentThread().getName());
            }, "ConsumerWatcher");

            // 启动线程
            watcherThread.setDaemon(true);
            watcherThread.start();
        }
    }
}
