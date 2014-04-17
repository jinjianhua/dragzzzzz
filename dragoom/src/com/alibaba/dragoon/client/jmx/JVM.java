/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.client.jmx;

import java.io.PrintStream;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.alibaba.dragoon.common.utils.JVMUtils;

public class JVM implements JVMMBean {

    private RuntimeMXBean      Runtime;
    private ClassLoadingMXBean ClassLoading;
    private CompilationMXBean  Compilation;

    private Properties         props;

    private String             inputArguments;
    private int                availableProcessors = 0;

    public JVM(){
        ClassLoading = ManagementFactory.getClassLoadingMXBean();
        Compilation = ManagementFactory.getCompilationMXBean();
        Runtime = ManagementFactory.getRuntimeMXBean();

        this.props = System.getProperties();
    }

    public Date getStartTime() {
        return new Date(Runtime.getStartTime());
    }

    public String getSystemProperty(String key) {
        return props.getProperty(key);
    }

    public String getJVM() {
        return getSystemProperty("java.vm.name") + " (" + getSystemProperty("java.vm.version") + ", "
               + getSystemProperty("java.vm.info") + ")";
    }

    public String getJavaVersion() {
        return getSystemProperty("java.version");
    }

    public String getPID() {
        return JVMUtils.getPID();
    }

    public String getInputArguments() {
        if (inputArguments == null) {
            List<String> arguments = Runtime.getInputArguments();

            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < arguments.size(); ++i) {
                if (i != 0) {
                    buf.append("\n");
                }
                buf.append(arguments.get(i));
            }

            inputArguments = buf.toString();
        }

        return inputArguments;
    }

    public String getJavaHome() {
        return getSystemProperty("java.home");
    }

    public String getArch() {
        return getSystemProperty("os.arch");
    }

    public String getOSName() {
        return getSystemProperty("os.name");
    }

    public String getOSVersion() {
        return getSystemProperty("os.version");
    }

    public String getJavaSpecificationVersion() {
        return getSystemProperty("java.specification.version");
    }

    public String getJavaLibraryPath() {
        return getSystemProperty("java.library.path");
    }

    public int getAvailableProcessors() {
        if (availableProcessors == 0) {
            availableProcessors = java.lang.Runtime.getRuntime().availableProcessors();
        }

        return availableProcessors;
    }

    public int getLoadedClassCount() {
        return ClassLoading.getLoadedClassCount();
    }

    public long getTotalLoadedClassCount() {
        return ClassLoading.getTotalLoadedClassCount();
    }

    public long getUnloadedClassCount() {
        return ClassLoading.getUnloadedClassCount();
    }

    public long getTotalCompilationTime() {
        return Compilation.getTotalCompilationTime();
    }

    public void dump(PrintStream out) {
        out.println("PID : " + this.getPID());
        out.println("StartTime : " + this.getStartTime());
        out.println("InputArguments : " + this.getInputArguments());
        out.println("Arch : " + this.getArch());
        out.println("OS Name : " + this.getOSName());
        out.println("OS Version : " + this.getOSVersion());
        out.println("JVM : " + this.getJVM());
        out.println("JavaVersion : " + this.getJavaVersion());
        out.println("JavaHome : " + this.getJavaHome());
        out.println("JavaLibraryPath : " + this.getJavaLibraryPath());
        out.println("JavaSpecificationVersion : " + this.getJavaSpecificationVersion());
        out.println("AvailableProcessors : " + this.getAvailableProcessors());

        out.println("LoadedClassCount : " + this.getLoadedClassCount());
        out.println("TotalLoadedClassCount : " + this.getTotalLoadedClassCount());
        out.println("UnloadedClassCount : " + this.getUnloadedClassCount());
        out.println("TotalCompilationTime : " + this.getTotalCompilationTime());
    }

    public void dump() {
        // this.getPID();
        // this.getStartTime();
        // this.getInputArguments();
        // this.getArch();
        // this.getOSName();
        // this.getOSVersion();
        // this.getJVM();
        // this.getJavaVersion();
        // this.getJavaHome();
        // this.getJavaLibraryPath();
        // this.getJavaSpecificationVersion();
        this.getAvailableProcessors(); //

        // this.getLoadedClassCount(); // 800 micro
        // this.getTotalLoadedClassCount(); // 800 micro
        // this.getUnloadedClassCount();
        // this.getTotalCompilationTime();
    }
}
