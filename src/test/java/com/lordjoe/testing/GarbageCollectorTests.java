package com.lordjoe.testing;

import com.lordjoe.utilities.*;

import java.util.*;

/**
 * com.lordjoe.testing.GarbageCollectorTests
 * User: Steve
 * Date: 7/15/2015
 */
public class GarbageCollectorTests {

    private static void wasteMemory() {
        List<int[]>  holder = new ArrayList<int[]>() ;
        for (int i = 0; i < 1000000; i++) {
             holder.add(new int[1000]) ;
        }
    }


    public static void main(String[] a) {
    //    GCTimeAccumulator acc =  GCTimeAccumulator.empty();
        long[] times = new long[10000] ;
        ElapsedTimer timer = new ElapsedTimer();

        for (int i = 0; i < 10000; i++) {
            wasteMemory();
            long gcTimme = GarbageCollectionTracker.getGCTime();
            times[i] = gcTimme;

            long elapsed = timer.getElapsedMillisec() ;
            if(i > 0 && i %1000 ==0 ) {
                times[i] = gcTimme; // break here
                double frac = gcTimme / (double)elapsed;
                frac = 0; // break here
            }
        }


    }
}
