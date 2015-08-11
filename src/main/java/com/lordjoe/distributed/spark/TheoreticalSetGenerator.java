package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.spark.accumulators.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spark.TheoreticalSetGenerator
 * use to generate  ITheoreticalSpectrumSet from  IPolypeptide
 * User: Steve
 * Date: 1/5/2015
 */
public class TheoreticalSetGenerator extends AbstractLoggingFlatMapFunction<IPolypeptide,ITheoreticalSpectrumSet> {

    public static final int MAX_CHARGE = 4;
    private final SequenceUtilities sequenceUtilities;
    public TheoreticalSetGenerator(SequenceUtilities s) {
        sequenceUtilities = s;
    }

    /**
     * do work here
     *
     * @param t@return
     */
    @Override
    public Iterable<ITheoreticalSpectrumSet> doCall(final IPolypeptide pp) throws Exception {
        List<ITheoreticalSpectrumSet> ret = new ArrayList<ITheoreticalSpectrumSet>();

        ret.add(generateTheoreticalSet(pp,MAX_CHARGE,sequenceUtilities));  // todo any reason to drop some
        return ret;
    }

    public static ITheoreticalSpectrumSet generateTheoreticalSet(final IPolypeptide peptide,int charge,SequenceUtilities sequenceUtilities) {
         ITheoreticalSpectrumSet ret = new TheoreticalSpectrumSet(charge,peptide.getMatchingMass(),peptide);
         for (int i = 0; i < charge - 1; i++) {
             PeptideSpectrum ps = new PeptideSpectrum(ret, i + 1, IonType.B_ION_TYPES,
                    sequenceUtilities);
             ITheoreticalSpectrum conditioned = ScoringUtilities.applyConditioner(ps,
                     new XTandemTheoreticalScoreConditioner());

         }
               return ret;
     }

}
