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
public class BrowserCommon {

    private String  appNumber;
    private String  pageId;      // 页面名称
    private String  ua;          // 浏览器useragent字段
    private String  url;         // 当前页面的url
    private Integer screenH;     // 屏幕高度（像素）

    private Integer screenW;     // 屏幕宽度（像素）
    private Integer colorDepth;  // 色彩深度（bit）
    private Integer visibleW;    // 可用宽度（像素）
    private Integer visibleH;    // 可用高度（像素）
    private String  navName;     // 浏览器名称

    private String  navEngine;
    private String  navVer;      // 浏览器版本
    private String  lang;        // 浏览器语言
    private String  osName;      // 操作系统名称
    private String  osVer;       // 操作系统版本

    private String  flashVer;    // Flash版本
    private String  javaEnabled; // Java支持情况
    private String  device;      // 移动设备类型
    private Integer loadTime;    // 页面加载时间（毫秒）
    private String  session;     // 会话id
    private Long    responseTime; // 网速测试的响应时间

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

    public Integer getScreenH() {
        return screenH;
    }

    public void setScreenH(Integer screenH) {
        this.screenH = screenH;
    }

    public Integer getScreenW() {
        return screenW;
    }

    public void setScreenW(Integer screenW) {
        this.screenW = screenW;
    }

    public Integer getColorDepth() {
        return colorDepth;
    }

    public void setColorDepth(Integer colorDepth) {
        this.colorDepth = colorDepth;
    }

    public Integer getVisibleW() {
        return visibleW;
    }

    public void setVisibleW(Integer visibleW) {
        this.visibleW = visibleW;
    }

    public Integer getVisibleH() {
        return visibleH;
    }

    public void setVisibleH(Integer visibleH) {
        this.visibleH = visibleH;
    }

    public String getNavName() {
        return navName;
    }

    public void setNavName(String navName) {
        this.navName = navName;
    }

    public String getNavEngine() {
        return navEngine;
    }

    public void setNavEngine(String navEngine) {
        this.navEngine = navEngine;
    }

    public String getNavVer() {
        return navVer;
    }

    public void setNavVer(String navVer) {
        this.navVer = navVer;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVer() {
        return osVer;
    }

    public void setOsVer(String osVer) {
        this.osVer = osVer;
    }

    public String getFlashVer() {
        return flashVer;
    }

    public void setFlashVer(String flashVer) {
        this.flashVer = flashVer;
    }

    public String getJavaEnabled() {
        return javaEnabled;
    }

    public void setJavaEnabled(String javaEnabled) {
        this.javaEnabled = javaEnabled;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Integer getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(Integer loadTime) {
        this.loadTime = loadTime;
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

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

}
