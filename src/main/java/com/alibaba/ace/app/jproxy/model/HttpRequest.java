package com.alibaba.ace.app.jproxy.model;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 代表用户的请求。
 * 
 * @author jjz
 */
public class HttpRequest extends HttpMessage {

    private String method;
    /**
     * 请求行中的中间部分
     */
    private String url;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected void sendHeadLine(OutputStream os) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(url).append(" ").append(this.getProtocol())
                .append("/").append(this.getVersion());
        writeLine(sb.toString(), os);
    }

}
