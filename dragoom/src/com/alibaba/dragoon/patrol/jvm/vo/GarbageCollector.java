package com.alibaba.dragoon.patrol.jvm.vo;

public class GarbageCollector {

    private String name;
    private long   collectionCount;
    private long   collectionTime;
    private long   invocations;

    public long getInvocations() {
        return invocations;
    }

    public void setInvocations(long invocations) {
        this.invocations = invocations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

}
