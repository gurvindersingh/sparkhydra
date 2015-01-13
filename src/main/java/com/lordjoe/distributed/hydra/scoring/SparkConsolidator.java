package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import com.lordjoe.utilities.FileUtilities;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
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

    /*class writeData implements FlatMapFunction<Iterator<IScoredScan>, String> {
        public Iterator<IScoredScan> call (Iterator<IScoredScan>) {
            while (t.hasNext()) {
                IScoredScan scan = t.next();
                writer.appendScan(out, getApplication(), scan);
            }
            return null;
        }
    }*/

    /**
     * write scores into a file
     *
     * @param out
     * @param scans
     */
    public void writeScores(final Appendable out, JavaRDD<IScoredScan> scans) {
        writer.appendHeader(out, getApplication());

        // Print scans in sorted order
        scans.sortBy(new Function<IScoredScan, String>() {
            @Override
            public String call(final IScoredScan v1) throws Exception {
                return v1.getId();
            }
        },true, SparkUtilities.getDefaultNumberPartitions());

        Iterator<IScoredScan> scanIterator = scans.toLocalIterator();
        while(scanIterator.hasNext())  {
            IScoredScan scan = scanIterator.next();
            writer.appendScan(out, getApplication(), scan);
        }
        //scans.mapPartitions(new writeData());
        writer.appendFooter(out, getApplication());
    }



}
