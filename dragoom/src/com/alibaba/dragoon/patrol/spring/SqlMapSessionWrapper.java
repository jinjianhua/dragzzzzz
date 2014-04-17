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
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;

@SuppressWarnings("deprecation")
public class SqlMapSessionWrapper extends SqlMapExecutorWrapper implements SqlMapSession {

    private SqlMapSession session;

    public SqlMapSessionWrapper(ExtendedSqlMapClient client, SpringIbatisStats stats, SqlMapSession session){
        super(client, stats, session);
        this.session = session;
    }

    public void startTransaction() throws SQLException {
        stats.incrementStartTransactionCount();
        session.startTransaction();
    }

    public void startTransaction(int transactionIsolation) throws SQLException {
        stats.incrementStartTransactionCount();
        session.startTransaction(transactionIsolation);
    }

    public void commitTransaction() throws SQLException {
        stats.incrementCommitTransactionCount();
        session.commitTransaction();
    }

    public void endTransaction() throws SQLException {
        stats.incrementEndTransactionCount();
        session.endTransaction();
    }

    public void setUserConnection(Connection connnection) throws SQLException {
        session.setUserConnection(connnection);
    }

    @Deprecated
    public Connection getUserConnection() throws SQLException {
        return session.getUserConnection();
    }

    public Connection getCurrentConnection() throws SQLException {
        return session.getCurrentConnection();
    }

    public DataSource getDataSource() {
        return session.getDataSource();
    }

    public void close() {
        stats.incrementCloseSessionCount();
        session.close();
    }

}
