package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.CountedItem;
import org.apache.spark.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.accumulators.MachineUseAccumulator
 * track on which machine a an entry is made
 * User: Steve
 * Date: 11/24/2014
 */
public class MachineUseAccumulator implements IAccumulator<MachineUseAccumulator> {
    public static final AccumulatorParam<MachineUseAccumulator> PARAM_INSTANCE = new IAccumulatorParam<MachineUseAccumulator>();

    public static MachineUseAccumulator empty() {
       return new MachineUseAccumulator();
    }


    // key is the machine MAC address
    private Map<String, Long> items = new HashMap<String, Long>();
    private long totalCalls;  // number function calls
    private long totalTime;   // call time in nanosec

    /**
     * Use static method empty
     */
     private MachineUseAccumulator() {
    }

    /**
     * will be called to count use on a single machine
     */
    public MachineUseAccumulator(long n, long totalTime) {
        this();
        add(n, totalTime);
    }

    /**
     * copy constructor
     */
    public MachineUseAccumulator(MachineUseAccumulator copy) {
        this();
        items.putAll(copy.items);
        totalTime += copy.getTotalTime();
    }


    public void add(long value, long totalT) {
        String macAddress = SparkUtilities.getMacAddress();
        addEntry(macAddress, value);
        totalTime += totalT;
    }

    protected void addEntry(String entry, long value) {
        long present = 0;
        if (items.containsKey(entry))
            present += items.get(entry);
        long value1 = value + present;
        items.put(entry, value1);
        totalCalls += value;
    }


    public MachineUseAccumulator add(MachineUseAccumulator added) {
        for (String t : added.items.keySet()) {
            long value = added.get(t);
            addEntry(t, value);
        }
        totalTime += added.getTotalTime();
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

    /**
     * given a value return it as 0
     * default behavior os th return the value itself
     *
     * @return
     */
    @Override
    public MachineUseAccumulator asZero() {
        return empty();
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

    public String getBalanceReport() {
        List<CountedItem> items = asCountedItems();
        int n = items.size();
        if (n < 2)
            return " variance 0"; // one machine is balanced

        long[] calls = new long[n];
        int index = 0;
        for (CountedItem item : items) {
            long count = item.getCount();
            calls[index++] = count;
        }
        return generateVariance(  calls);
    }

    public static String generateVariance( final long[] pCalls) {
        StringBuilder sb = new StringBuilder();
        double min = Double.MAX_VALUE;
        double max = 0;
        double sum = 0;
        double sumsq = 0;

        for (int i = 0; i < pCalls.length; i++) {
            long count = pCalls[i];
            min = Math.min(min, count);
            max = Math.max(max, count);
            sum += count;
            sumsq += count * count;

        }
        int n = pCalls.length;
        double average = sum / n;
        double sdsq = (n * sumsq - sum * sum) / (n * (n - 1));
        double sd = Math.sqrt(sdsq);
        sb.append(" variance " + String.format("%6.3f", (sd / average)));
        return sb.toString();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" totalCalls:");
        long total1 = getTotalCalls();
        sb.append(SparkUtilities.formatLargeNumber(total1));

        sb.append(" totalTime:");
        long totaltime = getTotalTime();
        sb.append(SparkUtilities.formatNanosec(totaltime));

        sb.append(" machines:");
        sb.append(size());

        sb.append(getBalanceReport());

//        List<CountedItem> items = asCountedItems();
//        for (CountedItem item : items) {
//            sb.append(item.toString());
//            sb.append("\n");
//        }

        return sb.toString();
    }


}
