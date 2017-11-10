package in.odachi.douyucollector;

import in.odachi.douyucollector.common.constant.Constants;
import in.odachi.douyucollector.consumer.Consumer;
import in.odachi.douyucollector.consumer.rank.RankProcessor;
import in.odachi.douyucollector.crawler.Crawler;
import in.odachi.douyucollector.crawler.Request;
import in.odachi.douyucollector.crawler.pipeline.DatabasePipeline;
import in.odachi.douyucollector.crawler.pipeline.ProducerPipeline;
import in.odachi.douyucollector.crawler.processor.CategoryProcessor;
import in.odachi.douyucollector.crawler.processor.RoomListProcessor;
import in.odachi.douyucollector.producer.reactor.ThreadedSelector;
import in.odachi.douyucollector.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 启动类
 */
public class Bootstrap {

    /**
     * 任务队列
     */
    private static final BlockingQueue<ChannelTask> channelTasks = new LinkedBlockingQueue<>();

    /**
     * 消息队列
     */
    private static final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();

    /**
     * 定时任务线程池
     */
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    public static void main(String[] args) {
        new BootConsumer().go();
        new BootProducer().go();
        new BootCrawler().go();
    }

    private static class BootCrawler {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private void go() {
            scheduleCrawler();
        }

        private void scheduleCrawler() {
            executor.scheduleAtFixedRate(this::categoryCrawler, 0, 1, TimeUnit.DAYS);
            executor.scheduleAtFixedRate(this::roomCrawler, 0, 12, TimeUnit.MINUTES);
            shutdownGracefully();
        }

        private void roomCrawler() {
            Map<String, Object> param = new HashMap<>(2);
            param.put("page", 1);
            Request request = new Request(new RoomListProcessor())
                    .url(Constants.HTTPS_M_DOUYU_COM_ROOMLISTS)
                    .method(Constants.Method.POST)
                    .addHeader(Constants.Header.USER_AGENT, Constants.Header.USER_AGENT_PAD)
                    .addHeader(Constants.Header.X_REQUESTED_WITH, "XMLHttpRequest")
                    .body(param);
            new Crawler().startRequest(request)
                    .pipeline(new DatabasePipeline())
                    .pipeline(new ProducerPipeline(channelTasks))
                    .threadNum(8)
                    .run();
        }

        private void categoryCrawler() {
            Request request = new Request(new CategoryProcessor())
                    .url(Constants.HTTPS_M_DOUYU_COM_CATEGORY)
                    .method(Constants.Method.GET)
                    .addHeader(Constants.Header.USER_AGENT, Constants.Header.USER_AGENT_PAD)
                    .addHeader(Constants.Header.X_REQUESTED_WITH, "XMLHttpRequest");
            new Crawler().startRequest(request)
                    .pipeline(new DatabasePipeline())
                    .threadNum(1)
                    .run();
        }

        private void shutdownGracefully() {
            // 优雅停机
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        logger.info("Exit signal is received, program will exit.");
                        executor.shutdown();
                    }, "CrawlerShutdownHook")
            );
        }
    }

    private static class BootConsumer {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private void go() {
            // 启动消息消费者线程
            Consumer consumer = new Consumer(messages);
            consumer.messageProcessor(new RankProcessor());
            consumer.start();
            // 优雅停机
            shutdownGracefully(consumer);
        }

        private void shutdownGracefully(Consumer consumer) {
            // 优雅停机
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        logger.info("Exit signal is received, program will exit.");
                        // 中断监视线程
                        consumer.interrupt();
                        try {
                            for (int i = 0; i < 20; i++) {
                                Thread.sleep(1000);
                                if (consumer.getState() == Thread.State.TERMINATED) {
                                    logger.info("ShutdownHook exit.");
                                    return;
                                }
                            }
                        } catch (InterruptedException ignored) {
                        }
                        logger.info("Exit timeout, program will exit forcibly.");
                    }, "ConsumerShutdownHook")
            );
        }
    }

    private static class BootProducer {

        private static final String[] hosts = new String[]{
                // "123.150.206.162",  // openbarrage.douyutv.com
                "124.95.174.146",   // openbarrage.douyutv.com
                "180.97.182.50",    // danmu.douyutv.com (aliyun)
                "115.231.96.22",    // danmu.douyu.com (aliyun)
                "115.231.96.20",    // danmu.douyu.tv (aliyun)
                "42.81.84.66",      // openbarrage.douyucdn.cn (aliyun)
                "119.97.145.130",   // danmu.douyucdn.cn (aliyun)
                "119.97.145.131",   // danmu.douyutv.com (local)
                "115.231.96.21",    // danmu.douyu.com (local)
                "115.231.96.18",    // danmu.douyucdn.cn (local)
        };

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private void go() {
            ThreadedSelector.Args args = new ThreadedSelector.Args()
                    .hosts(hosts)
                    .channelTaskQueue(channelTasks)
                    .messageQueue(messages);
            ThreadedSelector selector = new ThreadedSelector(args);
            // 优雅停机
            shutdownGracefully(selector);
        }

        private void shutdownGracefully(ThreadedSelector selector) {
            // 优雅停机
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        logger.info("Exit signal is received, program will exit.");
                        // 中断IO线程
                        selector.stop();
                        try {
                            for (int i = 0; i < 20; i++) {
                                Thread.sleep(1000);
                                if (selector.isStopped()) {
                                    return;
                                }
                            }
                        } catch (InterruptedException ignored) {
                        }
                        logger.info("Exit timeout, program will exit forcibly.");
                    }, "ProducerShutdownHook")
            );
        }
    }
}
