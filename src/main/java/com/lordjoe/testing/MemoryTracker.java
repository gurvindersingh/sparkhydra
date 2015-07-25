package com.lordjoe.testing;

import java.lang.management.*;

/**
 * com.lordjoe.testing.MemoryTracker
 * A class to track memory allocation
 * User: Steve
 * Date: 7/2/2015
 *
 */

public class MemoryTracker {


    /**
     * how many bytes allocated in the current thread
     * @return
     */
    public static long usedBytes() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }



    /**
     * how many bytes allocated in the current thread
     * @return
     */
    public static long threadAllocatedBytes() {
        com.sun.management.ThreadMXBean tBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        return tBean.getThreadAllocatedBytes(Thread.currentThread().getId());
    }

 //    /**
//     * how many bytes are in use right now
//     * @return
//     */
//  public static long threadAllocatedBytes() {
//
//      ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
//      long allocated = ((com.sun.management.ThreadMXBean)threadMXBean).getThreadAllocatedBytes();
//
//    try {
//      return (Long) ManagementFactory.getPlatformMBeanServer()
//          .invoke(
//              new ObjectName(
//                  ManagementFactory.THREAD_MXBEAN_NAME),
//              "getThreadAllocatedBytes",
//              new Object[]{Thread.currentThread().getId()},
//              new String[]{long.class.getName()}
//          );
//    } catch (Exception e) {
//      throw new IllegalStateException(e);
//    }
//  }
}

