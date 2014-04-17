/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.openmbean.TabularData;

public final class SpringStatManager extends NotificationBroadcasterSupport implements SpringStatManagerMBean {

    private final static SpringStatManager instance     = new SpringStatManager();

    private final AtomicLong               resetCounter = new AtomicLong();

    public static SpringStatManager getInstance() {
        return instance;
    }

    private SpringIbatisStats ibatisStat = new SpringIbatisStats();
    private SpringMethodStats methodStat = new SpringMethodStats();

    private boolean           statEnable = true;

    private SpringStatManager(){

    }

    public boolean isStatEnable() {
        return statEnable;
    }

    public void setStatEnable(boolean statEnable) {
        this.statEnable = statEnable;
    }

    public SpringMethodStats getMethodStat() {
        return methodStat;
    }

    public SpringIbatisStats getIbatisStat() {
        return ibatisStat;
    }

    public void reset() {
        if (ibatisStat != null) {
            ibatisStat.reset();
        }

        if (methodStat != null) {
            methodStat.reset();
        }

        resetCounter.incrementAndGet();
    }

    public long getResetCount() {
        return resetCounter.get();
    }

    public TabularData getMethodList() throws JMException {
        return methodStat.getMethodList();
    }

}
