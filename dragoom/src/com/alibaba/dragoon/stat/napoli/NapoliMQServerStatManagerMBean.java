package com.alibaba.dragoon.stat.napoli;

import javax.management.JMException;
import javax.management.openmbean.TabularData;

public interface NapoliMQServerStatManagerMBean {

    TabularData getNapoliMQServerQueueStats() throws JMException;

    TabularData getNapoliMQServerStats() throws JMException;

    void resetServerQueueStats() throws JMException;
    void resetServerStats() throws JMException;
    
    long getServerQueueStatsResetCount() throws JMException;
    long getServerStatsResetCount() throws JMException;
}
