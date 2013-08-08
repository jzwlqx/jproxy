package com.alibaba.ace.app.jproxy.model;

import java.io.InputStream;
import java.util.Map;

/**
 * 代表用户的请求。
 * 
 * @author jjz
 */
public class HttpRequest {
    private String              method;
    private String              version;
    private Map<String, String> headers;
    /**
     * headline中的uri path
     */
    private String              path;
    /**
     * headline中的queryString，没有decode
     */
    private String              queryString;
    /**
     * headline中的host
     */
    private String              host;
    private InputStream         body;
    private String              protocol;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(InputStream body) {
        this.body = body;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

}
