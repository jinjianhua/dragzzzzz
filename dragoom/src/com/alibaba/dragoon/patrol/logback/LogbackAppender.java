package com.alibaba.dragoon.patrol.logback;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;

import com.alibaba.dragoon.patrol.log4j.ThrowableInfo;
import com.alibaba.dragoon.patrol.log4j.ThrowableInfoStat;
import com.alibaba.dragoon.patrol.web.PatrolWebUtils;
import com.alibaba.dragoon.stat.LogStatManager;
import com.alibaba.dragoon.stat.WebSessionStatistic;

/**
 * 用于将异常信息推送到Dragoon监控的Appender Dragoon2.6.2版本提供的Logback支持(com.alibaba.dragoon.patrol.logback.LogbackAppender)无法使用
 * 
 * @author hailve.linhl
 */
public class LogbackAppender<E> extends AppenderBase<E> {

    private final LogStatManager logStatManager  = LogStatManager.getInstance();

    private boolean              noneStackEnable = true;

    /*
     * (non-Javadoc)
     * @see ch.qos.logback.core.AppenderBase#append(java.lang.Object)
     */
    @Override
    protected void append(E eventObject) {
        try {
            ILoggingEvent event = (ILoggingEvent) eventObject;
            if (Level.ERROR.equals(event.getLevel())) {

                logStatManager.incrementErrorCount();
                logStatManager.setLastErrorTime(System.currentTimeMillis());

                IThrowableProxy throwableProxy = event.getThrowableProxy();

                if (!isNoneStackEnable() && throwableProxy == null) {
                    return;
                }

                String lastMessage = null;

                if (throwableProxy == null) {
                    if (event.getMessage() != null) {
                        lastMessage = event.getMessage().toString();
                    }
                } else {
                    if (event.getMessage() == null) {
                        lastMessage = throwableProxy.getMessage();
                    } else {
                        lastMessage = event.getMessage() + " | " + throwableProxy.getMessage();
                    }
                }

                String errorClassName = null;

                if (throwableProxy != null) {
                    errorClassName = throwableProxy.getClassName();
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
                    StackTraceElement[] callerData = event.getCallerData();
                    if (callerData != null && callerData.length > 0 && callerData[0] != null) {
                        errorClassName = callerData[0].getClassName();
                    }
                }

                if (errorClassName == null) {
                    errorClassName = "UnknownException";
                }

                ThrowableInfo throwableInfo = new ThrowableInfo();
                throwableInfo.setType(errorClassName);

                String method = null;

                {
                    StackTraceElement[] callerData = event.getCallerData();
                    if (callerData != null && callerData.length > 0 && callerData[0] != null) {
                        method = callerData[0].getMethodName();
                    }
                }

                // 获得抛出异常的方法名称
                if (throwableProxy != null) {
                    StackTraceElementProxy[] stackTraceElementProxyArray = throwableProxy.getStackTraceElementProxyArray();
                    if (throwableProxy != null && stackTraceElementProxyArray != null
                        && stackTraceElementProxyArray.length != 0) {
                        StackTraceElementProxy lastElem = stackTraceElementProxyArray[0]; // ArrayOutOfBoundException
                        if (method == null && lastElem != null && lastElem.getStackTraceElement() != null) {
                            method = lastElem.getStackTraceElement().getClassName() + "."
                                     + lastElem.getStackTraceElement().getMethodName();
                        }
                    }
                }

                if (method == null) {
                    throwableInfo.setMethod("unknown");
                }

                throwableInfo.setMethod(method);

                String stackTrace = null;
                if (layout != null) {
                    stackTrace = layout.doLayout(eventObject);
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
                exceptionStat.setLastStackTrace(stackTrace); // Dragoon2.6.2没有这个方法；Dragoon2.6.5中可以把这句话加上
                exceptionStat.setLastDate(new Date());
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
                Throwable error = null;
                if (throwableProxy instanceof ThrowableProxy) {
                    ThrowableProxy proxy = (ThrowableProxy) throwableProxy;
                    error = proxy.getThrowable();
                } else {
                    error = new Throwable(lastMessage, new Throwable(stackTrace));
                }
                PatrolWebUtils.addError(request, error, lastMessage);
            } else if (Level.INFO.equals(event.getLevel())) {
                logStatManager.incrementInfoCount();
            } else if (Level.DEBUG.equals(event.getLevel())) {
                logStatManager.incrementDebugCount();
            } else if (Level.WARN.equals(event.getLevel())) {
                logStatManager.incrementWarnCount();
            } else {
                logStatManager.incrementOtherCount();
            }
        } catch (Throwable ex) {
            addError("handle append error", ex);
        }
    }

    public boolean isNoneStackEnable() {
        return noneStackEnable;
    }

    public void setNoneStackEnable(boolean noneStackEnable) {
        this.noneStackEnable = noneStackEnable;
    }
}
