/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanServer;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class Memory extends ManagedBeanBase implements MemoryMBean {

    private MBeanServer           mbeanServer;
    private MemoryMXBean          memoryMXBean;

    private OperatingSystemMXBean OperatingSystem;

    public Memory(MBeanServer mbeanServer){
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        OperatingSystem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    // Memory Heap
    public long getMemoryHeapCommitted() {
        return getRuntimeTotalMemory();
    }

    public long getMemoryHeapInit() {
        return memoryMXBean.getHeapMemoryUsage().getInit();
    }

    public long getMemoryHeapMax() {
        return getRuntimeMaxMemory();
    }

    public long getMemoryHeapUsed() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    // Memory NonHeap
    public long getMemoryNonHeapCommitted() {
        return memoryMXBean.getNonHeapMemoryUsage().getCommitted();
    }

    public long getNonMemoryHeapInit() {
        return memoryMXBean.getNonHeapMemoryUsage().getInit();
    }

    public long getNonMemoryHeapMax() {
        return memoryMXBean.getNonHeapMemoryUsage().getMax();
    }

    public long getMemoryNonHeapUsed() {
        return memoryMXBean.getNonHeapMemoryUsage().getUsed();
    }

    // /

    public long getCommittedVirtualMemorySize() {
        return OperatingSystem.getCommittedVirtualMemorySize();
    }

    public long getUsedPhysicalMemorySize() {
        return getTotalPhysicalMemorySize() - OperatingSystem.getFreePhysicalMemorySize();
    }

    public long getTotalPhysicalMemorySize() {
        return OperatingSystem.getTotalPhysicalMemorySize();
    }

    public long getUsedSwapSpaceSize() {
        return OperatingSystem.getFreeSwapSpaceSize();
    }

    public long getTotalSwapSpaceSize() {
        return OperatingSystem.getTotalPhysicalMemorySize();
    }

    public void dumpAll(PrintStream out) {
        out.println("Memory Heap committed : " + this.getMemoryHeapCommitted());
        out.println("Memory Heap init : " + this.getMemoryHeapInit());
        out.println("Memory Heap max : " + this.getMemoryHeapMax());
        out.println("Memory Heap used : " + this.getMemoryHeapUsed());

        out.println("Memory NonHeap committed : " + this.getMemoryNonHeapCommitted());
        out.println("Memory NonHeap init : " + this.getNonMemoryHeapInit());
        out.println("Memory NonHeap max : " + this.getNonMemoryHeapMax());
        out.println("Memory NonHeap used : " + this.getMemoryNonHeapUsed());

        out.println("CommittedVirtualMemorySize : " + this.getCommittedVirtualMemorySize());
        out.println("UsedPhysicalMemorySize : " + this.getUsedPhysicalMemorySize());
        out.println("TotalPhysicalMemorySize : " + this.getTotalPhysicalMemorySize());
        out.println("UsedSwapSpaceSize : " + this.getUsedSwapSpaceSize());
        out.println("TotalSwapSpaceSize : " + this.getTotalSwapSpaceSize());
        out.println("RuntimeMaxMemory : " + this.getRuntimeMaxMemory());
        out.println("RuntimeFreeMemory : " + this.getRuntimeFreeMemory());
        out.println("RuntimeTotalMemory : " + this.getRuntimeTotalMemory());
    }

    public void dump(PrintStream out) {
        out.println("Memory Heap committed : " + this.getMemoryHeapCommitted());
        out.println("Memory Heap used : " + this.getMemoryHeapUsed());
        out.println("Memory NonHeap committed : " + this.getMemoryNonHeapCommitted());
        out.println("Memory NonHeap used : " + this.getMemoryNonHeapUsed());
        out.println("UsedPhysicalMemorySize : " + this.getUsedPhysicalMemorySize());
        out.println("TotalPhysicalMemorySize : " + this.getTotalPhysicalMemorySize());
        out.println("UsedSwapSpaceSize : " + this.getUsedSwapSpaceSize());
        out.println("TotalSwapSpaceSize : " + this.getTotalSwapSpaceSize());
    }

    public void dump() {
        // this.getMemoryHeapCommitted(); // 3
        // this.getMemoryHeapUsed(); // 3
        // this.getMemoryNonHeapCommitted(); //2
        // this.getMemoryNonHeapUsed(); //2
        // this.getUsedPhysicalMemorySize(); // 9
        // this.getTotalPhysicalMemorySize(); // 5
        // this.getUsedSwapSpaceSize(); // 10
        // this.getTotalSwapSpaceSize(); // 5
        this.getRuntimeMaxMemory();
        this.getRuntimeFreeMemory();
        this.getRuntimeTotalMemory();
    }

    public long getRuntimeMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public long getRuntimeFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public long getRuntimeTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }
}
