/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.alibaba.dragoon.patrol.web.PatrolWebUtils;
import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.Profiler.EntryType;
import com.alibaba.dragoon.stat.SpringMethodInvokeStatistic;
import com.alibaba.dragoon.stat.SpringMethodStats;
import com.alibaba.dragoon.stat.SpringMethodStats.MethodInfo;
import com.alibaba.dragoon.stat.SpringStatManager;
import com.alibaba.dragoon.stat.WebSessionStatistic;

public class DragoonMethodInterceptor implements MethodInterceptor, InitializingBean {

    private final static Log  LOG              = LogFactory.getLog(DragoonMethodInterceptor.class);

    private SpringMethodStats methodStats      = SpringStatManager.getInstance().getMethodStat();
    private String            profileType;
    private boolean           profileEnable    = true;

    private EntryType         profileEntryType = EntryType.OTHER;

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public boolean isProfileEnable() {
        return profileEnable;
    }

    public void setProfileEnable(boolean profileEnable) {
        this.profileEnable = profileEnable;
    }

    public String getProfileName() {
        return profileType;
    }

    public void setProfileName(String profileName) {
        this.profileType = profileName;
    }

    public SpringMethodStats getMethodStats() {
        return methodStats;
    }

    public void setMethodStats(SpringMethodStats methodStats) {
        this.methodStats = methodStats;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(methodStats, "methodStats can not be null");

        if (profileType != null) {
            this.profileEntryType = EntryType.valueOf(this.profileType);
        }
    }

    public boolean isStatEnable() {
        return SpringStatManager.getInstance().isStatEnable();
    }

    public boolean isTraceEnable() {
        return false;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!isStatEnable()) {
            return invocation.proceed();
        }

        SpringMethodInvokeStatistic lastStat = SpringMethodInvokeStatistic.current();

        try {
            MethodInfo methodInfo = getMethodInfo(invocation);

            SpringMethodInvokeStatistic stat = methodStats.get(methodInfo);

            long startNano = System.nanoTime();

            beforeInvoke(methodInfo, stat, invocation);

            Profiler.enter(methodInfo.getSignature(), profileEntryType);
            try {
                Object returnObject = invocation.proceed();

                return returnObject;
            } catch (Throwable ex) {
                String user = null;

                WebSessionStatistic sessionStat = PatrolWebUtils.getSessionStatistic();
                if (sessionStat != null) {
                    user = sessionStat.getUser();
                }

                stat.error(ex, user);

                Profiler.setError(ex);

                throw ex;
            } finally {
                Profiler.release();
                long nanoSpan = System.nanoTime() - startNano;
                afterInvoke(methodInfo, stat, nanoSpan);
            }
        } finally {
            SpringMethodInvokeStatistic.setCurrent(lastStat);
        }
    }

    public MethodInfo getMethodInfo(MethodInvocation invocation) {
        return getMethodInfo(invocation, null);
    }

    public MethodInfo getMethodInfo(MethodInvocation invocation, String paramter) {
        Object thisObject = invocation.getThis();
        Method method = invocation.getMethod();

        Class<?> clazz = null;

        try {
            if (thisObject == null) {
                clazz = method.getDeclaringClass();
            } else {
                // 最多支持10层代理
                for (int i = 0; i < 10; ++i) {
                    if (thisObject instanceof org.springframework.aop.framework.Advised) {
                        TargetSource targetSource = ((org.springframework.aop.framework.Advised) thisObject).getTargetSource();

                        if (targetSource == null) {
                            break;
                        }

                        Object target = targetSource.getTarget();
                        if (target != null) {
                            thisObject = target;
                        } else {
                            clazz = targetSource.getTargetClass();
                            break;
                        }
                    } else {
                        break;
                    }
                }

                if (clazz == null) {
                    clazz = thisObject.getClass();

                    if (clazz.getName().startsWith("$")) {
                        clazz = method.getDeclaringClass();
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }

        if (clazz == null) {
            clazz = method.getDeclaringClass();
        }

        return new MethodInfo(clazz, method, paramter);
    }

    private void beforeInvoke(MethodInfo methodInfo, SpringMethodInvokeStatistic stat, MethodInvocation invocation) {
        stat.beforeInvoke();

        WebSessionStatistic sessionStat = PatrolWebUtils.getSessionStatistic();

        if (sessionStat != null && sessionStat.isSpringBeanMethodInvokeBeforeNotifyEnabled()) {
            Notification notification = createBeforeInvokeNotification(methodInfo, invocation);
            sessionStat.sendNotification(notification);
        }

        if (methodStats.isBeforeEventEnabled()) {
            Notification notification = createBeforeInvokeNotification(methodInfo, invocation);
            methodStats.sendNotification(notification);
        }
    }

    private void afterInvoke(MethodInfo methodInfo, SpringMethodInvokeStatistic stat, long nanoSpan) {
        stat.afterInvoke(nanoSpan);

        WebSessionStatistic sessionCounter = PatrolWebUtils.getSessionStatistic();

        if (methodStats.isAfterEventEnabled()) {
            Notification notification = createAfterInvokeNotification(methodInfo);
            methodStats.sendNotification(notification);
        }

        if (sessionCounter != null && sessionCounter.isSpringBeanMethodInvokeAfterNotifyEnabled()) {
            Notification notification = createAfterInvokeNotification(methodInfo);
            sessionCounter.sendNotification(notification);
        }
    }

    private Notification createAfterInvokeNotification(MethodInfo methodInfo) {
        Notification notification = new Notification("method.invokeAfter",
                                                     methodStats.getNotificationSource(methodInfo),
                                                     methodStats.createNotificationSequence(),
                                                     System.currentTimeMillis(), methodInfo.getSignature());
        return notification;
    }

    private Notification createBeforeInvokeNotification(MethodInfo methodInfo, MethodInvocation invocation) {
        Notification notification = new Notification("method.invokeBefore",
                                                     methodStats.getNotificationSource(methodInfo),
                                                     methodStats.createNotificationSequence(),
                                                     System.currentTimeMillis(), methodInfo.getSignature());

        List<Object> args = new ArrayList<Object>();
        for (int i = 0; i < invocation.getArguments().length; ++i) {
            Object arg = invocation.getArguments()[i];
            if (arg == null) {
                args.add(null);
            } else {
                args.add(arg.toString());
            }
        }
        notification.setUserData(args);
        return notification;
    }

}
