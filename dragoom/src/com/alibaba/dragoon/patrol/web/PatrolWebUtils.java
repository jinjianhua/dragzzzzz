/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.web;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.dragoon.stat.WebSessionStatistic;
import com.alibaba.dragoon.stat.WebURIStatistic;

/**
 * @author shaojin.wensj
 */
public class PatrolWebUtils {

    private static final ThreadLocal<HttpServletRequest>  localRequest        = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> localResponse       = new ThreadLocal<HttpServletResponse>();
    private static final ThreadLocal<WebSessionStatistic> localSessionCounter = new ThreadLocal<WebSessionStatistic>();

    // public final static String ATTR_CONTEXT_PATH =
    // "javax.servlet.ServletContext.contextPath";
    private static String getContextPath_2_5(ServletContext context) {
        String contextPath = context.getContextPath();

        if (contextPath == null || contextPath.length() == 0) {
            contextPath = "/";
        }

        return contextPath;
    }

    public static String getContextPath(ServletContext context) {
        if (context.getMajorVersion() == 2 && context.getMinorVersion() < 5) {
            return null;
        }

        try {
            return getContextPath_2_5(context);
        } catch (NoSuchMethodError error) {
            return null;
        }
    }

    public static HttpServletRequest getHttpServletRequest() {
        return localRequest.get();
    }

    public static void setHttpServletRequest(HttpServletRequest request) {
        localRequest.set(request);
    }

    public static WebSessionStatistic getSessionStatistic() {
        return localSessionCounter.get();
    }

    public static void setSessionStatistic(WebSessionStatistic sessionStat) {
        localSessionCounter.set(sessionStat);
    }

    public static HttpServletResponse getHttpServletResponse() {
        return localResponse.get();
    }

    public static void setHttpServletResponse(HttpServletResponse response) {
        localResponse.set(response);
    }

    /**
     * 获取IP，能够获得经过代理之后的真实IP
     * 
     * @param request
     * @return
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
    
    public static void addError(HttpServletRequest request, Throwable error) {
        addError(request, error, null);
    }

    /**
     * 增加uri错误统计
     * 
     * @param uri
     * @param request
     * @param error
     * @author wangye
     */
    public static void addError(HttpServletRequest request, Throwable error, String lastErrorMessage) {
        WebURIStatistic uriStat = WebURIStatistic.current();
        if (uriStat == null) return;
        // 获取跳转地址
        String referer = request.getHeader("referer");
        // 从request中获取最后访问的用户信息
        WebSessionStatistic sessionStatistic = getSessionStatistic();
        // 不提取原始异常，因为中间可能存在大量的嵌套封装，同时还存在某些异常类的jar并没有被引入jvm
        // error = extractAppException(error);
        // 记录下用户
        String user = sessionStatistic == null ? null : sessionStatistic.getUser();
        String url = getFullUrl(request);
        uriStat.error(lastErrorMessage, error, user, referer, url == null ? null : url);
    }

    /**
     * 提取完整的参数信息，拼接其中的参数
     * 
     * @param request
     * @return
     * @author wangye
     */
    private static String getFullUrl(HttpServletRequest request) {
        StringBuffer sb = request.getRequestURL();
        if (sb == null) {
            return null;
        }
        Enumeration<?> names = request.getParameterNames();
        if (names == null) {
            return sb.toString();
        }
        StringBuffer sb2 = new StringBuffer(sb);
        int length = sb2.length();
        boolean first = true;
        while (names.hasMoreElements() && length < 2000) {
            String name = (String) names.nextElement();
            String value = request.getParameter(name);
            if (name == null) continue;
            name = name.toLowerCase();
            if (first) {
                sb2.append('?');
            } else {
                sb2.append('&');
            }
            sb2.append(name);
            sb2.append('=');
            length += (2 + name.length());
            // 如果这个字段可能是密码的话，直接过滤掉，不会记录下来
            if (name.indexOf("password") >= 0 || name.indexOf("passwd") >= 0 || name.indexOf("pwd") >= 0) {
                String s = "[password unavailable......]";
                sb2.append(s);
                length += s.length();
            }
            // 每对参数也需要过滤，因为post提交会带有很多数据的，截获前100个字符
            else if (value != null && value.length() > 1000) {
                sb2.append(value.subSequence(0, 994));
                sb2.append("......"); // 最后几个字符用......代替
                length += 1000;
            } else {
                sb2.append(value == null ? "" : value);
                length += (value == null ? 0 : value.length());
            }
            first = false;
        }
        // 最后截断成2000个字符
        if (sb2.length() > 2000) {
            return sb2.substring(0, 2000);
        } else {
            return sb2.toString();
        }
    }

    
}
