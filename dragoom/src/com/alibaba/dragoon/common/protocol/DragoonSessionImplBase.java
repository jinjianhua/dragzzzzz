/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import java.net.SocketAddress;

public abstract class DragoonSessionImplBase implements DragoonSessionImpl {

    public abstract SocketAddress getLocalAddress();

    public abstract SocketAddress getRemoteAddress();

    public String getSessionName() {
        if (getLocalAddress() == null) {
            return " -> " + getRemoteAddress();
        } else if (getRemoteAddress() == null) {
            return getLocalAddress() + " -> ";
        } else {
            return getLocalAddress() + " -> " + getRemoteAddress();
        }

    }

    public String toString() {
        return getSessionName();
    }
}
