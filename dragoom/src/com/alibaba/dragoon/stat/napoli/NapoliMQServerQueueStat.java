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
 * 已QueueName为单位的状态统计类 类NapoliMQServerQStat.java的实现描述：TODO 类实现描述
 * 
 * @author chriszhang Sep 20, 2011 3:02:31 PM
 */
public class NapoliMQServerQueueStat implements Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 354047121727993484L;

    private Key               key;

    // 域名 MQ名称 MQIP 队列名称 接收个数 消费个数 累积个数 队列负责人

    public static class Key {

        private String domainName;

        private String mqServerName;

        private String mqServerIP;

        private String queueName;

        public Key(String domainName, String mqName, String mqIP, String queueName){
            super();
            this.domainName = domainName;
            this.mqServerName = mqName;
            this.mqServerIP = mqIP;
            this.queueName = queueName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
            result = prime * result + ((mqServerIP == null) ? 0 : mqServerIP.hashCode());
            result = prime * result + ((mqServerName == null) ? 0 : mqServerName.hashCode());
            result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (domainName == null) {
                if (other.domainName != null) {
                    return false;
                }
            } else if (!domainName.equals(other.domainName)) {
                return false;
            }
            if (mqServerIP == null) {
                if (other.mqServerIP != null) {
                    return false;
                }
            } else if (!mqServerIP.equals(other.mqServerIP)) {
                return false;
            }
            if (mqServerName == null) {
                if (other.mqServerName != null) {
                    return false;
                }
            } else if (!mqServerName.equals(other.mqServerName)) {
                return false;
            }
            if (queueName == null) {
                if (other.queueName != null) {
                    return false;
                }
            } else if (!queueName.equals(other.queueName)) {
                return false;
            }
            return true;
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

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

    }

    // 域名 MQ名称 MQIP 队列名称 接收个数 消费个数 累积个数 队列负责人

    private AtomicLong                        receiveCount    = new AtomicLong();

    private AtomicLong                        dealCount       = new AtomicLong();

    private AtomicLong                        localStoreCount = new AtomicLong();

    private AtomicLong                        consumerCount   = new AtomicLong();

    // 用语差值采集的实现
    private transient NapoliMQServerQueueStat last            = null;

    public NapoliMQServerQueueStat(NapoliMQServerQueueStat other){
        this.key = other.key;
        this.receiveCount.set(other.receiveCount.get());
        this.dealCount.set(other.dealCount.get());
        this.localStoreCount.set(other.localStoreCount.get());
        this.consumerCount.set(other.consumerCount.get());
    }

    public NapoliMQServerQueueStat clone() {
        return new NapoliMQServerQueueStat(this);
    }

    public long getConsumerCount() {
        return consumerCount.get();
    }

    private void setConsumentCount(long consumerCount) {
        this.consumerCount.set(consumerCount);
    }

    public long getReceiveCount() {
        return receiveCount.get();
    }

    private void setReceiveCount(long receiveCount) {
        this.receiveCount.set(receiveCount);
    }

    public long getDealCount() {
        return dealCount.get();
    }

    private void setDealCount(long dealCount) {
        this.dealCount.set(dealCount);
    }

    public long getLocalStoreCount() {
        return localStoreCount.get();
    }

    private void setLocalStoreCount(long localStoreCount) {
        this.localStoreCount.set(localStoreCount);
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public NapoliMQServerQueueStat(NapoliMQServerQueueStat.Key key){
        this.key = key;
    }

    /**
     * 队列发送信息记录
     * 
     * @param enqueueCount 入列数目
     * @param dequeueCount 出列数目
     * @param queueSize 队列大小
     */
    public void sendQueueInfo(long enqueueCount, long dequeueCount, long queueSize, long consumerCount) {
        this.setReceiveCount(enqueueCount);
        this.setDealCount(dequeueCount);
        this.setLocalStoreCount(queueSize);
        this.setConsumentCount(consumerCount);
    }

    public static CompositeType getCompositeType() throws JMException {
        // 域名 MQ名称 MQIP 队列名称 接收个数 消费个数 累积个数 队列负责人
        OpenType<?>[] indexTypes = new OpenType<?>[] { //
        SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, SimpleType.LONG //
        };

        String[] indexNames = {//
        "DomainName", "MQServerName", "MQServerIP", "QueueName", "ReceiveCount", "DealCount", "LocalStoreCount",
                "ConsumerCount" };
        String[] indexDescriptions = indexNames;
        return new CompositeType("NapoliMQServerQStats", "Napoli MQServer Q Stats", indexNames, indexDescriptions,
                                 indexTypes);
    }

    public CompositeDataSupport toCompositeData() throws JMException {
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("DomainName", key.domainName);
        map.put("MQServerName", key.mqServerName);
        map.put("MQServerIP", key.mqServerIP);
        map.put("QueueName", key.queueName);
        map.put("LocalStoreCount", localStoreCount.get());
        map.put("ConsumerCount", consumerCount.get());

        NapoliMQServerQueueStat current = this;
        if (StatUtils.isRequiredReset()) {
            current = new NapoliMQServerQueueStat(this);
            if (last == null) {
                map.put("ReceiveCount", current.receiveCount.get());
                map.put("DealCount", current.dealCount.get());

            } else {
                map.put("ReceiveCount", current.receiveCount.get() - last.receiveCount.get());
                map.put("DealCount", current.dealCount.get() - last.dealCount.get());
            }
            last = current;
        } else {
            map.put("ReceiveCount", current.receiveCount.get());
            map.put("DealCount", current.dealCount.get());
        }

        return new CompositeDataSupport(getCompositeType(), map);
    }

}
