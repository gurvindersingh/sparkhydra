package com.lordjoe.distributed.spark;


import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.storage.*;
import scala.*;

import java.io.Serializable;
import java.lang.Boolean;
import java.util.*;

public class LargeArrayGeneratedTest {
    public static final Random RND = new Random();
    private static long idIndex = 1;
    private static int fragmentLength = 100;
    public static final String[] BASES = {"A", "C", "G", "T"};

    /**
     * data such as would be seem in a fasta file to test large file processing
     */
    public static class DNAFragment implements Serializable {
        public final String id;  //  something like 55643
        public final String data; // something like AGGCAATTAGA... 100 long

        public DNAFragment(final String pId, final String pData) {
            id = pId;
            data = pData;
        }

        // Fasta files look like this
        public String toString() {
            return ">" + id + "\n" + data;
        }
    }

    // make up a DNA something like AGGCAATTAGA... fragmentLength long
    public static String makeFragment(int fragmentLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fragmentLength; i++) {
            sb.append(BASES[RND.nextInt(BASES.length)]);
        }
        return sb.toString();
    }

    /**
     * generate s new element - called by the iterator  listSize times
     *
     * @return
     */
    public static DNAFragment generateDNAFragment() {
        String id = java.lang.Long.toString(idIndex++);
        String dnaData = makeFragment(fragmentLength);
        return new DNAFragment(id, dnaData);
    }


    // NOTE this will not work in parallel but what will
    public static class CountOnlyList implements List<Boolean> {
        private final BitSet elements;

        public CountOnlyList(final int pListSize) {
            elements = new BitSet((int) pListSize);
        }

        public int size() {
            return elements.size();
        }

        public Iterator<Boolean> iterator() {
            return new Iterator<Boolean>() {
                int current = 0;

                public boolean hasNext() {
                    return current < elements.size();
                }

                public Boolean next() {
                    return elements.get(current++);
                }

                public void remove() {
                    throw new UnsupportedOperationException("Unsupported");
                }
            };
        }

        // All other operations throw exceptions and are not used
        // All other operations throw exceptions and are not used
        public boolean isEmpty() {
            return false;
        }

        public boolean contains(final Object o) {
            return false;
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException("Unsupported");
        }

        public <T1> T1[] toArray(final T1[] a) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public boolean add(final Boolean e) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public boolean remove(final Object o) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public boolean containsAll(final Collection<?> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public boolean addAll(final Collection<? extends Boolean> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public boolean addAll(final int index, final Collection<? extends Boolean> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public boolean removeAll(final Collection<?> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public boolean retainAll(final Collection<?> c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public void clear() {
            throw new UnsupportedOperationException("Unsupported");
        }

        public Boolean get(final int index) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public Boolean set(final int index, final Boolean element) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public void add(final int index, final Boolean element) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public Boolean remove(final int index) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public int indexOf(final Object o) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public int lastIndexOf(final Object o) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public ListIterator<Boolean> listIterator() {
            throw new UnsupportedOperationException("Unsupported");
        }

        public ListIterator<Boolean> listIterator(final int index) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public List<Boolean> subList(final int fromIndex, final int toIndex) {
            throw new UnsupportedOperationException("Unsupported");
        }
    }

    /**
     * argument is collection size in millions
     */
    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        int millionFragmentsCreated = java.lang.Integer.parseInt(args[0]);
        int oneMillion = 1000 * 1000;
        if (millionFragmentsCreated > Integer.MAX_VALUE / oneMillion)
            throw new IllegalArgumentException("too large a space must be less than " + Integer.MAX_VALUE / 1000 * 1000);

        int numberFragmentsCreated = millionFragmentsCreated * 1000 * 1000;
        long byteLength = numberFragmentsCreated * (long) fragmentLength;

        System.err.println("Handling " + byteLength + " with numberLines " + numberFragmentsCreated);
        SparkConf sparkConf = new SparkConf().setAppName("TestLargeFiles");
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())    // use local over nothing
            sparkConf.setMaster("local[*]");
        else
            sparkConf.set("spark.shuffle.manager", "HASH");

        sparkConf.set("spark.mesos.coarse", "true");  // always allow a job to be killed
        sparkConf.set("spark.ui.killEnabled", "true");  // always allow a job to be killed
        //      sparkConf.set("spark.mesos.executor.memoryOverhead","1G");
        sparkConf.set("spark.executor.memory", "15G");
        sparkConf.set("spark.task.cpus", "4");
        sparkConf.set("spark.shuffle.memoryFraction", "0.5");
        sparkConf.set("spark.default.parallelism", "120");


        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        CountOnlyList dnaFragments = new CountOnlyList(numberFragmentsCreated);

        JavaRDD<Boolean> initial = ctx.parallelize(dnaFragments);
        initial = initial.persist(StorageLevel.MEMORY_AND_DISK());

        long numberFragments = initial.count();
        long diffFragments = numberFragments - numberFragmentsCreated;

        boolean forceShuffle = true;
        initial = initial.coalesce(120, forceShuffle);

        JavaRDD<DNAFragment> dnaData = initial.map(new org.apache.spark.api.java.function.Function<Boolean, DNAFragment>() {
                @Override
                 public DNAFragment call(final Boolean v1) throws Exception {
                       return generateDNAFragment();
                  }
         });

        dnaData = dnaData.persist(StorageLevel.DISK_ONLY());
        numberFragments = dnaData.count();
        diffFragments = numberFragments - numberFragmentsCreated;

        System.out.println("Generating lines " + millionFragmentsCreated + "m numberCreated " + numberFragments / oneMillion + "m difference " + diffFragments);
        long end = System.currentTimeMillis();
        System.out.println("Done in " + (end - start) / 1000 + " Sec");

    }


}
