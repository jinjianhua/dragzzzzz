/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * 用于Monitor接收被动消息。
 * 
 * @author lijun.cailj 2011-10-27 下午02:30:52
 */

public class PassiveMonitorItemData extends DragoonNotificationMessage {

    private int                 sequence;
    private Map<String, Object> source;
    private Map<String, Object> data;
    private long                timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String toString() {
        return toJSONString();
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("TS", getTimestamp());
        body.put("S", getSource());
        body.put("D", getData());

        return constructMessage(getType(), getSequence(), body);
    }

    public String getType() {
        return "PassiveMonitorItemData";
    }
}
