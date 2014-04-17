/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.dragoon.common.daemon.filter.DaemonServicLifeCycleFilter;
import com.alibaba.dragoon.common.daemon.filter.DaemonServiceLifeCycleFilterChain;

public abstract class DaemonService implements DaemonServiceMBean {

    private DaemonServiceConfig config;

    protected final Lock        lock            = new ReentrantLock();

    private final Condition     startedConditon = lock.newCondition();
    private final Condition     stoppedConditon = lock.newCondition();

    private volatile State      state           = State.NotRunning;

    private volatile boolean    inited          = false;
    private Date                startDate;

    public DaemonService(){
        config = new DaemonServiceConfig();
    }

    public DaemonService(DaemonServiceConfig config){
        this.config = config;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void init() {
        lock.lock();
        try {
            for (DaemonServicLifeCycleFilter filter : config.getFilters()) {
                filter.init(this);
            }
            inited = true;
        } finally {
            lock.unlock();
        }
    }

    public boolean start() {
        lock.lock();
        try {
            startDate = new Date();

            if (!inited) {
                init();
            }

            if (state == State.Running) {
                return false;
            }

            createLifeCycleChain().start();

            state = State.Running;
            startedConditon.signalAll();

            return true;
        } finally {
            lock.unlock();
        }
    }

    protected DaemonServiceLifeCycleFilterChain createLifeCycleChain() {
        return new DaemonServiceLifeCycleFilterChainImpl(this);
    }

    protected void startInternal() {

    }

    public void waitForRunning(long time, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            if (state == State.Running) {
                return;
            }

            startedConditon.await(time, unit);
        } finally {
            lock.unlock();
        }
    }

    public boolean stop() {
        lock.lock();
        try {
            if (state == State.NotRunning) {
                return false;
            }

            createLifeCycleChain().stop();

            this.stopInternal();

            state = State.NotRunning;
            stoppedConditon.signalAll();

            return true;
        } finally {
            lock.unlock();
        }
    }

    protected void stopInternal() {

    }

    public void waitForNotRunning() throws InterruptedException {
        lock.lock();
        try {
            if (state == State.NotRunning) {
                return;
            }

            stoppedConditon.await();
        } finally {
            lock.unlock();
        }
    }

    public void waitForNotRunning(long time, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            if (state == State.NotRunning) {
                return;
            }

            stoppedConditon.await(time, unit);
        } finally {
            lock.unlock();
        }
    }

    public String getState() {
        return state.name();
    }

    public abstract String getName();

    public abstract int getMajorVersion();

    public abstract int getMinorVersion();

    public String getFullName() {
        return getName() + " " + getMajorVersion() + "." + getMinorVersion();
    }

    public DaemonServiceConfig getConfig() {
        return config;
    }

    public enum State {
        NotRunning, Running
    }

    public class DaemonServiceLifeCycleFilterChainImpl implements DaemonServiceLifeCycleFilterChain {

        private DaemonService server;

        private int           pos = 0;

        public DaemonServiceLifeCycleFilterChainImpl(DaemonService server){
            super();
            this.server = server;
        }

        public DaemonService getServer() {
            return server;
        }

        public void start() {
            if (pos < getFilters().size()) {
                nextFilter().start(this);
                return;
            }

            server.startInternal();
        }

        public void stop() {
            if (pos < getFilters().size()) {
                nextFilter().stop(this);
                return;
            }

            server.stopInternal();
        }

        public DaemonServicLifeCycleFilter nextFilter() {
            return server.getConfig().getFilters().get(pos++);

        }

        public List<DaemonServicLifeCycleFilter> getFilters() {
            return server.getConfig().getFilters();
        }
    }
}
