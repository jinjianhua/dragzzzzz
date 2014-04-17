/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.transport.socket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.protocol.DragoonConnector;
import com.alibaba.dragoon.common.protocol.DragoonSession;
import com.alibaba.dragoon.common.protocol.DragoonSessionConfig;
import com.alibaba.dragoon.common.protocol.MessageHandler;
import com.alibaba.dragoon.common.utils.DaemonThreadFactory;

public class SocketConnector implements DragoonConnector, SocketConnectorMBean {

    private final static Log LOG                     = LogFactory.getLog(SocketConnector.class);

    private ExecutorService  connectorExecutor;
    private AtomicLong       connectCount            = new AtomicLong();
    private final AtomicLong connectEstablishedCount = new AtomicLong();

    private AtomicLong       receivedBytes           = new AtomicLong();
    private AtomicLong       receivedMessages        = new AtomicLong();
    private AtomicLong       sentBytes               = new AtomicLong();
    private AtomicLong       sentMessages            = new AtomicLong();
    private AtomicLong       sessionIdSeed           = new AtomicLong(1000);

    private String           remoteAddress;

    public SocketConnector(){
        connectorExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("SocketConnecor"));
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public long getConnectCount() {
        return connectCount.get();
    }

    public long getConnectEstablishedCount() {
        return connectEstablishedCount.get();
    }

    public long getReceivedMessages() {
        return receivedMessages.get();
    }

    public long getSentMessages() {
        return sentMessages.get();
    }

    public long getReceivedBytes() {
        return receivedBytes.get();
    }

    public long getSentBytes() {
        return sentBytes.get();
    }

    public Future<DragoonSession> connect(final SocketAddress address, final MessageHandler messageHandler) {
        if (address == null) {
            throw new IllegalArgumentException("address is null");
        }

        if (!(address instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("address must be VmPipeAddress.");
        }

        final InetSocketAddress inetSocketAddress = (InetSocketAddress) address;

        FutureTask<DragoonSession> task = new FutureTask<DragoonSession>(new Callable<DragoonSession>() {

            public DragoonSession call() throws Exception {
                connectCount.incrementAndGet();
                if (LOG.isInfoEnabled()) {
                    LOG.info("CONNECT TO " + inetSocketAddress);
                }

                remoteAddress = inetSocketAddress.toString();
                Socket socket = new Socket(inetSocketAddress.getAddress(), inetSocketAddress.getPort());

                connectEstablishedCount.incrementAndGet();

                if (LOG.isInfoEnabled()) {
                    LOG.info("CONNECTED TO " + inetSocketAddress);
                }

                SocketSessionImpl impl = new SocketSessionImpl(socket, receivedBytes, receivedMessages, sentBytes,
                                                               sentMessages);

                final long sessionId = generateSessionId();

                DragoonSession session = new DragoonSession(sessionId, new DragoonSessionConfig(messageHandler), impl);
                impl.init(session);

                return session;
            }

        });
        connectorExecutor.submit(task);

        return task;
    }

    public synchronized void close() {
        if (connectorExecutor != null) {
            connectorExecutor.shutdownNow();
            connectorExecutor = null;
        }
    }

    public long generateSessionId() {
        return sessionIdSeed.incrementAndGet();
    }
}
