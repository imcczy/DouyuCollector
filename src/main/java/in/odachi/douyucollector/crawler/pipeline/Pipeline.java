package in.odachi.douyucollector.crawler.pipeline;

import in.odachi.douyucollector.crawler.Item;

public interface Pipeline {

    /**
     * Process extracted results.
     */
    boolean process(Item item);
}
