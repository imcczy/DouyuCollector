package in.odachi.douyucollector.crawler.pipeline;

import in.odachi.douyucollector.crawler.Item;

import java.io.Closeable;

public interface Pipeline extends Closeable {

    /**
     * Process extracted results.
     */
    boolean process(Item item);

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     */
    default void close() {
    }
}
