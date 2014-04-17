/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.message;

import javax.management.Notification;

import com.alibaba.fastjson.JSONObject;

public class NotificationMessage extends DragoonNotificationMessage {

    private Notification notification;

    public NotificationMessage(){
    }

    public NotificationMessage(Notification notification){
        super();
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public void setSequence(int sequence) {
        this.notification.setSequenceNumber(sequence);
    }

    @Override
    public String toJSONString() {
        JSONObject body = new JSONObject();

        body.put("source", toJSON(notification.getSource()));
        body.put("userData", toJSON(notification.getUserData()));
        body.put("message", notification.getMessage());
        body.put("timeStamp", notification.getTimeStamp());
        body.put("notificatinType", notification.getType());
        body.put("type", "Notification");

        return constructMessage(getType(), notification.getSequenceNumber(), body);
    }
    
    public String getType() {
        return "Notification";
    }

}
