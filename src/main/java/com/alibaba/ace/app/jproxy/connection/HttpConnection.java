package com.alibaba.ace.app.jproxy.connection;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.exception.ProxyException;
import com.alibaba.ace.app.jproxy.model.HttpMessage;
import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.model.HttpResponse;
import com.alibaba.ace.app.jproxy.model.Proxy;
import com.alibaba.ace.app.jproxy.parser.HttpResponseParser;

/**
 * 就是一个connection
 * 
 * @author jjz
 */
public class HttpConnection {
    private static final Log  log = LogFactory.getLog(HttpConnection.class);

    private Proxy             proxy;
    private HttpRequest       req;
    private Socket            socket;
    private URL               url;
    private InetSocketAddress address;

    public HttpConnection(HttpRequest req, Proxy proxy) {
        this.req = req;
        this.proxy = proxy;
        if (proxy == null) {
            try {
                url = new URL(req.getUrl());
            } catch (MalformedURLException e) {
                throw new BadRequestException(req.getUrl() + " is not a valid url.", e);
            }
        }
    }

    /**
     * 发送请求并且拿到响应
     * 
     * @return
     * @throws IOException
     */
    public HttpResponse request() throws IOException {
        req = createRequest();
        connect();
        req.send(socket.getOutputStream());
        HttpResponseParser parser = new HttpResponseParser();
        return parser.parse(socket.getInputStream());
    }

    /**
     * 规整请求里的信息
     * 
     * @return
     */
    private HttpRequest createRequest() {
        req.removeHeader("Proxy-Connection");
        if (this.proxy == null) {
            req.setUrl(buildURI(url));
            req.addHeader(new HttpMessage.Header("Connection", "close"));
        } else {
            req.addHeader(new HttpMessage.Header("Proxy-Connection", "close"));
        }
        return req;
    }

    private String buildURI(URL url) {
        StringBuilder sb = new StringBuilder();
        sb.append(url.getPath());
        if (StringUtils.isNotEmpty(url.getQuery())) {
            sb.append("?");
            sb.append(url.getQuery());
        }
        if (StringUtils.isNotEmpty(url.getRef())) {
            sb.append("#");
            sb.append(url.getRef());
        }
        return sb.toString();
    }

    public void close() {
        IOUtils.closeQuietly(socket);
    }

    private void connect() {
        String host = null;
        int port = -1;
        if (this.proxy != null) {
            host = proxy.getHost();
            port = proxy.getPort();
            if (port <= 0) {
                port = 80;
            }
        } else {
            host = url.getHost();
            port = url.getPort() > 0 ? url.getPort() : url.getDefaultPort();
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
