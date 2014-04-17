/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public abstract class ManagedBeanBase {

    protected Object getAttribute(ObjectName objectName, String attribute) {
        try {
            return getMBeanServer().getAttribute(objectName, attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected abstract MBeanServer getMBeanServer();
}
