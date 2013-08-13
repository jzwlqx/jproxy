package com.alibaba.ace.app.jproxy.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.alibaba.ace.app.jproxy.exception.BadRequestException;

public class ParserUtil {

    /**
     * 读取\r\n分割的一行
     * 
     * @param is
     * @return 没有去除\r\n的解析结果. 如果已经到结束，返回null
     * @throws IOException
     */
    public static String readLine(InputStream is) throws IOException {
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

    /**
     * 确认行尾以\r\n结尾并去除之。
     * 
     * @param line
     * @return 去除\r\n之后的字符串
     */
    public static String ensureLine(String line) {
        if (line == null || !line.endsWith("\r\n")) {
            throw new BadRequestException(line + " not end with \r\n");
        }
        return line.substring(0, line.length() - 2);
    }
}
