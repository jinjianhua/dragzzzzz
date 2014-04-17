package com.alibaba.dragoon.patrol.web;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;

import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.SpringMethodInvokeStatistic;
import com.alibaba.dragoon.stat.WebURIStatistic;
import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.util.TransactionInfo;

public class PatrolWebJdbcStatFilter extends FilterAdapter {

    public void resultSet_close(FilterChain chain, ResultSetProxy resultSet) throws SQLException {
        chain.resultSet_close(resultSet);

        int fetchRowCount = resultSet.getFetchRowCount();
        {
            WebURIStatistic current = WebURIStatistic.current();
            if (current != null) {
                current.addJdbcFetchRowCount(fetchRowCount);
            }
        }
        {
            SpringMethodInvokeStatistic current = SpringMethodInvokeStatistic.current();
            if (current != null) {
                current.addJdbcFetchRowCount(fetchRowCount);
            }
        }
    }

    void executeBefore(String executeType) {
        {
            WebURIStatistic current = WebURIStatistic.current();
            if (current != null) {
                current.incrementJdbcExecuteCount();
            }
        }
        {
            SpringMethodInvokeStatistic current = SpringMethodInvokeStatistic.current();
            if (current != null) {
                current.incrementJdbcExecuteCount();
            }
        }
    }

    void executeAfter() {
    }

    void addUpdateCount(int updateCount) {
        if (updateCount <= 0) { // oracle executeBatch -2
            return;
        }

        WebURIStatistic current = WebURIStatistic.current();
        if (current != null) {
            current.addJdbcUpdateCount(updateCount);
        }
    }

    @Override
    public void connection_commit(FilterChain chain, ConnectionProxy connection) throws SQLException {
        if (!connection.getAutoCommit()) {
            {
                WebURIStatistic current = WebURIStatistic.current();
                if (current != null) {
                    current.incrementJdbcCommitCount();
                    
                    Map<String, Object> attributes = connection.getAttributes();
                    TransactionInfo transInfo = (TransactionInfo) attributes.get(StatFilter.ATTR_TRANSACTION);
                    if (transInfo != null) {
                        if (transInfo.getEndTimeMillis() == 0) {
                            transInfo.setEndTimeMillis(System.currentTimeMillis());
                        }
                        
                        long millis = transInfo.getEndTimeMillis() - transInfo.getStartTimeMillis();
                        current.incrementJdbcTransactionTimeMillis(millis);
                    }
                }
            }

            {
                SpringMethodInvokeStatistic current = SpringMethodInvokeStatistic.current();
                if (current != null) {
                    current.incrementJdbcCommitCount();
                }
            }

            // incrementTransactionTimeNano
        }
        chain.connection_commit(connection);
    }

    @Override
    public void connection_rollback(FilterChain chain, ConnectionProxy connection) throws SQLException {
        {
            WebURIStatistic current = WebURIStatistic.current();
            if (current != null) {
                current.incrementJdbcRollbackCount();
            }
        }
        {
            SpringMethodInvokeStatistic current = SpringMethodInvokeStatistic.current();
            if (current != null) {
                current.incrementJdbcRollbackCount();
            }
        }
        chain.connection_rollback(connection);
    }

    @Override
    public void connection_rollback(FilterChain chain, ConnectionProxy connection, Savepoint savepoint)
                                                                                                       throws SQLException {
        {
            WebURIStatistic current = WebURIStatistic.current();
            if (current != null) {
                current.incrementJdbcRollbackCount();
            }
        }
        {
            SpringMethodInvokeStatistic current = SpringMethodInvokeStatistic.current();
            if (current != null) {
                current.incrementJdbcRollbackCount();
            }
        }
        chain.connection_rollback(connection, savepoint);
    }

    // ///////////////
    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        executeBefore("Execute");
        try {
            return chain.statement_execute(statement, sql);
        } finally {
            Profiler.release();
        }
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys)
                                                                                                                    throws SQLException {
        executeBefore("Execute");
        try {
            return chain.statement_execute(statement, sql, autoGeneratedKeys);
        } finally {
            executeAfter();
        }
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int columnIndexes[])
                                                                                                                  throws SQLException {
        executeBefore("Execute");
        try {
            return chain.statement_execute(statement, sql, columnIndexes);
        } finally {
            executeAfter();
        }
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, String columnNames[])
                                                                                                                   throws SQLException {
        executeBefore("Execute");
        try {
            return chain.statement_execute(statement, sql, columnNames);
        } finally {
            executeAfter();
        }
    }

    @Override
    public int[] statement_executeBatch(FilterChain chain, StatementProxy statement) throws SQLException {
        executeBefore("ExecuteBatch");
        try {
            return chain.statement_executeBatch(statement);
        } finally {
            executeAfter();
        }
    }

    // /////////////////////////////
    @Override
    public ResultSetProxy statement_executeQuery(FilterChain chain, StatementProxy statement, String sql)
                                                                                                         throws SQLException {
        executeBefore("ExecuteQuery");
        try {
            return chain.statement_executeQuery(statement, sql);
        } finally {
            executeAfter();
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        executeBefore("ExecuteUpdate");
        try {
            int updateCount = chain.statement_executeUpdate(statement, sql);
            addUpdateCount(updateCount);
            return updateCount;
        } finally {
            executeAfter();
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys)
                                                                                                                      throws SQLException {
        executeBefore("ExecuteUpdate");
        try {
            int updateCount = chain.statement_executeUpdate(statement, sql, autoGeneratedKeys);
            addUpdateCount(updateCount);
            return updateCount;
        } finally {
            executeAfter();
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int columnIndexes[])
                                                                                                                    throws SQLException {
        executeBefore("ExecuteUpdate");
        try {
            int updateCount = chain.statement_executeUpdate(statement, sql, columnIndexes);
            addUpdateCount(updateCount);
            return updateCount;
        } finally {
            executeAfter();
        }
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, String columnNames[])
                                                                                                                     throws SQLException {
        executeBefore("ExecuteUpdate");
        try {
            int updateCount = chain.statement_executeUpdate(statement, sql, columnNames);
            addUpdateCount(updateCount);
            return updateCount;
        } finally {
            executeAfter();
        }
    }

    // ////////

    @Override
    public boolean preparedStatement_execute(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        executeBefore("Execute");
        try {
            return chain.preparedStatement_execute(statement);
        } finally {
            executeAfter();
        }
    }

    @Override
    public ResultSetProxy preparedStatement_executeQuery(FilterChain chain, PreparedStatementProxy statement)
                                                                                                             throws SQLException {
        executeBefore("ExecuteQuery");
        try {
            return chain.preparedStatement_executeQuery(statement);
        } finally {
            executeAfter();
        }
    }

    @Override
    public int preparedStatement_executeUpdate(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        executeBefore("ExecuteUpdate");
        try {
            int updateCount = chain.preparedStatement_executeUpdate(statement);
            addUpdateCount(updateCount);
            return updateCount;
        } finally {
            executeAfter();
        }
    }
}
