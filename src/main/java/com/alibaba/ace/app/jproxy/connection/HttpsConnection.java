package com.alibaba.ace.app.jproxy.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.exception.ProxyException;
import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.model.Proxy;

/**
 * 处理https代理请求
 * 
 * @author jjz
 */
public class HttpsConnection {
    private static final Log log = LogFactory.getLog(HttpsConnection.class);

    private OutputStream     clientOutputStream;
    private Proxy            proxy;
    private HttpRequest      req;
    private Socket           socket;

    public HttpsConnection(HttpRequest req, OutputStream clientOutputStream, Proxy proxy)
            throws UnsupportedEncodingException, IOException {
        this.clientOutputStream = clientOutputStream;
        this.req = req;
        connect();
        responseOK(); //返回第一步200响应
        redirectIO();
    }

    private void redirectIO() throws IOException {
        IORedirector client2Server = new IORedirector(req.getContent(), socket.getOutputStream());

        IORedirector server2Client = new IORedirector(socket.getInputStream(), clientOutputStream);
        client2Server.start();
        server2Client.start();

        try {
            client2Server.join();
            server2Client.join();
        } catch (Exception e) {
            throw new ProxyException(e.getMessage(), e);
        }

    }

    static class IORedirector extends Thread {
        private InputStream  is;
        private OutputStream os;

        public IORedirector(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            try {
                //IOUtils.copy(is, os);
                byte[] buffer = new byte[4 * 1024];
                int count = -1;
                while ((count = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
                os.flush();
            } catch (IOException e) {
                throw new ProxyException(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(this.is);
                IOUtils.closeQuietly(this.os);
            }
        }
    }

    private void responseOK() throws UnsupportedEncodingException, IOException {
        String response = "HTTP/1.1 200 OK\r\nServer: JProxy\r\n\r\n";
        clientOutputStream.write(response.getBytes("ISO-8859-1"));
        clientOutputStream.flush();
    }

    private void connect() {
        String host = null;
        int port = 443;
        if (this.proxy != null) {
            host = proxy.getHost();
            if (proxy.getPort() > 0) {
                port = proxy.getPort();
            }
        } else {
            String[] arr = req.getUrl().split(":", 2);
            host = arr[0];
            if (arr.length >= 2) {
                port = Integer.parseInt(arr[1]);
            }
        }
        InetSocketAddress address = new InetSocketAddress(host, port);
        socket = new Socket();
        try {
            socket.setSoTimeout(20 * 1000);
        } catch (SocketException e) {
            throw new ProxyException("set timeout error", e);
        }
        try {
            socket.connect(address, 3 * 1000);
        } catch (ConnectException e) {
            throw new ProxyException("can't not connet to " + address + ". " + e.getMessage(), e);
        } catch (SocketTimeoutException ste) {
            throw new ProxyException("connect to " + address + " timeout. " + ste.getMessage(), ste);
        } catch (IOException e) {
            throw new ProxyException("inner io error. " + e.getMessage(), e);
        }
    }

}
