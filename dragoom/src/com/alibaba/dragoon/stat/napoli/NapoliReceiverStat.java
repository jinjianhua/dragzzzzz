package com.alibaba.dragoon.stat.napoli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.stat.Histogram;
import com.alibaba.dragoon.stat.Profiler;
import com.alibaba.dragoon.stat.Profiler.EntryStatistic;
import com.alibaba.dragoon.stat.ProfilerEntryVisitor;

/**
 * 接收者状态类
 * 
 * @author chriszhang
 */
public class NapoliReceiverStat implements Serializable {

    /**
     * 
     */
    private static final long                           serialVersionUID     = -8693750965381917502L;

    private final static Log                            LOG                  = LogFactory.getLog(NapoliReceiverStat.class);

    // SRC_IP
    // SRC_HOSTNAME
    // QUEQUE_NAME
    // 消息存活总时间(message_life_time) LONG 从发送方发送到被完全消费的时间（分15个区间，单位nano） 1.4.8
    // 处理消息的时间(deal_message_time) LONG 消费此消息所花费的时间（分15个区间，单位nano） 1.4.8
    // 处理时的方法堆栈(deal_profile) 处理消息的所有方法堆栈，profile 1.4.8
    // 接收的消息总个数(receive_message_num) LONG 从MQ接收到的消息个数 1.4.8
    // 本地BDB消息存储个数(receive_localstore_num) LONG 由于执行出错,转存到本地的消息个数 1.4.8
    // 执行处理的总次数(deal_total_num) LONG 消费消息的总次数，包括执行成功+执行失败+失败后本地存储重试的次数 1.4.8
    /**
     * 消息存活总时间统计容器
     */
    private Histogram                                   liveTimeMillisList   = null;

    /**
     * 处理消息的时间
     */
    private Histogram                                   dealTimeMillisList   = null;

    /**
     * 调用堆栈纪录
     */
    private final ConcurrentMap<String, EntryStatistic> dealProfile          = new ConcurrentHashMap<String, EntryStatistic>();

    private ProfilerEntryVisitor                        profilerEntryVisitor = new ProfilerEntryVisitor(dealProfile);

    /**
     * 本地BDB消息存储个数
     */
    private final AtomicLong                            localStoreCount      = new AtomicLong();

    /**
     * 处理失败次数
     */
    private final AtomicLong                            failureCount         = new AtomicLong();

    /**
     * 执行处理成功的次数
     */
    private final AtomicLong                            dealCount            = new AtomicLong();

    /**
     * 处理失败不存本地的消息个数
     */
    public final AtomicLong                             falseCount           = new AtomicLong();

    /**
     * 重新处理成功的次数
     */
    public final AtomicLong                             redealCount          = new AtomicLong();

    /**
     * 重新处理失败次数
     */
    public final AtomicLong                             refailureCount       = new AtomicLong();

    /**
     * napoli key 唯一标识
     */
    private NapoliReceiverStat.Key                      key                  = null;

    {
        long SECONDS = 1000;
        long MINUTES = SECONDS * 60;
        long HOURS = MINUTES * 60;
        long DAYS = 24 * HOURS;
        long[] ranges = new long[] { 2 * SECONDS, 5 * SECONDS, 10 * SECONDS, 30 * SECONDS, 1 * MINUTES,
            2 * MINUTES, //
            5 * MINUTES, 10 * MINUTES, 30 * MINUTES, 1 * HOURS, 2 * HOURS, 3 * HOURS, 6 * HOURS, 10 * HOURS, 1 * DAYS,
            2 * DAYS, 3 * DAYS, 5 * DAYS, 10 * DAYS };
        liveTimeMillisList = new Histogram(ranges);
    }

    {
        long SECONDS = 1000;
        long MINUTES = 1000 * 60;
        long HOURS = 1000 * 60 * 60;
        long[] ranges = new long[] { 2, 5, 10, 20, 50,
            100, //
            200, 500, 1 * SECONDS, 2 * SECONDS, 5 * SECONDS, 10 * SECONDS, 30 * SECONDS, 1 * MINUTES, 2 * MINUTES,
            5 * MINUTES, 10 * MINUTES, 30 * MINUTES, 1 * HOURS };
        dealTimeMillisList = new Histogram(ranges);
    }

    public NapoliReceiverStat(Key queueIdentity){
        this.key = queueIdentity;
    }

    public long getFalseCount() {
        return StatUtils.get(falseCount);
    }

    public void incrementFalseCount() {
        falseCount.incrementAndGet();
    }

    public long getFailureCount() {
        return StatUtils.get(failureCount);
    }

    public void incrementFailureCount() {
        failureCount.incrementAndGet();
    }

    public void setLocalStoreCount(long newValue) {
        localStoreCount.set(newValue);
    }

    public long getLocalStoreCount() {
        return StatUtils.get(localStoreCount);
    }

    public void decrementLocalStoreCount() {
        this.localStoreCount.decrementAndGet();
    }

    public Histogram getLiveTimeMillisList() {
        return liveTimeMillisList;
    }

    public Histogram getDealTimeMillisList() {
        return dealTimeMillisList;
    }

    public long getDealCount() {
        return StatUtils.get(dealCount);
    }

    public ConcurrentMap<String, EntryStatistic> getDealProfile() {
        return dealProfile;
    }

    public void recordLiveTimeMillis(long millis) {
        liveTimeMillisList.recode(millis);
    }

    public void recordDealTimeMillis(long millis) {
        dealTimeMillisList.recode(millis);
    }

    /**
     * 记录本地BDB消息存储
     */
    public void incrementLocalStoreCount() {
        localStoreCount.incrementAndGet();
    }

    /**
     * 记录执行处理的消息
     */
    public void incrementDealCount() {
        dealCount.incrementAndGet();
    }

    public void recordProfilerInfo(Profiler.Entry profilerEntry) {
        try {
            profilerEntry.accept(this.profilerEntryVisitor);
        } catch (Exception ex) {
            LOG.error("recordProfilerInfo error", ex);
        }
    }

    public void receiveSuccess(long dealTime, long costTime) {

        incrementDealCount();
        recordDealTimeMillis(dealTime);
        recordLiveTimeMillis(costTime);
    }

    public void receiveFailure(long dealTime) {
        incrementFailureCount();
        recordDealTimeMillis(dealTime);
    }

    public void rereceiveSuccess(long dealTime, long costTime) {
        recordDealTimeMillis(dealTime);
        recordLiveTimeMillis(costTime);
        incrementRedealCount();
    }

    private void incrementRedealCount() {
        redealCount.incrementAndGet();

    }

    public void rereceiveFailure(long dealTime) {

        incrementRefailureCount();
        recordDealTimeMillis(dealTime);
    }

    private void incrementRefailureCount() {
        refailureCount.incrementAndGet();

    }

    public void receiveFalse(long dealTime) {
        incrementFalseCount();
        recordDealTimeMillis(dealTime);
    }

    public static class Key {

        private final String ip;
        private final String hostName;
        private final String srcAppNum;
        private final String queueName;
        private final String queueAddress;

        public Key(String ip, String hostName, String srcAppNum, String queueAddress, String queueName){
            super();
            this.ip = ip;
            this.hostName = hostName;
            this.srcAppNum = srcAppNum;
            this.queueName = queueName;
            this.queueAddress = queueAddress;
        }

        public static CompositeType getCompositeType() throws JMException {
            OpenType<?>[] indexTypes = new OpenType<?>[] { //
            SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, //
                    new ArrayType<CompositeType>(1, Profiler.EntryStatistic.getCompositeType()) };

            String[] indexNames = { "SrcHostName", "SrcAppNum", "QueueName", "QueueAddress" };
            String[] indexDescriptions = indexNames;
            return new CompositeType("NapoliReceiverStats", "Napoli reveiver Stats", indexNames, indexDescriptions,
                                     indexTypes);

        }

        public CompositeData toCompositeData() throws JMException {
            HashMap<String, Object> map = new HashMap<String, Object>();

            return new CompositeDataSupport(getCompositeType(), map);
        }

        public String getSourceAppNum() {
            return srcAppNum;
        }

        public String getIp() {
            return ip;
        }

        public String getHostName() {
            return hostName;
        }

        public String getQueueName() {
            return queueName;
        }

        public String getSrcAppNum() {
            return srcAppNum;
        }

        public String getQueueAddress() {
            return queueAddress;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
            result = prime * result + ((ip == null) ? 0 : ip.hashCode());
            result = prime * result + ((queueAddress == null) ? 0 : queueAddress.hashCode());
            result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
            result = prime * result + ((srcAppNum == null) ? 0 : srcAppNum.hashCode());
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
            if (ip == null) {
                if (other.ip != null) return false;
            } else if (!ip.equals(other.ip)) return false;
            if (queueAddress == null) {
                if (other.queueAddress != null) return false;
            } else if (!queueAddress.equals(other.queueAddress)) return false;
            if (queueName == null) {
                if (other.queueName != null) return false;
            } else if (!queueName.equals(other.queueName)) return false;
            if (srcAppNum == null) {
                if (other.srcAppNum != null) return false;
            } else if (!srcAppNum.equals(other.srcAppNum)) return false;
            return true;
        }
    }
    
    public void reset() {
        this.localStoreCount.set(0);
        this.liveTimeMillisList.reset();
        this.dealTimeMillisList.reset();
        this.falseCount.set(0);
        this.dealCount.set(0);
        this.failureCount.set(0);
    }

    public static CompositeType getCompositeType() throws JMException {
        OpenType<?>[] indexTypes = new OpenType<?>[] { //
        SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, //
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,//
                new ArrayType<CompositeType>(1, Profiler.EntryStatistic.getCompositeType()) };

        String[] indexNames = { "SrcHostName", "SrcAppNum", "QueueName", "QueueAddress", "FalseCount", "RedealCount",
                "RefailCount",//
                "LocalStoreCount", "FailureCount", "DealCount", "Deal_T_1", "Deal_T_2",//
                "Deal_T_3", "Deal_T_4", "Deal_T_5", "Deal_T_6", "Deal_T_7",//
                "Deal_T_8", "Deal_T_9", "Deal_T_10", "Deal_T_11", "Deal_T_12",//
                "Deal_T_13", "Deal_T_14", "Deal_T_15", "Deal_T_16", "Deal_T_17",//
                "Deal_T_18", "Deal_T_19", "Deal_T_20", "Live_T_1", "Live_T_2",//
                "Live_T_3", "Live_T_4", "Live_T_5", "Live_T_6", "Live_T_7",//
                "Live_T_8", "Live_T_9", "Live_T_10", "Live_T_11", "Live_T_12",//
                "Live_T_13", "Live_T_14", "Live_T_15", "Live_T_16", "Live_T_17",//
                "Live_T_18", "Live_T_19", "Live_T_20", "ProfileEntries" };
        String[] indexDescriptions = indexNames;
        return new CompositeType("NapoliReceiverStats", "Napoli reveiver Stats", indexNames, indexDescriptions,
                                 indexTypes);

    }

    public CompositeData toCompositeData() throws JMException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("SrcHostName", this.key.hostName);
        map.put("SrcAppNum", this.key.srcAppNum);
        map.put("QueueName", this.key.queueName);
        map.put("QueueAddress", this.key.queueAddress);
        map.put("RedealCount", StatUtils.get(redealCount));
        map.put("RefailCount", StatUtils.get(refailureCount));
        map.put("FalseCount", StatUtils.get(falseCount));
        map.put("LocalStoreCount", StatUtils.get(localStoreCount));
        map.put("FailureCount", StatUtils.get(failureCount));
        map.put("DealCount", StatUtils.get(dealCount));
        long[] arrs1 = dealTimeMillisList.toArray();
        for (int i = 1; i <= arrs1.length; i++) {
            map.put("Deal_T_" + i, arrs1[i-1]);
        }
        long[] arrs2 = liveTimeMillisList.toArray();
        for (int i = 1; i <= arrs2.length; i++) {
            map.put("Live_T_" + i, arrs2[i-1]);
        }
        EntryStatistic[] profilerEntryStatArray = this.dealProfile.values().toArray(new EntryStatistic[this.dealProfile.values().size()]);
        List<CompositeDataSupport> profilerEntryStatCompositeDataList = new ArrayList<CompositeDataSupport>();
        for (int i = 0; i < profilerEntryStatArray.length; ++i) {
            if (profilerEntryStatArray[i].getCount() == 0) {
                continue;
            }
            profilerEntryStatCompositeDataList.add(profilerEntryStatArray[i].getCompositeData());
        }

        CompositeDataSupport[] profilerEntryStatCompositeDataArray = new CompositeDataSupport[profilerEntryStatCompositeDataList.size()];
        profilerEntryStatCompositeDataList.toArray(profilerEntryStatCompositeDataArray);

        map.put("ProfileEntries", profilerEntryStatCompositeDataArray);
        return new CompositeDataSupport(getCompositeType(), map);
    }
}
