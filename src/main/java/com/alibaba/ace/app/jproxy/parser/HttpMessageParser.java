package com.alibaba.ace.app.jproxy.parser;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.model.HttpMessage;
import com.alibaba.ace.app.jproxy.utils.ParserUtil;

/**
 * 解析http消息
 * 
 * @author jjz
 */
public abstract class HttpMessageParser<T extends HttpMessage> {

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
    public T parse(InputStream is) throws IOException {
        String line = null;
        Stage stage = Stage.HEADLINE;
        T ret = null;
        for (;;) {
            line = ParserUtil.readLine(is);
            switch (stage) {
                case HEADLINE:
                    if (line == null)
                        return null;
                    ret = parseFirstLine(line);
                    stage = Stage.HEADER;
                    break;
                case HEADER:
                    if (line.equals("\r\n")) {
                        ret.setContent(is);
                        return ret;
                    } else {
                        parseHeader(line, ret);
                    }
            }
        }
    }

    private void parseHeader(String line, T t) {
        line = ParserUtil.ensureLine(line);
        String[] arr = line.split(":", 2);
        if (arr.length != 2) {
            throw new BadRequestException(line + " is not valid http header.");
        }
        String key = arr[0].trim();
        String value = arr[1].trim();
        HttpMessage.Header h = new HttpMessage.Header();
        h.setName(key);
        h.setValue(value);
        t.addHeader(h);
    }

    //    protected  void parseFirstLine(String line, HttpRequest req) throws IOException {
    //        line = ensureLine(line);
    //        String[] arr = line.split("\\s+", 3);
    //        if (arr.length != 3) {
    //            //TODO 考虑宽容的解析
    //            throw new BadRequestException("Bad request line: " + line);
    //        }
    //        req.setMethod(arr[0]);
    //        req.setUrl(arr[1]);
    //        if (!arr[2].toLowerCase().startsWith("http/")) {
    //            throw new BadRequestException("Unsupport protocol " + arr[2]);
    //        }
    //
    //        req.setProtocol("http");
    //        String version = arr[2].substring(5, arr[2].length());
    //        req.setVersion(version);
    //    }

    protected abstract T parseFirstLine(String line);

}
