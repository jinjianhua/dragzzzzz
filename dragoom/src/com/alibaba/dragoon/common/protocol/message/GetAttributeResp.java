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

/**
 * @author JOBS
 */
public class GetAttributeResp extends ResponseMessage {

    private Map<String, Object> values;
    private long                timestamp;

    public GetAttributeResp(){
        this(System.currentTimeMillis());
    }

    public GetAttributeResp(long timestamp){
        values = new HashMap<String, Object>();
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public GetAttributeResp(Exception error){
        super(error);
    }

    public GetAttributeResp(ExceptionInfo error){
        super(error);
    }

    public GetAttributeResp(Map<String, Object> values){
        super();
        this.values = values;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public String toString() {
        return toJSONString();
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("VAL", toJSON(getValues()));
        body.put("ERROR", getError());
        body.put("TS", getTimestamp());

        return constructMessage(getType(), getFrameId(), body);
    }
    
    public String getType() {
        return "GetAttributeResp";
    }
}
