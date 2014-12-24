package com.lordjoe.distributed;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.IMapReduce
 * User: Steve
 * Date: 8/25/2014
 */
public interface IMapReduce<KEYIN extends Serializable,VIN extends Serializable, KEYOUT extends Serializable, VOUT extends Serializable>{


    /**
     * sources may be very implementation specific
     * @param source some source of data - might be a hadoop directory or a Spark RDD - this will be cast internally
     * @param otherData
     */
     public void mapReduceSource( @Nonnull Object source,Object... otherData);


    /**
     * take the results of another engine and ues it as the input
     *
     * @param source some other engine - usually this will be cast to a specific type
     */
    public void chain( @Nonnull IMapReduce<?,?,KEYIN,VIN>  source);


    /**
     * the last step in mapReduce - returns the output as an iterable
     * @return
     */
    public @Nonnull Iterable<KeyValueObject<KEYOUT, VOUT>> collect();

}
