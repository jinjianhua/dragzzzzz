package com.alibaba.dragoon.patrol.jvm;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.utils.JVMUtils;
import com.alibaba.dragoon.patrol.jvm.vo.GarbageCollector;
import com.alibaba.dragoon.patrol.jvm.vo.JVMClassLoading;
import com.alibaba.dragoon.patrol.jvm.vo.JVMProcessStat;
import com.alibaba.dragoon.patrol.jvm.vo.JVMThreadInfo;
import com.alibaba.dragoon.patrol.jvm.vo.JavaProcess;
import com.alibaba.dragoon.patrol.jvm.vo.MemoryPool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JVM implements JVMMBean {

    private final static Log      LOG              = LogFactory.getLog(JVM.class);

    private String                PID;
    private Date                  uptime;
    private String                bootClassPath;
    private boolean               bootClasssPathSupported;
    private String                classPath;
    private String                inputArguments;
    private String                libraryPath;
    private String                managementSpecVersion;
    private String                systemProperties;
    private String                vmName;
    private String                vmVendor;
    private String                vmVersion;
    private String                OSVersion;
    private String                OSArch;
    private String                OSName;
    private int                   OSAvailableProcessors;

    private RuntimeMXBean         Runtime;
    private ClassLoadingMXBean    ClassLoading;
    private CompilationMXBean     Compilation;
    private OperatingSystemMXBean OS;
    private ThreadMXBean          Thread;

    private ToolsJarProvider      toolsJarProvider = null;

    public JVM(){
        this.Runtime = ManagementFactory.getRuntimeMXBean();
        this.ClassLoading = ManagementFactory.getClassLoadingMXBean();
        this.Compilation = ManagementFactory.getCompilationMXBean();
        OS = ManagementFactory.getOperatingSystemMXBean();
        Thread = ManagementFactory.getThreadMXBean();

        PID = JVMUtils.getPID();
        uptime = new Date(Runtime.getUptime());
        bootClassPath = Runtime.getBootClassPath();
        bootClasssPathSupported = Runtime.isBootClassPathSupported();
        classPath = Runtime.getClassPath();
        inputArguments = Runtime.getInputArguments().toString();
        libraryPath = Runtime.getLibraryPath();
        managementSpecVersion = Runtime.getManagementSpecVersion();
        systemProperties = System.getProperties().toString();
        vmName = Runtime.getVmName();
        vmVendor = Runtime.getVmVendor();
        vmVersion = Runtime.getVmVersion();
        OSVersion = OS.getVersion();
        OSArch = OS.getArch();
        OSName = OS.getName();
        OSAvailableProcessors = OS.getAvailableProcessors();

        try {
            toolsJarProvider = new ToolsJarProvider();
        } catch (Throwable e) {
            LOG.warn("create ToolsProvider error, please add tools.jar to your classpath.");
        }
    }

    public JavaProcess getJavaProcess() {
        JavaProcess process = new JavaProcess();
        process.setPID(PID);
        process.setUptime(uptime);
        process.setBootClassPath(bootClassPath);
        process.setBootClassPathSupported(bootClasssPathSupported);
        process.setClassPath(classPath);
        process.setInputArguments(inputArguments);
        process.setLibraryPath(libraryPath);
        process.setManagementSpecVersion(managementSpecVersion);
        process.setSystemProperties(systemProperties);
        process.setVmName(vmName);
        process.setVmVendor(vmVendor);
        process.setVmVersion(vmVersion);
        process.setOSArch(OSArch);
        process.setOSAvailableProcessors(OSAvailableProcessors);
        process.setOSName(OSName);
        process.setOSVersion(OSVersion);

        ThreadInfo[] threads = Thread.dumpAllThreads(false, false);

        JVMProcessStat stat = new JVMProcessStat();
        stat.setSystemLoadAverage(OS.getSystemLoadAverage());
        stat.setDaemonThreadCount(Thread.getDaemonThreadCount());
        stat.setPeakThreadCount(Thread.getPeakThreadCount());
        stat.setTotalStartedThreadCount(Thread.getTotalStartedThreadCount());
        stat.setThreadCount(threads.length);

        Thread.resetPeakThreadCount();

        for (ThreadInfo thread : threads) {
            JVMThreadInfo threadInfo = new JVMThreadInfo();

            threadInfo.setThreadID(thread.getThreadId());
            threadInfo.setThreadName(thread.getThreadName());
            threadInfo.setThreadState(thread.getThreadState().name());
            threadInfo.setBlockedCount(thread.getBlockedCount());
            threadInfo.setBlockedTime(thread.getBlockedTime());
            threadInfo.setWaitedCount(threadInfo.getWaitedCount());
            threadInfo.setWaitedTime(thread.getWaitedTime());
            threadInfo.setInNative(thread.isInNative());
            threadInfo.setSuspended(thread.isSuspended());
            threadInfo.setStackTrace(thread.getStackTrace());
            process.getTheadInfos().add(threadInfo);
        }

        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryPool poolInfo = new MemoryPool();

            MemoryUsage usage = pool.getUsage();

            poolInfo.setName(pool.getName());
            poolInfo.setInit(usage.getInit());
            poolInfo.setMax(usage.getMax());
            poolInfo.setCommitted(usage.getCommitted());
            poolInfo.setUsed(usage.getUsed());
            poolInfo.setPeakUsed(pool.getPeakUsage().getUsed());

            if (pool.isCollectionUsageThresholdSupported()) {
                poolInfo.setCollectionUsed(pool.getCollectionUsage().getUsed());
            }

            process.getMemoryPools().add(poolInfo);
        }

        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            GarbageCollector collectorInfo = new GarbageCollector();

            collectorInfo.setCollectionCount(collector.getCollectionCount());
            collectorInfo.setCollectionTime(collector.getCollectionTime());
            collectorInfo.setName(collector.getName());

            process.getGarbageCollectors().add(collectorInfo);
        }

        {
            JVMClassLoading classLoadingInfo = new JVMClassLoading();

            classLoadingInfo.setLoadedClassCount(ClassLoading.getLoadedClassCount());
            classLoadingInfo.setTotalCompilationTime(Compilation.getTotalCompilationTime());
            classLoadingInfo.setTotalLoadedClassCount(ClassLoading.getTotalLoadedClassCount());
            classLoadingInfo.setUnloadedClassCount(ClassLoading.getUnloadedClassCount());

            process.setClassLoading(classLoadingInfo);
        }

        {
            JVMProcessStat processStat = new JVMProcessStat();
            processStat.setDaemonThreadCount(Thread.getDaemonThreadCount());
            processStat.setPeakThreadCount(Thread.getPeakThreadCount());
            processStat.setSystemLoadAverage(OS.getSystemLoadAverage());
            processStat.setThreadCount(Thread.getThreadCount());
            processStat.setTotalStartedThreadCount(Thread.getTotalStartedThreadCount());

            process.setProcessStat(processStat);
        }

        if (toolsJarProvider != null) {
            toolsJarProvider.fill(process);
        }

        return process;
    }

    public String dump() {
        return JSON.toJSONString(getJavaProcess(), SerializerFeature.PrettyFormat);
    }
}
