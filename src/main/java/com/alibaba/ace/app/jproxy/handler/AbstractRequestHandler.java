package com.alibaba.ace.app.jproxy.handler;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.exception.ProxyException;
import com.alibaba.ace.app.jproxy.model.HttpRequest;

/**
 * 抽象请求处理类，主要用来处理错误。子类遇到错误时只要抛异常就可以了。
 * 
 * @author jjz
 */
public abstract class AbstractRequestHandler implements RequestHandler {
    private static final Log log = LogFactory.getLog(AbstractRequestHandler.class);

    @Override
    public void handle(HttpRequest req, OutputStream os) {
        try {
            process(req, os);
        } catch (BadRequestException e) {
            log.error("", e);
            error(os, 400, e.toString());
        } catch (ProxyException e) {
            log.error("", e);
            error(os, 502, e.toString());
        } catch (Exception e) {
            log.error("", e);
            error(os, 502, "Internal error " + e.toString());
        } finally {
            IOUtils.closeQuietly(os);
        }

    }

    private void error(OutputStream os, int code, String message) {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        StringBuilder sb = new StringBuilder();
        String desc = null;
        switch (code) {
            case 400:
                desc = "Bad Request";
                break;
            case 502:
                desc = "Bad Gateway";
                break;
        }
        sb.append("HTTP/1.1 ");
        sb.append(code);
        sb.append(" ");
        sb.append(desc);
        sb.append("\r\n");
        byte[] b = new byte[] {};
        try {
            b = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("encode string", e);
        }
        addHeader("Server", "JProxy", sb);
        addHeader("Content-Type", "text/html; charset=utf-8", sb);
        addHeader("Content-Length", String.valueOf(b.length), sb);
        sb.append("\r\n");

        try {
            os.write(sb.toString().getBytes("ISO-8859-1"));
            os.write(b);
        } catch (Exception e) {
            log.error("error send to client.", e);
        }
    }

    private void addHeader(String name, String value, StringBuilder sb) {
        sb.append(name);
        sb.append(": ");
        sb.append(value);
        sb.append("\r\n");
    }

    protected abstract void process(HttpRequest req, OutputStream os) throws Exception;

}
