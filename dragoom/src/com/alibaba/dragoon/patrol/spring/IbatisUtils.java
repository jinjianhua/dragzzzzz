package com.alibaba.dragoon.patrol.spring;

import java.lang.reflect.Method;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImplWrapper;
import com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapSessionImplUtils;

public class IbatisUtils {

    private static boolean VERSION_2_3_4 = false;

    static {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass("com.ibatis.sqlmap.engine.mapping.result.AutoResultMap");
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.equals("setResultObjectValues")) { // ibatis 2.3.4 add method 'setResultObjectValues'
                    VERSION_2_3_4 = true;
                    break;
                }
            }
        } catch (Throwable e) {
            // skip
        }
    }

    public static boolean isVersion2_3_4() {
        return VERSION_2_3_4;
    }

    public static SqlMapExecutor setClientImpl(SqlMapExecutor session, SqlMapClientImplWrapper clientImplWrapper) {
        if (session == null || clientImplWrapper == null) {
            return session;
        }
        
        if (session.getClass() == SqlMapSessionImpl.class) {
            SqlMapSessionImpl sessionImpl = (SqlMapSessionImpl) session;
            SqlMapSessionImplUtils.set(sessionImpl, clientImplWrapper);
        }
        
        return session;
    }
}
