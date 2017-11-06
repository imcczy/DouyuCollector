package in.odachi.douyucollector.crawler;

import in.odachi.douyucollector.common.constant.Constants;
import in.odachi.douyucollector.crawler.processor.Processor;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private Processor processor;

    private String url;

    private String method = HttpGet.METHOD_NAME;

    private byte[] body;

    private Map<String, Object> bodyParam;

    private String contentType = Constants.ContentType.FORM;

    private String encoding = Constants.Encoding.UTF_8;

    private String charset = Constants.Encoding.UTF_8;

    private Map<String, String> cookies = new HashMap<>();

    private Map<String, String> headers = new HashMap<>();

    public Request(Processor processor) {
        this.processor = processor;
    }

    public Request url(String url) {
        this.url = url;
        return this;
    }

    public Request method(String method) {
        this.method = method;
        return this;
    }

    public Request body(byte[] body) {
        this.body = body;
        return this;
    }

    public Request body(Map<String, Object> param) {
        this.bodyParam = param;
        List<NameValuePair> nameValuePairs = new ArrayList<>(param.size());
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        }
        try {
            this.body = URLEncodedUtils.format(nameValuePairs, encoding).getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("illegal encoding " + encoding, e);
        }
        return this;
    }

    public Request contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Request encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public Request charset(String charset) {
        this.charset = charset;
        return this;
    }

    public Request addCookie(String name, String value) {
        cookies.put(name, value);
        return this;
    }

    public Request headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Request addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getCharset() {
        return charset;
    }

    public Processor getProcessor() {
        return processor;
    }

    @Override
    public String toString() {
        return "Request {" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", body=" + bodyParam +
                ", headers=" + headers +
                ", cookies=" + cookies +
                '}';
    }
}
