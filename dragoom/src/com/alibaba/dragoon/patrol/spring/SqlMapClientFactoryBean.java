/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.spring;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.support.lob.LobHandler;

import com.alibaba.dragoon.stat.SpringIbatisStats;
import com.alibaba.dragoon.stat.SpringStatManager;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;

@SuppressWarnings("deprecation")
public class SqlMapClientFactoryBean implements FactoryBean, InitializingBean {

    private SpringIbatisStats                                      stats = SpringStatManager.getInstance().getIbatisStat();

    private org.springframework.orm.ibatis.SqlMapClientFactoryBean raw   = new org.springframework.orm.ibatis.SqlMapClientFactoryBean();

    public void afterPropertiesSet() throws Exception {
        raw.afterPropertiesSet();
    }

    public Object getObject() throws Exception {
        ExtendedSqlMapClient client = (ExtendedSqlMapClient) raw.getObject();
        SqlMapClientWrapper wrapper = new SqlMapClientWrapper(stats, client);
        return wrapper;
    }

    public Class<?> getObjectType() {
        return ExtendedSqlMapClient.class;
    }

    public SpringIbatisStats getStats() {
        return stats;
    }

    public void setStats(SpringIbatisStats stats) {
        this.stats = stats;
    }

    public boolean isSingleton() {
        return raw.isSingleton();
    }

    public void setConfigLocation(Resource configLocation) {
        raw.setConfigLocation(configLocation);
    }

    public void setConfigLocations(Resource[] configLocations) {
        raw.setConfigLocations(configLocations);
    }

    public void setMappingLocations(Resource[] mappingLocations) {
        raw.setMappingLocations(mappingLocations);
    }

    public void setSqlMapClientProperties(Properties sqlMapClientProperties) {
        raw.setSqlMapClientProperties(sqlMapClientProperties);
    }

    public void setDataSource(DataSource dataSource) {
        raw.setDataSource(dataSource);
    }

    public void setUseTransactionAwareDataSource(boolean useTransactionAwareDataSource) {
        raw.setUseTransactionAwareDataSource(useTransactionAwareDataSource);
    }

    public void setTransactionConfigClass(Class<?> transactionConfigClass) {
        raw.setTransactionConfigClass(transactionConfigClass);
    }

    public void setTransactionConfigProperties(Properties transactionConfigProperties) {
        raw.setTransactionConfigProperties(transactionConfigProperties);
    }

    public void setLobHandler(LobHandler lobHandler) {
        raw.setLobHandler(lobHandler);
    }
}
