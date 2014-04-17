/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import javax.servlet.http.HttpSession;

import com.alibaba.dragoon.common.utils.JMXUtils;
import com.alibaba.dragoon.patrol.web.PatrolServletSessionListener;

/**
 * 这是一个基于HttpSession的计数器，用于单独监控一个Session的SQL运行情况
 * 
 * @author shaojin.wensj
 */
public class WebSessionStatistic extends NotificationBroadcasterSupport implements WebSessionStatisticMBean {

    private final HttpSession session;

    private final AtomicLong  jdbcStatementExecuteErrorCounter             = new AtomicLong();

    private final AtomicLong  jdbcStatementExecuteSuccessCounter           = new AtomicLong();
    private final AtomicLong  jdbcStatementExecuteSpanNanoTotal            = new AtomicLong();

    private boolean           jdbcStatementCreateAfterNotifyEnabled        = false;
    private boolean           jdbcStatementPrepareCallAfterNotifyEnabled   = false;
    private boolean           jdbcStatementPrepareAfterNotifyEnabled       = false;

    private boolean           jdbcStatementExecuteAfterNotifyEnabled       = false;
    private boolean           jdbcStatementExecuteUpdateAfterNotifyEnabled = false;
    private boolean           jdbcStatementExecuteQueryAfterNotifyEnabled  = false;
    private boolean           jdbcStatementExecuteBatchAfterNotifyEnabled  = false;

    private boolean           jdbcStatementExecuteErrorAfterNotifyEnabled  = false;
    private boolean           jdbcStatementCloseAfterNotifyEnabled         = false;

    private boolean           jdbcResultSetCloseAfterNotifyEnabled         = false;

    private boolean           springBeanMethodInvokeBeforeNotifyEnabled    = false;
    private boolean           springBeanMethodInvokeAfterNotifyEnabled     = false;

    private String            remoteAddress;

    private AtomicLong        accessedPageCounter                          = new AtomicLong(0);

    private String            userAttributeName;

    private ObjectName        objectName;

    private final AtomicLong  resetCount                                   = new AtomicLong();

    public WebSessionStatistic(HttpSession session, ObjectName objectName, String userAttributeName){
        this.session = session;
        this.objectName = objectName;
        this.userAttributeName = userAttributeName;
    }

    public String getUserAttributeName() {
        return this.userAttributeName;
    }

    public void setUserAttributeName(String userAttributeName) {
        this.userAttributeName = userAttributeName;
    }

    public boolean isSpringBeanMethodInvokeBeforeNotifyEnabled() {
        return springBeanMethodInvokeBeforeNotifyEnabled;
    }

    public void setSpringBeanMethodInvokeBeforeNotifyEnabled(boolean springBeanMethodInvokeBeforeNotifyEnable) {
        this.springBeanMethodInvokeBeforeNotifyEnabled = springBeanMethodInvokeBeforeNotifyEnable;
    }

    public boolean isSpringBeanMethodInvokeAfterNotifyEnabled() {
        return springBeanMethodInvokeAfterNotifyEnabled;
    }

    public void setSpringBeanMethodInvokeAfterNotifyEnabled(boolean springBeanMethodInvokeAfterNotifyEnable) {
        this.springBeanMethodInvokeAfterNotifyEnabled = springBeanMethodInvokeAfterNotifyEnable;
    }

    public final HttpSession getSession() {
        return session;
    }

    public final String getUser() {
        if (userAttributeName != null) {
            Object attrVal = this.session.getAttribute(userAttributeName);
            if (attrVal != null) {
                return attrVal.toString();
            }

            return null;
        }

        return null;
    }

    public final String getRemoteAddress() {
        return remoteAddress;
    }

    public final void setRemoteAddress(String initialRemoteAddress) {
        this.remoteAddress = initialRemoteAddress;
    }

    // ////////////////////////// Servlet Begin ////////////////////////////

    public final String getSessionId() {
        return session.getId();
    }

    public Date getSessionCreationTime() {
        return new Date(session.getCreationTime());
    }

    public Date getSessionLastAccessedTime() {
        return new Date(session.getLastAccessedTime());
    }

    public int getSessionMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    public void setSessionMaxInactiveInterval(int interval) {
        this.session.setMaxInactiveInterval(interval);
    }

    /**
     * 获取属性值，如果值不能够被序列化，调用toString()返回
     */
    public Object getSessionAttribute(String name) {
        if (name == null) {
            return null;
        }

        Object value = this.session.getAttribute(name);

        if (value instanceof CompositeData) {
            return value;
        }

        if (value instanceof TabularData) {
            return value;
        }

        if (value == null || value instanceof Serializable) {
            return value;
        }

        return value.toString();
    }

    public void setSessionAttribute(String name, Object value) {
        this.session.setAttribute(name, value);
    }

    public void sessionRemoveAttribute(String name) {
        this.session.removeAttribute(name);
    }

    public String getSessionClassName() {
        return this.session.getClass().getName();
    }

    @SuppressWarnings("rawtypes")
    public List<String> getSessionAttributeNames() {
        List<String> names = new ArrayList<String>();
        for (Enumeration e = this.session.getAttributeNames(); e.hasMoreElements();) {
            names.add((String) e.nextElement());
        }

        return names;
    }

    @SuppressWarnings("rawtypes")
    public Map<String, Object> getSessionAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        for (Enumeration e = this.session.getAttributeNames(); e.hasMoreElements();) {
            String attrName = (String) e.nextElement();
            attributes.put(attrName, getSessionAttribute(attrName));
        }
        return attributes;
    }

    /**
     * Invalidates this session then unbinds any objects bound to it.
     * 
     * @exception IllegalStateException if this method is called on an already invalidated session
     */
    public void sessionInvalidate() {
        this.session.invalidate();
    }

    // ////////////////////////// Servlet End////////////////////////////

    public boolean cancelMonitor() {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.getMBeanName());
        } catch (JMException e) {
            return false;
        }

        WebSessionStatistic val = PatrolServletSessionListener.monitoredCounters.remove(this.getSessionId());

        return val != null;

    }

    public ObjectName getMBeanName() {
        return objectName;
    }

    // ////////

    public long getJdbcStatementExecuteSuccessCount() {
        return jdbcStatementExecuteSuccessCounter.get();
    }

    public void incrementJdbcStatementExecuteSuccessCount() {
        this.jdbcStatementExecuteSuccessCounter.incrementAndGet();
    }

    public long getJdbcStatementExecuteSpanNanoTotal() {
        return jdbcStatementExecuteSpanNanoTotal.get();
    }

    public void addJdbcStatementExecuteSpanNano(long nano) {
        this.jdbcStatementExecuteSpanNanoTotal.addAndGet(nano);
    }

    public long getJdbcStatementExecuteErrorCount() {
        return jdbcStatementExecuteErrorCounter.get();
    }

    public void incrementJdbcStatementExecuteErrorCount() {
        this.jdbcStatementExecuteErrorCounter.incrementAndGet();
    }

    public void reset() {
        this.jdbcStatementExecuteSuccessCounter.set(0);
        this.jdbcStatementExecuteSpanNanoTotal.set(0);
        this.jdbcStatementExecuteErrorCounter.set(0);

        resetCount.incrementAndGet();
    }

    public long getResetCount() {
        return resetCount.get();
    }

    // ////////////

    public void setJdbcStatementExecuteNotifyEnableAll() {
        this.jdbcStatementCloseAfterNotifyEnabled = true;
        this.jdbcStatementCreateAfterNotifyEnabled = true;
        this.jdbcStatementExecuteAfterNotifyEnabled = true;
        this.jdbcStatementExecuteUpdateAfterNotifyEnabled = true;
        this.jdbcStatementExecuteQueryAfterNotifyEnabled = true;
        this.jdbcStatementExecuteBatchAfterNotifyEnabled = true;
        this.jdbcStatementExecuteErrorAfterNotifyEnabled = true;
        this.jdbcStatementPrepareCallAfterNotifyEnabled = true;
        this.jdbcStatementPrepareAfterNotifyEnabled = true;
    }

    public void setJdbcStatementExecuteNotifyDiableAll() {
        this.jdbcStatementCloseAfterNotifyEnabled = false;
        this.jdbcStatementCreateAfterNotifyEnabled = false;
        this.jdbcStatementExecuteAfterNotifyEnabled = false;
        this.jdbcStatementExecuteUpdateAfterNotifyEnabled = false;
        this.jdbcStatementExecuteQueryAfterNotifyEnabled = false;
        this.jdbcStatementExecuteBatchAfterNotifyEnabled = false;
        this.jdbcStatementExecuteErrorAfterNotifyEnabled = false;
        this.jdbcStatementPrepareCallAfterNotifyEnabled = false;
        this.jdbcStatementPrepareAfterNotifyEnabled = false;
    }

    public boolean isJdbcStatementCloseAfterNotifyEnabled() {
        return jdbcStatementCloseAfterNotifyEnabled;
    }

    public void setJdbcStatementCloseAfterNotifyEnabled(boolean afterStatementCloseNotifyEnable) {
        this.jdbcStatementCloseAfterNotifyEnabled = afterStatementCloseNotifyEnable;
    }

    public boolean isJdbcStatementCreateAfterNotifyEnabled() {
        return jdbcStatementCreateAfterNotifyEnabled;
    }

    public void setJdbcStatementCreateAfterNotifyEnabled(boolean afterStatementCreateNotifyEnable) {
        this.jdbcStatementCreateAfterNotifyEnabled = afterStatementCreateNotifyEnable;
    }

    public boolean isJdbcStatementExecuteBatchAfterNotifyEnabled() {
        return jdbcStatementExecuteBatchAfterNotifyEnabled;
    }

    public void setJdbcStatementExecuteBatchAfterNotifyEnabled(boolean afterStatementExecuteBatchNotifyEnable) {
        this.jdbcStatementExecuteBatchAfterNotifyEnabled = afterStatementExecuteBatchNotifyEnable;
    }

    public boolean isJdbcStatementExecuteAfterNotifyEnabled() {
        return jdbcStatementExecuteAfterNotifyEnabled;
    }

    public void setJdbcStatementExecuteAfterNotifyEnabled(boolean afterStatementExecuteNotifyEnable) {
        this.jdbcStatementExecuteAfterNotifyEnabled = afterStatementExecuteNotifyEnable;
    }

    public boolean isJdbcStatementExecuteQueryAfterNotifyEnabled() {
        return jdbcStatementExecuteQueryAfterNotifyEnabled;
    }

    public void setJdbcStatementExecuteQueryAfterNotifyEnabled(boolean afterStatementExecuteQueryNotifyEnable) {
        this.jdbcStatementExecuteQueryAfterNotifyEnabled = afterStatementExecuteQueryNotifyEnable;
    }

    public boolean isJdbcStatementExecuteUpdateAfterNotifyEnabled() {
        return jdbcStatementExecuteUpdateAfterNotifyEnabled;
    }

    public void setJdbcStatementExecuteUpdateAfterNotifyEnabled(boolean afterStatementExecuteUpdateNotifyEnable) {
        this.jdbcStatementExecuteUpdateAfterNotifyEnabled = afterStatementExecuteUpdateNotifyEnable;
    }

    public boolean isJdbcStatementPrepareCallAfterNotifyEnabled() {
        return jdbcStatementPrepareCallAfterNotifyEnabled;
    }

    public void setJdbcStatementPrepareCallAfterNotifyEnabled(boolean afterStatementPrepareCallNotifyEnable) {
        this.jdbcStatementPrepareCallAfterNotifyEnabled = afterStatementPrepareCallNotifyEnable;
    }

    public boolean isJdbcStatementPrepareAfterNotifyEnabled() {
        return jdbcStatementPrepareAfterNotifyEnabled;
    }

    public void setJdbcStatementPrepareAfterNotifyEnabled(boolean afterStatementPrepareNotifyEnable) {
        this.jdbcStatementPrepareAfterNotifyEnabled = afterStatementPrepareNotifyEnable;
    }

    public boolean isJdbcResultSetCloseAfterNotifyEnabled() {
        return jdbcResultSetCloseAfterNotifyEnabled;
    }

    public void setJdbcResultSetCloseAfterNotifyEnabled(boolean afterResultSetCloseNotifyEnable) {
        this.jdbcResultSetCloseAfterNotifyEnabled = afterResultSetCloseNotifyEnable;
    }

    public boolean isJdbcStatementExecuteErrorAfterNotifyEnabled() {
        return jdbcStatementExecuteErrorAfterNotifyEnabled;
    }

    public void setJdbcStatementExecuteErrorAfterNotifyEnabled(boolean afterStatementExecuteErrorNotifyEnable) {
        this.jdbcStatementExecuteErrorAfterNotifyEnabled = afterStatementExecuteErrorNotifyEnable;
    }

    public long incrementAndGetAccessedPageCount() {
        return accessedPageCounter.incrementAndGet();
    }

    public long getAccessedPageCount() {
        return accessedPageCounter.get();
    }

    public static CompositeType getCompositeType() throws JMException {
        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.DATE, SimpleType.DATE,
                SimpleType.STRING, SimpleType.STRING,

                SimpleType.INTEGER, SimpleType.STRING, JMXUtils.getAttributesTabularType(), SimpleType.LONG,
                SimpleType.LONG,

                SimpleType.LONG, SimpleType.LONG

        };

        String[] indexNames = { "id", "creationTime", "lastAccessedTime", "initialRemoteAddress", "sessionClassName",
                "maxInactiveInterval", "user", "attributes", "accessedPageCount", "jdbcStatementExecuteErrorCount",
                "jdbcStatementExecuteSuccessCount", "jdbcStatementExecuteTime" };
        String[] indexDescriptions = { "id", "creationTime", "lastAccessedTime", "initialRemoteAddress",
                "sessionClassName", "maxInactiveInterval", "user", "attributes", "accessedPageCount",
                "jdbcStatementExecuteErrorCount", "jdbcStatementExecuteSuccessCount", "jdbcStatementExecuteTime" };

        return new CompositeType("SessionStatistic", "Session Statistic", indexNames, indexDescriptions, indexTypes);
    }

    public CompositeDataSupport getCompositeData() throws JMException {
        Map<String, Object> attributes = getSessionAttributes();

        CompositeType compositeType = getCompositeType();

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("id", getSessionId());
        map.put("creationTime", getSessionCreationTime());
        map.put("lastAccessedTime", getSessionLastAccessedTime());
        map.put("initialRemoteAddress", getRemoteAddress());
        map.put("sessionClassName", getSessionClassName());

        map.put("maxInactiveInterval", getSessionMaxInactiveInterval());
        map.put("user", getUser());
        map.put("attributes",
                JMXUtils.getAttributesTabularData((TabularType) compositeType.getType("attributes"), attributes));
        map.put("accessedPageCount", getAccessedPageCount());
        map.put("jdbcStatementExecuteErrorCount", getJdbcStatementExecuteErrorCount());

        map.put("jdbcStatementExecuteSuccessCount", getJdbcStatementExecuteSuccessCount());
        map.put("jdbcStatementExecuteTime", getJdbcStatementExecuteSpanNanoTotal() / (1000 * 1000));

        return new CompositeDataSupport(compositeType, map);
    }

}
