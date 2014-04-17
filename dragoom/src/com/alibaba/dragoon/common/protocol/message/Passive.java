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

public class Passive extends DragoonNotificationMessage {

    private int                 sequence;
    private String              appNumber;
    private String              instNumber;
    private String              miName;
    private Date                timestamp;
    private Map<String, Object> values = new HashMap<String, Object>();

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public String getAppNumber() {
        return appNumber;
    }

    public void setAppNumber(String appNumber) {
        this.appNumber = appNumber;
    }

    public String getInstNumber() {
        return instNumber;
    }

    public void setInstNumber(String instNumber) {
        this.instNumber = instNumber;
    }

    /**
     * @return the miName
     */
    public String getMiName() {
        return miName;
    }

    /**
     * @param miName the miName to set
     */
    public void setMiName(String miName) {
        this.miName = miName;
    }

    
    /**
     * @return the values
     */
    public Map<String, Object> getValues() {
        return values;
    }

    
    /**
     * @param values the values to set
     */
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimestamp(Date timeStamp) {
        this.timestamp = timeStamp;
    }

    public String toString() {
        return toJSONString();
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("APP_NUM", getAppNumber());
        body.put("INST_NUM", getInstNumber());
        body.put("MI_NAME", getMiName());
        body.put("TS", getTimestamp().getTime());
        body.put("VAL", getValues());

        return constructMessage(getType(), getSequence(), body);
    }
    
    public String getType() {
        return "Passive";
    }
}
