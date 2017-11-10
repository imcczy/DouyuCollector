package in.odachi.douyucollector.crawler.processor;

import in.odachi.douyucollector.common.constant.Constants;
import in.odachi.douyucollector.common.util.NumberUtil;
import in.odachi.douyucollector.crawler.Request;
import in.odachi.douyucollector.crawler.Response;
import in.odachi.douyucollector.database.Log2DB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomListProcessor implements Processor {

    private final Log2DB log2DB = Log2DB.getLog();

    /**
     * 用于解析房间详情页面
     */
    private final Processor processor = new RoomPageProcessor();

    @Override
    public void process(Response response) {
        Map<String, Object> data = response.getJson().toMap();
        Integer pageCount = NumberUtil.parseInt(data.get("pageCount"));
        Integer nowPage = NumberUtil.parseInt(data.get("nowPage"));
        if (nowPage == 1) {
            for (int page = nowPage + 1; page <= pageCount; page++) {
                Map<String, Object> param = new HashMap<>(2);
                param.put("page", page);
                Request r2 = new Request(this)
                        .url(response.getRequest().getUrl())
                        .method(Constants.Method.POST)
                        .headers(response.getRequest().getHeaders())
                        .body(param);
                response.addTargetRequest(r2);
            }
        }

        ((List<Map<String, Object>>) data.get("result")).forEach((result) -> {
            Integer roomId = NumberUtil.parseInt(result.get("room_id"));
            Request r3 = new Request(processor)
                    .mark(roomId.toString())
                    .url(Constants.HTTP_OPEN_DOUYUCDN_CN_API_ROOM_API_ROOM + roomId);
            response.addTargetRequest(r3);
        });
        response.getItem().setSkip(true);
    }
}
