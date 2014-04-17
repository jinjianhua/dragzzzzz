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
import java.util.List;

public interface DragoonAcceptor extends DragoonIoService {

    List<SocketAddress> getListeningSocketAddresses();

    DragoonSessionConfig getSessionConfig();

    // DragoonSession accept(MessageHandler messageHandler) throws
    // InterruptedException;

    void start() throws IOException;

    void stop();

    void bind(SocketAddress address) throws IOException;

    List<DragoonSession> getSessionList();

    long getAcceptCount();
}
