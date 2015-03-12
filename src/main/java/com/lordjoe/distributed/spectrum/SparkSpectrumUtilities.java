package com.lordjoe.distributed.spectrum;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.input.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import scala.*;

import javax.annotation.*;
import java.io.*;
import java.lang.Boolean;

/**
 * com.lordjoe.distributed.spectrum.SparkSpectrumUtilities
 * User: Steve
 * Date: 9/24/2014
 */
public class SparkSpectrumUtilities {

    private static boolean gMSLevel1Dropped = true;

    public static boolean isMSLevel1Dropped() {
        return gMSLevel1Dropped;
    }

    public static void setMSLevel1Dropped(final boolean pMSLevel1Dropped) {
        gMSLevel1Dropped = pMSLevel1Dropped;
    }

    /**
     * parse a Faste File returning the comment > line as the key
     * and the rest as the value
     *
     * @param path
     * @param ctx
     * @return
     */
    @Nonnull
    public static JavaPairRDD<String, String> parseFastaFile(@Nonnull String path, @Nonnull JavaSparkContext ctx) {
        Class inputFormatClass = FastaInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        return ctx.newAPIHadoopFile(
                path,
                inputFormatClass,
                keyClass,
                valueClass,
                ctx.hadoopConfiguration()
        );

    }


    public static class MZXMLInputFormat extends XMLTagInputFormat {
        public MZXMLInputFormat() {
            super("scan");
        }
    }

    @Nonnull
    public static JavaPairRDD<String, IMeasuredSpectrum> parseSpectrumFile(@Nonnull String path,XTandemMain application) {
        JavaSparkContext ctx = SparkUtilities.getCurrentContext();

        //     if(path.toLowerCase().endsWith(".mgf"))
        //           return parseAsTextMGF(path,ctx);     this will fail
        if (path.toLowerCase().endsWith(".mgf"))
            return parseAsMGF(path, ctx,application);
        if (path.toLowerCase().endsWith(".mzxml"))
            return parseAsMZXML(path, ctx,application);
        throw new UnsupportedOperationException("Cannot understand extension " + path);
    }

    @Nonnull
    public static JavaPairRDD<String, IMeasuredSpectrum> parseAsMZXML(@Nonnull final String path, @Nonnull final JavaSparkContext ctx,XTandemMain application) {
        Class inputFormatClass = MZXMLInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        JavaPairRDD<String, String> spectraAsStrings = ctx.newAPIHadoopFile(
                path,
                inputFormatClass,
                keyClass,
                valueClass,
                SparkUtilities.getHadoopConfiguration()
        );

        long[] countRef = new long[1];
        spectraAsStrings = SparkUtilities.persistAndCountPair("Raw spectra", spectraAsStrings, countRef);
        // debug code
        //spectraAsStrings = SparkUtilities.realizeAndReturn(spectraAsStrings);

        if (isMSLevel1Dropped()) {
            // filter out MS Level 1 spectra
            spectraAsStrings = spectraAsStrings.filter(new Function<Tuple2<String, String>, Boolean>() {
                                                           public Boolean call(Tuple2<String, String> s) {
                                                               String s1 = s._2();
                                                               if (s1.contains("msLevel=\"2\""))
                                                                   return true;
                                                               return false;
                                                           }
                                                       }
            );
            spectraAsStrings = SparkUtilities.persistAndCountPair("Filtered spectra", spectraAsStrings, countRef);
        }

        // debug code
        //spectraAsStrings = SparkUtilities.realizeAndReturn(spectraAsStrings);

        // parse scan tags as  IMeasuredSpectrum key is id
        JavaPairRDD<String, IMeasuredSpectrum> parsed = spectraAsStrings.mapToPair(new MapSpectraStringToRawScan());
        // TODO normalize

        // kill duplicates todo why are there duplicated
        parsed = SparkUtilities.chooseOneValue(parsed);

        return parsed;
    }

//    @Nonnull
//    public static JavaPairRDD<String, IMeasuredSpectrum> parseAsOldMGF(@Nonnull final String path, @Nonnull final JavaSparkContext ctx) {
//        Class inputFormatClass = MGFOldInputFormat.class;
//        Class keyClass = String.class;
//        Class valueClass = String.class;
//
//        JavaPairRDD<String, String> spectraAsStrings = ctx.hadoopFile(
//                path,
//                inputFormatClass,
//                keyClass,
//                valueClass
//        );
//        JavaPairRDD<String, IMeasuredSpectrum> spectra = spectraAsStrings.mapToPair(new MGFStringTupleToSpectrumTuple());
//        return spectra;
//    }

    /**
     * NOTE this will not work since Text is not serializable
     *
     * @param path
     * @param ctx
     * @return
     */
    @Nonnull
    public static JavaPairRDD<String, IMeasuredSpectrum> parseAsTextMGF(@Nonnull final String path, @Nonnull final JavaSparkContext ctx,XTandemMain application) {
        Class inputFormatClass = MGFTextInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        JavaPairRDD<String, String> spectraAsStrings = ctx.newAPIHadoopFile(
                path,
                inputFormatClass,
                keyClass,
                valueClass,
                ctx.hadoopConfiguration()
        );

        long[] counts = new long[1];
        spectraAsStrings = SparkUtilities.persistAndCountPair("Spectra as read",spectraAsStrings,counts);

        JavaPairRDD<String, IMeasuredSpectrum> spectra = spectraAsStrings.flatMapToPair(new MGFStringTupleToSpectrumTuple(application));

        long[] spectraCounts = new long[1];
        spectra = SparkUtilities.persistAndCountPair("Spectra as read",spectra,spectraCounts);

        return spectra;
    }


    public static String cleanMGFRepresentation(String inpStr) {
        LineNumberReader inp = new LineNumberReader(new StringReader(inpStr));
        IMeasuredSpectrum spectrum = XTandemUtilities.readMGFScan(inp, "");
        IMeasuredSpectrum ret = IntensitySetTransformer.findSpectrum(spectrum);
        StringBuilder sb = new StringBuilder();
        ((RawPeptideScan)spectrum).appendAsMGF(sb);
        return sb.toString();
    }

    @Nonnull
    public static JavaPairRDD<String, IMeasuredSpectrum> parseAsMGF(@Nonnull final String path, @Nonnull final JavaSparkContext ctx,XTandemMain application) {
        Class inputFormatClass = MGFInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        JavaPairRDD<String, String> spectraAsStrings = ctx.newAPIHadoopFile(
                path,
                inputFormatClass,
                keyClass,
                valueClass,
                ctx.hadoopConfiguration()
        );



        spectraAsStrings = SparkUtilities.persist(spectraAsStrings);
        long spectraStringCount = spectraAsStrings.count();

        JavaPairRDD<String, IMeasuredSpectrum> spectra = spectraAsStrings.flatMapToPair(new MGFStringTupleToSpectrumTuple(application));
        spectra = SparkUtilities.persist(spectra);
        long spectraCount = spectra.count();

        String countString = "Read " + spectraStringCount + " spectra parsed " + spectraCount;
     //   if (true)
     //       throw new IllegalStateException(countString); // look and stop
        return spectra;
    }

    /**
     * sample using the olf Hadoop api
     *
     * @param path path to the file
     * @param ctx  context
     * @return contents as scans
     */
    @Nonnull
    public static JavaPairRDD<String, IMeasuredSpectrum> parseSpectrumFileOld(@Nonnull String path, @Nonnull JavaSparkContext ctx,XTandemMain application) {

        Class inputFormatClass = MGFOldInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        JavaPairRDD<String, String> spectraAsStrings = ctx.hadoopFile(
                path,
                inputFormatClass,
                keyClass,
                valueClass
        );
        JavaPairRDD<String, IMeasuredSpectrum> spectra = spectraAsStrings.flatMapToPair(new MGFStringTupleToSpectrumTuple(application));
        return spectra;
    }

    private static class MapSpectraStringToRawScan extends AbstractLoggingPairFunction<Tuple2<String, String>, String, IMeasuredSpectrum> {
        @Override
        public Tuple2<String, IMeasuredSpectrum> doCall(final Tuple2<String, String> in) throws Exception {
            //String key = in._1();
            String scanText = in._2();
            RawPeptideScan scan = XTandemHadoopUtilities.readScan(scanText, null);
            return new Tuple2(scan.getId(), scan);
        }
    }

}
