/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

public interface MemoryMBean {

    // Memory Heap
    long getMemoryHeapCommitted();

    long getMemoryHeapInit();

    long getMemoryHeapMax();

    long getMemoryHeapUsed();

    // Memory NonHeap
    long getMemoryNonHeapCommitted();

    long getNonMemoryHeapInit();

    long getNonMemoryHeapMax();

    long getMemoryNonHeapUsed();

    long getCommittedVirtualMemorySize();

    long getUsedPhysicalMemorySize();

    long getTotalPhysicalMemorySize();

    long getUsedSwapSpaceSize();

    long getTotalSwapSpaceSize();

    long getRuntimeMaxMemory();

    long getRuntimeFreeMemory();

    long getRuntimeTotalMemory();
}
