package org.systemsbiology.xtandem.hadoop;

import com.lordjoe.utilities.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.JXTandemStatistics
 * class to keep statistics on a job run
 * User: Steve
 * Date: 11/1/11
 */
public class JXTandemStatistics implements Serializable{

    public static final int ONE_SEC = 1000;
    public static final int ONE_MIN = 60 * ONE_SEC;
    public static final int ONE_HOUR = 60 * ONE_MIN;
    public static final int ONE_DAY = 60 * ONE_HOUR;

    /**
     * present a duration as a string
      * @param time
     * @return
     */
    public static String durationString(long time)
    {
        if(time > 2 * ONE_DAY) {
             return String.format("%5.2f days",time / (double)ONE_DAY);
         }
        if(time > 4 * ONE_HOUR) {
             return String.format("%5.2f hours",time / (double)ONE_HOUR);
         }
        if(time > 4 * ONE_MIN) {
             return String.format("%5.2f min",time / (double)ONE_MIN);
         }
        if(time >   ONE_SEC) {
             return String.format("%5.2f sec",time / (double)ONE_SEC);
         }
        return String.format("%5d millisec",time );
     }

    public static class JobTime {
        private final String m_Name;
        private final Long m_Time;

        private JobTime(final String pName, final Long pTime) {
            m_Name = pName;
            m_Time = pTime;
        }

        @Override
        public String toString() {
            return m_Name + " took " + durationString(m_Time);
        }
    }

    private final ElapsedTimer m_TotalTime = new ElapsedTimer();
    private final Map<String, Long> m_Jobs = new HashMap<String, Long>();
    private final List<JobTime> m_JobTimes = new ArrayList<JobTime>();
    private final Map<String, String> m_Data = new HashMap<String, String>();

    public JXTandemStatistics() {
    }

    public ElapsedTimer getTotalTime() {
        return m_TotalTime;
    }

    public void startJob(String name) {
        m_Jobs.put(name, System.currentTimeMillis());

    }

    public void setData(String name, String value) {
        if(value == null)
            m_Data.remove(name);
        else
            m_Data.put(name, value);

    }

    public String getData(String name) {
        return m_Data.get(name);

    }


    public String[] getDataKeys() {
        Set<String> stx = m_Data.keySet();
        String[] ret = stx.toArray(new String[stx.size()]);
        Arrays.sort(ret);
        return ret;
    }


    public JobTime[] getJobTimes() {
        JobTime[] ret = m_JobTimes.toArray(new JobTime[m_JobTimes.size()]);
         return ret;
    }

    public void endJob(String name) {
        long now = System.currentTimeMillis();
        m_JobTimes.add(new JobTime(name, now - m_Jobs.get(name)));
        m_Jobs.remove(name);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] keys = getDataKeys();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            sb.append(key + " = " + getData(key) + "\n");
        }
         sb.append( m_TotalTime.formatElapsed("total time") + "\n");

        JobTime[] jobs = getJobTimes();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < jobs.length; i++) {
            JobTime job = jobs[i];
            //noinspection StringConcatenationInsideStringBufferAppend
            sb.append(job.toString() + "\n");
        }

        return sb.toString();
    }



}
