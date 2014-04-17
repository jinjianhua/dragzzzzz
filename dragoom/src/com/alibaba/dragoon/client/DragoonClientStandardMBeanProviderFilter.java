/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client;

import java.lang.management.ManagementFactory;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.client.jmx.DragoonStandardMBeanProvider;
import com.alibaba.dragoon.common.daemon.DaemonService;
import com.alibaba.dragoon.common.daemon.filter.DaemonServicLifeCycleFilterAdapter;
import com.alibaba.dragoon.common.daemon.filter.DaemonServiceLifeCycleFilterChain;

public class DragoonClientStandardMBeanProviderFilter extends DaemonServicLifeCycleFilterAdapter {

    private final static Log             LOG = LogFactory.getLog(DragoonClientStandardMBeanProviderFilter.class);

    private DragoonStandardMBeanProvider standardMBeanProvider;

    public DragoonStandardMBeanProvider getStandardMBeanProvider() {
        return standardMBeanProvider;
    }

    public void setStandardMBeanProvider(DragoonStandardMBeanProvider standardMBeanProvider) {
        this.standardMBeanProvider = standardMBeanProvider;
    }

    @Override
    public void init(DaemonService service) {
        this.service = service;

        if (standardMBeanProvider == null) {
            standardMBeanProvider = new DragoonStandardMBeanProvider();
        }
    }

    public DaemonService getService() {
        return service;
    }

    @Override
    public void start(DaemonServiceLifeCycleFilterChain chain) {
        try {
            standardMBeanProvider.register(ManagementFactory.getPlatformMBeanServer());
        } catch (JMException e) {
            LOG.error("start error", e);
        }

        chain.start();
    }

    @Override
    public void stop(DaemonServiceLifeCycleFilterChain chain) {
        try {
            standardMBeanProvider.unregister(ManagementFactory.getPlatformMBeanServer());
        } catch (JMException e) {
            LOG.error("stop error", e);
        }

        chain.stop();
    }
}
