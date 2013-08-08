package com.alibaba.ace.app.jproxy.handler;

import java.io.OutputStream;

import com.alibaba.ace.app.jproxy.model.HttpRequest;

/**
 * 根据路由规则，把请求转发到二级代理上。
 * 
 * @author jjz
 */
public class RouteRequestHandler implements RequestHandler {

    @Override
    public void handle(HttpRequest req, OutputStream os) {

    }

}
