package com.alibaba.dragoon.client.jmx.impl;

import java.lang.management.ManagementFactory;

import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;

import com.alibaba.dragoon.client.jmx.GCProvider;

@SuppressWarnings("restriction")
public class ToolsProvider extends GCProvider {

    private VmIdentifier vmId;
    private Monitor      fullGCMonitor;
    private Monitor      youngGCMonitor;

    public ToolsProvider() throws Exception{
        String name = ManagementFactory.getRuntimeMXBean().getName();
        name = name.substring(0, name.indexOf("@"));
        vmId = new VmIdentifier(name);

        MonitoredHost monitorHost = MonitoredHost.getMonitoredHost(vmId);
        MonitoredVm vm = monitorHost.getMonitoredVm(vmId, 0);
        youngGCMonitor = vm.findByName("sun.gc.collector.0.invocations");
        fullGCMonitor = vm.findByName("sun.gc.collector.1.invocations");
    }

    public long getYoungGCCollectionCount() {
        if (youngGCMonitor == null) {
            return 0;
        }

        return ((Number) youngGCMonitor.getValue()).longValue();
    }

    public long getFullGCCollectionCount() {
        if (fullGCMonitor == null) {
            return 0;
        }

        return ((Number) fullGCMonitor.getValue()).longValue();
    }
}
