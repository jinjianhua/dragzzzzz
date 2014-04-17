/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

import java.util.Date;

public interface JVMMBean {

    Date getStartTime();

    String getJVM();

    String getJavaVersion();

    String getPID();

    String getInputArguments();

    String getJavaHome();

    String getArch();

    String getOSName();

    String getOSVersion();

    String getJavaSpecificationVersion();

    String getJavaLibraryPath();

    int getAvailableProcessors();

    int getLoadedClassCount();

    long getTotalLoadedClassCount();

    long getUnloadedClassCount();

    long getTotalCompilationTime();
}
