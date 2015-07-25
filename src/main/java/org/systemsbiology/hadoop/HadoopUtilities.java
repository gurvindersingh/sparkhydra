package org.systemsbiology.hadoop;

import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.filecache.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.hadoop.HadoopUtilities
 * written by Steve Lewis
 * on May 4, 2010
 */
public class HadoopUtilities {
    public static final Class THIS_CLASS = HadoopUtilities.class;

    public static final int DEFAULT_MAX_SPLIT_SIZE = 64 * 1024 * 1024;
    private static int gMaxSplitSize = DEFAULT_MAX_SPLIT_SIZE;


    public static final String GROUP_NAME = "Genome";

    public static final Text HEADER_KEY = new Text("<SAM HEADER>");

    public static final String DEFAULT_HEADER_TEXT = "@HD\tVN:1.0\tSO:unsorted";

    public static final String SYSBIO_KEY_BASE = "org.systemsbiology";
    public static final String CONFIGURATION_KEY = SYSBIO_KEY_BASE + ".configuration";
    public static final String INPUT_FILE_KEY = SYSBIO_KEY_BASE + ".inputFile";
    public static final String INPUT_NAME_KEY = SYSBIO_KEY_BASE + ".inputName";
    public static final String STEP_NAME_KEY = SYSBIO_KEY_BASE + ".StepName";
    public static final String UUID_KEY = SYSBIO_KEY_BASE + ".UUID";
    public static final String JOB_ID_NAME_KEY = SYSBIO_KEY_BASE + ".JobId";

    public static final String CONF_KEY = SYSBIO_KEY_BASE + ".configfile";
    public static final String REPORT_KEY = SYSBIO_KEY_BASE + ".reportfile"; // "Report";
    public static final int IN_POSITION_START = 4;
    public static final String JAR_PROPERTY = "org.systemsbiology.tandem.hadoop.PrebuiltJar";


    public static final String PARAMS_PROPERTY = "params";
    public static final String REMOTE_HOST_PROPERTY = "remoteHost";
    public static final String REMOTE_PORT_PROPERTY = "remotePort";
    public static final String REMOTE_USER_PROPERTY = "remoteUser";
    public static final String REMOTE_JOBTRACKER_PROPERTY = "remoteJobTracker";
    public static final String REMOTE_ENCRYPTED_PASSWORD_PROPERTY = "encryptedRemotePassword";
    public static final String REMOTE_PLAINTEXT_PASSWORD_PROPERTY = "plainTextRemotePassword";
    public static final String REMOTEDIRECTORY_PROPERTY = "remoteBaseDirectory";
    public static final String COMPRESS_INTERMEDIATE_FILES_PROPERTY = "compressIntermediateFiles";
    public static final String MAX_SPLIT_SIZE_PROPERTY = "maxSplitSize";
    public static final String DELETE_OUTPUT_DIRECTORIES_PROPERTY = "deleteOutputDirectories";
    public static final String MAX_CLUSTER_MEMORY_PROPERTY = "maxClusterMemory";
    public static final String CLUSTER_SIZE_PROPERTY = "clusterSize";
    public static final String HADOOP02_HOST = "hadoop02Host";
    public static final String HADOOP02_PORT = "hadoop02Port";
    public static final String HADOOP02_JOBTRACKER = "hadoop02remoteJobTracker";
    public static final String HADOOP10_HOST = "hadoop10Host";
    public static final String HADOOP10_PORT = "hadoop10Port";
    public static final String HADOOP10_JOBTRACKER = "hadoop10remoteJobTracker";
    public static final String JOB_SIZE_PROPERTY = "job_size";
    public static final String MAX_REDUCE_TASKS_PROPERTY = "maxReduceTasks";


    public static final String WRITING_PEPXML_PROPERTY = "org.systemsbiology.xtandem.hadoop.WritePepXML";
    public static final String WRITING_MGF_PROPERTY = "org.systemsbiology.xtandem.hadoop.WriteMGFSpectraWithHyperscoreGreaterThan";
    public static final String WRITING_MGF_PROPERTY_2 = "org.systemsbiology.xtandem.hadoop.WriteMGFSpectraWithExpectValueLowerThan";
    public static final String CREATE_DECOY_PEPTIDES_PROPERTY = "org.systemsbiology.xtandem.CreateDecoyPeptides";


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


    private static final List<IStreamOpener> gPreLoadOpeners =
            new ArrayList<IStreamOpener>();

    public static void addPreLoadOpener(IStreamOpener opener) {
        gPreLoadOpeners.add(opener);
    }

    public static IStreamOpener[] getPreloadOpeners() {
        return gPreLoadOpeners.toArray(new IStreamOpener[gPreLoadOpeners.size()]);
    }

    // Hard code this so we can debug partitioner code
    public static final int DEFAULT_TEST_NUMBER_REDUCERS = 64;

    public static final int DEFAULT_REDUCE_TASKS = 14;
    // in development to speed up
    public static String gReuseJar; //"Mar231041_0.jar";   //  null; //


    private static Properties gRemoteProperties = new Properties();

    public static void setProperty(String key, String value) {
        gRemoteProperties.setProperty(key, value);
    }

    public static String getProperty(String key) {
        return gRemoteProperties.getProperty(key);
    }

    public static String getReuseJar() {
        return gReuseJar;
    }

    public static void setReuseJar(final String pReuseJar) {
        gReuseJar = pReuseJar;
    }

    private static IConfigureFileSystem gFileSystemConfigurer = IConfigureFileSystem.NULL_CONFIGURE_FILE_SYSTEM;

    /**
     * return the configurer
     *
     * @return !null configurer
     */
    public static synchronized IConfigureFileSystem getFileSystemConfigurer() {
        if (gFileSystemConfigurer == null)
            gFileSystemConfigurer = IConfigureFileSystem.NULL_CONFIGURE_FILE_SYSTEM;
        return gFileSystemConfigurer;
    }

    public static synchronized void setFileSystemConfigurer(final IConfigureFileSystem pFileSystemConfigurer) {
        gFileSystemConfigurer = pFileSystemConfigurer;
    }

    // limit to stop memory from beign exceeded
    public static final String MAXRECORDS_KEY = SYSBIO_KEY_BASE + ".max_records_per_partition";
    public static final int MAXIMUM_PARTITION_RECORDS = 18000;


    public static final String STATISTICS_REPORT_KEY = SYSBIO_KEY_BASE + ".statistics";

//    private static SAMFileReader.ValidationStringency gValidationStringency =
//            SAMFileReader.ValidationStringency.LENIENT;

    private HadoopUtilities() {
    } // do not construct

    /**
     * make sure splits cover the file and do not duplicate
     * @param pSplits
     */
    public static void validateSplits(final List<InputSplit> pSplits) {
         long nextStart = 0;

         for (InputSplit split : pSplits) {
             org.apache.hadoop.mapreduce.lib.input.FileSplit fs = (org.apache.hadoop.mapreduce.lib.input.FileSplit)split;
             long start = fs.getStart();
             long length = fs.getLength();
             if(start > 0) {
                if(start != nextStart)
                    throw new IllegalStateException("bad split starts at " + start + " should be " + nextStart);
             }
             nextStart = start + length;
         }
     }


    /**
     * return all cached local files
     *
     * @param context
     * @return
     */
    public static File[] getDistributedCacheFiles(final TaskInputOutputContext context) {
        try {
            Configuration configuration = context.getConfiguration();

            LocalFileSystem system = FileSystem.getLocal(configuration);
            Path[] files = DistributedCache.getLocalCacheFiles(configuration);
            List<File> holder = new ArrayList<File>();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    Path file = files[i];
                    File file1 = system.pathToFile(file);
                    holder.add(file1);
                }
            }


            File[] ret = new File[holder.size()];
            holder.toArray(ret);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    public static long getCounterValue(Enum val, Job job) {
        try {
            Counters counters = job.getCounters();
            Counter counter = counters.findCounter(val);
            return counter.getValue();
        }
//        catch (InterruptedException e) {
//               throw new RuntimeException(e);
//
//           }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    public static final int DEFAULT_CLUSTER_SIZE = 16;

    /**
     * return the current cluster size which should be set somewhere
     *
     * @return as above
     */
    public static int getClusterSize() {
        String property = getProperty(CLUSTER_SIZE_PROPERTY);
        if (property != null)
            return Integer.parseInt(property);
        else
            return DEFAULT_CLUSTER_SIZE;
    }

    public static void setClusterSize(int size) {
        setProperty(CLUSTER_SIZE_PROPERTY, Integer.toString(size));
    }

    public static final JobSizeEnum DEFAULT_JOB_SIZE = JobSizeEnum.Enormous;

    /**
     * return the jobsize estimate - otherwise return JobSizeEnum.Medium
     *
     * @return as above
     */
    public static JobSizeEnum getJobSize() {
        String property = getProperty(JOB_SIZE_PROPERTY);
        if (property != null)
            return JobSizeEnum.valueOf(property);
        else
            return DEFAULT_JOB_SIZE;

    }

    public static void setCounterValue(Enum val, long newValue, Job job) {
        try {
            Counters counters = job.getCounters();
            Counter counter = counters.findCounter(val);
            long value = counter.getValue();
            counter.increment(newValue - value);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    public static boolean isStandAlone() {
        //noinspection SimplifiableIfStatement,RedundantIfStatement
        if (!isWindows())
            return false;
        return true; // todo add more tests
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux");
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("windows");
    }

    public static void writeResourceAsFile(Class cls, String resourceName, LocalFileSystem localFs, String dstFile) {
        InputStream inp = cls.getResourceAsStream(resourceName);
        writeStreamAsFile(localFs, dstFile, inp);
    }

    public static void writeStreamAsFile(final LocalFileSystem localFs, final String dstFile, final InputStream pInp) {
        Path path = new Path(dstFile);
        try {
            FSDataOutputStream outStream = localFs.create(path);
            copyFile(pInp, outStream);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void writeStringAsFile(final LocalFileSystem localFs, final String dstFile, final String s) {
        ByteArrayInputStream inp = new ByteArrayInputStream(s.getBytes());
        writeStreamAsFile(localFs, dstFile, inp);
    }

    /**
     * { method
     *
     * @param dst destination file name
     * @param src source file name
     * @return true for success
     * }
     * @name copyFile
     * @function copy file named src into new file named dst
     */
    public static boolean copyFile(InputStream inp, FSDataOutputStream outStream) {
        int bufsize = 1024;
        try {
            // failure - no data

            int bytesRead = 0;
            byte[] buffer = new byte[bufsize];
            while ((bytesRead = inp.read(buffer, 0, bufsize)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException ex) {
            return (false);
        } finally {
            FileUtilities.guaranteeClosed(inp);
            FileUtilities.guaranteeClosed(outStream);
        }

    }


    private static void foo() throws Exception {
        Path p = new Path("/my/path");
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        FileStatus[] fileStats = fs.listStatus(p);
        for (int i = 0; i < fileStats.length; i++) {
            Path f = fileStats[i].getPath();// do something interesting}
        }
    }


    public static final Text ONLY_KEY = new Text(); // avoid garbage collection


    public static final Text ONLY_KEY1 = new Text(); // avoid garbage collection
    public static final Text ONLY_VALUE = new Text(); // avoid garbage collection

    /**
     * wrapper around text output
     *
     * @param ctx     !null context
     * @param key
     * @param message
     */
    public static void writeText(TaskInputOutputContext ctx, String key, String message) {
        try {
            ONLY_KEY1.set(key);
            ONLY_VALUE.set(message);
            //noinspection unchecked
            ctx.write(ONLY_KEY1, ONLY_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }

    }


    public static final String FIELD_SEPARATOR = "\t";

    public static String[] readConfigFileFS(String pArg) {
        //   if (pArg.startsWith("s3n://"))
        //       return AWSUtilities.readConfigFileS3(pArg);

        File configuration = new File(pArg);
        String[] pLines = null;
        boolean exists = configuration.exists();
        System.err.println("File " + pArg + " exists " + exists);
        boolean isFile = configuration.isFile();
        System.err.println("File " + pArg + " isFile " + isFile);
        if (exists && isFile) {
            List<String> holder = null;
            String line = null;
            try {
                System.err.println("File " + pArg + " ready to open");
                LineNumberReader nr = new LineNumberReader(new FileReader(pArg));
                System.err.println("File " + pArg + "   open");
                holder = new ArrayList<String>();
                line = nr.readLine();
                while (line != null) {
                    System.err.println(line);
                    holder.add(line);
                    line = nr.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
                throw new RuntimeException(e);

            }
            String[] ret = new String[holder.size()];
            pLines = holder.toArray(ret);
            for (int i = 0; i < pLines.length; i++) {
                line = pLines[i];
                //   System.err.println("line " + i + " " + line);
                // tell the system about the chromosome set
            }

        }
        return pLines;
    }


    //
//    private static final TextTagCodec tagCodec = new TextTagCodec();
//    private static final SAMTagUtil tagUtil = new SAMTagUtil();

    public static enum KeepAliveEnum {
        KeepAlive
    }


    public static enum MapFailureCount {
        FailureCount
    }


    public static enum ReduceFailureCount {
        FailureCount
    }


    /**
     * a certain number of failures are allowed in a mapper
     * note that one has occured
     *
     * @param ctx         !null context
     * @param id          count id
     * @param maxFailures throw an exception after this
     * @throws TaskFailException
     */
    public static void addFailure(TaskInputOutputContext cts, Throwable ex, Enum id, int maxFailures) {
        //noinspection unchecked
        Counter counter = cts.getCounter(KeepAliveEnum.KeepAlive);
        int value = (int) counter.getValue();
        if (value > maxFailures)
            throw new TaskFailException(value, ex);
        ex.printStackTrace(System.err);
        counter.increment(1);

    }

    public static class TaskFailException extends RuntimeException {
        /**
         * Constructs a new runtime exception with <code>null</code> as its
         * detail message.  The cause is not initialized, and may subsequently be
         * initialized by a call to {@link #initCause}.
         */
        public TaskFailException(int numberFailures, Throwable ex) {
            super("Failed after " + numberFailures + " failures", ex);
        }
    }

    public static final int DEFAULT_KEEP_ALIVE_TIME = 10000; // 10 sec
    private static int gKeepAliveTimeMillisec = DEFAULT_KEEP_ALIVE_TIME;

    public static int getKeepAliveTimeMillisec() {
        return gKeepAliveTimeMillisec;
    }

    public static void setKeepAliveTimeMillisec(int pKeepAliveTimeMillisec) {
        gKeepAliveTimeMillisec = pKeepAliveTimeMillisec;
    }

    public static long getLastKeepAlive() {
        return gLastKeepAlive;
    }

    public static void setLastKeepAlive(long pLastKeepAlive) {
        gLastKeepAlive = pLastKeepAlive;
    }

    public static long gLastKeepAlive = System.currentTimeMillis();

    public static final ThreadLocal<TaskInputOutputContext> gCurrentContext = new ThreadLocal<TaskInputOutputContext>();

    public static void setCurrentContext(TaskInputOutputContext ctx) {
        gCurrentContext.set(ctx);
    }

    public static TaskInputOutputContext getCurrentContext() {
        return gCurrentContext.get();
    }

    /**
     * Set a counter to keep job from terminating
     *
     * @param cts !null task to tickle
     */
    public static void keepAlive() {
        keepAlive(getCurrentContext());
    }

    /**
     * /**
     * Set a counter to keep job from terminating
     *
     * @param cts !null task to tickle
     */
    public static void keepAlive(TaskInputOutputContext cts) {
        long now = System.currentTimeMillis();
        if (now < getLastKeepAlive() + getKeepAliveTimeMillisec())
            return;
        //noinspection unchecked
        final Counter counter = cts.getCounter(KeepAliveEnum.KeepAlive);
        counter.increment(1);
        setLastKeepAlive(now);
    }


    public static int numberReducersFromJobSize(JobSizeEnum size, int clusterSize) {
        switch (size) {
            case Micro:
                return 1;
            case Small:
                return 8;
            case Medium:
                return 4 * clusterSize;
            case Large:
                return 8 * clusterSize;
            case Enormous:
                return 3500;
            default:
                throw new IllegalArgumentException("unknown size");
        }
    }


    public static void setRecommendedMaxReducers(Job job, JobSizeEnum jobSize) {
        try {
            final Configuration conf = job.getConfiguration();
            if (isLocal(conf))
                return; // local = 1 reducer
            // Specify the number of reduces if defined as a non-negative param.
            // Otherwise, use 9/10 of the maximum reduce tasks (as mentioned by Aalto Cloud,
            // there appears to be no non-deprecated way to do this).
            JobClient jobClient = new JobClient(new JobConf());
            ClusterStatus clusterStatus = jobClient.getClusterStatus();
            @SuppressWarnings("deprecation")


            int maxReduceTasks = clusterStatus.getMaxReduceTasks();

            maxReduceTasks = Math.max(maxReduceTasks, numberReducersFromJobSize(jobSize, getClusterSize()));
            int reduces = conf.getInt("reduces", -1);
            if (reduces >= 0) {
                job.setNumReduceTasks(reduces);
            } else {
                reduces = (int) Math.ceil((double) maxReduceTasks * 9.0 / 10.0);
            }
            job.setNumReduceTasks(reduces);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
//    /**
//     * Writes the record to disk.  Sort order has been taken care of by the time
//     * this method is called.
//     *
//     * @param alignment
//     */
//    public static String buildAlignmentText(final SAMRecord alignment) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(alignment.getReadName());
//        sb.append(FIELD_SEPARATOR);
//        sb.append(Integer.toString(alignment.getFlags()));
//        sb.append(FIELD_SEPARATOR);
//        sb.append(alignment.getReferenceName());
//        sb.append(FIELD_SEPARATOR);
//        sb.append(Integer.toString(alignment.getAlignmentStart()));
//        sb.append(FIELD_SEPARATOR);
//        sb.append(Integer.toString(alignment.getMappingQuality()));
//        sb.append(FIELD_SEPARATOR);
//        sb.append(alignment.getCigarString());
//        sb.append(FIELD_SEPARATOR);
//
//        //  == is OK here because these strings are interned
//        if (alignment.getReferenceName().equals(alignment.getMateReferenceName()) &&
//                !SAMRecord.NO_ALIGNMENT_REFERENCE_NAME.equals(alignment.getReferenceName())) {
//            sb.append("=");
//        }
//        else {
//            sb.append(alignment.getMateReferenceName());
//        }
//        sb.append(FIELD_SEPARATOR);
//        sb.append(Integer.toString(alignment.getMateAlignmentStart()));
//        sb.append(FIELD_SEPARATOR);
//        sb.append(Integer.toString(alignment.getInferredInsertSize()));
//        sb.append(FIELD_SEPARATOR);
//        sb.append(alignment.getReadString());
//        sb.append(FIELD_SEPARATOR);
//        sb.append(alignment.getBaseQualityString());
//        if(true)
//            throw new UnsupportedOperationException("Fix This"); // ToDo
////        if (alignment.getBinaryAttributes() != null) {  // todo fix
////            for (final SAMBinaryTagAndValue attribute : alignment.getBinaryAttributes()) {
////                sb.append(FIELD_SEPARATOR);
////                sb.append(tagCodec.encode(tagUtil.makeStringTag(attribute.tag), attribute.value));
////            }
////        }
////
//        return sb.toString();
//    }

    /**
     * convert a name like  C:\Inetpub\wwwroot\ISB\data\parameters\isb_default_input_kscore.xml
     * to  isb_default_input_kscore.xml
     *
     * @param fileName !null file name
     * @return !null name
     */
    public static String asLocalFile(String fileName) {
        fileName = fileName.replace("\\", "/");
        File f = new File(fileName);
        return f.getName();
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


    private static Path gDefaultPath;

    public static void setDefaultPath(String s) {
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
            outputLine(key + ":" + counters.get(key).getValue());
        }
    }


    public static void outputLine(String text) {
        System.out.println(text);
    }

    public static void outputText(String text) {
        System.out.print(text);
    }

    public static void errorLine(String text) {
        System.err.println(text);
    }

    public static void errorLine() {
        System.err.println();
    }

    public static void errorText(String text) {
        System.err.print(text);
    }

    public static String freeMemoryString() {
        StringBuilder sb = new StringBuilder();
        Runtime rt = Runtime.getRuntime();
        double mem = rt.freeMemory() / 1000000;
        double totmem = rt.totalMemory() / 1000000;

        sb.append(String.format("%5.1f", mem));
        sb.append(String.format(" %4.2f", mem / totmem));
        return sb.toString();
    }

//
//    public static HadoopConfigurationPropertySet parseHadoopProperties(final InputStream pIs) {
//        HadoopConfigurationPropertySetHandler handler = new HadoopConfigurationPropertySetHandler();
//        return XTandemUtilities.parseFile(pIs, handler, null);
//    }
//

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
            if (isLocal(conf))
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

//
//    /**
//     * parse an xml fragment as a string and return the generated object
//     *
//     * @param text    !null xml fragment
//     * @param handler !null handler
//     * @param <T>     type to return
//     * @return !null return
//     */
//    public static <T> T parseXMLString(String text, AbstractElementSaxHandler<T> handler) {
//        ByteArrayInputStream inp = null;
//        byte[] bytes = null;
//        try {
//            bytes = text.toString().getBytes();
//
//            inp = new ByteArrayInputStream(bytes);
//
//            T scan =  parseFile(inp, handler, "");
//            return scan;
//        } catch (Exception e) {
//            e.printStackTrace();
//            FileUtilities.writeFile("parseError.xml", text);
//            FileUtilities.writeFile("parseError2.xml", new String(bytes));
//            String message = "Bad XML Parse Caused by " + e.getMessage() + "\n" + text;
//            throw new IllegalArgumentException(message, e);
//
//        }
//
//    }
//
//

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
     * cheat until we get distributed cache going
     *
     * @param fileName !b=null name of an existing file
     * @param context  ignores
     * @return !null file
     */
    public static File getDistributedFile(String fileName, final TaskInputOutputContext context) {
        return new File(fileName);
    }


    public static ISetableParameterHolder loadFromContext(final TaskInputOutputContext context) {
        final Configuration configuration = context.getConfiguration();

        return loadFromConfiguration(context, configuration);
    }

    public static ISetableParameterHolder loadFromConfiguration(final TaskInputOutputContext context, final Configuration pConfiguration) {
        ISetableParameterHolder ret = DefaultParameterHolder.getInstance();
        if (ret != null)
            return ret;

        // note we are reading from hdsf
        IStreamOpener opener = null;
        try {
            opener = new HDFSStreamOpener(pConfiguration);
            DefaultParameterHolder.addPreLoadOpener(opener);

        } catch (Exception e) {

            //noinspection ConstantConditions
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
            //   opener = new FileStreamOpener();
        }

        //noinspection UnusedDeclaration
        final String basedir = pConfiguration.get(DefaultParameterHolder.PATH_KEY);
        final String paramsFile = pConfiguration.get(DefaultParameterHolder.PARAMS_KEY);
        if (context != null) {
            if (context instanceof MapContext) {
                System.err.println("in mapper paramsFile = " + paramsFile);
            } else if (context instanceof ReduceContext) {
                System.err.println("in reducer paramsFile = " + paramsFile);

            } else {
                // Huh - who knows where we are
                System.err.println("in context " + context.getClass().getName() + " paramsFile = " + paramsFile);

            }
        }
        //     File params  =  getDistributedFile(paramsFile,context);
        InputStream is = opener.open(paramsFile);
        if (is == null)
            throw new IllegalStateException(
                    "cannot open parameters file " + ((HDFSStreamOpener) opener).buildFilePath(
                            paramsFile));
        ret = DefaultParameterHolder.getInstance(is, paramsFile, pConfiguration);


        return ret;
    }

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
            org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.setOutputPath(job, outputDir);

        } catch (FileNotFoundException e) {
            // in a local file ststem this may lead to an error so make the directory
            org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.setOutputPath(job, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


//    public static PrintWriter buildWriter(final TaskInputOutputContext context,
//                                          ISetableParameterHolder data, String added) {
//        OutputStream os = buildOutputStream(context, data, added);
//        PrintWriter ret = new PrintWriter(new OutputStreamWriter(os));
//        return ret;
//    }
//

    public static String dropExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1)
            return filename;
        return filename.substring(0, index);
    }
//
//    public static PrintWriter buildWriter(final TaskInputOutputContext context,
//                                          ISetableParameterHolder data) {
//        return buildWriter(context, data, null);
//    }

//
//    public static LineNumberReader buildreader(final TaskInputOutputContext context,
//                                               ISetableParameterHolder data) {
//        InputStream os = buildInputStream(context, data);
//
//        LineNumberReader ret = new LineNumberReader(new InputStreamReader(os));
//
//
//        return ret;
//    }
//
//    public static OutputStream buildOutputStream(TaskInputOutputContext context, String paramsFile, String added) {
//        final Configuration configuration = context.getConfiguration();
//        // note we are reading from hdsf
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//
//        if (added != null)
//            paramsFile += added;
//        String filePath = opener.buildFilePath(paramsFile);
//        DefaultParameterHolder.addPreLoadOpener(opener);
//        // note we are reading from hdsf
//        safeWrite(context, "Output File", paramsFile);
//        HDFSAccessor accesor = opener.getAccesor();
//        Path path = new Path(paramsFile);
//        OutputStream os = accesor.openFileForWrite(path);
//
//        return os;
//    }
//
//    public static PrintWriter buildPrintWriter(TaskInputOutputContext context, String paramsFile, String added) {
////        String paramsFile = buildOutputFileName(context, data);
////        if (added != null)
////            paramsFile += added;
//        OutputStream out = buildOutputStream(context, paramsFile, added);
//        PrintWriter ret = new PrintWriter(out);
//        return ret;
//    }
//


    public static Path setInputPath(final Job pJob, String pInputFile) throws IOException {
        if (pInputFile.startsWith("s3n://"))
            pInputFile = pInputFile.substring(pInputFile.lastIndexOf("s3n://"));
        System.err.println("inputFile " + pInputFile);

        Path path = new Path(pInputFile);

        org.apache.hadoop.mapreduce.lib.input.FileInputFormat.addInputPath(pJob, path);
        return path;
    }

//    public static PrintWriter buildPrintWriter(TaskInputOutputContext context, ISetableParameterHolder data) {
//        return buildPrintWriter(context, data, null);
//    }
//
//
//    public static PrintWriter buildPrintWriter(TaskInputOutputContext context, ISetableParameterHolder data, String added) {
//        OutputStream out = buildOutputStream(context, data, added);
//        PrintWriter ret = new PrintWriter(out);
//        return ret;
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
                setInputPath(pJob, arg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


//    public static String buildDebugOutputFileName(TaskInputOutputContext context,
//                                                  ISetableParameterHolder data)
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
//    public static String buildOutputFileName(TaskInputOutputContext context,
//                                             ISetableParameterHolder data) {
//        final Configuration configuration = context.getConfiguration();
//        // note we are reading from hdsf
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//
//        final String paramsFile = BiomlReporter.buildDefaultFileName(data);
//        String filePath = opener.buildFilePath(paramsFile);
//        return filePath;
//    }

//    /**
//     * return a stream representing where the data was written
//     *
//     * @param context
//     * @param data
//     * @return
//     */
//    public static InputStream buildInputStream(TaskInputOutputContext context,
//                                               ISetableParameterHolder data) {
//        final Configuration configuration = context.getConfiguration();
//        // note we are reading from hdsf
//        HDFSStreamOpener opener = new HDFSStreamOpener(configuration);
//        DefaultParameterHolder.addPreLoadOpener(opener);
//
//        final String paramsFile = BiomlReporter.buildDefaultFileName(data);
//        InputStream os = opener.open(paramsFile);
//        return os;
//    }
//

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


//    public static String buildCounterFileName(IJobRunner runner, Configuration pConf) {
//        String dir = pConf.get(DefaultParameterHolder.PATH_KEY);
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
            Set<String> strings = counters.keySet();
            String[] items = strings.toArray(new String[strings.size()]);
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


    /**
     * in the file system delete all files om the directory with filename as its path
     * which end in tmp
     *
     * @param conf !null
     */
    public static void deleteTmpFiles(Path p, Configuration conf) {

        if (p == null) {
            throw new IllegalArgumentException("Tmp file path cannot be null");
        }

        try {
//            String jobPath = conf.get(XTandemHadoopUtilities.PATH_KEY);
//            Path p = new Path(jobPath);
            FileSystem fs = FileSystem.get(conf);
            FileStatus[] statuses = fs.listStatus(p);
            if (statuses == null)
                return; // no files
            for (int i = 0; i < statuses.length; i++) {
                FileStatus statuse = statuses[i];
                if (statuse.isDir())
                    continue; // we only care about files
                Path testPath = statuse.getPath();
                String s = testPath.getName();
                if (s.endsWith(".tmp")) {
                    fs.delete(testPath, false);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

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
     * parse an xml file using a specific handler
     *
     * @param is !null stream
     * @return !null key value set
     */
    public static Map<String, String> readNotes(String str) {

        final InputStream is = Util.getDescribedStream(str);
        return readNotes(is, str);
    }

    /**
     * parse a bioml file holding nothing but note tags
     *
     * @param is !null stream
     * @return !null key value set
     */
    public static Map<String, String> readNotes(InputStream is, String url) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        DelegatingSaxHandler handler = new DelegatingSaxHandler();
//        final BiomlHandler handler1 = new BiomlHandler(handler, url);
//        handler.pushCurrentHandler(handler1);
//        handler.parseDocument(is);
//
//        if (handler1 instanceof AbstractXTandemElementSaxHandler) {
//            AbstractXTandemElementSaxHandler<AbstractXTandemElementSaxHandler> handlerx = handler1;
//            Map<String, String> notes = handlerx.getNotes();
//          return notes;
//
//        }
//        throw new UnsupportedOperationException("Fix This"); // ToDo
//
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
//
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


    /**
     * add a define top a list of arguments
     *
     * @param key   key
     * @param value value
     * @param args  old argument set
     * @return new argument set
     */
    public static
    @Nonnull
    String[] addDefine(@Nonnull String key, @Nonnull String value, @Nonnull String[] args) {
        String[] ret = new String[args.length + 2];
        ret[0] = "-D";
        ret[1] = key + "=" + value;
        for (int i = 0; i < args.length; i++) {
            ret[i + 2] = args[i];

        }
        return ret;
    }




    public static String buildCounterFileName(String fileName, Configuration pConf) {
        String dir = pConf.get(DefaultParameterHolder.PATH_KEY);
        if (dir == null)
            dir = "";
        else
            dir += "/";
        return dir + fileName;
    }


    public static final String BIN_COUNTER_START = "Binning:MZ";

    /**
     * return a map of mz vs count based on the results of pass1
     *
     * @param fileSystem !null file system
     * @param fileName   pat to the file as a string
     * @return
     */
    public static Map<Integer, Integer> readBinCounters(FileSystem fileSystem, String fileName) {
        Path p = new Path(fileName);
        Map ret = new HashMap<Integer, Integer>();
        try {
            FSDataInputStream open = fileSystem.open(p);
            LineNumberReader rddr = new LineNumberReader(new InputStreamReader(open));
            String line = rddr.readLine();
            while (line != null) {
                if (line.startsWith(BIN_COUNTER_START)) {
                    line = line.substring(BIN_COUNTER_START.length());
                    String[] items = line.split("=");
                    int mz = Integer.parseInt(items[0].trim());
                    int count = Integer.parseInt(items[1].trim());
                    ret.put(mz, count);
                }
                line = rddr.readLine();
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
        return ret;
    }


}
