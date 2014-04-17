/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol;

import java.util.ArrayList;
import java.util.List;

public class DragoonSessionConfig {

    private List<DragoonIoFilter> sessionFilters = new ArrayList<DragoonIoFilter>();

    private MessageHandler        messageHandler;

    public DragoonSessionConfig(){

    }

    public DragoonSessionConfig(MessageHandler messageHandler){
        this.messageHandler = messageHandler;
    }

    public List<DragoonIoFilter> getSessionFilters() {
        return sessionFilters;
    }

    public void setSessionFilters(List<DragoonIoFilter> sessionFilters) {
        this.sessionFilters = sessionFilters;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public DragoonIoFilterChain createChain() {
        return new DragoonIoFilterChainImpl(this);
    }

}
