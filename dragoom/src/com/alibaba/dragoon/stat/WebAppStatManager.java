package com.alibaba.dragoon.stat;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;

import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.patrol.web.PatrolWebUtils;

public class WebAppStatManager implements WebAppStatManagerMBean {

    private final static WebAppStatManager instance       = new WebAppStatManager();

    private final AtomicInteger            concurrentMax  = new AtomicInteger();
    private final AtomicInteger            runningCount   = new AtomicInteger();

    private final List<String>             library        = new ArrayList<String>();
    private final List<String>             initParameters = new ArrayList<String>();

    private int                            majorVersion;
    private int                            minorVersion;
    private String                         contextPath;
    
    private final AtomicLong                            osMacOSXCount           = new AtomicLong(0);
    private final AtomicLong                            osWindowsCount          = new AtomicLong(0);
    private final AtomicLong                            osLinuxCount            = new AtomicLong(0);

    private final AtomicLong                            browserIE6Count         = new AtomicLong(0);
    private final AtomicLong                            browserIE7Count         = new AtomicLong(0);
    private final AtomicLong                            browserIE8Count         = new AtomicLong(0);
    private final AtomicLong                            browserIE9Count         = new AtomicLong(0);
    private final AtomicLong                            browserIE10Count        = new AtomicLong(0);

    private final AtomicLong                            browserIECount          = new AtomicLong(0);
    private final AtomicLong                            browserFirefoxCount     = new AtomicLong(0);
    private final AtomicLong                            browserChromeCount      = new AtomicLong(0);
    private final AtomicLong                            browserSafariCount      = new AtomicLong(0);
    private final AtomicLong                            browserOperaCount       = new AtomicLong(0);

    private final AtomicLong                            deviceAndroidCount      = new AtomicLong(0);
    private final AtomicLong                            deviceIpadCount         = new AtomicLong(0);
    private final AtomicLong                            deviceIphoneCount       = new AtomicLong(0);
    private final AtomicLong                            deviceWindowsPhoneCount = new AtomicLong(0);

    public WebAppStatManager(){

    }
    
    public void reset() {
        osMacOSXCount.set(0);
        osWindowsCount.set(0);
        osLinuxCount.set(0);
        
        browserIE6Count.set(0);
        browserIE7Count.set(0);
        browserIE8Count.set(0);
        browserIE9Count.set(0);
        browserIE10Count.set(0);
        
        browserIECount.set(0);
        browserFirefoxCount.set(0);
        browserChromeCount.set(0);
        browserSafariCount.set(0);
        browserOperaCount.set(0);
        
        deviceAndroidCount.set(0);
        deviceIpadCount.set(0);
        deviceIphoneCount.set(0);
        deviceWindowsPhoneCount.set(0);
        
        concurrentMax.set(0);
    }
    
    public long getDeviceAdndroidCount() {
        return deviceAndroidCount.get();
    }
    
    public void incrementDeviceAdndroidCount() {
        deviceAndroidCount.incrementAndGet();
    }

    public long getDeviceIpadCount() {
        return deviceIpadCount.get();
    }
    
    public void incrementDeviceIpadCount() {
        deviceIpadCount.incrementAndGet();
    }
    
    public long getDeviceIphoneCount() {
        return deviceIphoneCount.get();
    }
    
    public void incrementDeviceIphoneCount() {
        deviceIphoneCount.incrementAndGet();
    }

    public long getDeviceWindowsPhoneCount() {
        return deviceWindowsPhoneCount.get();
    }
    
    public void incrementDeviceWindowsPhoneCount() {
        deviceWindowsPhoneCount.incrementAndGet();
    }

    public long getOSWindowsCount() {
        return osWindowsCount.get();
    }
    
    public void incrementOSWindowsCount() {
        osWindowsCount.incrementAndGet();
    }

    public long getOSMacOSX() {
        return osMacOSXCount.get();
    }
    
    public void incrementOSMacOSX() {
        osMacOSXCount.incrementAndGet();
    }

    public long getOSLinuxCount() {
        return osLinuxCount.get();
    }
    
    public void incrementOSLinuxCount() {
        osLinuxCount.incrementAndGet();
    }

    public long getBrowserIECount() {
        return browserIECount.get();
    }
    
    public void incrementBrowserIECount() {
        browserIECount.incrementAndGet();
    }

    public long getBrowserIE6Count() {
        return browserIE6Count.get();
    }
    
    public void incrementBrowserIE6Count() {
        browserIE6Count.incrementAndGet();
    }

    public long getBrowserIE7Count() {
        return browserIE7Count.get();
    }
    
    public void incrementBrowserIE7Count() {
        browserIE7Count.incrementAndGet();
    }

    public long getBrowserIE8Count() {
        return browserIE8Count.get();
    }
    
    public void incrementBrowserIE8Count() {
        browserIE8Count.incrementAndGet();
    }

    public long getBrowserIE9Count() {
        return browserIE9Count.get();
    }
    
    public void incrementBrowserIE9Count() {
        browserIE9Count.incrementAndGet();
    }

    public long getBrowserIE10Count() {
        return browserIE10Count.get();
    }
    
    public void incrementBrowserIE10Count() {
        browserIE10Count.incrementAndGet();
    }

    public long getBrowserOperaCount() {
        return browserOperaCount.get();
    }
    
    public void incrementBrowserOperaCount() {
        browserOperaCount.incrementAndGet();
    }

    public long getBrowserFirefoxCount() {
        return browserFirefoxCount.get();
    }
    
    public void incrementBrowserFirefoxCount() {
        browserFirefoxCount.incrementAndGet();
    }

    public long getBrowserChromeCount() {
        return browserChromeCount.get();
    }
    
    public void incrementBrowserChromeCount() {
        browserChromeCount.incrementAndGet();
    }
    
    public long getBrowserSafariCount() {
        return browserSafariCount.get();
    }
    
    public void incrementBrowserSafariCount() {
        browserSafariCount.incrementAndGet();
    }

    public final List<String> getLibrary() {
        return library;
    }

    public List<String> getInitParameters() {
        return initParameters;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    @SuppressWarnings("rawtypes")
    public void contextInitialized(ServletContext context) {
        Set paths = context.getResourcePaths("/WEB-INF/lib");
        if (paths != null) {
            for (Object item : paths) {
                String path = (String) item;
                library.add(path);
            }
        }

        this.majorVersion = context.getMajorVersion();
        this.minorVersion = context.getMinorVersion();

        this.contextPath = PatrolWebUtils.getContextPath(context);
        Enumeration<?> e = context.getInitParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String value = context.getInitParameter(name);
            initParameters.add(name + ":" + value);
        }
    }

    public void incrementRunningCount() {
        int count = runningCount.incrementAndGet();
        for (;;) {
            int max = concurrentMax.get();
            if (count > max) {
                if (concurrentMax.compareAndSet(max, count)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

    public long getConcurrentMax() {
        return StatUtils.get(concurrentMax);
    }

    public void decrementRunningCount() {
        runningCount.decrementAndGet();
    }

    public int getRunningCount() {
        return runningCount.get();
    }

    public final static WebAppStatManager getInstance() {
        return instance;
    }
}
