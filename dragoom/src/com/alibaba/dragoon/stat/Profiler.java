/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import com.alibaba.dragoon.common.utils.StatUtils;

/**
 * @author shaojin.wensj
 */
public final class Profiler {

    private final static ThreadLocal<Entry> currentStack = new ThreadLocal<Entry>();

    private static int                      maxEntries   = 100;

    public final static Entry enter(String message, EntryType type) {
        Entry currentStack = currentEntry();

        Entry stack = new Entry(currentStack, message, type);

        if (currentStack != null) {
            currentStack.addSubEntry(stack);
        }

        setCurrentStack(stack);

        return stack;
    }

    public final static Entry release() {
        Entry currentStack = currentEntry();

        if (currentStack == null) {
            return null;
        }

        currentStack.release();

        setCurrentStack(currentStack.getParentEntry());

        return currentStack;
    }

    public final static void reset() {
        currentStack.set(null);
    }

    public final static void setError(Throwable error) {
        Entry currentStack = currentEntry();
        currentStack.setError(error);
    }

    public static Entry currentEntry() {
        return currentStack.get();
    }

    private final static void setCurrentStack(Entry entry) {
        currentStack.set(entry);
    }

    public static enum EntryType {
        URL, SERVICE, DAO, OTHER, CACHE, SE, RPC, IBATIS, JDBC, DUBBO_SERVICE, NAPOLI_SERVICE, ACTION;
    }

    public static class Entry {

        private final Entry       parentEntry;
        private final String      name;
        private final EntryType   type;
        private final long        startNanoTime;
        private final List<Entry> subEntryList = new ArrayList<Entry>(4);
        private long              totalNanoSpan;
        private Throwable         error        = null;
        private int               count        = 1;

        public Entry(Entry parentEntry, String name, EntryType type){
            this.parentEntry = parentEntry;
            this.name = name;
            this.type = type;
            this.startNanoTime = System.nanoTime();
        }

        public int getCount() {
            return count;
        }

        public void addCount(int delta) {
            this.count += delta;
        }

        public Entry getParentEntry() {
            return parentEntry;
        }

        public Throwable getError() {
            return error;
        }

        public void setError(Throwable error) {
            this.error = error;
        }

        public String getName() {
            return name;
        }

        public EntryType getType() {
            return type;
        }

        public List<Entry> getSubEntryList() {
            return subEntryList;
        }

        public long getNanoSpanTotal() {
            return totalNanoSpan;
        }

        public long getNanoSpan() {
            long span = totalNanoSpan;

            for (Entry entry : getSubEntryList()) {
                span -= entry.getNanoSpanTotal();
            }

            return span;
        }

        public void addSubEntry(Entry subEntry) {
            if (subEntryList.size() > maxEntries) {
                return;
            }

            this.subEntryList.add(subEntry);
        }

        public String toString() {
            return this.name;
        }

        public void release() {
            totalNanoSpan = System.nanoTime() - this.startNanoTime;
        }

        public void accept(EntryVisitor visitor) {
            visitor.preVisit(this);
            if (visitor.visit(this)) {
                for (Entry subEntry : this.subEntryList) {
                    subEntry.accept(visitor);
                }
            }
            visitor.postVisit(this);
        }

        public void dump(Appendable appendable) {
            accept(new EntryDumpVisitor(appendable));
        }

        public void dump() {
            dump(System.out);
        }
    }

    public static interface EntryVisitor {

        void preVisit(Entry entry);

        boolean visit(Entry entry);

        void postVisit(Entry entry);
    }

    public static class EntryDumpVisitor implements EntryVisitor {

        private Appendable buf;

        public EntryDumpVisitor(Appendable buf){
            super();
            this.buf = buf;
        }

        public boolean visit(Entry entry) {
            try {
                buf.append(entry.getType().name());
                buf.append("\t");
                buf.append(entry.getName());
                buf.append("\t");
                buf.append(NumberFormat.getInstance().format(entry.getNanoSpanTotal() / (1000 * 1000)));
                buf.append("\t");
                buf.append(NumberFormat.getInstance().format(entry.getNanoSpan() / (1000 * 1000)));
                buf.append("\n");
            } catch (IOException ex) {
                throw new IllegalStateException("visit error");
            }
            return true;
        }

        public void preVisit(Entry entry) {

        }

        public void postVisit(Entry entry) {
        }
    }

    public static class EntryStatistic {

        private final String        name;
        private final String        parentName;
        private long                nano       = 0;
        private long                nanoTotal  = 0;
        private long                errorCount = 0;
        private long                count      = 0;
        private final EntryType     type;

        private final ReentrantLock lock       = new ReentrantLock();

        public EntryStatistic(String name, EntryType type){
            this(name, null, type);
        }
        
        public EntryStatistic(String name, String parentName, EntryType type){
            super();
            this.name = name;
            this.parentName = parentName;
            this.type = type;
        }

        public String getParentName() {
            return parentName;
        }

        public void reset() {
            lock.lock();
            try {
                nano = 0L;
                nanoTotal = 0L;
                errorCount = 0L;
                count = 0L;
            } finally {
                lock.unlock();
            }
        }

        public EntryType getType() {
            return type;
        }

        public final String getName() {
            return name;
        }

        public void addNano(long nano, long nanoTotal) {
            lock.lock();
            try {
                this.nano += nano;
                this.nanoTotal += nanoTotal;
                count++;
            } finally {
                lock.unlock();
            }
        }

        public void incrementErrorCount() {
            lock.lock();
            try {
                errorCount++;
            } finally {
                lock.unlock();
            }
        }

        public long getCount() {
            return count;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public final long getNano() {
            return nano;
        }

        public final long getNanoTotal() {
            return nanoTotal;
        }

        public final long getMillis() {
            return getNano() / (1000 * 1000);
        }

        public final long getMillisTotal() {
            return getNanoTotal() / (1000 * 1000);
        }

        private static CompositeType compositeType = null;

        public static CompositeType getCompositeType() throws JMException {
            if (compositeType != null) {
                return compositeType;
            }

            OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG,
                    SimpleType.LONG, SimpleType.LONG, SimpleType.LONG };
            String[] indexNames = { "Name", "ParentName", "Type", "TimeMillis", "TimeMillisTotal", "ErrorCount", "Count" };
            String[] indexDescriptions = indexNames;
            compositeType = new CompositeType("EntryStatistic", "EntryStatistic", indexNames, indexDescriptions,
                                              indexTypes);

            return compositeType;
        }

        public CompositeDataSupport getCompositeData() throws JMException {
            Map<String, Object> map = new HashMap<String, Object>();

            if (StatUtils.isRequiredReset()) {
                lock.lock();
                try {
                    getCompositeData0(map);
                    reset();
                } finally {
                    lock.unlock();
                }
            } else {
                getCompositeData0(map);                
            }

            return new CompositeDataSupport(getCompositeType(), map);
        }

        private void getCompositeData0(Map<String, Object> map) {
            map.put("Name", getName());
            map.put("ParentName", getParentName());
            map.put("Type", getType().name());
            map.put("TimeMillis", getMillis());
            map.put("TimeMillisTotal", getMillisTotal());
            map.put("ErrorCount", getErrorCount());
            map.put("Count", getCount());
        }
    }
}
