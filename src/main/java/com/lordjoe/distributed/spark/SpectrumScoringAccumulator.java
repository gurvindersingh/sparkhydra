package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.*;
import org.apache.spark.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.SpectrumScoringAccumulator
 * track the number and names of for the scoring of a peptide with a specific ID
 * User: Steve
 * Date: 11/24/2014
 */
public class SpectrumScoringAccumulator implements Serializable {
    public static final SpectrumScoringAccumulableParam PARAM_INSTANCE = new SpectrumScoringAccumulableParam();

    public static class SpectrumScoringAccumulableParam implements AccumulatorParam<SpectrumScoringAccumulator>, Serializable {
        private SpectrumScoringAccumulableParam() {
        }

        @Override
        public SpectrumScoringAccumulator addAccumulator(final SpectrumScoringAccumulator t1, final SpectrumScoringAccumulator t2) {
            t1.addAll(t2);
            return new SpectrumScoringAccumulator(t1);
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
        public SpectrumScoringAccumulator addInPlace(final SpectrumScoringAccumulator r1, final SpectrumScoringAccumulator r2) {
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
        public SpectrumScoringAccumulator zero(final SpectrumScoringAccumulator initialValue) {
            return new SpectrumScoringAccumulator(initialValue);
        }
    }

    // key is the machine MAC address
    public final String scoredID;
    private Map<String, Long> items = new HashMap<String, Long>();
    private long totalCalls;  // number function calls

    /**
     * will be called to count use on a single machine
     */
    public SpectrumScoringAccumulator(String id) {
        scoredID = id;
    }

    /**
     * will be called to count use on a single machine
     */
    public SpectrumScoringAccumulator(String id,String peptide,long value ) {
        this(id);
        addEntry(peptide,value);
    }

    /**
     * copy constructor
     */
    public SpectrumScoringAccumulator(SpectrumScoringAccumulator copy) {
        this(copy.scoredID);
        items.putAll(copy.items);
     }



    protected void addEntry(String peptide,long value) {
        long present = 0;
        if (items.containsKey(peptide))
            present += items.get(peptide);
         items.put(peptide,present + value);
        }


    protected void addAll(SpectrumScoringAccumulator added) {
        for (String t : added.items.keySet()) {
            long value = added.get(t);
            addEntry(t, value);
        }
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
