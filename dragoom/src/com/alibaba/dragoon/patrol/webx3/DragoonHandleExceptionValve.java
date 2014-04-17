/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
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

/**
 * 测试uri异常
 * 
 * @author ye.wangy 笨笨
 * @datetime Jan 13, 2012 2:20:09 PM
 * @email ye.wangy@alibaba-inc.com
 */
public class DragoonHandleExceptionValve extends AbstractValve {

    private String             name;
    private HttpServletRequest request;
    private final List<String> exclusions = new ArrayList<String>();

    public void invoke(PipelineContext context) throws Exception {
        if (isExclusion(request.getRequestURI())) {
            context.invokeNext();
            return;
        }
        // 本地记录异常
        Throwable originalException = (Throwable) context.getAttribute(name);
        // 记录错误
        PatrolWebUtils.addError(request, originalException, null);
        // 往下走
        context.invokeNext();
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

    public HttpServletRequest getRequest() {
        return request;
    }

    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
