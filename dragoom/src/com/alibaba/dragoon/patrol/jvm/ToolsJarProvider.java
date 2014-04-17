package com.alibaba.dragoon.patrol.jvm;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;

import com.alibaba.dragoon.patrol.jvm.vo.GarbageCollector;
import com.alibaba.dragoon.patrol.jvm.vo.JVMProcessStat;
import com.alibaba.dragoon.patrol.jvm.vo.JavaProcess;

@SuppressWarnings("restriction")
public class ToolsJarProvider {

    private final static Log LOG = LogFactory.getLog(ToolsJarProvider.class);

    private VmIdentifier     vmId;
    private MonitoredHost    monitorHost;
    private MonitoredVm      vm;

    public ToolsJarProvider() throws Exception{
        String name = ManagementFactory.getRuntimeMXBean().getName();
        name = name.substring(0, name.indexOf("@"));
        vmId = new VmIdentifier(name);

        monitorHost = MonitoredHost.getMonitoredHost(vmId);
        vm = monitorHost.getMonitoredVm(vmId, 0);
    }

    public void fill(JavaProcess process) {

        try {
            fillGC(process);
            fillSync(process);
            fillTlab(process);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void fillTlab(JavaProcess process) throws Exception {
        JVMProcessStat stat = process.getProcessStat();
        {
            Monitor monitor = vm.findByName("sun.gc.tlab.alloc");
            if (monitor != null) {
                stat.setTlabAlloc(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.gc.tlab.allocThreads");
            if (monitor != null) {
                stat.setTlabAllocThreads(((Number) monitor.getValue()).intValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.gc.tlab.fastWaste");
            if (monitor != null) {
                stat.setTlabFastWaste(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.gc.tlab.fills");
            if (monitor != null) {
                stat.setTlabFills(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.gc.tlab.gcWaste");
            if (monitor != null) {
                stat.setTlabGcWaste(((Number) monitor.getValue()).longValue());
            }
        }
    }

    private void fillSync(JavaProcess process) throws Exception {
        JVMProcessStat stat = process.getProcessStat();
        {
            Monitor monitor = vm.findByName("sun.rt._sync_ContendedLockAttempts");
            if (monitor != null) {
                stat.setSyncContendedLockAttempts(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.rt._sync_Inflations");
            if (monitor != null) {
                stat.setSyncInflations(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.rt._sync_Deflations");
            if (monitor != null) {
                stat.setSyncDeflations(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.rt._sync_MonExtant");
            if (monitor != null) {
                stat.setSyncMonExtant(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.rt._sync_Notifications");
            if (monitor != null) {
                stat.setSyncNotifications(((Number) monitor.getValue()).longValue());
            }
        }
        {
            Monitor monitor = vm.findByName("sun.rt._sync_Parks");
            if (monitor != null) {
                stat.setSyncParks(((Number) monitor.getValue()).longValue());
            }
        }
    }

    private void fillGC(JavaProcess process) throws MonitorException {
        List<GarbageCollector> collectorList = process.getGarbageCollectors();

        Monitor countMonitor = vm.findByName("sun.gc.policy.collectors");

        if (countMonitor == null) {
            return;
        }

        int count = ((Number) countMonitor.getValue()).intValue();
        for (int i = 0; i < count; ++i) {
            Monitor valueMonitor = vm.findByName("sun.gc.collector." + i + ".invocations");
            Monitor nameMonitor = vm.findByName("sun.gc.collector." + i + ".name");
            long value = ((Number) valueMonitor.getValue()).longValue();
            String name = nameMonitor.getValue().toString();

            for (GarbageCollector collectorInfo : collectorList) {
                if (collectorInfo.getName().equals(name)) {
                    collectorInfo.setInvocations(value);
                }
            }
        }
    }

    public void dumpAll() throws Exception {
        List<?> list = vm.findByPattern(".*");
        for (Object item : list) {
            Monitor monitor = (Monitor) item;
            System.out.println(monitor.getName() + " : " + monitor.getValue());
        }
    }
}
