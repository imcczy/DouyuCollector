package in.odachi.douyucollector.crawler;

import in.odachi.douyucollector.crawler.parser.Html;
import in.odachi.douyucollector.crawler.parser.Json;
import in.odachi.douyucollector.database.entity.Entity;
import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Response {

    private Request request;

    private Item item = new Item();

    private byte[] bytes;

    private Map<String, List<String>> headers;

    private int statusCode = HttpStatus.SC_OK;

    private String rawText;

    private Json json;

    private Html html;

    private List<Request> targetRequests = new LinkedList<>();

    private boolean downloadSuccess = false;

    public Response(Request request) {
        this.request = request;
    }

    public boolean isDownloadSuccess() {
        return downloadSuccess;
    }

    public void setDownloadSuccess(boolean downloadSuccess) {
        this.downloadSuccess = downloadSuccess;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Request getRequest() {
        return request;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public void addField(Entity field) {
        item.addField(field);
    }

    public Item getItem() {
        return item;
    }

    public List<Request> getTargetRequests() {
        return targetRequests;
    }

    public void addTargetRequest(Request request) {
        targetRequests.add(request);
    }

    public Json getJson() {
        if (json == null) {
            json = new Json(rawText);
        }
        return json;
    }

    public Html getHtml() {
        if (html == null) {
            html = new Html(rawText);
        }
        return html;
    }

    @Override
    public String toString() {
        return "Response {" +
                "request=" + request +
                ", headers=" + headers +
                ", statusCode=" + statusCode +
                ", downloadSuccess=" + downloadSuccess +
                ", targetRequests=" + targetRequests +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }
}
