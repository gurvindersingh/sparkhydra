package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.spark.accumulators.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.KeyedCometScoredScan
 * User: Steve
 * Date: 1/25/2016
 */
public class KeyedCometScoredScan extends CometScoredScan {

    public static final AbstractLoggingFlatMapFunction<CometScoredScan, KeyedCometScoredScan> MAPPER = new  MapScoredScans();

    private static class MapScoredScans extends AbstractLoggingFlatMapFunction<CometScoredScan, KeyedCometScoredScan> {

        /**
         * do work here
         *
         * @param v1
         * @return
         */
        @Override
        public Iterable<KeyedCometScoredScan> doCall(final CometScoredScan v1) throws Exception {
            Set<BinChargeKey> binChargeKeys = BinChargeMapper.keysFromSpectrum(v1.getRaw());
            List<KeyedCometScoredScan> holder = new ArrayList<KeyedCometScoredScan>();
            for (BinChargeKey binChargeKey : binChargeKeys) {
                holder.add(new KeyedCometScoredScan(v1, binChargeKey));
            }

            return holder;
        }
    }

    private BinChargeKey binChargeKey;

    public KeyedCometScoredScan(final CometScoredScan copy, final BinChargeKey pKey) {
        super(copy);
        binChargeKey = pKey;
    }

    public BinChargeKey getBinChargeKey() {

        return binChargeKey;
    }

    public void setBinChargeKey(final BinChargeKey pKey) {
        binChargeKey = pKey;
    }

}
