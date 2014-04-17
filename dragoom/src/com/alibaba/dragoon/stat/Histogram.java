package com.alibaba.dragoon.stat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.dragoon.common.utils.StatUtils;

/**
 * 类RangeList.java的实现描述：TODO 类实现描述
 * 
 * @author chriszhang Aug 18, 2011 5:21:27 PM
 */
public class Histogram {

    private final long[]       ranges;
    private final AtomicLong[] rangeCounters;

    public Histogram(long... ranges){
        this.ranges = ranges;
        rangeCounters = new AtomicLong[ranges.length + 1];
        for (int i = 0; i < rangeCounters.length; i++) {
            rangeCounters[i] = new AtomicLong();
        }
    }

    public Histogram(TimeUnit timeUnit, long... ranges){
        this.ranges = new long[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            this.ranges[i] = TimeUnit.MILLISECONDS.convert(ranges[i], timeUnit);
        }

        rangeCounters = new AtomicLong[ranges.length + 1];
        for (int i = 0; i < rangeCounters.length; ++i) {
            rangeCounters[i] = new AtomicLong();
        }
    }

    public void reset() {
        for (int i = 0; i < rangeCounters.length; i++) {
            rangeCounters[i].set(0);
        }
    }

    public void recode(long millis) {
        int index = rangeCounters.length - 1;
        for (int i = 0; i < ranges.length; ++i) {
            if (millis < ranges[i]) {
                index = i;
                break;
            }
        }

        rangeCounters[index].incrementAndGet();
    }

    public long get(int index) {
        return rangeCounters[index].get();
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < ranges.length; i++) {
            strBuilder.append((i == 0 ? 0l : ranges[i - 1]) + " 到 " + ranges[i] + " : " + rangeCounters[i] + " ");
        }
        return strBuilder.toString();
    }

    public long[] toArray() {
        long[] array = new long[rangeCounters.length];
        for (int i = 0; i < rangeCounters.length; i++) {
            array[i] = StatUtils.get(rangeCounters[i]);
        }
        return array;
    }

    public long[] getRanges() {
        return ranges;
    }
}
