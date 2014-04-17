/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.trace.selector;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

public class DefaultHttpSessionMatchRule implements SessionMatchRule {

    private final Map<String, Object> sessionMatchValues = new HashMap<String, Object>();

    public boolean match(HttpSession session) {
        if (session != null) {
            if (sessionMatchValues.size() != 0) {
                for (Map.Entry<String, Object> item : sessionMatchValues.entrySet()) {
                    Object value = session.getAttribute(item.getKey());
                    Object matchValue = item.getValue();
                    if (matchValue == null) {
                        if (value == null) {
                            return true;
                        }
                    } else {
                        if (matchValue.equals(value)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
