package com.alibaba.ace.app.jproxy;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.alibaba.ace.app.jproxy.handler.RequestHandler;
import com.alibaba.ace.app.jproxy.handler.RouteRequestHandler;
import com.alibaba.ace.app.jproxy.model.HttpRequest;

public class ProxyThread extends Thread {

    private Socket           socket      = null;
    private static final int BUFFER_SIZE = 32768;

    public ProxyThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            HttpRequest req = RequestParser.parse(socket.getInputStream());
            RequestHandler handler = new RouteRequestHandler();
            handler.handle(req, socket.getOutputStream());
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(socket);
        }

    }

}
