package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.comet.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.SparkXTandemMain
 * Override to use spark files not local
 * User: Steve
 * Date: 10/21/2014
 */
public class SparkXTandemMain extends XTandemMain {

     // Define some property names
     public static final String PREPEND_NAME_PROPERTY =  "com.lordjoe.distributed.PathPrepend";
    // if true  do not score  - default false
    public static final String BYPASS_SCORING_PROPERTY =  "com.lordjoe.distributed.hydra.BypassScoring";
    // if true  do keep statistics on bin sizes  - default false
    public static final String KEEP__BIN_STATISTICS_PROPERTY =  "com.lordjoe.distributed.hydra.KeepBinStatistics";
    // if true  do keep statistics on bin sizes  - default false
    public static final String DO_GC_AFTER_BIN =  "com.lordjoe.distributed.hydra.doGCAfterBin";
    // maximum bin size - default Integer.MAX_VALUE
    public static final String MAX_BIN_SIZE_PROPERTY =  "com.lordjoe.distributed.hydra.MaxBinSize";

    public SparkXTandemMain(final InputStream is, final String url) {
        super(is, url);
        String algorithm = getParameter("scoring, algorithm");
        if (algorithm != null) {
            if (algorithm.equalsIgnoreCase(CometScoringAlgorithm.ALGORITHM_NAME)) {
                addAlgorithm(new CometScoringAlgorithm());
            }
            if (algorithm.equalsIgnoreCase(TandemKScoringAlgorithm.ALGORITHM_NAME)) {
                addAlgorithm(TandemKScoringAlgorithm.DEFAULT_ALGORITHM);
            }
        }
        addOpener(new SparkFileOpener(this));
        Properties sparkProperties = SparkUtilities.getSparkProperties();
        String pathPrepend = sparkProperties.getProperty(PREPEND_NAME_PROPERTY);

        for (String key : sparkProperties.stringPropertyNames()) {
            setParameter(key, sparkProperties.getProperty(key));

        }


        if (pathPrepend != null) {
            System.err.println("Setting default path " + pathPrepend);
            XTandemHadoopUtilities.setDefaultPath(pathPrepend);
          }
        try {
            is.close();
        }
        catch (IOException e) {
            // just ignore
        }
    }

    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(final String fileName, final Object... otherData) {
        return super.open(fileName, otherData);
    }


}
