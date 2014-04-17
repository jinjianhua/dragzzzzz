/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import com.alibaba.fastjson.JSONObject;

public class Invoke extends ObjectRequest {

    private String       operatorName;
    private List<Object> parameters;
    private List<String> signature;

    public Invoke(){
        parameters = new ArrayList<Object>();
        signature = new ArrayList<String>();
    }

    public Invoke(ObjectName objectName, String operatorName){
        super(objectName);
        parameters = new ArrayList<Object>();
        signature = new ArrayList<String>();
        this.operatorName = operatorName;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public List<String> getSignature() {
        return signature;
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("OBJ_NAME", getObjectName().toString());
        body.put("OP_NAME", getOperatorName());
        body.put("PARAMS", getParameters());
        body.put("SIGNATURE", getSignature());

        return constructMessage(getType(), getFrameId(), body);
    }

    public String getType() {
        return "Invoke";
    }
}
