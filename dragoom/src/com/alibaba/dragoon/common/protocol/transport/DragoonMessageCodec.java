/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.protocol.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.protocol.message.DefaultDragoonMessageDecoder;
import com.alibaba.dragoon.common.protocol.message.DragoonMessage;
import com.alibaba.dragoon.common.protocol.message.DragoonMessageDecoder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;

public class DragoonMessageCodec {

    protected static Log        LOG                       = LogFactory.getLog(DragoonMessageCodec.class);



    public final static int     COMMAND_REQUEST           = 0;
    public final static int     COMMAND_RESPONSE          = 1;
    public final static int     COMMAND_NOTIFICATION      = 2;

    private List<DragoonMessageDecoder> decoders = new ArrayList<DragoonMessageDecoder>();
    
    public DragoonMessageCodec() {
        decoders.add(new DefaultDragoonMessageDecoder());
        
        ServiceLoader<DragoonMessageDecoder> serviceLoader = ServiceLoader.load(DragoonMessageDecoder.class);
        Iterator<DragoonMessageDecoder> it = serviceLoader.iterator();
        while (it.hasNext()) {
            DragoonMessageDecoder decoder = it.next();
            decoders.add(decoder);
        }
    }

    public DragoonMessage decode(StringBuffer buf) throws IOException {
        return decode(buf.toString());
    }
    
    public DragoonMessage decode(String text) throws IOException {
        JSONArray array = parseArray(text);
        JSONObject header = (JSONObject) array.get(0);
        JSONObject body = (JSONObject) array.get(1);
        
        DragoonMessage message = null;
        
        for (DragoonMessageDecoder decoder : decoders) {
            message = decoder.decode(text, header, body);
            if (message != null) {
                break;
            }
        }
        
        if (message == null) {
            throw new UnsupportedOperationException("decode error : " + text);
        }
        
        return message;
    }

    public static final JSONArray parseArray(String text) {
        if (text == null) {
            return null;
        }

        DefaultJSONParser parser = new DefaultJSONParser(text, ParserConfig.getGlobalInstance());
        for (Feature feature : Feature.values()) {
            if (feature.name().equals("DisableCircularReferenceDetect")) {
                parser.config(feature, true);
            }
        }

        JSONArray array;

        JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken();
            array = null;
        } else if (lexer.token() == JSONToken.EOF) {
            array = null;
        } else {
            array = new JSONArray();
            parser.parseArray(array);
        }

        parser.close();

        return array;
    }

    public String encode(DragoonMessage message) {
        return message.toJSONString();
    }

}
