/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.dragoon.common.utils.StatUtils;

/**
 * @author shaojin.wensj
 */
public class SpringMethodInvokeStatistic {

    /**
     * 并发
     */
    private final AtomicInteger                                   runningCount      = new AtomicInteger();
    private final AtomicInteger                                   concurrentMax     = new AtomicInteger();

    private final AtomicLong                                      invokCount        = new AtomicLong();
    private final AtomicLong                                      errorCount        = new AtomicLong();

    private final AtomicLong                                      nanoTotal         = new AtomicLong();
    private final AtomicLong                                      nanoMax           = new AtomicLong();

    private long                                                  lastInvokeTime    = 0;
    private Throwable                                             lastError;
    private long                                                  lastErrorTime;

    private String                                                lastErrorUser;

    private final AtomicLong                                      jdbcFetchRowCount = new AtomicLong();
    private final AtomicLong                                      jdbcFetchRowPeak  = new AtomicLong();
    private final AtomicLong                                      jdbcUpdateCount   = new AtomicLong();
    private final AtomicLong                                      jdbcExecuteCount  = new AtomicLong();
    private final AtomicLong                                      jdbcCommitCount   = new AtomicLong();
    private final AtomicLong                                      jdbcRollbackCount = new AtomicLong();
    
    private Histogram                                             histogram         = new Histogram(new long[] {//
                                                                                                    1, 10, 100, 1000,
            10 * 1000
                                                                                                    //
                                                                                                    });

    private final static ThreadLocal<SpringMethodInvokeStatistic> currentLocal      = new ThreadLocal<SpringMethodInvokeStatistic>();

    public static SpringMethodInvokeStatistic current() {
        return currentLocal.get();
    }
    
    public static void setCurrent(SpringMethodInvokeStatistic current) {
        currentLocal.set(current);
    }

    public void reset() {
        concurrentMax.set(0);
        invokCount.set(0);
        errorCount.set(0);
        nanoTotal.set(0);
        nanoMax.set(0);
        lastInvokeTime = 0;
        lastError = null;
        lastErrorTime = 0;
        lastErrorUser = null;

        jdbcFetchRowCount.set(0);
        jdbcFetchRowPeak.set(0);
        jdbcUpdateCount.set(0);
        jdbcExecuteCount.set(0);
        jdbcCommitCount.set(0);
        jdbcRollbackCount.set(0);

        histogram.reset();
    }

    public void beforeInvoke() {
        int invoking = runningCount.incrementAndGet();

        for (;;) {
            int max = concurrentMax.get();
            if (invoking > max) {
                if (concurrentMax.compareAndSet(max, invoking)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }

        invokCount.incrementAndGet();
        lastInvokeTime = System.currentTimeMillis();

        currentLocal.set(this);
    }

    public long getNanoMax() {
        return StatUtils.get(nanoMax);
    }

    public long getErrorCount() {
        return StatUtils.get(errorCount);
    }

    public int getRunningCount() {
        return runningCount.get();
    }

    public int getConcurrentMax() {
        return StatUtils.get(concurrentMax);
    }

    public long getInvokeCount() {
        return StatUtils.get(invokCount);
    }

    public Date getLastInvokeTime() {
        if (lastInvokeTime == 0) {
            return null;
        }

        Date date = new Date(lastInvokeTime);
        if (StatUtils.isRequiredReset()) {
            this.lastInvokeTime = 0;
        }

        return date;
    }

    public long getNanoTotal() {
        return StatUtils.get(nanoTotal);
    }

    public Date getLastErrorTime() {
        if (lastErrorTime <= 0) {
            return null;
        }

        Date date = new Date(lastErrorTime);

        if (StatUtils.isRequiredReset()) {
            lastErrorTime = 0;
        }

        return date;
    }

    public Throwable getLastError() {
        Throwable error = lastError;
        if (StatUtils.isRequiredReset()) {
            lastError = null;
        }
        return error;
    }

    public void afterInvoke(long nanoSpan) {
        runningCount.decrementAndGet();

        nanoTotal.addAndGet(nanoSpan);

        for (;;) {
            long max = nanoMax.get();
            if (nanoSpan > max) {
                if (nanoMax.compareAndSet(max, nanoSpan)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }

        long millis = nanoSpan / (1000 * 1000);
        histogram.recode(millis);
        currentLocal.set(null);
    }

    public void error(Throwable error, String user) {
        errorCount.incrementAndGet();
        lastError = error;
        lastErrorTime = System.currentTimeMillis();
    }

    public String getLastErrorUser() {
        String user = lastErrorUser;
        if (StatUtils.isRequiredReset()) {
            lastErrorUser = null;
        }
        return user;
    }

    public long[] getHistogramRanges() {
        return this.histogram.getRanges();
    }

    public long[] getHistogramValues() {
        return this.histogram.toArray();
    }
    
    public void addJdbcFetchRowCount(long delta) {
        for (;;) {
            long peak = jdbcFetchRowPeak.get();
            if (delta <= peak) {
                break;
            }

            if (jdbcFetchRowPeak.compareAndSet(peak, delta)) {
                break;
            }
        }
        this.jdbcFetchRowCount.addAndGet(delta);
    }

    public long getJdbcFetchRowCount() {
        return StatUtils.get(jdbcFetchRowCount);
    }

    public long getJdbcFetchRowPeak() {
        return StatUtils.get(jdbcFetchRowPeak);
    }

    public void addJdbcUpdateCount(int delta) {
        this.jdbcUpdateCount.addAndGet(delta);
    }

    public long getJdbcUpdateCount() {
        return StatUtils.get(jdbcUpdateCount);
    }

    public void incrementJdbcExecuteCount() {
        jdbcExecuteCount.incrementAndGet();
    }

    public long getJdbcExecuteCount() {
        return StatUtils.get(jdbcExecuteCount);
    }

    public void incrementJdbcCommitCount() {
        jdbcCommitCount.incrementAndGet();
    }

    public long getJdbcCommitCount() {
        return StatUtils.get(jdbcCommitCount);
    }

    public void incrementJdbcRollbackCount() {
        jdbcRollbackCount.incrementAndGet();
    }

    public long getJdbcRollbackCount() {
        return StatUtils.get(jdbcRollbackCount);
    }

}
