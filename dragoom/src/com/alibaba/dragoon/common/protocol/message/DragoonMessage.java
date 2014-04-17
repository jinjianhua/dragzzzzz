package com.alibaba.dragoon.common.protocol.message;


public interface DragoonMessage {
    String getType();
    
    String toJSONString();
    
    int getSourceLength();
    
    void setSourceLength(int sourceLength);
}
