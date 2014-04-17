package com.alibaba.dragoon.common.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatUtils {

    private final static ThreadLocal<Boolean> resetFlag         = new ThreadLocal<Boolean>();
    private static boolean                    systemPropertySet = false;

    public final static void clearRequireResetFlag() {
        resetFlag.set(null);
    }

    public final static void setRequireReset() {
        if (!systemPropertySet) {
            System.getProperties().put("dragoon.stat.resetFlag", resetFlag);
            systemPropertySet = true;
        }
        resetFlag.set(Boolean.TRUE);
    }

    public final static boolean isRequiredReset() {
        Boolean flag = resetFlag.get();
        if (flag == null) {
            return false;
        }

        return flag.booleanValue();
    }

    public static long get(AtomicLong atomic) {
        if (isRequiredReset()) {
            return getAndZero(atomic);
        }

        return atomic.get();
    }

    public static int get(AtomicInteger atomic) {
        if (isRequiredReset()) {
            return getAndZero(atomic);
        }

        return atomic.get();
    }

    public static long getAndZero(AtomicLong atomic) {
        long val = atomic.get();
        for (;;) {
            if (atomic.compareAndSet(val, 0)) {
                break;
            }
            val = atomic.get();
        }
        return val;
    }

    public static int getAndZero(AtomicInteger atomic) {
        int val = atomic.get();
        for (;;) {
            if (atomic.compareAndSet(val, 0)) {
                break;
            }
            val = atomic.get();
        }
        return val;
    }
}
