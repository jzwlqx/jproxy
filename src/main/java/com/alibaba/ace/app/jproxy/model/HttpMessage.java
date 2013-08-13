package com.alibaba.ace.app.jproxy.model;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.utils.ParserUtil;

public abstract class HttpMessage {
    private static final Log log = LogFactory.getLog(HttpMessage.class);

    public static class Header {
        private String name;
        private String value;

        public Header() {

        }

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append(": ").append(value);
            return sb.toString();
        }
    }

    private List<HttpMessage.Header> headers;
    private String                   protocol;
    private String                   version;
    private InputStream              content;
    private Map<String, String>      headerMap;

    public List<HttpMessage.Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HttpMessage.Header> headers) {
        this.headers = headers;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVersion() {
        return version;
    }

    public void removeHeader(String name) {
        if (this.headers == null) {
            return;
        }
        Iterator<Header> it = headers.iterator();
        while (it.hasNext()) {
            Header h = it.next();
            if (h.getName().equals(name)) {
                it.remove();
                headerMap.remove(name);
            }
        }

    }

    public void setVersion(String version) {
        this.version = version;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public long getContentLength() {
        String value = headerMap.get("Content-Length");
        return value == null ? -1 : Long.parseLong(value);
    }

    public String getHeader(String name) {
        return headerMap.get(name);
    }

    public boolean isChunked() {
        String transferEncoding = headerMap.get("Transfer-Encoding");
        return transferEncoding != null && transferEncoding.toLowerCase().equals("chunked");
    }

    public void addHeader(Header header) {
        if (this.headers == null) {
            headers = new ArrayList<Header>();
        }
        if (this.headerMap == null) {
            headerMap = new HashMap<String, String>();
        }
        headers.add(header);
        headerMap.put(header.getName(), header.getValue());
    }

    /**
     * 发送到指定的流里
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public void send(OutputStream os) throws IOException {
        os = new BufferedOutputStream(os);
        sendHeadLine(os);
        sendHeaders(os);
        finishHeader(os);
        //write body
        long contentLength = this.getContentLength();
        if (log.isDebugEnabled()) {
            log.debug("will send data size: " + contentLength);
        }
        if (contentLength > 0) {
            sendFully(contentLength, this.getContent(), os);
        } else if (isChunked()) {
            for (;;) {
                String lengthString = ParserUtil.readLine(this.getContent());
                lengthString = ParserUtil.ensureLine(lengthString).trim();
                long size = Long.parseLong(lengthString, 16);
                sendFully(size + 2, this.getContent(), os);
                if (size == 0) {
                    break;
                }
            }
        } else {
            //不知道怎么输出了，子类自己搞定
            unknowSize(os);
        }
        os.flush();
    }

    protected void unknowSize(OutputStream os) throws IOException {

    }

    /**
     * 从输入流读取指定数量的数据发送到输出流. 如果输入流不够，抛出EOFException
     * 
     * @param size
     * @param is
     * @param os
     * @throws IOException
     */
    private void sendFully(long size, InputStream is, OutputStream os) throws IOException {
        long rest = size;
        byte[] buffer = new byte[(int) Math.min(rest, 4 * 1024)];
        while (rest > 0) {
            int toRead = (int) Math.min(rest, buffer.length);
            int read = this.getContent().read(buffer, 0, toRead);
            if (read < 0) {
                throw new EOFException();
            }
            os.write(buffer, 0, read);
            rest -= read;
            if (log.isDebugEnabled()) {
                log.debug("send data size " + read + ", rest is " + rest);
            }
        }
    }

    private void finishHeader(OutputStream os) throws IOException {
        writeLine(null, os);
    }

    private void sendHeaders(OutputStream os) throws IOException {
        for (Header h : this.getHeaders()) {
            writeHeader(h, os);
        }
    }

    protected abstract void sendHeadLine(OutputStream os) throws IOException;

    protected void writeLine(String line, OutputStream os) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("send: " + line);
        }
        try {
            if (line != null) {
                os.write(line.getBytes("ISO-8859-1"));
            }
            os.write("\r\n".getBytes("ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException " + line, e);
        }
    }

    private void writeHeader(HttpMessage.Header header, OutputStream os)
            throws UnsupportedEncodingException, IOException {
        writeLine(header.toString(), os);
    }
}
