package com.lordjoe.distributed.database;

import com.lordjoe.algorithms.*;
import com.lordjoe.distributed.hydra.peptide.*;
import com.lordjoe.distributed.spark.*;
import org.apache.hadoop.fs.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.systemsbiology.common.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import scala.Serializable;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.database.PeptideDatabaseWriter
 * User: Steve
 * Date: 10/24/2014
 */
public class PeptideDatabaseWriter implements Serializable {
    private transient ThreadLocal<Integer> currentMass;
    private transient ThreadLocal<Path> currentPath;
    private transient ThreadLocal<PrintWriter> currentWriter;
    private Function<IPolypeptide, String> startCombiner;
    private Function2<String, IPolypeptide, String> continueCombiner;
    private Function2<String, String, String> endCombiner;
    private MZPartitioner partitioner;
    private XTandemMain application;
    private CountedMap<String> peptideUses = new CountedMap<String>();
    private String defaultPath;

    public static final int NUMBER_MZ_PARTITIONS = 1000;

    // this keeps Kryo happy
    private PeptideDatabaseWriter() {}

    public PeptideDatabaseWriter(XTandemMain app) {
        application = app;
        defaultPath = XTandemHadoopUtilities.getDefaultPath().toString();
        if (defaultPath == null) {
            defaultPath = app.getParameter("com.lordjoe.distributed.PathPrepend");
            System.err.println("Setting Path Prepend " + defaultPath);
            XTandemHadoopUtilities.setDefaultPath(defaultPath);
        }

        startCombiner = new StartCombiner();
        continueCombiner = new ContinueCombiner();
        endCombiner = new MergeCombiner();
        partitioner = new MZPartitioner();

    }

    public MZPartitioner getPartitioner() {
        return partitioner;
    }

    public XTandemMain getApplication() {
        return application;
    }

    public List<String> saveRDDAsDatabaseFiles(JavaPairRDD<Integer, IPolypeptide> pByMZ) {
        JavaPairRDD<Integer, String> databasePaths = pByMZ.combineByKey(
                startCombiner,
                continueCombiner,
                endCombiner
                , partitioner
        );
        Map<Integer, String> integerPathMap = databasePaths.collectAsMap();
        List<String> paths = new ArrayList(integerPathMap.values());
        Collections.sort(paths);
        return paths;
    }

    protected class MZPartitioner extends Partitioner {
        @Override
        public int numPartitions() {
            return NUMBER_MZ_PARTITIONS;
        }

        @Override
        public int getPartition(final Object key) {
            int pp = (Integer) key;
            return Math.abs(pp % numPartitions());
        }
    }


    protected class StartCombiner implements Function<IPolypeptide, String> {

        @Override
        public String call(final IPolypeptide pp) throws Exception {
            String String = buildPathFromPolypeptide(pp);
            writePeptide(pp);
            return String;
        }
    }


    protected class ContinueCombiner implements Function2<String, IPolypeptide, String> {
        @Override
        public String call(final String v1, final IPolypeptide pp) throws Exception {
            String path = buildPathFromPolypeptide(pp);
            writePeptide(pp);
            return path;
        }

    }

    protected synchronized PrintWriter getCurrentWriter() {
        if (currentWriter == null)
            currentWriter = new ThreadLocal<PrintWriter>();
        return currentWriter.get();
    }

    protected synchronized Integer getCurrentMass() {
        if (currentMass == null)
            currentMass = new ThreadLocal<Integer>();
        return currentMass.get();
    }

    protected synchronized Path getCurrentPath() {
        if (currentPath == null)
            currentPath = new ThreadLocal<Path>();
        return currentPath.get();
    }

    public synchronized void setCurrentMass(Integer pCurrentMass) {
        if (currentMass == null)
            currentMass = new ThreadLocal<Integer>();

        currentMass.set(pCurrentMass);
    }

    public void setCurrentPath(Path pCurrentPath) {
        if (currentPath == null)
            currentPath = new ThreadLocal<Path>();

        currentPath.set(pCurrentPath);
    }

    public void setCurrentWriter(PrintWriter pCurrentWriter) {
        if (currentWriter == null)
            currentWriter = new ThreadLocal<PrintWriter>();
        currentWriter.set(pCurrentWriter);
    }

    public void closeCurrentWriter() {
        if (currentWriter == null)
            return;
        PrintWriter printWriter = currentWriter.get();
        if (printWriter == null)
            return;
        printWriter.close();
        currentWriter.remove();
    }

    protected class MergeCombiner implements Function2<String, String, String> {
        @Override
        public String call(final String v1, final String v2) throws Exception {
            closeCurrentWriter();
            if (!v1.equals(v2))
                return v1;
            throw new UnsupportedOperationException("Now What"); // ToDo
        }
    }

    public static final boolean SHOW_PATHS = false;

    protected String buildPathFromPolypeptide(IPolypeptide pp) {
        int mass = (int) pp.getMatchingMass();
        Integer usingMass = getCurrentMass();
        if (usingMass != null && mass == usingMass)
            return getCurrentPath().toString();
        String dbName = getApplication().getDatabaseName();
        String nameFromMass = HadoopUtilities.buildFileNameFromMass(mass);

        if(SHOW_PATHS)
          System.err.println("Default path " + defaultPath);
        Path dbPath = new Path(defaultPath, dbName);
        if(SHOW_PATHS)
             System.err.println("DB path " + dbPath);
        setCurrentMass(mass);
        Path currentPathP = new Path(dbPath, nameFromMass);
        setCurrentPath(currentPathP);
        setWriterPath(dbPath);
        return currentPathP.toString();

    }

    protected void setWriterPath(final Path pDbPath) {
        closeCurrentWriter();
        IFileSystem hadoopFileSystem = HydraSparkUtilities.getHadoopFileSystem();
        String hdfsPath = pDbPath.toString();
        hadoopFileSystem.guaranteeDirectory(hdfsPath);

        String hdfsPath1 = getCurrentPath().toString();
        peptideUses.add(hdfsPath1);
        int count = peptideUses.get(hdfsPath1);
        if (count > 1)
            count = peptideUses.get(hdfsPath1); // is this right
        OutputStream outputStream = hadoopFileSystem.openPathForWrite(hdfsPath1);
        setCurrentWriter(new PrintWriter(new OutputStreamWriter(outputStream)));

    }


    protected void writePeptide(IPolypeptide pp) {

        String s = new PeptideSchemaBean(pp).toString();
        getCurrentWriter().println(s);

    }

}
