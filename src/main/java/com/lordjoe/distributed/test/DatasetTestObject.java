package com.lordjoe.distributed.test;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.test.DatasetTestObject
 * User: Steve
 * Date: 1/15/2016
 */
public class DatasetTestObject implements Serializable {

    public static final Random RND = new Random();

    public static DatasetTestObject generateTestObject()
    {
        DatasetTestObject ret = new DatasetTestObject();
        Map<Integer, Double> fastScoringMap = ret.getFastScoringMap();
        // fake a little data
        for (int i = 0; i < 100; i++) {
            fastScoringMap.put(RND.nextInt(20000),RND.nextDouble()) ;
         }
        return ret;
    }


    public static final String DEFAULT_ALGORITHM = "Foo";
     private String m_Version = "Bar";
     private String m_Algorithm = DEFAULT_ALGORITHM;
     private boolean normalizationDone;
     private Map<Integer, Double> fastScoringMap = new HashMap<Integer, Double>();     // not final for dataset - slewis


    public DatasetTestObject() {
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

    public Map<Integer, Double> getFastScoringMap() {
        return fastScoringMap;
    }

    public void setFastScoringMap(final Map<Integer, Double> pFastScoringMap) {
        fastScoringMap = pFastScoringMap;
    }
}
