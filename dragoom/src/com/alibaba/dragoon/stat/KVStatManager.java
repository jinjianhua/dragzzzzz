/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.patrol.kv.KVItem;
import com.alibaba.dragoon.patrol.kv.KVTask;

public class KVStatManager implements KVStatManagerMBean, Serializable {

    private static final long   serialVersionUID = 2298927362473158215L;
    public final static Log     log              = LogFactory.getLog(KVItem.class);

    public static KVStatManager instance         = new KVStatManager();

    public final static int     KV_LIST_SIZE     = 1024;

    public static final KVStatManager getInstance() {
        return instance;
    }

    private final AtomicLong          resetCount = new AtomicLong();

    private List<Map<String, Object>> kvList     = new ArrayList<Map<String, Object>>();

    private long                      lastChangedTime;

    private KVTask                    kvTask;

    private KVStatManager(){
    }

    public long getResetCount() {
        return resetCount.get();
    }

    public void reset() {
        resetCount.incrementAndGet();

        lastChangedTime = 0;

        kvList.clear();
    }

    public Date getLastErrorTime() {
        if (lastChangedTime <= 0) {
            return null;
        }
        return new Date(lastChangedTime);
    }

    public void setLastChangedTime(long lastChangedTime) {
        this.lastChangedTime = lastChangedTime;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized void add(Map kvMap) {
        if (kvMap == null || kvMap.isEmpty()) {
            return;
        }

        lastChangedTime = new Date().getTime();

        if (kvList.size() == KV_LIST_SIZE) {
            kvList.remove(0);
        }
        kvList.add(kvMap);
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.dragoon.stat.KVStatManagerMBean#getDataCount()
     */
    public long getDataCount() {
        return kvList.size();
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.dragoon.stat.KVStatManagerMBean#getLastChangedTime()
     */
    public Date getLastChangedTime() {
        return new Date(lastChangedTime);
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.dragoon.stat.KVStatManagerMBean#getKVList()
     */
    public List<Map<String, Object>> getKVList() throws JMException {

        List<Map<String, Object>> itemsFromTask = new ArrayList<Map<String, Object>>();

        String kvTaskClassName = System.getProperty(KVTask.KV_TASK_KEY);

        if (kvTaskClassName != null && kvTaskClassName.trim().length() > 0) {
            try {
                KVTask task = null;

                // use cache to avoid construct an instance every time
                if (kvTask != null && kvTask.getClass().getName().equals(kvTaskClassName)) {
                    task = kvTask;
                } else {
                    @SuppressWarnings("rawtypes")
                    Class clazz = Class.forName(kvTaskClassName);
                    Object instance = clazz.newInstance();
                    if (!(instance instanceof KVTask)) {
                        log.error(kvTaskClassName + " does not implements KVTask");
                    }
                    task = (KVTask) instance;
                    kvTask = task;
                }

                List<KVItem> kvItems = task.getKVItems();
                int start = kvItems.size() > KV_LIST_SIZE ? kvItems.size() - KV_LIST_SIZE : 0;
                for (int i = start; i < kvItems.size(); i++) {
                    itemsFromTask.add(kvItems.get(i).getInternalMap());
                }
            } catch (ClassNotFoundException e) {
                log.error("can not find class with name:" + kvTaskClassName, e);
            } catch (InstantiationException e) {
                log.error(kvTaskClassName + " can not does not contain a null-argument constructor", e);
            } catch (IllegalAccessException e) {
                log.error("can not access null-argument constructor: ", e);
            } catch (Throwable e) {
                log.error("something wrong happened when execute KVTask: ", e);
            }
        }

        List<Map<String, Object>> allItems = new ArrayList<Map<String, Object>>();
        allItems.addAll(itemsFromTask);
        allItems.addAll(kvList);
        
        if (StatUtils.isRequiredReset()) {
            this.reset();
        }

        return allItems;
    }

}
