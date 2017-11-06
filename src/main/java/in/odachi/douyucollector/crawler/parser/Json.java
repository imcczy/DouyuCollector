package in.odachi.douyucollector.crawler.parser;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class Json {

    private String sourceTexts;

    public Json(String text) {
        this.sourceTexts = text;
    }

    public <T> T toObject(Class<T> clazz) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        Gson gson = gsonBuilder.create();
        return gson.fromJson(sourceTexts, clazz);
    }

    public Map<String, Object> toMap() {
        return new Gson().fromJson(sourceTexts, new TypeToken<Map<String, Object>>() {
        }.getType());
    }
}
