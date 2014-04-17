/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import java.net.SocketAddress;
import java.util.concurrent.Future;

public interface DragoonConnector extends DragoonIoService {

    Future<DragoonSession> connect(SocketAddress address, MessageHandler handler);

    void close();

    String getRemoteAddress();

    long getConnectCount();

    long getConnectEstablishedCount();

    long getReceivedBytes();

    long getSentBytes();

    long getReceivedMessages();

    long getSentMessages();
}
