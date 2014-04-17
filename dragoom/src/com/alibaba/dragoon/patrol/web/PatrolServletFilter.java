/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.web;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.Profiler.EntryType;
import com.alibaba.dragoon.stat.WebSessionStatistic;
import com.alibaba.dragoon.stat.WebStatisticManager;
import com.alibaba.dragoon.stat.WebURIStatistic;
import com.alibaba.druid.stat.JdbcStatContext;
import com.alibaba.druid.stat.JdbcStatManager;

/**
 * @author shaojin.wensj
 */
public class PatrolServletFilter implements javax.servlet.Filter, PatrolServletFilterMBean {

    public static final String      REQUEST_ATTR_ID                = "patrol-web-request.id";

    public static final String      INIT_PARAM_NAME_ENABLE_SESSION = "enableS";

    private static final AtomicLong requestIdSeed                  = new AtomicLong(0L);

    private ObjectName              objectName;

    private ServletContext          context;

    private String                  userSessionAttributeName;

    private final List<String>      exclusions                     = new ArrayList<String>();

    private boolean                 sessionStatEnable              = true;

    public PatrolServletFilter(){

    }

    public void init(javax.servlet.FilterConfig config) throws ServletException {
        try {
            this.context = config.getServletContext();

            userSessionAttributeName = context.getInitParameter("userSessionAttribute");

            {
                String paramValue = config.getInitParameter("sessionStat");
                if (paramValue != null && paramValue.trim().length() != 0) {
                    paramValue = paramValue.trim();
                    if (paramValue.equalsIgnoreCase("true") || paramValue.equals("1")) {
                        sessionStatEnable = true;
                    }
                }
            }

            String paramObjectname = config.getInitParameter("objectName");
            if (paramObjectname == null) {
                paramObjectname = "com.alibaba.dragoon:type=PatrolServletFilter";
            }
            objectName = new ObjectName(paramObjectname);
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);

            String paramExclusions = config.getInitParameter("exclusions");
            if (paramExclusions != null) {
                String[] items = paramExclusions.split(",");
                for (String item : items) {
                    if (item != null && item.length() != 0) {
                        exclusions.add(item);
                    }
                }
            }

        } catch (Exception e) {
            this.context.log("init patrol filter error", e);
        }
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void destroy() {
        try {
            if (objectName != null) {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
            }
        } catch (Exception e) {
            this.context.log("destroy patrol filter error", e);
        }
    }

    public String getContextPath() {
        String contextPath = PatrolWebUtils.getContextPath(this.context);
        return contextPath;
    }

    public static Long getRequestId(HttpServletRequest request) {
        return (Long) request.getAttribute(REQUEST_ATTR_ID);
    }

    public boolean isExclusion(String uri) {
        for (String exclusion : this.exclusions) {
            if (uri.endsWith(exclusion)) {
                return true;
            }
        }

        return false;
    }

    public void createStatContext() {
        JdbcStatManager jdbcStatManager = JdbcStatManager.getInstance();
        JdbcStatContext jdbcStatContext = jdbcStatManager.createStatContext();
        jdbcStatManager.setStatContext(jdbcStatContext);
    }

    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    public void doFilter(ServletRequest request, ServletResponse response, javax.servlet.FilterChain chain)
                                                                                                           throws IOException,
                                                                                                           ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        PatrolHttpServletResponseWrapper responseWrapper = new PatrolHttpServletResponseWrapper(httpResponse);

        final String requestURI = getRequestURI(httpRequest);

        if (isExclusion(requestURI)) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        PatrolWebUtils.setHttpServletRequest(httpRequest);
        PatrolWebUtils.setHttpServletResponse(httpResponse);

        createStatContext();

        request.setAttribute(REQUEST_ATTR_ID, new Long(requestIdSeed.incrementAndGet()));

        // -------------
        WebSessionStatistic sessionStat = null;
        if (sessionStatEnable) {
            sessionStat = PatrolServletSessionListener.getCounter(httpRequest.getSession(false));
            PatrolWebUtils.setSessionStatistic(sessionStat);
        }

        final WebURIStatistic uriStat = WebStatisticManager.getInstance().getURIStat(requestURI);
        // -------------

        doFilterBefore(httpRequest, httpResponse, sessionStat, uriStat);

        long startNano = System.nanoTime();
        uriStat.beforeInvoke(httpRequest);

        int status = 0;
        String errorURI = null;
        WebURIStatistic errorUriStat = null;

        Profiler.Entry profilerEntry = Profiler.enter(requestURI, EntryType.URL);
        try {
            chain.doFilter(httpRequest, responseWrapper);

            status = responseWrapper.getStatus();

            if (status == HttpServletResponse.SC_NOT_FOUND) {
                errorURI = "error_" + status;
                WebStatisticManager.getInstance().removeURIStat(requestURI); // 防攻击处理
                errorUriStat = WebStatisticManager.getInstance().getURIStat(errorURI);
                errorUriStat.beforeInvoke();
            }

            if (errorUriStat == null) {
                uriStat.afterInvoke(System.nanoTime() - startNano);
            } else {
                errorUriStat.afterInvoke(System.nanoTime() - startNano);
            }
        } catch (ServletException ex) {
            error(ex, startNano, httpRequest, httpResponse, sessionStat, uriStat);
            throw ex;
        } catch (IOException ex) {
            error(ex, startNano, httpRequest, httpResponse, sessionStat, uriStat);
            throw ex;
        } catch (RuntimeException ex) {
            error(ex, startNano, httpRequest, httpResponse, sessionStat, uriStat);
            throw ex;
        } catch (Error ex) {
            error(ex, startNano, httpRequest, httpResponse, sessionStat, uriStat);
            throw ex;
        } finally {
            PatrolWebUtils.setHttpServletRequest(null);
            PatrolWebUtils.setHttpServletResponse(null);
            PatrolWebUtils.setSessionStatistic(null);

            Profiler.release();
            if (errorUriStat == null) {
                uriStat.recordProfilerInfo(profilerEntry);
            }

            doFilterAfter(httpRequest, httpResponse, sessionStat, uriStat);
        }
    }

    public void error(Throwable ex, long startNano, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                      WebSessionStatistic sessionStat, WebURIStatistic uriStat) {

        String referer = httpRequest.getHeader("referer");
        String user = null;
        StringBuffer url = httpRequest.getRequestURL();
        if (userSessionAttributeName != null && sessionStatEnable) {
            HttpSession session = httpRequest.getSession(false);

            if (session != null) {
                Object attrVal = session.getAttribute(this.userSessionAttributeName);
                if (attrVal != null) {
                    user = attrVal.toString();
                }
            }
        }

        if (uriStat != null) {
            uriStat.afterInvoke(System.nanoTime() - startNano);
            uriStat.error(ex, user, referer, url == null ? null : url.toString());
        }
    }

    public void doFilterBefore(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                               WebSessionStatistic sessionStat, WebURIStatistic uriStat) {
        if (sessionStat != null && sessionStat.getRemoteAddress() == null) {
            sessionStat.setRemoteAddress(PatrolWebUtils.getRemoteAddr(httpRequest));
        }
    }

    public void doFilterAfter(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                              WebSessionStatistic sessionStat, WebURIStatistic uriStat) {
        if (sessionStat != null) {
            sessionStat.incrementAndGetAccessedPageCount();
        }
    }

    public String getUserSessionAttributeName() {
        return this.userSessionAttributeName;
    }

    public void setUserSessionAttributeName(String userSessionAttributeName) {
        this.userSessionAttributeName = userSessionAttributeName;
    }

}
