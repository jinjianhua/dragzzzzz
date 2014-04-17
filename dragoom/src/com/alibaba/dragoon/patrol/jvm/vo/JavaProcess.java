package com.alibaba.dragoon.patrol.jvm.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class JavaProcess {

    private String                 PID;
    private Date                   uptime;
    private String                 bootClassPath;
    private boolean                bootClassPathSupported;
    private String                 classPath;
    private String                 inputArguments;
    private String                 libraryPath;
    private String                 managementSpecVersion;
    private String                 systemProperties;
    private String                 vmName;
    private String                 vmVendor;
    private String                 vmVersion;
    private String                 OSVersion;
    private String                 OSArch;
    private String                 OSName;
    private int                    OSAvailableProcessors;

    private List<JVMThreadInfo>    theadInfos        = new ArrayList<JVMThreadInfo>();
    private List<GarbageCollector> garbageCollectors = new ArrayList<GarbageCollector>();
    private List<MemoryPool>       memoryPools       = new ArrayList<MemoryPool>();
    private JVMClassLoading        classLoading;
    private JVMProcessStat         processStat;

    public List<GarbageCollector> getGarbageCollectors() {
        return garbageCollectors;
    }

    public void setGarbageCollectors(List<GarbageCollector> garbageCollectors) {
        this.garbageCollectors = garbageCollectors;
    }

    public List<MemoryPool> getMemoryPools() {
        return memoryPools;
    }

    public void setMemoryPools(List<MemoryPool> memoryPools) {
        this.memoryPools = memoryPools;
    }

    public JVMClassLoading getClassLoading() {
        return classLoading;
    }

    public void setClassLoading(JVMClassLoading classLoading) {
        this.classLoading = classLoading;
    }

    public JVMProcessStat getProcessStat() {
        return processStat;
    }

    public void setProcessStat(JVMProcessStat processStat) {
        this.processStat = processStat;
    }

    @JSONField(name = "PID")
    public String getPID() {
        return PID;
    }

    @JSONField(name = "PID")
    public void setPID(String pID) {
        this.PID = pID;
    }

    public List<JVMThreadInfo> getTheadInfos() {
        return theadInfos;
    }

    public void setTheadInfos(List<JVMThreadInfo> theadInfos) {
        this.theadInfos = theadInfos;
    }

    public Date getUptime() {
        return uptime;
    }

    public void setUptime(Date uptime) {
        this.uptime = uptime;
    }

    public String getBootClassPath() {
        return bootClassPath;
    }

    public void setBootClassPath(String bootClassPath) {
        this.bootClassPath = bootClassPath;
    }

    public boolean isBootClassPathSupported() {
        return bootClassPathSupported;
    }

    public void setBootClassPathSupported(boolean bootClassPathSupported) {
        this.bootClassPathSupported = bootClassPathSupported;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getInputArguments() {
        return inputArguments;
    }

    public void setInputArguments(String inputArguments) {
        this.inputArguments = inputArguments;
    }

    public String getLibraryPath() {
        return libraryPath;
    }

    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    public String getManagementSpecVersion() {
        return managementSpecVersion;
    }

    public void setManagementSpecVersion(String managementSpecVersion) {
        this.managementSpecVersion = managementSpecVersion;
    }

    public String getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmVendor() {
        return vmVendor;
    }

    public void setVmVendor(String vmVendor) {
        this.vmVendor = vmVendor;
    }

    public String getVmVersion() {
        return vmVersion;
    }

    public void setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String oSVersion) {
        OSVersion = oSVersion;
    }

    public String getOSArch() {
        return OSArch;
    }

    public void setOSArch(String oSArch) {
        OSArch = oSArch;
    }

    public String getOSName() {
        return OSName;
    }

    public void setOSName(String oSName) {
        OSName = oSName;
    }

    public int getOSAvailableProcessors() {
        return OSAvailableProcessors;
    }

    public void setOSAvailableProcessors(int oSAvailableProcessors) {
        OSAvailableProcessors = oSAvailableProcessors;
    }

}
