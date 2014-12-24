package com.lordjoe.algorithms;

import java.io.*;
import java.util.*;

/**
 * map items to counts of their occurrences
 *
 * @param <T>
 */
public class CountedMap<T> implements Serializable {
    private Map<T, Integer> items = new HashMap<T, Integer>();
    private int total;

    public CountedMap() {
    }

    public void add(T item) {
        int value = 1;
        if (items.containsKey(item)) {
            value = items.get(item) + 1;
        }
          items.put(item, value);
        total += 1; // recompute
    }

    public void add(T item, int value) {
        items.put(item, value);
        total += value;
    }


    public int get(T item) {
        if (items.containsKey(item)) {
            return items.get(item);
        }
        return 0;
    }

    public int getTotal() {
        if (total == 0)
            total = computeTotal();
        return total;
    }

    public int computeTotal() {
        int sum = 0;
        for (Integer v : items.values()) {
            sum += v;
        }
        return sum;
    }


    public int size() {
        return items.size();
    }

    public int getCountsMoreThanN(int n) {
        int sum = 0;
        for (Integer v : items.values()) {
            if (v > n)
                sum++;
        }
        return sum;
    }

    /**
     * return all keys with a count more than n
     * @param n
     * @return
     */
    public List<T> getItemsMoreThanN(int n) {
        List<T> ret = (List<T>)new ArrayList();
        for (T t : items.keySet()) {
             if(items.get(t) > n)
                 ret.add(t);
        }
        return ret;
    }

    public int getDuplicates() {
        return getCountsMoreThanN(1);
    }

    public CountedMap<T> getDifferences(CountedMap<T> others) {
        CountedMap<T> ret = new CountedMap<T>();
        for (T t : items.keySet()) {
            int me = get(t);
            int other = others.get(t);
            if (me != other)
                ret.add(t, me - other);
        }
        // handle cases we have but not they
        for (T t : others.items.keySet()) {
            if (ret.get(t) != 0)
                continue;
            int other = others.get(t);
            ret.add(t, -other);
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("size:");
        sb.append(size());
        sb.append(" total:");
        sb.append(getTotal());
        sb.append(" duplicates:");
        sb.append(getDuplicates());
        return sb.toString();
    }

    public List<CountedString> asCountedStrings() {
        List<CountedString> holder = new ArrayList<CountedString>();
        for (T t : items.keySet()) {
            int count = get(t);
            holder.add(new CountedString(t.toString(), count));
        }
        Collections.sort(holder);
        return holder;
    }

    /**
     * should ba an array 0 .. 1
     *
     * @return
     */
    public double[] getCountDistribution() {
        List<CountedString> holder = asCountedStrings();
        if (holder.isEmpty())
            return new double[0];
        int max = holder.get(0).getCount(); // highest
        max = Math.max(5,max); // padd
        double[] ret = new double[max  ];
        int current = max;
        int total = getTotal();
        double fullsize = total;
         int current_total = 0;
         for (CountedString cs : holder) {
            int count = cs.getCount();
            if (count < current) {
                while (count < current) {
                    ret[(current--) - 1] =  total / fullsize;
                }
                total -= current_total;
                current_total = 0;
            }
            current_total += count;
        }
        return ret;

    }
}
