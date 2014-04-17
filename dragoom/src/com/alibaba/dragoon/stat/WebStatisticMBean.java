/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Date;

/**
 * @author shaojin.wensj
 */
public interface WebStatisticMBean {

    long getId();

    String getURI();

    long getNanoMax();

    long getErrorCount();

    int getRunningCount();

    int getConcurrentMax();

    long getCount();

    Date getLastAccessTime();

    long getNanoTotal();

    Date getLastErrorTime();

    String getLastErrorReferer();

    /**
     * @return 最后出错的原始url（如果是get请求，会包含原始的参数）
     * @author wangye
     */
    String getLastErrorUrl();

    /**
     * @return 最后出错的请求用户
     * @author wangye
     */
    String getLastErrorUser();
}
