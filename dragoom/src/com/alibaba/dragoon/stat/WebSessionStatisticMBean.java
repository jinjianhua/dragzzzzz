/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.util.Date;
import java.util.List;

/**
 * @author shaojin.wensj
 */
public interface WebSessionStatisticMBean {

    String getSessionId();

    String getRemoteAddress();

    Date getSessionCreationTime();

    Date getSessionLastAccessedTime();

    int getSessionMaxInactiveInterval();

    void setSessionMaxInactiveInterval(int interval);

    List<String> getSessionAttributeNames();

    /**
     * Returns the object bound with the specified name in this session, or <code>null</code> if no object is bound
     * under the name.
     * 
     * @param name a string specifying the name of the object
     * @return the object with the specified name
     * @exception IllegalStateException if this method is called on an invalidated session
     */
    Object getSessionAttribute(String name);

    void setSessionAttribute(String name, Object value);

    /**
     * Removes the object bound with the specified name from this session. If the session does not have an object bound
     * with the specified name, this method does nothing.
     * <p>
     * After this method executes, and if the object implements <code>HttpSessionBindingListener</code>, the container
     * calls <code>HttpSessionBindingListener.valueUnbound</code>. The container then notifies any
     * <code>HttpSessionAttributeListener</code>s in the web application.
     * 
     * @param name the name of the object to remove from this session
     * @exception IllegalStateException if this method is called on an invalidated session
     */
    void sessionRemoveAttribute(String name);

    /**
     * Invalidates this session then unbinds any objects bound to it.
     * 
     * @exception IllegalStateException if this method is called on an already invalidated session
     */
    void sessionInvalidate();

    /**
     * 获取Session实现的ClassName，这个属性查看Session的实现
     * 
     * @return
     */
    String getSessionClassName();

    // /////////////////////////////

    /**
     * 取消监控
     */
    boolean cancelMonitor() throws Exception;

    long getJdbcStatementExecuteErrorCount();

    long getResetCount();

    void reset();

    long getJdbcStatementExecuteSuccessCount();

    long getJdbcStatementExecuteSpanNanoTotal();

    String getUser();

    // ////////////// notifier

    boolean isJdbcStatementCloseAfterNotifyEnabled();

    void setJdbcStatementCloseAfterNotifyEnabled(boolean afterStatementCloseNotifyEnable);

    boolean isJdbcStatementCreateAfterNotifyEnabled();

    void setJdbcStatementCreateAfterNotifyEnabled(boolean afterStatementCreateNotifyEnable);

    boolean isJdbcStatementExecuteBatchAfterNotifyEnabled();

    void setJdbcStatementExecuteBatchAfterNotifyEnabled(boolean afterStatementExecuteBatchNotifyEnable);

    boolean isJdbcStatementExecuteAfterNotifyEnabled();

    void setJdbcStatementExecuteAfterNotifyEnabled(boolean afterStatementExecuteNotifyEnable);

    boolean isJdbcStatementExecuteQueryAfterNotifyEnabled();

    void setJdbcStatementExecuteQueryAfterNotifyEnabled(boolean afterStatementExecuteQueryNotifyEnable);

    boolean isJdbcStatementExecuteUpdateAfterNotifyEnabled();

    void setJdbcStatementExecuteUpdateAfterNotifyEnabled(boolean afterStatementExecuteUpdateNotifyEnable);

    boolean isJdbcStatementPrepareCallAfterNotifyEnabled();

    void setJdbcStatementPrepareCallAfterNotifyEnabled(boolean afterStatementPrepareCallNotifyEnable);

    boolean isJdbcStatementPrepareAfterNotifyEnabled();

    void setJdbcStatementPrepareAfterNotifyEnabled(boolean afterStatementPrepareNotifyEnable);

    boolean isJdbcResultSetCloseAfterNotifyEnabled();

    void setJdbcResultSetCloseAfterNotifyEnabled(boolean afterResultSetCloseNotifyEnable);

    boolean isJdbcStatementExecuteErrorAfterNotifyEnabled();

    void setJdbcStatementExecuteErrorAfterNotifyEnabled(boolean afterStatementExecuteErrorNotifyEnable);

    void setJdbcStatementExecuteNotifyEnableAll();

    void setJdbcStatementExecuteNotifyDiableAll();

    String getUserAttributeName();

    void setUserAttributeName(String userAttributeName);

    boolean isSpringBeanMethodInvokeBeforeNotifyEnabled();

    void setSpringBeanMethodInvokeBeforeNotifyEnabled(boolean springBeanMethodInvokeBeforeNotifyEnable);

    boolean isSpringBeanMethodInvokeAfterNotifyEnabled();

    void setSpringBeanMethodInvokeAfterNotifyEnabled(boolean springBeanMethodInvokeAfterNotifyEnable);
}
