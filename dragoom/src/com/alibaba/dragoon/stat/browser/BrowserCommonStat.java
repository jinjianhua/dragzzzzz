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

import com.alibaba.dragoon.patrol.browser.BrowserCommon;

public class BrowserCommonStat {

    private BrowserCommon     browserCommon;
    private Long              seq;                          // 为了避免 MBean异常 ，keyExistException

    private static AtomicLong seqCreator = new AtomicLong();

    public BrowserCommonStat(BrowserCommon browserCommon){
        this.browserCommon = browserCommon;
        this.seq = seqCreator.incrementAndGet();
    }

    private static final String[] indexNames     = { "appNumber", "pageId", "ua", "url", "screenH", "screenW",
            "colorDepth", "visibleW", "visibleH", "navName", "navEngine", "navVer", "lang", "osName", "osVer",
            "flashVer", "javaEnabled", "device", "loadTime", "session", "pageSeed", "responseTime", "seq" };

    private static CompositeType  COMPOSITE_TYPE = null;

    public static CompositeType getCompositeType() throws JMException {
        if (COMPOSITE_TYPE != null) {
            return COMPOSITE_TYPE;
        }

        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                SimpleType.STRING, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER, SimpleType.INTEGER,
                SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.LONG };

        String[] indexDescriptions = indexNames;

        COMPOSITE_TYPE = new CompositeType("Browser Common Statistic", "Browser Common Statistic", indexNames,
                                           indexDescriptions, indexTypes);

        return COMPOSITE_TYPE;
    }

    public CompositeDataSupport getCompositeData() throws JMException {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(indexNames[0], browserCommon.getAppNumber());
        map.put(indexNames[1], browserCommon.getPageId());
        map.put(indexNames[2], browserCommon.getUa());
        map.put(indexNames[3], browserCommon.getUrl());
        map.put(indexNames[4], browserCommon.getScreenH());

        map.put(indexNames[5], browserCommon.getScreenW());
        map.put(indexNames[6], browserCommon.getColorDepth());
        map.put(indexNames[7], browserCommon.getVisibleW());
        map.put(indexNames[8], browserCommon.getVisibleH());
        map.put(indexNames[9], browserCommon.getNavName());

        map.put(indexNames[10], browserCommon.getNavEngine());
        map.put(indexNames[11], browserCommon.getNavVer());
        map.put(indexNames[12], browserCommon.getLang());
        map.put(indexNames[13], browserCommon.getOsName());
        map.put(indexNames[14], browserCommon.getOsVer());

        map.put(indexNames[15], browserCommon.getFlashVer());
        map.put(indexNames[16], browserCommon.getJavaEnabled());
        map.put(indexNames[17], browserCommon.getDevice());
        map.put(indexNames[18], browserCommon.getLoadTime());
        map.put(indexNames[19], browserCommon.getSession());

        map.put(indexNames[20], browserCommon.getPageSeed());
        map.put(indexNames[21], browserCommon.getResponseTime());
        map.put(indexNames[22], this.seq);

        return new CompositeDataSupport(getCompositeType(), map);
    }
}
