/**
 * Project: dragoon-monitor-browser
 * 
 * File Created at 2011-7-17
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Comment of BrowserMonitorHandlerServlet
 * 
 * @author lijun.cailj
 */
public class BrowserMonitorHandlerServlet extends HttpServlet {

    private final static Log                          log              = LogFactory.getLog(BrowserMonitorHandlerServlet.class);

    private static final long                         serialVersionUID = -5661571257090639711L;

    private final BrowserMonitorHandlerServletService service          = new BrowserMonitorHandlerServletService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String method = request.getParameter("method");
            if (method == null) {
                log.error("browser method is null.");
            }

            String appNumber = request.getParameter("appNum");
            if (appNumber == null) {
                log.error(" browser appNumber is null.");
            }

            if ("common".equalsIgnoreCase(method)) {
                service.saveCommon(request);

            } else if ("error".equalsIgnoreCase(method)) {
                service.saveError(request);

            } else if ("element".equalsIgnoreCase(method)) {
                service.saveElement(request);

            } else {
                throw new IllegalArgumentException(" unsupported browser method :" + method);
            }

            response.getWriter().write("Successfully!");
            response.getWriter().flush();
        } finally {
            if (response.getWriter() != null) {
                response.getWriter().close();
            }
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
