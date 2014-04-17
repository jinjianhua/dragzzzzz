/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import com.alibaba.fastjson.JSONObject;

public class SetAttribute extends ObjectRequest {

    private String attributeName;
    private Object attributeValue;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Object getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(Object attributeValue) {
        this.attributeValue = attributeValue;
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();
        body.put("OBJ_NAME", getObjectName().toString());
        body.put("ATTRS", getAttributeName());

        return constructMessage(getType(), getFrameId(), body);
    }

    public String getType() {
        return "SetAttribute";
    }
}
