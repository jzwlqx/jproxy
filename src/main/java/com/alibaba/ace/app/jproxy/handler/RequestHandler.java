package com.alibaba.ace.app.jproxy.handler;

import java.io.OutputStream;

import com.alibaba.ace.app.jproxy.model.HttpRequest;

/**
 * 处理用户请求. 实现类可以自定义处理的逻辑。
 * 
 * @author jjz
 */
public interface RequestHandler {

    /**
     * 处理用户的请求
     * 
     * @param req 用户请求
     * @param os 给用户的输出流
     */
    void handle(HttpRequest req, OutputStream os);
}
