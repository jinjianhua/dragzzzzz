package com.alibaba.dragoon.client.jmx.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ListIterator;

import com.alibaba.dragoon.client.jmx.GCProvider;

public class ManagementProvider extends GCProvider {

    private GarbageCollectorMXBean old;
    private GarbageCollectorMXBean young;

    public ManagementProvider(){
        for (ListIterator<GarbageCollectorMXBean> iter = ManagementFactory.getGarbageCollectorMXBeans().listIterator(); iter.hasNext();) {
            GarbageCollectorMXBean item = iter.next();
            if ("ConcurrentMarkSweep".equals(item.getName()) || "MarkSweepCompact".equals(item.getName())
                || "PS MarkSweep".equals(item.getName())) {
                old = item;
                break;
            }
        }

        for (ListIterator<GarbageCollectorMXBean> iter = ManagementFactory.getGarbageCollectorMXBeans().listIterator(); iter.hasNext();) {
            GarbageCollectorMXBean item = iter.next();
            if ("ParNew".equals(item.getName()) || "Copy".equals(item.getName())
                || "PS Scavenge".equals(item.getName())) {
                young = item;
                break;
            }
        }
    }

    @Override
    public long getYoungGCCollectionCount() {
        if (young == null) {
            return 0;
        }
        return young.getCollectionCount();
    }

    @Override
    public long getFullGCCollectionCount() {
        if (old == null) {
            return 0;
        }
        return old.getCollectionCount();
    }

}
