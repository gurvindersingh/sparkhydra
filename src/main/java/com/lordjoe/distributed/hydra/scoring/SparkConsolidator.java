package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.comet_spark.SparkCometScanScorer;
import org.apache.hadoop.fs.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.reporting.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.Serializable;
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
    public <T extends IScoredScan>  int writeScores(JavaRDD<T> scans) {

        scans = sortByIndex(scans);

        List<String> header = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        writer.appendHeader(sb, getApplication());
        header.add(sb.toString());
        sb.setLength(0);
        //header.add(out.toString());
        //JavaRDD<String> headerRDD = SparkUtilities.getCurrentContext().parallelize(header);

        List<String> footer = new ArrayList<String>();
        writer.appendFooter(sb, getApplication());
        footer.add(sb.toString());
        sb.setLength(0);
        long[] scoreCounts = new long[1];

        //JavaRDD<String> footerRDD = SparkUtilities.getCurrentContext().parallelize(footer);

        // make an RDD of the text for every Spectrum
        JavaRDD<String> textOut = scans.map(new AppendScanStringToWriter(writer,getApplication()));

        if(SparkCometScanScorer.isDebuggingCountMade())
           textOut = SparkUtilities.persistAndCount("Total Scored Scans",textOut,scoreCounts);


        //JavaRDD<String> data = headerRDD.union(textOut).union(footerRDD).coalesce(1);

        String outputPath = BiomlReporter.buildDefaultFileName(getApplication());
        Path path = XTandemHadoopUtilities.getRelativePath(outputPath);

        SparkFileSaver.saveAsFile(path, textOut, header.toString(), footer.toString());

        return (int)scoreCounts[0];
    }


    public static  <T extends IScoredScan> JavaRDD<T> sortByIndex(JavaRDD<T> bestScores)
      {
          JavaPairRDD<Integer, T> byIndex = bestScores.mapToPair(new ToIndexTuple());
           JavaPairRDD<Integer, T> sortedByIndex = byIndex.sortByKey();

          return sortedByIndex.values();
     //     return byIndex.values();

      }

}
