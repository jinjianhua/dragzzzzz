package com.alibaba.dragoon.client.jmx;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;

@SuppressWarnings("restriction")
public class JVMStat {

    private final static Log     LOG      = LogFactory.getLog(JVMStat.class);

    private VmIdentifier         vmId;

    private Map<String, Monitor> monitors = new HashMap<String, Monitor>();

    @SuppressWarnings("unchecked")
    public JVMStat(){
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            name = name.substring(0, name.indexOf("@"));
            vmId = new VmIdentifier(name);

            MonitoredHost monitorHost = MonitoredHost.getMonitoredHost(vmId);
            MonitoredVm vm = monitorHost.getMonitoredVm(vmId, 0);

            List<Monitor> list = vm.findByPattern(".*");
            for (Monitor item : list) {
                monitors.put(item.getName(), item);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public Map<String, Monitor> getMonitors() {
        return monitors;
    }
}
