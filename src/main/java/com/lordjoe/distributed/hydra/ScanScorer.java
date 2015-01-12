package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.spectrum.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.pepxml.*;
import org.systemsbiology.xtandem.reporting.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import java.io.*;
import java.util.*;


/**
 * com.lordjoe.distributed.hydra.ScanScorer
 * User: Steve
 * Date: 10/7/2014
 */
public class ScanScorer {

    public static class writeScoresMapper extends AbstractLoggingFunction<Tuple2<String, IScoredScan>, Tuple2<String, String>> {
        final BiomlReporter reporter;

        private writeScoresMapper(final BiomlReporter pReporter) {
            reporter = pReporter;
        }


        @Override
        public Tuple2<String, String> doCall(final Tuple2<String, IScoredScan> v1) throws Exception {
            IScoredScan scan = v1._2();
            StringWriter sw = new StringWriter();
            Appendable out = new PrintWriter(sw);
            reporter.writeScanScores(scan, out, 1);
            return new Tuple2(v1._1(), sw.toString());
        }
    }

    public static class ScanKeyMapper extends AbstractLoggingPairFlatMapFunction< Iterator<KeyValueObject<String, IScoredScan>>, String, IScoredScan> {


        @Override
        public Iterable<Tuple2<String, IScoredScan>> doCall(final  Iterator<KeyValueObject<String, IScoredScan>> t) throws Exception {
            List<Tuple2<String, IScoredScan>> mapped = new ArrayList<Tuple2<String, IScoredScan>>();
            while (t.hasNext()) {
                KeyValueObject<String, IScoredScan> kscan = t.next();
                IScoredScan value = kscan.value;
                String id = value.getId(); //  now we match scans
                mapped.add(new Tuple2(id, value));
            }
            return mapped;
        }
    }

    public static class DropNoMatchScansFilter extends AbstractLoggingFunction<KeyValueObject<String, IScoredScan>, java.lang.Boolean> {

        @Override
        public java.lang.Boolean doCall(final KeyValueObject<String, IScoredScan> v1) throws Exception {
            IScoredScan vx = v1.value;
            return v1.value.isMatchPresent();
        }
    }

    public static class chooseBestScanScore extends AbstractLoggingFunction2<IScoredScan, IScoredScan, IScoredScan> {

        @Override
        public IScoredScan doCall(final IScoredScan v1, final IScoredScan v2) throws Exception {
            ISpectralMatch match1 = v1.getBestMatch();
            ISpectralMatch match2 = v2.getBestMatch();
            if (match1.getHyperScore() > match2.getHyperScore())
                return v1;
            else
                return v2;
        }
    }

    /**
     * create an output writer
     *
     * @param pApplication
     * @return
     * @throws IOException
     */
    public static PrintWriter buildWriter(final XTandemMain pApplication) throws IOException {
        String outputPath = BiomlReporter.buildDefaultFileName(pApplication);
        outputPath = outputPath.replace(".xml", ".pep.xml");
        return SparkHydraUtilities.nameToPrintWriter(outputPath, pApplication);
    }

    public static final int SPARK_CONFIG_INDEX = 0;
    public static final int TANDEM_CONFIG_INDEX = 1;
    public static final int SPECTRA_INDEX = 2;

    /**
     * call with args like or20080320_s_silac-lh_1-1_11short.mzxml in Sample2
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        // code to run class loader
        //String runner = SparkUtilities.buildLoggingClassLoaderPropertiesFile(ScanScorer.class  , args);
        //System.out.println(runner);


        if (args.length < TANDEM_CONFIG_INDEX + 1) {
            System.out.println("usage sparkconfig configFile fastaFile");
            return;
        }

        SparkUtilities.setLogToWarn();

        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);

        String pathPrepend = SparkUtilities.getSparkProperties().getProperty("com.lordjoe.distributed.PathPrepend");
        if (pathPrepend != null)
            XTandemHadoopUtilities.setDefaultPath(pathPrepend);


        String configStr = SparkUtilities.buildPath(args[TANDEM_CONFIG_INDEX]);

        String spectra = SparkUtilities.buildPath(args[SPECTRA_INDEX]);

        SparkMapReduceScoringHandler handler = new SparkMapReduceScoringHandler(configStr, false);

        // handler.buildLibraryIfNeeded();
        JavaRDD<IPolypeptide> databasePeptides = handler.buildLibrary(0);

        // actually needed in a later stage
        //Map<Integer, Integer> databaseSizes = handler.getDatabaseSizes();

        // read spectra
        JavaPairRDD<String, IMeasuredSpectrum> scans = SparkSpectrumUtilities.parseSpectrumFile(spectra);
        // we need Tuple2 not KeyValueObject
        JavaRDD<KeyValueObject<String, IMeasuredSpectrum>> scansKV = SparkUtilities.fromTuples(scans);


        handler.performSingleReturnMapReduce(scansKV);

        JavaRDD<KeyValueObject<String, IScoredScan>> scores = handler.getOutput();

          /*
          Drop unmatched scans
         */
        scores = scores.filter(new DropNoMatchScansFilter());

        /**
         * make into tuples
         */
        JavaPairRDD<String, IScoredScan> mappedByScanKey = scores.mapPartitionsToPair(new ScanKeyMapper());

        /**
         *  find the best score
         */
        JavaPairRDD<String, IScoredScan> bestScores = mappedByScanKey.reduceByKey(new chooseBestScanScore());
        /**
         * collect and write
         */
        bestScores = bestScores.sortByKey();


        XTandemMain application = handler.getApplication();
        PepXMLWriter pwrtr = new PepXMLWriter(application);
        PepXMLScoredScanWriter pWrapper = new PepXMLScoredScanWriter(pwrtr);
        SparkConsolidator consolidator = new SparkConsolidator(pWrapper, application);


        JavaRDD<IScoredScan> values = bestScores.values();

        PrintWriter out = buildWriter(application);
        consolidator.writeScores(out, values);
        out.close();

    }


}
