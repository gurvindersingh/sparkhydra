package com.lordjoe.testing;

import com.sun.management.*;

import java.lang.management.*;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;

/**
 * com.lordjoe.testing.MemoryTracker
 * A class to track garbae collection times
 * User: Steve
 * Date: 7/2/2015
 */

public class GarbageCollectionTracker {


    private long startTime;
    private long gcTime;

    /**
     * how many bytes are in use right now
     *
     * @return
     */
    public static long getGCTime() {
        List<GarbageCollectorMXBean> gcmxb = ManagementFactory.getGarbageCollectorMXBeans();
        long total = 0;
        for (GarbageCollectorMXBean ob : gcmxb) {
            if(ob instanceof com.sun.management.GarbageCollectorMXBean) {
                 GcInfo collect = ((com.sun.management.GarbageCollectorMXBean)ob).getLastGcInfo();
                if(collect == null)
                    break;
                long duration = collect.getDuration();
                Map<String, MemoryUsage> memoryUsageAfterGc = collect.getMemoryUsageAfterGc();
                Map<String, MemoryUsage> memoryUsageBeforeGc = collect.getMemoryUsageBeforeGc();

            }
             String name = ob.getName();
            long time = ob.getCollectionTime();
            long count = ob.getCollectionCount();
            total += time;
        }
        return total;
    }
}


