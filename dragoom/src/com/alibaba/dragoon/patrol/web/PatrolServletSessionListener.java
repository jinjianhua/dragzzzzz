/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.web;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.alibaba.dragoon.stat.WebSessionStatistic;

/**
 * @author shaojin.wensj
 */
public class PatrolServletSessionListener implements HttpSessionListener, PatrolServletSessionListenerMBean {

    // TODO 此处两个静态变量需要再仔细考虑
    private static transient ConcurrentMap<String, WebSessionStatistic> counters              = new ConcurrentHashMap<String, WebSessionStatistic>();
    public static transient ConcurrentMap<String, WebSessionStatistic>  monitoredCounters     = new ConcurrentHashMap<String, WebSessionStatistic>();

    private volatile boolean                                            jmxRegistered         = false;
    private Object                                                      jmxRegisterLock       = new Object();

    public final static List<PatrolServletSessionListener>              instances             = new CopyOnWriteArrayList<PatrolServletSessionListener>();

    private final AtomicLong                                            sessionCounterTotal   = new AtomicLong(0L);
    private final AtomicInteger                                         sessionCountMax       = new AtomicInteger(0);

    private AtomicLong                                                  sessionAliveTimeTotal = new AtomicLong(0);

    private String                                                      userAttributeName     = null;
    private ObjectName                                                  objectName;

    public PatrolServletSessionListener(){
        instances.add(this);
    }

    public void reset() {
        sessionCounterTotal.set(0);
        sessionCountMax.set(0);
        sessionAliveTimeTotal.set(0);
        counters.clear();
    }

    public final static List<PatrolServletSessionListener> getInstanceList() {
        return instances;
    }

    public void sessionCreated(HttpSessionEvent event) {
        jmxRegister(event.getSession().getServletContext());

        if (userAttributeName == null) {
            userAttributeName = event.getSession().getServletContext().getInitParameter("userSessionAttribute");
        }

        HttpSession session = event.getSession();
        ObjectName sessionObjectName = null;
        try {
            sessionObjectName = new ObjectName(objectName.getDomain() + ".sessions:type=session_" + session.getId());
        } catch (MalformedObjectNameException ex) {
            event.getSession().getServletContext().log(ex.getMessage(), ex);
        }
        counters.put(session.getId(), new WebSessionStatistic(session, sessionObjectName, userAttributeName));
        setSessionCount(counters.size());

        sessionCounterTotal.incrementAndGet();
    }

    private void setSessionCount(int count) {
        int max = sessionCountMax.get();
        for (;;) {
            if (count > max) {
                if (sessionCountMax.compareAndSet(max, count)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

    /**
     * 在Jetty下，某些情况，SessionListener不起作用，此时返回可能为NULL
     * 
     * @param session
     * @return
     */
    public static WebSessionStatistic getCounter(HttpSession session) {
        if (session == null) {
            return null;
        }

        final String sessionId = session.getId();

        WebSessionStatistic counter = counters.get(sessionId);

        return counter;
    }

    public String findSessionIdByAttributeValue(String attr, String value) {
        for (Map.Entry<String, WebSessionStatistic> entry : counters.entrySet()) {
            HttpSession session = entry.getValue().getSession();
            Object sessionAttr = session.getAttribute(attr);

            if (sessionAttr == null) {
                if (value == null) {
                    return session.getId();
                }
            } else {
                if (value.equals(sessionAttr.toString())) {
                    return session.getId();
                }
            }
        }

        return null;
    }

    /*
     * JdbcStatisticWebServletContextListener负责调用此方法，进行初始化
     */
    public void contextInitialized(ServletContext context) {
        jmxRegister(context);
    }

    /*
     * JdbcStatisticWebServletContextListener负责调用此方法，释放资源
     */
    public void contextDestroyed(ServletContext context) {
        try {
            if (jmxRegistered == true) {
                synchronized (jmxRegisterLock) {
                    if (jmxRegistered == true) {
                        ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
                        jmxRegistered = false;
                    }
                }
            }
        } catch (Exception ex) {
            context.log("jmxRegister error", ex);
        }
    }

    private void jmxRegister(ServletContext context) {
        try {
            if (jmxRegistered == false) {
                synchronized (jmxRegisterLock) {
                    if (jmxRegistered == false) {
                        objectName = new ObjectName(getMBeanName(context));
                        ManagementFactory.getPlatformMBeanServer().registerMBean(this,
                                                                                 new ObjectName(getMBeanName(context)));
                        jmxRegistered = true;
                    }
                }
            }
        } catch (Exception ex) {
            context.log("jmxRegister error", ex);
        }
    }

    public String getMBeanName(ServletContext context) {
        String appName = context.getInitParameter("appName");
        if (appName == null) {
            return "patrol:type=PatrolServletSessionListener";
        } else {
            return appName + ":type=PatrolServletSessionListener";
        }
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        WebSessionStatistic counter = counters.remove(session.getId());

        long aliveTime = session.getLastAccessedTime() - session.getCreationTime();
        addSessionAliveTime(aliveTime);

        if (counter != null) {
            if (monitoredCounters.get(session.getId()) != null) {
                counter.cancelMonitor();
            }
        }
    }

    private void addSessionAliveTime(long aliveTime) {
        sessionAliveTimeTotal.addAndGet(aliveTime);
    }

    public long getSessionAliveTimeTotal() {
        long total = sessionAliveTimeTotal.get();
        for (WebSessionStatistic counter : counters.values()) {
            long aliveTime = counter.getSession().getLastAccessedTime() - counter.getSession().getCreationTime();
            total += aliveTime;
        }
        return total;
    }

    public int getSessionCount() {
        return counters.size();
    }

    public int getSessionCountMax() {
        return sessionCountMax.get();
    }

    public long getSessionCountTotal() {
        return this.sessionCounterTotal.get();
    }

    public Object getSessionAttibuteValue(int index, String attrName) {
        if (index < 0) {
            return null;
        }

        WebSessionStatistic sesion = getSession(index);

        if (sesion != null) {
            return sesion.getSessionAttribute(attrName);
        }

        return null;
    }

    public String getSessionIdAt(int index) {
        if (index < 0) {
            return null;
        }

        WebSessionStatistic sesion = getSession(index);

        if (sesion != null) {
            return sesion.getSessionId();
        }

        return null;
    }

    public WebSessionStatistic getSession(int index) {
        if (index < 0) {
            return null;
        }

        int i = 0;

        for (Iterator<Map.Entry<String, WebSessionStatistic>> iter = counters.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, WebSessionStatistic> entry = iter.next();
            if (i++ == index) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 使用JMX的MBean来监控某个HttpSession
     */
    public boolean monitorSession(String sessionId) {
        if (sessionId == null) {
            return false;
        }

        WebSessionStatistic sessionCounter = counters.get(sessionId);

        if (sessionCounter == null) {
            return false;
        }

        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(sessionCounter, sessionCounter.getMBeanName());
        } catch (Exception e) {
            return false;
        }

        monitoredCounters.put(sessionId, sessionCounter);
        return true;
    }

    /**
     * 取消监控, 在MBeanServer中unregister
     * 
     * @param sessionId
     * @return
     */
    public boolean cancelMonitorSession(String sessionId) throws JMException {
        if (sessionId == null) {
            return false;
        }

        WebSessionStatistic sessionCounter = counters.get(sessionId);

        if (sessionCounter == null) {
            return false;
        }

        sessionCounter.cancelMonitor();

        return true;
    }

    public List<String> getSessionRemoteAddresses() {
        List<String> list = new ArrayList<String>(counters.size());
        for (WebSessionStatistic counter : counters.values()) {
            list.add(counter.getRemoteAddress());
        }
        return list;
    }

    public List<Object> listSessionAttributes(String attrName) {
        List<Object> list = new ArrayList<Object>(counters.size());
        for (WebSessionStatistic counter : counters.values()) {
            list.add(counter.getSessionAttribute(attrName));
        }
        return list;
    }

    public TabularData getSessionList() throws JMException {
        CompositeType rowType = WebSessionStatistic.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);

        TabularType tabularType = new TabularType("SessionStatisticList", "SessionStatisticList", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        for (Map.Entry<String, WebSessionStatistic> entry : counters.entrySet()) {
            data.put(entry.getValue().getCompositeData());
        }

        return data;
    }
}
