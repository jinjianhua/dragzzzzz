/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Date;
import java.util.List;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;

public interface SpringIbatisStatsMBean {

    long getInsertCount();

    long getDeleteCount();

    long getUpdateCount();

    long getQueryForObjectCount();

    long getQueryForListCount();

    long getQueryForRowHandlerCount();

    long getQueryForPaginatedListCount();

    long getQueryForMapCount();

    long getStartBatchCount();

    long getExecuteBatchCount();

    long getExecuteBatchDetailedCount();

    long getTransactionStartCount();

    long getTransactionEndCount();

    long getTransactionCommitCount();

    long getSessionOpenCount();

    long getSessionCloseCount();

    long getFlushDataCacheCount();

    List<SpringIbatisStatementStats> getStatements();

    CompositeData getLastError() throws JMException;

    Date getLastErrorTime();

    long getErrorCount();

    int getRunningCount();

    int getConcurrentMax();

    long getFetchRowCount();

    long getEffectedRowCount();

    long getTotalTime();

    List<String> getProxyBeanNames();

    void reset();

    long getResetCount();
}
