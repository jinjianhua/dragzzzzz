package com.alibaba.dragoon.stat.napoli;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.client.DragoonClient;
import com.alibaba.dragoon.stat.Profiler.EntryStatistic;

/**
 * 发送端，接收端stat统一管理类 类NapoliStatManager.java的实现描述：TODO 类实现描述
 * 
 * @author chriszhang Aug 18, 2011 9:16:14 AM
 */
public class NapoliStatManager implements NapoliStatManagerMBean {

    private final static NapoliStatManager                                  instance           = new NapoliStatManager();

    private final static Log                                                LOG                = LogFactory.getLog(NapoliStatManager.class);

    private static String                                                   hostName           = null;

    /**
     * 本机ip
     */
    private static String                                                   nativeIp           = null;

    private final AtomicLong                                                senderResetCount   = new AtomicLong();

    private final AtomicLong                                                receiverResetCount = new AtomicLong();

    static {
        try {
            hostName = getNativeHostName();

            nativeIp = getIP();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 接收端状态容器，其中一个<k,v>代表一个receiver
     */
    private final ConcurrentMap<NapoliReceiverStat.Key, NapoliReceiverStat> receiverStats      = new ConcurrentHashMap<NapoliReceiverStat.Key, NapoliReceiverStat>();

    /**
     * 发送端状态容器，其中一个<k,v>代表一个sender
     */
    private final ConcurrentMap<NapoliSenderStat.Key, NapoliSenderStat>     senderStats        = new ConcurrentHashMap<NapoliSenderStat.Key, NapoliSenderStat>();

    public static final NapoliStatManager getInstance() {
        return instance;
    }

    public static final String getNativeHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
    }

    public NapoliSenderStat getSenderStat(String queueAddress, String queueName) {
        NapoliSenderStat.Key key = new NapoliSenderStat.Key(nativeIp, hostName, getAppNumber(), queueName, queueAddress);
        NapoliSenderStat stat = senderStats.get(key);
        if (stat == null) {
            senderStats.putIfAbsent(key, new NapoliSenderStat(key));
            stat = senderStats.get(key);
        }
        return stat;
    }

    public NapoliReceiverStat getReceiverStat(String srcIp, String srcHostname, String srcApp, String queueAddress,
                                              String queueName) {
        if (srcHostname != null) {
            int index = srcHostname.indexOf('.');
            if (index != -1) {
                srcHostname = srcHostname.substring(0, index);
            }

            if (srcHostname.length() > 32) {
                srcHostname = srcHostname.substring(0, 31);
            }
        }
        NapoliReceiverStat.Key queueIdentity = new NapoliReceiverStat.Key(srcIp, srcHostname, srcApp, queueAddress,
                                                                          queueName);
        NapoliReceiverStat stat = receiverStats.get(queueIdentity);
        if (stat == null) {
            receiverStats.putIfAbsent(queueIdentity, new NapoliReceiverStat(queueIdentity));
            stat = receiverStats.get(queueIdentity);
        }
        return stat;
    }

    public static String getHostName() {
        return hostName;
    }

    public TabularData getSenderStats() throws JMException {
        CompositeType rowType = NapoliSenderStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);
        TabularType tabularType = new TabularType("NapoliSenderStats", "Napoli sender Stats", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);
        for (Map.Entry<NapoliSenderStat.Key, NapoliSenderStat> entry : senderStats.entrySet()) {
            NapoliSenderStat senderStat = entry.getValue();
            data.put(senderStat.toCompositeData());
        }
        return data;
    }

    public TabularData getReceiverStats() throws JMException {
        CompositeType rowType = NapoliReceiverStat.getCompositeType();
        String[] indexNames = rowType.keySet().toArray(new String[rowType.keySet().size()]);
        TabularType tabularType = new TabularType("NapoliReceiverStats", "Napoli receiver Stats", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);
        for (Map.Entry<NapoliReceiverStat.Key, NapoliReceiverStat> entry : receiverStats.entrySet()) {
            NapoliReceiverStat receiverStat = entry.getValue();
            data.put(receiverStat.toCompositeData());
        }
        return data;
    }

    public void senderReset() throws JMException {
        for (Map.Entry<NapoliSenderStat.Key, NapoliSenderStat> entry : senderStats.entrySet()) {
            entry.getValue().reset();
            entry.getValue().getDataSizeList().reset();
            entry.getValue().getSendToMQ_Count_Time_List().reset();
        }
        senderResetCount.decrementAndGet();
    }

    public void receiverReset() throws JMException {
        for (Map.Entry<NapoliReceiverStat.Key, NapoliReceiverStat> entry : receiverStats.entrySet()) {
            entry.getValue().reset();

            Map<String, EntryStatistic> profileMap = entry.getValue().getDealProfile();
            for (Map.Entry<String, EntryStatistic> profileEntry : profileMap.entrySet()) {
                profileEntry.getValue().reset();
            }
        }
        receiverResetCount.incrementAndGet();
    }

    public static final String getIP() {
        List<String> list = new ArrayList<String>();

        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            if (e == null) {
                return "127.0.0.1";
            }
            
            while (e.hasMoreElements()) {
                NetworkInterface item = e.nextElement();
                
                if (item == null) {
                    continue;
                }
                
                if (item.isLoopback()) {
                    continue;
                }

                if (item.isVirtual()) {
                    continue;
                }

                if (!item.isUp()) {
                    continue;
                }

                for (InterfaceAddress address : item.getInterfaceAddresses()) {
                    if (address == null) {
                        continue;
                    }
                    
                    if (address.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) address.getAddress();

                        list.add(inet4Address.getHostAddress());
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        
        if (list.size() == 0) {
            return "127.0.0.1";
        }

        return list.get(0);
    }

    public static String getNativeIp() {
        return nativeIp;
    }

    public static void setNativeIp(String nativeIp) {
        NapoliStatManager.nativeIp = nativeIp;
    }

    public final static String getAppNumber() {
        DragoonClient client = DragoonClient.getInstance();
        if (client != null) {
            return client.getAppNumber();
        }

        return null;
    }

    public long getSenderResetCount() throws JMException {
        return senderResetCount.get();
    }

    public long getReceiverResetCount() throws JMException {
        return receiverResetCount.get();
    }

}
