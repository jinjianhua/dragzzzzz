/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * some common method for conversion
 * 
 * @author lijun.cai
 */
public class ConversionUtils {

    private ConversionUtils(){
    }

    /**
     * split a string to a List<Long> with the given regular expression.
     * 
     * @param str
     * @param regex
     * @return
     */
    public static List<Long> splitToLongList(String str, String regex) {
        String[] strList = str.split(regex);
        return convertStrArrayToLongList(strList);
    }

    /**
     * @param idList
     */
    public static List<Long> convertStrArrayToLongList(String[] strList) {
        List<Long> longList = new ArrayList<Long>();
        for (String str : strList) {
            long l = 0L;
            try {
                l = Long.parseLong(str);
            } catch (NumberFormatException e) {
                return null;
            }
            longList.add(l);
        }
        return longList;
    }
}
