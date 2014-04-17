package com.ibatis.sqlmap.engine.impl;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibatis.sqlmap.engine.scope.SessionScope;


public class SqlMapSessionImplUtils {
    private final static Log LOG = LogFactory.getLog(SqlMapSessionImplUtils.class);
    private static Field sessionField;
    
    public static void set(SqlMapSessionImpl session, SqlMapClientImpl client) {
        if (sessionField == null) {
            for (Field field : SqlMapSessionImpl.class.getDeclaredFields()) {
                if (field.getName().equals("session") || field.getName().equals("sessionScope")) {
                    sessionField = field;
                    sessionField.setAccessible(true);
                    break;
                }
            }
        }
        
        if (sessionField != null) {
            SessionScope sessionScope;
            try {
                sessionScope = (SessionScope) sessionField.get(session);
                
                if (sessionScope != null) {
                    if (sessionScope.getSqlMapClient() != null && sessionScope.getSqlMapClient().getClass() == SqlMapClientImpl.class) {
                        sessionScope.setSqlMapClient(client);
                    }
                    if (sessionScope.getSqlMapExecutor() != null && sessionScope.getSqlMapExecutor().getClass() == SqlMapClientImpl.class) {
                        sessionScope.setSqlMapExecutor(client);
                    }
                    if (sessionScope.getSqlMapTxMgr() != null && sessionScope.getSqlMapTxMgr().getClass() == SqlMapClientImpl.class) {
                        sessionScope.setSqlMapTxMgr(client);
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

}
