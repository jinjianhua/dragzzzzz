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
public class BrowserElement {

    private String appNumber;
    private String pageId;     // 页面名称
    private String ua;         // 浏览器useragent字段
    private String url;        // 当前页面的url
    private String customData; // 用户数据

    private String funcName;   // 功能名称
    private Long   enabledTime; // 从页面开始渲染到功能可用的时间（毫秒）
    private String session;    // 会话id
    private String pageSeed;   //

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

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public Long getEnabledTime() {
        return enabledTime;
    }

    public void setEnabledTime(Long enabledTime) {
        this.enabledTime = enabledTime;
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
