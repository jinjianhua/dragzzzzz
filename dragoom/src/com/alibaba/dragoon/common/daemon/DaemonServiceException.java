/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.daemon;

public class DaemonServiceException extends RuntimeException {

    private static final long serialVersionUID = 1874191075356061696L;

    public DaemonServiceException(){
        super();
    }

    public DaemonServiceException(String message){
        super(message);
    }

    public DaemonServiceException(String message, Throwable cause){
        super(message, cause);
    }

    public DaemonServiceException(Throwable cause){
        super(cause);
    }
}
