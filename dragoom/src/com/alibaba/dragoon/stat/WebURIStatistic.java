/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.dragoon.stat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.JMException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.patrol.web.PatrolWebUtils;
import com.alibaba.dragoon.stat.Profiler.EntryStatistic;

/**
 * @author shaojin.wensj
 */
public class WebURIStatistic implements WebStatisticMBean {

    private final static Log                            LOG                       = LogFactory.getLog(WebURIStatistic.class);
    private final String                                uri;
    private long                                        id;

    private final AtomicInteger                         runningCount              = new AtomicInteger();
    private final AtomicInteger                         concurrentMax             = new AtomicInteger();

    private final AtomicLong                            count                     = new AtomicLong(0);
    private final AtomicLong                            errorCount                = new AtomicLong();

    private final AtomicLong                            nanoTotal                 = new AtomicLong(0);
    private final AtomicLong                            nanoMax                   = new AtomicLong(0);
    private Throwable                                   lastError;
    private long                                        lastErrorTime;
    private long                                        lastSampleTime            = 0;
    private String                                      lastErrorReferer;
    private String                                      lastErrorUser;
    private String                                      lastErrorMessage;
    /**
     * 出错情况下原始的带全路径的url（get方式的时候保留原始参数）
     * 
     * @author wangye
     */
    private String                                      lastErrorUrl;

    private final AtomicLong                            jdbcFetchRowCount         = new AtomicLong();
    private final AtomicLong                            jdbcFetchRowPeak          = new AtomicLong();
    private final AtomicLong                            jdbcUpdateCount           = new AtomicLong();
    private final AtomicLong                            jdbcExecuteCount          = new AtomicLong();
    private final AtomicLong                            jdbcCommitCount           = new AtomicLong();
    private final AtomicLong                            jdbcRollbackCount         = new AtomicLong();
    private final AtomicLong                            jdbcTransactionTimeMillis = new AtomicLong();

    private final ConcurrentMap<String, EntryStatistic> profilerEntryStatMap      = new ConcurrentHashMap<String, EntryStatistic>();
    private final ProfilerEntryVisitor                  profilerEntryVisitor      = new ProfilerEntryVisitor(
                                                                                                             profilerEntryStatMap);

    private final Histogram                             histogram                 = new Histogram(new long[] { //
                                                                                                               //
            10, 100, 1000, 10 * 1000                                                             });

    private final static ThreadLocal<WebURIStatistic>   currentLocal              = new ThreadLocal<WebURIStatistic>();

    private final AtomicLong                            osMacOSXCount             = new AtomicLong(0);
    private final AtomicLong                            osWindowsCount            = new AtomicLong(0);
    private final AtomicLong                            osLinuxCount              = new AtomicLong(0);
    private final AtomicLong                            osSymbianCount            = new AtomicLong(0);
    private final AtomicLong                            osFreeBSDCount            = new AtomicLong(0);
    private final AtomicLong                            osOpenBSDCount            = new AtomicLong(0);
    private final AtomicLong                            osAndroidCount            = new AtomicLong(0);

    private final AtomicLong                            osAndroid15Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid16Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid20Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid21Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid22Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid23Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid30Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid31Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid32Count          = new AtomicLong(0);
    private final AtomicLong                            osAndroid40Count          = new AtomicLong(0);

    private final AtomicLong                            osLinuxUbuntuCount        = new AtomicLong(0);

    private final AtomicLong                            browserIECount            = new AtomicLong(0);
    private final AtomicLong                            browserFirefoxCount       = new AtomicLong(0);
    private final AtomicLong                            browserChromeCount        = new AtomicLong(0);
    private final AtomicLong                            browserSafariCount        = new AtomicLong(0);
    private final AtomicLong                            browserOperaCount         = new AtomicLong(0);
    private final AtomicLong                            browserWgetCount          = new AtomicLong(0);

    private final AtomicLong                            browserIE6Count           = new AtomicLong(0);
    private final AtomicLong                            browserIE7Count           = new AtomicLong(0);
    private final AtomicLong                            browserIE8Count           = new AtomicLong(0);
    private final AtomicLong                            browserIE9Count           = new AtomicLong(0);
    private final AtomicLong                            browserIE10Count          = new AtomicLong(0);

    private final AtomicLong                            deviceAndroidCount        = new AtomicLong(0);
    private final AtomicLong                            deviceIpadCount           = new AtomicLong(0);
    private final AtomicLong                            deviceIphoneCount         = new AtomicLong(0);
    private final AtomicLong                            deviceWindowsPhoneCount   = new AtomicLong(0);

    private int[]                                       ipArray                   = new int[128];
    private int                                         ipArraySize;
    private int                                         ipArrayThredshold         = 1000 * 1000 * 10;                               // 40m

    public final static WebURIStatistic current() {
        return currentLocal.get();
    }

    public static void setCurrent(WebURIStatistic current) {
        currentLocal.set(current);
    }

    public boolean addIp(HttpServletRequest request) {
        String ip = PatrolWebUtils.getRemoteAddr(request);
        return addIp(ip);
    }

    public boolean addIp(String ip) {
        if (ip == null || ip.length() == 0) {
            return false;
        }
        int commaIndex = ip.indexOf(',');
        if (commaIndex != -1) {
            ip = ip.substring(0, commaIndex);
        }

        InetAddress addr;
        try {
            addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return false;
        }

        int ipValue = bytesToInt(addr.getAddress());
        return addIp(ipValue);
    }

    private static int bytesToInt(byte[] address) {
        int ipnum = 0;
        for (int i = 0; i < 4; ++i) {
            int y = address[i];
            if (y < 0) {
                y += 256;
            }
            ipnum += y << ((3 - i) * 8);
        }
        return ipnum;
    }

    public synchronized boolean addIp(int ip) {
        if (ipArraySize > ipArrayThredshold) {
            return false;
        }

        int oldCapacity = ipArray.length;

        if (ipArraySize >= oldCapacity) {
            int newCapacity = (ipArray.length * 3) / 2 + 1;
            if (newCapacity > ipArrayThredshold) {
                newCapacity = ipArrayThredshold;
            }
            ipArray = Arrays.copyOf(ipArray, newCapacity);
        }

        ipArray[ipArraySize] = ip;
        ipArraySize++;
        return true;
    }

    public synchronized void clearIp() {
        ipArray = new int[128];
        ipArraySize = 0;
    }

    public synchronized int[] getIP() {
        int[] values = Arrays.copyOf(ipArray, ipArraySize);
        if (StatUtils.isRequiredReset()) {
            clearIp();
        }
        return values;
    }

    public void reset() {
        osMacOSXCount.set(0);
        osWindowsCount.set(0);
        osLinuxCount.set(0);
        osSymbianCount.set(0);
        osOpenBSDCount.set(0);
        osFreeBSDCount.set(0);
        osAndroidCount.set(0);

        osLinuxUbuntuCount.set(0);

        osAndroid15Count.set(0);
        osAndroid16Count.set(0);
        osAndroid20Count.set(0);
        osAndroid21Count.set(0);
        osAndroid22Count.set(0);
        osAndroid23Count.set(0);
        osAndroid30Count.set(0);
        osAndroid31Count.set(0);
        osAndroid32Count.set(0);
        osAndroid40Count.set(0);

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
        browserWgetCount.set(0);

        deviceAndroidCount.set(0);
        deviceIpadCount.set(0);
        deviceIphoneCount.set(0);
        deviceWindowsPhoneCount.set(0);

        jdbcFetchRowCount.set(0);
        jdbcFetchRowPeak.set(0);
        jdbcUpdateCount.set(0);
        jdbcExecuteCount.set(0);
        jdbcCommitCount.set(0);
        jdbcRollbackCount.set(0);
        jdbcTransactionTimeMillis.set(0);

        concurrentMax.set(0);
        count.set(0);
        errorCount.set(0);
        nanoTotal.set(0);
        nanoMax.set(0);
        lastError = null;
        lastSampleTime = 0;
        lastErrorReferer = null;
        lastErrorUser = null;
        lastErrorMessage = null;
        lastErrorUrl = null;// added by wangye

        profilerEntryStatMap.clear();

        histogram.reset();
        clearIp();
    }

    public long getDeviceAdndroidCount() {
        return StatUtils.get(deviceAndroidCount);
    }

    public long getDeviceIpadCount() {
        return StatUtils.get(deviceIpadCount);
    }

    public long getDeviceIphoneCount() {
        return StatUtils.get(deviceIphoneCount);
    }

    public long getDeviceWindowsPhoneCount() {
        return StatUtils.get(deviceWindowsPhoneCount);
    }

    public long getOSWindowsCount() {
        return StatUtils.get(osWindowsCount);
    }

    public long getOSMacOSX() {
        return StatUtils.get(osMacOSXCount);
    }

    public long getOSAndroidCount() {
        return StatUtils.get(osAndroidCount);
    }

    public long getOSAndroid15Count() {
        return StatUtils.get(osAndroid15Count);
    }

    public long getOSAndroid16Count() {
        return StatUtils.get(osAndroid16Count);
    }

    public long getOSAndroid20Count() {
        return StatUtils.get(osAndroid20Count);
    }

    public long getOSAndroid21Count() {
        return StatUtils.get(osAndroid21Count);
    }

    public long getOSAndroid22Count() {
        return StatUtils.get(osAndroid22Count);
    }

    public long getOSAndroid23Count() {
        return StatUtils.get(osAndroid23Count);
    }

    public long getOSAndroid30Count() {
        return StatUtils.get(osAndroid30Count);
    }

    public long getOSAndroid31Count() {
        return StatUtils.get(osAndroid31Count);
    }

    public long getOSAndroid32Count() {
        return StatUtils.get(osAndroid32Count);
    }

    public long getOSAndroid40Count() {
        return StatUtils.get(osAndroid40Count);
    }

    public long getOSLinuxCount() {
        return StatUtils.get(osLinuxCount);
    }

    public long getOSFreeBSDCount() {
        return StatUtils.get(osFreeBSDCount);
    }

    public long getOSOpenBSDCount() {
        return StatUtils.get(osOpenBSDCount);
    }

    public long getOSLinuxUbuntuCount() {
        return StatUtils.get(osLinuxUbuntuCount);
    }

    public long getOSSymbianCount() {
        return StatUtils.get(osSymbianCount);
    }

    public long getBrowserIECount() {
        return StatUtils.get(browserIECount);
    }

    public long getBrowserIE6Count() {
        return StatUtils.get(browserIE6Count);
    }

    public long getBrowserIE7Count() {
        return StatUtils.get(browserIE7Count);
    }

    public long getBrowserIE8Count() {
        return StatUtils.get(browserIE8Count);
    }

    public long getBrowserIE9Count() {
        return StatUtils.get(browserIE9Count);
    }

    public long getBrowserIE10Count() {
        return StatUtils.get(browserIE10Count);
    }

    public long getBrowserOperaCount() {
        return StatUtils.get(browserOperaCount);
    }

    public long getBrowserFirefoxCount() {
        return StatUtils.get(browserFirefoxCount);
    }

    public long getBrowserChromeCount() {
        return StatUtils.get(browserChromeCount);
    }

    public long getBrowserSafariCount() {
        return StatUtils.get(browserSafariCount);
    }

    public long getBrowserWgetCount() {
        return StatUtils.get(browserWgetCount);
    }

    public WebURIStatistic(String url){
        this.uri = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getURI() {
        return uri;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void beforeInvoke() {
        beforeInvoke(null);
    }

    public void computeUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() == 0) {
            return;
        }

        WebAppStatManager appStat = WebAppStatManager.getInstance();

        {
            final String prefix = "Mozilla/4.0 (compatible; MSIE ";
            if (userAgent.startsWith(prefix)) {
                browserIECount.incrementAndGet();
                appStat.incrementBrowserIECount();

                char version = ' ';
                if (userAgent.length() > prefix.length()) {
                    version = userAgent.charAt(prefix.length());
                }

                switch (version) {
                    case '6':
                        browserIE6Count.incrementAndGet();
                        appStat.incrementBrowserIE6Count();
                        break;
                    case '7':
                        browserIE7Count.incrementAndGet();
                        appStat.incrementBrowserIE7Count();
                        break;
                    case '8':
                        browserIE8Count.incrementAndGet();
                        appStat.incrementBrowserIE8Count();
                        break;
                    default:
                        break;
                }

                osWindowsCount.incrementAndGet();
                appStat.incrementOSWindowsCount();

                if (userAgent.indexOf("Windows Phone") != -1) {
                    deviceWindowsPhoneCount.incrementAndGet();
                    appStat.incrementDeviceWindowsPhoneCount();
                }

                return;
            }
        }
        {
            final String prefix = "Mozilla/5.0 (compatible; MSIE ";

            if (userAgent.startsWith(prefix)) {
                browserIECount.incrementAndGet();
                appStat.incrementBrowserIECount();

                char v1 = ' ', v2 = ' ';
                if (userAgent.length() > prefix.length() + 1) {
                    v1 = userAgent.charAt(prefix.length());
                    v2 = userAgent.charAt(prefix.length() + 1);
                } else if (userAgent.length() > prefix.length()) {
                    v1 = userAgent.charAt(prefix.length());
                }

                if (v1 == '9') {
                    browserIE9Count.incrementAndGet();
                    appStat.incrementBrowserIE9Count();
                } else if (v1 == '1' && v2 == '0') {
                    browserIE10Count.incrementAndGet();
                    appStat.incrementBrowserIE10Count();
                }

                osWindowsCount.incrementAndGet();
                appStat.incrementOSWindowsCount();

                if (userAgent.indexOf("Windows Phone") != -1) {
                    deviceWindowsPhoneCount.incrementAndGet();
                    appStat.incrementDeviceWindowsPhoneCount();
                }

                return;
            }
        }

        boolean isAndroid = false;

        if (userAgent.indexOf("Windows") != -1) {
            osWindowsCount.incrementAndGet();
            appStat.incrementOSWindowsCount();

            if (userAgent.indexOf("Windows Phone") != -1) {
                deviceWindowsPhoneCount.incrementAndGet();
                appStat.incrementDeviceWindowsPhoneCount();
            }
        } else if (userAgent.indexOf("Mac OS X") != -1) {
            osMacOSXCount.incrementAndGet();
            appStat.incrementOSMacOSX();
            if (userAgent.indexOf("iPad") != -1) {
                deviceIpadCount.incrementAndGet();
                appStat.incrementDeviceIpadCount();
            } else if (userAgent.indexOf("iPhone") != -1) {
                deviceIphoneCount.incrementAndGet();
                appStat.incrementDeviceIphoneCount();
            }
        } else if (userAgent.indexOf("Linux") != -1) {
            osLinuxCount.incrementAndGet();
            appStat.incrementOSLinuxCount();

            isAndroid = computeUserAgentAndroid(userAgent);
        } else if (userAgent.indexOf("Symbian") != -1) {
            osSymbianCount.incrementAndGet();
        } else if (userAgent.indexOf("Ubuntu") != -1) {
            osLinuxCount.incrementAndGet();
            osLinuxUbuntuCount.incrementAndGet();
        } else if (userAgent.indexOf("OpenBSD") != -1) {
            osOpenBSDCount.incrementAndGet();
        } else if (userAgent.indexOf("FreeBSD") != -1) {
            osFreeBSDCount.incrementAndGet();
        }

        if (userAgent.indexOf("Chrome") != -1) {
            browserChromeCount.incrementAndGet();
            appStat.incrementBrowserChromeCount();
            return;
        }

        if ((!isAndroid) && userAgent.indexOf("Safari") != -1) {
            browserSafariCount.incrementAndGet();
            appStat.incrementBrowserSafariCount();
            return;
        }

        if (userAgent.indexOf("Firefox") != -1) {
            browserFirefoxCount.incrementAndGet();
            appStat.incrementBrowserFirefoxCount();
            return;
        }

        if (userAgent.indexOf("Opera/") != -1) {
            browserOperaCount.incrementAndGet();
            appStat.incrementBrowserOperaCount();
            return;
        }

    }

    private boolean computeUserAgentAndroid(String userAgent) {
        int index = userAgent.indexOf("Android");
        if (index != -1) {
            osAndroidCount.incrementAndGet();

            deviceAndroidCount.incrementAndGet();
            WebAppStatManager.getInstance().incrementDeviceAdndroidCount();

            int toffset = index + "Android ".length();
            if (userAgent.startsWith("1.5", toffset)) {
                osAndroid15Count.incrementAndGet();
            } else if (userAgent.startsWith("1.6", toffset)) {
                osAndroid16Count.incrementAndGet();
            } else if (userAgent.startsWith("2.0", toffset)) {
                osAndroid20Count.incrementAndGet();
            } else if (userAgent.startsWith("2.1", toffset)) {
                osAndroid21Count.incrementAndGet();
            } else if (userAgent.startsWith("2.2", toffset)) {
                osAndroid22Count.incrementAndGet();
            } else if (userAgent.startsWith("2.3.3", toffset)) {
                osAndroid23Count.incrementAndGet();
            } else if (userAgent.startsWith("2.3.4", toffset)) {
                osAndroid23Count.incrementAndGet();
            } else if (userAgent.startsWith("3.0", toffset)) {
                osAndroid30Count.incrementAndGet();
            } else if (userAgent.startsWith("3.1", toffset)) {
                osAndroid31Count.incrementAndGet();
            } else if (userAgent.startsWith("3.2", toffset)) {
                osAndroid32Count.incrementAndGet();
            } else if (userAgent.startsWith("4.0", toffset)) {
                osAndroid40Count.incrementAndGet();
            }

            return true;
        }

        return false;
    }

    public void beforeInvoke(HttpServletRequest request) {
        if (request == null) {
            request = PatrolWebUtils.getHttpServletRequest();
        }

        if (request != null) {
            String userAgent = request.getHeader("user-agent");
            computeUserAgent(userAgent);
            addIp(request);
        }

        WebAppStatManager.getInstance().incrementRunningCount();

        int invoking = runningCount.incrementAndGet();

        for (;;) {
            int max = concurrentMax.get();
            if (invoking > max) {
                if (concurrentMax.compareAndSet(max, invoking)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }

        count.incrementAndGet();
        lastSampleTime = System.currentTimeMillis();

        currentLocal.set(this);
    }

    public long getNanoMax() {
        return nanoMax.get();
    }

    public long getErrorCount() {
        return StatUtils.get(errorCount);
    }

    public int getRunningCount() {
        return runningCount.get();
    }

    public int getConcurrentMax() {
        return StatUtils.get(concurrentMax);
    }

    public long getCountDirect() {
        return count.get();
    }

    public long getCount() {
        return StatUtils.get(count);
    }

    public Date getLastAccessTime() {
        if (lastSampleTime == 0) {
            return null;
        }

        return new Date(lastSampleTime);
    }

    public long getNanoTotal() {
        return StatUtils.get(nanoTotal);
    }

    public void afterInvoke(long nanoSpan) {
        runningCount.decrementAndGet();

        nanoTotal.addAndGet(nanoSpan);

        for (;;) {
            long max = nanoMax.get();
            if (nanoSpan > max) {
                if (nanoMax.compareAndSet(max, nanoSpan)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }

        long millis = nanoSpan / (1000 * 1000);
        histogram.recode(millis);

        WebAppStatManager.getInstance().decrementRunningCount();
        currentLocal.set(null);
    }

    public Throwable getLastError() {
        Throwable lastError = this.lastError;
        if (StatUtils.isRequiredReset()) {
            this.lastError = null;
        }
        return lastError;
    }

    public Date getLastErrorTime() {
        if (lastErrorTime <= 0) {
            return null;
        }

        return new Date(lastErrorTime);
    }

    public String getLastErrorReferer() {
        String val = lastErrorReferer;
        if (StatUtils.isRequiredReset()) {
            this.lastErrorReferer = null;
        }
        return val;
    }

    /**
     * @author wangye
     */
    public String getLastErrorUrl() {
        String val = lastErrorUrl;
        if (StatUtils.isRequiredReset()) {
            this.lastErrorUrl = null;
        }
        return val;
    }

    public void error(String errorMessage, Throwable error, String user, String referer, String url) {
        // 有可能多个线程一起修改，但不作并发控制 modified by wangye
        lastErrorUser = user;
        lastErrorReferer = referer;
        errorCount.incrementAndGet();
        lastError = error;
        lastErrorUrl = url;
        lastErrorTime = System.currentTimeMillis();
        lastErrorMessage = null;
    }

    public void error(Throwable error, String user, String referer, String url) {
        error(null, error, user, referer, url);
    }

    public static CompositeType getCompositeType() throws JMException {

        OpenType<?>[] indexTypes = new OpenType<?>[] {
                //
                SimpleType.LONG, SimpleType.STRING, SimpleType.INTEGER, SimpleType.INTEGER,
                SimpleType.LONG, // count
                SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.STRING,
                SimpleType.STRING,// LastErrorClass
                SimpleType.STRING, SimpleType.DATE, SimpleType.STRING, SimpleType.DATE,
                SimpleType.STRING // LastErrorUser
                , SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                SimpleType.LONG, // LastErrorUser
                SimpleType.LONG, new ArrayType<Long>(SimpleType.LONG, true),
                new ArrayType<CompositeType>(1, Profiler.EntryStatistic.getCompositeType())//
                , SimpleType.STRING //
                //
                , SimpleType.LONG, SimpleType.LONG, SimpleType.LONG//
                //
                , SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG //
                , SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG//
                , SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG //
                , new ArrayType<Integer>(SimpleType.INTEGER, true), SimpleType.LONG };

        String[] indexNames = {//
                //
                "ID", "URI",
                "RunningCount",
                "ConcurrentMax",
                "Count", //
                "ErrorCount", "TotalTime",
                "MaxTime",
                "LastErrorMessage",
                "LastErrorClass" //
                , "LastErrorStackTrace", "LastErrorTime",
                "LastErrorReferer",
                "LastAccessedTime",
                "LastErrorUser" //
                , "JdbcExecuteCount", "JdbcFetchRowCount",
                "JdbcFetchRowPeak",
                "JdbcUpdateCount",
                "JdbcCommitCount" //
                , "JdbcRollbackCount",
                "Histogram",
                "ProfileEntries",
                "LastErrorUrl" // added by wangye

                , "OSMacOSXCount", "OSWindowsCount", "OSLinuxCount", "BrowserIE6Count", "BrowserIE7Count",
                "BrowserIE8Count", "BrowserIE9Count", "BrowserIE10Count", "BrowserIECount", "BrowserFirefoxCount",
                "BrowserChromeCount", "BrowserSafariCount", "BrowserOperaCount", "DeviceAndroidCount",
                "DeviceIpadCount", "DeviceIphoneCount", "DeviceWindowsPhoneCount" //
                , "IP", "JdbcTransactionMillis" };
        String[] indexDescriptions = indexNames;
        return new CompositeType("URIStatistic", "URI Statistic", indexNames, indexDescriptions, indexTypes);
    }

    public CompositeDataSupport getCompositeData() throws JMException {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("ID", getId());
        map.put("URI", getURI());
        map.put("RunningCount", getRunningCount());
        map.put("ConcurrentMax", getConcurrentMax());
        map.put("Count", getCount());

        map.put("ErrorCount", getErrorCount());
        map.put("TotalTime", getNanoTotal() / (1000 * 1000));
        map.put("MaxTime", getNanoMax() / (1000 * 1000));

        Throwable lastError = getLastError();
        if (lastError != null) {
            if (lastErrorMessage != null) {
                map.put("LastErrorMessage", lastErrorMessage);
            } else {
                map.put("LastErrorMessage", lastError.getMessage());
            }
            map.put("LastErrorClass", lastError.getClass().getName());

            StringWriter buf = new StringWriter();
            lastError.printStackTrace(new PrintWriter(buf));
            map.put("LastErrorStackTrace", buf.toString());
        } else {
            map.put("LastErrorMessage", lastErrorMessage);
            map.put("LastErrorClass", null);
            map.put("LastErrorStackTrace", null);
        }

        map.put("LastErrorTime", getLastErrorTime());

        map.put("LastErrorReferer", getLastErrorReferer());
        map.put("LastAccessedTime", getLastAccessTime());
        map.put("LastErrorUser", getLastErrorUser());
        map.put("LastErrorUrl", getLastErrorUrl());// added by wangye

        map.put("JdbcExecuteCount", getJdbcExecuteCount());
        map.put("JdbcFetchRowCount", getJdbcFetchRowCount());
        map.put("JdbcFetchRowPeak", getJdbcFetchRowPeak());
        map.put("JdbcUpdateCount", getJdbcUpdateCount());
        map.put("JdbcCommitCount", getJdbcCommitCount());
        map.put("JdbcRollbackCount", getJdbcRollbackCount());
        map.put("Histogram", histogram.toArray());

        EntryStatistic[] profilerEntryStatArray = this.profilerEntryStatMap.values().toArray(new EntryStatistic[this.profilerEntryStatMap.values().size()]);
        List<CompositeDataSupport> profilerEntryStatCompositeDataList = new ArrayList<CompositeDataSupport>();
        for (int i = 0; i < profilerEntryStatArray.length; ++i) {
            if (profilerEntryStatArray[i].getCount() == 0) {
                continue;
            }
            profilerEntryStatCompositeDataList.add(profilerEntryStatArray[i].getCompositeData());
        }

        CompositeDataSupport[] profilerEntryStatCompositeDataArray = new CompositeDataSupport[profilerEntryStatCompositeDataList.size()];
        profilerEntryStatCompositeDataList.toArray(profilerEntryStatCompositeDataArray);

        map.put("ProfileEntries", profilerEntryStatCompositeDataArray);

        map.put("OSMacOSXCount", getOSMacOSX());
        map.put("OSWindowsCount", getOSWindowsCount());
        map.put("OSLinuxCount", getOSLinuxCount());

        map.put("BrowserIE6Count", getBrowserIE6Count());
        map.put("BrowserIE7Count", getBrowserIE7Count());
        map.put("BrowserIE8Count", getBrowserIE8Count());
        map.put("BrowserIE9Count", getBrowserIE9Count());
        map.put("BrowserIE10Count", getBrowserIE10Count());

        map.put("BrowserIECount", getBrowserIECount());
        map.put("BrowserFirefoxCount", getBrowserFirefoxCount());
        map.put("BrowserChromeCount", getBrowserChromeCount());
        map.put("BrowserSafariCount", getBrowserSafariCount());
        map.put("BrowserOperaCount", getBrowserOperaCount());

        map.put("DeviceAndroidCount", getDeviceAdndroidCount());
        map.put("DeviceIpadCount", getDeviceIpadCount());
        map.put("DeviceIphoneCount", getDeviceIphoneCount());
        map.put("DeviceWindowsPhoneCount", getDeviceWindowsPhoneCount());
        map.put("IP", getIP());
        map.put("JdbcTransactionMillis", getJdbcTransactionTimeMillis());

        return new CompositeDataSupport(getCompositeType(), map);
    }

    public String getLastErrorUser() {
        String val = lastErrorUser;
        if (StatUtils.isRequiredReset()) {
            this.lastErrorUser = null;
        }
        return val;
    }

    public void recordProfilerInfo(Profiler.Entry profilerEntry) {
        try {
            profilerEntry.accept(this.profilerEntryVisitor);
        } catch (Exception ex) {
            LOG.error("recordProfilerInfo error", ex);
        }
    }

    public void addJdbcFetchRowCount(long delta) {
        for (;;) {
            long peak = jdbcFetchRowPeak.get();
            if (delta <= peak) {
                break;
            }

            if (jdbcFetchRowPeak.compareAndSet(peak, delta)) {
                break;
            }
        }
        this.jdbcFetchRowCount.addAndGet(delta);
    }

    public long getJdbcFetchRowCount() {
        return StatUtils.get(jdbcFetchRowCount);
    }

    public long getJdbcFetchRowPeak() {
        return StatUtils.get(jdbcFetchRowPeak);
    }

    public void addJdbcUpdateCount(int delta) {
        this.jdbcUpdateCount.addAndGet(delta);
    }

    public long getJdbcUpdateCount() {
        return StatUtils.get(jdbcUpdateCount);
    }

    public void incrementJdbcExecuteCount() {
        jdbcExecuteCount.incrementAndGet();
    }

    public long getJdbcExecuteCount() {
        return StatUtils.get(jdbcExecuteCount);
    }

    public void incrementJdbcCommitCount() {
        jdbcCommitCount.incrementAndGet();
    }

    public long getJdbcCommitCount() {
        return StatUtils.get(jdbcCommitCount);
    }

    public void incrementJdbcRollbackCount() {
        jdbcRollbackCount.incrementAndGet();
    }

    public long getJdbcRollbackCount() {
        return StatUtils.get(jdbcRollbackCount);
    }

    public void incrementJdbcTransactionTimeMillis(long millis) {
        jdbcTransactionTimeMillis.addAndGet(millis);
    }

    public long getJdbcTransactionTimeMillis() {
        return StatUtils.get(jdbcTransactionTimeMillis);
    }

    public void setLastErrorUrl(String lastErrorUrl) {
        this.lastErrorUrl = lastErrorUrl;
    }
}
