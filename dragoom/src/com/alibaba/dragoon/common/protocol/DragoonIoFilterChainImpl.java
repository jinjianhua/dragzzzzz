/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import java.util.List;

public class DragoonIoFilterChainImpl implements DragoonIoFilterChain {

    private DragoonSessionConfig config;
    private int                  pos;

    public DragoonIoFilterChainImpl(DragoonSessionConfig config){
        super();
        this.config = config;
    }

    public DragoonIoFilter nextFilter() {
        return config.getSessionFilters().get(pos++);
    }

    public List<DragoonIoFilter> getFilters() {
        return config.getSessionFilters();
    }

    public void sessionOpened(DragoonSession session) {
        if (this.pos < getFilters().size()) {
            nextFilter().sessionOpened(this, session);
            return;
        }
    }

    public void sessionClosed(DragoonSession session) {
        if (this.pos < getFilters().size()) {
            nextFilter().sessionClosed(this, session);
            return;
        }

        session.getImpl().close();
    }
}
