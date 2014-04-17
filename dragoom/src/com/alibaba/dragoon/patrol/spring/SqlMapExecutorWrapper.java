/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.spring;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.Profiler.EntryType;
import com.alibaba.dragoon.stat.SpringIbatisStatementStats;
import com.alibaba.dragoon.stat.SpringIbatisStats;
import com.alibaba.druid.stat.JdbcSqlStat;
import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.BatchException;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImplWrapper;
import com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;

@SuppressWarnings({ "rawtypes", "deprecation" })
public class SqlMapExecutorWrapper implements SqlMapExecutor {

    private SqlMapExecutor            executor;
    protected final SpringIbatisStats stats;
    
    protected final SqlMapClientImpl clientImpl;
    protected final SqlMapClientImplWrapper clientImplWrapper;

    public SqlMapExecutorWrapper(ExtendedSqlMapClient client, SpringIbatisStats stats, SqlMapExecutor executor){
        this.stats = stats;
        this.executor = executor;
        
        this.clientImpl = client.getClass() == SqlMapClientImpl.class ? (SqlMapClientImpl) client : null;
        this.clientImplWrapper = clientImpl != null ? new SqlMapClientImplWrapper(stats, clientImpl) : null;
    }

    public Object insert(String id, Object parameterObject) throws SQLException {
        stats.incrementInsertCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            return executor.insert(id, parameterObject);
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public Object insert(String id) throws SQLException {
        stats.incrementInsertCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            return executor.insert(id);
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public int update(String id, Object parameterObject) throws SQLException {
        stats.incrementUpdateCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);
        if (statementStats != null) {
            statementStats.incrementExecuteCount();
            statementStats.setLastTime(System.currentTimeMillis());
        }

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            int effectedRowCount = executor.update(id, parameterObject);

            stats.addEffectedRowCount(effectedRowCount);
            if (statementStats != null) {
                statementStats.addEffectedRowCount(effectedRowCount);
            }

            return effectedRowCount;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public int update(String id) throws SQLException {
        stats.incrementUpdateCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);
        if (statementStats != null) {
            statementStats.incrementExecuteCount();
            statementStats.setLastTime(System.currentTimeMillis());
        }

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            int effectedRowCount = executor.update(id);

            stats.addEffectedRowCount(effectedRowCount);
            if (statementStats != null) {
                statementStats.addEffectedRowCount(effectedRowCount);
            }

            return effectedRowCount;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public int delete(String id, Object parameterObject) throws SQLException {
        stats.incrementDeleteCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            int effectedRowCount = executor.delete(id, parameterObject);

            stats.addEffectedRowCount(effectedRowCount);
            if (statementStats != null) {
                statementStats.addEffectedRowCount(effectedRowCount);
            }

            return effectedRowCount;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public int delete(String id) throws SQLException {
        stats.incrementDeleteCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            int effectedRowCount = executor.delete(id);

            stats.addEffectedRowCount(effectedRowCount);
            if (statementStats != null) {
                statementStats.addEffectedRowCount(effectedRowCount);
            }

            return effectedRowCount;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    private final void executeBefore(SpringIbatisStatementStats statementStats) {
        stats.incrementRunningCount();
        if (statementStats != null) {
            statementStats.incrementExecuteCount();
            statementStats.setLastTime(System.currentTimeMillis());
            statementStats.incrementRunningCount();

            Profiler.enter(statementStats.getKey(), EntryType.IBATIS);

            JdbcSqlStat.setContextSqlName(statementStats.getId());
            JdbcSqlStat.setContextSqlFile(statementStats.getResource());
        }
    }

    private final void executeAfter(SpringIbatisStatementStats statementStats, long startNano) {
        long nano = System.nanoTime() - startNano;

        stats.addExecuteNano(nano);
        stats.decrementRunningCount();
        if (statementStats != null) {
            statementStats.addExecuteNano(nano);
            statementStats.decrementRunningCount();

            Profiler.release();

            JdbcSqlStat.setContextSqlName(null);
            JdbcSqlStat.setContextSqlFile(null);
        }
    }

    private final void handleError(SpringIbatisStatementStats statementStats, Throwable ex) {
        stats.error(ex);
        if (statementStats != null) {
            statementStats.error(ex);
            Profiler.setError(ex);
        }
    }

    public Object queryForObject(String id, Object parameterObject) throws SQLException {
        stats.incrementQueryForObjectCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            Object object = executor.queryForObject(id, parameterObject);

            stats.addFetchRowCount(1);
            if (statementStats != null) {
                statementStats.addFetchRowCount(1);
            }

            return object;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public Object queryForObject(String id) throws SQLException {
        stats.incrementQueryForObjectCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            Object object = executor.queryForObject(id);

            stats.addFetchRowCount(1);
            if (statementStats != null) {
                statementStats.addFetchRowCount(1);
            }

            return object;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public Object queryForObject(String id, Object parameterObject, Object resultObject) throws SQLException {
        stats.incrementQueryForObjectCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            Object object = executor.queryForObject(id, parameterObject, resultObject);

            stats.addFetchRowCount(1);
            if (statementStats != null) {
                statementStats.addFetchRowCount(1);
            }

            return object;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public List queryForList(String id, Object parameterObject) throws SQLException {
        stats.incrementQueryForListCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            List list = executor.queryForList(id, parameterObject);

            int size = list.size();
            stats.addFetchRowCount(size);
            if (statementStats != null) {
                statementStats.addFetchRowCount(size);
            }

            return list;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public List queryForList(String id) throws SQLException {
        stats.incrementQueryForListCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            List list = executor.queryForList(id);

            int size = list.size();
            stats.addFetchRowCount(size);
            if (statementStats != null) {
                statementStats.addFetchRowCount(size);
            }

            return list;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public List queryForList(String id, Object parameterObject, int skip, int max) throws SQLException {
        stats.incrementQueryForListCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            List list = executor.queryForList(id, parameterObject, skip, max);

            int size = list.size();
            stats.addFetchRowCount(size);
            if (statementStats != null) {
                statementStats.addFetchRowCount(size);
            }

            return list;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public List queryForList(String id, int skip, int max) throws SQLException {
        stats.incrementQueryForListCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            List list = executor.queryForList(id, skip, max);

            int size = list.size();
            stats.addFetchRowCount(size);
            if (statementStats != null) {
                statementStats.addFetchRowCount(size);
            }

            return list;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public void queryWithRowHandler(String id, Object parameterObject, RowHandler rowHandler) throws SQLException {
        stats.incrementQueryForRowHandlerCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            executor.queryWithRowHandler(id, parameterObject, rowHandler);
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public void queryWithRowHandler(String id, RowHandler rowHandler) throws SQLException {
        stats.incrementQueryForRowHandlerCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            executor.queryWithRowHandler(id, rowHandler);
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }

    }

    public PaginatedList queryForPaginatedList(String id, Object parameterObject, int pageSize) throws SQLException {
        stats.incrementQueryForPaginatedListCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            PaginatedList list = executor.queryForPaginatedList(id, parameterObject, pageSize);

            int size = list.size();
            stats.addFetchRowCount(size);
            if (statementStats != null) {
                statementStats.addFetchRowCount(size);
            }

            return list;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public PaginatedList queryForPaginatedList(String id, int pageSize) throws SQLException {
        stats.incrementQueryForPaginatedListCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            PaginatedList list = executor.queryForPaginatedList(id, pageSize);

            int size = list.size();
            stats.addFetchRowCount(size);
            if (statementStats != null) {
                statementStats.addFetchRowCount(size);
            }

            return list;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public Map queryForMap(String id, Object parameterObject, String keyProp) throws SQLException {
        stats.incrementQueryForMapCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            Map map = executor.queryForMap(id, parameterObject, keyProp);

            int mapSize = map.size();
            stats.addFetchRowCount(mapSize);
            if (statementStats != null) {
                statementStats.addFetchRowCount(mapSize);
            }

            return map;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public Map queryForMap(String id, Object parameterObject, String keyProp, String valueProp) throws SQLException {
        stats.incrementQueryForMapCount();

        SpringIbatisStatementStats statementStats = getStatementStats(id, parameterObject, executor);

        long startNano = System.nanoTime();

        try {
            executeBefore(statementStats);

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            Map map = executor.queryForMap(id, parameterObject, keyProp, valueProp);

            int mapSize = map.size();
            stats.addFetchRowCount(mapSize);
            if (statementStats != null) {
                statementStats.addFetchRowCount(mapSize);
            }

            return map;
        } catch (SQLException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(statementStats, ex);
            throw ex;
        } catch (Error ex) {
            handleError(statementStats, ex);
            throw ex;
        } finally {
            executeAfter(statementStats, startNano);
        }
    }

    public void startBatch() throws SQLException {
        stats.incrementStartBatchCount();
        IbatisUtils.setClientImpl(executor, clientImplWrapper);
        executor.startBatch();
    }

    public int executeBatch() throws SQLException {
        stats.incrementExecuteBatchCount();

        long startNano = System.nanoTime();

        try {
            stats.incrementRunningCount();

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            return executor.executeBatch();
        } catch (SQLException ex) {
            handleError(null, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(null, ex);
            throw ex;
        } catch (Error ex) {
            handleError(null, ex);
            throw ex;
        } finally {
            long nano = System.nanoTime() - startNano;

            stats.addExecuteNano(nano);
            stats.decrementRunningCount();
        }
    }

    public List executeBatchDetailed() throws SQLException, BatchException {
        stats.incrementExecuteBatchDetailedCount();
        long startNano = System.nanoTime();

        try {
            stats.incrementRunningCount();

            IbatisUtils.setClientImpl(executor, clientImplWrapper);
            return executor.executeBatchDetailed();
        } catch (SQLException ex) {
            handleError(null, ex);
            throw ex;
        } catch (RuntimeException ex) {
            handleError(null, ex);
            throw ex;
        } catch (Error ex) {
            handleError(null, ex);
            throw ex;
        } finally {
            long nano = System.nanoTime() - startNano;

            stats.addExecuteNano(nano);
            stats.decrementRunningCount();
        }
    }

    public SpringIbatisStatementStats getStatementStats(String id, SqlMapExecutor executor) {
        if (executor instanceof SqlMapSessionImpl) {
            return getStatementStats(id, (SqlMapSessionImpl) executor);
        }

        if (executor instanceof SqlMapClientImpl) {
            return getStatementStats(id, (SqlMapClientImpl) executor);
        }

        return null;
    }

    public SpringIbatisStatementStats getStatementStats(String id, Object parameterObject, SqlMapExecutor executor) {
        if (executor instanceof SqlMapSessionImpl) {
            SpringIbatisStatementStats stat = getStatementStats(id, parameterObject, (SqlMapSessionImpl) executor);

            if (stat.getId() == null) {
                return null;
            }

            return stat;
        }

        if (executor instanceof SqlMapClientImpl) {
            SpringIbatisStatementStats stat = getStatementStats(id, parameterObject, (SqlMapClientImpl) executor);

            if (stat.getId() == null) {
                return null;
            }

            return stat;
        }

        return null;
    }

    public SpringIbatisStatementStats getStatementStats(String id, SqlMapSessionImpl sessionImpl) {
        MappedStatement statement = sessionImpl.getMappedStatement(id);
        String key = id;
        SpringIbatisStatementStats statementStats = stats.getStatementMap().get(key);

        if (statementStats != null) {
            return statementStats;
        }

        stats.getStatementMap().putIfAbsent(key, createStatementStat(statement));

        return stats.getStatementMap().get(key);
    }

    public SpringIbatisStatementStats getStatementStats(String id, Object parameterObject, SqlMapSessionImpl sessionImpl) {
        MappedStatement statement = sessionImpl.getMappedStatement(id);
        String key = id;
        SpringIbatisStatementStats statementStats = stats.getStatementMap().get(key);

        if (statementStats != null) {
            return statementStats;
        }

        stats.getStatementMap().putIfAbsent(key, createStatementStat(parameterObject, statement));

        return stats.getStatementMap().get(key);
    }

    protected SpringIbatisStatementStats createStatementStat(Object parameterObject, MappedStatement statement) {
        SpringIbatisStatementStats stat = new SpringIbatisStatementStats(statement, parameterObject);

        return stat;
    }

    protected SpringIbatisStatementStats createStatementStat(MappedStatement statement) {
        return new SpringIbatisStatementStats(statement);
    }

    public SpringIbatisStatementStats getStatementStats(String id, SqlMapClientImpl clientImpl) {
        MappedStatement statement = clientImpl.getMappedStatement(id);
        String key = id;
        SpringIbatisStatementStats statementStats = stats.getStatementMap().get(key);

        if (statementStats != null) {
            return statementStats;
        }

        stats.getStatementMap().putIfAbsent(key, createStatementStat(statement));

        return stats.getStatementMap().get(key);
    }

    public SpringIbatisStatementStats getStatementStats(String id, Object parameterObject, SqlMapClientImpl clientImpl) {
        MappedStatement statement = clientImpl.getMappedStatement(id);
        String key = id;
        SpringIbatisStatementStats statementStats = stats.getStatementMap().get(key);

        if (statementStats != null) {
            return statementStats;
        }

        stats.getStatementMap().putIfAbsent(key, createStatementStat(parameterObject, statement));

        return stats.getStatementMap().get(key);
    }
}
