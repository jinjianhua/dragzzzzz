package com.alibaba.dragoon.stat.napoli;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * MQServer 状态监控 类NapoliMQServerStatManager.java的实现描述：TODO 类实现描述
 * 
 * @author chriszhang Sep 20, 2011 2:59:16 PM
 */
public class NapoliMQServerStatManager implements NapoliMQServerStatManagerMBean {

    private final static NapoliMQServerStatManager                              instance                   = new NapoliMQServerStatManager();

    /**
     * 本机ip
     */
    private static String                                                       nativeIp                   = null;

    private final AtomicLong                                                    serverQueueStatsResetCount = new AtomicLong();

    private final AtomicLong                                                    serverStatsResetCount      = new AtomicLong();

    static {
        nativeIp = NapoliStatManager.getIP();
    }

    /**
     * 已queueName为单位的状态统计
     */
    private ConcurrentMap<NapoliMQServerQueueStat.Key, NapoliMQServerQueueStat> mqServerQueueStats         = new ConcurrentHashMap<NapoliMQServerQueueStat.Key, NapoliMQServerQueueStat>();

    private ConcurrentMap<NapoliMQServerStat.Key, NapoliMQServerStat>           mqServerStats              = new ConcurrentHashMap<NapoliMQServerStat.Key, NapoliMQServerStat>();

    public NapoliMQServerQueueStat getNapoliMQServerQueueStat(String domainName, String mqServerName, String queueName) {

        NapoliMQServerQueueStat.Key key = new NapoliMQServerQueueStat.Key(domainName, mqServerName, nativeIp, queueName);
        NapoliMQServerQueueStat stat = mqServerQueueStats.get(key);
        if (stat == null) {
            mqServerQueueStats.putIfAbsent(key, new NapoliMQServerQueueStat(key));
            stat = mqServerQueueStats.get(key);
        }
        return stat;
    }

    public ConcurrentMap<NapoliMQServerQueueStat.Key, NapoliMQServerQueueStat> getMqServerQueueStats() {
        return mqServerQueueStats;
    }

    public void setMqServerQueueStats(ConcurrentMap<NapoliMQServerQueueStat.Key, NapoliMQServerQueueStat> mqServerQueueStats) {
        this.mqServerQueueStats = mqServerQueueStats;
    }

    public ConcurrentMap<NapoliMQServerStat.Key, NapoliMQServerStat> getMqServerStats() {
        return mqServerStats;
    }

    public void setMqServerStats(ConcurrentMap<NapoliMQServerStat.Key, NapoliMQServerStat> mqServerStats) {
        this.mqServerStats = mqServerStats;
    }

    public NapoliMQServerStat getNapoliMQServerStat(String domainName, String mqServerName) {
        NapoliMQServerStat.Key key = new NapoliMQServerStat.Key(domainName, mqServerName, nativeIp);
        NapoliMQServerStat stat = mqServerStats.get(key);
        if (stat == null) {
            mqServerStats.putIfAbsent(key, new NapoliMQServerStat(key));
            stat = mqServerStats.get(key);
        }
        return stat;
    }

    public static String getNativeIp() {
        return nativeIp;
    }

    private NapoliMQServerStatManager(){

    }

    public static NapoliMQServerStatManager getInstance() {
        return instance;
    }

    public TabularData getNapoliMQServerQueueStats() throws JMException {
        CompositeType rowType = NapoliMQServerQueueStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);
        TabularType tabularType = new TabularType("NapoliMQServerQueueStats", "Napoli MQServer Q Stats", rowType,
                                                  indexNames);
        TabularData data = new TabularDataSupport(tabularType);
        for (Map.Entry<NapoliMQServerQueueStat.Key, NapoliMQServerQueueStat> entry : this.mqServerQueueStats.entrySet()) {
            NapoliMQServerQueueStat mqserverqStat = entry.getValue();
            data.put(mqserverqStat.toCompositeData());
        }
        return data;
    }

    public TabularData getNapoliMQServerStats() throws JMException {
        CompositeType rowType = NapoliMQServerStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);
        TabularType tabularType = new TabularType("NapoliMQServerStats", "Napoli MQServer Stats", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);
        for (Map.Entry<NapoliMQServerStat.Key, NapoliMQServerStat> entry : this.mqServerStats.entrySet()) {
            NapoliMQServerStat mqserverStat = entry.getValue();
            data.put(mqserverStat.toCompositeData());
        }
        return data;
    }

    public void resetServerQueueStats() {
        this.mqServerQueueStats.clear();
        serverQueueStatsResetCount.incrementAndGet();
    }

    public void resetServerStats() {
        this.mqServerStats.clear();
        serverStatsResetCount.incrementAndGet();
    }

    public long getServerQueueStatsResetCount() throws JMException {
        return serverQueueStatsResetCount.get();
    }

    public long getServerStatsResetCount() throws JMException {
        return serverStatsResetCount.get();
    }

}
