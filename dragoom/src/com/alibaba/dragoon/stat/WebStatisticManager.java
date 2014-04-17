/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebStatisticManager implements WebStatisticManagerMBean {

    public final static Log                                LOG                = LogFactory.getLog(WebStatisticManager.class);

    public final static int                                MAX_STAT_URL_COUNT = 1000 * 10;

    protected final AtomicLong                             uriIdSeed          = new AtomicLong(1000);

    private String                                         contextPath;
    private final AtomicLong                               resetCount         = new AtomicLong();

    protected final static WebStatisticManager             instance           = new WebStatisticManager();

    protected final ConcurrentMap<String, WebURIStatistic> statsMap           = new ConcurrentHashMap<String, WebURIStatistic>();

    public WebStatisticManager(){
        super();
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public ConcurrentMap<String, WebURIStatistic> getUriStatsMap() {
        return statsMap;
    }

    public final static WebStatisticManager getInstance() {
        return instance;
    }

    public long getResetCount() {
        return resetCount.get();
    }

    public void reset() {
        for (WebURIStatistic item : this.statsMap.values()) {
            if (item.getCountDirect() == 0) {
                continue;
            }

            item.reset();
        }

        resetCount.incrementAndGet();
    }

    public WebURIStatistic getURIStat(String uri) {
        return getURIStat(uri, true);
    }

    public WebURIStatistic getURIStat(String uri, boolean create) {
        WebURIStatistic counter = this.statsMap.get(uri);

        if (counter == null) {
            if (!create) {
                return null;
            }

            if (statsMap.size() > MAX_STAT_URL_COUNT) {
                statsMap.clear();
                LOG.error("uri statsMap.size > " + MAX_STAT_URL_COUNT, new Exception());
            }

            WebURIStatistic newCounter = new WebURIStatistic(uri);

            if (statsMap.putIfAbsent(uri, newCounter) == null) {
                newCounter.setId(uriIdSeed.incrementAndGet());
            }

            counter = statsMap.get(uri);
        }

        return counter;
    }

    public void removeURIStat(String uri) {
        statsMap.remove(uri);
    }

    public TabularData getURIList() throws JMException {
        CompositeType rowType = WebURIStatistic.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);

        TabularType tabularType = new TabularType("URIStatisticList", "URIStatisticList", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        for (Map.Entry<String, WebURIStatistic> entry : this.statsMap.entrySet()) {
            if (entry.getValue().getCountDirect() == 0) {
                continue;
            }

            data.put(entry.getValue().getCompositeData());
        }

        return data;
    }
}
