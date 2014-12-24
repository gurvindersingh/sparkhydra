package com.lordjoe.distributed;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.IReducerFunction
 * User: Steve
 * Date: 8/25/2014
 */
public interface ISingleOutputReducerFunction<K extends Serializable,V   extends Serializable,KOUT extends Serializable,VOUT   extends Serializable > extends Serializable {
    /**
     * implement this A reducer is quaranteed to return one Key Value pair for every Key
     * @param key
     * @param value
     * @return
     */
     public @Nonnull  KeyValueObject<KOUT,VOUT> handleValue(@Nonnull K key, @Nonnull V value);

}
