package com.alibaba.dragoon.patrol.jvm.vo;

import com.alibaba.fastjson.annotation.JSONField;

public class JVMThreadInfo {

    private long                threadID;
    private String              threadState;
    private String              threadName;
    private long                blockedCount;
    private long                blockedTime;
    private long                waitedCount;
    private long                waitedTime;
    private boolean             inNative;
    private boolean             suspended;
    private StackTraceElement[] stackTrace;
    private long                lockOwnerId;
    private String              lockOwnerName;

    @JSONField(name = "id")
    public long getThreadID() {
        return threadID;
    }

    @JSONField(name="id")
    public void setThreadID(long threadID) {
        this.threadID = threadID;
    }

    @JSONField(name="state")
    public String getThreadState() {
        return threadState;
    }

    @JSONField(name="state")
    public void setThreadState(String threadState) {
        this.threadState = threadState;
    }

    @JSONField(name="name")
    public String getThreadName() {
        return threadName;
    }

    @JSONField(name="name")
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public void setBlockedTime(long blockedTime) {
        this.blockedTime = blockedTime;
    }

    public long getWaitedCount() {
        return waitedCount;
    }

    public void setWaitedCount(long waitedCount) {
        this.waitedCount = waitedCount;
    }

    public long getWaitedTime() {
        return waitedTime;
    }

    public void setWaitedTime(long waitedTime) {
        this.waitedTime = waitedTime;
    }

    public boolean getInNative() {
        return inNative;
    }

    public void setInNative(boolean inNative) {
        this.inNative = inNative;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public long getLockOwnerId() {
        return lockOwnerId;
    }

    public void setLockOwnerId(long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }

    public String getLockOwnerName() {
        return lockOwnerName;
    }

    public void setLockOwnerName(String lockOwnerName) {
        this.lockOwnerName = lockOwnerName;
    }

}
