package org.systemsbiology.remotecontrol;

import com.lordjoe.utilities.*;
import org.apache.log4j.lf5.util.*;
import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

/**
 * org.systemsbiology.remotecontrol.RemoteUtilities
 * User: steven
 * Date: Jun 4, 2010
 */
public class RemoteUtilities {
    public static final RemoteUtilities[] EMPTY_ARRAY = {};

    public static final boolean USE_1_0_3 = true;

    public static final String EDIT_REMOTE_PRPOERTIES =
            "You will need to configure asccess properties java  org.systemsbiology.remotecontrol.RemoteUtilities user=<user_name> password=<password> path=<hdfs_path> \n" +
                    " - user should be the name of the user on the hadoop main system \n" +
                    " - password should be the password of the user on the hadoop main system \n" +
                    " - host should be the name of the  hadoop main system  " +
                    " - path should be the name of the a path on hdfs  " +
                    " - port should be the port of  hdfs - 9000 is the default " +
                    " - hadoop_home should be the value of what you get when you say echo $HADOOP_HOME  " +
                    "";


    public static final String USER_STRING = "user";
    public static final String PASSWORD_STRING = "password";
    public static final String HOST_STRING = "host";
    public static final String JOB_TRACKER_STRING = "jobtracker";
    public static final String PORT_STRING = "port";
    public static final String HADOOP_HOME = "hadoop_home";
    public static final String DEFAULT_PATH_STRING = "default_path";

    //   private static Properties gAccessProperties;
    private static boolean gResourcePropertiesRead = false;

    public static void readResourceProperties() {
        if (gResourcePropertiesRead)
            return;
        gResourcePropertiesRead = true; // only do this once
        InputStream is = RemoteUtilities.class.getResourceAsStream("ResourceSettings.properties");
        if (is == null) {
            try {
                is = new FileInputStream("RemoteSettings.properties");
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find ResourceSettings.properties");
                return;
            }
        }
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            System.err.println("Cannot load ResourceSettings.properties");
            return;
        }
        String prop;
        prop = props.getProperty("user");
        if (prop != null)
            setUser(prop.trim());
        prop = props.getProperty("host");
        if (prop != null)
            setHost(prop.trim());
        prop = props.getProperty("jobtracker");
        if (prop != null)
            setJobTracker(prop.trim());
        prop = props.getProperty("port");
        if (prop != null)
            setPort(Integer.parseInt(prop.trim()));
        prop = props.getProperty("path");
        if (prop != null)
            setDefaultPath(prop.trim());
        prop = props.getProperty("encryptedRemotePassword");
        if (prop != null)
            setPassword(Encrypt.decryptString(prop.trim()));
    }




    private static void validateProperties(final Properties pP) {
        if (getProperty(USER_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(PASSWORD_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(HOST_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(DEFAULT_PATH_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(PORT_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
    }


    private static void validatePreferences() {
        if (getProperty(USER_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(PASSWORD_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(HOST_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(DEFAULT_PATH_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
        if (getProperty(PORT_STRING) == null)
            throw new IllegalStateException(EDIT_REMOTE_PRPOERTIES);
    }


    public static void setProperty(String name, String value) {
        Preferences prefs = Preferences.userNodeForPackage(RemoteUtilities.class);
        if (PASSWORD_STRING.equals(name))
            value = Encrypt.encryptString(value);
        prefs.put(name, value);

    }

    public static String getProperty(String name) {
        Preferences prefs = Preferences.userNodeForPackage(RemoteUtilities.class);
        String BadValue = "Set " + name;
        String s = prefs.get(name, BadValue);
        if (BadValue.equals(s))
            return null;
        return s;
    }

private static String g_User;

    public static String getUser() {
        if (g_User == null)
            g_User = getProperty(USER_STRING);
        return g_User;
    }

    public static void setUser(String user) {
        g_User = user;
    }

public static final File KEYTAB_DIR = new File("/keytabs");

    /**
     * look for a keytab file with Kerboros keys
     * for the user may be local ot in .keytabs
     * see http://kb.iu.edu/data/aumh.html
     *
     * @return possibly null file
     */
    public static File getKeyTabFile() {
        String fileName = getUser() + ".keytab";
        File local = new File(fileName);
        if (local.exists())
            return local;
        if (KEYTAB_DIR.exists() && KEYTAB_DIR.isDirectory()) {
            local = new File(KEYTAB_DIR, fileName);
            if (local.exists())
                return local;
        }
        return null;
    }

private static String g_Password;

    public static String getPassword() {
        if (g_Password == null) {
            String encrypted = getProperty(PASSWORD_STRING);
            if (encrypted != null)
                g_Password = Encrypt.decryptString(encrypted);
        }
        return g_Password;
    }

    public static void setPassword(String password) {
        g_Password = password;
    }

private static String g_Host;

    public static String getHost() {
        if (g_Host == null)
            g_Host = getProperty(HOST_STRING);
        return g_Host;
    }

    public static void setHost(String host) {
        g_Host = host;
    }

private static String g_JobTracker;

    public static String getJobTracker() {
        if (g_JobTracker == null)
            g_JobTracker = getProperty(JOB_TRACKER_STRING);
        return g_JobTracker;
    }


    public static void setJobTracker(String jobTracker) {
        g_JobTracker = jobTracker;
    }


private static Integer g_Port;

    public static int getPort() {
        if (g_Port == null) {
            final String property = getProperty(PORT_STRING);
            g_Port = Integer.parseInt(property);
        }
        return g_Port;
//          if (USE_1_0_3) {
//            // for the momeent hard code for 1.03 system
//            port = 8020;
    }

    public static void setPort(int port) {
        g_Port = port;
    }

private static String g_DefaultPath;

    public static String getDefaultPath() {
        if (g_DefaultPath == null)
            g_DefaultPath = getProperty(DEFAULT_PATH_STRING);
        return g_DefaultPath;
    }

    public static void setDefaultPath(String defaultPath) {
        String oldPath = getDefaultPath();
        g_DefaultPath = defaultPath;
    }

public static final String[] EEXCLUDED_JARS_LIST = {
        "idea_rt.jar",
        "javaws.jar",
        "jce.jar",
        "hadoop-0.20.1+152-mrunit",
        "management-agent.jar",
        "alt-rt.jar",
        "charsets.jar",
        "classes.jar",
        "jconsole.jar",
        "slf4j-api-1.4.3.jar",
        "slf4j-log4j12-1.4.3.jar",
        "jsse.jar",
        "laf.jar",
        "ui.jar",
        "testng-5.5-jdk15.jar",
        "junit-dep-4.8.1.jar",
        "hadoop-0.20.2-core.jar",
        "hadoop-0.20.1+152-mrunit.jar",
        "hadoop-0.20.2-tools.jar",
        "hadoop-core-0.20.1.jar",
        "commons-logging-1.1.1.jar",
        "commons-cli-1.2.jar",
        //     "commons-logging-1.1.1.jar",
        "slf4j-log4j12-1.4.3.jar",
        "log4j-1.2.15.jar",
        //  "xmlenc-0.52.jar",           //
        //    "commons-cli-1.2.jar",          //
        //    "commons-logging-api-1.0.4.jar",
        //    "commons-httpclient-3.0.1.jar",    //
        //    "commons-net-1.4.1.jar",           //
        //    "slf4j-api-1.4.3.jar",
        "karmasphere-client.jar",
        "servlet-api-2.5-6.1.14.jar",
        //    "commons-codec-1.3.jar",     //
        "commons-el-1.0.jar",
        // //   "commons-io-1.4.jar",
        //    "aws-java-sdk-1.0.005.jar",    // leave off
        "junit-4.4.jar",
        "junit-4.8.1.jar"
};

public static final Set<String> EXCLUDED_JARS = new HashSet(Arrays.asList(EEXCLUDED_JARS_LIST));

private int gJarNumber = 0;
private static boolean isQuiet;

    public static File[] filterClassPath(String[] pathItems, String javaHome) {
        List holder = new ArrayList();
        for (int i = 0; i < pathItems.length; i++) {
            String item = pathItems[i];
            if (".".equals(item))
                continue;
            if (EXCLUDED_JARS.contains(item))
                continue;
            if (item.indexOf(javaHome) > -1)
                continue;
            File itemFile = new File(item);
            if (!itemFile.exists())
                continue;
            if (itemFile.isFile()) {
                continue;
            }

        }

        for (int i = 0; i < pathItems.length; i++) {
            String item = pathItems[i];
            if (".".equals(item))
                continue;
            if (inExcludedJars(item))
                continue;
            if (item.indexOf(javaHome) > -1)
                continue;
            File itemFile = new File(item);
            if (!itemFile.exists())
                continue;
            if (itemFile.isFile()) {
                holder.add(itemFile);
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


    protected static boolean inExcludedJars(String s) {
        for (int i = 0; i < EEXCLUDED_JARS_LIST.length; i++) {
            String test = EEXCLUDED_JARS_LIST[i];
            if (s.endsWith(test))
                return true;
        }
        return false;
    }

    public static void handlePreference(final Preferences prefs, String key, String value) {
        if ("path".equals(key)) {
            key = DEFAULT_PATH_STRING;
        }
        if (PASSWORD_STRING.equals(key)) {
            value = Encrypt.encryptString(value);
            System.out.println("Encrypted Password:" + value);
        }
        if (key.endsWith("_root_password")) {
            value = Encrypt.encryptString(value);
        }
        prefs.put(key, value);
    }

    /**
     * @param prefs
     * @param args
     * @return true if test needed
     */
    private static boolean buildPreferences(Preferences prefs, final String[] args) {
        if (args.length == 0) {
            usage();
            return false;
        }
        boolean testNeeded = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("test".equals(arg)) {
                testNeeded = true;
                continue;
            }
            String[] items = arg.split("=");
            if (items.length != 2)
                throw new IllegalStateException("bad property spec " + arg);
            handlePreference(prefs, items[0], items[1]);
        }
        return testNeeded;
    }

    private static boolean testPreferences(Preferences prefs) {
        String user = getProperty(USER_STRING);
        if (user == null || user.length() == 0)
            return false;
        String password = getProperty(PASSWORD_STRING);
        if (password == null || password.length() == 0)
            return false;
        String host = getProperty(HOST_STRING);
        if (host == null || host.length() == 0)
            return false;
        String path = getProperty(DEFAULT_PATH_STRING);
        if (path == null || path.length() == 0)
            return false;
        String port = getProperty(PORT_STRING);
        if (port == null || port.length() == 0)
            return false;
        return true;
    }

    protected static void usage() {
        System.out.println("Usage:");
        System.out.println("java ... org.systemsbiology.remotecontrol.RemoteUtilities \"host=glados\" \"jobtracker=glados:9001\" \"port=9000\" \"user=slewis\" \"password=secret\" \"path=/users/hdfs\"");
        System.out.println("or any subset of the above");
    }


public static final String TEST_CONTENT =
        "Mary had a little lamb,\n" +
                "little lamb, little lamb,\n" +
                "Mary had a little lamb,\n" +
                "whose fleece was white as snow.\n" +
                "And everywhere that Mary went,\n" +
                "Mary went, Mary went,\n" +
                "and everywhere that Mary went,\n" +
                "the lamb was sure to go.";
public static final String FILE_NAME = "little_lamb.txt";


    public static void hdfsReadTest() {
        String host = getHost();
        String password = getPassword();
        String user = getUser();
        String path = getDefaultPath();
        String jobtracker = getJobTracker();
        int port = getPort();
        IHDFSFileSystem access = HDFSAccessor.getFileSystem(host, port);
        String BASE_DIRECTORY = path + "/test/";
        String filePath = BASE_DIRECTORY + FILE_NAME;
        access.guaranteeDirectory(path);

        if (!access.exists(path))
            throw new IllegalStateException("Directory Create failed");

        access.writeToFileSystem(filePath, TEST_CONTENT);

        if (!access.exists(filePath))
            throw new IllegalStateException("File Create failed");

        String result = access.readFromFileSystem(filePath);
        if (result == null)
            throw new IllegalStateException("File read failed");

        if (!result.equals(TEST_CONTENT))
            throw new IllegalStateException("File content not as expected");


        access.deleteFile(filePath);

        if (access.exists(filePath))
            throw new IllegalStateException("File Delete failed");
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
            return;
        }

        Preferences prefs = Preferences.userNodeForPackage(RemoteUtilities.class);
        //      if (!testPreferences(prefs))
        boolean testNeeded = buildPreferences(prefs, args);
        String home = getProperty("hadoop_home");
        String host = getHost();
        String password = getPassword();
        String user = getUser();
        String path = getDefaultPath();
        String jobtracker = getJobTracker();
        int port = getPort();
        validatePreferences();
        if (testNeeded) {
            hdfsReadTest();
        }
    }


}
