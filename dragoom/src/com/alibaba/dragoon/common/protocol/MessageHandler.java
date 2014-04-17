/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import com.alibaba.dragoon.common.protocol.message.DragoonNotificationMessage;
import com.alibaba.dragoon.common.protocol.message.RequestMessage;
import com.alibaba.dragoon.common.protocol.message.ResponseMessage;

public interface MessageHandler {
    
    ResponseMessage handle(DragoonSession session, RequestMessage request);

//    GetAttributeResp handle(DragoonSession session, GetAttribute message);
//
//    ConnectResp handle(DragoonSession session, Connect message);
//
//    InvokeResp handle(DragoonSession session, Invoke message);
//
//    StatusResp handle(DragoonSession session, Status message);
//
//    SetAttributeResp handle(DragoonSession session, SetAttribute message);

    //RegisterResp handle(DragoonSession session, Register message);

    //MonitorListResp handle(DragoonSession session, MonitorList message);

    void handle(DragoonSession session, DragoonNotificationMessage message);

}
