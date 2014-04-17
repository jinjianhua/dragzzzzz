package com.alibaba.dragoon.common.protocol.message;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.management.Notification;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class DefaultDragoonMessageDecoder implements DragoonMessageDecoder {

    protected static Log        LOG                       = LogFactory.getLog(DefaultDragoonMessageDecoder.class);

    public final static String  CONNECT                   = "Connect";
    public final static String  CONNECT_RESP              = "ConnectResp";

    public final static String  GETATTRIBUTE              = "GetAttribute";
    public final static String  GETATTRIBUTE_RESP         = "GetAttributeResp";

    public final static String  INVOKE                    = "Invoke";
    public final static String  INVOKE_RESP               = "InvokeResp";

    public final static String  OBJ_QUERY                 = "ObjectQuery";
    public final static String  OBJ_QUERY_RESP            = "ObjectQueryResp";

    public final static String  NOTIFICATION              = "Notification";

    public final static String  PASSIVE_MONITOR_ITEM_DATA = "PassiveMonitorItemData";

    public final static String  HEARTBEAT                 = "Heartbeat";

    public final static String  ERROR                     = "Error";

    public final static String  STATUS                    = "Status";
    public final static String  STATUS_RESP               = "StatusResp";

    private static final String PASSIVE                   = "Passive";

    public DragoonMessage decode(String text) throws IOException {
        JSONArray array = JSON.parseArray(text);
        JSONObject header = (JSONObject) array.get(0);
        JSONObject body = (JSONObject) array.get(1);

        return decode(text, header, body);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DragoonMessage decode(String text, JSONObject header, JSONObject body) throws IOException {
        try {
            String type = (String) header.get("T");

            if (PASSIVE_MONITOR_ITEM_DATA.equals(type)) {
                PassiveMonitorItemData data = new PassiveMonitorItemData();
                data.setSequence(header.getIntValue("S"));

                final Number ts = (Number) body.get("TS");
                if (ts == null) {
                    data.setTimestamp(new Date().getTime());
                } else {
                    data.setTimestamp(ts.longValue());
                }
                data.setSource((Map) body.get("S"));
                data.setData((Map) body.get("D"));

                return data;
            }

            if (HEARTBEAT.equals(type)) {
                Heartbeat heartbeat = new Heartbeat();
                heartbeat.setSequence(header.getIntValue("S"));
                Object ts = body.getDate("TS");
                if (ts != null) {
                    if (ts instanceof Number) {
                        heartbeat.setDate(new Date(((Number) ts).longValue()));
                    } else {
                        heartbeat.setDate((Date) ts);
                    }

                }

                heartbeat.getAttributes().putAll(body);

                return heartbeat;
            }

            if (CONNECT.equals(type)) {
                Connect connect = new Connect();
                connect.setFrameId(header.getIntValue("S"));

                connect.getProperties().putAll(body);

                return connect;
            }

            if (CONNECT_RESP.equals(type)) {
                ConnectResp connectResp = new ConnectResp();
                connectResp.setFrameId(header.getIntValue("S"));
                final JSONObject error = (JSONObject) body.get("ERROR");
                if (error != null) {
                    connectResp.setError(new ExceptionInfo(error));
                }

                final Object val = body.get("VAL");
                if (val != null && val instanceof JSONObject) {
                    JSONObject values = (JSONObject) val;
                    connectResp.getProperties().putAll(values);
                }

                return connectResp;
            }

            if (GETATTRIBUTE.equals(type)) {
                GetAttribute request = new GetAttribute();
                request.setFrameId(header.getIntValue("S"));
                request.setObjectName(new ObjectName((String) body.get("OBJ_NAME")));

                Object attrs = body.get("ATTRS");
                if (attrs != null) {
                    request.getAttributeNames().addAll((List<String>) attrs);
                }

                final Object object = body.get("OPTS");
                if (object != null) {
                    request.getOptions().addAll((List<String>) object);
                }

                return request;
            }

            if (GETATTRIBUTE_RESP.equals(type)) {
                Long timestamp = body.getLong("TS");

                GetAttributeResp response;
                if (timestamp == null) {
                    response = new GetAttributeResp();
                } else {
                    response = new GetAttributeResp(timestamp);
                }

                final JSONObject error = (JSONObject) body.get("ERROR");
                if (error != null) {
                    response.setError(new ExceptionInfo(error));
                }

                response.setFrameId(header.getIntValue("S"));
                JSONObject values = (JSONObject) body.get("VAL");
                if (values != null) {
                    response.getValues().putAll(values);
                }

                return response;
            }

            if (INVOKE.equals(type)) {
                Invoke request = new Invoke();

                request.setFrameId(header.getIntValue("S"));
                request.setObjectName(new ObjectName((String) body.get("OBJ_NAME")));
                request.setOperatorName((String) body.get("OP_NAME"));
                request.getParameters().addAll((JSONArray) body.get("PARAMS"));
                request.getSignature().addAll((List<String>) body.get("SIGNATURE"));

                return request;
            }

            if (INVOKE_RESP.equals(type)) {
                InvokeResp response = new InvokeResp();

                response.setFrameId(header.getIntValue("S"));
                response.setReturnValue(body.get("VAL"));

                final JSONObject error = (JSONObject) body.get("ERROR");
                if (error != null) {
                    response.setError(new ExceptionInfo(error));
                }

                return response;
            }

            if (NOTIFICATION.equals(type)) {
                String notificatinType = (String) body.get("notificatinType");
                String message = (String) body.get("message");
                Object source = body.get("source");
                Object userData = body.get("userData");
                long sequenceNumber = ((Number) header.get("S")).longValue();
                Notification notification = new Notification(notificatinType, source, sequenceNumber, message);
                notification.setTimeStamp(((Number) body.get("timeStamp")).longValue());
                notification.setUserData(userData);
                return new NotificationMessage(notification);
            }

            if (ERROR.equals(type)) {
                Error error = new Error();

                error.setSequence(header.getIntValue("S"));
                error.setCode(body.getString("CODE"));
                error.setType(body.getString("TYPE"));
                error.setMessage(body.getString("MSG"));
                error.setStackTrace(body.getString("STACK_TRACE"));
                error.setAttributes((Map<String, Object>) body.get("S"));

                return error;
            }

            if (STATUS.equals(type)) {
                Status status = new Status();

                status.setFrameId(header.getIntValue("S"));
                status.getProperties().putAll(body);

                return status;
            }

            if (STATUS_RESP.equals(type)) {
                StatusResp statusResp = new StatusResp();

                statusResp.setFrameId(header.getIntValue("S"));
                statusResp.getProperties().putAll(body);

                return statusResp;
            }

            if (PASSIVE.equals(type)) {
                Passive passive = new Passive();

                passive.setSequence(header.getIntValue("S"));
                passive.setTimestamp(new Date(body.getLongValue("TS")));
                passive.setAppNumber(body.getString("APP_NUM"));
                passive.setInstNumber(body.getString("INST_NUM"));
                passive.setMiName(body.getString("MI_NAME"));
                Map<String, Object> values = (Map<String, Object>) body.get("VAL");
                if (values != null) {
                    passive.setValues(values);
                }

                return passive;
            }

            return null;
        } catch (Exception ex) {
            LOG.error("decode error : " + text, ex);
            throw new IOException("decode error : " + text, ex);
        }
    }

}
