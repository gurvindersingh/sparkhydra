package com.lordjoe.distributed.spark;

///**
// * com.lordjoe.distributed.spark.SparkJob
// * User: steven
// * Date: Jun 7, 2010
// */
//public class SparkJob implements IHadoopJob {
//
//    public static final String SPARK_COMMMAND = "";
//    private static String gSparkCommand = SPARK_COMMMAND;
//
//      private static String gDefaultJarName;
//
//    public static String getDefaultJarName() {
//        return gDefaultJarName;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public static void setDefaultJarName(final String pDefaultJarName) {
//        gDefaultJarName = pDefaultJarName;
//        setJarRequired(pDefaultJarName == null);
//    }
//
//    /**
//     * for local jobs we do not need a jar
//     */
//    private static boolean gJarRequired = true;
//
//    public static boolean isJarRequired() {
//        return gJarRequired;
//    }
//
//    public static void setJarRequired(final boolean pJarRequired) {
//        gJarRequired = pJarRequired;
//    }
//
//    public String makeJarName() {
//        String name = new SimpleDateFormat("MMMddHHmm").format(new Date());
//        return name + "_" + getStepNumber() + ".jar";
//    }
//
//    public static SparkJob buildJob(Class  mainClass, String inputDirectory, String jobDirectory, String outDir, String... added) {
//        return buildJob2(mainClass, inputDirectory, jobDirectory, outDir,   added);
//    }
//
//    public static SparkJob buildJob2(Class  mainClass, String inputDirectory, String jobDirectory, String outDir, String[] added) {
//        SparkJob ret = new SparkJob();
//        ret.setMainClass(mainClass.getName());
//        ret.setJobDirectory(jobDirectory);
//
//        if(getDefaultJarName() != null)
//          ret.setJarFile("jobs" + "/" + getDefaultJarName());
//        if (isJarRequired()) {
//            String jarName = jobDirectory + "/" + ret.makeJarName();
//            HadoopDeployer.makeHadoopJar(jarName);
//            ret.setJarFile(jarName);
//        }
//
//        //     ret.setFilesDirectory(filesDirectory);
//        ret.setHDFSFilesDirectory(inputDirectory);
//        ret.setOutputDirectory(outDir);
//        ret.setOtherArgs(added);
//        return ret;
//    }
//
//    public static SparkJob buildJob3(Class  mainClass, String inputDirectory, String jobDirectory, String outDir, String jar, String... added) {
//        SparkJob ret = new SparkJob();
//        ret.setMainClass(mainClass.getName());
//        ret.setJarFile(jar);
//        ret.setHDFSFilesDirectory(inputDirectory);
//        ret.setJobDirectory(jobDirectory);
//        ret.setOutputDirectory(outDir);
//        ret.setOtherArgs(added);
//        return ret;
//    }
//
//    private UUID m_UID = UUID.randomUUID();
//    private String m_JarFile;
//    private String m_MainClass;
//    private String m_SourceFiles;
//    private String m_JobDirectory;
//    private String m_HDFSFilesDirectory;
//    private String m_OutputDirectory;
//    private String m_EmptyOutputDirectory = "";
//    private String[] m_OtherArgs;
//    private int m_NumberInstances;
//    private String m_DefaultDirectory;
//    private int m_StepNumber;
//    private Job m_Job; // actual Hadop Job
//    private Map<String, Long> m_AllCounterValues; // actual Hadop Job
//
//    @Override
//    public int getStepNumber() {
//        return m_StepNumber;
//    }
//
//    @Override
//    public synchronized void incrementStepNumber() {
//        m_StepNumber++;
//    }
//
//    @Override
//    public Job getJob() {
//        return m_Job;
//    }
//
//    @Override
//    public String getName() {
//        String ret = getMainClass();
//        int li = ret.lastIndexOf(".");
//        if(li > -1)
//            ret = ret.substring(li + 1);
//        return ret;
//    }
//
//    @Override
//    public UUID getUID() {
//        return m_UID;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public void setUID(final UUID pUID) {
//        m_UID = pUID;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public static String getSparkCommand() {
//        return gSparkCommand;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public static void setSparkCommand(final String pSparkCommand) {
//        gSparkCommand = pSparkCommand;
//    }
//
//    public Map<String, Long> getAllCounterValues() {
//        if(m_AllCounterValues == null)  {
//            m_AllCounterValues = new HashMap<String, Long>();
//            getAllCounters();
//        }
//        return m_AllCounterValues;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public void setAllCounterValues(final Map<String, Long> pAllCounterValues) {
//        m_AllCounterValues = pAllCounterValues;
//    }
//
//    public void getAllCounters() {
//
//        try {
//            Job job = getJob();
//            if(job != null) {
//                Counters counters = job.getCounters();
//                Iterator<CounterGroup> iterator = counters.iterator();
//                //noinspection WhileLoopReplaceableByForEach
//                while (iterator.hasNext()) {
//                    CounterGroup cg = iterator.next();
//                    Iterator<Counter> iterator1 = cg.iterator();
//                    //noinspection WhileLoopReplaceableByForEach
//                    while (iterator1.hasNext()) {
//                        Counter counter = iterator1.next();
//                        m_AllCounterValues.put(counter.getDisplayName(), counter.getValue());
//                    }
//                }
//
//            }
//
//         }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }
//
//    }
//
//    /**
//     * get all counters in a specific group
//     *
//     * @param group group name
//     * @return
//     */
//    @SuppressWarnings("UnusedDeclaration")
//      public Counter[] getAllCountersInGroup(String group) {
//        List<Counter> holder = new ArrayList<Counter>();
//
//        try {
//            Job job = getJob();
//            Counters counters = job.getCounters();
//            Iterator<CounterGroup> iterator = counters.iterator();
//            //noinspection WhileLoopReplaceableByForEach
//            while (iterator.hasNext()) {
//                CounterGroup cg = iterator.next();
//                if (group.equals(cg.getName())) {
//                    Iterator<Counter> iterator1 = cg.iterator();
//                    //noinspection WhileLoopReplaceableByForEach
//                    while (iterator1.hasNext()) {
//                        Counter counter = iterator1.next();
//                        holder.add(counter);
//                    }
//                }
//            }
//
//            Counter[] ret = new Counter[holder.size()];
//            holder.toArray(ret);
//            return ret;
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//
//        }
//
//    }
//
//
//    public void setJob(final Job pJob) {
//        m_Job = pJob;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public String getDefaultDirectory() {
//        return m_DefaultDirectory;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public void setDefaultDirectory(final String pDefaultDirectory) {
//        m_DefaultDirectory = pDefaultDirectory;
//    }
//
//    public int getNumberInstances() {
//        return m_NumberInstances;
//    }
//
//    public void setNumberInstances(final int pNumberInstances) {
//        m_NumberInstances = pNumberInstances;
//    }
//
//    @Override
//    public String getJarFile() {
//        return m_JarFile;
//    }
//
//    @Override
//    public void setJarFile(final String pJarFile) {
//        m_JarFile = pJarFile;
//    }
//
//    @Override
//    public String getMainClass() {
//        return m_MainClass;
//    }
//
//    @Override
//    public void setMainClass(final String pMainClass) {
//        m_MainClass = pMainClass;
//    }
//
//    @Override
//    public String getSourceFiles() {
//        return m_SourceFiles;
//    }
//
//    @Override
//    public void setSourceFiles(final String pSourceFiles) {
//        m_SourceFiles = pSourceFiles;
//    }
//
//    @Override
//    public String getOutputDirectory() {
//        return m_OutputDirectory;
//    }
//
//    @Override
//    public void setOutputDirectory(final String pOutputDirectory) {
//        m_OutputDirectory = pOutputDirectory;
//    }
//
//    @Override
//    public String getJobDirectory() {
//        return m_JobDirectory;
//    }
//
//    @Override
//    public void setJobDirectory(final String pJobDirectory) {
//        m_JobDirectory = pJobDirectory;
//    }
//
////    public String getFilesDirectory() {
////        return m_FilesDirectory;
////    }
////
////    public void setFilesDirectory(final String pFilesDirectory) {
////        m_FilesDirectory = pFilesDirectory;
////    }
////
//
//    @Override
//    public String getHDFSFilesDirectory() {
//        return m_HDFSFilesDirectory;
//    }
//
//    @Override
//    public void setHDFSFilesDirectory(final String pHDFSFilesDirectory) {
//        m_HDFSFilesDirectory = pHDFSFilesDirectory;
//    }
//
//    @Override
//    public String getEmptyOutputDirectory() {
//        return m_EmptyOutputDirectory;
//    }
//
//    @Override
//    public void setEmptyOutputDirectory(final String pEmptyOutputDirectory) {
//        m_EmptyOutputDirectory = pEmptyOutputDirectory;
//    }
//
//    @Override
//    public String[] getOtherArgs() {
//        return m_OtherArgs;
//    }
//
//    @Override
//    public String[] getAllArgs() {
//        List<String> holder = new ArrayList<String>(Arrays.asList(getOtherArgs()));
//
//        String hdfsFilesDirectory = getHDFSFilesDirectory();
//        if (hdfsFilesDirectory != null)
//            holder.add(hdfsFilesDirectory);
//
//        String emptyOutputDirectory = getOutputDirectory();
//        holder.add(emptyOutputDirectory);
//
//        String[] ret = new String[holder.size()];
//        holder.toArray(ret);
//        return ret;
//    }
//
//    @Override
//    public void setOtherArgs(final String[] pOtherArgs) {
//        m_OtherArgs = new String[pOtherArgs.length];
//        System.arraycopy(pOtherArgs, 0, m_OtherArgs, 0, pOtherArgs.length);
//    }
//
//    public void makeJarAsNeeded() {
//        if (getJarFile() != null)
//            return;
//        String jarName = makeJarName();
//        if (isJarRequired()) {
//            HadoopDeployer.makeHadoopJar(jarName);
//            setJarFile(jarName);
//        }
//
//    }
//
//    /**
//     * build command string to make all files in directory public
//     * as this is not the default
//     * @return
//     */
//    @Override
//    public String buildChmodCommandString() {
//        makeJarAsNeeded();
//        StringBuilder sb = new StringBuilder();
//        sb.append(getSparkCommand());
//          sb.append(" fs -chmod -R 777 " );
//
//        //noinspection UnnecessaryLocalVariable
//        String s = sb.toString();
//        return s;
//    }
//
//    @Override
//    public String buildCommandString() {
//        makeJarAsNeeded();
//        StringBuilder sb = new StringBuilder();
//        final String hadoopCommend = getSparkCommand();
//        sb.append(hadoopCommend);
//
//
//        String jarFile = getJarFile();
//        jarFile = jarFile.replace("res://", ""); // might be a resource
//        //noinspection StringConcatenationInsideStringBufferAppend
//        sb.append(" jar " + jarFile);
//        //noinspection StringConcatenationInsideStringBufferAppend
//        sb.append("  " + getMainClass());
//
//        // add a dummy argument
//       // sb.append(" -D org.systemsbiology.status=foobar ");
//
//        String[] others = getAllArgs();
//        if (others != null) {
//            //noinspection ForLoopReplaceableByForEach
//            for (int i = 0; i < others.length; i++) {
//                sb.append(" " + others[i]);
//            }
//        }
//               // todo replace this Hack
//
//        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
//         String s = sb.toString();
//        return s;
//    }
//}
