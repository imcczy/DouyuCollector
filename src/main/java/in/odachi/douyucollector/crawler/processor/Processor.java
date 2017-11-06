package in.odachi.douyucollector.crawler.processor;

import in.odachi.douyucollector.crawler.Response;

public interface Processor {

    /**
     * Process the response, extract urls to scheduler, extract the data and store
     */
    void process(Response response);
}
