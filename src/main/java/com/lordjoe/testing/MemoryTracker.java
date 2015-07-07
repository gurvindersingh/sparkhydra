package com.lordjoe.testing;

import javax.management.*;
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
     * how many bytes are in use right now
     * @return
     */
  public static long threadAllocatedBytes() {
    try {
      return (Long) ManagementFactory.getPlatformMBeanServer()
          .invoke(
              new ObjectName(
                  ManagementFactory.THREAD_MXBEAN_NAME),
              "getThreadAllocatedBytes",
              new Object[]{Thread.currentThread().getId()},
              new String[]{long.class.getName()}
          );
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}

