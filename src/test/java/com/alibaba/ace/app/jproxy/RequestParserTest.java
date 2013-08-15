package com.alibaba.ace.app.jproxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.ace.app.jproxy.model.HttpRequest;

public class RequestParserTest {

    @Test
    public void testParse() throws IOException {
        String headers = "GET / HTTP/1.1\r\nHost: cn.bing.com\r\nConnection: keep-alive\r\n\r\n";
        HttpRequest req = RequestParser.parse(new ByteArrayInputStream(headers.getBytes()));
        Assert.assertEquals("GET", req.getMethod());
    }
}
