package in.odachi.douyucollector.crawler;

import in.odachi.douyucollector.crawler.downloader.Downloader;
import in.odachi.douyucollector.crawler.downloader.HttpClientDownloader;
import in.odachi.douyucollector.crawler.pipeline.Pipeline;
import in.odachi.douyucollector.crawler.scheduler.QueueScheduler;
import in.odachi.douyucollector.crawler.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Crawler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SLEEP_MILLIS = 30000;

    protected Scheduler scheduler;
    protected Downloader downloader;
    protected List<Pipeline> pipelines = new LinkedList<>();
    protected List<Request> startRequests = new LinkedList<>();

    /**
     * 默认启动10个线程用于网页抓取
     */
    protected int threadNum = 10;

    /**
     * 指定核心线程数的线程池，超过线程数量的调用将会被阻塞
     */
    protected CountableThreadPool threadPool;

    private ReentrantLock newUrlLock = new ReentrantLock();
    private Condition newUrlCondition = newUrlLock.newCondition();

    public Crawler() {
    }

    public Crawler startRequest(Request request) {
        this.startRequests.add(request);
        return this;
    }

    public Crawler pipeline(Pipeline pipeline) {
        this.pipelines.add(pipeline);
        return this;
    }

    public Crawler downloader(Downloader downloader) {
        this.downloader = downloader;
        return this;
    }

    public Crawler threadNum(int threadNum) {
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    protected void init() {
        if (scheduler == null) {
            scheduler = new QueueScheduler();
        }
        if (downloader == null) {
            downloader = new HttpClientDownloader();
        }
        downloader.setThread(threadNum);
        if (threadPool == null || threadPool.isShutdown()) {
            threadPool = new CountableThreadPool(threadNum);
        }
        for (Request request : startRequests) {
            scheduler.push(request);
        }
        startRequests.clear();
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        init();
        logger.info("Crawler start.");
        while (!Thread.currentThread().isInterrupted()) {
            final Request request = scheduler.poll();
            if (request == null) {
                if (threadPool.getThreadAlive() == 0) {
                    break;
                }
                // wait until new url added
                waitNewUrl();
            } else {
                threadPool.execute(() -> {
                    try {
                        Response response = downloader.download(request);
                        if (response.isDownloadSuccess() && request.getProcessor() != null) {
                            request.getProcessor().process(response);
                            extractAndAddRequests(response);
                            if (!response.getItem().isSkip()) {
                                pipelines.forEach(pipeline -> {
                                    if (!pipeline.process(response.getItem())) {
                                        logger.error("Process entity {} FAILED.", response.getItem());
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Process request {} error, {}", request, e);
                    } finally {
                        signalNewUrl();
                    }
                });
            }
        }
        close();
        logger.info("Crawler finished, cost: {} seconds.", (System.currentTimeMillis() - start) / 1000);
    }

    protected void extractAndAddRequests(Response response) {
        for (Request request : response.getTargetRequests()) {
            scheduler.push(request);
        }
    }

    private void waitNewUrl() {
        newUrlLock.lock();
        try {
            // double check
            if (threadPool.getThreadAlive() == 0) {
                return;
            }
            newUrlCondition.await(SLEEP_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

    private void close() {
        destroyEach(downloader);
        destroyEach(scheduler);
        for (Pipeline pipeline : pipelines) {
            destroyEach(pipeline);
        }
        threadPool.shutdown();
    }

    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }
}
