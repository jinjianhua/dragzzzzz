package com.alibaba.dragoon.stat;

import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.dragoon.common.utils.StatUtils;

public class TaskStatManager {

    private final static TaskStatManager instance      = new TaskStatManager();

    private final AtomicInteger          concurrentMax = new AtomicInteger();
    private final AtomicInteger          runningCount  = new AtomicInteger();

    public static TaskStatManager getInstance() {
        return instance;
    }

    public void incrementRunningCount() {
        int count = runningCount.incrementAndGet();
        for (;;) {
            int max = concurrentMax.get();
            if (count > max) {
                if (concurrentMax.compareAndSet(max, count)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

    public long getConcurrentMax() {
        return StatUtils.get(concurrentMax);
    }

    public void decrementRunningCount() {
        runningCount.decrementAndGet();
    }

    public int getRunningCount() {
        return runningCount.get();
    }
}
