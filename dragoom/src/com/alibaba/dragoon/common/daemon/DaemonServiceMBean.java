/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon;

import java.util.Date;

public interface DaemonServiceMBean {

    boolean start();

    boolean stop();

    String getState();

    String getName();

    int getMajorVersion();

    int getMinorVersion();

    Date getStartDate();
}
