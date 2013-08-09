package com.alibaba.ace.app.jproxy.exception;

/**
 * 代理服务器内部错误
 * 
 * @author jjz
 */
public class ProxyException extends RuntimeException {

    public ProxyException(String message) {
        super(message);
    }

    public ProxyException(String message, Throwable cause) {
        super(message, cause);
    }

}
