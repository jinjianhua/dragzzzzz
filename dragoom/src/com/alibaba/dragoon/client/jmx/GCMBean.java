/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

public interface GCMBean {

    long getPermGenCommitted();

    long getPermGenInit();

    long getPermGenMax();

    long getPermGenUsed();

    // OldGen
    long getOldGenUsed();

    long getOldGenCommitted();

    long getOldGenInit();

    long getOldGenMax();

    // PS Eden Gen
    long getEdenSpaceCommitted();

    long getEdenSpaceInit();

    long getEdenSpaceMax();

    long getEdenSpaceUsed();

    // PS_Survivor_Space
    long getSurvivorSpaceCommitted();

    long getSurvivorSpaceInit();

    long getSurvivorSpaceMax();

    long getSurvivorSpaceUsed();

    // GarbageCollector PS MarkSweep
    long getMarkSweepCollectionCount();

    long getMarkSweepCollectionTime();

    // GarbageCollector PS Scavenge
    long getYoungGCCollectionCount();

    long getYoungGCCollectionTime();

    long getFullGCCollectionCount();

    long getFullGCCollectionTime();
}
