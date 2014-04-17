/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONStreamAware;

public abstract class DragoonAbstractMessage implements DragoonMessage {

    /**
     * 来源长度，用于统计网络流量
     */
    private transient int sourceLength;

    /**
     * 来源长度，用于统计网络流量
     */
    public int getSourceLength() {
        return sourceLength;
    }

    public void setSourceLength(int sourceLength) {
        this.sourceLength = sourceLength;
    }

    protected String constructMessage(String type, long sequence, JSONObject body) {
        JSONArray array = new JSONArray();

        JSONObject header = new JSONObject();
        header.put("T", type);
        header.put("S", sequence);

        array.add(header);
        array.add(body);

        return array.toJSONString();
    }

    public Object toJSON(Object value) {
        if (value instanceof JSONAware) {
            return value;
        }

        if (value instanceof JSONStreamAware) {
            return value;
        }

        if (value instanceof SimpleType) {
            SimpleType<?> simpleType = (SimpleType<?>) value;
            return simpleType.getTypeName();
        }

        if (value instanceof ArrayType) {
            ArrayType<?> arrayType = (ArrayType<?>) value;
            JSONObject data = new JSONObject();

            data.put("_type_", ArrayType.class.getName());
            data.put("className", arrayType.getClassName());
            data.put("description", arrayType.getDescription());
            data.put("typeName", arrayType.getTypeName());
            data.put("elementOpenType", arrayType.getElementOpenType());

            return data;
        }

        if (value instanceof CompositeType) {
            CompositeType compositeType = (CompositeType) value;

            JSONObject data = new JSONObject();

            data.put("_type_", CompositeType.class.getName());
            data.put("className", compositeType.getClassName());
            data.put("description", compositeType.getDescription());
            data.put("typeName", compositeType.getTypeName());

            JSONObject types = new JSONObject();
            for (String key : compositeType.keySet()) {
                OpenType<?> openType = compositeType.getType(key);

                types.put(key, toJSON(openType));
            }
            data.put("types", types);

            return data;
        }

        if (value instanceof CompositeData) {
            CompositeData compositeData = (CompositeData) value;

            JSONObject object = new JSONObject();
            for (String key : compositeData.getCompositeType().keySet()) {
                Object entryValue = compositeData.get(key);
                object.put(key, toJSON(entryValue));
            }

            return object;
        }

        if (value instanceof TabularData) {
            TabularData tabularData = (TabularData) value;

            JSONArray array = new JSONArray();
            for (Object item : tabularData.values()) {
                array.add(toJSON(item));
            }

            return array;
        }

        if (value instanceof Map) {
            JSONObject object = new JSONObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                object.put(entry.getKey().toString(), toJSON(entry.getValue()));
            }
            return object;
        }

        if (value instanceof Collection) {
            JSONArray array = new JSONArray();
            for (Object item : (Collection<?>) value) {
                array.add(toJSON(item));
            }
            return array;
        }

        if (value instanceof Object[]) {
            JSONArray array = new JSONArray();
            for (Object item : (Object[]) value) {
                array.add(toJSON(item));
            }
            return array;
        }

        if (value instanceof Date) {
            return ((Date) value).getTime();
        }

        if (value instanceof Exception) {
            Exception paramInfo = (Exception) value;
            JSONObject object = new JSONObject();

            object.put("_type_", "_Exc");

            object.put("message", paramInfo.getMessage());
            object.put("class", paramInfo.getClass().getName());
            object.put("stackTrace", toJSON(paramInfo.getStackTrace()));

            return object;
        }

        return value;
    }

    public abstract String toJSONString();
}
