package com.lordjoe.distributed;

import java.util.*;

/**
 * com.lordjoe.distributed.IStreamable
 * interface served by a stream or a collection or other producer
 * but workable in Java 6
 * User: Steve
 * Date: 8/28/2014
 */
public interface IStreamable<T> {
    /**
      * Returns an iterator for the elements of this stream.
      *
      * <p>This is a <a href="package-summary.html#StreamOps">terminal
      * operation</a>.
      *
      * @return the element iterator for this stream
      */
     public Iterator<T> iterator();

}
