/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import com.alibaba.fastjson.JSONObject;

/**
 * @author JOBS
 */
public class InvokeResp extends ResponseMessage {

    private Object returnValue;

    public InvokeResp(Exception error){
        super(error);
    }

    public InvokeResp(ExceptionInfo error){
        super(error);
    }

    public InvokeResp(){
    }

    public InvokeResp(Object returnValue){
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("VAL", getReturnValue());
        body.put("ERROR", getError());

        return constructMessage(getType(), getFrameId(), body);
    }

    public String getType() {
        return "InvokeResp";
    }
}
