/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

import java.io.PrintStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ListIterator;

import javax.management.MBeanServer;

public class GC extends ManagedBeanBase implements GCMBean {

    private MemoryPoolMXBean       Perm_Gen;
    private MemoryPoolMXBean       Old_Gen;
    private MemoryPoolMXBean       Eden_Space;
    private MemoryPoolMXBean       PS_Survivor_Space;

    private GarbageCollectorMXBean FullGC;
    private GarbageCollectorMXBean YoungGC;

    private GCProvider             gcProvider = null;

    public GC(){
        try {
            gcProvider = GCProvider.createGCProvider();

            for (ListIterator<MemoryPoolMXBean> iter = ManagementFactory.getMemoryPoolMXBeans().listIterator(); iter.hasNext();) {
                MemoryPoolMXBean item = iter.next();
                if ("CMS Perm Gen".equals(item.getName()) //
                    || "Perm Gen".equals(item.getName()) //
                    || "PS Perm Gen".equals(item.getName()) //
                    || "G1 Perm Gen".equals(item.getName()) //
                ) {
                    Perm_Gen = item;
                } else if ("CMS Old Gen".equals(item.getName()) //
                           || "Tenured Gen".equals(item.getName()) //
                           || "PS Old Gen".equals(item.getName()) //
                           || "G1 Old Gen".equals(item.getName()) //
                ) {
                    Old_Gen = item;
                } else if ("Par Eden Space".equals(item.getName()) //
                           || "Eden Space".equals(item.getName()) //
                           || "PS Eden Space".equals(item.getName()) //
                           || "G1 Eden".equals(item.getName()) //
                ) {
                    Eden_Space = item;
                } else if ("Par Survivor Space".equals(item.getName()) //
                           || "Survivor Space".equals(item.getName()) //
                           || "PS Survivor Space".equals(item.getName()) //
                           || "G1 Survivor".equals(item.getName()) //
                ) {
                    PS_Survivor_Space = item;
                }
            }

            for (ListIterator<GarbageCollectorMXBean> iter = ManagementFactory.getGarbageCollectorMXBeans().listIterator(); iter.hasNext();) {
                GarbageCollectorMXBean item = iter.next();
                if ("ConcurrentMarkSweep".equals(item.getName()) //
                    || "MarkSweepCompact".equals(item.getName()) //
                    || "PS MarkSweep".equals(item.getName()) //
                    || "G1 Old Generation".equals(item.getName()) //
                ) {
                    FullGC = item;
                } else if ("ParNew".equals(item.getName()) //
                           || "Copy".equals(item.getName()) //
                           || "G1 Young Generation".equals(item.getName()) //
                ) {
                    YoungGC = item;
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public GCProvider getGCProvider() {
        return gcProvider;
    }

    public long getPermGenCommitted() {
        if (Perm_Gen == null) {
            return 0;
        }

        return Perm_Gen.getUsage().getCommitted();
    }

    public long getPermGenInit() {
        if (Perm_Gen == null) {
            return 0;
        }

        return Perm_Gen.getUsage().getInit();
    }

    public long getPermGenMax() {
        if (Perm_Gen == null) {
            return 0;
        }

        return Perm_Gen.getUsage().getMax();
    }

    public long getPermGenUsed() {
        if (Perm_Gen == null) {
            return 0;
        }

        return Perm_Gen.getUsage().getUsed();
    }

    // OldGen
    public long getOldGenCommitted() {
        if (Old_Gen == null) {
            return 0;
        }

        return Old_Gen.getUsage().getCommitted();
    }

    public long getOldGenInit() {
        if (Old_Gen == null) {
            return 0;
        }

        return Old_Gen.getUsage().getInit();
    }

    public long getOldGenMax() {
        if (Old_Gen == null) {
            return 0;
        }

        return Old_Gen.getUsage().getMax();
    }

    public long getOldGenUsed() {
        if (Old_Gen == null) {
            return 0;
        }

        return Old_Gen.getUsage().getUsed();
    }

    // PS Eden Gen
    // 360 nano
    public long getEdenSpaceCommitted() {
        if (Eden_Space == null) {
            return 0;
        }

        return Eden_Space.getUsage().getCommitted();
    }

    public long getEdenSpaceInit() {
        if (Eden_Space == null) {
            return 0;
        }

        return Eden_Space.getUsage().getInit();
    }

    public long getEdenSpaceMax() {
        if (Eden_Space == null) {
            return 0;
        }

        return Eden_Space.getUsage().getMax();
    }

    public long getEdenSpaceUsed() {
        if (Eden_Space == null) {
            return 0;
        }

        return Eden_Space.getUsage().getInit();
    }

    // PS_Survivor_Space
    public long getSurvivorSpaceCommitted() {
        if (PS_Survivor_Space == null) {
            return 0;
        }

        return PS_Survivor_Space.getUsage().getCommitted();
    }

    public long getSurvivorSpaceInit() {
        if (PS_Survivor_Space == null) {
            return 0;
        }

        return PS_Survivor_Space.getUsage().getInit();
    }

    public long getSurvivorSpaceMax() {
        if (PS_Survivor_Space == null) {
            return 0;
        }

        return PS_Survivor_Space.getUsage().getMax();
    }

    public long getSurvivorSpaceUsed() {
        if (PS_Survivor_Space == null) {
            return 0;
        }

        return PS_Survivor_Space.getUsage().getUsed();
    }

    // GarbageCollector PS MarkSweep
    public long getMarkSweepCollectionCount() {
        if (FullGC == null) {
            return 0;
        }

        return FullGC.getCollectionCount();
    }

    public long getMarkSweepCollectionTime() {
        if (FullGC == null) {
            return 0;
        }

        return FullGC.getCollectionTime();
    }

    // GarbageCollector PS Scavenge
    public long getYoungGCCollectionCount() {
        if (gcProvider == null) {
            return 0;
        }

        return gcProvider.getYoungGCCollectionCount();
    }

    public long getYoungGCCollectionTime() {
        if (YoungGC == null) {
            return 0;
        }

        return YoungGC.getCollectionTime();
    }

    public long getFullGCCollectionCount() {
        if (gcProvider == null) {
            return 0;
        }

        return gcProvider.getFullGCCollectionCount();
    }

    public long getFullGCCollectionTime() {
        return getMarkSweepCollectionTime();
    }

    public void dump() {
        this.getPermGenUsed();

        this.getOldGenUsed();

        this.getEdenSpaceUsed();

        this.getSurvivorSpaceUsed();

        this.getFullGCCollectionCount();
        this.getFullGCCollectionTime();

        this.getYoungGCCollectionCount();
        this.getYoungGCCollectionTime();
    }

    public void dump(PrintStream out) {
        out.println("Perm Gen used : " + this.getPermGenUsed());

        out.println("Old Gen used : " + this.getOldGenUsed());

        out.println("Eden Space used : " + this.getEdenSpaceUsed());

        out.println("Survivor Space used : " + this.getSurvivorSpaceUsed());

        out.println("Full GC CollectionCount : " + this.getFullGCCollectionCount());
        out.println("Full GC CollectionTime : " + this.getFullGCCollectionTime());

        out.println("Young GC CollectionCount : " + this.getYoungGCCollectionCount());
        out.println("Young GC CollectionTime : " + this.getYoungGCCollectionTime());
    }

    public void dumpAll(PrintStream out) {
        out.println("Perm Gen committed : " + this.getPermGenCommitted());
        out.println("Perm Gen init : " + this.getPermGenInit());
        out.println("Perm Gen max : " + this.getPermGenMax());
        out.println("Perm Gen used : " + this.getPermGenUsed());

        out.println("Old Gen committed : " + this.getOldGenCommitted());
        out.println("Old Gen init : " + this.getOldGenInit());
        out.println("Old Gen max : " + this.getOldGenMax());
        out.println("Old Gen used : " + this.getOldGenUsed());

        out.println("Eden Space committed : " + this.getEdenSpaceCommitted());
        out.println("Eden Space init : " + this.getEdenSpaceInit());
        out.println("Eden Space max : " + this.getEdenSpaceMax());
        out.println("Eden Space used : " + this.getEdenSpaceUsed());

        out.println("Survivor Space committed : " + this.getSurvivorSpaceCommitted());
        out.println("Survivor Space init : " + this.getSurvivorSpaceInit());
        out.println("Survivor Space max : " + this.getSurvivorSpaceMax());
        out.println("Survivor Space used : " + this.getSurvivorSpaceUsed());

        out.println("Full GC CollectionCount : " + this.getFullGCCollectionCount());
        out.println("Full GC CollectionTime : " + this.getFullGCCollectionTime());

        out.println("Young GC CollectionCount : " + this.getYoungGCCollectionCount());
        out.println("Young GC CollectionTime : " + this.getYoungGCCollectionTime());
    }

    @Override
    protected MBeanServer getMBeanServer() {
        return null;
    }
}
