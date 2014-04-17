/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.log4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.ThrowableInformation;

import com.alibaba.dragoon.patrol.web.PatrolWebUtils;
import com.alibaba.dragoon.stat.LogStatManager;
import com.alibaba.dragoon.stat.WebSessionStatistic;

public class PatrolLog4jAppender extends AppenderSkeleton implements Appender, OptionHandler {

    private boolean              noneStackEnable = true;

    private final LogStatManager logStatManager  = LogStatManager.getInstance();

    public boolean isNoneStackEnable() {
        return noneStackEnable;
    }

    public void setNoneStackEnable(boolean noneStackEnable) {
        this.noneStackEnable = noneStackEnable;
    }

    @Override
    protected void append(LoggingEvent event) {
        try {
            if (Level.ERROR.equals(event.getLevel())) {

                ThrowableInformation errorInfo = event.getThrowableInformation();

                Throwable error = null;

                if (!isNoneStackEnable() && errorInfo == null) {
                    return;
                }

                logStatManager.incrementErrorCount();
                logStatManager.setLastErrorTime(System.currentTimeMillis());

                if (errorInfo != null) {
                    error = event.getThrowableInformation().getThrowable();
                }

                String lastMessage = null;

                if (error == null) {
                    if (event.getMessage() != null) {
                        lastMessage = event.getMessage().toString();
                    }
                } else {
                    if (event.getMessage() == null) {
                        lastMessage = error.getMessage();
                    } else {
                        lastMessage = event.getMessage() + " | " + error.getMessage();
                    }
                }

                String errorClassName = null;

                if (error != null) {
                    errorClassName = error.getClass().getName();
                }

                if (errorClassName == null) {
                    if (lastMessage != null) {
                        int index = lastMessage.indexOf("::");
                        if (index != -1) {
                            errorClassName = lastMessage.substring(0, index);
                        }
                    }
                }

                if (errorClassName == null) {
                    errorClassName = event.getLoggerName();
                }

                if (errorClassName == null) {
                    LocationInfo locationInfo = event.getLocationInformation();
                    if (locationInfo != null) {
                        errorClassName = locationInfo.getClassName();
                    }
                }

                if (errorClassName == null) {
                    errorClassName = "UnknownException";
                }

                ThrowableInfo throwableInfo = new ThrowableInfo();
                throwableInfo.setType(errorClassName);

                String method = null;
                LocationInfo locationInfo = event.getLocationInformation();
                if (locationInfo != null && errorClassName.equals(locationInfo.getClassName())) {
                    method = locationInfo.getMethodName();
                }

                // 获得抛出异常的方法名称
                if (error != null && error.getStackTrace() != null && error.getStackTrace().length != 0) {
                    StackTraceElement lastElem = error.getStackTrace()[0]; // ArrayOutOfBoundException
                    if (method == null) {
                        method = lastElem.getClassName() + "." + lastElem.getMethodName();
                    }
                }

                if (method == null) {
                    throwableInfo.setMethod("unknown");
                }

                throwableInfo.setMethod(method);

                String stackTrace = null;
                if (error != null) {
                    StringWriter buf = new StringWriter();
                    error.printStackTrace(new PrintWriter(buf));
                    stackTrace = buf.toString();
                }

                ThrowableInfoStat exceptionStat = logStatManager.get(throwableInfo);

                if (exceptionStat == null) {
                    logStatManager.putIfAbsent(throwableInfo, new ThrowableInfoStat(throwableInfo));
                    exceptionStat = logStatManager.get(throwableInfo);
                }

                HttpServletRequest request = PatrolWebUtils.getHttpServletRequest();
                if (request != null) {
                    exceptionStat.setLastUrl(request.getRequestURL().toString());
                }

                exceptionStat.setLastMessage(lastMessage);
                exceptionStat.setLastStackTrace(stackTrace);
                exceptionStat.setLastDate(new Date()); // 不使用event.getTimeStamp()，因为有的log4j版本没有getTimeStamp方法
                exceptionStat.incrementCount();

                Map<String, Object> source = new HashMap<String, Object>();
                {
                    if (request != null) {
                        source.put("requestURI", request.getRequestURI());
                        source.put("remoteAddr", PatrolWebUtils.getRemoteAddr(request));
                    }
                    source.put("thread", Thread.currentThread().getName());

                    WebSessionStatistic sessionStat = PatrolWebUtils.getSessionStatistic();
                    if (sessionStat != null) {
                        String user = sessionStat.getUser();
                        if (user != null) {
                            source.put("user", user);
                        }
                    }
                }

                // added by wangye 针对这个uri产生的这个错误累积，需要累积到uriStat上去（如果异常被抛出，则会在uri的异常累积中增加；如果被截获，则会在这里被累积）
                PatrolWebUtils.addError(request, error, lastMessage);
            } else if (Level.INFO.equals(event.getLevel())) {
                logStatManager.incrementInfoCount();
            } else if (Level.DEBUG.equals(event.getLevel())) {
                logStatManager.incrementDebugCount();
            } else if (Level.WARN.equals(event.getLevel())) {
                logStatManager.incrementWarnCount();
                // PatrolLog4jAppender兼容log4j 1.2.12之前版本
                // } else if (Level.TRACE.equals(event.getLevel())) {
                // logStatManager.incrementTraceCount();
            } else if (Level.FATAL.equals(event.getLevel())) {
                logStatManager.incrementFatalCount();
            } else {
                logStatManager.incrementOtherCount();
            }
        } catch (Throwable ex) {
            LogLog.error("handle append error", ex);
        }
    }

    public boolean requiresLayout() {
        return false;
    }

    public void close() {
        super.closed = true;
    }

}
