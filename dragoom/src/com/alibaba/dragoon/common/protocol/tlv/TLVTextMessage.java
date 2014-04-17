/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.tlv;

import java.io.UnsupportedEncodingException;

public class TLVTextMessage implements TLVMessage {

    private final String  message;
    private final short   type;
    private final String  charset;
    private final transient int sourceLength;

    public TLVTextMessage(short type, String message, String charset){
        this(type, message, charset, 0);
    }

    public int getSourceLength() {
        return sourceLength;
    }

    public TLVTextMessage(short type, String message, String charset, int sourceLength){
        super();
        this.message = message;
        this.type = type;
        this.charset = charset;
        this.sourceLength = sourceLength;
    }

    public String getCharset() {
        return charset;
    }

    public String getMessage() {
        return message;
    }

    public short getType() {
        return type;
    }

    public byte[] getValue() {
        try {
            return message.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String toString() {
        return message;
    }

}
