package com.alibaba.dragoon.stat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.dragoon.common.utils.StatUtils;
import com.alibaba.dragoon.stat.Profiler.EntryStatistic;

public class TaskStat {

    private final String                                name;

    private final static Log                            LOG                  = LogFactory.getLog(WebURIStatistic.class);
    private long                                        id;

    private final AtomicInteger                         runningCount         = new AtomicInteger();
    private final AtomicInteger                         concurrentMax        = new AtomicInteger();

    private final AtomicLong                            count                = new AtomicLong(0);
    private final AtomicLong                            errorCount           = new AtomicLong();

    private final AtomicLong                            nanoTotal            = new AtomicLong(0);
    private final AtomicLong                            nanoMax              = new AtomicLong(0);
    private Throwable                                   lastError;
    private long                                        lastErrorTime;
    private long                                        lastSampleTime       = 0;
    private String                                      lastErrorReferer;
    private String                                      lastErrorUser;
    /**
     * 出错情况下原始的带全路径的url（get方式的时候保留原始参数）
     * 
     * @author wangye
     */
    private String                                      lastErrorUrl;

    private final AtomicLong                            jdbcFetchRowCount    = new AtomicLong();
    private final AtomicLong                            jdbcFetchRowPeak     = new AtomicLong();
    private final AtomicLong                            jdbcUpdateCount      = new AtomicLong();
    private final AtomicLong                            jdbcExecuteCount     = new AtomicLong();
    private final AtomicLong                            jdbcCommitCount      = new AtomicLong();
    private final AtomicLong                            jdbcRollbackCount    = new AtomicLong();

    private final ConcurrentMap<String, EntryStatistic> profilerEntryStatMap = new ConcurrentHashMap<String, EntryStatistic>();
    private final ProfilerEntryVisitor                  profilerEntryVisitor = new ProfilerEntryVisitor(
                                                                                                        profilerEntryStatMap);

    private final Histogram                             histogram            = new Histogram(new long[] { //
                                                                                                          //
            10, 100, 1000, 10 * 1000, 100 * 1000, 1000 * 1000, 1000 * 1000 * 10                               });

    private final static ThreadLocal<TaskStat>          currentLocal         = new ThreadLocal<TaskStat>();

    public TaskStat(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void reset() {
        jdbcFetchRowCount.set(0);
        jdbcFetchRowPeak.set(0);
        jdbcUpdateCount.set(0);
        jdbcExecuteCount.set(0);
        jdbcCommitCount.set(0);
        jdbcRollbackCount.set(0);

        concurrentMax.set(0);
        count.set(0);
        errorCount.set(0);
        nanoTotal.set(0);
        nanoMax.set(0);
        lastError = null;
        lastSampleTime = 0;
        lastErrorReferer = null;
        lastErrorUser = null;
        lastErrorUrl = null;// added by wangye

        profilerEntryStatMap.clear();

        histogram.reset();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void beforeInvoke() {
        TaskStatManager.getInstance().incrementRunningCount();

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

        TaskStatManager.getInstance().decrementRunningCount();
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

    public void error(Throwable error, String user, String referer, String url) {
        // 有可能多个线程一起修改，但不作并发控制 modified by wangye
        lastErrorUser = user;
        lastErrorReferer = referer;
        errorCount.incrementAndGet();
        lastError = error;
        lastErrorUrl = url;
        lastErrorTime = System.currentTimeMillis();
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
        };

        String[] indexNames = {//
        //
                "ID", "URI", "RunningCount", "ConcurrentMax", "Count", //
                "ErrorCount", "TotalTime", "MaxTime", "LastErrorMessage", "LastErrorClass" //
                , "LastErrorStackTrace", "LastErrorTime", "LastErrorReferer", "LastAccessedTime", "LastErrorUser" //
                , "JdbcExecuteCount", "JdbcFetchRowCount", "JdbcFetchRowPeak", "JdbcUpdateCount", "JdbcCommitCount" //
                , "JdbcRollbackCount", "Histogram", "ProfileEntries", "LastErrorUrl" // added by wangye

        };
        String[] indexDescriptions = indexNames;
        return new CompositeType("URIStatistic", "URI Statistic", indexNames, indexDescriptions, indexTypes);
    }

    public CompositeDataSupport getCompositeData() throws JMException {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("ID", getId());
        map.put("Name", getName());
        map.put("RunningCount", getRunningCount());
        map.put("ConcurrentMax", getConcurrentMax());
        map.put("Count", getCount());

        map.put("ErrorCount", getErrorCount());
        map.put("TotalTime", getNanoTotal() / (1000 * 1000));
        map.put("MaxTime", getNanoMax() / (1000 * 1000));

        Throwable lastError = getLastError();
        if (lastError != null) {
            map.put("LastErrorMessage", lastError.getMessage());
            map.put("LastErrorClass", lastError.getClass().getName());

            StringWriter buf = new StringWriter();
            lastError.printStackTrace(new PrintWriter(buf));
            map.put("LastErrorStackTrace", buf.toString());
        } else {
            map.put("LastErrorMessage", null);
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

    public void setLastErrorUrl(String lastErrorUrl) {
        this.lastErrorUrl = lastErrorUrl;
    }
}
