package com.alibaba.ace.app.jproxy;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class SimpleRequest {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "10000");

        URL url = new URL("http://www.google.com/");
        //URL url = new URL("http://www.alibaba.com/");

        String s = IOUtils.toString(url.openStream());
        System.out.println(s);
    }

}
