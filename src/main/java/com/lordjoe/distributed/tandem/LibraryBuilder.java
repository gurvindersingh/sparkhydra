package com.lordjoe.distributed.tandem;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.database.*;
import com.lordjoe.distributed.hydra.*;
import com.lordjoe.distributed.hydra.peptide.*;
import com.lordjoe.distributed.hydra.protein.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.protein.*;
import com.lordjoe.distributed.spectrum.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.api.java.*;
import org.apache.spark.storage.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import scala.Serializable;
import scala.*;

import java.io.*;
import java.lang.Boolean;
import java.util.*;

/**
 * com.lordjoe.distributed.tandem.LibraryBuilder
 * User: Steve
 * Date: 9/24/2014
 */
public class LibraryBuilder implements Serializable {

    public static final boolean USE_PARQUET_DATABASE = true;
    private final XTandemMain application;

    public LibraryBuilder(SparkMapReduceScoringHandler pHandler) {
        this(pHandler.getApplication());
    }

    public LibraryBuilder(XTandemMain app) {
        application = app;
    }


//  //  public SparkApplicationContext getContext() {
//        return context;
//    }

//     public JavaSparkContext getJavaContext() {
//        SparkApplicationContext context1 = getContext();
//        return context1.getCtx();
//    }

    /**
     * generate an RDD of proteins from the database
     *
     * @return
     */
    public JavaRDD<IProtein> readProteins(JavaSparkContext ctx) {
        //      JavaSparkContext ctx = getJavaContext();


        XTandemMain application1 = getApplication();
        String fastaBase = application1.getDatabaseName();
        Path defaultPath = XTandemHadoopUtilities.getDefaultPath();
        String fasta = defaultPath.toString() + "/" + fastaBase + ".fasta";

        // this is a list of proteins the key is the annotation line
        // the value is the sequence
        JavaPairRDD<String, String> parsed = SparkSpectrumUtilities.parseFastaFile(fasta, ctx);

        // if not commented out this line forces proteins to be realized
        //  parsed = SparkUtilities.realizeAndReturn(parsed, ctx);

        return parsed.map(new ParsedProteinToProtein(fastaBase  + ".fasta"));
    }


    public XTandemMain getApplication() {
        return application;
    }

    public JavaRDD<IPolypeptide> buildLibrary() {
        return buildLibrary(0);
    }

    public JavaRDD<IPolypeptide> buildLibrary(final int max_proteins) {

        JavaSparkContext jctx = SparkUtilities.getCurrentContext();
        // if not commented out this line forces proteins to be realized
        //    proteins = SparkUtilities.realizeAndReturn(proteins, ctx);

        XTandemMain app = getApplication();


        JavaRDD<IProtein> proteins = readProteins(jctx);

        long[] proteinCountRef = new long[1];
         proteins = SparkUtilities.persistAndCount("Proteins  to Score", proteins, proteinCountRef);
         long proteinCount = proteinCountRef[0];

        // filter to fewer spectra todo place in loop
        if (max_proteins > 0 && proteinCount > max_proteins) {
             int countPercentile = (int) (PercentileFilter.PERCENTILE_DIVISION * max_proteins / proteinCount);  // try scoring about 1000
            System.err.println("max_proteins " + max_proteins + " proteinCount " + proteinCount + " countPercentile " + countPercentile);
            if (countPercentile < PercentileFilter.PERCENTILE_DIVISION) {
                System.err.println("Filter on "  + countPercentile);
                proteins = proteins.filter(new PercentileFilter<IProtein>(countPercentile)); // todo make a loop
                //proteins = SparkUtilities.persistAndCount("Filtered Proteins  to Score", proteins, proteinCountRef);
             }
        }
        else {
            System.err.println("max_proteins " + max_proteins + " proteinCount " + proteinCount + " NOT FILTERING" );

        }


        // distribute the work
        proteins = SparkUtilities.guaranteePartition(proteins);


        proteins = SparkUtilities.persistAndCount("Proteins  to Score", proteins, proteinCountRef);
        proteinCount = proteinCountRef[0];
        //proteins = SparkUtilities.persistAndCount("Total Proteins", proteins);
        List<IProtein> proteinList  = proteins.collect();
        Collections.sort(proteinList);

        JavaRDD<IPolypeptide> digested = proteins.flatMap(new DigestProteinFunction(app));

        digested = SparkUtilities.persistAndCount("Digested Proteins", digested);

        digested = digested.distinct();

         digested = SparkUtilities.persistAndCount("Digested Proteins Distinct", digested);
        //digested = SparkUtilities.persistAndCount("Digested Proteins", digested);

        // digested = digested.repartition(SparkUtilities.getDefaultNumberPartitions());
        // uncomment when you want to look
        //  digested = SparkUtilities.realizeAndReturn(digested, jctx);


        // Peptide Sequence is the key

        JavaPairRDD<String, IPolypeptide> bySequence = digested.mapToPair(new MapPolyPeptideToSequenceKeys());

        // distribute the work
        bySequence = SparkUtilities.guaranteePairedPartition(bySequence);

        // uncomment when you want to look
        // bySequence = SparkUtilities.realizeAndReturn(bySequence, jctx);

        // Peptide Sequence is the key
        bySequence = PolypeptideCombiner.combineIdenticalPolyPeptides(bySequence);

        // uncomment when you want to look
        // bySequence = SparkUtilities.realizeAndReturn(bySequence, jctx);

       /*
        MassToBinMapper mapper = new MassToBinMapper(app);

        JavaPairRDD<MassBin, IPolypeptide> byMZ = mapper.mapToBins(bySequence.values());
        byMZ = byMZ.persist(StorageLevel.MEMORY_AND_DISK());

        Map<MassBin, Object> massBinCounts = byMZ.countByKey();



        // Mass is the key in daltons
        // JavaPairRDD<Integer, IPolypeptide> byMZ = bySequence.mapToPair(new PeptideByStringToByMass());




        // uncomment when you want to look
        //  byMZ = SparkUtilities.realizeAndReturn(byMZ, jctx);

        //saveAsDatabase(byMZ);
        //saveAsFiles(byMZ);

        saveDigester();
         */

        return bySequence.values();

    }


    protected void saveAsDatabase(JavaPairRDD<Integer, IPolypeptide> pByMZ) {
        if (USE_PARQUET_DATABASE)
            saveAsParquetDatabase(pByMZ);
        else
            saveAsFileDatabase(pByMZ);

    }

    protected void saveAsFileDatabase(JavaPairRDD<Integer, IPolypeptide> byMZ) {

        PeptideDatabaseWriter dbw = new PeptideDatabaseWriter(getApplication());
        JavaPairRDD<Integer, IPolypeptide> sorted = byMZ.sortByKey();
        //JavaPairRDD<Integer, IPolypeptide> sortedAndViewed = SparkUtilities.realizeAndReturn(sorted);
        dbw.saveRDDAsDatabaseFiles(sorted);
    }


    protected PrintWriter getOutputWriter(final int pMass) throws IOException {
        Configuration cfg = SparkUtilities.getHadoopConfiguration();
        Path outPath = XTandemHadoopUtilities.buildPathFromMass(pMass, getApplication());
        FileSystem fs = FileSystem.get(cfg);
        FSDataOutputStream fsout = fs.create(outPath);
        return new PrintWriter(fsout);
    }


    protected void saveAsParquetDatabase(JavaPairRDD<Integer, IPolypeptide> pByMZ) {
        // we will want to use this twice
        pByMZ = pByMZ.persist(StorageLevel.MEMORY_AND_DISK());
        String dbName = buildDatabaseName();
        System.err.println("making database " + dbName);

        Map<Integer, Object> dbSizes = pByMZ.countByKey();
        saveDatabaseSizes(dbName + ".sizes", dbSizes);

        JavaRDD<IPolypeptide> peptides = pByMZ.map(SparkUtilities.TUPLE_VALUES);
        JavaRDD<PeptideSchemaBean> beans = peptides.map(PeptideSchemaBean.TO_BEAN);

        // uncomment to see what is going on
        // beans = SparkUtilities.realizeAndReturn(beans);

        throw new UnsupportedOperationException("Fix This"); // ToDo uncomment next line IntelliJ Does not like it
        //     DatabaseUtilities.buildParaquetDatabase(dbName, beans, PeptideSchemaBean.class);
    }


    protected void saveDatabaseSizes(final String pDbName, final Map<Integer, Object> pDbSizes) {

        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (Integer key : pDbSizes.keySet()) {
            String s = pDbSizes.get(key).toString();
            int count = Integer.parseInt(s);
            counts.put(key, count);
        }
        XTandemMain app = getApplication();
        String paramsFile = application.getDatabaseName() + ".sizes";
        PrintWriter out = SparkHydraUtilities.nameToPrintWriter(paramsFile, app);
        String[] lines = XTandemHadoopUtilities.sizesToStringList(counts);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            out.println(line);
        }
        out.close();

    }

    /**
     * remember the digester uset to generate the current database
     */
    protected void saveDigester() {
        XTandemMain app = getApplication();
        DigesterDescription dd = DigesterDescription.fromApplication(app);
        String paramsFile = application.getDatabaseName() + ".params";
        PrintWriter out = SparkHydraUtilities.nameToPrintWriter(paramsFile, app);
        out.println(dd.asXMLString());
        out.close();
    }


    protected String buildDatabaseName() {
        String fasta = getApplication().getDatabaseName();
        Path defaultPath = XTandemHadoopUtilities.getDefaultPath();
        if (USE_PARQUET_DATABASE)
            return defaultPath.toString() + "/" + fasta + ".parquet";
        else
            return defaultPath.toString() + "/" + fasta;
    }

    /**
     * build text files to save the library
     *
     * @param pByMZ
     */
    protected void saveAsFiles(final JavaPairRDD<Integer, IPolypeptide> pByMZ) {
        JavaPairRDD<Integer, LibraryWriter.WriterObject> files = LibraryWriter.writeDatabase(pByMZ);

        // close all files as a side effect - return nothing
        files = files.filter(new Function<Tuple2<Integer, LibraryWriter.WriterObject>, Boolean>() {
            @Override
            public Boolean call(final Tuple2<Integer, LibraryWriter.WriterObject> v1) throws Exception {
                v1._2().close();
                return false;
            }
        });

        // because of the filter there is nothing there
        files.collect();
    }

    public Map<Integer, Integer> getDatabaseSizes() {
        if (USE_PARQUET_DATABASE)
            return getParquetDatabaseSizes();
        else
            return getFileDatabaseSizes();

    }

    /**
     * get database sizes assuming we use files on HDFS as a database
     *
     * @return
     */
    public Map<Integer, Integer> getFileDatabaseSizes() {
        Configuration conf = SparkUtilities.getCurrentContext().hadoopConfiguration();
        Map<Integer, Integer> ret = XTandemHadoopUtilities.guaranteeDatabaseSizes(getApplication(), conf);
        return ret;
    }


    public Map<Integer, Integer> getParquetDatabaseSizes() {
        try {
            JavaSparkContext sc = SparkUtilities.getCurrentContext();
            JavaSQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
            // Read in the Parquet file created above.  Parquet files are self-describing so the schema is preserved.
            // The result of loading a parquet file is also a JavaSchemaRDD.
            String dbName = buildDatabaseName();

            JavaSchemaRDD parquetFile = sqlContext.parquetFile(dbName);
            //Parquet files can also be registered as tables and then used in SQL statements.
            parquetFile.registerAsTable("peptides");
            JavaSchemaRDD binCounts = sqlContext.sql("SELECT massBin,COUNT(massBin) FROM " + "peptides" + "  GROUP BY  massBin");
            final Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
            JavaRDD<Tuple2<Integer, Integer>> counts = binCounts.map(new Function<Row, Tuple2<Integer, Integer>>() {
                public Tuple2<Integer, Integer> call(Row row) {
                    int mass = row.getInt(0);
                    int count = (int) row.getLong(1);
                    ret.put(mass, count);
                    return new Tuple2<Integer, Integer>(mass, count);
                }
            });
            for (Tuple2<Integer, Integer> countTuple : counts.collect()) {
                ret.put(countTuple._1(), countTuple._2());
            }
            return ret;
        }
        catch (Exception e) {
            return null; // not found
        }
    }


    public static class ParsedProteinToProtein extends AbstractLoggingFunction<Tuple2<String, String>, IProtein> {
        private final String url;

        public ParsedProteinToProtein(final String pUrl) {
            url = pUrl;
        }

        @Override
        public IProtein doCall(final Tuple2<String, String> v1) throws Exception {
            String annotation = v1._1();
            String sequence = v1._2();
            String id = Protein.idFromAnnotation(annotation);
            return Protein.getProtein(id, annotation, sequence, url);

        }
    }


    public static class ProcessByKey extends AbstractLoggingPairFunction<Tuple2<String, IPolypeptide>, Integer, IPolypeptide> {

        private transient List<String> lines;
        private transient String currentKey;

        @Override
        public Tuple2<Integer, IPolypeptide> doCall(final Tuple2<String, IPolypeptide> t) throws Exception {
            String key = t._1();
            if (!key.equals(currentKey))
                processNewKey(key);
            IPolypeptide pp = t._2();
            Integer mz = (int) pp.getMatchingMass(); // todo make more precise
            return new Tuple2(mz, pp);
        }


        private void processNewKey(final String pKey) {
            JavaSparkContext ctx = SparkUtilities.getCurrentContext();
            String path = pKey + ".data";
            JavaRDD<String> linesRDD = ctx.textFile(path);
            lines = linesRDD.collect();
            currentKey = pKey;
        }
    }


    private static class PeptideByStringToByMass implements PairFunction<Tuple2<String, IPolypeptide>, Integer, IPolypeptide> {


        @Override
        public Tuple2<Integer, IPolypeptide> call(final Tuple2<String, IPolypeptide> t) throws Exception {
            IPolypeptide pp = t._2();
            Integer mz = (int) pp.getMatchingMass(); // todo make more precise
            return new Tuple2(mz, pp);
        }
    }


    private static class MapPolyPeptideToSequenceKeys extends AbstractLoggingPairFunction<IPolypeptide, String, IPolypeptide> {

        @Override
        public Tuple2<String, IPolypeptide> doCall(final IPolypeptide t) throws Exception {
            return new Tuple2<String, IPolypeptide>(t.toString(), t);
        }
    }


    public static final int SPARK_CONFIG_INDEX = 0;
    public static final int TANDEM_CONFIG_INDEX = 1;

    public static void main(String[] args) {
        if (args.length < TANDEM_CONFIG_INDEX + 1) {
            System.out.println("usage LibraryBuilder sparkconfigFile tandem.xml");
            return;
        }
      // code to run class loader
        //String runner = SparkUtilities.buildLoggingClassLoaderPropertiesFile(ScanScorer.class  , args);
        //System.out.println(runner);
        ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();

        if (args.length < TANDEM_CONFIG_INDEX + 1) {
            System.out.println("usage sparkconfig configFile fastaFile");
            return;
        }
        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);

        System.out.println("Set Log to Warn");
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);


        Properties sparkProperties = SparkUtilities.getSparkProperties();
        String pathPrepend = sparkProperties.getProperty("com.lordjoe.distributed.PathPrepend");
        if (pathPrepend != null)
            XTandemHadoopUtilities.setDefaultPath(pathPrepend);

        String maxScoringPartitionSize = sparkProperties.getProperty(SparkScanScorer.SCORING_PARTITIONS_SCANS_NAME);
        if (maxScoringPartitionSize != null)
            SparkMapReduceScoringHandler.setMaxScoringPartitionSize(Integer.parseInt(maxScoringPartitionSize));


        String configStr = SparkUtilities.buildPath(args[TANDEM_CONFIG_INDEX]);

        Configuration hadoopConfiguration = SparkUtilities.getHadoopConfiguration();
        //hadoopConfiguration.setLong(org.apache.hadoop.mapreduce.lib.input.FileInputFormat.SPLIT_MAXSIZE, 64 * 1024L * 1024L);

        Configuration hadoopConfiguration2 = SparkUtilities.getHadoopConfiguration();  // did we change the original or a copy


        SparkMapReduceScoringHandler handler = new SparkMapReduceScoringHandler(configStr, false);

        LibraryBuilder builder = new LibraryBuilder(handler);

        String name =  builder.buildDatabaseName();

        JavaRDD<IPolypeptide> peptides = builder.buildLibrary();

        JavaRDD<PeptideSchemaBean> beans = peptides.map(PeptideSchemaBean.TO_BEAN);

        DatabaseUtilities.buildParaquetDatabase(name,beans,PeptideSchemaBean.class);

        totalTime.showElapsed("Done building database " + name);
    }
}
