package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.*;
import org.apache.spark.api.java.*;
import org.apache.spark.broadcast.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.SparkBroadcastObjects
 * this class allows a large program to use broadcast to send data to slaves
 * User: Steve
 * Date: 11/12/2014
 */
public class SparkBroadcastObjects implements Serializable {


    private static SparkBroadcastObjects instance;

    public static SparkBroadcastObjects getInstance() {
          return instance;
    }

    // Call in the executor if this object is used -
    public static void createInstance() {
        instance = new SparkBroadcastObjects();
     }

    // this is a singleton and should be serialized
    private SparkBroadcastObjects() {
    }

    /**
     * holds nameToBroadcast by name
     */
    private final Map<String, Broadcast<Serializable>> nameToBroadcast = new HashMap<String,Broadcast<Serializable>>();

    /**
     * must be called in the Executor before nameToBroadcast can be used
     *
     * @param acc
     */
    public static void broadcast(String name,Serializable acc) {
        SparkBroadcastObjects me = getInstance();
        if (me.nameToBroadcast.get(acc) != null)
            return; // already done - should an exception be thrown
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
        Broadcast<Serializable> broadcast = currentContext.broadcast(acc);

        me.nameToBroadcast.put(name, broadcast);
    }

    /**
      * must be called in the Executor before nameToBroadcast can be used
      *
      * @param acc
      */
     public static Serializable getBroadcastValue(String name ) {
         SparkBroadcastObjects me = getInstance();
         Broadcast<Serializable> bc = me.nameToBroadcast.get(name) ;
         if(bc == null)
             return null;
         return bc.getValue();
     }


    /**
     * return all registerd broadcasts
     *
     * @return
     */
    public List<String> getBroadcastNames() {
        List<String> keys = new ArrayList<String>(nameToBroadcast.keySet());
        Collections.sort(keys);  // alphapetize
        return keys;
    }




}
