/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonsLogFilter extends LogFilterAdapter {

    private final Log log;

    public CommonsLogFilter(){
        log = LogFactory.getLog(CommonsLogFilter.class);
    }

    public CommonsLogFilter(Log log){
        this.log = log;
    }

    protected void beforeStartLog(DaemonServiceLifeCycleFilterChain chain) {
        log.info(service.getConfig().getName() + " start ...");
    }

    protected void AfterStartLog(DaemonServiceLifeCycleFilterChain chain) {
        log.info(service.getConfig().getName() + " started.");
    }

    protected void beforeStopLog(DaemonServiceLifeCycleFilterChain chain) {
        log.info(service.getConfig().getName() + " stop ...");
    }

    protected void AfterStopLog(DaemonServiceLifeCycleFilterChain chain) {
        log.info(service.getConfig().getName() + " stopped.");
    }
}
