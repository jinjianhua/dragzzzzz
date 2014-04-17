/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import javax.management.JMException;
import javax.management.openmbean.TabularData;

public interface WebStatisticManagerMBean {

    String getContextPath();

    TabularData getURIList() throws JMException;

    long getResetCount();

    void reset();
}
