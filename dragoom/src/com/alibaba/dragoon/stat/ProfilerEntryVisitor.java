package com.alibaba.dragoon.stat;

import java.util.concurrent.ConcurrentMap;

import com.alibaba.dragoon.stat.Profiler.Entry;
import com.alibaba.dragoon.stat.Profiler.EntryStatistic;

public class ProfilerEntryVisitor implements Profiler.EntryVisitor {

    private ConcurrentMap<String, EntryStatistic> entries;

    public ProfilerEntryVisitor(ConcurrentMap<String, EntryStatistic> entries){
        this.entries = entries;
    }

    public void preVisit(Entry entry) {

    }

    public boolean visit(Entry entry) {
        String entryName = entry.getName();
        String parentName = null;

        if (entry.getParentEntry() != null) {
            parentName = entry.getParentEntry().getName();
        }

        EntryStatistic entryStat = entries.get(entryName);
        if (entryStat == null) {
            entries.putIfAbsent(entryName, new EntryStatistic(entryName, parentName, entry.getType()));
            entryStat = entries.get(entryName);
        }

        entryStat.addNano(entry.getNanoSpan(), entry.getNanoSpanTotal());
        if (entry.getError() != null) {
            entryStat.incrementErrorCount();
        }

        return true;
    }

    public void postVisit(Entry entry) {

    }
}
