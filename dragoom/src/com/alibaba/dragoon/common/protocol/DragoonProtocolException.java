/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import com.alibaba.dragoon.common.protocol.message.ExceptionInfo;

public class DragoonProtocolException extends Exception {

    private static final long serialVersionUID = -2717500356544168493L;

    private ExceptionInfo     exceptionInfo;

    public DragoonProtocolException(){
        super();
    }

    public DragoonProtocolException(ExceptionInfo exceptionInfo){
        this((String) exceptionInfo.getMessage());
    }

    public DragoonProtocolException(String message){
        super(message);
    }

    public DragoonProtocolException(String message, Throwable cause){
        super(message, cause);
    }

    public ExceptionInfo getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

}
