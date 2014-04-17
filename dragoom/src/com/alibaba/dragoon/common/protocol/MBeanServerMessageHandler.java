/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.protocol.message.Connect;
import com.alibaba.dragoon.common.protocol.message.ConnectResp;
import com.alibaba.dragoon.common.protocol.message.DragoonNotificationMessage;
import com.alibaba.dragoon.common.protocol.message.GetAttribute;
import com.alibaba.dragoon.common.protocol.message.GetAttributeResp;
import com.alibaba.dragoon.common.protocol.message.Invoke;
import com.alibaba.dragoon.common.protocol.message.InvokeResp;
import com.alibaba.dragoon.common.protocol.message.SetAttribute;
import com.alibaba.dragoon.common.protocol.message.SetAttributeResp;
import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.patrol.PatrolUtils;

public class MBeanServerMessageHandler extends MessageHandlerAdapter implements MessageHandler {

    private final static Log LOG                     = LogFactory.getLog(MBeanServerMessageHandler.class);

    private MBeanServer      mbeanServer;

    private final AtomicLong handleGetAttributeCount = new AtomicLong();
    private final AtomicLong handleSetAttributeCount = new AtomicLong();
    private final AtomicLong handleInvokeCount       = new AtomicLong();

    private Set<String>      resetSupportNames       = new HashSet<String>();

    {
        resetSupportNames.add(PatrolUtils.BEAN_NAME_SPRING_STAT);
        resetSupportNames.add(PatrolUtils.BEAN_NAME_SPRING_METHOD_STAT);
        resetSupportNames.add(PatrolUtils.BEAN_NAME_SPRING_IBATIS_STAT);
        resetSupportNames.add(PatrolUtils.BEAN_NAME_LOG_STAT);
        resetSupportNames.add(PatrolUtils.BEAN_NAME_EXCEPTION);
        resetSupportNames.add(PatrolUtils.BEAN_NAME_NAPOLI_STAT);
        resetSupportNames.add(PatrolUtils.BEAN_NAME_NAPOLI_MQSERVER_STAT);
    }

    public MBeanServerMessageHandler(MBeanServer mbeanServer){
        super();

        if (mbeanServer == null) {
            this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
        } else {
            this.mbeanServer = mbeanServer;
        }
    }

    @Override
    public GetAttributeResp handle(DragoonSession session, GetAttribute message) {
        handleGetAttributeCount.incrementAndGet();

        StatUtils.clearRequireResetFlag();
        String objectName = message.getObjectName().toString();

        try {
            Map<String, Object> values = new HashMap<String, Object>();

            if (message.getOptions() != null) {
                for (String option : message.getOptions()) {
                    option = option.trim();
                    if (option.toLowerCase().startsWith("reset=true")) {
                        if (resetSupportNames.contains(objectName)) {
                            StatUtils.setRequireReset();
                        }
                    } else if (option.toLowerCase().startsWith("reset=")) {
                        if (resetSupportNames.contains(objectName)) {
                            StatUtils.setRequireReset();
                        }
                    }
                }
            }

            for (String attributeName : message.getAttributeNames()) {
                values.put(attributeName, getAttributeInternal(message, attributeName));
            }
            
            if (message.getOptions() != null) {
                for (String option : message.getOptions()) {
                    option = option.trim();
                    if (option.toLowerCase().startsWith("reset=true")) {
                        if (!resetSupportNames.contains(objectName)) {
                            reset(session, "reset", message.getObjectName());
                        }
                    } else if (option.toLowerCase().startsWith("reset=")) {
                        if (!resetSupportNames.contains(objectName)) {
                            String[] resetArray = option.split("=");
                            String method = resetArray[1];

                            if (method != null && !"".equals(method) && !"true".equals(method)) {
                                reset(session, method, message.getObjectName());
                            }
                        }
                    }
                }
            }

            return new GetAttributeResp(values);
        } catch (Exception ex) {
            LOG.error("handle message error objectName=" + message.getObjectName().toString(), ex);

            return new GetAttributeResp(ex);
        }
    }

    private void reset(DragoonSession session, String method, ObjectName objectName) throws Exception {
        mbeanServer.invoke(objectName, method, new String[0], new String[0]);
    }

    private Object getAttributeInternal(GetAttribute message, String attributeName) throws Exception {
        String[] items = attributeName.split("\\.");
        Object value = null;
        Object current = null;
        for (int i = 0; i < items.length; ++i) {
            if (i == 0) {
                current = mbeanServer.getAttribute(message.getObjectName(), items[i]);
                if (current instanceof List) {
                    try {
                        current = deepCopyList(current);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // FIXME high risk
                        current = deepCopyList(current);
                    }
                }
            } else {
                if (current instanceof CompositeData) {
                    CompositeData compositeDate = (CompositeData) current;
                    current = compositeDate.get(items[i]);
                } else if (current instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) current;
                    current = map.get(items[i]);
                } else {
                    current = null;
                }
            }

            if (current == null) {
                break;
            }

            if (i == items.length - 1) {
                value = current;
            }
        }

        return value;
    }

    /**
     * @param current
     * @param current2
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Object> deepCopyList(Object current) throws ArrayIndexOutOfBoundsException {
        List<Object> list = new LinkedList<Object>();
        addAll(list, new Object[((List<Object>) current).size()]);
        Collections.copy(list, (List<Object>) current);

        return list;
    }

    public static void addAll(Collection<Object> collection, Object[] elements) {
        for (int i = 0, size = elements.length; i < size; i++) {
            collection.add(elements[i]);
        }
    }

    @Override
    public InvokeResp handle(DragoonSession session, Invoke message) {
        handleInvokeCount.incrementAndGet();
        try {
            Object returnValue = mbeanServer.invoke(message.getObjectName(),
                                                    message.getOperatorName(),
                                                    message.getParameters().toArray(),
                                                    message.getSignature().toArray(new String[message.getSignature().size()]));

            return new InvokeResp(returnValue);
        } catch (Exception ex) {
            LOG.error("handle message error", ex);

            return new InvokeResp(ex);
        }
    }

    @Override
    public SetAttributeResp handle(DragoonSession session, SetAttribute message) {
        handleSetAttributeCount.incrementAndGet();
        try {
            mbeanServer.setAttribute(message.getObjectName(),
                                     new Attribute(message.getAttributeName(), message.getAttributeValue()));
            return new SetAttributeResp();
        } catch (Exception ex) {
            LOG.error("handle message error", ex);

            return new SetAttributeResp(ex);
        }
    }

    @Override
    public void handle(DragoonSession session, DragoonNotificationMessage message) {

    }

    @Override
    public ConnectResp handle(DragoonSession session, Connect message) {
        return new ConnectResp(new UnsupportedOperationException());
    }

    public long getHandleGetAttributeCount() {
        return handleGetAttributeCount.get();
    }

    public long getHandleSetAttributeCount() {
        return handleSetAttributeCount.get();
    }

    public long getHandleInvokeCount() {
        return handleInvokeCount.get();
    }

}
