package com.alibaba.ace.app.jproxy.parser;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;
import com.alibaba.ace.app.jproxy.model.HttpResponse;
import com.alibaba.ace.app.jproxy.utils.ParserUtil;

public class HttpResponseParser extends HttpMessageParser<HttpResponse> {

    @Override
    protected HttpResponse parseFirstLine(String line) {
        line = ParserUtil.ensureLine(line);
        String[] arr = line.split("\\s+", 3);
        if (arr[0].startsWith("http/")) {
            throw new BadRequestException("first line " + line + " is not valid.");
        }
        HttpResponse response = new HttpResponse();
        response.setProtocol("HTTP");
        String version = arr[0].substring(5, arr[0].length());

        int code = Integer.parseInt(arr[1]);
        String message = arr[2];
        response.setCode(code);
        response.setMessage(message);
        response.setVersion(version);
        return response;
    }
}
