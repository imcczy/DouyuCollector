package in.odachi.douyucollector.crawler.downloader;

import in.odachi.douyucollector.crawler.Request;
import in.odachi.douyucollector.crawler.Response;

public interface Downloader {

    /**
     * Downloads web pages and store in Response object.
     */
    Response download(Request request);

    /**
     * Tell the downloader how many threads the spider used.
     */
    void setThread(int threadNum);
}
