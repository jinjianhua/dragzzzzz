/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.alibaba.dragoon.common.utils.JMXUtils;
import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.patrol.spring.SqlMapClientWrapper;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;

@SuppressWarnings("deprecation")
public class SpringIbatisStats implements SpringIbatisStatsMBean, MethodInterceptor {

    private final AtomicLong                                        insertCount                = new AtomicLong();
    private final AtomicLong                                        deleteCount                = new AtomicLong();
    private final AtomicLong                                        updateCount                = new AtomicLong();
    private final AtomicLong                                        queryForObjectCount        = new AtomicLong();
    private final AtomicLong                                        queryForListCount          = new AtomicLong();

    private final AtomicLong                                        queryForRowHandlerCount    = new AtomicLong();
    private final AtomicLong                                        queryForPaginatedListCount = new AtomicLong();
    private final AtomicLong                                        queryForMapCount           = new AtomicLong();
    private final AtomicLong                                        startBatchCount            = new AtomicLong();
    private final AtomicLong                                        executeBatchCount          = new AtomicLong();

    private final AtomicLong                                        executeBatchDetailedCount  = new AtomicLong();
    private final AtomicLong                                        startTransactionCount      = new AtomicLong();
    private final AtomicLong                                        endTransactionCount        = new AtomicLong();
    private final AtomicLong                                        commitTransactionCount     = new AtomicLong();
    private final AtomicLong                                        openSessionCount           = new AtomicLong();

    private final AtomicLong                                        flushDataCacheCount        = new AtomicLong();
    private final AtomicLong                                        closeSessionCount          = new AtomicLong();

    private final AtomicLong                                        fetchRowCount              = new AtomicLong();
    private final AtomicLong                                        effectedRowCount           = new AtomicLong();

    private final ConcurrentMap<String, SpringIbatisStatementStats> statements                 = new ConcurrentHashMap<String, SpringIbatisStatementStats>();

    private final AtomicLong                                        executeTotalNano           = new AtomicLong();
    private final AtomicInteger                                     executingCount             = new AtomicInteger();
    private final AtomicInteger                                     concurrentMax              = new AtomicInteger();
    private final List<String>                                      proxyBeanNames             = new ArrayList<String>();

    private Throwable                                               lastError;
    private long                                                    lastErrorMillis;
    private final AtomicLong                                        errorCount                 = new AtomicLong();

    private final AtomicLong                                        resetCounter               = new AtomicLong();

    public SpringIbatisStats(){

    }

    public long getResetCount() {
        return resetCounter.get();
    }

    public void reset() {
        insertCount.set(0);
        deleteCount.set(0);
        updateCount.set(0);
        queryForObjectCount.set(0);
        queryForListCount.set(0);

        queryForRowHandlerCount.set(0);
        queryForPaginatedListCount.set(0);
        queryForMapCount.set(0);
        startBatchCount.set(0);
        executeBatchCount.set(0);

        executeBatchDetailedCount.set(0);
        startTransactionCount.set(0);
        endTransactionCount.set(0);
        commitTransactionCount.set(0);
        openSessionCount.set(0);

        flushDataCacheCount.set(0);
        closeSessionCount.set(0);

        statements.clear();

        executeTotalNano.set(0);
        executingCount.set(0);
        concurrentMax.set(0);
        errorCount.set(0);

        lastError = null;
        lastErrorMillis = 0;

        resetCounter.incrementAndGet();
    }

    public List<String> getProxyBeanNames() {
        return proxyBeanNames;
    }

    public ConcurrentMap<String, SpringIbatisStatementStats> getStatementMap() {
        return statements;
    }

    public void addEffectedRowCount(long delta) {
        effectedRowCount.addAndGet(delta);
    }

    public long getEffectedRowCount() {
        return StatUtils.get(effectedRowCount);
    }

    public long getErrorCount() {
        return StatUtils.get(errorCount);
    }

    public CompositeData getLastError() throws JMException {
        CompositeData error = JMXUtils.getErrorCompositeData(lastError);
        if (StatUtils.isRequiredReset()) {
            lastError = null;
        }
        return error;
    }

    public Date getLastErrorTime() {
        if (lastErrorMillis <= 0) {
            return null;
        }

        Date date = new Date(lastErrorMillis);
        
        if (StatUtils.isRequiredReset()) {
            lastErrorMillis = 0;
        }
        
        return date;
    }

    public int getConcurrentMax() {
        return StatUtils.get(concurrentMax);
    }

    public void incrementRunningCount() {
        int val = executingCount.incrementAndGet();

        for (;;) {
            int max = concurrentMax.get();
            if (val > max) {
                if (concurrentMax.compareAndSet(max, val)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

    public void decrementRunningCount() {
        executingCount.decrementAndGet();
    }

    public int getRunningCount() {
        return executingCount.get();
    }

    public void addFetchRowCount(long delta) {
        fetchRowCount.addAndGet(delta);
    }

    public long getFetchRowCount() {
        return StatUtils.get(fetchRowCount);
    }

    public void addExecuteNano(long delta) {
        executeTotalNano.addAndGet(delta);
    }

    public long getInsertCount() {
        return StatUtils.get(insertCount);
    }

    public long getDeleteCount() {
        return StatUtils.get(deleteCount);
    }

    public long getUpdateCount() {
        return StatUtils.get(updateCount);
    }

    public long getQueryForObjectCount() {
        return StatUtils.get(queryForObjectCount);
    }

    public long getQueryForListCount() {
        return StatUtils.get(queryForListCount);
    }

    public long getQueryForRowHandlerCount() {
        return StatUtils.get(queryForRowHandlerCount);
    }

    public long getQueryForPaginatedListCount() {
        return StatUtils.get(queryForPaginatedListCount);
    }

    public long getQueryForMapCount() {
        return StatUtils.get(queryForMapCount);
    }

    public long getStartBatchCount() {
        return StatUtils.get(startBatchCount);
    }

    public long getExecuteBatchCount() {
        return StatUtils.get(executeBatchCount);
    }

    public long getExecuteBatchDetailedCount() {
        return StatUtils.get(executeBatchDetailedCount);
    }

    public long getTransactionStartCount() {
        return StatUtils.get(startTransactionCount);
    }

    public long getTransactionEndCount() {
        return StatUtils.get(endTransactionCount);
    }

    public long getTransactionCommitCount() {
        return StatUtils.get(commitTransactionCount);
    }

    public long getSessionOpenCount() {
        return StatUtils.get(openSessionCount);
    }

    public long getFlushDataCacheCount() {
        return StatUtils.get(flushDataCacheCount);
    }

    public long getSessionCloseCount() {
        return StatUtils.get(closeSessionCount);
    }

    public final void incrementFlushDataCacheCount() {
        flushDataCacheCount.incrementAndGet();
    }

    public final void incrementOpenSessionCount() {
        openSessionCount.incrementAndGet();
    }

    public final void incrementEndTransactionCount() {
        endTransactionCount.incrementAndGet();
    }

    public final void incrementCommitTransactionCount() {
        commitTransactionCount.incrementAndGet();
    }

    public final void incrementStartTransactionCount() {
        startTransactionCount.incrementAndGet();
    }

    public final void incrementExecuteBatchDetailedCount() {
        executeBatchDetailedCount.incrementAndGet();
    }

    public final void incrementStartBatchCount() {
        startBatchCount.incrementAndGet();
    }

    public final void incrementExecuteBatchCount() {
        executeBatchCount.incrementAndGet();
    }

    public final void incrementInsertCount() {
        insertCount.incrementAndGet();
    }

    public final void incrementDeleteCount() {
        deleteCount.incrementAndGet();
    }

    public final void incrementUpdateCount() {
        updateCount.incrementAndGet();
    }

    public final void incrementQueryForObjectCount() {
        queryForObjectCount.incrementAndGet();
    }

    public final void incrementQueryForListCount() {
        queryForListCount.incrementAndGet();
    }

    public final void incrementQueryForRowHandlerCount() {
        queryForRowHandlerCount.incrementAndGet();
    }

    public final void incrementQueryForPaginatedListCount() {
        queryForPaginatedListCount.incrementAndGet();
    }

    public final void incrementQueryForMapCount() {
        queryForMapCount.incrementAndGet();
    }

    public final void incrementCloseSessionCount() {
        closeSessionCount.incrementAndGet();
    }

    public void error(Throwable error) {
        lastError = error;
        lastErrorMillis = System.currentTimeMillis();
        errorCount.incrementAndGet();
    }

    public List<SpringIbatisStatementStats> getStatements() {
        return new ArrayList<SpringIbatisStatementStats>(this.statements.values());
    }

    public long getTotalTime() {
        return StatUtils.get(executeTotalNano) / (1000 * 1000);
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object returnObject = invocation.proceed();

        Method method = invocation.getMethod();
        Class<?> thisObjectClass = invocation.getThis().getClass();
        if (method.getName().equals("getObject")
            && "org.springframework.orm.ibatis.SqlMapClientFactoryBean".equals(thisObjectClass.getName())) {
            // return thisObjectClass;
            return new SqlMapClientWrapper(this, (ExtendedSqlMapClient) returnObject);
        }

        return returnObject;
    }

}
