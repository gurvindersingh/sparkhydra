package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.*;
import org.apache.spark.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.MachineUseAccumulator
 * track on which machine a an entry is made
 * User: Steve
 * Date: 11/24/2014
 */
public class MachineUseAccumulator implements Serializable {
    public static final MachineUseAccumulableParam PARAM_INSTANCE = new MachineUseAccumulableParam();

    public static class MachineUseAccumulableParam implements AccumulatorParam<MachineUseAccumulator>, Serializable {
        private MachineUseAccumulableParam() {
        }

        @Override
        public MachineUseAccumulator addAccumulator(final MachineUseAccumulator t1, final MachineUseAccumulator t2) {
            t1.addAll(t2);
            return new MachineUseAccumulator(t1);
        }

        /**
         * Merge two accumulated values together. Is allowed to modify and return the first value
         * for efficiency (to avoid allocating objects).
         *
         * @param r1 one set of accumulated data
         * @param r2 another set of accumulated data
         * @return both data sets merged together
         */
        @Override
        public MachineUseAccumulator addInPlace(final MachineUseAccumulator r1, final MachineUseAccumulator r2) {
            r1.addAll(r2);
            return r1;
        }

        /**
         * Return the "zero" (identity) value for an accumulator type, given its initial value. For
         * example, if R was a vector of N dimensions, this would return a vector of N zeroes.
         *
         * @param initialValue
         */
        @Override
        public MachineUseAccumulator zero(final MachineUseAccumulator initialValue) {
            return new MachineUseAccumulator(initialValue);
        }
    }

    /**
     * convenient class to report results of machine use
     */
    public static class CountedItem implements Comparable<CountedItem>, Serializable {
        private final String m_Value;
        private final long m_Count;

        public CountedItem(final String pValue, final long pCount) {
            m_Value = pValue;
            m_Count = pCount;
        }

        public String getValue() {
            return m_Value;
        }

        public long getCount() {
            return m_Count;
        }

        /**
         * sort value - then count
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(final CountedItem o) {

            int ret = getValue().compareTo(o.getValue());
            if(ret != 0)
                return ret; // sort by name
            long count = getCount();
            long ocount = o.getCount();
            // High count first
            if (count != ocount)
                return count > ocount ? -1 : 1;
            return 0;
        }

        @Override
        public String toString() {
            return getValue() + ":" + SparkUtilities.formatLargeNumber(getCount());
        }

    }

    // key is the machine MAC address
    private Map<String, Long> items = new HashMap<String, Long>();
    private long totalCalls;  // number function calls
    private long totalTime;   // call time in nanosec

    /**
     * will be called to count use on a single machine
     */
    public MachineUseAccumulator() {
    }

    /**
     * will be called to count use on a single machine
     */
    public MachineUseAccumulator(long n,long totalTime) {
        this();
        add(n,totalTime);
    }

    /**
     * copy constructor
     */
    public MachineUseAccumulator(MachineUseAccumulator copy) {
        this();
        items.putAll(copy.items);
        totalTime += copy.getTotalTime();
    }


    public void add(long value,long totalT) {
        String macAddress = SparkUtilities.getMacAddress();
        addEntry(macAddress, value);
        totalTime +=  totalT;
    }

    protected void addEntry(String entry, long value) {
        long present = 0;
        if (items.containsKey(entry))
            present += items.get(entry);
        long value1 = value + present;
        items.put(entry, value1);
        totalCalls += value;
    }


    protected void addAll(MachineUseAccumulator added) {
        for (String t : added.items.keySet()) {
            long value = added.get(t);
            addEntry(t, value);
        }
        totalTime += added.getTotalTime();
    }


    public long get(String item) {
        if (items.containsKey(item)) {
            return items.get(item);
        }
        return 0;
    }

    public long getTotalCalls() {
        if (totalCalls == 0)
            totalCalls = computeTotal();
        return totalCalls;
    }

    public long computeTotal() {
        long sum = 0;
        for (Long v : items.values()) {
            sum += v;
        }
        return sum;
    }



    public long getTotalTime() {
           return totalTime;
    }


    public int size() {
        return items.size();
    }

    /**
     * return counts with high first
     *
     * @return
     */
    public List<CountedItem> asCountedItems() {
        List<CountedItem> holder = new ArrayList<CountedItem>();
        for (String s : items.keySet()) {
            holder.add(new CountedItem(s, items.get(s)));
        }
        Collections.sort(holder);
        return holder;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" totalCalls:");
        long total1 = getTotalCalls();
        sb.append(SparkUtilities.formatLargeNumber(total1));
        sb.append("\n");

        sb.append(" totalTime:");
          long totaltime = getTotalTime();
          sb.append(SparkUtilities.formatNanosec(totaltime));
          sb.append("\n");

        sb.append(" totalCalls entries:");
        sb.append(size());
        sb.append("\n");

        List<CountedItem> items = asCountedItems();
        for (CountedItem item : items) {
            sb.append(item.toString());
            sb.append("\n");
        }

        return sb.toString();
    }


}
