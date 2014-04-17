/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon.filter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.protocol.DragoonConnector;
import com.alibaba.dragoon.common.protocol.DragoonIoFilterAdapter;
import com.alibaba.dragoon.common.protocol.DragoonIoFilterChain;
import com.alibaba.dragoon.common.protocol.DragoonSession;
import com.alibaba.dragoon.common.protocol.MessageHandler;
import com.alibaba.dragoon.common.protocol.message.Connect;
import com.alibaba.dragoon.common.protocol.message.ConnectResp;
import com.alibaba.dragoon.common.protocol.message.DragoonNotificationMessage;
import com.alibaba.dragoon.common.protocol.message.ResponseMessage;

public class DragoonConnectorFilter extends DaemonServicLifeCycleFilterAdapter implements DragoonConnectorFilterMBean {

    private final static Log                          LOG                         = LogFactory.getLog(DragoonConnectorFilter.class);

    private volatile DragoonSession                   session;
    protected DragoonConnector                        connector;
    protected SocketAddress                           serverAddress;

    private Thread                                    connectorThread;

    protected long                                    connectCheckDelayTimeMillis = 1000 * 3;

    protected CountDownLatch                          stopLatch;

    private MessageHandler                            messageHandler;

    private Thread                                    sendThread;

    protected final Lock                              lock                        = new ReentrantLock();

    protected final Condition                         connectedSignal             = lock.newCondition();

    private BlockingQueue<DragoonNotificationMessage> notifyQueue                 = new ArrayBlockingQueue<DragoonNotificationMessage>(
                                                                                                                                       50);

    private int                                       notifyQueueSizeThreshold    = 50;

    private final AtomicLong                          abandonNotificationCount    = new AtomicLong();

    protected AtomicInteger                           connectCount                = new AtomicInteger();

    protected volatile boolean                        connected                   = false;

    protected volatile boolean                        stopped                     = false;

    public DragoonConnectorFilter(){
        super();
    }

    public void waitForConnected(long timeout) throws InterruptedException {
        lock.lock();
        try {
            if (this.connected) {
                return;
            }

            connectedSignal.await(timeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public DragoonConnector getConnector() {
        return connector;
    }

    public final DragoonSession getSession() {
        return session;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public DragoonSession getSessionUntilConnected() throws InterruptedException {
        for (;;) {
            lock.lock();
            try {
                if (stopped) {
                    return null;
                }

                if (this.getSession() != null && this.isConnected()) {
                    return getSession();
                }

                connectedSignal.await(1, TimeUnit.SECONDS);
            } finally {
                lock.unlock();
            }
        }
    }

    public final void setSession(DragoonSession session) {
        lock.lock();
        try {
            this.session = session;
            if (this.session != null) {
                this.session.getConfig().getSessionFilters().add(new DragoonIoFilterAdapter() {

                    @Override
                    public void sessionClosed(DragoonIoFilterChain chain, DragoonSession session) {
                        DragoonConnectorFilter.this.session = null;
                        super.sessionClosed(chain, session);
                    }
                });
            }

            if (this.session == null) {
                connected = false;

                if (LOG.isDebugEnabled()) {
                    LOG.debug(service.getFullName() + " session disconnected.");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(service.getFullName() + " session established.");
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void setConnector(DragoonConnector connector) {
        this.connector = connector;
    }

    public SocketAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(SocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Override
    public void start(DaemonServiceLifeCycleFilterChain chain) {
        stopped = false;

        super.start(chain);

        if (connectorThread != null && connectorThread.isAlive()) {
            throw new IllegalStateException();
        }

        connectorThread = createConnectorThread();
        connectorThread.setDaemon(true);
        connectorThread.start();

        sendThread = new SendThread(service.getFullName() + " send");
        sendThread.setDaemon(true);
        sendThread.start();
    }

    protected Thread createConnectorThread() {
        ConnectorThread thread = new ConnectorThread();
        return thread;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void stop(DaemonServiceLifeCycleFilterChain chain) {
        stopped = true;

        chain.stop();

        if (connectorThread != null) {
            stopLatch = new CountDownLatch(1);

            connectorThread.interrupt();

            try {
                stopLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // skip
            }

            stopLatch = null;
            connectorThread = null;
        }

        if (sendThread != null) {
            stopLatch = new CountDownLatch(1);

            sendThread.interrupt();

            try {
                stopLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // skip
            }

            stopLatch = null;
            sendThread = null;
        }

        if (this.getSession() != null) {
            this.getSession().close();
            this.setSession(null);
        }

    }

    public BlockingQueue<DragoonNotificationMessage> getNotifyQueue() {
        return notifyQueue;
    }

    public SocketAddress parseAddress(String address, int port) {
        if (address == null || port == 0) {
            return null;
        } else {
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            return socketAddress;
        }
    }

    public int getConnectCount() {
        return connectCount.get();
    }

    public void sendNotification(DragoonNotificationMessage notification) {
        try {
            notifyQueue.put(notification);

            // 防止队列过长
            if (notifyQueue.size() > notifyQueueSizeThreshold) {

                DragoonNotificationMessage abandonNotification = notifyQueue.poll(1, TimeUnit.NANOSECONDS);
                if (abandonNotification != null) {
                    abandonNotificationCount.incrementAndGet();
                }
            }
        } catch (InterruptedException e) {
            // skip
        }
    }

    private final class SendThread extends Thread {

        private SendThread(String name){
            super(name);
        }

        public void run() {
            if (LOG.isDebugEnabled()) {
                LOG.debug(service.getFullName() + " send thread started.");
            }

            for (;;) {

                try {
                    if (!connected) {
                        waitForConnected(1000);
                    }

                    DragoonNotificationMessage notification = getNotifyQueue().poll(1, TimeUnit.SECONDS);

                    if (notification != null) {
                        getSession().sendNotificationMessage(notification);
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    LOG.error(service.getFullName() + " send message error", e);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(service.getFullName() + " send thread exited.");
            }

            if (stopLatch != null) {
                stopLatch.countDown();
            }
        }
    }

    protected Connect createConnectMessage() {
        return new Connect();
    }

    protected void handleConnectResp(DragoonSession session, ConnectResp response) {
    }

    protected void check() {
        if (connector != null) {
            if (getSession() == null) {
                lock.lock();
                try {
                    if (getSession() == null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(service.getFullName() + " connect to " + serverAddress + " ...");
                        }

                        connectCount.incrementAndGet();

                        DragoonSession newSession = connector.connect(serverAddress, getMessageHandler()).get();
                        setSession(newSession);

                        Connect connect = createConnectMessage();

                        Future<ResponseMessage> connectResponseFuture = getSession().sendMessage(connect);
                        handleConnectResp(getSession(), (ConnectResp) connectResponseFuture.get(30, TimeUnit.SECONDS));

                        if (LOG.isDebugEnabled()) {
                            LOG.debug(service.getFullName() + " connected to " + serverAddress + ".");
                        }

                        DragoonConnectorFilter.this.connected = true;
                        DragoonConnectorFilter.this.connectedSignal.signalAll();
                    }

                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    LOG.error(service.getFullName() + " connect error");
                } finally {
                    lock.unlock();
                }
            }
        } else {
            LOG.error(service.getFullName() + " connector is null");
        }
    }

    private class ConnectorThread extends Thread {

        public ConnectorThread(){
            super(service.getFullName() + " connector");
        }

        public void run() {
            for (;;) {
                if (stopped) {
                    break;
                }

                check();

                if (Thread.interrupted()) {
                    break;
                }

                try {
                    Thread.sleep(connectCheckDelayTimeMillis);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(service.getFullName() + " connector thread exited.");
            }

            if (stopLatch != null) {
                stopLatch.countDown();
            }
        }
    }

    public int getNotificationQueueSize() {
        return notifyQueue.size();
    }

    public int getNotificationQueueSizeThreshold() {
        return notifyQueueSizeThreshold;
    }

    public boolean closeSession() {
        if (!isConnected()) {
            return false;
        }

        DragoonSession session = this.session;

        if (session != null) {
            session.close();
            return true;
        }

        return false;
    }
}
