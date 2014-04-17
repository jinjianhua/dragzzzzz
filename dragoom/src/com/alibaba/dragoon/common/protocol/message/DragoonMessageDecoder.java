package com.alibaba.dragoon.common.protocol.message;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;


public interface DragoonMessageDecoder {
    DragoonMessage decode(String text) throws IOException;
    
    DragoonMessage decode(String text, JSONObject header, JSONObject body) throws IOException;
}
