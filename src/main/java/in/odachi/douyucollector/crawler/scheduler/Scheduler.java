package in.odachi.douyucollector.crawler.scheduler;

import in.odachi.douyucollector.crawler.Request;

public interface Scheduler {

    /**
     * add a url to scheduler
     */
    void push(Request request);

    /**
     * get an url to crawl
     */
    Request poll();
}
