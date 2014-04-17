/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat.browser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import com.alibaba.dragoon.patrol.browser.BrowserElement;

public class BrowserElementStat {

    private BrowserElement    browserElement;
    private Long              seq;                          // 为了避免 MBean异常 ，keyExistException

    private static AtomicLong seqCreator = new AtomicLong();

    public BrowserElementStat(BrowserElement browserElement){
        this.browserElement = browserElement;
        this.seq = seqCreator.incrementAndGet();
    }

    private static final String[] indexNames     = { "appNumber", "pageId", "ua", "url", "customData", "funcName",
            "enabledTime", "session", "pageSeed", "seq" };

    private static CompositeType  COMPOSITE_TYPE = null;

    public static CompositeType getCompositeType() throws JMException {
        if (COMPOSITE_TYPE != null) {
            return COMPOSITE_TYPE;
        }

        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING, SimpleType.LONG };

        String[] indexDescriptions = indexNames;

        COMPOSITE_TYPE = new CompositeType("Browser Element Statistic", "Browser Element Statistic", indexNames,
                                           indexDescriptions, indexTypes);

        return COMPOSITE_TYPE;
    }

    public CompositeDataSupport getCompositeData() throws JMException {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(indexNames[0], browserElement.getAppNumber());
        map.put(indexNames[1], browserElement.getPageId());
        map.put(indexNames[2], browserElement.getUa());
        map.put(indexNames[3], browserElement.getUrl());
        map.put(indexNames[4], browserElement.getCustomData());

        map.put(indexNames[5], browserElement.getFuncName());
        map.put(indexNames[6], browserElement.getEnabledTime());
        map.put(indexNames[7], browserElement.getSession());
        map.put(indexNames[8], browserElement.getPageSeed());
        map.put(indexNames[9], this.seq);

        return new CompositeDataSupport(getCompositeType(), map);
    }
}
