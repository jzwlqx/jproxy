package com.alibaba.ace.app.jproxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        int port = 10000; //default
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            //ignore me
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Started on: " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        while (true) {
            Socket s = serverSocket.accept();
            System.out.println("request: " + s.getRemoteSocketAddress());
            Thread t = new ProxyThread(s);
            t.start();

        }
        //new ProxyThread(serverSocket.accept()).start();
        //serverSocket.close();
    }
}
