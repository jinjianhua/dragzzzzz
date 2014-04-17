/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.tlv;

public interface TLVMessage {

    public final static short RAW_BINARY = 0;
    public final static short TEXT_UTF_8 = 1;

    short getType();

    byte[] getValue();
}
