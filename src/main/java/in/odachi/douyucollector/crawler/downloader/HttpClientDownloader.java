package in.odachi.douyucollector.crawler.downloader;

import in.odachi.douyucollector.crawler.Request;
import in.odachi.douyucollector.crawler.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpClientDownloader implements Downloader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

    @Override
    public Response download(Request request) {
        CloseableHttpClient httpClient = httpClientGenerator.generateClient();
        CloseableHttpResponse httpResponse = null;
        Response response = new Response(request);
        try {
            httpResponse = httpClient.execute(generateHttpUriRequest(request), generateHttpClientContext(request));
            response = handleResponse(request, httpResponse);
            logger.debug("Downloading page success {}", request);
        } catch (IOException e) {
            logger.warn("Download page {} error", request, e);
            response.setDownloadSuccess(false);
        } finally {
            if (httpResponse != null) {
                // ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return response;
    }

    @Override
    public void setThread(int threadNum) {
        httpClientGenerator.poolSize(threadNum);
    }

    /**
     * 将HttpResponse转换为Response
     */
    protected Response handleResponse(Request request, HttpResponse httpResponse) throws IOException {
        byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
        Response response = new Response(request);
        response.setBytes(bytes);
        response.setRawText(new String(bytes, request.getCharset()));
        response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        response.setDownloadSuccess(true);
        response.setHeaders(convertHeaders(httpResponse.getAllHeaders()));
        return response;
    }

    private Map<String, List<String>> convertHeaders(Header[] headers) {
        Map<String, List<String>> results = new HashMap<>();
        for (Header header : headers) {
            List<String> list = results.computeIfAbsent(header.getName(), k -> new ArrayList<>());
            list.add(header.getValue());
        }
        return results;
    }

    /**
     * 创建HttpUriRequest对象
     */
    private HttpUriRequest generateHttpUriRequest(Request request) {
        RequestBuilder requestBuilder = selectRequestMethod(request).setUri(fixIllegalCharacterInUrl(request.getUrl()));
        HttpUriRequest httpUriRequest = requestBuilder.build();
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                httpUriRequest.addHeader(header.getKey(), header.getValue());
            }
        }
        return httpUriRequest;
    }

    private RequestBuilder selectRequestMethod(Request request) {
        String method = request.getMethod();
        if (method == null || method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
            // default GET
            return RequestBuilder.get();
        } else if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
            return addFormParams(RequestBuilder.post(), request);
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
    }

    private RequestBuilder addFormParams(RequestBuilder requestBuilder, Request request) {
        if (request.getBody() != null) {
            ByteArrayEntity entity = new ByteArrayEntity(request.getBody());
            entity.setContentType(request.getContentType());
            requestBuilder.setEntity(entity);
        }
        return requestBuilder;
    }

    private String fixIllegalCharacterInUrl(String url) {
        return url.replace(" ", "%20").replaceAll("#+", "#");
    }

    /**
     * 创建HttpClientContext对象
     */
    private HttpClientContext generateHttpClientContext(Request request) {
        HttpClientContext httpContext = new HttpClientContext();
        if (request.getCookies() != null && !request.getCookies().isEmpty()) {
            CookieStore cookieStore = new BasicCookieStore();
            for (Map.Entry<String, String> cookieEntry : request.getCookies().entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
                cookie.setDomain(removePort(getDomain(request.getUrl())));
                cookieStore.addCookie(cookie);
            }
            httpContext.setCookieStore(cookieStore);
        }
        return httpContext;
    }

    private String getDomain(String url) {
        Pattern patternForProtocol = Pattern.compile("[\\w]+://");
        String domain = patternForProtocol.matcher(url).replaceAll("");
        int i = StringUtils.indexOf(domain, "/", 1);
        if (i > 0) {
            domain = StringUtils.substring(domain, 0, i);
        }
        return removePort(domain);
    }

    private String removePort(String domain) {
        int portIndex = domain.indexOf(":");
        if (portIndex != -1) {
            return domain.substring(0, portIndex);
        } else {
            return domain;
        }
    }
}
