/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.dragoon.patrol.web.PatrolServletFilter;
import com.alibaba.dragoon.patrol.web.PatrolWebUtils;

/**
 * @author shaojin.wensj
 */
public class SpringMethodStats extends NotificationBroadcasterSupport implements SpringMethodStatsMBean {

    private ConcurrentMap<MethodInfo, SpringMethodInvokeStatistic> methodStats        = new ConcurrentHashMap<MethodInfo, SpringMethodInvokeStatistic>();
    private AtomicLong                                             sequenceSeed       = new AtomicLong();
    private boolean                                                beforeEventEnabled = false;
    private boolean                                                afterEventEnabled  = false;

    private final AtomicLong                                       resetCounter       = new AtomicLong();

    public long getResetCount() {
        return resetCounter.get();
    }

    public void reset() {
        for (SpringMethodInvokeStatistic item : methodStats.values()) {
            if (item.getInvokeCount() == 0) {
                continue;
            }

            item.reset();
        }

        resetCounter.incrementAndGet();
    }

    public boolean isBeforeEventEnabled() {
        return beforeEventEnabled;
    }

    public void setBeforeEventEnabled(boolean beforeEventEnabled) {
        this.beforeEventEnabled = beforeEventEnabled;
    }

    public boolean isAfterEventEnabled() {
        return afterEventEnabled;
    }

    public void setAfterEventEnabled(boolean afterEventEnabled) {
        this.afterEventEnabled = afterEventEnabled;
    }

    public long createNotificationSequence() {
        return sequenceSeed.incrementAndGet();
    }

    public Object getNotificationSource(MethodInfo methodInfo) {
        List<Object> source = new ArrayList<Object>();

        source.add(methodInfo.getSignature());

        HttpServletRequest request = PatrolWebUtils.getHttpServletRequest();
        if (request != null) {
            source.add("web-session-" + request.getSession().getId());

            Long requestId = PatrolServletFilter.getRequestId(request);
            if (requestId != null) {
                source.add("web-req-id-" + requestId);
            }
            source.add("web-req-ip-" + PatrolWebUtils.getRemoteAddr(request));
            source.add("web-req-uri-" + request.getRequestURI());
        }

        return source;
    }

    public TabularData getMethodList() throws JMException {
        OpenType<?>[] indexTypes = new OpenType<?>[] { //
        // 0 - 4
                SimpleType.STRING, //
                SimpleType.STRING, //
                SimpleType.LONG, //
                SimpleType.LONG, //
                SimpleType.INTEGER, //

                // 5 - 9
                SimpleType.INTEGER, //
                SimpleType.DATE, //
                SimpleType.LONG, //
                SimpleType.LONG, //
                SimpleType.STRING, //

                // 10 - 14
                SimpleType.STRING, //
                SimpleType.STRING, //
                SimpleType.DATE, //
                SimpleType.STRING, //
                new ArrayType<Long>(SimpleType.LONG, true), //

                // 15 - 19
                SimpleType.LONG, //
                SimpleType.LONG, //
                SimpleType.LONG, //
                SimpleType.LONG, //
                SimpleType.LONG, //

                // 20 -
                SimpleType.LONG, //

        };

        String[] indexNames = {
                // 0 - 4
                "Class", //
                "Method", //
                "InvokeCount", //
                "TotalTime", //
                "RunningCount", //

                // 5 -9
                "ConcurrentMax", //
                "LastInvokeTime", //
                "MaxTime", //
                "ErrorCount", //
                "LastErrorMessage", //

                // 10 - 14
                "LastErrorClass", //
                "LastErrorStackTrace", //
                "LastErrorTime", //
                "LastErrorUser", //
                "Histogram", //

                // 15 - 19
                "JdbcExecuteCount", //
                "JdbcFetchRowCount", //
                "JdbcFetchRowPeak", //
                "JdbcUpdateCount", //
                "JdbcCommitCount" //

                // 20 -
                , "JdbcRollbackCount"
        //
        };

        String[] indexDescriptions = indexNames;
        CompositeType rowType = new CompositeType("RowType", "RowType", indexNames, indexDescriptions, indexTypes);

        TabularType tabularType = new TabularType("MethodInvokeStatistic", "MethodInvokeStatistic", rowType, indexNames);
        TabularData data = new TabularDataSupport(tabularType);

        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();

        for (Map.Entry<MethodInfo, SpringMethodInvokeStatistic> entry : methodStats.entrySet()) {
            MethodInfo methodInfo = entry.getKey();
            Method method = methodInfo.getMethod();
            SpringMethodInvokeStatistic stat = entry.getValue();

            long invokeCount = stat.getInvokeCount();
            if (invokeCount == 0) {
                continue;
            }

            Map<String, Object> row = new HashMap<String, Object>();

            row.put("Class", methodInfo.getInstanceClass().getName());
            row.put("Method", getMethodSignature(method, methodInfo.getParameter()));
            row.put("InvokeCount", invokeCount);
            row.put("TotalTime", stat.getNanoTotal() / (1000 * 1000));
            row.put("RunningCount", stat.getRunningCount());

            row.put("ConcurrentMax", stat.getConcurrentMax());
            row.put("LastInvokeTime", stat.getLastInvokeTime());
            row.put("MaxTime", stat.getNanoMax() / (1000 * 1000));
            row.put("ErrorCount", stat.getErrorCount());

            Throwable lastError = stat.getLastError();
            if (lastError != null) {
                row.put("LastErrorMessage", lastError.getMessage());
                row.put("LastErrorClass", lastError.getClass().getName());

                StringWriter buf = new StringWriter();
                lastError.printStackTrace(new PrintWriter(buf));
                row.put("LastErrorStackTrace", buf.toString());
            } else {
                row.put("LastErrorMessage", null);
                row.put("LastErrorClass", null);
                row.put("LastErrorStackTrace", null);
            }

            row.put("LastErrorTime", stat.getLastErrorTime());
            row.put("LastErrorUser", stat.getLastErrorUser());

            row.put("Histogram", stat.getHistogramValues());

            row.put("JdbcExecuteCount", stat.getJdbcExecuteCount());
            row.put("JdbcFetchRowCount", stat.getJdbcFetchRowCount());
            row.put("JdbcFetchRowPeak", stat.getJdbcFetchRowPeak());
            row.put("JdbcUpdateCount", stat.getJdbcUpdateCount());
            row.put("JdbcCommitCount", stat.getJdbcCommitCount());
            row.put("JdbcRollbackCount", stat.getJdbcRollbackCount());

            rowList.add(row);
        }

        for (Map<String, Object> row : rowList) {
            data.put(new CompositeDataSupport(rowType, row));
        }

        return data;
    }

    public static String getMethodSignature(Method method, String parameter) {
        StringBuffer sb = new StringBuffer();

        sb.append(method.getName() + "(");
        Class<?>[] params = method.getParameterTypes();
        for (int j = 0; j < params.length; j++) {
            sb.append(params[j].getName());
            if (j < (params.length - 1)) sb.append(",");
        }
        sb.append(")");

        if (parameter != null && parameter.length() != 0) {
            sb.append(":");
            sb.append(parameter);
        }

        return sb.toString();
    }

    public static class MethodInfo {

        private final Class<?> instanceClass;
        private final Method   method;
        protected String       parameter;

        public MethodInfo(Class<?> instanceClass, Method method){
            this(instanceClass, method, null);
        }

        public MethodInfo(Class<?> instanceClass, Method method, String parameter){
            super();
            this.instanceClass = instanceClass;
            this.method = method;
            this.parameter = parameter;
        }

        public Class<?> getInstanceClass() {
            return instanceClass;
        }

        public Method getMethod() {
            return method;
        }

        public String getParameter() {
            return parameter;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((instanceClass == null) ? 0 : instanceClass.hashCode());
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            MethodInfo other = (MethodInfo) obj;
            if (instanceClass == null) {
                if (other.instanceClass != null) return false;
            } else if (!instanceClass.equals(other.instanceClass)) return false;
            if (method == null) {
                if (other.method != null) return false;
            } else if (!method.equals(other.method)) return false;
            if (parameter == null) {
                if (other.parameter != null) return false;
            } else if (!parameter.equals(other.parameter)) return false;
            return true;
        }

        public String getSignature() {
            String signature = this.getInstanceClass().getName() + "."
                               + getMethodSignature(this.getMethod(), parameter);

            return signature;
        }
    }

    public SpringMethodInvokeStatistic get(MethodInfo methodInfo) {
        SpringMethodInvokeStatistic stat = methodStats.get(methodInfo);

        if (stat == null) {
            methodStats.putIfAbsent(methodInfo, new SpringMethodInvokeStatistic());
            stat = methodStats.get(methodInfo);
        }

        return methodStats.get(methodInfo);
    }
}
