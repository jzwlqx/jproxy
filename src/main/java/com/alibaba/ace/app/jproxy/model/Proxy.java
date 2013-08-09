package com.alibaba.ace.app.jproxy.model;

/**
 * 代理服务器模型。暂时不支持认证。
 * 
 * @author jjz
 */
public class Proxy {

    private String host;
    private int    port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
