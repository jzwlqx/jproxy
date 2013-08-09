package com.alibaba.ace.app.jproxy.connection;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.model.HttpRequest.Header;
import com.alibaba.ace.app.jproxy.model.Proxy;

/**
 * 就是一个connection
 * 
 * @author jjz
 */
public class HttpConnection {
    private static final Log log = LogFactory.getLog(HttpConnection.class);
    private Proxy            proxy;
    private HttpRequest      req;
    private Socket           socket;
    private URL              url;
    private OutputStream     os;

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

    public void connect() {
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
            os = new BufferedOutputStream(socket.getOutputStream());
        } catch (ConnectException e) {
            throw new ProxyException("can't not connet to " + address + ". " + e.getMessage(), e);

        } catch (SocketTimeoutException ste) {
            throw new ProxyException("connect to " + address + " timeout. " + ste.getMessage(), ste);
        } catch (IOException e) {
            throw new ProxyException("inner io error. " + e.getMessage(), e);
        }
    }

    public void sendRequest() {
        String uri = null;
        if (this.proxy != null) {
            uri = this.req.getUrl();
        } else {
            uri = buildURI(url);
        }

        try {
            writeRequestLine(req.getMethod(), uri, "HTTP/1.1");
            for (Header h : req.getHeaders()) {
                writeHeader(h);
            }
            writeLine(null);

            if (req.getBody() != null) {
                IOUtils.copy(req.getBody(), os);
            }
            os.flush();
        } catch (Exception e) {
            throw new ProxyException("internal error", e);
        }
    }

    private void writeRequestLine(String method, String uri, String protocol)
            throws UnsupportedEncodingException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(uri).append(" ").append(protocol);
        writeLine(sb.toString());
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

    private void writeLine(String line) throws UnsupportedEncodingException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("send: " + line);
        }
        if (line != null) {
            os.write(line.getBytes("ISO-8859-1"));
        }
        os.write("\r\n".getBytes("ISO-8859-1"));
    }

    private void writeHeader(Header header) throws UnsupportedEncodingException, IOException {
        writeLine(header.toString());
    }

    /**
     * 连接并且发送请求后调用
     * 
     * @return
     * @throws IOException
     */
    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            throw new ProxyException("innert io error", e);
        }
    }
}
