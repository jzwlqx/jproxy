package com.alibaba.ace.app.jproxy.route;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.ace.app.jproxy.model.Proxy;

/**
 * @author jjz
 */
public class ProxySelector {

    private static final Log                  log             = LogFactory
                                                                      .getLog(ProxySelector.class);
    private static CopyOnWriteArrayList<Rule> rules           = new CopyOnWriteArrayList<Rule>();
    private static final String               SYSTEM_PROPERTY = "route.configure";

    static {
        //读取配置文件，先从系统配置找。如果没有，再找当前目录下的conf目录
        InputStream is = null;
        try {
            if (System.getProperty(SYSTEM_PROPERTY) != null) {
                is = new FileInputStream(System.getProperty(SYSTEM_PROPERTY));
            } else {
                File f = new File("conf/route.properties");
                if (f.exists()) {
                    is = new FileInputStream(f);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Read Config Error.", e);
        }
        if (is == null) {
            log.warn("Could not find route.properties, will skip route.");
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#") || line.isEmpty()) {
                        continue;
                    }
                    Rule rule = new Rule(line);
                    rules.add(rule);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    private static String removeQueryString(String url) {
        int pos = url.indexOf("?");
        if (pos >= 0) {
            return url.substring(0, pos);
        }
        return url;
    }

    /**
     * 路由规则
     * 
     * @author jjz
     */
    static class Rule {
        private Pattern pattern;
        private Proxy   proxy;
        private boolean reverse = false;

        /**
         * rule格式： pattern proxy [!i]
         * 
         * @param rule
         */
        public Rule(String rule) {
            String[] arr = rule.split("\\s+");
            if (arr.length > 3) {
                throw new RuntimeException(rule + " is not valid.");
            }
            boolean ignorecase = false;
            if (arr.length == 3) {
                for (int i = 0; i < arr[2].length(); i++) {
                    char c = arr[2].charAt(i);
                    switch (c) {
                        case '!':
                            reverse = true;
                            break;
                        case 'i':
                            ignorecase = true;
                            break;
                        default:
                            throw new RuntimeException("Unknown flag " + c
                                    + ", valid flag is ! and i");
                    }
                }
            }
            if (ignorecase) {
                pattern = Pattern.compile(arr[0], Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile(arr[0]);
            }

            proxy = new Proxy();
            if (arr[1].contains(":")) {
                String[] address = arr[1].split(":", 2);
                proxy.setHost(address[0]);
                proxy.setPort(Integer.parseInt(address[1]));
            } else {
                proxy.setHost(arr[1]);
                proxy.setPort(80);
            }

        }

        /**
         * 判断请求url是否匹配当前规则。如果匹配，返回路由，否则返回null
         * 
         * @param url
         * @return
         */
        Proxy test(String url) {
            Matcher m = this.pattern.matcher(url);
            boolean match = m.matches();
            if (match != reverse) {
                return proxy;
            }
            return null;
        }
    }

    /**
     * 移除诸如http这样的schema
     * 
     * @param url
     * @return
     */
    private static String removeSchema(String url) {
        int pos = url.indexOf("://");
        if (pos >= 0) {
            return url.substring(pos + 3);
        }
        return url;
    }

    public static Proxy select(String url) {
        url = removeQueryString(url);
        url = removeSchema(url);
        Proxy p = null;
        for (Rule rule : rules) {
            if (log.isDebugEnabled()) {
                log.debug("Test " + url + " on " + rule.pattern);
            }
            p = rule.test(url);
            if (p != null) {
                break;
            }
        }
        return p;
    }

}
