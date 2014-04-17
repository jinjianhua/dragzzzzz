package com.alibaba.dragoon.patrol.jvm.vo;

public class MemoryPool {

    private String name;
    private long   init;
    private long   max;
    private long   used;
    private long   committed;
    private long   peakUsed;
    private long   collectionUsed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getInit() {
        return init;
    }

    public void setInit(long init) {
        this.init = init;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getCommitted() {
        return committed;
    }

    public void setCommitted(long committed) {
        this.committed = committed;
    }

    public long getPeakUsed() {
        return peakUsed;
    }

    public void setPeakUsed(long peakUsed) {
        this.peakUsed = peakUsed;
    }

    public long getCollectionUsed() {
        return collectionUsed;
    }

    public void setCollectionUsed(long collectionUsed) {
        this.collectionUsed = collectionUsed;
    }

}
