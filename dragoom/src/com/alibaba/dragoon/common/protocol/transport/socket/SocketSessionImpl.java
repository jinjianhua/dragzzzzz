/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.transport.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.protocol.DragoonSession;
import com.alibaba.dragoon.common.protocol.DragoonSessionImpl;
import com.alibaba.dragoon.common.protocol.DragoonSessionImplBase;
import com.alibaba.dragoon.common.protocol.message.DragoonMessage;
import com.alibaba.dragoon.common.protocol.tlv.TLVMessage;
import com.alibaba.dragoon.common.protocol.transport.DragoonMessageCodec;

public class SocketSessionImpl extends DragoonSessionImplBase implements DragoonSessionImpl {

    private final static Log    LOG   = LogFactory.getLog(SocketSessionImpl.class);

    private Socket              socket;

    private DataInputStream     dataInput;

    private DataOutputStream    writer;

    private DragoonMessageCodec codec = new DragoonMessageCodec();

    private Thread              readThread;

    private Lock                lock  = new ReentrantLock();

    private Condition           stopSignal;

    protected volatile State    state;

    private DragoonSession      session;

    private final AtomicLong    receivedBytes;
    private final AtomicLong    receivedMessages;
    private final AtomicLong    sentBytes;
    private final AtomicLong    sentMessages;

    public SocketSessionImpl(Socket socket, AtomicLong receivedBytes, AtomicLong receivedMessages,
                             AtomicLong sentBytes, AtomicLong sentMessages){
        super();
        this.socket = socket;
        state = State.Established;

        this.receivedBytes = receivedBytes;
        this.receivedMessages = receivedMessages;
        this.sentBytes = sentBytes;
        this.sentMessages = sentMessages;

        try {
            dataInput = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    private DragoonMessage readMessage() {
        try {
            for (;;) {
                int type = dataInput.readShort();

                int length = dataInput.readInt();
                byte[] bytes = new byte[length];
                dataInput.readFully(bytes);

                receivedBytes.addAndGet(length + 6);
                receivedMessages.incrementAndGet();

                if (type == TLVMessage.TEXT_UTF_8) {
                    String text = new String(bytes, "UTF-8");

                    return codec.decode(text);
                }

                throw new IOException("protocol error, not support type : " + type);

                // codec.encode(message)
                // TODO
            }
        } catch (IOException ex) {
            session.setLastError(ex);

            if (socket.isClosed()) {
                if (state == State.Established) {
                    session.close();
                } else if (state == State.Closing) {
                    // skip
                } else {
                    LOG.error(SocketSessionImpl.this.getSessionName() + " read message error", ex);
                    session.close();
                }
            } else {
                LOG.error(this.getSessionName() + " read message error", ex);
                session.close();
            }

        }

        if (state == State.Established) {
            session.close();
        }

        // TODO
        return null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return socket.getLocalSocketAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return socket.getRemoteSocketAddress();
    }

    public void close() {
        lock.lock();
        try {
            state = State.Closing;

            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOG.error("close error", e);
                }
            }

            if (readThread != null && readThread.isAlive()) {
                stopSignal = lock.newCondition();

                readThread.interrupt();

                try {
                    stopSignal.await(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // skip
                }

                readThread = null;

                stopSignal = null;
            }

            state = State.Closed;
        } finally {
            lock.unlock();
        }

    }

    public void sendMessageDirect(DragoonMessage message) throws IOException {
        lock.lock();
        try {
            String text = codec.encode(message);

            byte[] bytes = text.getBytes("UTF-8");

            writer.writeShort(TLVMessage.TEXT_UTF_8);
            writer.writeInt(bytes.length);
            writer.write(bytes);
            writer.flush();

            sentBytes.addAndGet(bytes.length + 6);
            sentMessages.incrementAndGet();

            if (LOG.isDebugEnabled()) {
                LOG.debug(getSessionName() + " MSG SENT : " + TLVMessage.TEXT_UTF_8 + " " + bytes.length + " " + text);
            }
        } catch (IOException ex) {
            session.close();
            throw ex;
        } finally {
            lock.unlock();
        }
    }

    public void init(DragoonSession session) {
        this.session = session;
        readThread = new Thread(getSessionName() + " session read") {

            public void run() {
                try {
                    for (;;) {
                        DragoonMessage message = readMessage();
                        if (message != null) {
                            logReceiveMessage(message);
                            SocketSessionImpl.this.session.receiveMessage(message);
                        }

                        if (state != DragoonSessionImpl.State.Established) {
                            break;
                        }

                        if (Thread.interrupted()) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }

                lock.lock();
                try {
                    if (stopSignal != null) {
                        stopSignal.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            }
        };
        readThread.start();
    }

    private void logReceiveMessage(DragoonMessage message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(getSessionName() + " MSG RECEVIE : " + message);
        }
    }
}
