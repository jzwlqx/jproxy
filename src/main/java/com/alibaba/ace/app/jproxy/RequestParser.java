package com.alibaba.ace.app.jproxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.model.HttpRequest.Header;

public class RequestParser {
    private static Log log = LogFactory.getLog(RequestParser.class);

    /**
     * 从InputStream读取，解析成HttpRequest对象。如果是post请求，InputStream中剩下的部分（request
     * body）不会解析
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public static HttpRequest parse(InputStream is) throws IOException {
        HttpRequest req = new HttpRequest();
        parseFirstLine(is, req);
        parseHeaders(is, req);
        if ("post".equalsIgnoreCase(req.getMethod())) {
            req.setBody(is);
        }
        return req;
    }

    private static void parseHeaders(InputStream is, HttpRequest req) throws IOException {
        String line = null;
        List<Header> headerList = new ArrayList<Header>();
        while (!(line = ensureReadLine(is)).isEmpty()) {
            Header h = parseHeader(line);
            headerList.add(h);
        }
        req.setHeaders(headerList);
    }

    private static Header parseHeader(String line) {
        String[] arr = line.split(":");
        if (arr.length != 2) {
            throw new BadRequestException(line + " is not valid http header.");
        }
        String key = arr[0].trim();
        String value = arr[1].trim();
        Header h = new Header();
        h.setName(key);
        h.setValue(value);
        return h;
    }

    private static void parseFirstLine(InputStream is, HttpRequest req) throws IOException {
        String line = ensureReadLine(is);
        String[] arr = line.split("\\s+", 3);
        if (arr.length != 3) {
            //TODO 考虑宽容的解析
            throw new BadRequestException("Bad request line: " + line);
        }
        req.setMethod(arr[0]);
        req.setUrl(arr[1]);
        if (!arr[2].toLowerCase().startsWith("http/")) {
            throw new BadRequestException("Unsupport protocol " + arr[2]);
        }

        req.setProtocol("http");
        String version = arr[2].substring(5, arr[2].length());
        req.setVersion(version);
    }

    /**
     * 读取一个一行，并且验证行以\r\n结尾，否则抛出异常。
     * 
     * @param is
     * @return 去掉\r\n之后的行
     * @throws IOException
     */
    private static String ensureReadLine(InputStream is) throws IOException {
        String line = readLine(is);
        if (!line.endsWith("\r\n")) {
            throw new BadRequestException(line + " not end with \r\n");
        }
        return line.substring(0, line.length() - 2);
    }

    /**
     * 读取\r\n分割的一行
     * 
     * @param is
     * @return 没有去除\r\n的解析结果
     * @throws IOException
     */
    private static String readLine(InputStream is) throws IOException {
        boolean last = false; //wait for \n?
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean finish = false;
        while (!finish) {
            int b = is.read();
            switch (b) {
                case -1:
                    finish = true;
                    break;
                case '\n':
                    os.write(b);
                    if (last) {
                        finish = true;
                    }
                    break;
                case '\r':
                    last = true;
                    os.write(b);
                    break;
                default:
                    os.write(b);
            }
        }
        return new String(os.toByteArray(), "ISO-8859-1");
    }
}
