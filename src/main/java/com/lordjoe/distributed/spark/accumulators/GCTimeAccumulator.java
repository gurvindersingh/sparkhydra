package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.algorithms.*;
import com.lordjoe.testing.*;
import org.apache.spark.*;

import java.io.*;
import java.lang.management.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.accumulators.GCTimeAccumulator
 * Accululator to look at garbage collection use
 * call checkGCTime()  to save gc use
 *
 * @author Steve Lewis
 * @date 5/28/2015
 */
public class GCTimeAccumulator implements IAccumulator<GCTimeAccumulator> {

    public static final String GCTIME_ACCUMULATOR_NAME = "GCTimeAccumulator";

    public static class GCUseStatistics implements Serializable {
        private transient GarbageCollectorMXBean bean;
        public final long gcCount;
        public final long gcTime;

        public GCUseStatistics(final GCStatistics pBean) {
            gcCount = pBean.usedGCCount();
            gcTime = pBean.usedGCTime();
        }
    }

    public static class GCStatistics  {
        private transient GarbageCollectorMXBean bean;
        public final long startGCCount;
        public final long startGCTime;

        public GCStatistics(final GarbageCollectorMXBean pBean) {
            bean = pBean;
            startGCCount = pBean.getCollectionCount();
            startGCTime = pBean.getCollectionTime();
        }

        public long usedGCCount() {
            return bean.getCollectionCount() - startGCCount;
        }

        public long usedGCTime() {
            return bean.getCollectionTime() - startGCTime;
        }
    }

    public static final int MEG_40 = 10000000; // 10 MB
    public static final int MAX_TRACKED_10_MEG_CHUNKS = 2000; // up to 20 gb
    public static final int MEG_4 = 4000000; // 1 MB
    public static final int MAX_TRACKED_4_MEG_CHUNKS = 2000; // up to 20 gb

    // how to build this type of accumulator
    public static final AccumulatorParam<GCTimeAccumulator> PARAM_INSTANCE = new IAccumulatorParam<GCTimeAccumulator>();

    public static GCTimeAccumulator empty() {
        return new GCTimeAccumulator();
    }

    private transient long startAllocation;
    private transient long maxAllocated;
    private transient long startRunTime;
    private transient List<GCStatistics> gcmxb;

    private long maxHeap;
    private final Map<Integer,GCUseStatistics> binToUse = new HashMap<Integer, GCUseStatistics>();

    /**
     * Use static method empty
     */
    private GCTimeAccumulator() {
        startAllocation = MemoryTracker.usedBytes();
        maxHeap = startAllocation;
        maxAllocated = 0;
        startRunTime = System.nanoTime();
    }

    protected void checkGCTime() {
        if (gcmxb == null) {   // first run
            gcmxb = new ArrayList<GCStatistics>();
            for (GarbageCollectorMXBean ob : ManagementFactory.getGarbageCollectorMXBeans()) {
                gcmxb.add(new GCStatistics(ob));
            }
        }


    }

    /**
     * given a value return it as 0
     * default behavior os th return the value itself
     *
     * @return
     */
    @Override
    public GCTimeAccumulator asZero() {
        return empty();
    }


    public void check() {
        long current = MemoryTracker.usedBytes();
        long allocated = current - startAllocation;
        maxHeap = Math.max(maxHeap, current);
        maxAllocated = Math.max(maxAllocated, allocated);
    }





    public GCTimeAccumulator add(GCTimeAccumulator added) {

        maxHeap = Math.max(maxHeap, added.maxHeap);
        binToUse.putAll(added.binToUse);
        return this;
    }


    /**
     * like toString but might add more information than a shorter string
     * usually implemented bu appending toString
     *
     * @param out
     */
    @Override
    public void buildReport(final Appendable out) {
        try {
            out.append(toString());
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("GC Time Max Allocation " + Long_Formatter.format(maxHeap));
        sb.append("\n");
    //    throw new UnsupportedOperationException("Fix This"); // ToDo

        return sb.toString();
    }


}
