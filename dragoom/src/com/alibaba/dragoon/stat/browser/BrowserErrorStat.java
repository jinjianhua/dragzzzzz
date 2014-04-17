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

import com.alibaba.dragoon.patrol.browser.BrowserError;

public class BrowserErrorStat {

    private BrowserError      browserError;
    private Long              seq;                          // 为了避免 MBean异常 ，keyExistException

    private static AtomicLong seqCreator = new AtomicLong();

    public BrowserErrorStat(BrowserError browserError){
        this.browserError = browserError;
        this.seq = seqCreator.incrementAndGet();
    }

    private static final String[] indexNames     = { "appNumber", "pageId", "ua", "url", "errName", "errFileName",
            "errUrl", "errLineNum", "errMsg", "errStack", "errOtherData", "session", "pageSeed", "seq" };

    private static CompositeType  COMPOSITE_TYPE = null;

    public static CompositeType getCompositeType() throws JMException {
        if (COMPOSITE_TYPE != null) {
            return COMPOSITE_TYPE;
        }

        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.INTEGER,
                SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                SimpleType.LONG };

        String[] indexDescriptions = indexNames;

        COMPOSITE_TYPE = new CompositeType("Browser Error Statistic", "Browser Error Statistic", indexNames,
                                           indexDescriptions, indexTypes);

        return COMPOSITE_TYPE;
    }

    public CompositeDataSupport getCompositeData() throws JMException {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(indexNames[0], browserError.getAppNumber());
        map.put(indexNames[1], browserError.getPageId());
        map.put(indexNames[2], browserError.getUa());
        map.put(indexNames[3], browserError.getUrl());
        map.put(indexNames[4], browserError.getErrName());

        map.put(indexNames[5], browserError.getErrFileName());
        map.put(indexNames[6], browserError.getErrUrl());
        map.put(indexNames[7], browserError.getErrLineNum());
        map.put(indexNames[8], browserError.getErrMsg());
        map.put(indexNames[9], browserError.getErrStack());

        map.put(indexNames[10], browserError.getErrOtherData());
        map.put(indexNames[11], browserError.getSession());
        map.put(indexNames[12], browserError.getPageSeed());
        map.put(indexNames[13], this.seq);

        return new CompositeDataSupport(getCompositeType(), map);
    }
}
