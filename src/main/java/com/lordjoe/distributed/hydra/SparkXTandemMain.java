package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.comet.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.SparkXTandemMain
 * Override to use spark files not local
 * User: Steve
 * Date: 10/21/2014
 */
public class SparkXTandemMain extends XTandemMain {

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
        String pathPrepend = SparkUtilities.getSparkProperties().getProperty("com.lordjoe.distributed.PathPrepend");

        if (pathPrepend != null) {
            System.err.println("Setting default path " + pathPrepend);
            XTandemHadoopUtilities.setDefaultPath(pathPrepend);
            setParameter("com.lordjoe.distributed.PathPrepend", pathPrepend);
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
