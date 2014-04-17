/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory implements ThreadFactory {

    final ThreadGroup   group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String        namePrefix;
    final String        nameSuffix   = "]";

    public DaemonThreadFactory(String poolName){
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = poolName + " Pool [Thread-";
    }

    public DaemonThreadFactory(String poolName, ThreadGroup threadGroup){
        group = threadGroup;
        namePrefix = poolName + " Pool [Thread-";
    }

    public ThreadGroup getThreadGroup() {
        return group;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement() + nameSuffix, 0);
        t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
