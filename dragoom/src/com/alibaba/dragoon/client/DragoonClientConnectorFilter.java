/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client;

import com.alibaba.dragoon.VERSION;
import com.alibaba.dragoon.common.daemon.filter.DragoonConnectorFilter;
import com.alibaba.dragoon.common.protocol.message.Connect;
import com.alibaba.dragoon.common.utils.JVMUtils;

public class DragoonClientConnectorFilter extends DragoonConnectorFilter {

    protected Connect createConnectMessage() {
        DragoonClient client = (DragoonClient) getService();

        Connect request = new Connect();

        request.getProperties().put("VERSION", VERSION.VERSION);
        request.getProperties().put("APP_NUM", client.getAppNumber());
        request.getProperties().put("INST_NUM", client.getAppInstNumber());
        request.getProperties().put("PID", JVMUtils.getPID());

        return request;
    }

}
