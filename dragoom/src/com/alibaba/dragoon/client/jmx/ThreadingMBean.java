/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

import java.math.BigDecimal;

public interface ThreadingMBean {

    int getDaemonThreadCount();

    int getThreadCount();

    long getTotalStartedThreadCount();

    BigDecimal getProcessCpuTimeRate();

    BigDecimal getFullGCCollectionTimeRate();

    int getRunnableThreadCount();

    int getNewThreadCount();

    int getBlockedThreadCount();

    int getWaitingThreadCount();

    int getTimedWaitingThreadCount();

    int getTeminatedThreadCount();

    int getDeadLockedThreadCount();
}
