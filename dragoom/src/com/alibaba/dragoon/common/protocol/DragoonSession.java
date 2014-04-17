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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.protocol.message.DragoonMessage;
import com.alibaba.dragoon.common.protocol.message.DragoonNotificationMessage;
import com.alibaba.dragoon.common.protocol.message.RequestMessage;
import com.alibaba.dragoon.common.protocol.message.ResponseMessage;

public class DragoonSession {

    private final static Log                                      LOG                  = LogFactory.getLog(DragoonSession.class);

    protected AtomicInteger                                       sentMessageCount     = new AtomicInteger();
    protected AtomicInteger                                       receivedMessageCount = new AtomicInteger();
    protected Date                                                lastReceivedMessageDate;

    private final int                                             INIT_SEQ             = 1;
    private final int                                             MAX_SEQ              = 2000000000;
    private AtomicInteger                                         sequenceSeed         = new AtomicInteger(INIT_SEQ);

    private DragoonSessionConfig                                  config;

    private DragoonSessionImpl                                    impl;

    protected ConcurrentMap<Integer, FutureTask<ResponseMessage>> futureMap            = new ConcurrentHashMap<Integer, FutureTask<ResponseMessage>>();
    protected ConcurrentMap<Integer, ResponseMessageTask>         taskMap              = new ConcurrentHashMap<Integer, ResponseMessageTask>();

    private final ConcurrentMap<String, Object>                   attributes           = new ConcurrentHashMap<String, Object>();

    private Date                                                  createDate           = new Date();

    private Exception                                             lastError            = null;

    private final long                                            id;

    public DragoonSession(long id, DragoonSessionConfig config, DragoonSessionImpl impl){
        this.id = id;
        this.config = config;
        this.impl = impl;
    }

    public long getId() {
        return id;
    }

    public int getSentMessageCount() {
        return sentMessageCount.get();
    }

    public int getReceivedMessageCount() {
        return receivedMessageCount.get();
    }

    public Date getLastReceivedMessageDate() {
        return lastReceivedMessageDate;
    }

    public Date getCreatedDate() {
        return createDate;
    }

    public DragoonSessionImpl getImpl() {
        return impl;
    }

    public DragoonSessionConfig getConfig() {
        return config;
    }

    public void close() {
        for (Map.Entry<Integer, ResponseMessageTask> entry : taskMap.entrySet()) {
            if (lastError != null) {
                entry.getValue().setError(lastError);
            }

            FutureTask<ResponseMessage> future = futureMap.get(entry.getKey());
            future.run();
        }

        new DragoonIoFilterChainImpl(config).sessionClosed(this);
    }

    public Exception getLastError() {
        return lastError;
    }

    public void setLastError(Exception lastError) {
        this.lastError = lastError;
    }

    public Future<ResponseMessage> sendMessage(RequestMessage message) {
        int frameId = nextSequence();
        message.setFrameId(frameId);

        ResponseMessageTask task = new ResponseMessageTask();
        taskMap.put(frameId, task);

        FutureTask<ResponseMessage> future = new FutureTask<ResponseMessage>(task);
        futureMap.put(frameId, future);

        try {
            sentMessageCount.incrementAndGet();
            impl.sendMessageDirect(message);
        } catch (IOException e) {
            task.setError(e);
            future.run();
        }

        return future;
    }

    protected int nextSequence() {
        int seq = sequenceSeed.incrementAndGet();

        while (seq >= MAX_SEQ) {
            sequenceSeed.compareAndSet(INIT_SEQ, seq);
            seq = sequenceSeed.incrementAndGet();
        }
        // 2147483647
        // 2000000000
        // if (seq > 214)

        return seq;
    }

    public class ResponseMessageTask implements Callable<ResponseMessage> {

        private ResponseMessage response;
        private Exception       error;

        public Exception getError() {
            return error;
        }

        public void setError(Exception error) {
            this.error = error;
        }

        public ResponseMessage call() throws Exception {
            if (error != null) {
                throw error;
            }

            return response;
        }

        public ResponseMessage getResponse() {
            return response;
        }

        public void setResponse(ResponseMessage response) {
            this.response = response;
        }
    }

    public void receiveMessage(DragoonMessage message) {
        try {
            receiveMessageIntenal(message);
        } catch (Throwable e) {
            String text = message == null ? "" : message.toString();
            if (text.length() > 1000 * 10) {
                text = text.substring(0, 1000);
            }
            LOG.error("receive message error, @session:" + toString() + ", @message:" + text, e);
        }
    }

    private void receiveMessageIntenal(DragoonMessage message) throws IOException {
        receivedMessageCount.incrementAndGet();
        lastReceivedMessageDate = new Date();

        if (LOG.isDebugEnabled()) {
            LOG.debug(this.toString() + " MSG RECEIVED : " + message);
        }

        if (message instanceof ResponseMessage) {
            handleResponse((ResponseMessage) message);

            return;
        }

        if (message instanceof RequestMessage) {
            RequestMessage request = (RequestMessage) message;
            ResponseMessage response = getConfig().getMessageHandler().handle(this, request);
            if (response != null) {
                response.setFrameId(request.getFrameId());

                impl.sendMessageDirect(response);
                sentMessageCount.incrementAndGet();
            }

            return;
        }

        if (message instanceof DragoonNotificationMessage) {
            getConfig().getMessageHandler().handle(this, (DragoonNotificationMessage) message);

            return;
        }

        throw new UnsupportedOperationException();
    }

    private void handleResponse(ResponseMessage response) {
        int frameId = response.getFrameId();

        ResponseMessageTask task = taskMap.remove(frameId);

        if (task == null) {
            LOG.warn("error frame " + frameId);
            return;
        }

        if (response.getError() != null) {
            // TODO SET error
            task.setError(new DragoonProtocolException(response.getError()));
        } else {
            task.setResponse(response);
        }

        FutureTask<ResponseMessage> future = futureMap.remove(frameId);

        if (future == null) {
            LOG.warn("error frame " + frameId);
            return;
        }

        future.run();
    }

    public void sendNotificationMessage(DragoonNotificationMessage message) {
        try {
            message.setSequence(nextSequence());
            impl.sendMessageDirect(message);
            sentMessageCount.incrementAndGet();
        } catch (IOException e) {
            LOG.error(this.toString() + " sendNotificationMessage error", e);
        }
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public SocketAddress getLocalAddress() {
        return impl.getLocalAddress();
    }

    public SocketAddress getRemoteAddress() {
        return impl.getRemoteAddress();
    }

    public String toString() {
        return impl.toString();
    }
}
