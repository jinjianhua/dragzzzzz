/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.kv;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.stat.KVStatManager;

/**
 * Comment of KVInfoStat
 * 
 * @author lijun.cailj
 */
public class KVItem implements Serializable {

    private static final long    serialVersionUID = -3281975300960337030L;

    public final static Log      log              = LogFactory.getLog(KVItem.class);
    private static KVStatManager kvStatManager    = KVStatManager.getInstance();
    public final static int      KV_SIZE          = 1024;

    private Map<String, Object>  kvMap;

    public KVItem(){
        kvMap = new HashMap<String, Object>();
    }

    private void copyMap(Map<String, Object> srcMap, Map<String, Object> destMap) {
        if (srcMap == null || destMap == null) {
            return;
        }

        for (String key : srcMap.keySet()) {
            destMap.put(key, srcMap.get(key));
        }
    }

    /**
     * add self to JMX
     */
    public synchronized void submit() {

        if (kvMap.isEmpty()) {
            return;
        }

        Map<String, Object> destMap = new HashMap<String, Object>();
        copyMap(kvMap, destMap);
        kvMap.clear();

        kvStatManager.add(destMap);
    }

    /*
     * get internal map which hold key-value pairs for this item
     */
    public Map<String, Object> getInternalMap() {
        return kvMap;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        kvMap.clear();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return kvMap.containsKey(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return kvMap.containsValue(value);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return kvMap.entrySet();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return kvMap.get(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return kvMap.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return kvMap.keySet();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        return kvMap.remove(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return kvMap.size();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<Object> values() {
        return kvMap.values();
    }

    /**
     * warning : put(null,Object) or put (String, null) are not be supported.
     */
    public Object put(String key, Object value) {
        if (key == null || value == null) {
            Exception e = new IllegalArgumentException("not be supported, key or value is null.");
            log.error(e.getMessage(), e);
            return null;
        }

        if (isVaildClassType(value.getClass()) && kvMap.size() < KV_SIZE) {
            return kvMap.put(key, value);
        }
        return null;
    }

    public static boolean isVaildClassType(Class<?> type) {
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class
            || type == boolean.class || type == Boolean.class || type == float.class || type == Float.class
            || type == double.class || type == Double.class || type == String.class || type == Date.class
            || type == BigDecimal.class) {
            return true;
        }

        Exception e = new IllegalArgumentException("not be supported, class type:" + type);
        log.error(e.getMessage(), e);

        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return kvMap.toString();
    }

}
