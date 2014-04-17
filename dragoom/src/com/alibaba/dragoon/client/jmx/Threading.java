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
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.MathContext;

import javax.management.MBeanServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class Threading extends ManagedBeanBase implements ThreadingMBean {

    private final static Log      LOG = LogFactory.getLog(Threading.class);

    private MBeanServer           mbeanServer;
    private final ThreadMXBean    Threading;
    private OperatingSystemMXBean OperatingSystem;
    private RuntimeMXBean         Runtime;
    private GC                    gc;

    private volatile long         lastGCTime;
    private volatile long         lastCPUTime;
    private volatile long         lastGCUpTime;
    private volatile long         lastCPUUpTime;

    public Threading(){
        Threading = ManagementFactory.getThreadMXBean();
        OperatingSystem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Runtime = ManagementFactory.getRuntimeMXBean();

        try {
            gc = new GC();

            // 初始化
            lastGCTime = gc.getFullGCCollectionTime() + gc.getYoungGCCollectionTime();
            lastCPUTime = getProcessCpuTime();
            lastGCUpTime = lastCPUUpTime = getUpTime();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public int getDaemonThreadCount() {
        return Threading.getDaemonThreadCount();
    }

    public int getThreadCount() {
        return Threading.getThreadCount();
    }

    public long getTotalStartedThreadCount() {
        return Threading.getTotalStartedThreadCount();
    }

    public long getProcessCpuTime() {
        return OperatingSystem.getProcessCpuTime();
    }

    public long getUpTime() {
        return Runtime.getUptime();
    }

    public BigDecimal getProcessCpuTimeRate() {
        long cpuTime = getProcessCpuTime();
        long upTime = getUpTime();

        long elapsedCpu = cpuTime - lastCPUTime;
        long elapsedTime = upTime - lastCPUUpTime;

        lastCPUTime = cpuTime;
        lastCPUUpTime = upTime;

        BigDecimal cpuRate;
        if (elapsedTime <= 0) {
            return new BigDecimal(0);
        }

        float cpuUsage = elapsedCpu / (elapsedTime * 10000F);
        cpuRate = new BigDecimal(cpuUsage, new MathContext(4));

        return cpuRate;
    }

    public BigDecimal getFullGCCollectionTimeRate() {
        long gcTime = getFullGCCollectionTime() + getYoungGCCollectionTime();
        long upTime = getUpTime();

        long elapsedGc = gcTime - lastGCTime;
        long elapsedTime = upTime - lastGCUpTime;

        BigDecimal gcRate;
        if (elapsedTime <= 0) {
            gcRate = new BigDecimal(0);
        } else {
            float gcUsage = (elapsedGc * 10F) / (elapsedTime);
            gcRate = new BigDecimal(gcUsage, new MathContext(4));
        }

        lastGCTime = gcTime;
        lastGCUpTime = upTime;

        return gcRate;
    }

    public long getFullGCCollectionTime() {
        return gc.getFullGCCollectionTime();
    }

    public long getYoungGCCollectionTime() {
        return gc.getYoungGCCollectionTime();
    }

    public int getRunnableThreadCount() {
        return getThreadCountByState("RUNNABLE");
    }

    public int getNewThreadCount() {
        return getThreadCountByState("NEW");
    }

    public int getBlockedThreadCount() {
        return getThreadCountByState("BLOCKED");
    }

    public int getWaitingThreadCount() {
        return getThreadCountByState("WAITING");
    }

    public int getAvailableProcessors() {
        return OperatingSystem.getAvailableProcessors();
    }

    public int getTimedWaitingThreadCount() {
        return getThreadCountByState("TIMED_WAITING");
    }

    public int getTeminatedThreadCount() {
        return getThreadCountByState("TERMINATED");
    }

    public int getDeadLockedThreadCount() {
        try {
            long[] deadLockedThreadIds = Threading.findDeadlockedThreads();

            if (deadLockedThreadIds == null) {
                return 0;
            }

            return deadLockedThreadIds.length;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private int getThreadCountByState(final String state) {
        ThreadInfo[] threadInfoArray = dumpAllThreads();
        int runnableCount = 0;
        for (ThreadInfo threadInfo : threadInfoArray) {
            if (state.equals(threadInfo.getThreadState().name())) {
                runnableCount++;
            }
        }

        return runnableCount;
    }

    private ThreadInfo[] dumpAllThreads() {
        return Threading.dumpAllThreads(false, false);
    }

    public void dump(PrintStream out) {
        out.println("DaemonThreadCount : " + this.getDaemonThreadCount());
        out.println("ThreadCount : " + this.getThreadCount());

        out.println("NewThreadCount : " + this.getNewThreadCount());
        out.println("RunnableThreadCount : " + this.getRunnableThreadCount());
        out.println("BlockedThreadCount : " + this.getBlockedThreadCount());
        out.println("WaitingThreadCount : " + this.getWaitingThreadCount());
        out.println("TimedWaitingThreadCount : " + this.getTimedWaitingThreadCount());
        out.println("TeminatedThreadCount : " + this.getTeminatedThreadCount());

        out.println("DeadLockedThreadCount : " + this.getDeadLockedThreadCount());
        out.println("TotalStartedThreadCount : " + this.getTotalStartedThreadCount());

        out.println("ProcessCpuTimeRate : " + this.getProcessCpuTimeRate());
        out.println("FullGCCollectionTimeRate : " + this.getFullGCCollectionTimeRate());
    }

    public void dump() {
        this.getDaemonThreadCount();
        this.getThreadCount();

        this.getNewThreadCount();
        this.getRunnableThreadCount();
        this.getBlockedThreadCount();
        this.getWaitingThreadCount();
        this.getTimedWaitingThreadCount();
        this.getTeminatedThreadCount();

        this.getDeadLockedThreadCount();
        this.getTotalStartedThreadCount();

        this.getProcessCpuTimeRate();
        this.getFullGCCollectionTimeRate();
    }
}
