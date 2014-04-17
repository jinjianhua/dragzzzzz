/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.kv;

/**
 * 暴露一些统计变量
 * @author lijun.cailj 2011-10-29 上午11:27:48
 */
public interface PassiveSenderMBean {

    /**
     * @return
     */
    Long getSuccessSubmitCount();

    /**
     * @return
     */
    Long getAbandonDataSize();

    /**
     * @return
     */
    Long getAbandonCount();

    /**
     * @return
     */
    Long getContinualSubmitCount();
    
}
