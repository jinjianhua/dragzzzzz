/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

public interface KVStatManagerMBean {

    long getDataCount();

    Date getLastChangedTime();

    void reset();

    long getResetCount();

    List<Map<String, Object>> getKVList() throws JMException;
}
