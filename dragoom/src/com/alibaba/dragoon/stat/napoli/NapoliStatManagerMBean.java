package com.alibaba.dragoon.stat.napoli;

import javax.management.JMException;
import javax.management.openmbean.TabularData;

/**
 * Napoli监控类 类NapoliStatManagerMBean.java的实现描述：TODO 类实现描述
 * 
 * @author chriszhang Aug 18, 2011 10:30:11 AM
 */
public interface NapoliStatManagerMBean {

    TabularData getSenderStats() throws JMException;

    TabularData getReceiverStats() throws JMException;

    long getSenderResetCount() throws JMException;
    
    long getReceiverResetCount() throws JMException;

    void senderReset() throws JMException;
    
    void receiverReset() throws JMException;
}
