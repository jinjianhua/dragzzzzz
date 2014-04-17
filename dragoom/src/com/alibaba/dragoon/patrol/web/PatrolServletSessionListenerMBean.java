/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.web;

import java.util.List;

import javax.management.JMException;
import javax.management.openmbean.TabularData;

/**
 * @author shaojin.wensj
 */
public interface PatrolServletSessionListenerMBean {

    boolean monitorSession(String sessionId);

    int getSessionCount();

    int getSessionCountMax();

    long getSessionCountTotal();

    /**
     * 遍历已经存在的Session，按序获取SessionId
     * 
     * @param index
     * @return
     */
    String getSessionIdAt(int index);

    Object getSessionAttibuteValue(int index, String attrName);

    boolean cancelMonitorSession(String sessionId) throws JMException;

    List<String> getSessionRemoteAddresses();

    List<Object> listSessionAttributes(String attrName);

    /**
     * 根据属性的值查找一个Session的Id，例如查找user='wenshao'的Session，通过这个接口，使得监控更方便。
     * 
     * @param attr
     * @param value
     * @return
     */
    String findSessionIdByAttributeValue(String attr, String value);

    long getSessionAliveTimeTotal();

    TabularData getSessionList() throws JMException;

    void reset();
}
