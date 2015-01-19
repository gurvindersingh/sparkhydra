package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import org.apache.hadoop.fs.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.pepxml.*;
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
     * @param scans
     */
    public void writeScores(PepXMLWriter pwriter, JavaRDD<IScoredScan> scans) {
        // Print scans in sorted order
        scans.sortBy(new Function<IScoredScan, Object>() {
            @Override
            public String call(final IScoredScan v1) throws Exception {
                return v1.getId();
            }
        },true, SparkUtilities.getDefaultNumberPartitions());

        List<String> header = new ArrayList<String>();
        header.add(pwriter.getPepXMLHeader());
        //header.add(out.toString());
        JavaRDD<String> headerRDD = SparkUtilities.getCurrentContext().parallelize(header);
        List<String> footer = new ArrayList<String>();
        footer.add(pwriter.getPepXMLFooter());
        JavaRDD<String> footerRDD = SparkUtilities.getCurrentContext().parallelize(footer);
        // make an RDD of the text for every SPrectrum
        JavaRDD<String> textOut  = scans.map(new AbstractLoggingFunction<IScoredScan, String>() {
            @Override
            public String doCall(final IScoredScan v1) throws Exception {
                StringBuilder sb = new StringBuilder();
                writer.appendScan(sb, getApplication(), v1);
                return sb.toString();
            }
        });

        JavaRDD<String> data = headerRDD.union(textOut).union(footerRDD).coalesce(1);

        Path result = XTandemHadoopUtilities.getRelativePath("result.pep.xml");

        SparkFileSaver.saveAsFile(result,data);
    }
}
