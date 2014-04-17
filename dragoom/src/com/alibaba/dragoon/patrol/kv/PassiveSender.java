/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.patrol.kv;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.client.DragoonClient;
import com.alibaba.dragoon.common.DragoonException;
import com.alibaba.dragoon.common.protocol.message.Passive;
import com.alibaba.dragoon.common.utils.JMXUtils;

/**
 * 被动监控的主动发送者，用户应用及时发送数据。数据必须是kv格式。
 * 
 * @author lijun.cailj
 */
public final class PassiveSender implements PassiveSenderMBean {

    public final static Log      log                        = LogFactory.getLog(PassiveSender.class);

    private static DragoonClient client;
    private static PassiveSender instance                   = new PassiveSender();

    public final static int      ALLOW_DATA_SIZE_MAX        = 1000;                                    // 每笔数据最多1000个key-value

    private static Long          lastSubmitTimeMills;
    public final static int      SUBMIT_FREQUENCY_MIN_MILLS = 100;                                     // 每秒最多10笔数据
    public final static String   KV_LIST                    = "KVList";
    public final static String   KV                         = "kv";

    public final static String   BEAN_NAME_PASSIVE_SENDER   = "com.alibaba.dragoon:type=PassiveSender";

    private static Long          abandonDataSize            = 0L;
    private static Long          abandonCount               = 0L;
    private static Long          continualSubmitCount       = 0L;
    private static Long          successSubmitCount         = 0L;

    private PassiveSender(){
    }

    public static PassiveSender getInstance() {
        return instance;
    }

    static {
        JMXUtils.register(BEAN_NAME_PASSIVE_SENDER, PassiveSender.getInstance());
    }

    /**
     * 不建议个人使用，请使用submit(Map<String, Object> data, String miName) 提交KV数据。
     * 
     * @param dataKey 监控项数据key，monitor需要根据此获取数据
     * @param data 实际的数据
     * @param miName 监控项名称
     * @return
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static boolean submit(String dataKey, Object data, String miName) {
        client = DragoonClient.getInstance();
        if (client == null) {
            throw new IllegalArgumentException("client is null. can't send passive message.");
        }

        if (dataKey == null) {
            throw new IllegalArgumentException("dataKey is null.");
        }
        if (data == null) {
            throw new IllegalArgumentException("data is null.");
        }
        if (miName == null) {
            throw new IllegalArgumentException(" The name of monitor item is null.");
        }

        Map<String, Object> kvData = null;
        if (data instanceof Map<?, ?>) {
            try {
                kvData = (Map<String, Object>) data;
            } catch (Exception e) {
                throw new IllegalArgumentException("data must be Map<String, Object> .");
            }

            if (kvData == null || kvData.isEmpty()) {
                throw new IllegalArgumentException("kvMap is empty.");
            }

            if (hasExceedDataSize(kvData)) {
                abandonDataSize += kvData.size();
                abandonCount++;
                throw new DragoonException(" data size of map could not great than " + ALLOW_DATA_SIZE_MAX
                                           + ", Current size:" + kvData.size());
            }

            isVaildClassType(kvData);
        }

        if (isContinual()) {
            continualSubmitCount++;
            throw new DragoonException("The operation of submiting is frequent. must be great than "
                                       + SUBMIT_FREQUENCY_MIN_MILLS + " mills");
        }

        Passive msg = getPassiveMessage(dataKey, data, miName);
        client.sendNotification(msg);
        successSubmitCount++;
        return true;
    }

    /**
     * 实时提交KV监控数据。
     * 
     * @param data kv的监控项数据
     * @return 成功标识
     */
    public static boolean submit(Map<String, Object> data) {
        return submit(KV_LIST, data, KV);
    }

    /**
     * 检查频率
     */
    private static boolean isContinual() {

        if (lastSubmitTimeMills != null
            && (System.currentTimeMillis() - lastSubmitTimeMills) < SUBMIT_FREQUENCY_MIN_MILLS) {
            return true;
        }
        lastSubmitTimeMills = System.currentTimeMillis();

        return false;
    }

    /**
     * 组装passive消息
     * 
     * @param kvMap
     * @return
     */
    private static Passive getPassiveMessage(String dataKey, Object data, String miName) {
        String appNumber = client.getAppNumber();
        String instNumber = client.getAppInstNumber();

        Passive msg = new Passive();
        msg.setAppNumber(appNumber);
        msg.setInstNumber(instNumber);
        msg.setMiName(miName);
        msg.setTimestamp(new Date());
        msg.getValues().put(dataKey, Collections.singletonList(data));

        return msg;
    }

    /**
     * 验证单次提交的数据长度
     * 
     * @param kvMap
     */
    private static boolean hasExceedDataSize(Map<String, Object> data) {
        if (data.size() > ALLOW_DATA_SIZE_MAX) {
            return true;
        }
        return false;
    }

    /**
     * @param kvMap
     */
    private static boolean isVaildClassType(Map<String, Object> kvMap) {
        for (Object value : kvMap.values()) {
            if (value != null && !KVItem.isVaildClassType(value.getClass())) {
                throw new DragoonException("not be supported, class type:" + value.getClass());
            }
        }
        return true;
    }

    /**
     * @return the abandonDataSize
     */
    public Long getAbandonDataSize() {
        return abandonDataSize;
    }

    /**
     * @return the abandonCount
     */
    public Long getAbandonCount() {
        return abandonCount;
    }

    /**
     * @return the continualSubmitCount
     */
    public Long getContinualSubmitCount() {
        return continualSubmitCount;
    }

    /**
     * @return the successSubmitCount
     */
    public Long getSuccessSubmitCount() {
        return successSubmitCount;
    }

}
