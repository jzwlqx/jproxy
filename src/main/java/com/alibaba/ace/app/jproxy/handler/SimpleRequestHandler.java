package com.alibaba.ace.app.jproxy.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.connection.HttpConnection;
import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.model.Proxy;

/**
 * 基本代理类。根据request，发送到实际服务器的请求。把服务器的输出流发送给client。
 * 
 * @author jjz
 */
public class SimpleRequestHandler extends AbstractRequestHandler {
    private static Log log = LogFactory.getLog(SimpleRequestHandler.class);

    private Proxy      proxy;

    public SimpleRequestHandler() {

    }

    public SimpleRequestHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    protected void process(HttpRequest req, OutputStream os) throws IOException {
        HttpConnection con = new HttpConnection(req, proxy);
        con.connect();
        con.sendRequest();
        InputStream is = con.getInputStream();
        IOUtils.copy(is, os);
    }

}
