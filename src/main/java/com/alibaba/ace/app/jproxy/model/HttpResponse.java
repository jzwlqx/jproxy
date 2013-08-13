package com.alibaba.ace.app.jproxy.model;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class HttpResponse extends HttpMessage {
    private int    code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected void sendHeadLine(OutputStream os) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getProtocol()).append("/").append(this.getVersion()).append(" ")
                .append(code).append(" ").append(message);
        writeLine(sb.toString(), os);
    }

    @Override
    protected void unknowSize(OutputStream os) throws IOException {
        String keepAlive = this.getHeader("Connection");
        //TODO 处理1.0的情况
        if (keepAlive != null && keepAlive.toLowerCase().equals("close")) {
            IOUtils.copy(this.getContent(), os);
        }
    }
}
