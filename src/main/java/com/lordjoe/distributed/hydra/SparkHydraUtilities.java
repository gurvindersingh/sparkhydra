package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.*;
import org.apache.hadoop.fs.*;
import org.apache.spark.*;
import org.systemsbiology.common.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.SparkHydraUtilities
 * User: Steve
 * Date: 10/21/2014
 */
public class SparkHydraUtilities {

    public static final int LINE_LENGTH = 80;

    public static String asFastaString(String in) {
        StringBuilder sb = new StringBuilder();
        while (in.length() > LINE_LENGTH) {
            sb.append(in.substring(0, LINE_LENGTH));
            sb.append("\n");
            in = in.substring(LINE_LENGTH);
        }
        if (in.length() > 0) {
            sb.append(in);
            sb.append("\n");
        }
         return sb.toString();
     }

    public static Partitioner getMeasuredSpectrumPartitioner() {
        return new Partitioner() {
            @Override
            public int numPartitions() {
                return SparkUtilities.getDefaultNumberPartitions();
            }

            @Override
            public int getPartition(final Object key) {
                String id = ((IMeasuredSpectrum) key).getId();
                return Math.abs(id.hashCode() % numPartitions());
            }
        };
    }


    /**
     * take a name - translate to application space and if needed hdfs and open for read
     *
     * @param name name in user.dir
     * @param app  current application
     * @return non null PrintWriter
     * @throws IllegalArgumentException if there is an attempt to write to a directory
     */
    public static PrintWriter nameToPrintWriter(String name, XTandemMain app) {
        return new PrintWriter(nameToOutputStream(name, app));
    }


    /**
     * take a name - translate to application space and if needed hdfs and open for read
     *
     * @param name name in user.dir
     * @param app  current application
     * @return non null OutputStream
     * @throws IllegalArgumentException if there is an attempt to write to a directory
     */
    public static OutputStream nameToOutputStream(String name, XTandemMain app) {
        IFileSystem fs = HydraSparkUtilities.getHadoopFileSystem();
        Path dd = XTandemHadoopUtilities.getRelativePath(name);
        String path = dd.toString();
        if (fs.exists(path)) {
            if (fs.isDirectory(path))
                throw new IllegalArgumentException("cannot delete directory " + path);
            else
                fs.deleteFile(path);
        }
        System.err.println("opening write " + name + " as " + path);
        return fs.openPathForWrite(path);
    }


    /**
     * take a name - translate to application space and if needed hdfs and open for read
     *
     * @param name name in user.dir
     * @param app  current application
     * @return LineNumberReader or null if the file does not exist
     */
    public static LineNumberReader nameToLineNumberReader(String name, XTandemMain app) {
        InputStream in = nameToInputStream(name, app);
        if (in == null)
            return null;
        return new LineNumberReader(new InputStreamReader(in));
    }

    /**
     * take a name - translate to application space and if needed hdfs and open for read
     *
     * @param name name in user.dir
     * @param app  current application
     * @return input stream or null if the file does not exist
     */
    public static InputStream nameToInputStream(String name, XTandemMain app) {
        IFileSystem fs = HydraSparkUtilities.getHadoopFileSystem();
        Path dd = XTandemHadoopUtilities.getRelativePath(name);
        String path = dd.toString();
        if (!fs.exists(path)) {
            return null;
        }
        System.err.println("opening " + name + " as " + path);
        return fs.openPath(path);
    }

    /**
     * throw an exception explaining a bad path
     *
     * @param name name in user.dir
     * @param app  current application
     * @return input stream or null if the file does not exist
     */
    public static void throwBadPathException(String name, XTandemMain app) {
        IFileSystem fs = HydraSparkUtilities.getHadoopFileSystem();
        Path dd = XTandemHadoopUtilities.getRelativePath(name);
        String path = dd.toString();
        throw new IllegalArgumentException("Cannot find path " + name + " as " + path);
    }

}
