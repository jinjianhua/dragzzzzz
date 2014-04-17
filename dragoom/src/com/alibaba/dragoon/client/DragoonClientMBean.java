/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;

import com.alibaba.dragoon.common.daemon.DaemonServiceMBean;

public interface DragoonClientMBean extends DaemonServiceMBean {

    String getAgentAddress();

    void setAgentAddress(String address, int port);

    boolean closeSession();

    long getConnectCount();

    CompositeData getSessionInfo() throws JMException;

    long getHandleGetAttributeCount();

    long getHandleSetAttributeCount();

    long getHandleInvokeCount();

    String getAppNumber();

    void setAppNumber(String value);

    String getAppInstNumber();

    void setAppInstNumber(String value);

    long getHeartbeatSentCount();

    long getHeartbeatSkipCount();

    long getHeartbeatPeriod();

    long getConnectEstablishedCount();

    long getReceivedBytes();

    long getSentBytes();

    long getReceivedMessages();

    long getSentMessages();
    
    String getVersion();
}
