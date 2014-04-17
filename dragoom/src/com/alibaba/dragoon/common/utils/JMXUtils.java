/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * @author shaojin.wensj
 */
public final class JMXUtils {

    public static ObjectName register(String name, Object mbean) {
        try {
            ObjectName objectName = new ObjectName(name);

            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            try {
                mbeanServer.registerMBean(mbean, objectName);
            } catch (InstanceAlreadyExistsException ex) {
                mbeanServer.unregisterMBean(objectName);
                mbeanServer.registerMBean(mbean, objectName);
            }

            return objectName;
        } catch (JMException e) {
            throw new IllegalArgumentException(name, e);
        }
    }

    public static void unregister(String name) {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            mbeanServer.unregisterMBean(new ObjectName(name));
        } catch (JMException e) {
            throw new IllegalArgumentException(name, e);
        }

    }

    public static List<Map<String, Object>> convertToList(TabularData tabularData) throws OpenDataException {
        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();

        for (Object item : tabularData.values()) {
            rowList.add(convert((CompositeData) item));
        }

        return rowList;
    }

    public static Map<String, Object> convert(CompositeData compositeData) throws OpenDataException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String name : compositeData.getCompositeType().keySet()) {
            Object value = compositeData.get(name);
            OpenType<?> type = compositeData.getCompositeType().getType(name);

            value = convert(type, value);

            map.put(name, value);
        }

        return map;
    }

    private static final String[]      THROWABLE_COMPOSITE_INDEX_NAMES        = { "message", "class", "stackTrace" };
    private static final String[]      THROWABLE_COMPOSITE_INDEX_DESCRIPTIONS = { "message", "class", "stackTrace" };
    private static final OpenType<?>[] THROWABLE_COMPOSITE_INDEX_TYPES        = new OpenType<?>[] { SimpleType.STRING,
            SimpleType.STRING, SimpleType.STRING                             };

    private static CompositeType       THROWABLE_COMPOSITE_TYPE               = null;

    public static CompositeType getThrowableCompositeType() throws OpenDataException {
        if (THROWABLE_COMPOSITE_TYPE == null) {
            THROWABLE_COMPOSITE_TYPE = new CompositeType("Throwable", "Throwable", THROWABLE_COMPOSITE_INDEX_NAMES,
                                                         THROWABLE_COMPOSITE_INDEX_DESCRIPTIONS,
                                                         THROWABLE_COMPOSITE_INDEX_TYPES);
        }

        return THROWABLE_COMPOSITE_TYPE;
    }

    public static CompositeData getErrorCompositeData(Throwable error) throws OpenDataException {
        if (error == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("class", error.getClass().getName());
        map.put("message", error.getMessage());

        StringWriter buf = new StringWriter();
        error.printStackTrace(new PrintWriter(buf));

        map.put("stackTrace", buf.toString());

        return new CompositeDataSupport(getThrowableCompositeType(), map);
    }

    public static TabularType getAttributesTabularType() throws OpenDataException {
        OpenType<?>[] indexTypes = new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING };

        String[] indexNames = new String[] { "name", "value" };

        CompositeType rowType = new CompositeType("Attributes", "Attributes", indexNames, indexNames, indexTypes);

        return new TabularType("Attributes", "Attributes", rowType, indexNames);
    }

    public static TabularData getAttributesTabularData(Map<?, ?> attributes) throws OpenDataException {
        return getAttributesTabularData(getAttributesTabularType(), attributes);
    }

    public static TabularData getAttributesTabularData(TabularType tabularType, Map<?, ?> attributes)
                                                                                                     throws OpenDataException {
        TabularData data = new TabularDataSupport(tabularType);

        for (Map.Entry<?, ?> entry : attributes.entrySet()) {
            Map<String, String> row = new HashMap<String, String>();
            row.put("name", entry.getKey().toString());
            row.put("value", entry.getValue() != null ? entry.getValue().toString() : null);
            data.put(new CompositeDataSupport(tabularType.getRowType(), row));
        }

        return data;
    }

    public static Object convert(OpenType<?> type, Object value) throws OpenDataException {
        if (value == null) {
            return null;
        }

        if (SimpleType.STRING.equals(type)) {
            return value.toString();
        }

        if (SimpleType.BIGDECIMAL.equals(type)) {
            return (BigDecimal) value;
        }

        if (SimpleType.BIGINTEGER.equals(type)) {
            return (BigInteger) value;
        }

        if (SimpleType.LONG.equals(type)) {
            return (Long) value;
        }

        if (SimpleType.INTEGER.equals(type)) {
            return (Integer) value;
        }

        if (SimpleType.SHORT.equals(type)) {
            return (Short) value;
        }

        if (SimpleType.BYTE.equals(type)) {
            return (Byte) value;
        }

        if (SimpleType.FLOAT.equals(type)) {
            return (Float) value;
        }

        if (SimpleType.DOUBLE.equals(type)) {
            return (Double) value;
        }

        if (SimpleType.CHARACTER.equals(type)) {
            return (Character) value;
        }

        if (SimpleType.DATE.equals(type)) {
            return (Date) value;
        }

        if (SimpleType.BOOLEAN.equals(type)) {
            return (Boolean) value;
        }

        if (SimpleType.OBJECTNAME.equals(type)) {
            return (ObjectName) value;
        }

        if (type instanceof CompositeType) {
            CompositeData compositeData = (CompositeData) value;
            CompositeType compositeType = (CompositeType) type;
            Map<String, Object> map = new HashMap<String, Object>();
            for (String key : compositeType.keySet()) {
                Object itemValue = convert(compositeType.getType(key), compositeData.get(key));
                map.put(key, itemValue);
            }
            return map;
        }

        if (type instanceof TabularType) {
            TabularType tabularType = (TabularType) type;
            CompositeType rowType = tabularType.getRowType();
            TabularData tabularValue = (TabularData) value;

            if (tabularType.getTypeName().equals("Attributes") && rowType.keySet().size() == 2) {
                String[] keys = rowType.keySet().toArray(new String[2]);
                OpenType<?> firstType = rowType.getType(keys[0]);
                OpenType<?> secondType = rowType.getType(keys[1]);

                if (firstType.equals(SimpleType.STRING) && "name".equals(keys[0]) && "value".equals(keys[1])) {
                    Map<String, Object> attributes = new HashMap<String, Object>();

                    for (Object item : tabularValue.values()) {
                        CompositeData rowData = (CompositeData) item;
                        String attributeName = (String) rowData.get("name");
                        Object attributeValue = convert(secondType, rowData.get("value"));

                        attributes.put(attributeName, attributeValue);
                    }

                    return attributes;
                }
            }

            // Attributes
            return convertToList(tabularValue);
        }

        if (type instanceof ArrayType<?>) {
            ArrayType<?> arrayType = (ArrayType<?>) type;
            List<Object> list = new ArrayList<Object>();
            if (value instanceof Collection<?>) {
                for (Object item : (Collection<?>) value) {
                    list.add(convert(arrayType.getElementOpenType(), item));
                }
            }

            if (value instanceof Object[]) {
                for (Object item : (Object[]) value) {
                    list.add(convert(arrayType.getElementOpenType(), item));
                }
            }

            return list;
        }

        return value.toString();
    }
}
