package com.alibaba.ace.app.jproxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {

    public static void main(String[] args) throws IOException {
        int port = 10080; //default
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                throw new IllegalArgumentException("Illegal port " + args[0]);
            }
        }

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Http proxy started on port " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        while (true) {
            //TODO 使用nio reactor模式
            Socket s = serverSocket.accept();
            System.out.println("request: " + s.getRemoteSocketAddress());
            Thread t = new ProxyThread(s);
            t.start();
        }
    }
}
