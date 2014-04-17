package com.alibaba.dragoon.patrol.kv;

import java.util.List;

public interface KVTask {

    public static final String KV_TASK_KEY = "com.alibaba.dragoon.KV_TASK_KEY";

    List<KVItem> getKVItems();

}
