package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometTheoreticalBinnedSet
 * User: Steve
 * Date: 4/3/2015
 */
public class CometTheoreticalBinnedSet extends TheoreticalSpectrumSet {
    private final List<BinnedChargeIonIndex> binnedIndex = new ArrayList<BinnedChargeIonIndex>();
    private final List<BinnedChargeIonIndex> binnedIndexNL = new ArrayList<BinnedChargeIonIndex>();

    public CometTheoreticalBinnedSet(final int pMaxCharge, final double mPlusH, final IPolypeptide pPeptide, CometScoringAlgorithm cmt, Scorer scorer) {
        super(pMaxCharge, mPlusH, pPeptide);
        buildBinnedList(cmt, scorer);
    }

    private void buildBinnedList(CometScoringAlgorithm comet, Scorer scorer) {
        int maxFragmentCharge = comet.getMaxFragmentCharge();
        for (int charge = 1; charge < maxFragmentCharge; charge++) {
            PeptideSpectrum ps = new PeptideSpectrum(this, charge, IonType.B_ION_TYPES, scorer.getSequenceUtilities());
            PeptideIon[] spectrum = ps.getSpectrum();

            if (charge > 1)
                TestUtilities.breakHere();

            for (int i = 0; i < spectrum.length; i++) {
                PeptideIon peptideIon = spectrum[i];
                double mz = peptideIon.getMassChargeRatio();
                int index = comet.asBin(mz);
                int binnedIndexInParent = peptideIon.getIndexInParent();
                IonType type = peptideIon.getType();


                if (type == IonType.Y) {   // ollok at this case binned position should go down
                    int sequenceLength = peptideIon.getSequence().length();
                    binnedIndexInParent = sequenceLength - 1;
                }
                else {
                    if (binnedIndexInParent == 10)
                        XTandemUtilities.breakHere();
                }
                BinnedChargeIonIndex bcs = new BinnedChargeIonIndex(index, peptideIon.getCharge(), type, binnedIndexInParent);
                binnedIndex.add(bcs);
            }
        }
        Collections.sort(binnedIndex);
    }

    public List<BinnedChargeIonIndex> getBinnedIndex() {
        return Collections.unmodifiableList(binnedIndex);
    }

    public List<BinnedChargeIonIndex> getBinnedIndexNL() {
        return Collections.unmodifiableList(binnedIndexNL);
    }

//    public void populateBinnedArray(CometScoringAlgorithm alg) {
//        float[] specPeaks = alg.getScoringFastXcorrData();
//        List<BinnedChargeIonIndex> binnedIndex = getBinnedIndex();
//        for (BinnedChargeIonIndex b : binnedIndex) {
//            for (BinnedChargeIonIndex chargeIonIndex : binnedIndex) {
//                specPeaks[b.index] = 1;
//            }
//        }
//        specPeaks = alg.getFastXcorrDataNL();
//        binnedIndex = getBinnedIndexNL();
//        for (BinnedChargeIonIndex b : binnedIndex) {
//            for (BinnedChargeIonIndex chargeIonIndex : binnedIndex) {
//                specPeaks[b.index] = 1;
//            }
//        }
//    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(ITheoreticalSpectrumSet test) {
        if (test == this)
            return true;
        if (getMaxCharge() != test.getMaxCharge())
            return false;
        if (XTandemUtilities.equivalentDouble(getMassPlusH(), test.getMassPlusH()))
            return false;
        if (getPeptide().equivalent(test.getPeptide()))
            return false;

        return true;
    }


}
