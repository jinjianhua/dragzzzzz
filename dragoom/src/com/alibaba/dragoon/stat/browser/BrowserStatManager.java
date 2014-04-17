/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat.browser;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import com.alibaba.dragoon.patrol.browser.BrowserCommon;
import com.alibaba.dragoon.patrol.browser.BrowserElement;
import com.alibaba.dragoon.patrol.browser.BrowserError;

public class BrowserStatManager implements BrowserStatManagerMBean, Serializable {

    private static final long        serialVersionUID = 8889300719172992134L;

    public static BrowserStatManager instance         = new BrowserStatManager();

    public static final BrowserStatManager getInstance() {
        return instance;
    }

    public static final int                                 ACCEPTED_COUNT        = 100 * 1000;

    private AtomicLong                                      commonDiscardedCount  = new AtomicLong();
    private AtomicLong                                      errorDiscardedCount   = new AtomicLong();
    private AtomicLong                                      elementDiscardedCount = new AtomicLong();

    private final AtomicLong                                commonResetCount      = new AtomicLong();
    private final AtomicLong                                errorResetCount       = new AtomicLong();
    private final AtomicLong                                elementResetCount     = new AtomicLong();

    private ConcurrentLinkedQueue<BrowserCommonStat>  commonList            = new ConcurrentLinkedQueue<BrowserCommonStat>();
    private ConcurrentLinkedQueue<BrowserErrorStat>   errorList             = new ConcurrentLinkedQueue<BrowserErrorStat>();
    private ConcurrentLinkedQueue<BrowserElementStat> elementList           = new ConcurrentLinkedQueue<BrowserElementStat>();

    private final AtomicLong                                commonCount           = new AtomicLong();
    private final AtomicLong                                errorCount            = new AtomicLong();
    private final AtomicLong                                elementCount          = new AtomicLong();

    private boolean                                         enable                = true;

    public BrowserStatManager(){
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void addCommon(BrowserCommon common) {
        if (!enable) {
            commonDiscardedCount.incrementAndGet();
            return;
        }

        if (commonCount.get() >= ACCEPTED_COUNT) {
            commonDiscardedCount.incrementAndGet();
            return;
        }

        commonList.add(new BrowserCommonStat(common));
        commonCount.incrementAndGet();
    }

    public void addError(BrowserError error) {
        if (!enable) {
            errorDiscardedCount.incrementAndGet();
            return;
        }

        if (errorCount.get() >= ACCEPTED_COUNT) {
            errorDiscardedCount.incrementAndGet();
            return;
        }

        errorList.add(new BrowserErrorStat(error));
        errorCount.incrementAndGet();
    }

    public void addElement(BrowserElement element) {
        if (!enable) {
            elementDiscardedCount.incrementAndGet();
            return;
        }

        if (elementCount.get() >= ACCEPTED_COUNT) {
            elementDiscardedCount.incrementAndGet();
            return;
        }

        elementList.add(new BrowserElementStat(element));
        elementCount.incrementAndGet();
    }

    public long getErrorResetCount() {
        return errorResetCount.get();
    }

    public long getCommonResetCount() {
        return commonResetCount.get();
    }

    public long getElementResetCount() {
        return elementResetCount.get();
    }

    public void commonReset() {
        commonResetCount.incrementAndGet();

        commonCount.set(0);
        commonList = new ConcurrentLinkedQueue<BrowserCommonStat>();
    }

    public void errorReset() {
        errorResetCount.incrementAndGet();

        errorCount.set(0);
        errorList = new ConcurrentLinkedQueue<BrowserErrorStat>();
    }

    public void elementReset() {
        elementResetCount.incrementAndGet();

        elementCount.set(0);
        elementList = new ConcurrentLinkedQueue<BrowserElementStat>();
    }

    public TabularData getCommonList() throws JMException {
        CompositeType rowType = BrowserCommonStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);

        TabularType tabularType = new TabularType("CommonList", "CommonList", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        for (BrowserCommonStat stat : commonList) {
            data.put(stat.getCompositeData());
        }

        return data;
    }

    public TabularData getErrorList() throws JMException {
        CompositeType rowType = BrowserErrorStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);

        TabularType tabularType = new TabularType("ErrorList", "ErrorList", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        for (BrowserErrorStat stat : errorList) {
            data.put(stat.getCompositeData());
        }

        return data;
    }

    public TabularData getElementList() throws JMException {
        CompositeType rowType = BrowserElementStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);

        TabularType tabularType = new TabularType("ElementList", "ElementList", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        for (BrowserElementStat stat : elementList) {
            data.put(stat.getCompositeData());
        }

        return data;
    }

    public long getCommonDiscardedCount() {
        return commonDiscardedCount.get();
    }

    public long getErrorDiscardedCount() {
        return errorDiscardedCount.get();
    }

    public long getElementDiscardedCount() {
        return elementDiscardedCount.get();
    }

    public String getVersion() {
        return "2.6.2 2011-10-24 17:42";
    }
}
