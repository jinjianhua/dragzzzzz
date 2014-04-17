/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.trace.selector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.dragoon.patrol.web.PatrolWebUtils;

public class DefaultHttpTraceSelector implements TraceSelector {

    private List<SessionMatchRule> sessionRules = new CopyOnWriteArrayList<SessionMatchRule>();
    private List<CookieMatchRule>  cookieRules  = new CopyOnWriteArrayList<CookieMatchRule>();

    public boolean select() {
        HttpServletRequest request = PatrolWebUtils.getHttpServletRequest();
        HttpServletResponse response = PatrolWebUtils.getHttpServletResponse();

        return selelct(request, response);
    }

    public boolean selelct(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();

        for (SessionMatchRule rule : sessionRules) {
            if (rule.match(session)) {
                return true;
            }
        }

        for (CookieMatchRule rule : cookieRules) {
            if (rule.match(request.getCookies())) {
                return true;
            }
        }

        return false;
    }

}
