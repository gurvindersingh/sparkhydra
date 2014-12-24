package com.lordjoe.distributed;

import java.io.*;

/**
 * com.lordjoe.distributed.IKeyValueConsumer
 * User: Steve
 * Date: 9/2/2014
 */
public interface IKeyValueConsumer<K extends Serializable,V extends Serializable> {

    public void consume(KeyValueObject<K,V> kv);

}
