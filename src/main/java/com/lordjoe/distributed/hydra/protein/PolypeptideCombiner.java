package com.lordjoe.distributed.hydra.protein;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.test.*;
import com.lordjoe.distributed.spark.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.protein.PolypeptideCombiner
 * User: Steve
 * Date: 10/13/2014
 */
public class PolypeptideCombiner {
    public static JavaPairRDD<String,IPolypeptide> combineIdenticalPolyPeptides(JavaPairRDD<String,IPolypeptide> toCombine) {
        return toCombine.combineByKey(
                IdentityFunction.INSTANCE,
                MERGE_INSTANCE,
                MERGE_INSTANCE);
    }

    /**
     * merge the protein positions of two polypeptides
     * @param pV1
     * @param pV2
     * @return
     */
    public static IPolypeptide mergeProteins(final IPolypeptide pV1, final IPolypeptide pV2) {
        IProteinPosition[] pp1 = pV1.getProteinPositions();
        IProteinPosition[] pp2 = pV2.getProteinPositions();
        Set<IProteinPosition> holder = new HashSet<IProteinPosition>();
        for (int i = 0; i < pp1.length; i++) {
            holder.add(pp1[i]);
           }
        for (int i = 0; i < pp2.length; i++) {
              holder.add(pp2[i]);
             }
         IProteinPosition[] pps = new IProteinPosition[holder.size()];
        holder.toArray(pps);
        ((Polypeptide)pV1).setContainedInProteins(pps);
        return pV1;
    }

    public static final MergePolyPeptides  MERGE_INSTANCE = new MergePolyPeptides();

    private static class MergePolyPeptides extends AbstractLoggingFunction2<IPolypeptide, IPolypeptide, IPolypeptide> {


        @Override
        public IPolypeptide doCall(final IPolypeptide v1, final IPolypeptide v2) throws Exception {
            TestUtilities.isInterestingPeptide(v1,v2);
                return mergeProteins(v1,v2);
        }
    }

}
