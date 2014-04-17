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

public class Error extends DragoonNotificationMessage {

    private int                 sequence;
    private String              type;
    private String              code;
    private String              message;
    private String              stackTrace;
    private Map<String, Object> attributes = new HashMap<String, Object>();

    public Error(){
    }

    public Error(JSONObject obj){
        fromJSONObject(obj);
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("sequence", sequence);
        obj.put("type", type);
        obj.put("code", code);
        obj.put("message", message);
        obj.put("stackTrace", stackTrace);
        obj.put("s", attributes);

        return obj;
    }

    public void fromJSONObject(JSONObject obj) {

        this.sequence = obj.getIntValue("sequence");
        this.type = obj.getString("type");
        this.code = obj.getString("code");
        this.message = obj.getString("message");
        this.stackTrace = obj.getString("stackTrace");

    }

    @Override
    public String toString() {
        return this.toJSONString();
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("CODE", getCode());
        body.put("TYPE", getType());
        body.put("MSG", getMessage());
        body.put("STACK_TRACE", getStackTrace());
        body.put("S", getAttributes());

        return constructMessage("Error", getSequence(), body);
    }

    /**
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}
