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
import com.alibaba.dragoon.stat.Histogram;

/**
 * 发送方状态类
 * 
 * @author chriszhang
 */
public class NapoliSenderStat implements Serializable {

    public NapoliSenderStat(NapoliSenderStat.Key key){
        this.key = key;
    }

    /**
     * 
     */
    private static final long    serialVersionUID           = 1241904658986939923L;

    /**
     * 发送成功消息个数
     */
    private final AtomicLong     sendSuccessCount           = new AtomicLong();

    /**
     * 发送失败消息个数
     */
    private final AtomicLong     sendFailCount              = new AtomicLong();

    /**
     * 重新发送成功次数
     */
    private final AtomicLong     resendSuccessCount         = new AtomicLong();

    /**
     * 发送失败不存本地的个数
     */
    private final AtomicLong     sendFalseCount             = new AtomicLong();

    /**
     * 本地BDB消息存储个数
     */
    private final AtomicLong     localStoreCount            = new AtomicLong();

    /**
     * 重新发送失败次数
     */
    private final AtomicLong     resendFailureCount         = new AtomicLong();

    /**
     * 存储每一个时间段中的统计次数
     */
    private Histogram            send_to_mq_count_time_List = null;

    /**
     * 存储每个size段中得统计次数
     */
    private Histogram            data_size_range_list       = null;

    /**
     * stat标识
     */
    private NapoliSenderStat.Key key                        = null;

    {
        long SECONDS = 1000;
        long MINUTES = 1000 * 60;
        long HOURS = 1000 * 60 * 60;
        long[] ranges = new long[] { 2, 5, 10, 20, 50,
            100, //
            200, 500, 1 * SECONDS, 2 * SECONDS, 5 * SECONDS, 10 * SECONDS, 30 * SECONDS, 1 * MINUTES, 2 * MINUTES,
            5 * MINUTES, 10 * MINUTES, 30 * MINUTES, 1 * HOURS };
        send_to_mq_count_time_List = new Histogram(ranges);
    }

    {
        long KB = 1000;
        long[] Data_Size_ranges = new long[] { 1 * KB, 2 * KB, 5 * KB };
        data_size_range_list = new Histogram(Data_Size_ranges);
    }

    /**
     * queue标识类 类NapoliSenderStat.java的实现描述：TODO 类实现描述
     * 
     * @author chriszhang Aug 19, 2011 9:15:26 AM
     */
    public static class Key {

        public Key(String sendIP, String hostName, String srcApp, String queueName, String queueAddress){
            this.sendIP = sendIP;
            this.queueName = queueName;
            this.srcApp = srcApp;
            this.hostName = hostName;
            this.queueAddress = queueAddress;
        }

        private String sendIP;

        private String queueName;

        private String srcApp;

        private String hostName;

        private String queueAddress;

        public String getSendIP() {
            return sendIP;
        }

        public void setSendIP(String sendIP) {
            this.sendIP = sendIP;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getSrcApp() {
            return srcApp;
        }

        public void setSrcApp(String srcApp) {
            this.srcApp = srcApp;
        }

        public String getQueueAddress() {
            return queueAddress;
        }

        public void setQueueAddress(String queueAddress) {
            this.queueAddress = queueAddress;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
            result = prime * result + ((queueAddress == null) ? 0 : queueAddress.hashCode());
            result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
            result = prime * result + ((sendIP == null) ? 0 : sendIP.hashCode());
            result = prime * result + ((srcApp == null) ? 0 : srcApp.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Key other = (Key) obj;
            if (hostName == null) {
                if (other.hostName != null) return false;
            } else if (!hostName.equals(other.hostName)) return false;
            if (queueAddress == null) {
                if (other.queueAddress != null) return false;
            } else if (!queueAddress.equals(other.queueAddress)) return false;
            if (queueName == null) {
                if (other.queueName != null) return false;
            } else if (!queueName.equals(other.queueName)) return false;
            if (sendIP == null) {
                if (other.sendIP != null) return false;
            } else if (!sendIP.equals(other.sendIP)) return false;
            if (srcApp == null) {
                if (other.srcApp != null) return false;
            } else if (!srcApp.equals(other.srcApp)) return false;
            return true;
        }

    }

    public void sendFalse(long msgSize, long dealTime) {
        recordDataSize(msgSize);
        recordSendToMQ_Count_Time_List(dealTime);
        incrementSendFalseCount();
    }

    private void incrementSendFalseCount() {
        sendFalseCount.incrementAndGet();
    }

    public void sendSuccess(long msgSize, long dealTime) {
        incrementSendSuccessCount();
        recordDataSize(msgSize);
        recordSendToMQ_Count_Time_List(dealTime);
    }

    public void sendFailure(long msgSize, long dealTime) {
        incrementSendFailCount();
        recordDataSize(msgSize);
        recordSendToMQ_Count_Time_List(dealTime);
    }

    public void resendSuccess(long msgSize, long dealTime) {
        incrementResendSuccessCount();
        recordDataSize(msgSize);
        recordSendToMQ_Count_Time_List(dealTime);

    }

    private void incrementResendSuccessCount() {
        resendSuccessCount.incrementAndGet();

    }

    public void resendFailure(long msgSize, long dealTime) {
        incrementResendFailCount();
        recordDataSize(msgSize);
        recordSendToMQ_Count_Time_List(dealTime);
    }

    private void incrementResendFailCount() {
        resendFailureCount.incrementAndGet();

    }

    /**
     * 记录发送时间，单位毫秒
     * 
     * @param millis
     */
    public void recordSendToMQ_Count_Time_List(long millis) {
        send_to_mq_count_time_List.recode(millis);
    }

    public Histogram getSendToMQ_Count_Time_List() {
        return send_to_mq_count_time_List;
    }

    /**
     * 记录数据包大小
     * 
     * @param bytes 单位 byte
     */
    public void recordDataSize(long bytes) {
        data_size_range_list.recode(bytes);
    }

    public Histogram getDataSizeList() {
        return data_size_range_list;
    }

    public void incrementSendSuccessCount() {
        sendSuccessCount.incrementAndGet();
    }

    public void incrementSendFailCount() {
        sendFailCount.incrementAndGet();

    }

    public void incrementStoreCount() {
        localStoreCount.incrementAndGet();
    }

    public void setLocalStoreCount(long newValue) {
        localStoreCount.set(newValue);
    }

    public void decrementStoreCount() {
        localStoreCount.decrementAndGet();
    }

    public long getSendSuccessCount() {
        return StatUtils.get(sendSuccessCount);
    }

    public long getSendFailCount() {
        return StatUtils.get(sendFailCount);
    }

    public long getLocalStoreCount() {
        return StatUtils.get(localStoreCount);
    }
    
    public void reset() {
        this.localStoreCount.set(0);
        this.sendFailCount.set(0);
        this.sendFalseCount.set(0);
        this.sendSuccessCount.set(0);
    }

    public static CompositeType getCompositeType() throws JMException {
        OpenType<?>[] indexTypes = new OpenType<?>[] { //
        SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,//
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, // count
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, // count
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, // count
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, // count
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, // count
                SimpleType.LONG, SimpleType.LONG };

        String[] indexNames = {//
        "QueueName", "QueueAddress", "SendFalseCount", "ResendSuccessCount", "ResendFailCount",//
                "SendSuccessCount", "SendFailCount", "LocalStoreCount", "Deal_T_1", "Deal_T_2",//
                "Deal_T_3", "Deal_T_4", "Deal_T_5", "Deal_T_6", "Deal_T_7",//
                "Deal_T_8", "Deal_T_9", "Deal_T_10", "Deal_T_11", "Deal_T_12",//
                "Deal_T_13", "Deal_T_14", "Deal_T_15", "Deal_T_16", "Deal_T_17",//
                "Deal_T_18", "Deal_T_19", "Deal_T_20", "Data_Size_1", "Data_Size_2",//
                "Data_Size_3", "Data_Size_4" };
        String[] indexDescriptions = indexNames;
        return new CompositeType("NapoliSenderStats", "Napoli sender Stats", indexNames, indexDescriptions, indexTypes);
    }

    public CompositeDataSupport toCompositeData() throws JMException {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("QueueName", this.key.queueName);
        map.put("QueueAddress", this.key.queueAddress);

        map.put("SendFalseCount", StatUtils.get(sendFalseCount));
        map.put("ResendSuccessCount", StatUtils.get(resendSuccessCount));
        map.put("ResendFailCount", StatUtils.get(resendFailureCount));
        map.put("SendSuccessCount", StatUtils.get(sendSuccessCount));
        map.put("SendFailCount", StatUtils.get(sendFailCount));
        map.put("LocalStoreCount", StatUtils.get(localStoreCount));
        
        long[] arrs1 = send_to_mq_count_time_List.toArray();
        for (int i = 1; i <= arrs1.length; i++) {
            map.put("Deal_T_" + i, arrs1[i - 1]);
        }
        long[] arrs2 = data_size_range_list.toArray();
        for (int i = 1; i <= arrs2.length; i++) {
            map.put("Data_Size_" + i, arrs2[i - 1]);
        }
        
        return new CompositeDataSupport(getCompositeType(), map);
    }

    public NapoliSenderStat.Key getKey() {
        return key;
    }

    public void setKey(NapoliSenderStat.Key key) {
        this.key = key;
    }

    public long getSendFalseCount() {
        return StatUtils.get(sendFalseCount);
    }

}
