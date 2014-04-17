/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon.filter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.protocol.DragoonAcceptor;
import com.alibaba.dragoon.common.protocol.DragoonSession;
import com.alibaba.dragoon.common.protocol.MessageHandler;

public class DragoonAcceptorFilter extends DaemonServicLifeCycleFilterAdapter {

    private Log                                      LOG       = LogFactory.getLog(DragoonAcceptorFilter.class);

    private final static List<DragoonAcceptorFilter> instances = new CopyOnWriteArrayList<DragoonAcceptorFilter>();

    private DragoonAcceptor                          acceptor;

    private MessageHandler                           messageHandler;

    public String getFullName() {
        return service.getFullName();
    }

    public DragoonAcceptor getAcceptor() {
        return acceptor;
    }

    public long getAcceptCount() {
        return acceptor.getAcceptCount();
    }

    public void setAcceptor(DragoonAcceptor acceptor) {
        this.acceptor = acceptor;
    }

    public List<DragoonSession> getSessionList() {
        return acceptor.getSessionList();
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void start(DaemonServiceLifeCycleFilterChain chain) {
        try {
            acceptor.start();
        } catch (IOException e) {
            LOG.error("start error", e);
        }

        instances.add(this);

        super.start(chain);
    }

    @Override
    public void stop(DaemonServiceLifeCycleFilterChain chain) {
        acceptor.stop();

        instances.remove(this);

        super.stop(chain);
    }

    public static List<DragoonAcceptorFilter> getInstances() {
        return instances;
    }

}
