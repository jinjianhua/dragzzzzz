/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon.filter;

import com.alibaba.dragoon.common.daemon.DaemonService;

public class DaemonServicLifeCycleFilterAdapter implements DaemonServicLifeCycleFilter {

    protected DaemonService service;

    public void init(DaemonService service) {
        this.service = service;
    }

    public DaemonService getService() {
        return service;
    }

    public void start(DaemonServiceLifeCycleFilterChain chain) {
        if (chain != null) {
            chain.start();
        }

    }

    public void stop(DaemonServiceLifeCycleFilterChain chain) {
        if (chain != null) {
            chain.stop();
        }
    }

}
