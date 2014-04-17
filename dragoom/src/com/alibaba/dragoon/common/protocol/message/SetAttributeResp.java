/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import com.alibaba.fastjson.JSONObject;

public class SetAttributeResp extends ResponseMessage {

    public SetAttributeResp(Exception error){
        super(error);
    }

    public SetAttributeResp(ExceptionInfo error){
        super(error);
    }

    public SetAttributeResp(){
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();

        return constructMessage(getType(), getFrameId(), body);
    }

    public String getType() {
        return "SetAttributeResp";
    }
}
