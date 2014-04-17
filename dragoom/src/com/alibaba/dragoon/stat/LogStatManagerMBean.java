/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Date;

import javax.management.JMException;
import javax.management.openmbean.TabularData;

public interface LogStatManagerMBean {

    long getInfoCount();

    long getDebugCount();

    long getWarnCount();

    long getTraceCount();

    long getErrorCount();

    long getFatalCount();

    long getOtherCount();

    Date getLastErrorTime();

    void reset();

    long getResetCount();

    TabularData getErrorList() throws JMException;
}
