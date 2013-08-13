package com.alibaba.ace.app.jproxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.model.HttpMessage;
import com.alibaba.ace.app.jproxy.model.HttpRequest;

public class RequestParser {
    private static Log log = LogFactory.getLog(RequestParser.class);

    private static enum Stage {
        HEADLINE,
        HEADER
    }

    /**
     * 从InputStream读取，解析成HttpRequest对象。如果是post请求，InputStream中剩下的部分（request
     * body）不会解析.
     * 
     * @param is
     * @return HttpRequest对象。如果流已经关闭，返回null
     * @throws IOException
     * @throws BadRequestException 如果不是期望的解析格式
     */
    public static HttpRequest parse(InputStream is) throws IOException {
        String line = null;
        Stage stage = Stage.HEADLINE;
        HttpRequest request = new HttpRequest();
        for (;;) {
            line = readLine(is);
            switch (stage) {
                case HEADLINE:
                    if (line == null)
                        return null;
                    parseFirstLine(line, request);
                    stage = Stage.HEADER;
                    break;
                case HEADER:
                    if (line.equals("\r\n")) {
                        return request;
                    } else {
                        parseHeader(line, request);
                    }
            }
        }
    }

    private static void parseHeader(String line, HttpRequest request) {
        line = ensureLine(line);
        String[] arr = line.split(":", 2);
        if (arr.length != 2) {
            throw new BadRequestException(line + " is not valid http header.");
        }
        String key = arr[0].trim();
        String value = arr[1].trim();
        HttpMessage.Header h = new HttpMessage.Header();
        h.setName(key);
        h.setValue(value);
        request.addHeader(h);
    }

    private static void parseFirstLine(String line, HttpRequest req) throws IOException {
        line = ensureLine(line);
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
     * 确认行尾以\r\n结尾并去除之。
     * 
     * @param line
     * @return 去除\r\n之后的字符串
     */
    protected static String ensureLine(String line) {
        if (line == null || !line.endsWith("\r\n")) {
            throw new BadRequestException(line + " not end with \r\n");
        }
        return line.substring(0, line.length() - 2);
    }

    /**
     * 读取\r\n分割的一行
     * 
     * @param is
     * @return 没有去除\r\n的解析结果. 如果已经到结束，返回null
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
        return os.size() > 0 ? new String(os.toByteArray(), "ISO-8859-1") : null;
    }
}
