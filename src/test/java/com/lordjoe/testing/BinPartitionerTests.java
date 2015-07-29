package com.lordjoe.testing;

import com.lordjoe.algorithms.*;
import com.lordjoe.distributed.hydra.comet_spark.*;
import com.lordjoe.distributed.hydra.fragment.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.testing.BinPartitionerTests
 * Not exactly a test - this reads a cache and allows testing of the BinPatritionerLogic
 * User: Steve
 * Date: 7/27/2015
 */
public class BinPartitionerTests {

    private static void readBinningData(final LineNumberReader pRdr,
                                        final long[] pHolder,
                                        final MapOfLists<Integer, BinChargeKey> pKeys,
                                        final Map<BinChargeKey, Long> pUsedBinsMap) {
        try {
            String line = pRdr.readLine();
            populateHolder(line, pHolder);
            line = pRdr.readLine();
            while (line != null) {
                if (!handleBinLine(line, pKeys))
                    break;
                line = pRdr.readLine();
            }
            // now reading bin sizes
            line = pRdr.readLine();
            while (line != null) {
                if (!handleSizeBinLine(line, pUsedBinsMap))
                    break;
                line = pRdr.readLine();
            }
            pRdr.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static boolean handleSizeBinLine(final String pLine, final Map<BinChargeKey, Long> pUsedBinsMap) {
        if (pLine.startsWith("="))
            return false;
        String[] items = pLine.split("\t");
        BinChargeKey aKey = new BinChargeKey(items[0]);
        Long size = new Long(items[1]);
        pUsedBinsMap.put(aKey, size);
        return true;
    }

    private static boolean handleBinLine(final String pLine, final MapOfLists<Integer, BinChargeKey> pKeys) {
        if (pLine.startsWith("="))
            return false;
        String[] items = pLine.split("\t");
        Integer key = new Integer(items[0]);
        for (int i = 1; i < items.length; i++) {
            BinChargeKey aKey = new BinChargeKey(items[i]);
            pKeys.putItem(key, aKey);
        }
        return true;
    }

    private static void populateHolder(final String pLine, final long[] pHolder) {
        String[] items = pLine.split("\t");
        int index = 0;
        String test = items[index];
        test = test.replace("TotalSpectra ", "").trim();
        pHolder[index++] = Long_Formatter.unFormat(test);

        test = items[index];
        test = test.replace("MaxSpectraInBin ", "").trim();
        pHolder[index++] = Long_Formatter.unFormat(test);

        test = items[index];
        test = test.replace("MaxKeysInBin ", "").trim();
        pHolder[index++] = Long_Formatter.unFormat(test);

    }


    public static void main(String[] args) throws Exception {
        File inp = new File(args[0]);
        LineNumberReader rdr = new LineNumberReader(new FileReader(inp));
        MapOfLists<Integer, BinChargeKey> keys = new MapOfLists<Integer, BinChargeKey>();
        Map<BinChargeKey, Long> usedBinsMap = new HashMap<BinChargeKey, Long>();
        long[] holder = new long[3];
        readBinningData(rdr, holder, keys, usedBinsMap);

        int totalSpectra = (int)holder[0];
        int maxSpectraInBin = (int)holder[1];
        int maxKeysInBin = (int)holder[2];

      //  maxSpectraInBin = 200;

        BinPartitioner partitioner = new BinPartitioner(totalSpectra, keys, usedBinsMap, maxSpectraInBin, maxKeysInBin);

        int numberPartitions = partitioner.numPartitions();
        System.out.println("number Partitions = " + numberPartitions);

    }

}
