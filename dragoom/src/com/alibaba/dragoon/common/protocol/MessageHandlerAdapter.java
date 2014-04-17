/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import com.alibaba.dragoon.common.protocol.message.Connect;
import com.alibaba.dragoon.common.protocol.message.ConnectResp;
import com.alibaba.dragoon.common.protocol.message.DragoonNotificationMessage;
import com.alibaba.dragoon.common.protocol.message.GetAttribute;
import com.alibaba.dragoon.common.protocol.message.GetAttributeResp;
import com.alibaba.dragoon.common.protocol.message.Invoke;
import com.alibaba.dragoon.common.protocol.message.InvokeResp;
import com.alibaba.dragoon.common.protocol.message.RequestMessage;
import com.alibaba.dragoon.common.protocol.message.ResponseMessage;
import com.alibaba.dragoon.common.protocol.message.SetAttribute;
import com.alibaba.dragoon.common.protocol.message.SetAttributeResp;
import com.alibaba.dragoon.common.protocol.message.Status;
import com.alibaba.dragoon.common.protocol.message.StatusResp;

public class MessageHandlerAdapter implements MessageHandler {

    public GetAttributeResp handle(DragoonSession session, GetAttribute message) {
        return new GetAttributeResp(new UnsupportedOperationException());
    }

    public ConnectResp handle(DragoonSession session, Connect message) {
        return new ConnectResp(new UnsupportedOperationException());
    }

    public StatusResp handle(DragoonSession session, Status message) {
        return new StatusResp();
    }

    public InvokeResp handle(DragoonSession session, Invoke message) {
        return new InvokeResp(new UnsupportedOperationException());
    }

    public SetAttributeResp handle(DragoonSession session, SetAttribute message) {
        return new SetAttributeResp(new UnsupportedOperationException());
    }

    public void handle(DragoonSession session, DragoonNotificationMessage message) {
    }



    public ResponseMessage handle(DragoonSession session, RequestMessage request) {
        if (request instanceof GetAttribute) {
            return handle(session, (GetAttribute) request);
        }
        
        if (request instanceof Connect) {
            return handle(session, (Connect) request);
        }
        
        if (request instanceof Status) {
            return handle(session, (Status) request);
        }
        
        if (request instanceof Invoke) {
            return handle(session, (Invoke) request);
        }
        
        if (request instanceof SetAttribute) {
            return handle(session, (SetAttribute) request);
        }
        
        throw new IllegalStateException("not support message : " + request);
    }

}
