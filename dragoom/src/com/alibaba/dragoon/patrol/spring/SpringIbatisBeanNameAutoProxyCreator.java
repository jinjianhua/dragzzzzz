/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.alibaba.dragoon.stat.SpringIbatisStats;
import com.alibaba.dragoon.stat.SpringStatManager;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;

@SuppressWarnings({ "serial", "deprecation" })
public class SpringIbatisBeanNameAutoProxyCreator extends BeanNameAutoProxyCreator implements InitializingBean, SpringIbatisBeanNameAutoProxyCreatorMBean {

    private final static Log   LOG            = LogFactory.getLog(SpringIbatisBeanNameAutoProxyCreator.class);

    private SpringIbatisStats  stats          = SpringStatManager.getInstance().getIbatisStat();
    private final List<String> proxyBeanNames = new ArrayList<String>();

    public List<String> getProxyBeanNames() {
        return proxyBeanNames;
    }

    public SpringIbatisStats getStats() {
        return stats;
    }

    public void setStats(SpringIbatisStats stats) {
        this.stats = stats;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(stats);
    }

    @SuppressWarnings("rawtypes")
    protected Object createProxy(Class beanClass, String beanName, Object[] specificInterceptors,
                                 TargetSource targetSource) {
        try {
            Object target = targetSource.getTarget();
            
            if (target instanceof SqlMapClientWrapper) {
                proxyBeanNames.add(beanName);
                stats.getProxyBeanNames().add(beanName);
                return target;
            }

            if (target instanceof SqlMapClient) {
                proxyBeanNames.add(beanName);
                stats.getProxyBeanNames().add(beanName);

                return new SqlMapClientWrapper(stats, (ExtendedSqlMapClient) target);
            }

            return super.createProxy(beanClass, beanName, specificInterceptors, targetSource);
        } catch (Throwable ex) {
            LOG.error(ex.getMessage(), ex);
            return super.createProxy(beanClass, beanName, specificInterceptors, targetSource);
        }
    }

}
