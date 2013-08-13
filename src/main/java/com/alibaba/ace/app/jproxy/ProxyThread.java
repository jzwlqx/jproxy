package com.alibaba.ace.app.jproxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.connection.HttpConnection;
import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.model.HttpMessage;
import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.model.HttpResponse;
import com.alibaba.ace.app.jproxy.model.Proxy;
import com.alibaba.ace.app.jproxy.parser.HttpRequestParser;

public class ProxyThread extends Thread {

    private static final Log log = LogFactory.getLog(ProxyThread.class);

    private Socket           socket;

    public ProxyThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            for (;;) {
                HttpRequestParser parser = new HttpRequestParser();
                HttpRequest req = parser.parse(socket.getInputStream());
                if (req == null) {
                    //client关闭连接了
                    break;
                }
                process(req);
            }
            // ACL 
            // Route
            // Process

        } catch (BadRequestException e) {
            log.error("", e);
            error(400, e.toString());
        } catch (Exception e) {
            log.error("", e);
            error(502, e.toString());
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }

    private void error(int code, String message) {
        String desc = null;
        switch (code) {
            case 400:
                desc = "Bad Request";
                break;
            case 502:
                desc = "Bad Gateway";
                break;
        }
        HttpResponse res = new HttpResponse();
        res.setCode(code);
        res.setProtocol("HTTP");
        res.setVersion("1.0");
        byte[] b = new byte[] {};
        try {
            b = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("encode string", e);
        }
        res.setContent(new ByteArrayInputStream(b));
        res.setMessage(desc);
        res.addHeader(new HttpMessage.Header("Server", "JProxy"));
        res.addHeader(new HttpMessage.Header("Content-Type", "text/html; charset=utf-8"));
        res.addHeader(new HttpMessage.Header("Content-Length", String.valueOf(b.length)));
        try {
            res.send(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(HttpRequest req) throws IOException {
        Proxy proxy = null;
        if (req.getMethod().equals("CONNECT")) {
            throw new RuntimeException("Un support");
        } else {
            HttpConnection conn = new HttpConnection(req, proxy);
            try {
                HttpResponse response = conn.request();
                response.send(socket.getOutputStream());
            } finally {
                conn.close();
            }
        }
    }
}
