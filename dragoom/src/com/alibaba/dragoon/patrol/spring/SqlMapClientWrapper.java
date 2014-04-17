/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.spring;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.alibaba.dragoon.stat.SpringIbatisStats;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;

@SuppressWarnings({ "deprecation" })
public class SqlMapClientWrapper extends SqlMapExecutorWrapper implements SqlMapClient, ExtendedSqlMapClient {

    protected final ExtendedSqlMapClient client;
    

    public SqlMapClientWrapper(SpringIbatisStats stats, ExtendedSqlMapClient client){
        super(client, stats, client);
        this.client = client;
         
    }
    
    public ExtendedSqlMapClient getClient() {
        return this.client;
    }

    public void startTransaction() throws SQLException {
        stats.incrementStartTransactionCount();
        client.startTransaction();
    }

    public void startTransaction(int transactionIsolation) throws SQLException {
        stats.incrementStartTransactionCount();
        client.startTransaction(transactionIsolation);
    }

    public void commitTransaction() throws SQLException {
        stats.incrementCommitTransactionCount();
        client.commitTransaction();
    }

    public void endTransaction() throws SQLException {
        stats.incrementEndTransactionCount();
        client.endTransaction();
    }

    public void setUserConnection(Connection connnection) throws SQLException {
        client.setUserConnection(connnection);
    }

    public Connection getUserConnection() throws SQLException {
        return client.getUserConnection();
    }

    public Connection getCurrentConnection() throws SQLException {
        return client.getCurrentConnection();
    }

    public DataSource getDataSource() {
        return client.getDataSource();
    }

    public SqlMapSession openSession() {
        stats.incrementOpenSessionCount();

        SqlMapSession session = client.openSession();
        IbatisUtils.setClientImpl(session, clientImplWrapper);
        return new SqlMapSessionWrapper(client, stats, session);
    }

    public SqlMapSession openSession(Connection conn) {
        stats.incrementOpenSessionCount();
        SqlMapSession session = client.openSession(conn);
        IbatisUtils.setClientImpl(session, clientImplWrapper);
        return new SqlMapSessionWrapper(client, stats, session);
    }

    public SqlMapSession getSession() {
        SqlMapSession session = client.getSession();
        IbatisUtils.setClientImpl(session, clientImplWrapper);
        return new SqlMapSessionWrapper(client, stats, session);
    }

    public void flushDataCache() {
        client.flushDataCache();
    }

    public void flushDataCache(String cacheId) {
        client.flushDataCache(cacheId);
    }

    public MappedStatement getMappedStatement(String id) {
        return client.getMappedStatement(id);
    }

    public boolean isLazyLoadingEnabled() {
        return client.isLazyLoadingEnabled();
    }

    public boolean isEnhancementEnabled() {
        return client.isEnhancementEnabled();
    }

    public SqlExecutor getSqlExecutor() {
        return client.getSqlExecutor();
    }

    public SqlMapExecutorDelegate getDelegate() {
        return client.getDelegate();
    }

    public ResultObjectFactory getResultObjectFactory() {
        return client.getResultObjectFactory();
    }

}
