/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class DragoonStandardMBeanProvider {

    private ObjectName OBJ_NAME_GC;
    private ObjectName OBJ_NAME_JVM;
    private ObjectName OBJ_NAME_Memory;
    private ObjectName OBJ_NAME_Threading;

    private GC         gc;
    private JVM        jvm;
    private Memory     memory;
    private Threading  threading;

    public GC getGC() {
        return gc;
    }

    public JVM getJVM() {
        return jvm;
    }

    public Memory getMemory() {
        return memory;
    }

    public Threading getThreading() {
        return threading;
    }

    public DragoonStandardMBeanProvider(){
        try {
            OBJ_NAME_GC = new ObjectName("com.alibaba.dragoon:type=GC");
            OBJ_NAME_JVM = new ObjectName("com.alibaba.dragoon:type=JVM");
            OBJ_NAME_Memory = new ObjectName("com.alibaba.dragoon:type=Memory");
            OBJ_NAME_Threading = new ObjectName("com.alibaba.dragoon:type=Threading");
        } catch (JMException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    public void register(MBeanServer mbeanServer) throws JMException {
        mbeanServer.registerMBean(gc = new GC(), OBJ_NAME_GC);
        mbeanServer.registerMBean(jvm = new JVM(), OBJ_NAME_JVM);
        mbeanServer.registerMBean(memory = new Memory(mbeanServer), OBJ_NAME_Memory);
        mbeanServer.registerMBean(threading = new Threading(), OBJ_NAME_Threading);
    }

    public void unregister(MBeanServer mbeanServer) throws JMException {
        mbeanServer.unregisterMBean(OBJ_NAME_GC);
        mbeanServer.unregisterMBean(OBJ_NAME_JVM);
        mbeanServer.unregisterMBean(OBJ_NAME_Memory);
        mbeanServer.unregisterMBean(OBJ_NAME_Threading);
    }
}
