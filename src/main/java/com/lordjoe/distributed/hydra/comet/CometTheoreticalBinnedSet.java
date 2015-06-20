package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.XTandemUtilities;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.scoring.Scorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * com.lordjoe.distributed.hydra.comet.CometTheoreticalBinnedSet
 * User: Steve
 * Date: 4/3/2015
 */
public class CometTheoreticalBinnedSet extends TheoreticalSpectrumSet {

    //  private static boolean isInteresting;

    private final List<BinnedChargeIonIndex> binnedIndex;
    //   private final List<BinnedChargeIonIndex> binnedIndexNL = new ArrayList<BinnedChargeIonIndex>();

    public CometTheoreticalBinnedSet(final int pMaxCharge, final double mPlusH, final IPolypeptide pPeptide, CometScoringAlgorithm cmt, Scorer scorer) {
        super(pMaxCharge, mPlusH, pPeptide);

//        isInteresting = false;
//        if(TestUtilities.isInterestingPeptide(pPeptide))
//            isInteresting = true;

        binnedIndex = buildBinnedList(cmt, pMaxCharge, scorer);
    }


    private List<BinnedChargeIonIndex> buildBinnedList(CometScoringAlgorithm comet, int maxCharge, Scorer scorer) {
        final List<BinnedChargeIonIndex> binnedChargeIonIndexes = new ArrayList<BinnedChargeIonIndex>();
        int maxFragmentCharge = Math.min(maxCharge, comet.getMaxFragmentCharge());

        for (int charge = 1; charge <= maxFragmentCharge; charge++) {      // was < max charge
            PeptideSpectrum ps = new PeptideSpectrum(this, charge, IonType.B_ION_TYPES, scorer.getSequenceUtilities());
            PeptideIon[] spectrum = ps.getSpectrum();

//            if (charge > 1)
//                TestUtilities.breakHere();

            for (int i = 0; i < spectrum.length; i++) {
                PeptideIon peptideIon = spectrum[i];
                int binnedIndexInParent = peptideIon.getIndexInParent();
                IonType type = peptideIon.getType();

//                if(isInteresting && type == IonType.Y && binnedIndexInParent == 9)
//                    TestUtilities.breakHere();

                double mz = peptideIon.getMassChargeRatio();
                int index = comet.asBin(mz);


                if (type == IonType.Y) {   // ollok at this case binned position should go down
                    int sequenceLength = peptideIon.getSequence().length();
                    binnedIndexInParent = sequenceLength - 1;
                } else {
                    if (binnedIndexInParent == 10)
                        XTandemUtilities.breakHere();
                }
                double mass = mz; // * peptideIon.getCharge();
                // todo take out
                // keep mass for debugging
                //          TestBinChargeIonIndex bcs = new TestBinChargeIonIndex(index, peptideIon.getCharge(), type, binnedIndexInParent,mass);
                BinnedChargeIonIndex bcs = new BinnedChargeIonIndex(index, peptideIon.getCharge(), type, binnedIndexInParent);
                binnedChargeIonIndexes.add(bcs);
            }
        }
        Collections.sort(binnedChargeIonIndexes, BinnedChargeIonIndex.BY_INDEX);
        return binnedChargeIonIndexes;
    }

    public List<BinnedChargeIonIndex> getBinnedIndex(CometScoringAlgorithm cmt, Scorer scorer) {

        return binnedIndex; //  buildBinnedList(cmt, scorer);
    }

    public int getBinnedIndexCount() {
        return binnedIndex.size();
    }

    public List<BinnedChargeIonIndex> getBinnedIndexNL(CometScoringAlgorithm cmt, Scorer scorer) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        if(binnedIndex.isEmpty())
//             buildBinnedList(cmt,scorer);
//        return Collections.unmodifiableList(binnedIndexNL);
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
