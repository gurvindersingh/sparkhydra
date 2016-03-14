package com.lordjoe.distributed.test;

import com.lordjoe.distributed.spark.*;
import scala.*;

import java.lang.Double;
import java.util.*;

/**
 * com.lordjoe.distributed.test.DatasetTestObject
 * User: Steve
 * Date: 1/15/2016
 */
public class DatasetTestObject implements Serializable {

    public static final int NUMBER_ENTRIES = 150;

    public static final Random RND = new Random();

    public static DatasetTestObject generateTestObject() {
        DatasetTestObject ret = new DatasetTestObject();
        // fake a little data
        while(ret.findCount() < NUMBER_ENTRIES)
              ret.addValue(RND.nextInt(20000), RND.nextDouble());
         return ret;
    }


    public static final String DEFAULT_ALGORITHM = "Foo";
    private String m_Version = "Bar";
    private String m_Algorithm = DEFAULT_ALGORITHM;
    private boolean normalizationDone;
    private List<KeyValue<Integer, Double>> values;
    private transient Map<Integer, Double> fastScoringMap;

    public DatasetTestObject() {
    }


    public synchronized void addValue(Integer i, Double d) {
        if (values == null) {
            setFastScoringMapValues(new ArrayList<KeyValue<Integer, Double>>());
        }
        if (!fastScoringMap.containsKey(i)) {
            fastScoringMap.put(i, d);
            values.add(new KeyValue<Integer, Double>(i, d));
        }
        else {    // unhappy path rebuild
            fastScoringMap.put(i, d);
            setFastScoringMapValues(DataSetUtilities.obtainValues(fastScoringMap)) ;
        }
    }

    public String getVersion() {
        return m_Version;
    }

    public void setVersion(final String pVersion) {
        m_Version = pVersion;
    }

    public String getAlgorithm() {
        return m_Algorithm;
    }

    public void setAlgorithm(final String pAlgorithm) {
        m_Algorithm = pAlgorithm;
    }

    public boolean isNormalizationDone() {
        return normalizationDone;
    }

    public void setNormalizationDone(final boolean pNormalizationDone) {
        normalizationDone = pNormalizationDone;
    }


    public int findCount() {
        if(values == null) return 0;
        else return values.size();
    }

    public List<KeyValue<Integer, Double>> getFastScoringMapValues() {
        return values;
    }

    public void setFastScoringMapValues(List<KeyValue<Integer, Double>> items) {
        values = items;
        fastScoringMap = DataSetUtilities.obtainMap(items);
    }
}
