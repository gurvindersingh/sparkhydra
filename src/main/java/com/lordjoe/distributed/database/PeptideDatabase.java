package com.lordjoe.distributed.database;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.hydra.peptide.*;
import org.apache.hadoop.fs.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import scala.*;

import java.lang.Boolean;
import java.lang.Long;
import java.util.*;

/**
 * com.lordjoe.distributed.database.PeptideDatabase
 * An object representing a parquet database of peptides capable of retreizing peptides with a specific
 * BinChargeKey
 * User: Steve
 * Date: 12/31/2014
 */
public class PeptideDatabase implements Serializable {

    private transient  boolean logSilenced;
    private final String databaseName;
    private final Map<Integer, Long> keyCounts = new HashMap<Integer, Long>();

    public PeptideDatabase(XTandemMain app) {
        String fasta = app.getDatabaseName();
        Path defaultPath = XTandemHadoopUtilities.getDefaultPath();
        databaseName = defaultPath.toString() + "/" + fasta + ".parquet";
        buildKeyCounts();
    }

    /**
     * find counts as an efficiency measure
     */
    private void buildKeyCounts() {
        JavaSQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
        String databaseName1 = getDatabaseName();
        try {
            JavaSchemaRDD parquetFile = sqlContext.parquetFile(databaseName1);

            //Parquet files can also be registered as tables and then used in SQL statements.
            parquetFile.registerAsTable("peptides");

            JavaSchemaRDD binCounts = sqlContext.sql("SELECT massBin,count(*) FROM  peptides  group by massBin ");
            Iterator<Row> rowIterator = binCounts.toLocalIterator();
            while (rowIterator.hasNext()) {
                Row rw = rowIterator.next();
                Integer bin = rw.getInt(0);
                Long count = rw.getLong(1);
                keyCounts.put(bin, count);
            }
            List<BinChargeKey> keysWithData = new ArrayList<BinChargeKey>();
            for (Integer keyCount : keyCounts.keySet()) {
                double mz = BinChargeKey.intToMz(keyCount);
                BinChargeKey key = new BinChargeKey(1, mz);
                keysWithData.add(key);
            }
            Collections.sort(keysWithData);
           // for debugging show keys with and without data
          //  showKeysWithAndWithoutData(keysWithData);
        }
        catch (Exception e) {
            System.err.println("cannot open database " + databaseName1);
            return;

        }
    }

    private void showKeysWithAndWithoutData(final List<BinChargeKey> pKeysWithData) {
        Set<Integer> keysWithoutData = new HashSet<Integer>() ;
        int start = pKeysWithData.get(0).getMzInt();
        int end = pKeysWithData.get(pKeysWithData.size() - 1).getMzInt();
        for (int i = start; i < end; i++) {
            keysWithoutData.add(i);
           }

        for (BinChargeKey binChargeKey : pKeysWithData) {
            System.out.println("Data " + binChargeKey);
            keysWithoutData.remove(binChargeKey.getMzInt());
        }
        List<BinChargeKey> noData = new ArrayList<BinChargeKey>();
        for (Integer integer : keysWithoutData) {
            noData.add(new BinChargeKey(1,BinChargeKey.intToMz(integer)));
        }
        Collections.sort(noData);
        for (BinChargeKey key : noData) {
            double mz = key.getMz();
            if(mz < 350 || mz > 2000)
                continue;
            System.out.println("No Data " + key);
        }
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public long getKeyCounts(BinChargeKey key) {
        Integer mzAsInt = key.getMzInt();
        Long count = keyCounts.get(mzAsInt);
        if (count == null || count == 0)
            return 0;
        return count;
    }

    public List<IPolypeptide> getPeptidesWithKey(BinChargeKey key) {
        guaranteeLogSilenced();
        List<IPolypeptide> ret = new ArrayList<IPolypeptide>();
        if (getKeyCounts(key) == 0)
            return ret;    // nothing to score

        int mzAsInt = key.getMzInt();
        try {
            JavaSQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
            SparkUtilities.setLogToWarn();
            String databaseName1 = getDatabaseName();
            JavaSchemaRDD parquetFile = sqlContext.parquetFile(databaseName1);

            //Parquet files can also be registered as tables and then used in SQL statements.
            parquetFile.registerAsTable("peptides");
            JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides" + " Where  massBin = " + mzAsInt);
            Iterator<Row> rowIterator = binCounts.toLocalIterator();
            while (rowIterator.hasNext()) {
                Row rw = rowIterator.next();
                PeptideSchemaBean bean = PeptideSchemaBean.FROM_ROW.call(rw);
                IPolypeptide pp = bean.asPeptide();
                ret.add(pp);
            }
            return ret;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }

    /**
     * try to get logs to shut up
     */
    private void guaranteeLogSilenced() {
        if(!logSilenced)  {
            logSilenced = true;
            SparkUtilities.setLogToWarn();
        }

    }

    /**
     * filter keys with no peptides
     *
     * @param pAllSpectrumPairs
     * @return keys with peptides
     */
    public JavaPairRDD<BinChargeKey, Tuple2<BinChargeKey, IMeasuredSpectrum>> filterKeysWithData(final JavaPairRDD<BinChargeKey, Tuple2<BinChargeKey, IMeasuredSpectrum>> pAllSpectrumPairs) {
        return pAllSpectrumPairs.filter(new Function<Tuple2<BinChargeKey, Tuple2<BinChargeKey, IMeasuredSpectrum>>, java.lang.Boolean>() {
            @Override
            public Boolean call(final Tuple2<BinChargeKey, Tuple2<BinChargeKey, IMeasuredSpectrum>> v1) throws Exception {
                BinChargeKey binChargeKey = v1._1();
                boolean hasData = getKeyCounts(binChargeKey) > 0;
                //noinspection RedundantIfStatement
                if (!hasData)
                    return false;
                return true;    // allow break
            }
        });
    }
}
