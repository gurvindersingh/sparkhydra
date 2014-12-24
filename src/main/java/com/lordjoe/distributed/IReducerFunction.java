package com.lordjoe.distributed;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.IReducerFunction
 * User: Steve
 * Date: 8/25/2014
 */
public interface IReducerFunction<K extends Serializable,V   extends Serializable,KOUT extends Serializable,VOUT   extends Serializable > extends Serializable {
    /**
      * this is what a reducer does
      * @param value  input value
      * @return iterator over mapped key values
      */
     public @Nonnull  void handleValues( @Nonnull K key,@Nonnull Iterable<V> values,IKeyValueConsumer<KOUT,VOUT>... consumer);

}
