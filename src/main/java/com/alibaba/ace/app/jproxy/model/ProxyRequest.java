package com.alibaba.ace.app.jproxy.model;

/**
 * 从proxy机器发送到真实服务的请求
 * 
 * @author jjz
 */
public class ProxyRequest extends HttpRequest {
    private String host;
    private String port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

}
