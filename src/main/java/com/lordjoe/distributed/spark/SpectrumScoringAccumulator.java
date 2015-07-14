package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.accumulators.*;
import org.apache.spark.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spark.SpectrumScoringAccumulator
 * track the number and names of for the scoring of a peptide with a specific ID
 * User: Steve
 * Date: 11/24/2014
 */
public class SpectrumScoringAccumulator implements IAccumulator<SpectrumScoringAccumulator> {
    public static final AccumulatorParam<SpectrumScoringAccumulator> PARAM_INSTANCE = new IAccumulatorParam<SpectrumScoringAccumulator>();

     public static SpectrumScoringAccumulator empty() {
        return new SpectrumScoringAccumulator();
     }


    // key is the machine MAC address
    public final String scoredID;
    private Map<String, Long> items = new HashMap<String, Long>();
    private long totalCalls;  // number function calls

    private  SpectrumScoringAccumulator() {
        scoredID = null;
    }
    /**
     * will be called to count use on a single machine
     */
    public SpectrumScoringAccumulator(String id) {
        scoredID = id;
    }

    /**
     * add the accumulated data to another instance
     *
     * @param added
     * @return
     */
    @Override
    public SpectrumScoringAccumulator add(final SpectrumScoringAccumulator added) {
        if(scoredID == null)
            return added;
        addAll(added);
        return this;
    }

    /**
     * given a value return it as 0
     * default behavior is to return the value itself
     *
     * @return
     */
    @Override
    public SpectrumScoringAccumulator asZero() {
        return null;
    }

    /**
     * like toString but might add more information than a shorter string
     * usually implemented bu appending toString
     *
     * @param out
     */
    @Override
    public void buildReport(final Appendable out) {

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

    protected void validateId() {
        if(this.scoredID == null)
            throw new IllegalStateException("no scored Id");
    }

    protected void validateId(SpectrumScoringAccumulator test) {
          if(!this.scoredID.equals(test.scoredID))
              throw new IllegalStateException("bad scored Id");
      }

    protected void addEntry(String peptide,long value) {
        validateId( );  // id better not be null

        long present = 0;
        if (items.containsKey(peptide))
            present += items.get(peptide);
         items.put(peptide,present + value);
        }


    protected void addAll(SpectrumScoringAccumulator added) {
        validateId(added);

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
