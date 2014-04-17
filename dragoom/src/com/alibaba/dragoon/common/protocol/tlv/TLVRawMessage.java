/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.tlv;

public class TLVRawMessage implements TLVMessage {

    private short  type;
    private int    length;
    private byte[] value;

    public TLVRawMessage(short type, int length){
        this(type, new byte[length]);
    }

    public TLVRawMessage(short type, byte[] value){
        super();
        this.type = type;
        this.length = value.length;
        this.value = value;
    }

    public short getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public byte[] getValue() {
        return value;
    }

    public int getFullLength() {
        return length + 6;
    }
}
