/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.patrol.log4j.ThrowableInfo;
import com.alibaba.dragoon.patrol.log4j.ThrowableInfoStat;

public class LogStatManager implements LogStatManagerMBean {

    public static LogStatManager instance = new LogStatManager();

    public static final LogStatManager getInstance() {
        return instance;
    }

    private final AtomicLong                                      errorCount       = new AtomicLong();
    private final AtomicLong                                      infoCount        = new AtomicLong();
    private final AtomicLong                                      debugCount       = new AtomicLong();
    private final AtomicLong                                      warnCount        = new AtomicLong();
    private final AtomicLong                                      traceCount       = new AtomicLong();
    private final AtomicLong                                      fatalCount       = new AtomicLong();
    private final AtomicLong                                      otherCount       = new AtomicLong();

    private final AtomicLong                                      resetCount       = new AtomicLong();

    private final ConcurrentMap<ThrowableInfo, ThrowableInfoStat> exceptionStatMap = new ConcurrentHashMap<ThrowableInfo, ThrowableInfoStat>();

    private long                                                  lastErrorTime;

    private final long                                            MAX_COUNT        = 1000 * 1;

    public LogStatManager(){

    }

    public void incrementInfoCount() {
        infoCount.incrementAndGet();
    }

    public void incrementDebugCount() {
        debugCount.incrementAndGet();
    }

    public void incrementWarnCount() {
        warnCount.incrementAndGet();
    }

    public void incrementTraceCount() {
        traceCount.incrementAndGet();
    }

    public void incrementFatalCount() {
        fatalCount.incrementAndGet();
    }

    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }

    public void incrementOtherCount() {
        otherCount.incrementAndGet();
    }

    public long getErrorCount() {
        return errorCount.get();
    }

    public long getInfoCount() {
        return infoCount.get();
    }

    public long getWarnCount() {
        return warnCount.get();
    }

    public long getDebugCount() {
        return debugCount.get();
    }

    public long getTraceCount() {
        return traceCount.get();
    }

    public long getFatalCount() {
        return fatalCount.get();
    }

    public long getOtherCount() {
        return otherCount.get();
    }

    public long getResetCount() {
        return resetCount.get();
    }

    public void reset() {
        resetCount.incrementAndGet();

        infoCount.set(0);
        debugCount.set(0);
        warnCount.set(0);
        errorCount.set(0);
        fatalCount.set(0);
        otherCount.set(0);
        lastErrorTime = 0;

        exceptionStatMap.clear();
    }

    public ConcurrentMap<ThrowableInfo, ThrowableInfoStat> getExceptionStatMap() {
        return exceptionStatMap;
    }

    public TabularData getErrorList() throws JMException {
        CompositeType rowType = ThrowableInfoStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);

        TabularType tabularType = new TabularType("ErrorList", "ErrorList", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        for (ThrowableInfoStat stat : exceptionStatMap.values()) {
            data.put(stat.getCompositeData());
        }
        
        if (StatUtils.isRequiredReset()) {
            exceptionStatMap.clear();
        }

        return data;
    }

    public Date getLastErrorTime() {
        if (lastErrorTime <= 0) {
            return null;
        }
        return new Date(lastErrorTime);
    }

    public void setLastErrorTime(long lastErrorTime) {
        this.lastErrorTime = lastErrorTime;
    }

    public ThrowableInfoStat get(ThrowableInfo key) {
        return exceptionStatMap.get(key);
    }

    public ThrowableInfoStat putIfAbsent(ThrowableInfo key, ThrowableInfoStat stat) {
        if (exceptionStatMap.size() > MAX_COUNT) {
            exceptionStatMap.clear();
        }
        
        return exceptionStatMap.putIfAbsent(key, stat);
    }
}
