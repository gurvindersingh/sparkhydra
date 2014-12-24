package com.lordjoe.distributed;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.ISortAndShuffleFunction
 * User: Steve
 * Date: 8/28/2014
 */
public interface ISortAndShuffleFunction<K   extends Serializable,V   extends Serializable  > {
    /**
        * this is what a shuffle does
         * @return iterator over mapped key values
        */
       public @Nonnull Iterable<KeyValueObject<K,V>> returnValues( @Nonnull Iterable<V> values);


}
