package in.odachi.douyucollector.crawler.pipeline;

import in.odachi.douyucollector.LCache;
import in.odachi.douyucollector.crawler.Item;
import in.odachi.douyucollector.database.DBUtility;
import in.odachi.douyucollector.database.entity.Category;
import in.odachi.douyucollector.database.entity.Gift;
import in.odachi.douyucollector.database.entity.Log;
import in.odachi.douyucollector.database.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePipeline implements Pipeline {

    private final DBUtility dbUtility = DBUtility.instance;

    private final LCache cache = LCache.instance;

    @Override
    public boolean process(Item item) {
        item.getFields().forEach((i) -> {
            if (i instanceof Category) {
                // 直接插入数据库
                dbUtility.insertCategory((Category) i);
            } else if (i instanceof Gift) {
                cache.putGift((Gift) i);
            } else if (i instanceof Room) {
                cache.putRoom((Room) i);
            }
        });
        return true;
    }
}
