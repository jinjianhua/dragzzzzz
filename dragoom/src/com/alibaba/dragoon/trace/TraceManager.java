/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.trace;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.alibaba.dragoon.trace.selector.TraceSelector;

public class TraceManager implements TraceManagerMBean {

    private final static TraceManager instance  = new TraceManager();

    private final List<TraceSelector> selectors = new CopyOnWriteArrayList<TraceSelector>();
    private boolean                   traceEnable;

    private TraceManager(){

    }

    public static TraceManager getInstance() {
        return instance;
    }

    public boolean isTraceEnable() {
        return traceEnable;
    }

    public void setTraceEnable(boolean traceEnable) {
        this.traceEnable = traceEnable;
    }

    public List<TraceSelector> getSelectors() {
        return selectors;
    }
}
