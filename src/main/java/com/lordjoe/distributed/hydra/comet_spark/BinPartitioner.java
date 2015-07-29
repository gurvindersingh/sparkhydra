package com.lordjoe.distributed.hydra.comet_spark;

import com.lordjoe.algorithms.*;
import com.lordjoe.distributed.hydra.fragment.*;
import org.apache.spark.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet_spark.BinPartitioner
 * specialized partitioner which is smart about the number of spectra
 * User: Steve
 * Date: 7/22/2015
 */
public class BinPartitioner extends Partitioner {
    public static final int DEFAULT_MAX_SPECTRA_IN_BIN = 50;
    public static final int DEFAULT_MAX_KEYS_IN_BIN = 20;

    public static final String MAX_SPECTRA_PARAMETER = "com.lordjoe.BinPartitioner.MaxSpectraInBin";
    public static final String MAX_KEYS_PARAMETER = "com.lordjoe.BinPartitioner.MaxKeysInBin";

    public final int maxSpectraInBin;
    public final int maxKeysInBin;
    private final long totalSpectra;
    private int maxIndex;
    private final Map<BinChargeKey, Integer> keyToPartition = new HashMap<BinChargeKey, Integer>();

    public BinPartitioner(final long pTotalSpectra, MapOfLists<Integer, BinChargeKey> keys, Map<BinChargeKey, Long> usedBinsMap, int pMaxSpectraInBin, int pMaxKeysInBin) {
        totalSpectra = pTotalSpectra;
        maxSpectraInBin = pMaxSpectraInBin;
        maxKeysInBin = pMaxKeysInBin;
        populateKeyMap(keys, usedBinsMap);
    }

    public long getTotalSpectra() {
        return totalSpectra;
    }

    @Override
    public int numPartitions() {
        return maxIndex;
    }

    @Override
    public int getPartition(final Object key) {
        Integer p = keyToPartition.get((BinChargeKey) key);
        if (p == null)
            return key.hashCode() % maxIndex;
        int partition = p;
        if (partition > numPartitions())
            throw new IllegalStateException("bad partition " + partition + " should not exceed " + numPartitions());
        return partition;
    }

    protected void populateKeyMap(MapOfLists<Integer, BinChargeKey> keys, Map<BinChargeKey, Long> usedBinsMap) {
        int keysPerBin = 0;
        int binSpectra = 0;
        maxIndex = 0;
        long total = 0;
        List<KeyCount> holder = new ArrayList<KeyCount>();
        for (BinChargeKey binChargeKey : usedBinsMap.keySet()) {
            Long size = usedBinsMap.get(binChargeKey);
            total += size;
            holder.add(new KeyCount(size,binChargeKey));
        }
        Collections.sort(holder);  // now holder has the biggest keys first

        keysPerBin = 0;
        binSpectra = 0;
        for (KeyCount keyCount : holder) {
            Long binsize = keyCount.size;
            if (binsize == null) {
                keyToPartition.put(keyCount.key, maxIndex++);  // maybe this is a split - better increment index
                keysPerBin = 0;
                binSpectra = 0;
            }
            else {
                binSpectra += (long) binsize;
                keysPerBin++;
                if (binSpectra > maxSpectraInBin || keysPerBin > maxKeysInBin) {
                    keyToPartition.put(keyCount.key, maxIndex++);     // use and increment index
                    keysPerBin = 0;
                    binSpectra = 0;
                }
                else {
                    keyToPartition.put(keyCount.key, maxIndex); // keep reusing index
                }
            }


        }

         if (keysPerBin > 0)
            maxIndex++; // up index for last partition
    }


//    protected void populateKeyMapOld(MapOfLists<Integer, BinChargeKey> keys, Map<BinChargeKey, Long> usedBinsMap) {
//         int keysPerBin = 0;
//         int binSpectra = 0;
//         maxIndex = 0;
//         List<KeyCount> holder = new ArrayList<KeyCount>();
//         for (BinChargeKey binChargeKey : usedBinsMap.keySet()) {
//             holder.add(new KeyCount(usedBinsMap.get(binChargeKey),binChargeKey));
//         }
//         Collections.sort(holder);  // now holder has the biggest keys first
//
//
//
//           for (List<BinChargeKey> binChargeKeys : keys.values()) {
//             for (BinChargeKey binChargeKey : binChargeKeys) {
//                 Long binsize = usedBinsMap.get(binChargeKey);
//                 if (binsize == null) {
//                     keyToPartition.put(binChargeKey, maxIndex++);  // maybe this is a split - better increment index
//                     keysPerBin = 0;
//                     binSpectra = 0;
//                 }
//                 else {
//                     binSpectra += (long) binsize;
//                     keysPerBin++;
//                     if (binSpectra > maxSpectraInBin || keysPerBin > maxKeysInBin) {
//                         keyToPartition.put(binChargeKey, maxIndex++);     // use and increment index
//                         keysPerBin = 0;
//                         binSpectra = 0;
//                     }
//                     else {
//                         keyToPartition.put(binChargeKey, maxIndex); // keep reusing index
//
//                     }
//                 }
//             }
//         }
//         if (keysPerBin > 0)
//             maxIndex++; // up index for last partition
//     }

    public static class KeyCount implements Comparable<KeyCount> {
        public final long size;
        public final BinChargeKey key;

        public KeyCount(final long pSize, final BinChargeKey pKey) {
            size = pSize;
            key = pKey;
        }


        @Override
        public int compareTo(final KeyCount o) {
            int ret = Long.compare(o.size,size) ;
            if(ret != 0)
                return ret;
            return key.compareTo(o.key);
        }
    }
}
