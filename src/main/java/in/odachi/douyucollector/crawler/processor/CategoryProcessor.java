package in.odachi.douyucollector.crawler.processor;

import in.odachi.douyucollector.common.util.NumberUtil;
import in.odachi.douyucollector.crawler.Response;
import in.odachi.douyucollector.database.entity.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryProcessor implements Processor {

    @Override
    public void process(Response response) {
        Map<String, Object> result = response.getJson().toMap();
        List<Map<String, Object>> cate1Info = (List<Map<String, Object>>) result.get("cate1Info");
        List<Map<String, Object>> cate2Info = (List<Map<String, Object>>) result.get("cate2Info");

        Map<Integer, Map<String, Object>> cate1InfoMap = new HashMap<>(16);
        cate1Info.forEach((cate1) -> cate1InfoMap.put(NumberUtil.parseInt(cate1.get("cate1Id")), cate1));

        cate2Info.forEach((cate2) -> {
            Integer cate1Id = NumberUtil.parseInt(cate2.get("cate1Id"));
            Category category = new Category();
            category.setCate1Id(cate1Id);
            category.setCate1Name((String) cate1InfoMap.get(cate1Id).get("cate1Name"));
            category.setCate1ShortName((String) cate1InfoMap.get(cate1Id).get("shortName"));
            category.setCate2Id(NumberUtil.parseInt(cate2.get("cate2Id")));
            category.setCate2Name((String) cate2.get("cate2Name"));
            category.setCate2ShortName((String) cate2.get("shortName"));
            category.setPic((String) cate2.get("pic"));
            category.setIcon((String) cate2.get("icon"));
            category.setSmallIcon((String) cate2.get("smallIcon"));
            category.setCount(NumberUtil.parseInt(cate2.get("count")));
            response.addField(category);
        });
    }
}
