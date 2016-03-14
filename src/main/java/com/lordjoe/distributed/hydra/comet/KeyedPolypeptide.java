package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.spark.accumulators.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.KeyedCometScoredScan
 * User: Steve
 * Date: 1/25/2016
 */
public class KeyedPolypeptide extends Polypeptide {

    public static final AbstractLoggingFlatMapFunction<Polypeptide, KeyedPolypeptide> MAPPER = new MapScoredScans();

    private static class MapScoredScans extends AbstractLoggingFlatMapFunction<Polypeptide, KeyedPolypeptide> {

        /**
         * do work here
         *
         * @param v1
         * @return
         */
        @Override
        public Iterable<KeyedPolypeptide> doCall(final Polypeptide v1) throws Exception {
            BinChargeKey binChargeKey = BinChargeMapper.oneKeyFromChargeMz(1, v1.getMass());
            List<KeyedPolypeptide> holder = new ArrayList<KeyedPolypeptide>();
            holder.add(new KeyedPolypeptide(v1, binChargeKey));
            return holder;
        }
    }

    private BinChargeKey binChargeKey;

    public KeyedPolypeptide(final Polypeptide copy, final BinChargeKey pKey) {
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
