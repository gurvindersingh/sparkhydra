package org.systemsbiology.xtandem.hadoop;

import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.XTandemHadoopUtilities
 * Common code as statis functions shared by much of the Hadoop implementation
 *
 * @author Steve Lewis
 * @date Mar 8, 2011
 */
public class XTandemHadoopUtilities {


    public static final String[] EXTENSIONS_TO_CLEAN = {".scans", ".pep.xml", ".counters", ".hydra", ".crc"};
    // sequence files are faster but harder to read - change to true
    // when things word well
    public static final boolean USE_SEQUENCE_INTERMEDIATE_FILES = true;
    public static final String[] HADOOP_INTERNAL_COUNTERS = {
            "Spilled Records",
            "FILE_BYTES_READ",
            "Combine output records",
            "Combine input records",
            "Map output bytes",
            "Map input records",
            "Reduce output records",
            "Reduce shuffle bytes",
            "FILE_BYTES_WRITTEN",
            "Reduce input groups",
            "Map output records",
            "Reduce output records",
    };
    // the protein is a decoy if a label starts with one of these
    public static final String[] DECOY_PREFIX = {
            //           "DECOY_",
            "###REV###",
            "###RND###",
            "RND_",
            "REV_",
            "REV1_",


    };

    public static final String DEFAULT_DECOY_PREFIX = "DECOY_";

    public static final Set<String> HADOOP_INTERNAL_COUNTER_SET = new HashSet<String>(Arrays.asList(HADOOP_INTERNAL_COUNTERS));

    public static boolean isCounterHadoopInternal(String name) {
        return HADOOP_INTERNAL_COUNTER_SET.contains(name);
    }

    /**
     * take the label of a protein which might represent a decoy
     * - if it is a decoy return the label with DECOY_ prepended and all other
     * decoy designating text dropped - if it is not a decoy return null
     * - we assume that if the method returns not null than the return is assigned as the protein label and
     * the protein is marked as a decoy
     *
     * @param label !null label
     * @return possibly null new label as above
     */
    public static String asDecoy(String label) {
        if (label.startsWith(DEFAULT_DECOY_PREFIX))
            return label;
        for (int i = 0; i < DECOY_PREFIX.length; i++) {
            String prefix = DECOY_PREFIX[i];
            if (label.startsWith(prefix)) {
                // prefix length removed from label and label goes into substring processed_label

                String processed_label = label.substring(prefix.length(), label.length());

                // if protein accession is prefix then we cannot handle the case
                if (processed_label.length() == 0) {
                    throw new IllegalArgumentException("The protein accession is the same as the decoy prefix!");
                }

                // standard DECOY_ prefix gets added to label and return that

                //noinspection UnnecessaryLocalVariable
                String post_label = DEFAULT_DECOY_PREFIX + processed_label;


                return post_label;

            }
        }
        // the label does not contain a decoy prefix and gets ignored

        return null;
    }


//    private static Set<String> gNotScoredIds = new HashSet<String>(Arrays.asList(NOT_SCORRED_SCANS));
//
//    public static boolean isIdNotScored(String id) {
//        return gNotScoredIds.contains(id);
//    }
//
//    public static boolean isNotScored(ISpectralScan scan) {
//         return isIdNotScored(scan.getId());
//     }
//
//


    /**
     * delete all files with extensions in  EXTENSIONS_TO_CLEAN
     *
     * @param fs
     * @param directory
     */
    public static void cleanFileSystem(FileSystem fs, Path directory) {
        try {
            FileStatus[] files = fs.listStatus(directory);
            if (files == null)
                return;
            for (int i = 0; i < files.length; i++) {
                FileStatus file = files[i];
                Path testPath = file.getPath();
                String name = testPath.getName();
                for (int j = 0; j < EXTENSIONS_TO_CLEAN.length; j++) {
                    if (name.endsWith(EXTENSIONS_TO_CLEAN[j])) {
                        fs.delete(testPath, true);
                        break;
                    }

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void cleanFile(FileSystem fs, Path directory, String file) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


//    public static final String[] MEANINGFUL_COUNTERS =
//            {
//                    "TotalScans",
//                    "TotalScoredScans",
//                    "TotalScoringScans",
//                    "TotalSpectra",
//                    "TotalDotProducts",
//                    "TotalProteins",
//                    "TotalDecoyProteins",
//                    "NumberUniqueFragments",
//                    "DuplicatesOfSize0001",
//                    "DuplicatesOfSize0002",
//                    "DuplicatesOfSize0003",
//            };
//

    public static final String DATABASE_COUNTER_GROUP_NAME = "MassDatabase";
    public static final String PARSER_COUNTER_GROUP_NAME = "Parser";


    public static final String MAX_SCORED_PEPTIDES_KEY = "org.systemsbiology.xtandem.max_scored_peptides";
    public static final String PARAMS_KEY = "org.systemsbiology.xtandem.params";
    public static final String PATH_KEY = "org.systemsbiology.xtandem.hdfs.basepath";
    public static final String FORCE_PATH_PREFIX_KEY = "org.systemsbiology.xtandem.hdfs.forcePathPrefix";
    public static final String HOST_KEY = "org.systemsbiology.xtandem.hdfs.host";
    public static final String HOST_PORT_KEY = "org.systemsbiology.xtandem.hdfs.port";
    public static final String HOST_PREFIX_KEY = "org.systemsbiology.xtandem.hostprefix";

    public static final int DEFAULT_MAX_SCORED_PEPTIDES = 30000;
    public static final int DEFAULT_MAX_SPLIT_SIZE = 64 * 1024 * 1024;

    public static final int DEFAULT_CARRIED_MATCHES = 8;
    public static int gNumberCarriedMatches = DEFAULT_CARRIED_MATCHES;

    /**
     * how many matches including the best are retained
     *
     * @return
     */
    public static int getNumberCarriedMatches() {
        return gNumberCarriedMatches;
    }

    public static void setNumberCarriedMatches(int numberCarriedMatches) {
        gNumberCarriedMatches = numberCarriedMatches;
    }


    private static int gMaxScoredPeptides = DEFAULT_MAX_SCORED_PEPTIDES;
    private static int gMaxSplitSize = DEFAULT_MAX_SPLIT_SIZE;

    public static int getMaxScoredPeptides() {
        return gMaxScoredPeptides;
    }


    public static void setMaxScoredPeptides(final int pMaxScoredPeptides) {
        gMaxScoredPeptides = pMaxScoredPeptides;
    }

    public static int getMaxSplitSize() {
        return gMaxSplitSize;
    }

    public static void setMaxSplitSize(final int pMaxSplitSize) {
        gMaxSplitSize = pMaxSplitSize;
    }

    public static long freeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    private static final Properties gHadoopProperties = new Properties();

    public static void setHadoopProperty(final String pProp, final String pValue) {
        gHadoopProperties.setProperty(pProp, pValue);
    }

    public static Properties getHadoopProperties() {
        return gHadoopProperties;
    }

    /**
     * things most tasks would want to set in their ocnf
     *
     * @param pConf
     */
    public static void setDefaultConfigurationArguments(Configuration pConf) {
        //  disableSpeculativeExecution(pConf);
        enableMapOutputCompression(pConf);
//        raiseIOSortLimits(pConf);

        // aly properties in the properties as DEFINE_...
        for (Object keyObj : gHadoopProperties.keySet()) {
            String key = keyObj.toString();
            String value = gHadoopProperties.getProperty(key);
            pConf.set(key, value);
            System.err.println(key + "=" + value);
        }
    }

    public static void addMoreMappers(final Configuration pConf) {
        pConf.set("mapred.max.split.size", Long.toString(getMaxSplitSize()));
    }

    protected static void enableMapOutputCompression(final Configuration pConf) {
        pConf.set("mapred.compress.map.output", "true");
        pConf.set("mapred.output.compression.type", "BLOCK");
        pConf.set("mapred.map.output.compression.codec", "org.apache.hadoop.io.compress.BZip2Codec");
    }

    protected static void disableSpeculativeExecution(final Configuration pConf) {
        pConf.set("mapred.map.tasks.speculative.execution", "false");
        pConf.set("mapred.reduce.tasks.speculative.execution", "false");
    }

    //     https://issues.apache.org/jira/browse/HADOOP-3473
    protected static void raiseIOSortLimits(final Configuration pConf) {
        pConf.set("io.sort.factor", "100");
        pConf.set("io.sort.mb", "300");
    }

    /**
     * tell a mapper how many peptides to score
     *
     * @param configuration !mull configuration maybe defining MAX_SCORED_PEPTIDES_KEY
     * @return how many to score
     */
    public static int getMaxScoredPeptides(final Configuration configuration) {
        final String maxScored = configuration.get(MAX_SCORED_PEPTIDES_KEY);
        if (maxScored == null)
            return getMaxScoredPeptides();
        else
            return Integer.parseInt(maxScored);
    }

    private static Path gDefaultPath;

    public static void setDefaultPath(String s) {
        XTandemMain.setRequiredPathPrefix(s);
        if (s == null || s.length() == 0)
            gDefaultPath = null;
        else
            gDefaultPath = new Path(s);
    }

    public static Path getDefaultPath() {
         return gDefaultPath;
    }

    public static Path getRelativePath(String s) {
        if (gDefaultPath == null)
            return new Path(s);
        else
            return new Path(gDefaultPath, s);
    }


    /**
     * return all counters in the job
     *
     * @param job
     * @return
     */
    public static Counter[] getAllCounters(Job job) {
        List<Counter> holder = new ArrayList<Counter>();

        try {
            Counters counters = job.getCounters();
            Iterator<CounterGroup> iterator = counters.iterator();
            while (iterator.hasNext()) {
                CounterGroup cg = iterator.next();
                Iterator<Counter> iterator1 = cg.iterator();
                while (iterator1.hasNext()) {
                    Counter counter = iterator1.next();
                    holder.add(counter);
                }
            }


            Counter[] ret = new Counter[holder.size()];
            holder.toArray(ret);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    public static void showAllCounters(Map<String, Counter> counters) {
        Set<String> strings = counters.keySet();
        String[] keys = strings.toArray(new String[strings.size()]);
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String text = key + ":" + counters.get(key).getValue();
            XMLUtilities.outputLine(text);
        }
    }


    public static HadoopConfigurationPropertySet parseHadoopProperties(final InputStream pIs) {
        HadoopConfigurationPropertySetHandler handler = new HadoopConfigurationPropertySetHandler();
        return XTandemUtilities.parseFile(pIs, handler, null);
    }


    /**
     * GenericOptionsParser is not doing its job so we add this
     *
     * @param conf !null conf
     * @param args arguments to process
     * @return !null unprocessed arguments
     */
    public static String[] internalProcessArguments(Configuration conf, String[] args) {
        List<String> holder = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-D".equals(arg)) {
                String define = args[++i];
                processDefine(conf, define);
                continue;
            }
            if ("-filecache".equals(arg)) {
                String file = args[++i];
                if (true)
                    throw new UnsupportedOperationException("Fix This"); // ToDo
                continue;
            }

            holder.add(arg); // not handled
        }

        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    protected static void processDefine(Configuration conf, String arg) {
        String[] items = arg.split("=");
        if (items.length > 1)
            conf.set(items[0].trim(), items[1].trim());
        else
            System.err.println("Argument " + arg + " cannot be parsed");
    }


    /**
     * kill a directory and all contents
     *
     * @param src
     * @param fs
     * @return
     */
    public static boolean expunge(Path src, FileSystem fs) {


        try {
            if (!fs.exists(src))
                return true;
            // break these out
            if (fs.getFileStatus(src).isDir()) {
                boolean doneOK = fs.delete(src, true);
                doneOK = !fs.exists(src);
                return doneOK;
            }
            if (fs.isFile(src)) {
                boolean doneOK = fs.delete(src, false);
                return doneOK;
            }
            throw new IllegalStateException("should be file of directory if it exists");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    // hard coded for our cluster
    private static int gMaxReduceTasks = 20;

    public static int getMaxReduceTasks() {
        return gMaxReduceTasks;
    }

    public static void setMaxReduceTasks(final int pMaxReduceTasks) {
        gMaxReduceTasks = pMaxReduceTasks;
    }

    public static void setRecommendedMaxReducers(Job job) {
        try {
            final Configuration conf = job.getConfiguration();
            if (XTandemHadoopUtilities.isLocal(conf))
                return; // local = 1 reducer
// Specify the number of reduces if defined as a non-negative param.
            // Otherwise, use 9/10 of the maximum reduce tasks (as mentioned by Aalto Cloud,
            // there appears to be no non-deprecated way to do this).
            @SuppressWarnings("deprecation")
            int maxReduceTasks =
                    new JobClient(new JobConf()).getClusterStatus().getMaxReduceTasks();
            int reduces = conf.getInt("reduces", -1);
            if (reduces >= 0) {
                job.setNumReduceTasks(reduces);
            } else {
                reduces = (int) Math.ceil((double) maxReduceTasks * 9.0 / 10.0);
            }
            if (reduces < getMaxReduceTasks())
                reduces = getMaxReduceTasks();
            job.setNumReduceTasks(reduces);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * GenericOptionsParser doea a crappy job on loacl runs - this
     * rechecks for defines
     *
     * @param conf  !null Configuration - properties are set here
     * @param items !null items
     * @return !null remaining items
     */
    public static String[] handleGenericInputs(Configuration conf, String[] items) {
        List<String> holder = new ArrayList<String>();
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            if ("-D".equals(item)) {
                handleDefine(conf, items[++i]);
                continue;
            }
            holder.add(item);
        }
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    protected static void handleDefine(Configuration conf, String pItem) {
        String[] items = pItem.split("=");
        conf.set(items[0].trim(), items[1].trim());
    }

    public static int getTaskNumber(Configuration conf) {
        String n = conf.get("mapred.task.partition");
        return Integer.parseInt(n);
    }


    public static int getNumberReducers(Configuration conf) {
        String n = conf.get("mapred.reduce.tasks");
        return Integer.parseInt(n);
    }


    public static boolean isLocal(Configuration conf) {
        String n = conf.get("mapred.job.tracker");
        return "local".equalsIgnoreCase(n);
    }


    public static Map<Integer, Integer> guaranteeDatabaseSizes(final XTandemMain pApplication,Configuration conf) {
        Map<Integer, Integer> ret;
        //noinspection EmptyCatchBlock
        try {
            ret = readDatabaseSizes(pApplication,conf);
            System.err.println("Read Database Sizes");
            if (ret != null && ret.size() > 0)
                return ret;
        } catch (Exception e) {

        }
        return buildDatabaseSizes(pApplication,conf);
    }

    /**
        * read any cached database parameters
        *
        * @param context     !null context
        * @param application !null application
        * @return possibly null descripotion - null is unreadable
        */
       public static Map<Integer, Integer> readDatabaseSizes(XTandemMain application, Configuration config ) {
           try {
         //      Configuration cfg = application.getContext();
               String paramsFile = application.getDatabaseName() + ".sizes";
                Path dd = XTandemHadoopUtilities.getRelativePath(paramsFile);
               FileSystem fs = FileSystem.get(config);
               if (!fs.exists(dd)) {
                   System.err.println("Cannot find file " + dd.toString());
                   return null;   // cant get it
               }
               InputStream fsout = fs.open(dd);
               Map<Integer, Integer> ret = parseDataFileSizes(fsout);
               return ret;
           } catch (IOException e) {
               return null;

           }
       }

    protected static Map<Integer, Integer> buildDatabaseSizes(final XTandemMain pApplication,Configuration conf) {
        System.err.println("Rebuilding Size database");
        ElapsedTimer et = new ElapsedTimer();

        Map<Integer, SearchDatabaseCounts> dbSizes = XTandemHadoopUtilities.buildFileSystemDatabaseSizes(pApplication,conf);

        Map<Integer, Integer> integerIntegerMap = writeDatabaseSizes(pApplication, dbSizes,conf);
        et.showElapsed("Rebuilt size  database");
        return integerIntegerMap;
    }

    public static Map<Integer, Integer> writeDatabaseSizes(final XTandemMain pApplication, final Map<Integer, SearchDatabaseCounts> pDbSizes,
             Configuration cfg) {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        if (pDbSizes.size() == 0) {
            return ret; // DO not wipe out a potentially better solution
        }
           String paramsFile = pApplication.getDatabaseName() + ".sizes";
        Path dd = XTandemHadoopUtilities.getRelativePath(paramsFile);
        PrintWriter out = null;
        try {
            FileSystem fs = FileSystem.get(cfg);
            OutputStream fsout = fs.create(dd);
            out = new PrintWriter(new OutputStreamWriter(fsout));
            if (true)
                throw new UnsupportedOperationException("Fix This"); // ToDo
            ret = convertSearchDatabaseCounts(pDbSizes);
            String[] lines = sizesToStringList(ret);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                out.println(line);
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (out != null)
                out.close();
        }
    }


    public static void sizesToStringList(PrintWriter out, final Map<Integer, SearchDatabaseCounts> pDbSizes) {
        // convert to a list of strings
        List<String> holder = new ArrayList<String>();
        for (Integer key : pDbSizes.keySet()) {
            SearchDatabaseCounts sc = pDbSizes.get(key);
            String sizeLine = key.toString() + "\t" + sc.getEntries() + "\t" + sc.getModified() + "\t" + sc.getUnmodified();
            out.println(sizeLine);
        }
    }

    public static Map<Integer, Integer> convertSearchDatabaseCounts(final Map<Integer, SearchDatabaseCounts> pDbSizes) {
        HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (Integer key : ret.keySet()) {
            SearchDatabaseCounts sc = pDbSizes.get(key);
            ret.put(key, sc.getEntries());
        }
        return ret;
    }


    public static String[] sizesToStringList(final Map<Integer, Integer> pDbSizes) {
        // convert to a list of strings
        List<String> holder = new ArrayList<String>();
        for (Integer key : pDbSizes.keySet()) {
            String sizeLine = key.toString() + "\t" + pDbSizes.get(key);
            holder.add(sizeLine);
        }
        String[] dbSizeLines = new String[holder.size()];
        holder.toArray(dbSizeLines);
        return dbSizeLines;
    }



    /**
     * find total peptide fragments in the database
     *
     * @param sizes Map of sizes
     * @return total peptides
     */
    public static long sumDatabaseSizes(Map<Integer, Integer> sizes) {
        long ret = 0;
        for (Integer mz : sizes.keySet()) {
            ret += sizes.get(mz);
        }
        return ret;
    }


    public static boolean isFirstMapTask(final Mapper.Context context) {
        TaskAttemptID taskAttemptID = context.getTaskAttemptID();
        TaskID taskID = taskAttemptID.getTaskID();
        String taskid = taskID.toString();
        return taskid.endsWith("_m_000000");
    }


    public static boolean isFirstRericeTask(final Reducer.Context context) {
        TaskAttemptID taskAttemptID = context.getTaskAttemptID();
        TaskID taskID = taskAttemptID.getTaskID();
        String taskid = taskID.toString();
        return taskid.endsWith("_r_000000");
    }

    /**
     * find total peptide fragments in the database
     *
     * @param sizes Map of sizes
     * @return total peptides
     */
    public static long maxDatabaseSizes(Map<Integer, Integer> sizes) {
        long ret = 0;
        if (sizes == null)
            return 0;
        Set<Integer> integers = sizes.keySet();
        for (Integer mz : integers) {
            Integer b = sizes.get(mz);
            if (b == null)
                return 0;
            ret = Math.max(ret, b);
        }
        return ret;
    }

//    /**
//     * read any cached database parameters
//     *
//     * @param context     !null context
//     * @param application !null application
//     * @return possibly null descripotion - null is unreadable
//     */
//    public static void writeDatabaseSizes(HadoopTandemMain application, String sizestr) {
//        try {
//            Configuration cfg = application.getContext();
//            String paramsFile = application.getDatabaseName() + ".sizes";
//            Path dd = XTandemHadoopUtilities.getRelativePath(paramsFile);
//            FileSystem fs = FileSystem.get(cfg);
//            OutputStream fsout = fs.create(dd);
//            FileUtilities.writeFile(fsout, sizestr);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }
//    }
//
    /**
     * reate a file wioth lines like mass\tnumber int o a map
     *
     * @param pRet
     * @param pFsout
     */
    public static Map<Integer, Integer> parseDataFileSizes(final InputStream pFsout) {
        final Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        String[] strings = FileUtilities.readInLines(new InputStreamReader(pFsout));
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            String[] items = string.split("\t");
            if (items.length != 2)
                continue;
            Integer mass = new Integer(items[0]);
            Integer number = new Integer(items[1]);
            ret.put(mass, number);
        }
        return ret;
    }


    /**
     * parse an xml fragment as a string and return the generated object
     *
     * @param text    !null xml fragment
     * @param handler !null handler
     * @param <T>     type to return
     * @return !null return
     */
    public static <T> T parseXMLString(String text, AbstractXTandemElementSaxHandler<T> handler) {
        ByteArrayInputStream inp = null;
        byte[] bytes = null;
        try {
            //noinspection RedundantStringToString
            bytes = text.toString().getBytes();

            inp = new ByteArrayInputStream(bytes);

            //noinspection UnnecessaryLocalVariable
            T scan = XTandemUtilities.parseFile(inp, handler, "");
            return scan;
        } catch (Exception e) {
            e.printStackTrace();
            FileUtilities.writeFile("parseError.xml", text);
            FileUtilities.writeFile("parseError2.xml", new String(bytes));
            String message = "Bad XML Parse Caused by " + e.getMessage() + "\n" + text;
            throw new IllegalArgumentException(message, e);

        }

    }

//    /**
//     * variant of the above to read the MzXML version of a scan fragment
//     *
//     * @param text !null xml fragment
//     * @return !null scan
//     */
//    public static RawPeptideScan readSpectrum(String text) {
//        TopLevelSpectrumHandler handler = new TopLevelSpectrumHandler();
//        return parseXMLString(text, handler);
//    }

    /**
     * variant of the above to read the MzXML version of a scan fragment
     *
     * @param text !null xml fragment
     * @return !null scan
     */
    public static RawPeptideScan readMGFText(String text) {
        LineNumberReader inp = null;
        /*
        * Convert String to InputStream using ByteArrayInputStream
        * class. This class constructor takes the string byte array
        * which can be done by calling the getBytes() method.
        */
        try {
            InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
            inp = new LineNumberReader(new InputStreamReader(is));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return XTandemUtilities.readMGFScan(inp, "");
    }

    /**
     * variant of the above to read the MzXML version of a scan fragment
     *
     * @param text !null xml fragment
     * @return !null scan
     */
    public static RawPeptideScan readScan(String text, String url) {
        text = text.trim();
        if (text.startsWith("<scan")) {
            // ignore level 1 scans
            if (text.contains("msLevel=\"1\""))
                return null;
            TopLevelScanHandler handler = new TopLevelScanHandler();
            if (url != null)
                handler.setDefaultURL(url);
            RawPeptideScan rawPeptideScan = null;
            try {
                rawPeptideScan = parseXMLString(text, handler);
            } catch (Exception e) {
                System.err.println(text);
                throw new RuntimeException(e);

            }
            if (rawPeptideScan == null) {
                handler = new TopLevelScanHandler();
                rawPeptideScan = parseXMLString(text, handler);  // repeat
                return null; // ignore level 1 scans

            }
            if (rawPeptideScan.getMsLevel() < 2)
                return null; // ignore level 1 scans
            return rawPeptideScan;
        }
//        if (text.startsWith("<spectrum")) {
//            if (text.contains("name=\"ms level\" value=\"1\""))
//                return null;
//            RawPeptideScan rawPeptideScan = MzMLReader.scanFromFragment(text);
//            if (rawPeptideScan.getMsLevel() < 2)
//                return null; // ignore level 1 scans
//            return rawPeptideScan;
//        }
        if (text.startsWith("BEGIN IONS")) {
            Reader inp = new StringReader(text);
            RawPeptideScan rawPeptideScan = XTandemUtilities.readMGFScan(new LineNumberReader(inp), "");
            return rawPeptideScan;
        }
        return null;
    }

//
//    /**
//     * variant of the above to read the MzXML ScoredScan of a scan fragment
//     *
//     * @param text !null xml fragment
//     * @return !null scan
//     */
//    public static ScoredScan readScoredScanX(String text, IMainData main) {
//        TopLevelScanScoreHandler handler = new TopLevelScanScoreHandler(main, null);
//        return parseXMLString(text, handler);
//    }
//
//
//    /**
//     * variant of the above to read the MzXML ScoredScan of a scan fragment
//     *
//     * @param text !null xml fragment
//     * @return !null scan
//     */
//    public static MultiScorer readMultiScoredScan(String text, IMainData main) {
//        TopLevelMultiScoreHandler handler = new TopLevelMultiScoreHandler(main, null);
//        return parseXMLString(text, handler);
//    }

    /**
     * turn mass into a file name
     *
     * @param mass
     * @return
     */
    public static String buildFileNameFromMass(int mass) {
        return String.format("M%06d.peptide", mass);
    }

    /**
     * build a file for storing scan data from files
     * Used  largely for building up data for timing tests
     *
     * @throws IOException
     */
    public static SequenceFile.Writer buildScanStoreWriter(Configuration configuration, int mass) {
        try {
            FileSystem fs = FileSystem.get(configuration);
            String fileName = "ScanData/" + XTandemHadoopUtilities.buildFileNameFromMass(mass);
            Path filePath = new Path(fileName);
            return new SequenceFile.Writer(fs, configuration, filePath, Text.class, Text.class);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * build a file for reading scan data from files
     * Used  largely for building up data for timing tests
     *
     * @throws RuntimeException
     */
    public static SequenceFile.Reader buildScanStoreReader(Configuration configuration, int mass) {
        try {
            FileSystem fs = FileSystem.get(configuration);
            String fileName = "ScanData/" + XTandemHadoopUtilities.buildFileNameFromMass(mass);
            Path filePath = new Path(fileName);
            return new SequenceFile.Reader(fs, filePath, configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * read the next scan of null of atr the end
     *
     * @param rdr !null open reader
     * @return possibly null String value
     */
    public static String readNextScan(SequenceFile.Reader rdr) {
        try {
            Text keyTxt = new Text();
            Text valueTxt = new Text();
            if (!rdr.next(keyTxt, valueTxt)) {
                return null;
            }
            return valueTxt.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * turn mass into a file name
     *
     * @param mass
     * @return
     */
    public static Path buildPathFromMass(int mass, IMainData md) {
        String name = buildFileNameFromMass(mass);
        String DataDirectory = md.getDatabaseName();
        Path dd = getRelativePath(DataDirectory);
        Path myPath = new Path(dd, name);
        return myPath;
    }

    /**
     * cheat until we get distributed cache going
     *
     * @param fileName !b=null name of an existing file
     * @param context  ignores
     * @return !null file
     */
    public static File getDistributedFile(String fileName, final TaskInputOutputContext context) {
        return new File(fileName);
    }


//    public static HadoopTandemMain loadFromContext(final TaskInputOutputContext context) {
//        final Configuration configuration = context.getConfiguration();
//
//        return loadFromConfiguration(context, configuration);
//    }
//
//    public static HadoopTandemMain loadFromConfiguration(final TaskInputOutputContext context, final Configuration pConfiguration) {
//        HadoopTandemMain ret = HadoopTandemMain.getInstance();
//        if (ret != null)
//            return ret;
//
//        // note we are reading from hdsf
//        IStreamOpener opener = null;
//        try {
//            opener = new HDFSStreamOpener(pConfiguration);
//            XTandemMain.addPreLoadOpener(opener);
//
//        } catch (Exception e) {
//
//            //noinspection ConstantConditions
//            if (e instanceof RuntimeException) {
//                throw (RuntimeException) e;
//            } else {
//                throw new RuntimeException(e);
//            }
//            //   opener = new FileStreamOpener();
//        }
//
//        final String basedir = pConfiguration.get(PATH_KEY);
//        final String paramsFile = pConfiguration.get(PARAMS_KEY);
//        if (context != null) {
//            if (context instanceof MapContext) {
//                System.err.println("in mapper paramsFile = " + paramsFile);
//            } else if (context instanceof ReduceContext) {
//                System.err.println("in reducer paramsFile = " + paramsFile);
//
//            } else {
//                // Huh - who knows where we are
//                System.err.println("in context " + context.getClass().getName() + " paramsFile = " + paramsFile);
//
//            }
//        }
//        //     File params  =  getDistributedFile(paramsFile,context);
//        InputStream is = opener.open(paramsFile);
//        if (is == null)
//            throw new IllegalStateException(
//                    "cannot open parameters file " + ((HDFSStreamOpener) opener).buildFilePath(
//                            paramsFile));
//        ret = HadoopTandemMain.getInstance(is, paramsFile, pConfiguration);
//
//
//        return ret;
//    }

    /**
     * set the output directory making sure it does not exist
     *
     * @param outputDir !null output directory
     * @param conf      !null conf
     */
    public static void setOutputDirecctory(Path outputDir, Job job) {
        try {
            FileSystem fs = FileSystem.get(outputDir.toUri(), job.getConfiguration());
            FileStatus fileStatus = fs.getFileStatus(outputDir);
            if (fileStatus != null)
                fs.delete(outputDir, true); // get rid of the output directory
            FileOutputFormat.setOutputPath(job, outputDir);

        } catch (FileNotFoundException e) {
            // in a local file ststem this may lead to an error so make the directory
            FileOutputFormat.setOutputPath(job, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

//
//    public static PrintWriter buildDebugWriter(final TaskInputOutputContext context,
//                                               HadoopTandemMain data) {
//        OutputStream os = buildDebugOutputStream(context, data);
//        PrintWriter ret = new PrintWriter(new OutputStreamWriter(os));
//
//
//        return ret;
//    }
//
//    public static PrintWriter buildWriter(final TaskInputOutputContext context,
//                                          HadoopTandemMain data, String added) {
//        OutputStream os = buildOutputStream(context, data, added);
//        PrintWriter ret = new PrintWriter(new OutputStreamWriter(os));
//        return ret;
//    }
//
//
//    public static String dropExtension(String filename) {
//        int index = filename.lastIndexOf(".");
//        if (index == -1)
//            return filename;
//        return filename.substring(0, index);
//    }
//
//    public static PrintWriter buildWriter(final TaskInputOutputContext context,
//                                          HadoopTandemMain data) {
//        return buildWriter(context, data, null);
//    }

//
//    public static LineNumberReader buildreader(final TaskInputOutputContext context,
//                                               HadoopTandemMain data) {
//        InputStream os = buildInputStream(context, data);
//
//        LineNumberReader ret = new LineNumberReader(new InputStreamReader(os));
//
//
//        return ret;
//    }

    public static OutputStream buildOutputStream(TaskInputOutputContext context, String paramsFile, String added) {
        final Configuration configuration = context.getConfiguration();
        // note we are reading from hdsf
        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);

        if (added != null)
            paramsFile += added;
        String filePath = opener.buildFilePath(paramsFile);
        XTandemMain.addPreLoadOpener(opener);
        // note we are reading from hdsf
        safeWrite(context, "Output File", paramsFile);
        HDFSAccessor accesor = opener.getAccesor();
        Path path = new Path(paramsFile);
        OutputStream os = accesor.openFileForWrite(path);

        return os;
    }

    public static PrintWriter buildPrintWriter(TaskInputOutputContext context, String paramsFile, String added) {
//        String paramsFile = buildOutputFileName(context, data);
//        if (added != null)
//            paramsFile += added;
        OutputStream out = buildOutputStream(context, paramsFile, added);
        PrintWriter ret = new PrintWriter(out);
        return ret;
    }


//    public static OutputStream buildOutputStream(TaskInputOutputContext context,
//                                                 HadoopTandemMain data, String added) {
//        final Configuration configuration = context.getConfiguration();
//        String paramsFile = buildOutputFileName(context, data);
//        String hpl = paramsFile.toLowerCase();
//        if (hpl.endsWith(".hydra")) {
//            paramsFile = paramsFile.substring(0, paramsFile.length() - ".hydra".length());
//            hpl = paramsFile.toLowerCase();
//        }
//        if (hpl.endsWith(".mzxml")) {
//            paramsFile = paramsFile.substring(0, paramsFile.length() - ".mzXML".length());
//            hpl = paramsFile.toLowerCase();
//        }
//        if (hpl.endsWith(".mzml")) {
//            paramsFile = paramsFile.substring(0, paramsFile.length() - ".mzml".length());
//            hpl = paramsFile.toLowerCase();
//        }
//        if (added != null)
//            paramsFile += added;
//        //      if (host != null || !"null".equals(host)) {
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//        XTandemMain.addPreLoadOpener(opener);
//        // note we are reading from hdsf
//        safeWrite(context, "Output File", paramsFile);
//        HDFSAccessor accesor = opener.getAccesor();
//        // use a counter to see what we do
//        context.getCounter("outputfile", paramsFile).increment(1);
//        Path path = new Path(paramsFile);
//        OutputStream os = accesor.openFileForWrite(path);
//
//        context.getCounter("outputfile", "total_files").increment(1);
//        return os;
//    }
//

    public static void setInputPath(final Job pJob, String pInputFile) throws IOException {
        if (pInputFile.startsWith("s3n://"))
            pInputFile = pInputFile.substring(pInputFile.lastIndexOf("s3n://"));
        System.err.println("inputFile " + pInputFile);

        Path ath = new Path(pInputFile);

        FileInputFormat.addInputPath(pJob, ath);
    }

//    public static PrintWriter buildPrintWriter(TaskInputOutputContext context, HadoopTandemMain data) {
//        return buildPrintWriter(context, data, null);
//    }


//    public static PrintWriter buildPrintWriter(TaskInputOutputContext context, HadoopTandemMain data, String added) {
//        OutputStream out = buildOutputStream(context, data, added);
//        PrintWriter ret = new PrintWriter(out);
//        return ret;
//    }
//
//
//    public static OutputStream buildOutputStream(TaskInputOutputContext context,
//                                                 HadoopTandemMain data) {
//        return buildOutputStream(context, data, null);
//    }
//
//    public static OutputStream buildDebugOutputStream(TaskInputOutputContext context,
//                                                      HadoopTandemMain data) {
//        return buildOutputStream(context, data, ".debug");
////        final Configuration configuration = context.getConfiguration();
////        String paramsFile = buildDebugOutputFileName(context, data);
////        String host = configuration.get(XTandemHadoopUtilities.HOST_KEY);
////        if (host != null) {
////            HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
////            XTandemMain.addPreLoadOpener(opener);
////            // note we are reading from hdsf
////
////            safeWrite(context, "Output File", paramsFile);
////            HDFSAccessor accesor = opener.getAccesor();
////            OutputStream os = accesor.openFileForWrite(paramsFile);
////
////            return os;
////        }
////        else {
////            try {
////                FileOutputStream os = new FileOutputStream(paramsFile);
////                return os;
////            }
////            catch (FileNotFoundException e) {
////                throw new RuntimeException(e);
////
////            }
////        }
//
//    }

    public static void safeWrite(final TaskInputOutputContext context, final String key,
                                 final String value) {
        try {
            //noinspection unchecked
            context.write(new Text(key), new Text(value));
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }


    public static void setInputArguments(final String[] pOtherArgs, final Job pJob) {
        try {
            if (pOtherArgs.length > 1) {
                String arg = pOtherArgs[0];
                System.err.println("Input Path " + arg);
                XTandemHadoopUtilities.setInputPath(pJob, arg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


//    public static String buildDebugOutputFileName(TaskInputOutputContext context,
//                                                  HadoopTandemMain data)
//    {
//        final Configuration configuration = context.getConfiguration();
//        // note we are reading from hdsf
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//
//        String paramsFile = BiomlReporter.buildDefaultFileName(data);
//        if(paramsFile.endsWith(".xml"))
//            paramsFile = paramsFile.replace(".xml", "_debug.xml");
//        if(paramsFile.endsWith(".params"))
//            paramsFile = paramsFile.replace(".params", "_debug.xml");
//        if(paramsFile.endsWith(".tandem"))
//            paramsFile = paramsFile.replace(".tandem", "_debug.xml");
//
//        String filePath = opener.buildFilePath(paramsFile);
//        return filePath;
//    }
//
//
//    public static String buildOutputFileName(TaskInputOutputContext context,
//                                             HadoopTandemMain data) {
//        final Configuration configuration = context.getConfiguration();
//        // note we are reading from hdsf
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//
//        final String paramsFile = BiomlReporter.buildDefaultFileName(data);
//        String filePath = opener.buildFilePath(paramsFile);
//        return filePath;
//    }
//
//    /**
//     * return a stream representing where the data was written
//     *
//     * @param context
//     * @param data
//     * @return
//     */
//    public static InputStream buildInputStream(TaskInputOutputContext context,
//                                               HadoopTandemMain data) {
//        final Configuration configuration = context.getConfiguration();
//        // note we are reading from hdsf
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//        XTandemMain.addPreLoadOpener(opener);
//
//        final String paramsFile = BiomlReporter.buildDefaultFileName(data);
//        InputStream os = opener.open(paramsFile);
//        return os;
//    }


    /**
     * real version uses distributed cache
     *
     * @param fileName !b=null name of an existing file
     * @param context  ignores
     * @return !null file
     */
    public static File getDistributedFile2(String fileName, final TaskInputOutputContext context) {
        final File[] files = HadoopUtilities.getDistributedCacheFiles(context);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (nameMatchesFile(fileName, file))
                return file;
        }
        return null;
    }

    public static boolean nameMatchesFile(String pFileName, File pFile) {
        return pFile.getAbsolutePath().contains(pFileName);
    }

    public static String cleanXML(final String txt) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);
            if (c == 0)
                continue;
            if (Character.isISOControl(c))
                continue;
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * count the number of lines in an input stream
     *
     * @param f existing readable file
     * @return number of lines
     */
    public static int getNumberLines(final Path path, Configuration conf) {
        try {
            FileSystem fs = FileSystem.get(conf);
            return getNumberLines(path, fs);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * count the number of lines in an input stream
     *
     * @param f existing readable file
     * @return number of lines
     */
    public static int getNumberLines(final Path path, final FileSystem pFs) {
        try {
            if (!pFs.exists(path))
                return 0;
            FSDataInputStream open = pFs.open(path);
            return FileUtilities.getNumberLines(open);
        } catch (IOException e) {
            return 0;

        }
    }

    /**
     * lift teh names of files in a directory
     *
     * @param hdfsPath
     * @param fs
     * @return
     */
    public static String[] ls(String hdfsPath, FileSystem fs) {
        try {
            FileStatus[] statuses = fs.listStatus(new Path(hdfsPath));
            if (statuses == null)
                return new String[0];
            List<String> holder = new ArrayList<String>();
            for (int i = 0; i < statuses.length; i++) {
                FileStatus statuse = statuses[i];
                String s = statuse.getPath().getName();
                holder.add(s);
            }
            String[] ret = new String[holder.size()];
            holder.toArray(ret);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * find the sizes of all databases
     *
     * @param databasePath path to an existing database
     * @param conf         - defined the file system
     * @return
     */
    public static Map<Integer, SearchDatabaseCounts> getDatabaseSizes(final Path databasePath, Configuration conf) {
        try {
            Map<Integer, SearchDatabaseCounts> ret = new HashMap<Integer, SearchDatabaseCounts>();
            FileSystem fs = FileSystem.get(conf);
            FileStatus[] fileStatuses = fs.globStatus(new Path(databasePath + "/M0*.peptide"));
            for (int i = 0; i < fileStatuses.length; i++) {
                FileStatus fileStatus = fileStatuses[i];
                Path path = fileStatus.getPath();
                addPathSize(ret, fs, path);
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static SearchDatabaseCounts buildFromPath(final FileSystem pFs, final Path pPath) {
        int nPeptides = 0;
        int nModifiedPeptides = 0;
        int nUnModifiedPeptides = 0;
        try {
            final FSDataInputStream open = pFs.open(pPath);
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(open));
            String line = rdr.readLine();
            while (line != null) {
                nPeptides++;
                if (!line.contains("]"))
                    nUnModifiedPeptides++;
                else
                    nModifiedPeptides++;
                line = rdr.readLine();
            }
            int bin = pathStringToBin(pPath.getName());
            SearchDatabaseCounts ret = new SearchDatabaseCounts(bin, nPeptides, nModifiedPeptides, nUnModifiedPeptides);
            return ret;

        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }


    }


    public static SearchDatabaseCounts buildFromSubCounts(Collection<SearchDatabaseCounts> subcounts) {
        int nPeptides = 0;
        int nModifiedPeptides = 0;
        int nUnModifiedPeptides = 0;
        for (SearchDatabaseCounts subcount : subcounts) {
            nPeptides += subcount.getEntries();
            nModifiedPeptides += subcount.getModified();
            nUnModifiedPeptides += subcount.getUnmodified();
        }
        SearchDatabaseCounts ret = new SearchDatabaseCounts(0, nPeptides, nModifiedPeptides, nUnModifiedPeptides);
        return ret;
    }


    protected static int pathStringToBin(String name) {
        name = name.replace(".peptide", "");
        name = name.replace("M0", "");
        return Integer.parseInt(name);
    }

    protected static void addPathSize(final Map<Integer, SearchDatabaseCounts> mapp, final FileSystem pFs, final Path pPath) {
        int nlines = XTandemHadoopUtilities.getNumberLines(pPath, pFs);
        SearchDatabaseCounts dc = buildFromPath(pFs, pPath);

        String mass = pPath.getName().replace("M0", "").replace(".peptide", "");
        int imass = Integer.parseInt(mass);
        mapp.put(imass, dc);
    }

//
//    public static String buildCounterFileName(IJobRunner runner, Configuration pConf) {
//        String dir = pConf.get(PATH_KEY);
//        if (dir == null)
//            dir = "";
//        else
//            dir += "/";
//        String fileName = runner.getClass().getSimpleName() + ".counters";
//        return dir + fileName;
//    }

    /**
     * write the jobs counters  to a file called fileName in fileSystem
     *
     * @param fileSystem !null
     * @param fileName   !null
     * @param job        !null
     */
    public static void saveCounters(FileSystem fileSystem, String fileName, Job job) {
        Map<String, Long> counters = getAllJobCounters(job);
        Path p = new Path(fileName);
        PrintWriter out = null;
        try {
            FSDataOutputStream os = fileSystem.create(p, true); // create with overwrite
            out = new PrintWriter(new OutputStreamWriter(os));
            Set<String> stringSet = counters.keySet();
            String[] items = stringSet.toArray(new String[stringSet.size()]);
            Arrays.sort(items);
            for (String s : items) {
                out.println(s + "=" + counters.get(s));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (out != null)
                out.close();
        }
    }


    public static Map<String, Long> getAllJobCounters(Job job) {
        try {
            Map<String, Long> ret = new HashMap<String, Long>();
            Counters counters = job.getCounters();
            Iterator<CounterGroup> iterator = counters.iterator();
            while (iterator.hasNext()) {
                addCounterGroup(iterator.next(), ret);
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    public static final String TASK_COUNTER_NAME = "org.apache.hadoop.mapred.Task$Counter";
    public static final String JOB_IN_PROGRESS_COUNTER_NAME = "org.apache.hadoop.mapred.JobInProgress$Counter";

    private static void addCounterGroup(final CounterGroup group, final Map<String, Long> map) {
        String groupName = group.getName();
        boolean groupNameIsDefault = TASK_COUNTER_NAME.equals(groupName) ||
                JOB_IN_PROGRESS_COUNTER_NAME.equals(groupName);
        Iterator<Counter> iterator = group.iterator();
        while (iterator.hasNext()) {
            Counter c = iterator.next();
            String counterName = c.getName();
            long value = c.getValue();

            String key;
            if (groupNameIsDefault)
                key = counterName;
            else
                key = groupName + ":" + counterName;
            map.put(key, value);
        }

    }


    /**
     * read a fine in the path as text lines
     * NOTE we may assume this is a "Small" file
     *
     * @param path !null existing path
     * @param conf !null conf
     * @return !null data
     */
    public static String[] readTextLines(final Path path, Configuration conf) {
        try {
            FileSystem fs = FileSystem.get(conf);
            if (!fs.exists(path))
                return new String[0];
            FSDataInputStream open = fs.open(path);
            try {
                return FileUtilities.readInLines(new InputStreamReader(open));
            } catch (RuntimeException e) {
                throw new RuntimeException("cannot read path" + path.getName() + " caused by " + e.getMessage(), e);
            } finally {
                FileUtilities.guaranteeClosed(open);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * read a fine in the path as text lines
     * NOTE we may assume this is a "Small" file
     *
     * @param path !null existing path
     * @param conf !null conf
     * @return !null data
     */
    public static LineNumberReader openTextLines(final Path path, Configuration conf) {
        try {
            FileSystem fs = FileSystem.get(conf);
            if (!fs.exists(path))
                return null;
            FSDataInputStream open = fs.open(path);
            return new LineNumberReader(new InputStreamReader(open));
        } catch (RuntimeException e) {
            throw new RuntimeException("cannot read path" + path.getName() + " caused by " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * return a map - key is MZ as int - value is number entries
     *
     * @param application
     * @return
     */
    protected static Map<Integer, SearchDatabaseCounts> buildFileSystemDatabaseSizes(final XTandemMain application, Configuration cfg) {
           Path databasePath = XTandemHadoopUtilities.getRelativePath(application.getDatabaseName());
        Map<Integer, SearchDatabaseCounts> ret = getDatabaseSizes(databasePath, cfg);
        return ret;

    }

    public static final String[] EXCLUDED_LIBRARIES =
            {
                    "openjpa-persistence-2.0.0.jar",
                    "openjpa-kernel-2.0.0.jar",
                    "openjpa-lib-2.0.0.jar",
                    //           "commons-logging-1.1.1.jar",
                    //            "commons-lang-2.1.jar",
                    //            "commons-collections-3.2.1.jar",
                    "serp-1.13.1.jar",
                    "geronimo-jms_1.1_spec-1.1.1.jar",
                    "geronimo-jta_1.1_spec-1.1.1.jar",
                    //           "commons-pool-1.3.jar",
                    "geronimo-jpa_2.0_spec-1.0.jar",
                    "mysql-connector-java-5.0.4.jar",
                    //            "commons-dbcp-1.2.2.jar",
                    //            "commons-cli-1.2.jar",
                    //            "jsch-0.1.44-1.jar",
                    //            "hadoop-core-0.20.2.jar",
                    //             "xmlenc-0.52.jar",
                    //            "commons-httpclient-3.0.1.jar",
                    //             "commons-codec-1.3.jar",
                    //            "commons-net-1.4.1.jar",
                    "oro-2.0.8.jar",
                    "jetty-6.1.25.jar",
                    "jetty-util-6.1.14.jar",
                    "servlet-api-2.5-6.1.14.jar",
                    "jasper-runtime-5.5.12.jar",
                    "jasper-compiler-5.5.12.jar",
                    "jsp-api-2.1-6.1.14.jar",
                    "jsp-2.1-6.1.14.jar",
                    //           "core-3.1.1.jar",
                    "ant-1.6.5.jar",
                    //           "commons-el-1.0.jar",
                    "jets3t-0.7.1.jar",
                    "kfs-0.3.jar",
                    "hsqldb-1.8.0.10.jar",
                    "servlet-api-2.5-20081211.jar",
                    "slf4j-log4j12-1.4.3.jar",
                    "slf4j-api-1.4.3.jar",
                    //          "log4j-1.2.9.jar",
                    "xml-apis-1.0.b2.jar",
                    "xml-apis-ext-1.3.04.jar",
                    "spring-jdbc-2.5.6.jar",
                    //          "spring-beans-2.5.6.jar",
                    //         "spring-core-2.5.6.jar",
                    //         "spring-context-2.5.6.jar",
                    //         "aopalliance-1.0.jar",
                    //        "spring-tx-2.5.6.jar",

            };

//    public static void main(String[] args) {
//        File deployDir = new File("/JXTandemDeploy");
//        JXTandemDeployer depl = new JXTandemDeployer();
//        depl.clearTaskExcludeJars();
//        for (int i = 0; i < EXCLUDED_LIBRARIES.length; i++) {
//            String arg = EXCLUDED_LIBRARIES[i];
//            depl.addTaskExcludeJar(arg);
//        }
//        Class mainClass = JXTandemLauncher.class;
//        depl.deploy(deployDir, mainClass, args);
//    }

}
