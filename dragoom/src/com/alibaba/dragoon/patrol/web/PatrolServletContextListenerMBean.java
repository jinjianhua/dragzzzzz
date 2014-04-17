/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.web;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

/**
 * @author shaojin.wensj
 */
public interface PatrolServletContextListenerMBean {

    String getContextPath();

    List<String> getAttributeNames();

    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    void removeAttribute(String name);

    /**
     * Returns the major version of the Java Servlet API that this servlet container supports. All implementations that
     * comply with Version 2.5 must have this method return the integer 2.
     * 
     * @return 2
     */

    int getMajorVersion();

    int getMinorVersion();

    List<String> getInitParameterNames();

    String getInitParameter(String name);

    String getServletContextName();

    String getServerInfo();

    String getMimeType(String file);

    String getRealPath(String path);

    String getResource(String path) throws MalformedURLException;

    Set<?> getResourcePaths(String path);
}
