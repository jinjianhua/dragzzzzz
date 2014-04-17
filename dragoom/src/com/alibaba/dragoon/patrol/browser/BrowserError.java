/**
 * Project: dragoon-common
 * 
 * File Created at 2011-6-13
 * $Id$
 * 
 * Copyright 2008 Alibaba.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.browser;

/**
 * @author lijun.cailj
 */
public class BrowserError {

    private String  appNumber;
    private String  pageId;      // 页面名称
    private String  ua;          // 浏览器useragent字段
    private String  url;         // 当前页面的url
    private String  errName;     // 错误名称

    private String  errFileName; // 发生错误的js文件名
    private String  errUrl;      // 发生错误的url
    private Integer errLineNum;  // 发生错误的行号
    private String  errMsg;      // 错误消息
    private String  errStack;    // 错误堆栈

    private String  errOtherData; // 其他错误消息
    private String  session;     // 会话id
    private String  pageSeed;

    public String getAppNumber() {
        return appNumber;
    }

    public void setAppNumber(String appNumber) {
        this.appNumber = appNumber;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getErrName() {
        return errName;
    }

    public void setErrName(String errName) {
        this.errName = errName;
    }

    public String getErrFileName() {
        return errFileName;
    }

    public void setErrFileName(String errFileName) {
        this.errFileName = errFileName;
    }

    public String getErrUrl() {
        return errUrl;
    }

    public void setErrUrl(String errUrl) {
        this.errUrl = errUrl;
    }

    public Integer getErrLineNum() {
        return errLineNum;
    }

    public void setErrLineNum(Integer errLineNum) {
        this.errLineNum = errLineNum;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrStack() {
        return errStack;
    }

    public void setErrStack(String errStack) {
        this.errStack = errStack;
    }

    public String getErrOtherData() {
        return errOtherData;
    }

    public void setErrOtherData(String errOtherData) {
        this.errOtherData = errOtherData;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getPageSeed() {
        return pageSeed;
    }

    public void setPageSeed(String pageSeed) {
        this.pageSeed = pageSeed;
    }

}
