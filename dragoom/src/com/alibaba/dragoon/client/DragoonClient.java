/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.VERSION;
import com.alibaba.dragoon.common.daemon.DaemonService;
import com.alibaba.dragoon.common.daemon.DaemonServiceConfig;
import com.alibaba.dragoon.common.daemon.filter.CommonsLogFilter;
import com.alibaba.dragoon.common.protocol.DragoonConnector;
import com.alibaba.dragoon.common.protocol.DragoonSession;
import com.alibaba.dragoon.common.protocol.message.DragoonNotificationMessage;
import com.alibaba.dragoon.common.protocol.message.Heartbeat;
import com.alibaba.dragoon.common.protocol.transport.socket.SocketConnector;
import com.alibaba.dragoon.common.utils.DaemonThreadFactory;
import com.alibaba.dragoon.common.utils.JMXUtils;
import com.alibaba.dragoon.patrol.PatrolUtils;

public class DragoonClient extends DaemonService implements DragoonClientMBean {

    private static DragoonClient                     instance;

    private final static Log                         LOG                      = LogFactory.getLog(DragoonClient.class);

    public final static String                       DEFAULT_HOST             = "127.0.0.1";
    public final static int                          DEFAULT_PORT             = 15777;

    private static boolean                           springStatEnable         = true;
    private static boolean                           jdbcStatEnable           = true;
    private static boolean                           uriStatEnable            = true;
    private static boolean                           log4jStatEnable          = true;
    private static boolean                           napoliStatEnable         = true;                                                                                // added
                                                                                                                                                                      // by
                                                                                                                                                                      // chris
    private static boolean                           napoliMQServerStatEnable = true;

    private final String                             name                     = "dragoon client";

    private DragoonClientConnectorFilter             connectorFilter          = new DragoonClientConnectorFilter();

    private DragoonClientMessageHandler              messageHandler;

    private String                                   appNumber;

    private String                                   appInstNumber;

    private DragoonConnector                         connector;

    private SocketAddress                            agentSocketAddress;

    private DragoonClientStandardMBeanProviderFilter standardMBeanProviderFilter;

    private ScheduledExecutorService                 scheduler                = Executors.newScheduledThreadPool(1,
                                                                                                                 new DaemonThreadFactory(
                                                                                                                                         "DragoonClient-Scheduler"));

    private ScheduledFuture<?>                       heartbeatFuture          = null;

    private final AtomicLong                         heartbeatSentCount       = new AtomicLong();

    private final AtomicLong                         heartbeatSkipCount       = new AtomicLong();

    private final long                               initialDelay             = 0;

    private long                                     heartbeatPeriod          = 60;

    private final TimeUnit                           unit                     = TimeUnit.SECONDS;

    public DragoonClientStandardMBeanProviderFilter getStandardMBeanProviderFilter() {
        return standardMBeanProviderFilter;
    }

    public void setStandardMBeanProviderFilter(DragoonClientStandardMBeanProviderFilter standardMBeanProviderFilter) {
        this.standardMBeanProviderFilter = standardMBeanProviderFilter;
    }

    public DragoonConnector getConnector() {
        return connector;
    }

    public void setConnector(DragoonConnector connector) {
        this.connector = connector;
    }

    public void setAgentSocketAddress(SocketAddress agentSocketAddress) {
        this.agentSocketAddress = agentSocketAddress;
    }

    public SocketAddress getAgentSocketAddress() {
        return agentSocketAddress;
    }

    public DragoonClient(){
        instance = this;
    }

    public static DragoonClient getInstance() {
        return instance;
    }

    public String getAppNumber() {
        return appNumber;
    }

    public void setAppNumber(String appNumber) {
        this.appNumber = appNumber;
    }

    public String getAppInstNumber() {
        return appInstNumber;
    }

    public void setAppInstNumber(String appInstNumber) {
        this.appInstNumber = appInstNumber;
    }

    public DragoonClientConnectorFilter getConnectorFilter() {
        return connectorFilter;
    }

    public void init() {
        try {
            if (agentSocketAddress == null) {
                agentSocketAddress = new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT);
            }

            if (connector == null) {
                connector = new SocketConnector();
            }

            if (connectorFilter.getMessageHandler() == null) {
                connectorFilter.setMessageHandler(messageHandler = new DragoonClientMessageHandler(this, null));
            }

            if (connectorFilter.getConnector() == null) {
                connectorFilter.setConnector(connector);
            }

            if (connectorFilter.getServerAddress() == null) {
                connectorFilter.setServerAddress(agentSocketAddress);
            }

            if (standardMBeanProviderFilter == null) {
                standardMBeanProviderFilter = new DragoonClientStandardMBeanProviderFilter();
            }

            DaemonServiceConfig config = this.getConfig();

            config.setName(getFullName());
            config.getFilters().add(new CommonsLogFilter(LOG));
            config.getFilters().add(connectorFilter);
            config.getFilters().add(standardMBeanProviderFilter);

            super.init();
        } catch (Exception e) {
            LOG.error(this.getFullName() + "init error", e);
        }
    }

    /**
     * start stand-alone with appNumber and default host and port connect to agent
     * 
     * @param appNumber
     */
    public static void start(String appNumber) {
        start(DEFAULT_HOST, DEFAULT_PORT, appNumber, null);
    }

    /**
     * start stand-alone with appNumber 、 instanceNumber and default host and port connect to agent
     * 
     * @param appNumber
     */
    public static void start(String appNumber, String instanceNumber) {
        start(DEFAULT_HOST, DEFAULT_PORT, appNumber, instanceNumber);
    }

    /**
     * start stand-alone with specified host and port connect to agent
     * 
     * @param host
     * @param port
     * @param appNumber
     */
    public static void start(String host, int port, String appNumber) {
        start(host, port, appNumber, null);
    }

    /**
     * start stand-alone with specified host and port connect to agent. uriStatEnable is false.
     * 
     * @param host
     * @param port
     * @param appNumber
     */
    public static void start(String host, int port, String appNumber, String instNum) {
        InetSocketAddress agentSocketAddress = new InetSocketAddress(host, port);

        DragoonClient client = new DragoonClient();
        client.setAppNumber(appNumber);
        client.setAgentSocketAddress(agentSocketAddress);
        client.setAppInstNumber(instNum);

        JMXUtils.register(DragoonClientServletContextListener.DEFAULT_CLIENT_OBJECT_NAME, client);
        // 默认把uri的不启用
        PatrolUtils.registerMBean(springStatEnable, jdbcStatEnable, false, log4jStatEnable, napoliStatEnable,
                                  napoliMQServerStatEnable);

        client.start();
    }

    public boolean start() {
        boolean result = super.start();

        heartbeatFuture = scheduler.scheduleAtFixedRate(createHeartTask(), initialDelay, heartbeatPeriod, unit);

        return result;
    }

    public long getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    public boolean stop() {
        boolean result = super.stop();

        if (heartbeatFuture != null) {
            if (heartbeatFuture.cancel(true)) {
                heartbeatFuture = null;
            }
        }

        return result;
    }

    public long getHeartbeatSentCount() {
        return heartbeatSentCount.get();
    }

    public long getHeartbeatSkipCount() {
        return heartbeatSkipCount.get();
    }

    protected Runnable createHeartTask() {
        return new HeartTask();
    }

    public String getName() {
        return name;
    }

    public int getMajorVersion() {
        return VERSION.MAJOR_VERSION;
    }

    public int getMinorVersion() {
        return VERSION.MINOR_VERSION;
    }
    
    public String getVersion() {
        return VERSION.MAJOR_VERSION + "." + VERSION.MINOR_VERSION + "." + VERSION.REVISION_VERSION;
    }

    public void sendNotification(DragoonNotificationMessage notification) {
        connectorFilter.sendNotification(notification);
    }

    public String getAgentAddress() {
        SocketAddress address = this.connectorFilter.getServerAddress();

        if (address != null) {
            return address.toString();
        }

        return null;
    }

    public void setAgentAddress(String address, int port) {
        SocketAddress masterAddress = this.connectorFilter.parseAddress(address, port);
        this.connectorFilter.setServerAddress(masterAddress);
    }

    public boolean closeSession() {
        DragoonSession session = this.connectorFilter.getSession();
        if (session != null) {
            session.close();

            return true;
        }

        return false;
    }

    private CompositeType getSessionType() throws JMException {
        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.DATE, SimpleType.STRING, SimpleType.STRING,
                SimpleType.INTEGER, SimpleType.INTEGER };

        String[] indexNames = { "createdDate", "localAddress", "remoteAddress", "receivedMessageCount",
                "sentMessageCount" };
        String[] indexDescriptions = { "createdDate", "localAddress", "remoteAddress", "receivedMessageCount",
                "sentMessageCount" };

        return new CompositeType("Session", "Session", indexNames, indexDescriptions, indexTypes);
    }

    public CompositeData getSessionInfo() throws JMException {
        DragoonSession session = this.connectorFilter.getSession();

        if (session == null) {
            return null;
        }

        CompositeType rowType = getSessionType();

        Map<String, Object> items = new HashMap<String, Object>();

        items.put("createdDate", session.getCreatedDate());
        items.put("localAddress", session.getLocalAddress() != null ? session.getLocalAddress().toString() : null);
        items.put("remoteAddress", session.getRemoteAddress() != null ? session.getRemoteAddress().toString() : null);
        items.put("receivedMessageCount", session.getReceivedMessageCount());
        items.put("sentMessageCount", session.getSentMessageCount());

        return new CompositeDataSupport(rowType, items);
    }

    public long getHandleGetAttributeCount() {
        return messageHandler.getHandleGetAttributeCount();
    }

    public long getHandleSetAttributeCount() {
        return messageHandler.getHandleSetAttributeCount();
    }

    public long getHandleInvokeCount() {
        return messageHandler.getHandleInvokeCount();
    }

    private final class HeartTask implements Runnable {

        public void run() {
            DragoonSession session = getConnectorFilter().getSession();
            if (session != null) {
                Heartbeat message = new Heartbeat();

                session.sendNotificationMessage(message);

                heartbeatSentCount.incrementAndGet();
            } else {
                heartbeatSkipCount.incrementAndGet();
            }
        }
    }

    public long getConnectCount() {
        return connector.getConnectCount();
    }

    public long getConnectEstablishedCount() {
        return connector.getConnectEstablishedCount();
    }

    public long getReceivedBytes() {
        return connector.getReceivedBytes();
    }

    public long getSentBytes() {
        return connector.getSentBytes();
    }

    public long getReceivedMessages() {
        return connector.getReceivedMessages();
    }

    public long getSentMessages() {
        return connector.getSentMessages();
    }

    public static void setSpringStatEnable(boolean springStatEnable) {
        DragoonClient.springStatEnable = springStatEnable;
    }

    public static void setJdbcStatEnable(boolean jdbcStatEnable) {
        DragoonClient.jdbcStatEnable = jdbcStatEnable;
    }

    public static void setUriStatEnable(boolean uriStatEnable) {
        DragoonClient.uriStatEnable = uriStatEnable;
    }

    public static boolean isUriStatEnable() {
        return uriStatEnable;
    }

    public static void setLog4jStatEnable(boolean log4jStatEnable) {
        DragoonClient.log4jStatEnable = log4jStatEnable;
    }

    public static void setNapoliStatEnable(boolean napoliStatEnable) {
        DragoonClient.napoliStatEnable = napoliStatEnable;
    }

    public static boolean isNapoliMQServerStatEnable() {
        return napoliMQServerStatEnable;
    }

    public static void setNapoliMQServerStatEnable(boolean napoliMQServerStatEnable) {
        DragoonClient.napoliMQServerStatEnable = napoliMQServerStatEnable;
    }

}
