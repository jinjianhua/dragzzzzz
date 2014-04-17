/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.utils.JMXUtils;
import com.alibaba.dragoon.stat.KVStatManager;
import com.alibaba.dragoon.stat.LogStatManager;
import com.alibaba.dragoon.stat.SpringStatManager;
import com.alibaba.dragoon.stat.WebStatisticManager;
import com.alibaba.dragoon.stat.napoli.NapoliMQServerStatManager;
import com.alibaba.dragoon.stat.napoli.NapoliStatManager;
import com.alibaba.dragoon.trace.TraceManager;
import com.alibaba.druid.stat.JdbcStatManager;

public class PatrolUtils {

    private final static Log    LOG                            = LogFactory.getLog(PatrolUtils.class);

    public final static String  BEAN_NAME_SPRING_STAT          = "com.alibaba.dragoon:type=SpringStats";
    public final static String  BEAN_NAME_SPRING_METHOD_STAT   = "com.alibaba.dragoon:type=SpringMethodStats";
    public final static String  BEAN_NAME_SPRING_IBATIS_STAT   = "com.alibaba.dragoon:type=SpringIbatisStats";
    public final static String  BEAN_NAME_WEB_STAT             = "com.alibaba.dragoon:type=WebStats";
    public final static String  BEAN_NAME_JDBC_STAT            = "com.alibaba.dragoon:type=JdbcStats";
    public final static String  BEAN_NAME_KV_STATS             = "com.alibaba.dragoon:type=KVStats";
    // public final static String BEAN_NAME_BROWSER_STATS = "com.alibaba.dragoon:type=BrowserStats";

    private final static String BEAN_NAME_DATASOURCE_STAT      = "com.alibaba.druid:type=DruidDataSourceStat";
    public final static String  BEAN_NAME_JDBC_CONNECT_STAT    = "com.alibaba.dragoon:type=JdbcConnectionStats";
    public final static String  BEAN_NAME_JDBC_STATEMENT_STAT  = "com.alibaba.dragoon:type=JdbcStatementStats";
    public final static String  BEAN_NAME_JDBC_RESULTSET_STAT  = "com.alibaba.dragoon:type=JdbcResultSetStats";
    public final static String  BEAN_NAME_JDBC_TRACE           = "com.alibaba.dragoon:type=JdbcTrace";
    public final static String  BEAN_NAME_LOG_STAT             = "com.alibaba.dragoon:type=LogStats";
    public final static String  BEAN_NAME_EXCEPTION            = "com.alibaba.dragoon:type=Exception";
    public final static String  BEAN_NAME_TRACE                = "com.alibaba.dragoon:type=Trace";
    public final static String  BEAN_NAME_NAPOLI_STAT          = "com.alibaba.dragoon:type=NapoliStat";
    public final static String  BEAN_NAME_NAPOLI_MQSERVER_STAT = "com.alibaba.dragoon:type=NapoliMQServerStat";

    public static void registerMBean(boolean springStatEnable, boolean jdbcStatEnable, boolean uriStatEnable,
                                     boolean log4jStatEnable, boolean napoliStatEnable, boolean napoliMQServerStatEnable) {
        registerMBean(false, null, springStatEnable, jdbcStatEnable, uriStatEnable, log4jStatEnable, napoliStatEnable,
                      napoliMQServerStatEnable);
    }

    public static void registerMBean(boolean useJndiDataSource, ClassLoader containerClassLoader) {
        registerMBean(useJndiDataSource, containerClassLoader, true, true, true, true, true, true);
    }

    public static void registerMBean(boolean useJndiDataSource, ClassLoader containerClassLoader,
                                     boolean springStatEnable, boolean jdbcStatEnable, boolean uriStatEnable,
                                     boolean log4jStatEnable, boolean napoliStatEnable, boolean napoliMQServerStatEnable) {

        JMXUtils.register(BEAN_NAME_KV_STATS, KVStatManager.getInstance());
        // JMXUtils.register(BEAN_NAME_BROWSER_STATS, BrowserStatManager.getInstance());

        if (springStatEnable) {
            JMXUtils.register(BEAN_NAME_SPRING_STAT, SpringStatManager.getInstance());
            JMXUtils.register(BEAN_NAME_SPRING_METHOD_STAT, SpringStatManager.getInstance().getMethodStat());
            JMXUtils.register(BEAN_NAME_SPRING_IBATIS_STAT, SpringStatManager.getInstance().getIbatisStat());
        }

        if (uriStatEnable) {
            JMXUtils.register(BEAN_NAME_WEB_STAT, WebStatisticManager.getInstance());
        }

        if (log4jStatEnable) {
            JMXUtils.register(BEAN_NAME_LOG_STAT, LogStatManager.getInstance());
            // TODO remove it
            JMXUtils.register(BEAN_NAME_EXCEPTION, LogStatManager.getInstance()); // 兼容
        }

        JMXUtils.register(BEAN_NAME_TRACE, TraceManager.getInstance());

        if (jdbcStatEnable) {
            if (useJndiDataSource) {
                try {
                    Class<?> clazz = null;

                    try {
                        clazz = containerClassLoader.loadClass("com.alibaba.druid.stat.JdbcStatManager");
                    } catch (ClassNotFoundException ex) {
                        LOG.warn(ex.getMessage(), ex);
                    }

                    if (clazz != null) {
                        Object inst = clazz.getMethod("getInstance").invoke(null);
                        JMXUtils.register(BEAN_NAME_JDBC_STAT, inst);

                        Object connStat = clazz.getMethod("getConnectionstat").invoke(inst);
                        JMXUtils.register(BEAN_NAME_JDBC_CONNECT_STAT, connStat);

                        Object statementStat = clazz.getMethod("getStatementStat").invoke(inst);
                        JMXUtils.register(BEAN_NAME_JDBC_STATEMENT_STAT, statementStat);

                        Object resultSetStat = clazz.getMethod("getResultSetStat").invoke(inst);
                        JMXUtils.register(BEAN_NAME_JDBC_RESULTSET_STAT, resultSetStat);

                        Class<?> traceClazz = containerClassLoader.loadClass("com.alibaba.druid.stat.JdbcTraceManager");
                        Object traceInst = traceClazz.getMethod("getInstance").invoke(null);
                        JMXUtils.register(BEAN_NAME_JDBC_TRACE, traceInst);
                        
                        Class<?> dataSourceStatManagerClazz = containerClassLoader.loadClass("com.alibaba.druid.stat.DruidDataSourceStatManager");
                        Object dataSourceStatManagerInst = dataSourceStatManagerClazz.getMethod("getInstance").invoke(null);
                        JMXUtils.register(BEAN_NAME_DATASOURCE_STAT, dataSourceStatManagerInst);
                    } else {
                        registerJDBCMBean();
                    }
                } catch (Exception ex) {
                    LOG.error("register jdbc stat error", ex);
                }
            } else {
                registerJDBCMBean();
            }
        }

        // added by chris
        if (napoliStatEnable) {
            JMXUtils.register(BEAN_NAME_NAPOLI_STAT, NapoliStatManager.getInstance());
        }
        if (napoliMQServerStatEnable) {
            JMXUtils.register(BEAN_NAME_NAPOLI_MQSERVER_STAT, NapoliMQServerStatManager.getInstance());
        }
    }

    private static void registerJDBCMBean() {
        JMXUtils.register(BEAN_NAME_JDBC_STAT, JdbcStatManager.getInstance());
        // JMXUtils.register(BEAN_NAME_JDBC_CONNECT_STAT, JdbcStatManager.getInstance().getConnectionstat());
        // JMXUtils.register(BEAN_NAME_JDBC_STATEMENT_STAT, JdbcStatManager.getInstance().getStatementStat());
        // JMXUtils.register(BEAN_NAME_JDBC_RESULTSET_STAT, JdbcStatManager.getInstance().getResultSetStat());
        // JMXUtils.register(BEAN_NAME_JDBC_TRACE, JdbcTraceManager.getInstance());
    }

    public static void unregisterMBean() {
        JMXUtils.unregister(BEAN_NAME_SPRING_STAT);
        JMXUtils.unregister(BEAN_NAME_SPRING_METHOD_STAT);
        JMXUtils.unregister(BEAN_NAME_SPRING_IBATIS_STAT);
        JMXUtils.unregister(BEAN_NAME_WEB_STAT);
        JMXUtils.unregister(BEAN_NAME_KV_STATS);
        JMXUtils.unregister(BEAN_NAME_JDBC_STAT);
        // JMXUtils.unregister(BEAN_NAME_JDBC_CONNECT_STAT);
        // JMXUtils.unregister(BEAN_NAME_JDBC_STATEMENT_STAT);
        // JMXUtils.unregister(BEAN_NAME_JDBC_RESULTSET_STAT);
        // JMXUtils.unregister(BEAN_NAME_JDBC_TRACE);
        JMXUtils.unregister(BEAN_NAME_LOG_STAT);
        JMXUtils.unregister(BEAN_NAME_NAPOLI_STAT);
        JMXUtils.unregister(BEAN_NAME_NAPOLI_MQSERVER_STAT);
        // TODO remove it
        JMXUtils.unregister(BEAN_NAME_EXCEPTION);
    }
}
