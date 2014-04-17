/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

public class ExceptionInfo {

    private String message;
    private String stackTrace;
    private String errorClass;

    public ExceptionInfo(Throwable error){
        super();
        this.message = error.getMessage();
        this.errorClass = error.getClass().getName();

        StringWriter buf = new StringWriter();
        error.printStackTrace(new PrintWriter(buf));
        stackTrace = buf.toString();
    }

    public ExceptionInfo(){
        super();
    }

    public ExceptionInfo(JSONObject json){
        super();

        this.errorClass = json.getString("CLASS");
        this.message = json.getString("MSG");
        this.stackTrace = json.getString("STACK_TRACE");
    }

    @JSONField(name = "MSG")
    public String getMessage() {
        return message;
    }

    @JSONField(name = "MSG")
    public void setMessage(String message) {
        this.message = message;
    }

    @JSONField(name = "STACK_TRACE")
    public String getStackTrace() {
        return stackTrace;
    }

    @JSONField(name = "STACK_TRACE")
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    @JSONField(name = "CLASS")
    public String getErrorClass() {
        return errorClass;
    }

    @JSONField(name = "CLASS")
    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }

}
