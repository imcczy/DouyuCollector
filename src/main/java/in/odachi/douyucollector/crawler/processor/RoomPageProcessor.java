package in.odachi.douyucollector.crawler.processor;

import in.odachi.douyucollector.crawler.Response;
import in.odachi.douyucollector.database.Log2DB;
import in.odachi.douyucollector.database.entity.Log;
import in.odachi.douyucollector.database.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomPageProcessor implements Processor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Log2DB log2DB = Log2DB.getLog();

    @Override
    public void process(Response response) {
        Result result;
        try {
            result = response.getJson().toObject(Result.class);
        } catch (RuntimeException e) {
            logger.error("Parse json from API error: {}", e.getLocalizedMessage());
            log2DB.log(new Log().crawler().error().rid(response.getRequest().getMark())
                    .message("Parse json from API error."));
            return;
        }
        if (result.error != 0) {
            logger.error("Request {} error!", response.getRequest());
            log2DB.log(new Log().crawler().error().rid(response.getRequest().getMark())
                    .message("Parse json from API error, code: " + result.error + "."));
            return;
        }
        response.addField(result.data);
        result.data.getGift().forEach(response::addField);
        log2DB.log(new Log().crawler().debug().rid(response.getRequest().getMark())
                .message("Parse json from API success."));
    }

    public static class Result {
        public Integer error;
        public Room data;
    }
}
