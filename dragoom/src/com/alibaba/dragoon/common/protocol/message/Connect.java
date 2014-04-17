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

public class Connect extends RequestMessage {

    private Map<String, Object> properties = new HashMap<String, Object>();

    public Connect(){
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String toString() {
        return toJSONString();
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject(getProperties());
        return constructMessage(getType(), getFrameId(), body);
    }
    
    public String getType() {
        return "Connect";
    }
}
