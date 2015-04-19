package com.lordjoe.distributed;

import com.lordjoe.distributed.spark.*;

import java.io.*;

/**
 * org.apache.spark.api.java.function.AbstraceLoggingFunction
 * superclass for defined functions that will log on first call making it easier to see
 * also will keep an accumulator to track calls and where ther are made
 * do work in doCall
 * User: Steve
 * Date: 10/23/2014
 */
public abstract class AbstractLoggingFunctionBase implements Serializable {


    private static int callReportInterval = 50000;

    public static int getCallReportInterval() {
        return callReportInterval;
    }

    public static void setCallReportInterval(final int pCallReportInterval) {
        callReportInterval = pCallReportInterval;
    }

    private transient boolean logged;   // transient so every machine keeps its own
    private transient long numberCalls;   // transient so every machine keeps its own
    private SparkAccumulators accumulators; // member so it will be serialized from the executor
    private transient long startTime;     // transient so every machine keeps its own
    private transient long totalTime;
    private transient long accumulatedTime;


    protected AbstractLoggingFunctionBase() {
        if (!isFunctionCallsLogged())
            return;
        SparkAccumulators instance = SparkAccumulators.getInstance();
        if (instance != null)
            accumulators = instance; // might be null but serialization should set
        // build an accumulator for this function
        if (accumulators != null) {
            String className = getClass().getSimpleName();
            SparkAccumulators.createFunctionAccumulator(className);
          }
    }

    public long getTotalTime() {
        return totalTime;
    }


    public long getRunningTimeMillisec() {
        return System.currentTimeMillis() - getStartTIme();
    }

    /**
     * really the time this function was first called as a local copy
     *
     * @return
     */
    public long getStartTIme() {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        return startTime;
    }

    public long getAccumulatedTime() {
        return accumulatedTime;
    }

    public void incrementAccumulatedTime(long added) {
        accumulatedTime += added;
        totalTime += added;
    }


    public long getAndClearAccumulatedTime() {
        long ret = accumulatedTime;
        accumulatedTime = 0;
        return ret;
    }

    /**
     * Override this to prevent logging
     *
     * @return
     */
    public boolean isFunctionCallsLogged() {
        return SparkAccumulators.isFunctionsLoggedByDefault();
    }

    public final boolean isLogged() {
        return logged;
    }

    public final void setLogged(final boolean pLogged) {
        logged = pLogged;
    }

    public final long getNumberCalls() {
        return numberCalls;
    }

    public final void incrementNumberCalled() {
        numberCalls++;
    }

    public SparkAccumulators getAccumulators() {
        return accumulators;
    }

    public static final double MILLISEC_IN_NANOSEC = 1000 * 1000;
    public static final double SEC_IN_NANOSEC = MILLISEC_IN_NANOSEC * 1000;
    public static final double MIN_IN_NANOSEC = SEC_IN_NANOSEC * 60;
    public static final double HOUR_IN_NANOSEC = MIN_IN_NANOSEC * 60;
    public static final double DAY_IN_NANOSEC = HOUR_IN_NANOSEC * 24;

    public static String formatNanosec(long timeNanosec) {
        if (Math.abs(timeNanosec) < 10 * SEC_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / MILLISEC_IN_NANOSEC) + " msec";
        if (Math.abs(timeNanosec) < 10 * MIN_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / SEC_IN_NANOSEC) + " sec";
        if (Math.abs(timeNanosec) < 10 * HOUR_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / MIN_IN_NANOSEC) + " min";
        if (Math.abs(timeNanosec) < 10 * DAY_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / HOUR_IN_NANOSEC) + " hour";
        return String.format("%10.2f", timeNanosec / DAY_IN_NANOSEC) + " days";
    }

    public static String formatMillisec(long timeMillisec) {
        return formatNanosec(timeMillisec * (long)MILLISEC_IN_NANOSEC);
    }

    public void reportCalls() {
        if (!isFunctionCallsLogged())
            return;
        String className = getClass().getSimpleName();
        if (!isLogged()) {
            SparkUtilities.setLogToWarn();
            System.err.println("Starting Function " + className);
            //SparkUtilities.setLogToWarn();
            setLogged(true);  // done once
        }
        // report every 100,000 calls
        if (getCallReportInterval() > 0) {
            long numberCalls1 = getNumberCalls();
            if (numberCalls1 > 0 && numberCalls1 % getCallReportInterval() == 0) {
                System.err.println("Calling Function " + className + " " + numberCalls1 / 1000 + "k times");
                System.err.println(" Function took " + className + " " + formatNanosec(totalTime) + " running for " + formatMillisec(getRunningTimeMillisec()));
             }
        }
        incrementNumberCalled();

        SparkAccumulators accumulators1 = getAccumulators();
        if (accumulators1 == null)
            return;
        long time = getAndClearAccumulatedTime();
        accumulators1.incrementFunctionAccumulator(className, time);
//        if ( accumulators1.isAccumulatorRegistered(className)) {
//            accumulators1.incrementAccumulator(className);
//        }
//        if(SparkUtilities.isLocal()) {
//            accumulators1.incrementThreadAccumulator(); // track which thread we are using
//        }
//        else {
//            accumulators1.incrementThreadAccumulator(); // track which thread we are using
//            accumulators1.incrementMachineAccumulator();
//        }
    }

//
//    /**
//     * Todo Why might this help SLewis - added only to debug serialization
//     * Always treat de-serialization as a full-blown constructor, by
//     * validating the final state of the de-serialized object.
//     */
//    private void readObject(
//            ObjectInputStream aInputStream
//    ) throws ClassNotFoundException, IOException {
//        //always perform the default de-serialization first
//        aInputStream.defaultReadObject();
//    }
//
//    /**
//     * Todo Why might this help SLewis - added only to debug serialization
//     * This is the default implementation of writeObject.
//     * Customise if necessary.
//     */
//    private void writeObject(
//            ObjectOutputStream aOutputStream
//    ) throws IOException {
//        //perform the default serialization for all non-transient, non-static fields
//        aOutputStream.defaultWriteObject();
//    }

}
