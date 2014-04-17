/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.log4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class ThrowableInfoStat {

    private ThrowableInfo    throwableInfo;
    private final AtomicLong count = new AtomicLong();
    private Date             lastDate;
    private String           lastMessage;
    private String           lastStackTrace;
    private String           lastUrl;

    public String getLastUrl() {
        return lastUrl;
    }

    public void setLastUrl(String lastUrl) {
        this.lastUrl = lastUrl;
    }

    public String getLastStackTrace() {
        return lastStackTrace;
    }

    public void setLastStackTrace(String lastStackTrace) {
        this.lastStackTrace = lastStackTrace;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public ThrowableInfoStat(ThrowableInfo throwableInfo){
        this.throwableInfo = throwableInfo;
    }

    public ThrowableInfo getThrowableInfo() {
        return throwableInfo;
    }

    public void setThrowableInfo(ThrowableInfo throwableInfo) {
        this.throwableInfo = throwableInfo;
    }

    public long getCount() {
        return count.get();
    }

    public void incrementCount() {
        this.lastDate = new Date();
        this.count.incrementAndGet();
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    private static CompositeType COMPOSITE_TYPE = null;

    public static CompositeType getCompositeType() throws JMException {
        if (COMPOSITE_TYPE != null) {
            return COMPOSITE_TYPE;
        }

        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING,
                SimpleType.STRING, SimpleType.LONG, SimpleType.DATE, SimpleType.STRING };

        String[] indexNames = { "Type", "StackTrace", "Method", "URI", "Count", "LastThrowDate", "LastThrowMessage" };
        String[] indexDescriptions = { "Type", "StackTrace", "Method", "URI", "Count", "LastThrowDate",
                "LastThrowMessage" };
        COMPOSITE_TYPE = new CompositeType("ExceptionStatistic", "Exception Statistic", indexNames, indexDescriptions,
                                           indexTypes);

        return COMPOSITE_TYPE;
    }

    public CompositeDataSupport getCompositeData() throws JMException {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("Type", throwableInfo.getType());
        map.put("StackTrace", lastStackTrace);
        map.put("Method", throwableInfo.getMethod());
        map.put("URI", getLastUrl());
        map.put("Count", getCount());

        map.put("LastThrowDate", getLastDate());
        map.put("LastThrowMessage", getLastMessage());

        return new CompositeDataSupport(getCompositeType(), map);
    }
}
