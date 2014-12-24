package com.lordjoe.distributed.tandem;

import com.lordjoe.utilities.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;

import java.io.*;

/**
 * com.lordjoe.distributed.tandem.AbstractTandemFunction
 * User: Steve
 * Date: 9/24/2014
 */
public abstract class AbstractTandemFunction implements Serializable {

    private final XTandemMain application;
    private final ElapsedTimer elapsed = new ElapsedTimer();

    public AbstractTandemFunction(final XTandemMain pMain) {
        application = pMain;
    }

    public XTandemMain getApplication() {
        return application;
    }

    public ElapsedTimer getElapsed() {
        return elapsed;
    }

    /**
     * called once in every process before use
     */
    public void setup(JavaSparkContext ctx) {
       // add something if needed
    }

    /**
     * called once in every process after use
     */
    public void cleanup(JavaSparkContext ctx)
    {
       // add something if needed

    }

    public void incrementCounter(String group,String counterName,long increment)   {
       // throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    /**
     * default increment by 1
      * @param group
     * @param counterName
     */
    public void incrementCounter(String group,String counterName )   {
        incrementCounter(group,  counterName,1);
    }

}
