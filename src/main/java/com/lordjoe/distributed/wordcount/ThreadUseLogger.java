package com.lordjoe.distributed.wordcount;

/**
 * com.lordjoe.distributed.wordcount.ThreadUseLogger
 * User: Steve
 * Date: 11/12/2014
 */
public class ThreadUseLogger {

    private static transient ThreadLocal<Integer> thisThreadNumber ;
    private static transient int threadNumber;

    public static String getThreadAccumulatorName(int thread)  {
        return "Thread" + String.format("%05d",thread);
    }

    /**
     * assign a unique number to each thread
     * @return
     */
    public static synchronized int getThreadNumber()
    {
        if(thisThreadNumber == null)
            thisThreadNumber = new ThreadLocal<Integer>();
        Integer myNum = thisThreadNumber.get();
        if(myNum == null)  {
            myNum = threadNumber++;
            thisThreadNumber.set(myNum);
        }
        return myNum;
    }

}
