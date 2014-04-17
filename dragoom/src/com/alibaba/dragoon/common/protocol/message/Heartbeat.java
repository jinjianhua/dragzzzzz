/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class Heartbeat extends DragoonNotificationMessage {

    private int                 sequence;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Date                date       = new Date();

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Heartbeat(){

    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String toString() {
        return toJSONString();
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        if (getAttributes() != null) {
            if (getDate() != null) {
                body.put("TS", getDate().getTime());
            }
            body.putAll(getAttributes());
        }

        return constructMessage(getType(), getSequence(), body);
    }
    
    public String getType() {
        return "Heartbeat";
    }
}
