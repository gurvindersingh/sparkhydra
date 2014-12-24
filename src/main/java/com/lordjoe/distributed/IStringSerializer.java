package com.lordjoe.distributed;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.IStringSerializer
 * User: Steve
 * NOTE is this something only Hadoop and TEXT care about
 * Date: 8/25/2014
 */
public interface IStringSerializer<T> {

    /**
     * convert a String to a T
     * @param src source string
     * @return possibly null T
     */
      public T fromString(@Nonnull String src);


      public String serialize(@Nonnull T src);

    /**
     * if this was Java this would be a default out.append(serialize(src); out.append("\n");
     * @param src  source ouject
     * @param out  sink - all exceptions convert to runtimes
     */
      public void append(@Nonnull T src,@Nonnull Appendable out);

    /**
     *
     * @param line array of length 1 - equilent to pass by reference
     * @param rdr reader
     * @return  possibly null T - also set the value of line to the current position
     */
      public T fromReader(@Nonnull LineNumberReader rdr);
}
