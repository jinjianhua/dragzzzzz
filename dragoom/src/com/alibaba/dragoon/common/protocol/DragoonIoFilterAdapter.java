/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

public class DragoonIoFilterAdapter implements DragoonIoFilter {

    public void sessionOpened(DragoonIoFilterChain chain, DragoonSession session) {
        chain.sessionOpened(session);
    }

    public void sessionClosed(DragoonIoFilterChain chain, DragoonSession session) {
        chain.sessionClosed(session);
    }

}
