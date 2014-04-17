/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon.filter;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JmxRegisterFilter extends DaemonServicLifeCycleFilterAdapter {

    private final Log   log = LogFactory.getLog(JmxRegisterFilter.class);

    private ObjectName  serviceObjectName;
    private MBeanServer mbeanServer;

    public JmxRegisterFilter(ObjectName serviceObjectName){
        this(null, serviceObjectName);
    }

    public JmxRegisterFilter(MBeanServer mbeanServer, ObjectName serviceObjectName){
        if (mbeanServer == null) {
            this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
        } else {
            this.mbeanServer = mbeanServer;
        }

        this.serviceObjectName = serviceObjectName;
    }

    public ObjectName getServiceObjectName() {
        return serviceObjectName;
    }

    @Override
    public void start(DaemonServiceLifeCycleFilterChain chain) {
        super.start(chain);
        try {
            mbeanServer.registerMBean(service, serviceObjectName);
        } catch (Exception e) {
            log.error("registerMBean error", e);
        }
    }

    @Override
    public void stop(DaemonServiceLifeCycleFilterChain chain) {
        super.start(chain);
        try {
            mbeanServer.unregisterMBean(serviceObjectName);
        } catch (Exception e) {
            log.error("registerMBean error", e);
        }
    }

}
