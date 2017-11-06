package in.odachi.douyucollector.crawler.processor;

import in.odachi.douyucollector.crawler.Response;
import in.odachi.douyucollector.database.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomPageProcessor implements Processor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Response response) {
        Result result = response.getJson().toObject(Result.class);
        if (result.error != 0) {
            logger.error("Request {} error!", response.getRequest());
            return;
        }
        response.addField(result.data);
        result.data.getGift().forEach(response::addField);
    }

    public static class Result {
        public Integer error;
        public Room data;
    }
}
