/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class ConnectResp extends ResponseMessage {

    private Map<String, Object> properties = new HashMap<String, Object>();

    public ConnectResp(){

    }

    public ConnectResp(Exception error){
        super(error);
    }

    public ConnectResp(ExceptionInfo error){
        super(error);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("VAL", getProperties());
        body.put("ERROR", getError());

        return constructMessage(getType(), getFrameId(), body);
    }
    
    @Override
    public String toString() {
        return this.toJSONString();
    }
    
    public String getType() {
        return "ConnectResp";
    }
}
