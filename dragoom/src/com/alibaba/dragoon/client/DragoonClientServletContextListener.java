/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client;

import java.net.InetSocketAddress;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.alibaba.dragoon.common.protocol.transport.socket.SocketConnector;
import com.alibaba.dragoon.common.utils.JMXUtils;

public class DragoonClientServletContextListener implements ServletContextListener, DragoonClientServletContextListenerMBean {

    public final static String DEFAULT_HOST               = "127.0.0.1";
    public final static int    DEFAULT_PORT               = 15777;
    public final static String DEFAULT_CLIENT_OBJECT_NAME = "com.alibaba.dragoon:type=DragoonClient";
    private DragoonClient      client;

    public void contextInitialized(ServletContextEvent event) {
        String appNum = event.getServletContext().getInitParameter("APP_NUM");
        String instNum = event.getServletContext().getInitParameter("INST_NUM");

        String host = event.getServletContext().getInitParameter("DRAGOON_CLIENT_HOST");

        Integer port = DEFAULT_PORT;

        String initParameterPort = event.getServletContext().getInitParameter("DRAGOON_CLIENT_PORT");

        if (initParameterPort != null && Integer.valueOf(initParameterPort) > 1024) {
            port = Integer.valueOf(initParameterPort);
        }

        if (host == null || host.trim().length() == 0) {
            host = DEFAULT_HOST;
        }

        InetSocketAddress agentSocketAddress = new InetSocketAddress(host, port);

        client = new DragoonClient();
        client.setConnector(new SocketConnector());
        client.setAgentSocketAddress(agentSocketAddress);
        client.setAppNumber(appNum);
        client.setAppInstNumber(instNum);

        JMXUtils.register(DEFAULT_CLIENT_OBJECT_NAME, client);

        client.start();
    }

    public void contextDestroyed(ServletContextEvent event) {
        if (client != null) {
            client.stop();
            JMXUtils.unregister(DEFAULT_CLIENT_OBJECT_NAME);
            client = null;
        }
    }

}
