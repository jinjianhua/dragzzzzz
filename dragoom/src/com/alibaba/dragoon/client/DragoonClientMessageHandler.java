/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client;

import javax.management.MBeanServer;

import com.alibaba.dragoon.common.protocol.MBeanServerMessageHandler;

public class DragoonClientMessageHandler extends MBeanServerMessageHandler {

    private final DragoonClient client;

    public DragoonClientMessageHandler(DragoonClient client, MBeanServer mbeanServer){
        super(mbeanServer);
        this.client = client;
    }

    public DragoonClient getClient() {
        return client;
    }

}
