/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import java.io.IOException;
import java.net.SocketAddress;

import com.alibaba.dragoon.common.protocol.message.DragoonMessage;

public interface DragoonSessionImpl {

    void sendMessageDirect(DragoonMessage message) throws IOException;

    void init(DragoonSession session);

    SocketAddress getLocalAddress();

    SocketAddress getRemoteAddress();

    void close();

    public static enum State {
        Established, Closing, Closed
    }
}
