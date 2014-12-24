package com.lordjoe.distributed.spark;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.spark.ObjectGenerator
 * User: Steve
 * Date: 12/8/2014
 */
public interface ObjectGenerator<T> extends Serializable {
    /**
     * create an instance of the known type
     * @return
     */
    public @Nonnull T generateObject();
}
