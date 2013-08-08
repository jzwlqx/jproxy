package com.alibaba.ace.app.jproxy.handler;

import java.io.OutputStream;

import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.model.Proxy;

/**
 * 基本代理类。根据request，发送到实际服务器的请求。把服务器的输出流发送给client。
 * 
 * @author jjz
 */
public class SimpleRequestHandler implements RequestHandler {
    private Proxy proxy;

    public SimpleRequestHandler() {

    }

    public SimpleRequestHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void handle(HttpRequest req, OutputStream os) {

    }

}
