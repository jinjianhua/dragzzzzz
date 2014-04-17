/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.webx2;

import java.util.Iterator;

import com.alibaba.common.lang.StringUtil;
import com.alibaba.dragoon.patrol.web.PatrolWebUtils;
import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.WebStatisticManager;
import com.alibaba.dragoon.stat.WebURIStatistic;
import com.alibaba.service.pipeline.PipelineException;
import com.alibaba.service.pipeline.PipelineInitializationException;
import com.alibaba.service.pipeline.ValveForward;
import com.alibaba.service.rundata.parser.ParameterParser;
import com.alibaba.turbine.pipeline.AbstractValve;
import com.alibaba.turbine.pipeline.PerformActionValve;
import com.alibaba.turbine.service.rundata.RunData;

public class URIStatValve extends AbstractValve {

    private boolean             enableProfile         = true;
    private static final String ACTION_SETUP          = "setup";
    private static final String ACTION_CLEANUP        = "cleanup";
    private static final String ERROR_VM              = "error.vm";
    private static final String ESITE_VM              = "/esite.htm";
    private static final String DEFAULT_EVENT_PATTERN = "eventSubmitDo";
    public static final String  COLUMN_FLAG           = "__column__";

    private static final String SPLIT_CHAR            = "?";
    private String              action;

    public URIStatValve(){

    }

    public void init() throws PipelineInitializationException {
        if (action == null) {
            action = ACTION_SETUP;
        }
    }

    protected void setupContext(RunData rundata) {
        String url = popUrl(rundata);

        PatrolWebUtils.setHttpServletRequest(rundata.getRequest());

        WebURIStatistic uriStat = WebStatisticManager.getInstance().getURIStat(url);
        uriStat.beforeInvoke(rundata.getRequest());

        Profiler.enter(url, Profiler.EntryType.URL);
    }

    public ValveForward invoke(RunData rundata) throws PipelineException {
        if (!enableProfile) {
            return null;
        }

        if (ACTION_CLEANUP.equals(action)) {
            cleanupContext(rundata);
        } else {
            setupContext(rundata);
        }

        return null;
    }

    protected String popUrl(RunData rundata) {
        // 对于重写到esite/esite.htm的url，如果有column则记录下column
        String pathInfo = rundata.getPathInfo();
        if (ESITE_VM.equals(pathInfo)) {
            String column = rundata.getParameters().getString(COLUMN_FLAG);
            if (column != null && column.length() != 0) {
                return pathInfo + SPLIT_CHAR + column;
            }
        }

        String uri = "";

        String component = rundata.getComponent();
        if (component != null) {
            uri += component;
        } else {
            uri += "/";
        }

        String target = rundata.getTarget();
        if (target != null) {
            uri += target;
        }

        String actionAndMethod = getActionAndMethod(rundata);
        if (actionAndMethod != null) {
            uri += SPLIT_CHAR + actionAndMethod;
        }

        return uri;
    }

    protected void cleanupContext(RunData rundata) {
        Profiler.Entry entry = Profiler.release();

        if (entry != null && entry.getType() == Profiler.EntryType.URL) {
            String url = entry.getName();
            WebURIStatistic uriStat = WebStatisticManager.getInstance().getURIStat(url);
            uriStat.afterInvoke(entry.getNanoSpanTotal());
            uriStat.recordProfilerInfo(entry);
        }

        if (!ERROR_VM.equals(rundata.getTarget())) { // 如果是跳转到错误页面的请求，不作统计
            // InvocationGatherer.add(Profiler.dump());
        }
        Profiler.reset();
    }

    public String getAction() {
        return this.action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    private String getActionAndMethod(RunData rundata) {
        ParameterParser params = rundata.getParameters();
        String action = params.getString(PerformActionValve.DEFAULT_ACTION_PARAM_NAME);
        if (StringUtil.isBlank(action)) {
            return null;
        }

        // 取得key=eventSubmit_doXyy, value不为空的参数
        String event = null;

        for (Iterator<?> i = params.keySet().iterator(); i.hasNext();) {
            String paramKey = StringUtil.toCamelCase((String) i.next());

            if (paramKey.startsWith(DEFAULT_EVENT_PATTERN) && StringUtil.isNotBlank(params.getString(paramKey))) {
                event = StringUtil.trimToNull(paramKey.substring(DEFAULT_EVENT_PATTERN.length()));

                if (event != null) {
                    break;
                }
            }
        }

        // 查找精确匹配的方法
        String methodName = null;

        if (event != null) {
            methodName = "do" + StringUtil.capitalize(event);
        }

        // 查找fallback method
        if (methodName == null) {
            methodName = "doPerform";
        }

        return "/" + action + "#" + methodName;
    }
}
