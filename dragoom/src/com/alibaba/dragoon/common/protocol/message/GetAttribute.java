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

public class GetAttribute extends ObjectRequest {

    private List<String> attributeNames;

    private List<String> options = new ArrayList<String>();

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public GetAttribute(){
        attributeNames = new ArrayList<String>();
    }

    public GetAttribute(ObjectName objectName, String... attributeNames){
        this();
        this.setObjectName(objectName);

        for (String name : attributeNames) {
            this.attributeNames.add(name);
        }
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public String toString() {
        return toJSONString();
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("OBJ_NAME", getObjectName().toString());
        body.put("ATTRS", getAttributeNames());
        body.put("OPTS", getOptions());

        return constructMessage(getType(), getFrameId(), body);
    }
    
    public String getType() {
        return "GetAttribute";
    }
}
