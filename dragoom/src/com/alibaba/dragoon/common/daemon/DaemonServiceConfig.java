/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.daemon.filter.DaemonServicLifeCycleFilter;

public class DaemonServiceConfig {

    private static final Log                  LOG     = LogFactory.getLog(DaemonServiceConfig.class);

    private String                            name;

    private List<DaemonServicLifeCycleFilter> filters = new CopyOnWriteArrayList<DaemonServicLifeCycleFilter>();

    public List<DaemonServicLifeCycleFilter> getFilters() {
        return filters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void load(Properties properties) {
        this.name = properties.getProperty("server-name");

        String filtersProperty = properties.getProperty("server-filters");

        if (filtersProperty != null) {
            filtersProperty = filtersProperty.trim();
            for (String item : filtersProperty.split(",")) {
                item = item.trim();
                if (item.length() > 0) {
                    String filterPropertyKey = "server-filters." + item;
                    String filterPropertyClass = properties.getProperty(filterPropertyKey);

                    if (filterPropertyClass == null) {
                        throw new DaemonServiceException("load server lifecycle filter config error, property item '"
                                                         + filterPropertyKey + "' not found.");
                    }

                    Class<?> filterClass = loadClass(filterPropertyClass);

                    try {
                        DaemonServicLifeCycleFilter filter = (DaemonServicLifeCycleFilter) filterClass.newInstance();
                        filters.add(filter);
                    } catch (InstantiationException e) {
                        throw new DaemonServiceException("load server lifecycle filter error. " + item, e);
                    } catch (IllegalAccessException e) {
                        throw new DaemonServiceException("load server lifecycle filter error. " + item, e);
                    }
                }
            }
        }
    }

    protected Class<?> loadClass(String className) {
        Class<?> clazz = null;

        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            LOG.error("load class error", e);
        }

        if (clazz == null) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                LOG.error("load class error", e);
            }
        }

        return clazz;
    }

}
