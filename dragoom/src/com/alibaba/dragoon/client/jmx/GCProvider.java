package com.alibaba.dragoon.client.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.client.jmx.impl.ManagementProvider;
import com.alibaba.dragoon.client.jmx.impl.ToolsProvider;

public abstract class GCProvider {

    private final static Log LOG = LogFactory.getLog(GCProvider.class);

    public static GCProvider createGCProvider() {
        GCProvider provider = null;

        try {
            provider = new ToolsProvider();
        } catch (Throwable e) {
            LOG.warn("create ToolsProvider error, please add tools.jar to your classpath.");
        }

        if (provider == null) {
            provider = new ManagementProvider();
        }

        return provider;
    }

    public abstract long getYoungGCCollectionCount();

    public abstract long getFullGCCollectionCount();
}
