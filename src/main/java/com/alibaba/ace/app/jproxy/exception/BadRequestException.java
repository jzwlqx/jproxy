package com.alibaba.ace.app.jproxy.exception;

/**
 * http请求格式错误
 * 
 * @author jjz
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
