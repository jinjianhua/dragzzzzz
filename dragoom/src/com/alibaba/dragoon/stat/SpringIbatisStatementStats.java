/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.utils.JMXUtils;
import com.alibaba.dragoon.common.utils.LazyCompositeData;
import com.alibaba.dragoon.common.utils.StatUtils;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;

public class SpringIbatisStatementStats extends LazyCompositeData {

    private static Log              LOG               = LogFactory.getLog(SpringIbatisStatementStats.class);

    private Method                  methodGetId       = null;
    private Method                  methodGetResource = null;

    protected final MappedStatement statement;
    private final AtomicLong        executeCount      = new AtomicLong();
    private final AtomicLong        executeTotalNano  = new AtomicLong();
    private final AtomicLong        fetchRowCount     = new AtomicLong();
    private final AtomicLong        effectedRowCount  = new AtomicLong();
    private final AtomicLong        updateCount       = new AtomicLong();
    private final AtomicInteger     runningCount      = new AtomicInteger();
    private final AtomicInteger     concurrentMax     = new AtomicInteger();

    private long                    lastTime;
    private Throwable               lastError;
    private long                    lastErrorMillis;
    private final AtomicLong        errorCount        = new AtomicLong();

    private final String            id;
    private final String            resource;

    public SpringIbatisStatementStats(MappedStatement statement){
        super();
        this.statement = statement;

        this.id = createId();
        this.resource = createResource();
    }

    public SpringIbatisStatementStats(MappedStatement statement, Object parameterObject){
        super();
        this.statement = statement;

        this.id = createId();
        this.resource = createResource();
    }

    /**
     * 通过反射的方式得到id，能够兼容2.3.0和2.3.4
     * 
     * @return
     */
    protected String createId() {
        try {
            if (methodGetId == null) {
                Class<?> clazz = statement.getClass();
                methodGetId = clazz.getMethod("getId");
            }

            Object returnValue = methodGetId.invoke(statement);

            if (returnValue == null) {
                return null;
            }

            return returnValue.toString();
        } catch (Exception ex) {
            LOG.error("createIdError", ex);
            return null;
        }
    }

    /**
     * 通过反射的方式得到resource，能够兼容2.3.0和2.3.4
     * 
     * @return
     */
    protected String createResource() {
        try {
            if (methodGetResource == null) {
                methodGetResource = statement.getClass().getMethod("getResource");
            }

            return (String) methodGetResource.invoke(statement);
        } catch (Exception ex) {
            return null;
        }
    }

    public String getKey() {
        return id;
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
        CompositeData data = JMXUtils.getErrorCompositeData(lastError);
        
        if (StatUtils.isRequiredReset()) {
            lastError = null;
        }
        
        return data;
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

    public Date getLastTime() {
        if (lastTime <= 0) {
            return null;
        }
        
        Date date = new Date(lastTime);
        
        if (StatUtils.isRequiredReset()) {
            lastTime = 0;
        }
        
        return date;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public void incrementRunningCount() {
        int val = runningCount.incrementAndGet();

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

    public int getConcurrentMax() {
        return StatUtils.get(concurrentMax);
    }

    public void decrementRunningCount() {
        runningCount.decrementAndGet();
    }

    public int getRunningCount() {
        return runningCount.get();
    }

    public long getExecuteCount() {
        return StatUtils.get(executeCount);
    }

    public void addExecuteNano(long delta) {
        executeTotalNano.addAndGet(delta);
    }

    public void error(Throwable error) {
        lastError = error;
        lastErrorMillis = System.currentTimeMillis();
        errorCount.incrementAndGet();
    }

    public void incrementExecuteCount() {
        executeCount.incrementAndGet();
    }

    public MappedStatement getStatement() {
        return statement;
    }

    public String getId() {
        return id;
    }

    public String getResource() {
        return this.resource;
    }

    public void addFetchRowCount(long delta) {
        fetchRowCount.addAndGet(delta);
    }

    public long getFetchRowCount() {
        return StatUtils.get(fetchRowCount);
    }

    public void addUpdateCount(long delta) {
        updateCount.addAndGet(delta);
    }

    public long getUpdateCount() {
        return StatUtils.get(updateCount);
    }

    private static CompositeType compositeType = null;

    public static CompositeType getCompositeTypeInternal() throws OpenDataException {
        if (compositeType != null) {
            return compositeType;
        }

        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING, SimpleType.LONG,
                SimpleType.LONG, SimpleType.LONG, SimpleType.DATE, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.INTEGER, SimpleType.INTEGER };

        String[] indexNames = { "id", "resource", "executeCount", "errorCount", "totalTime", "lastTime", "updateCount",
                "fetchObjectCount", "effectedRowCount", "concurrentMax", "runningCount" };

        String[] indexDescriptions = indexNames;

        compositeType = new CompositeType("IbatisStatementStat", "IbatisStatementStat", indexNames, indexDescriptions,
                                          indexTypes);

        return compositeType;
    }

    public long getTotalTime() {
        return StatUtils.get(executeTotalNano) / (1000 * 1000);
    }

    @Override
    protected CompositeData getCompositeData() {
        try {
            Map<String, Object> items = new HashMap<String, Object>();

            items.put("id", getId());
            items.put("resource", getResource());
            items.put("executeCount", getExecuteCount());
            items.put("errorCount", getErrorCount());
            items.put("totalTime", getTotalTime());

            items.put("lastTime", getLastTime());
            items.put("updateCount", getUpdateCount());
            items.put("fetchObjectCount", getFetchRowCount());
            items.put("effectedRowCount", getEffectedRowCount());
            items.put("concurrentMax", getConcurrentMax());

            items.put("runningCount", getRunningCount());

            return new CompositeDataSupport(getCompositeTypeInternal(), items);
        } catch (OpenDataException ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }
}
