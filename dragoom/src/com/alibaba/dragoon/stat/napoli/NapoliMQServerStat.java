package com.alibaba.dragoon.stat.napoli;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import com.alibaba.dragoon.common.utils.StatUtils;

/**
 * 已MQ Sever Name 为单位的MQ Server状态统计 类NapoliMQServerStat.java的实现描述：TODO 类实现描述
 * 
 * @author chriszhang Sep 20, 2011 3:23:13 PM
 */
public class NapoliMQServerStat implements Serializable {

    /**
     * 
     */
    private static final long            serialVersionUID = 1753175208517567940L;

    private Key                          key;

    // 域名 MQ名称 MQIP 接收个数 消费个数 累积个数 MQ负责人

    private final AtomicLong             receiveCount     = new AtomicLong();

    private final AtomicLong             dealCount        = new AtomicLong();

    private final AtomicLong             localStoreCount  = new AtomicLong();

    private final AtomicLong             consumerCount    = new AtomicLong();

    private transient NapoliMQServerStat last;

    public NapoliMQServerStat(NapoliMQServerStat other) {
        this.receiveCount.set(other.receiveCount.get());
        this.dealCount.set(other.dealCount.get());
        this.localStoreCount.set(other.localStoreCount.get());
        this.consumerCount.set(other.consumerCount.get());
    }

    public NapoliMQServerStat(NapoliMQServerStat.Key key) {
        this.key = key;
    }

    public long getConsumerCount() {
        return consumerCount.get();
    }

    public void setConsumerCount(long consumerCount) {
        this.consumerCount.set(consumerCount);
    }

    public static class Key {

        // 域名 MQ名称 MQIP 接收个数 消费个数 累积个数 MQ负责人
        private String domainName;
        private String mqServerName;
        private String mqServerIP;

        public Key(String domainName, String mqName, String mqIP) {
            this.domainName = domainName;
            this.mqServerName = mqName;
            this.mqServerIP = mqIP;
        }

        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        public String getMqServerName() {
            return mqServerName;
        }

        public void setMqServerName(String mqServerName) {
            this.mqServerName = mqServerName;
        }

        public String getMqServerIP() {
            return mqServerIP;
        }

        public void setMqServerIP(String mqServerIP) {
            this.mqServerIP = mqServerIP;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
            result = prime * result + ((mqServerIP == null) ? 0 : mqServerIP.hashCode());
            result = prime * result + ((mqServerName == null) ? 0 : mqServerName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (domainName == null) {
                if (other.domainName != null)
                    return false;
            } else if (!domainName.equals(other.domainName))
                return false;
            if (mqServerIP == null) {
                if (other.mqServerIP != null)
                    return false;
            } else if (!mqServerIP.equals(other.mqServerIP))
                return false;
            if (mqServerName == null) {
                if (other.mqServerName != null)
                    return false;
            } else if (!mqServerName.equals(other.mqServerName))
                return false;
            return true;
        }

    }

    public long getReceiveCount() {
        return this.receiveCount.get();
    }

    public void setReceiveCount(long receiveCount) {
        this.receiveCount.set(receiveCount);
    }

    public long getDealCount() {
        return this.dealCount.get();
    }

    public void setDealCount(long dealCount) {
        this.dealCount.set(dealCount);
    }

    public long getLocalStoreCount() {
        return this.localStoreCount.get();
    }

    public void setLocalStoreCount(long localStoreCount) {
        this.localStoreCount.set(localStoreCount);
    }

    public void sendMQInfo(long totalEnqueueCount, long totalDequeueCount, long messageSize,
                           long consumerCount) {
        this.setDealCount(totalDequeueCount);
        this.setLocalStoreCount(messageSize);
        this.setReceiveCount(totalEnqueueCount);
        this.setConsumerCount(consumerCount);
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public static CompositeType getCompositeType() throws JMException {
        // 域名 MQ名称 MQIP 队列名称 接收个数 消费个数 累积个数 队列负责人
        OpenType<?>[] indexTypes = new OpenType<?>[] { //
        SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.LONG //
        };

        String[] indexNames = {//
        "DomainName", "MQServerName", "MQServerIP", "ReceiveCount", "DealCount", "LocalStoreCount",
                "ConsumerCount" };
        String[] indexDescriptions = indexNames;
        return new CompositeType("NapoliMQServerStats", "Napoli MQServer Stats", indexNames,
                indexDescriptions, indexTypes);
    }

    public CompositeDataSupport toCompositeData() throws JMException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("DomainName", key.domainName);
        map.put("MQServerName", key.mqServerName);
        map.put("MQServerIP", key.mqServerIP);
        map.put("LocalStoreCount", localStoreCount.get());
        map.put("ConsumerCount", consumerCount.get());
        if (StatUtils.isRequiredReset()) {
            NapoliMQServerStat current = new NapoliMQServerStat(this);

            if (last == null) {
                map.put("ReceiveCount", current.receiveCount.get());
                map.put("DealCount", current.dealCount.get());
            } else {
                map.put("ReceiveCount", current.receiveCount.get() - last.receiveCount.get());
                map.put("DealCount", current.dealCount.get() - last.dealCount.get());
            }

            last = current;
        } else {
            map.put("ReceiveCount", this.receiveCount.get());
            map.put("DealCount", this.dealCount.get());
        }

        return new CompositeDataSupport(getCompositeType(), map);
    }
}
