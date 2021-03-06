package com.alibaba.dragoon.patrol.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

import com.alibaba.dragoon.stat.SpringIbatisStats;
import com.alibaba.dragoon.stat.SpringStatManager;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;

/**
 * 类BeanTypeAutoProxyCreator.java的实现描述：使用配置类型代替Springframework中配置名称的实现
 * 
 * @author hualiang.lihl 2011-12-31 上午10:48:20
 */
@SuppressWarnings("deprecation")
public class SpringIbatisBeanTypeAutoProxyCreator extends AbstractAutoProxyCreator implements InitializingBean, ApplicationContextAware, SpringIbatisBeanTypeAutoProxyCreatorMBean {

    private final static Log   LOG              = LogFactory.getLog(SpringIbatisBeanTypeAutoProxyCreator.class);

    private static final long  serialVersionUID = -9094985530794052264L;

    private Class<?>           targetBeanType   = SqlMapClient.class;

    private ApplicationContext context;

    private SpringIbatisStats  stats            = SpringStatManager.getInstance().getIbatisStat();

    private List<String>       beanNames        = new ArrayList<String>();
    private final List<String> proxyBeanNames   = new ArrayList<String>();

    /**
     * @param targetClass the targetClass to set
     */
    public void setTargetBeanType(Class<?> targetClass) {
        this.targetBeanType = targetClass;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    /**
     * Identify as bean to proxy if the bean name is in the configured list of names.
     */
    @SuppressWarnings("rawtypes")
    protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String beanName, TargetSource targetSource) {
        for (Iterator<String> it = this.beanNames.iterator(); it.hasNext();) {
            String mappedName = (String) it.next();
            if (FactoryBean.class.isAssignableFrom(beanClass)) {
                if (!mappedName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
                    continue;
                }
                mappedName = mappedName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
            }
            if (isMatch(beanName, mappedName)) {
                return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
            }
        }
        return DO_NOT_PROXY;
    }

    @SuppressWarnings({ "rawtypes" })
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

    /**
     * Return if the given bean name matches the mapped name.
     * <p>
     * The default implementation checks for "xxx*", "*xxx" and "*xxx*" matches, as well as direct equality. Can be
     * overridden in subclasses.
     * 
     * @param beanName the bean name to check
     * @param mappedName the name in the configured list of names
     * @return if the names match
     * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
     */
    protected boolean isMatch(String beanName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, beanName);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(targetBeanType, "targetType cannot be null");
        String[] beanNames = context.getBeanNamesForType(targetBeanType);
        for (String name : beanNames) {
            this.beanNames.add(name);
        }
    }

    public List<String> getBeanNames() {
        return beanNames;
    }

    public List<String> getProxyBeanNames() {
        return proxyBeanNames;
    }

}
