package com.alibaba.ace.app.jproxy.parser;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.model.HttpRequest;
import com.alibaba.ace.app.jproxy.utils.ParserUtil;

public class HttpRequestParser extends HttpMessageParser<HttpRequest> {

    @Override
    protected HttpRequest parseFirstLine(String line) {
        line = ParserUtil.ensureLine(line);
        String[] arr = line.split("\\s+", 3);
        if (arr.length != 3) {
            //TODO 考虑宽容的解析
            throw new BadRequestException("Bad request line: " + line);
        }
        HttpRequest req = new HttpRequest();
        req.setMethod(arr[0]);
        req.setUrl(arr[1]);
        if (!arr[2].toLowerCase().startsWith("http/")) {
            throw new BadRequestException("Unsupport protocol " + arr[2]);
        }

        req.setProtocol("HTTP");
        String version = arr[2].substring(5, arr[2].length());
        req.setVersion(version);
        return req;
    }

}
