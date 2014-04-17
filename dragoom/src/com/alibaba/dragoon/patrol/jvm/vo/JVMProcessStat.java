package com.alibaba.dragoon.patrol.jvm.vo;

public class JVMProcessStat {

    private double systemLoadAverage;
    private long   totalStartedThreadCount;
    private long   threadCount;
    private long   peakThreadCount;
    private long   daemonThreadCount;

    private long   syncInflations;
    private long   syncDeflations;
    private long   syncContendedLockAttempts;
    private long   syncMonExtant;
    private long   syncNotifications;
    
    private long   syncParks;

    private long   tlabAlloc;
    private int    tlabAllocThreads;
    private long   tlabFastWaste;
    private long   tlabFills;
    private long   tlabGcWaste;

    public long getTlabAlloc() {
        return tlabAlloc;
    }

    public void setTlabAlloc(long tlabAlloc) {
        this.tlabAlloc = tlabAlloc;
    }

    public int getTlabAllocThreads() {
        return tlabAllocThreads;
    }

    public void setTlabAllocThreads(int tlabAllocThreads) {
        this.tlabAllocThreads = tlabAllocThreads;
    }

    public long getTlabFastWaste() {
        return tlabFastWaste;
    }

    public void setTlabFastWaste(long tlabFastWaste) {
        this.tlabFastWaste = tlabFastWaste;
    }

    public long getTlabFills() {
        return tlabFills;
    }

    public void setTlabFills(long tlabFills) {
        this.tlabFills = tlabFills;
    }

    public long getTlabGcWaste() {
        return tlabGcWaste;
    }

    public void setTlabGcWaste(long tlabGcWaste) {
        this.tlabGcWaste = tlabGcWaste;
    }

    public long getSyncInflations() {
        return syncInflations;
    }

    public void setSyncInflations(long syncInflactions) {
        this.syncInflations = syncInflactions;
    }

    public long getSyncDeflations() {
        return syncDeflations;
    }

    public void setSyncDeflations(long syncDeflations) {
        this.syncDeflations = syncDeflations;
    }

    public long getSyncContendedLockAttempts() {
        return syncContendedLockAttempts;
    }

    public void setSyncContendedLockAttempts(long syncContendedLockAttempts) {
        this.syncContendedLockAttempts = syncContendedLockAttempts;
    }

    public long getSyncMonExtant() {
        return syncMonExtant;
    }

    public void setSyncMonExtant(long syncMonExtant) {
        this.syncMonExtant = syncMonExtant;
    }

    public long getSyncNotifications() {
        return syncNotifications;
    }

    public void setSyncNotifications(long syncNotifications) {
        this.syncNotifications = syncNotifications;
    }

    public long getSyncParks() {
        return syncParks;
    }

    public void setSyncParks(long syncParks) {
        this.syncParks = syncParks;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public long getTotalStartedThreadCount() {
        return totalStartedThreadCount;
    }

    public void setTotalStartedThreadCount(long totalStartedThreadCount) {
        this.totalStartedThreadCount = totalStartedThreadCount;
    }

    public long getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(long threadCount) {
        this.threadCount = threadCount;
    }

    public long getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(long peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    public long getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(long daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

}
