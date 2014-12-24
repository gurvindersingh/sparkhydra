package com.lordjoe.distributed.hydra;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.SparkFileOpener
 * User: Steve
 * Date: 10/21/2014
 */
public class SparkFileOpener implements IStreamOpener {

    private final XTandemMain app;

    public SparkFileOpener(final XTandemMain pApp) {
        app = pApp;
    }

    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(String fileName, Object... otherData)
    {
         InputStream inputStream = SparkHydraUtilities.nameToInputStream(fileName, app);
        if(inputStream != null)
            return inputStream;
        SparkHydraUtilities.throwBadPathException(fileName, app);  // explain what went wrong
        return null; // never get here
    }

}
