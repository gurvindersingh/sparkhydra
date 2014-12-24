package com.lordjoe.distributed.database;

import java.io.*;

/**
 * com.lordjoe.distributed.database.ObjectFoundListener
 * primarily for debugging do used to run through lists of objecta and act
 * usually when an interesting case is found
 * @see SparkUtilities.realizeAndReturn
 * User: Steve
 * Date: 11/4/2014
 */
public interface ObjectFoundListener<T> extends Serializable {

    /**
     * do something on finding the obejcts
     * for debugging thiis may just find interesting cases
     * @param found
     */
    public void onObjectFound(T found);

}
