package com.lordjoe.utilities;

import org.systemsbiology.remotecontrol.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.utilities.Deployer
 *
 * @author Steve Lewis
 * @date Apr 3, 2007
 */
public class Deployer {
    public Deployer[] EMPTY_ARRAY = {};
    public Class<Deployer> THIS_CLASS = Deployer.class;

    @SuppressWarnings("UnusedDeclaration")
    public final String WINDOWS_DIRECTORY_SEPARATOR = "\\";
    @SuppressWarnings("UnusedDeclaration")
    public final String LINUX_DIRECTORY_SEPARATOR = "/";
    public final String WINDOWS_PATH_SEPARATOR = ";";
    public final String LINUX_PATH_SEPARATOR = ":";

    public final String[] EEXCLUDED_JARS_LIST = {
            "idea_rt.jar",
            "javaws.jar",
            "jce.jar",
            "management-agent.jar",
            "alt-rt.jar",
            "charsets.jar",
            "classes.jar",
            "jconsole.jar",
            "jsse.jar",
            "laf.jar",
            "ui.jar",
            "testng-5.5-jdk15.jar",
            "junit-dep-4.8.1.jar",
            "dt",
            "junit-4.8.1.jar",
            "openjpa-persistence-2.0.0.jar",
            "openjpa-kernel-2.0.0.jar",
            "openjpa-lib-2.0.0.jar",
            //       "commons-logging-1.1.1.jar",
            //         "commons-lang-2.1.jar",
            //       "commons-collections-3.2.1.jar",
            "serp-1.13.1.jar",
            "geronimo-jms_1.1_spec-1.1.1.jar",
            "geronimo-jta_1.1_spec-1.1.1.jar",
            //        "commons-pool-1.3.jar",
            "geronimo-jpa_2.0_spec-1.0.jar",
            "mysql-connector-java-5.0.4.jar",
            //         "commons-dbcp-1.2.2.jar",
            //          "commons-cli-1.2.jar",
            //         "jsch-0.1.44-1.jar",
            //         "hadoop-core-0.20.2.jar",
            //        "xmlenc-0.52.jar",
            //         "commons-httpclient-3.0.1.jar",
            //         "commons-codec-1.3.jar",
            //         "commons-net-1.4.1.jar",
            "oro-2.0.8.jar",
            "jetty-6.1.25.jar",
            "jetty-util-6.1.14.jar",
            "servlet-api-2.5-6.1.14.jar",
            "jasper-runtime-5.5.12.jar",
            "jasper-compiler-5.5.12.jar",
            "jsp-api-2.1-6.1.14.jar",
            "jsp-2.1-6.1.14.jar",
            //       "core-3.1.1.jar",
            "ant-1.6.5.jar",
            //         "commons-el-1.0.jar",
            "jets3t-0.7.1.jar",
            "kfs-0.3.jar",
            "hsqldb-1.8.0.10.jar",
            //     "servlet-api-2.5-20081211.jar",
         //   "slf4j-log4j12-1.4.3.jar",
         //   "slf4j-api-1.4.3.jar",
        //    "log4j-1.2.9.jar",
            //       "xml-apis-1.0.b2.jar",
            //       "xml-apis-ext-1.3.04.jar",
            "spring-jdbc-2.5.6.jar",
            "spring-beans-2.5.6.jar",
            "spring-core-2.5.6.jar",
            "spring-context-2.5.6.jar",
            "aopalliance-1.0.jar",
            "spring-tx-2.5.6.jar",

    };


    public final Set<String> EXCLUDED_JARS = new HashSet<String>(Arrays.asList(EEXCLUDED_JARS_LIST));

    @SuppressWarnings("unchecked")
    private final Set<String> m_TaskExcludeJar = new HashSet<String>();

    public Deployer() {
    }



    public void clearTaskExcludeJars() {
        m_TaskExcludeJar.clear();
    }

    public void addTaskExcludeJar(String s) {
        m_TaskExcludeJar.add(s);
    }

    private boolean m_Quiet;

    public boolean isQuiet() {
        return m_Quiet;
    }

    public void setQuiet(final boolean pQuiet) {
        m_Quiet = pQuiet;
    }

    public File[] filterClassPath(String[] pathItems, String javaHome, File libDir) {

        List<File> holder = new ArrayList<File>();
        Set<File> directoryHolder = new HashSet<File> ();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < pathItems.length; i++) {
            String item = pathItems[i];
            if (".".equals(item))
                continue;
            if (EXCLUDED_JARS.contains(item))
                continue;
            if (item.contains(javaHome))
                continue;
            File itemFile = new File(item);

            if (!itemFile.exists())
                continue;
            if (itemFile.isFile()) {
                continue;
            }
            if (itemFile.isDirectory()) {
                directoryHolder.add(itemFile);
            }
          }

        if (!directoryHolder.isEmpty()) {
            makeJars(libDir,directoryHolder,holder);

        }

        String test = "a,ffff,ggg";
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        String[] items = test.split(",");

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < pathItems.length; i++) {
            String item = pathItems[i];
            if (".".equals(item))
                continue;
            if (inExcludedJars(item))
                continue;
            if (item.indexOf(javaHome) > -1)
                continue;
            File itemFile = new File(item);
            if (m_TaskExcludeJar.contains(itemFile.getName()))
                continue;
            if (!itemFile.exists())
                continue;
            if (itemFile.isFile()) {
                System.out.println("\"" + itemFile.getName() + "\",");
                if (!holder.contains(itemFile))
                    holder.add(itemFile);
                else
                    System.out.println("Block duplicate jar " + itemFile);
                continue;
            }
            if (itemFile.isDirectory()) {
                continue;
            }

        }
        File[] ret = new File[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    protected boolean inExcludedJars(String s) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < EEXCLUDED_JARS_LIST.length; i++) {
            String test = EEXCLUDED_JARS_LIST[i];
            if (s.endsWith(test))
                return true;
        }
        return false;
    }

    public String pathToJarName(File itemFile) {
        String test = itemFile.getName();
        if ("classes".equalsIgnoreCase(test)) {
            test = itemFile.getParentFile().getName();
        }
        if(!test.endsWith(".jar"))
            test += ".jar";
        return test;
    }

//    public File makeencryptJars(File libDir,  itemFile) {
//          String jarName = pathToJarName(itemFile);
//          File jarFile = new File(libDir, jarName);
//          String cmd = "jar -cvf " + jarFile.getAbsolutePath() + " -C " + itemFile.getAbsolutePath() + " .";
//          System.out.println(cmd);
//          try {
//              Runtime.getRuntime().exec(cmd);
//          }
//          catch (IOException e) {
//              throw new RuntimeException(e);
//          }
//          return jarFile;
//      }

    protected void makeJars(File libDir, Collection<File> jarDirectories, Collection<File> holder) {
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        Set<String>  usedClassName = new HashSet<String>();
        for (File jarDirectory : jarDirectories) {
            String name = jarDirectory.getName();
      //      File jar = new File(name + ".jar");
            File added = makeJar(libDir,jarDirectory , name + ".jar");
            holder.add(added);
        }
    }


    /**
     * jar all directories into one big jar called Target.jar
     *
     * @param jarDirectories
     * @param holder
     * @param jarFile        file to create
     */
    protected void makeOneJar(List<File> jarDirectories, List<File> holder, File jarFile ) {
        JarUtilities jarUtilities = new JarUtilities(jarFile);
        jarUtilities.jarDirectories(jarFile , jarDirectories.toArray(new File[jarDirectories.size()]));


        holder.add(jarFile);
    }

    public final File makeJar(File libDir, File itemFile ,String jarName) {
          File jarFile = new File(libDir, jarName);
        String cmd = "jar -cvf " + jarFile.getAbsolutePath() + " -C " + itemFile.getAbsolutePath() + " .";
        System.out.println(cmd);
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jarFile;
    }

    public void copyLibraries(File libDir, File[] libs) {
        for (int i = 0; i < libs.length; i++) {
            File lib = libs[i];
            if (libDir.equals(lib.getParentFile()))
                continue;
            File dst = new File(libDir, lib.getName());
            copyFile(lib, dst);
            System.out.println("" + dst.getName());
        }
    }

    /**
     * { method
     *
     * @param dst destination file name
     * @param src source file name
     * @return true for success
     *         }
     * @name copyFile
     * @function copy file named src into new file named dst
     */
    public boolean copyFile(File src, File dst) {
        int bufsize = 1024;
        try {
            RandomAccessFile srcFile = new RandomAccessFile(src, "r");
            long len = srcFile.length();
            if (len > 0x7fffffff) {
                return (false);
            }
            // too large
            int l = (int) len;
            if (l == 0) {
                return (false);
            }
            // failure - no data
            RandomAccessFile dstFile = new RandomAccessFile(dst, "rw");

            int bytesRead = 0;
            byte[] buffer = new byte[bufsize];
            while ((bytesRead = srcFile.read(buffer, 0, bufsize)) != -1) {
                dstFile.write(buffer, 0, bytesRead);
            }
            srcFile.close();
            dstFile.close();
            return true;
        } catch (IOException ex) {
            return (false);
        }
    }


    /**
     * { method
     *
     * @param TheFile name of file to create
     * @param data    date to write
     * @return true = success
     *         }
     * @name writeFile
     * @function write the string data to the file Filename
     */
    public boolean writeFile(File TheFile, String data) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(TheFile));
            if (out != null) {
                out.print(data);
                out.close();
                return (true);
            }
            return (false);
            // failure
        } catch (IOException ex) {
            return (false); // browser disallows
        } catch (SecurityException ex) {
            return (false); // browser disallows
        }
    }


    public File[] deployLibraries(File deployDir) {
        String javaHome = System.getProperty("java.home");
        String classpath = System.getProperty("java.class.path");
        String[] pathItems;
        if (classpath.contains(";")) {
            pathItems = classpath.split(";");
        } else {
            if (classpath.contains(":")) {
                pathItems = classpath.split(":");   // Linux stlye
            } else {
                String[] items = {classpath};
                pathItems = items; // only 1 I guess
            }
        }
        File libDir = new File(deployDir, "lib");
        File[] pathLibs = filterClassPath(pathItems, javaHome, libDir);
        //noinspection ResultOfMethodCallIgnored
        libDir.mkdirs();
        copyLibraries(libDir, pathLibs);
        return pathLibs;
    }

    public String buildBatchFile(File[] pathLibs, File deployDir, final Class mainClass, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("ECHO OFF\n");
        sb.append("set JAR_PATH=%HYDRA_HOME%/lib\n");
        sb.append("set q4path=%JAR_PATH%/" + pathLibs[0].getName() + "\n");
        for (int i = 1; i < pathLibs.length; i++) {
            File athLib = pathLibs[i];
            //noinspection StringConcatenationInsideStringBuilderAppend
            sb.append("set q4path=%q4path%" + WINDOWS_PATH_SEPARATOR + "%JAR_PATH%/" + athLib.getName() + "\n");
        }
        sb.append("ECHO ON\n");
        buildCommandLine(mainClass, args, sb);
        return sb.toString();

    }

    protected void buildCommandLine(final Class mainClass, final String[] args, final StringBuilder pSb) {
        if (isQuiet()) {
            pSb.append(/* "jre" + WINDOWS_DIRECTORY_SEPARATOR + "bin" + WINDOWS_DIRECTORY_SEPARATOR + */ "javaw ");
        } else {
            pSb.append(/* "jre\\bin\\" + */ "java ");
        }
        //noinspection StringConcatenationInsideStringBuilderAppend
        pSb.append(" -Xmx1024m -Xms128m -cp %q4path% " + mainClass.getName() + " ");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 2; i < args.length; i++) {
            //noinspection StringConcatenationInsideStringBuilderAppend
            pSb.append(" " + args[i]);
        }
        pSb.append(" params=%1 %2 %3 %4 \n");
    }

    public static final String SHELL_FILE_HEADER =
            "#!/bin/sh\n" +
                    "\n" +
                    "#If Java is not in the default path add a line like the following\n" +
                    "#export PATH=/hpc/java/jdk1.6.0_24/bin:$PATH\n" +
                    "#\n" +
                    "\n" +
                    "# add a line like the following to define HYDRA_HOME where HYDRA_HOME if the directory \n" +
                    "# where the code is installed - it will have bin,lib and data subdirectories \n" +
                    "#export HYDRA_HOME=/users/slewis/Hydra\n" +
                    "\n";

    public static final String LIST_JAVA_VERSION =
            "echo \"Using Java: \"\n" +
                    "which java\n" +
                    "java -version\n" +
                    "\n" +
                    "echo \"java needs to be version 1.6 or greater\"\n" +
                    "\n";

    public String buildShellFile(File[] pathLibs, File deployDir, final Class mainClass, String[] args) {

        StringBuilder sb = new StringBuilder();

        sb.append(SHELL_FILE_HEADER);

        sb.append("JAR_PATH=$HYDRA_HOME/lib\n");
        sb.append("q4path=$JAR_PATH/" + pathLibs[0].getName() + "\n");
        for (int i = 1; i < pathLibs.length; i++) {
            File athLib = pathLibs[i];
            //noinspection StringConcatenationInsideStringBuilderAppend
            sb.append("q4path=$q4path" + LINUX_PATH_SEPARATOR + "$JAR_PATH/" + athLib.getName() + "\n");
        }
        sb.append(SHELL_FILE_HEADER);
        String shl = buildShellCommandLine(mainClass, args);
        sb.append(LIST_JAVA_VERSION);
        sb.append("echo executing\"" + shl + "\"\n");

        sb.append(shl);
        return sb.toString();

    }

    protected String buildShellCommandLine(final Class mainClass, final String[] args) {
        StringBuilder pSb = new StringBuilder();

        pSb.append("java ");
        pSb.append(" -Xmx400m -Xms64m ");
        pSb.append(" -cp $q4path " + mainClass.getName() + " ");
        for (int i = 2; i < args.length; i++) {
            //noinspection StringConcatenationInsideStringBuilderAppend
            pSb.append(" " + args[i].replace('%', '$'));
        }
        pSb.append(" $1 $2 $3 $4 $5 $6 $7 $8\n");
        return pSb.toString();
        //  config=%HYDRA_HOME%/Launcher.properties config=%HYDRA_HOME%/data/JXTandem.jar
    }

    public String[] makeRunners(File[] pathLibs, File deployDir, final Class mainClass, final String commandName, String[] args) {
        File binDir = new File(deployDir, "bin");
        binDir.mkdirs();
        String bf = buildBatchFile(pathLibs, deployDir, mainClass, args);
        File rb = new File(binDir, commandName + ".bat");
        writeFile(rb, bf);
        String sf = buildShellFile(pathLibs, deployDir, mainClass, args);
        File rs = new File(binDir, commandName + ".sh");
        writeFile(rs, sf);
        String[] ret = {bf, sf};
        return ret;

    }


    public void deploy(final File pDeployDir, final Class mainClass, final String[] pRightArgs) {
        String deployPath = pDeployDir.getAbsolutePath();
        //noinspection ResultOfMethodCallIgnored
        pDeployDir.mkdirs();
        File[] pathLibs = deployLibraries(pDeployDir);
        makeRunners(pathLibs, pDeployDir, mainClass, "hydra", pRightArgs);
        // add configure code
        makeRunners(pathLibs, pDeployDir, RemoteUtilities.class, "configure", pRightArgs);
    }

    public static void main(String[] args) throws Exception {
        //   testRegex();
        String[] rightArgs;
        String[] otherArgs = new String[0];
        Deployer d = new Deployer();
        if ("-q".equals(args[0])) {
            d.setQuiet(true);
            rightArgs = new String[args.length - 1];
            System.arraycopy(args, 1, rightArgs, 0, args.length - 1);
        } else {
            rightArgs = args;
        }
        if (rightArgs.length > 0) {
            otherArgs = new String[rightArgs.length - 1];
            System.arraycopy(rightArgs, 1, otherArgs, 0, rightArgs.length - 1);
        }

        File deployDir = new File(args[0]);
        Class mainClass = Class.forName(args[1]);
        d.deploy(deployDir, mainClass, otherArgs);

    }
}