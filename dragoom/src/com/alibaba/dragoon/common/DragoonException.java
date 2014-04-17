/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common;

public class DragoonException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DragoonException(){
        super();
    }

    public DragoonException(String message){
        super(message);
    }

    public DragoonException(String message, Throwable cause){
        super(message, cause);
    }
}
