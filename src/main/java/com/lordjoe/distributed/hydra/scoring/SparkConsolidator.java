package com.lordjoe.distributed.hydra.scoring;

import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.SparkConsolidator     \
 * Responsible for writing an output file
 * User: Steve
 * Date: 10/20/2014
 */
public class SparkConsolidator implements Serializable {

    private final ScoredScanWriter writer;
    private final XTandemMain application;

    public SparkConsolidator(final ScoredScanWriter pWriter, final XTandemMain pApplication) {
        writer = pWriter;
        application = pApplication;
    }

    public ScoredScanWriter getWriter() {
        return writer;
    }

    public XTandemMain getApplication() {
        return application;
    }



    /**
     * write scores into a file
     *
     * @param out
     * @param scans
     */
    public void writeScores(final Appendable out, JavaRDD<IScoredScan> scans) {
        writer.appendHeader(out, getApplication());

        Iterator<IScoredScan> scanIterator = scans.toLocalIterator();
        while(scanIterator.hasNext())  {
            IScoredScan scan = scanIterator.next();
            writer.appendScan(out, getApplication(), scan);
        }
        writer.appendFooter(out, getApplication());
    }



}
