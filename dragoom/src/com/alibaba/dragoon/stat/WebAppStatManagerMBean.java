package com.alibaba.dragoon.stat;

import java.util.List;

public interface WebAppStatManagerMBean {

    void reset();

    int getRunningCount();

    List<String> getLibrary();

    int getMajorVersion();

    int getMinorVersion();

    String getContextPath();
}
