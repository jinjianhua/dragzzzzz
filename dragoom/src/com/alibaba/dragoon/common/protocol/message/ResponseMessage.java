/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

public abstract class ResponseMessage extends DragoonAbstractMessage {

    private ExceptionInfo errorInfo;

    private int           frameId;
    


    public ResponseMessage(){

    }

    public ResponseMessage(ExceptionInfo errorInfo){
        super();
        this.errorInfo = errorInfo;
    }

    public ResponseMessage(Exception error){
        super();
        this.errorInfo = new ExceptionInfo(error);
    }
    
    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(int frameId) {
        this.frameId = frameId;
    }

    public ExceptionInfo getError() {
        return errorInfo;
    }

    public void setError(ExceptionInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

}
