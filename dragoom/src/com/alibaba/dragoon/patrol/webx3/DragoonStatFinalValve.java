/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.webx3;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.dragoon.patrol.web.PatrolWebUtils;
import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.WebURIStatistic;

public class DragoonStatFinalValve extends AbstractValve {

    private HttpServletRequest request;
    private final List<String> exclusions = new ArrayList<String>();

    public void invoke(PipelineContext context) throws Exception {
        WebURIStatistic uriStat = WebURIStatistic.current();
        if (uriStat != null) {
            Profiler.Entry entry = Profiler.release();

            if (entry != null && entry.getType() == Profiler.EntryType.URL) {
                uriStat.afterInvoke(entry.getNanoSpanTotal());
                uriStat.recordProfilerInfo(entry);
            }

            Profiler.reset();
        }

        context.invokeNext();
        PatrolWebUtils.setHttpServletRequest(null);
        PatrolWebUtils.setHttpServletResponse(null);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setExclusions(String text) {
        if (text == null) {
            this.exclusions.clear();
            return;
        }

        text = text.trim();

        if (text.length() == 0) {
            this.exclusions.clear();
            return;
        }

        String[] items = text.split("\\,");
        for (String item : items) {
            exclusions.add(item);
        }
    }

    public boolean isExclusion(String uri) {
        for (String exclusion : this.exclusions) {
            if (uri.endsWith(exclusion)) {
                return true;
            }
        }

        return false;
    }

    public List<String> getExclusions() {
        return exclusions;
    }
}
