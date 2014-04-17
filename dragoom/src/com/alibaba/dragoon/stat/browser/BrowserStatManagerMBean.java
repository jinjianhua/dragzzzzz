/**
 * Project: dragoon-common
 * 
 * File Created at 2011-6-13
 * $Id$
 * 
 * Copyright 2008 Alibaba.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.dragoon.stat.browser;

import javax.management.JMException;
import javax.management.openmbean.TabularData;

/**
 * @author lijun.cailj
 */
public interface BrowserStatManagerMBean {

    void commonReset();

    void errorReset();

    void elementReset();

    long getCommonResetCount();

    long getErrorResetCount();

    long getElementResetCount();

    TabularData getCommonList() throws JMException;

    TabularData getErrorList() throws JMException;

    TabularData getElementList() throws JMException;

    long getCommonDiscardedCount();

    long getErrorDiscardedCount();

    long getElementDiscardedCount();

    boolean isEnable();
    
    void setEnable(boolean enable);
    
    String getVersion();
}
