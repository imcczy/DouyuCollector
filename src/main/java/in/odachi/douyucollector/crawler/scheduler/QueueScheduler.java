package in.odachi.douyucollector.crawler.scheduler;

import in.odachi.douyucollector.crawler.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueScheduler implements Scheduler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Queue<Request> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void push(Request request) {
        if (!shouldPush(request)) {
            logger.info("Request should not push into scheduler!");
            return;
        }
        if (!queue.offer(request)) {
            logger.error("Offer request into queue FAILED: {}", request);
        }
    }

    protected boolean shouldPush(Request request) {
        return true;
    }

    @Override
    public Request poll() {
        return queue.poll();
    }
}
