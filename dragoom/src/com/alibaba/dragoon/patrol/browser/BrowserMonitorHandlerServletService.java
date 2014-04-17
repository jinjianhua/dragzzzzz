/**
 * Project: dragoon-common-browser File Created at 2011-8-2 $Id$ Copyright 2008 Alibaba.com Corporation Limited. All
 * rights reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.browser;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.dragoon.stat.browser.BrowserStatManager;

/**
 * @author lijun.cailj
 */
public class BrowserMonitorHandlerServletService {

    private BrowserStatManager manager = BrowserStatManager.getInstance();

    public BrowserStatManager getManager() {
        return manager;
    }

    public void saveCommon(HttpServletRequest request) {
        if (!manager.isEnable()) {
            return;
        }
        
        BrowserCommon common = new BrowserCommon();
        common.setAppNumber(request.getParameter("appNum"));
        common.setPageId(request.getParameter("pageId"));
        common.setUa(request.getParameter("ua"));
        common.setUrl(request.getParameter("url"));
        common.setScreenH(isEmpty(request.getParameter("screenH"), 0));

        common.setScreenW(isEmpty(request.getParameter("screenW"), 0));
        common.setColorDepth(isEmpty(request.getParameter("colorDepth"), 0));
        common.setVisibleW(isEmpty(request.getParameter("visibleW"), 0));
        common.setVisibleH(isEmpty(request.getParameter("visibleH"), 0));
        common.setNavName(request.getParameter("navName"));

        common.setNavEngine(request.getParameter("navEngine"));
        common.setNavVer(request.getParameter("navVer"));
        common.setLang(request.getParameter("lang"));
        common.setOsName(request.getParameter("osName"));
        common.setOsVer(request.getParameter("osVer"));

        common.setFlashVer(request.getParameter("flashVer"));
        common.setJavaEnabled(request.getParameter("javaEnabled"));
        common.setDevice(request.getParameter("device"));
        common.setLoadTime(isEmpty(request.getParameter("loadTime"), 0));
        common.setSession(request.getParameter("session"));
        String respTime = request.getParameter("responseTime");
        try {
            if (respTime != null && !respTime.trim().equals("")) {
                common.setResponseTime(Long.parseLong(respTime.trim()));
            }
        } catch (Exception e) {

        }
        common.setPageSeed(request.getParameter("pageSeed"));

        manager.addCommon(common);
    }

    public void saveError(HttpServletRequest request) {
        if (!manager.isEnable()) {
            return;
        }
        
        BrowserError error = new BrowserError();

        error.setAppNumber(request.getParameter("appNum"));
        error.setPageId(request.getParameter("pageId"));
        error.setUa(request.getParameter("ua"));
        error.setUrl(request.getParameter("url"));
        error.setErrName(request.getParameter("errName"));

        error.setErrFileName(request.getParameter("errFileName"));
        error.setErrUrl(request.getParameter("errUrl"));
        error.setErrLineNum(isEmpty(request.getParameter("errLineNum"), 0));
        error.setErrMsg(request.getParameter("errMsg"));
        error.setErrStack(request.getParameter("errStack"));

        error.setErrOtherData(request.getParameter("errOtherData"));
        error.setSession(request.getParameter("session"));
        error.setPageSeed(request.getParameter("pageSeed"));

        manager.addError(error);
    }

    public void saveElement(HttpServletRequest request) {
        if (!manager.isEnable()) {
            return;
        }
        
        BrowserElement element = new BrowserElement();

        element.setAppNumber(request.getParameter("appNum"));
        element.setPageId(request.getParameter("pageId"));
        element.setUa(request.getParameter("ua"));
        element.setUrl(request.getParameter("url"));
        element.setCustomData(request.getParameter("customData"));

        element.setFuncName(request.getParameter("funcName"));
        element.setEnabledTime(isEmpty(request.getParameter("enabledTime"), 0L));
        element.setSession(request.getParameter("session"));
        element.setPageSeed(request.getParameter("pageSeed"));

        manager.addElement(element);
    }

    private Integer isEmpty(String para, Integer defaultValue) {
        if (para == null||para.trim().equals("undefined")) {
            return defaultValue;
        }
        int rs = 0; 
        try{
            rs = Integer.parseInt(para);
        }catch(Exception e){
           
        }
        
        return rs;
    }

    private Long isEmpty(String para, Long defaultValue) {
        if (para == null) {
            return defaultValue;
        }

        return Long.parseLong(para);
    }
}
