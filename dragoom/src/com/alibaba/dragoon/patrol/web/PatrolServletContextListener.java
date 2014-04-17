/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.web;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.alibaba.dragoon.patrol.PatrolUtils;
import com.alibaba.dragoon.stat.WebAppStatManager;
import com.alibaba.dragoon.stat.WebStatisticManager;

/**
 * @author shaojin.wensj
 */
@SuppressWarnings("serial")
public class PatrolServletContextListener implements ServletContextListener, PatrolServletContextListenerMBean, Serializable {

    private ServletContext     context;
    private String             appName;
    private boolean            useJndiDataSource             = false;

    public final static String BEAN_NAME_SPRING_STAT         = "com.alibaba.dragoon:type=SpringStats";
    public final static String BEAN_NAME_SPRING_METHOD_STAT  = "com.alibaba.dragoon:type=SpringMethodStats";
    public final static String BEAN_NAME_SPRING_IBATIS_STAT  = "com.alibaba.dragoon:type=SpringIbatisStats";
    public final static String BEAN_NAME_WEB_STAT            = "com.alibaba.dragoon:type=WebStats";
    public final static String BEAN_NAME_JDBC_STAT           = "com.alibaba.dragoon:type=JdbcStats";
    public final static String BEAN_NAME_JDBC_CONNECT_STAT   = "com.alibaba.dragoon:type=JdbcConnectionStats";
    public final static String BEAN_NAME_JDBC_STATEMENT_STAT = "com.alibaba.dragoon:type=JdbcStatementStats";
    public final static String BEAN_NAME_JDBC_RESULTSET_STAT = "com.alibaba.dragoon:type=JdbcResultSetStats";
    public final static String BEAN_NAME_LOG_STAT            = "com.alibaba.dragoon:type=LogStats";
    public final static String BEAN_NAME_EXCEPTION           = "com.alibaba.dragoon:type=Exception";

    public PatrolServletContextListener(){

    }

    public boolean isUseJndiDataSourc(ServletContext context) {
        String param = (String) context.getInitParameter("useJndiDataSource");
        if (param == null || param.length() == 0) {
            return false;
        }

        return "true".equalsIgnoreCase(param);
    }

    public void contextInitialized(ServletContextEvent event) {
        this.context = event.getServletContext();

        appName = context.getInitParameter("appName");
        this.useJndiDataSource = isUseJndiDataSourc(context);

        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName(getMBeanName()));
        } catch (Exception e) {
            this.context.log("patrol context listener init error", e);
        }

        for (PatrolServletSessionListener sessionListener : PatrolServletSessionListener.instances) {
            sessionListener.contextInitialized(event.getServletContext());
        }

        WebStatisticManager.getInstance().setContextPath(PatrolWebUtils.getContextPath(context));

        PatrolUtils.registerMBean(useJndiDataSource, context.getClass().getClassLoader());

        WebAppStatManager.getInstance().contextInitialized(context);
    }

    public void contextDestroyed(ServletContextEvent event) {
        for (PatrolServletSessionListener sessionListener : PatrolServletSessionListener.instances) {
            sessionListener.contextDestroyed(event.getServletContext());
        }

        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(getMBeanName()));
        } catch (Exception e) {
            this.context.log("patrol context listener destroy error", e);
        }

        PatrolUtils.unregisterMBean();
    }

    private String getMBeanName() {
        if (appName == null) {
            return "com.alibaba.dragoon:type=PatrolServletContextListener";
        } else {
            return appName + ":type=PatrolServletContextListener";
        }
    }

    // //////////////////// 以下代码用于MBean获取SevletContext的一些信息

    public String getContextPath() {
        return PatrolWebUtils.getContextPath(context);
    }

    public Object getAttribute(String name) {
        return this.context.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        this.context.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        this.context.removeAttribute(name);
    }

    public List<String> getAttributeNames() {
        List<String> names = new ArrayList<String>();
        for (Enumeration<?> e = this.context.getAttributeNames(); e.hasMoreElements();) {
            names.add((String) e.nextElement());
        }

        return names;
    }

    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    public int getMinorVersion() {
        return context.getMinorVersion();
    }

    public List<String> getInitParameterNames() {
        List<String> names = new ArrayList<String>();
        for (Enumeration<?> e = this.context.getInitParameterNames(); e.hasMoreElements();) {
            names.add((String) e.nextElement());
        }

        return names;
    }

    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }

    public String getServletContextName() {
        return context.getServletContextName();
    }

    public String getServerInfo() {
        return context.getServerInfo();
    }

    public String getMimeType(String file) {
        return context.getMimeType(file);
    }

    public String getRealPath(String path) {
        return context.getRealPath(path);
    }

    public String getResource(String path) throws MalformedURLException {
        URL url = context.getResource(path);
        if (url == null) {
            return null;
        }
        return url.toString();
    }

    public Set<?> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }
}
