/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon.filter;

public class LogFilterAdapter extends DaemonServicLifeCycleFilterAdapter {

    @Override
    public void start(DaemonServiceLifeCycleFilterChain chain) {
        beforeStartLog(chain);
        super.start(chain);
        AfterStartLog(chain);
    }

    @Override
    public void stop(DaemonServiceLifeCycleFilterChain chain) {
        beforeStopLog(chain);
        super.stop(chain);
        AfterStopLog(chain);
    }

    protected void beforeStartLog(DaemonServiceLifeCycleFilterChain chain) {

    }

    protected void AfterStartLog(DaemonServiceLifeCycleFilterChain chain) {

    }

    protected void beforeStopLog(DaemonServiceLifeCycleFilterChain chain) {

    }

    protected void AfterStopLog(DaemonServiceLifeCycleFilterChain chain) {

    }
}
