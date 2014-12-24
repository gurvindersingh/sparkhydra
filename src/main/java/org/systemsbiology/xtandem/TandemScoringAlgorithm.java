package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.TandemScoringAlgorithm
 *     this class emulated the scoring algorighm used by X!Tandel
 * @author Steve Lewis
  */
public abstract class TandemScoringAlgorithm extends AbstractScoringAlgorithm {
    public static TandemScoringAlgorithm[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = TandemScoringAlgorithm.class;


 //   private float m_SpectrumMonoIsotopicMassError;
 //   private float m_SpectrumMonoIsotopicMassErrorMinus;
  //  private float m_SpectrumHomologyError;
    private final double[] m_MeasuredMassesHolder = new double[MAX_MASS];
    private final double[] m_TheoreticalMassesHolder = new double[MAX_MASS];
    private final ITheoreticalPeak[] m_TheoreticalPeakHolder = new ITheoreticalPeak[MAX_MASS];


    public TandemScoringAlgorithm()
    {
    }


    public double[] getMeasuredMassesHolder()
    {
        return m_MeasuredMassesHolder;
    }

    public double[] getTheoreticalMassesHolder()
    {
        return m_TheoreticalMassesHolder;
    }

    public ITheoreticalPeak[] getTheoreticalPeakHolder()
    {
        return m_TheoreticalPeakHolder;
    }

    public void setMassType(MassType pMassType)
    {
        m_MassType = pMassType;
    }

    /*
     * hconvert is use to convert a hyper score to the value used in a score
     * histogram, since the actual scoring distribution may not lend itself
     * to statistical analysis through use of a histogram.
     */

    public double hconvert(double _f)
    {
        if (_f <= 0.0)
            return 0.0;
        return (float) (0.05 * Math.log10(_f));
    }

//     public boolean isScoringCyclicPermutation()
//    {
//        return m_ScoringCyclicPermutation;
//    }
//
//    public void setScoringCyclicPermutation(boolean pScoringCyclicPermutation)
//    {
//        m_ScoringCyclicPermutation = pScoringCyclicPermutation;
//    }
//
//     public boolean isScoringIncludeReverse()
//    {
//        return m_ScoringIncludeReverse;
//    }
//
//    public void setScoringIncludeReverse(boolean pScoringIncludeReverse)
//    {
//        m_ScoringIncludeReverse = pScoringIncludeReverse;
//    }
//
//     public boolean isRefineSpectrumSynthesis()
//    {
//        return m_RefineSpectrumSynthesis;
//    }
//
//
//    @Override
//    public float getSpectrumMonoIsotopicMassError()
//    {
//        return m_SpectrumMonoIsotopicMassError;
//    }
//
//    @Override
//    public float getSpectrumMonoIsotopicMassErrorMinus()
//    {
//        return m_SpectrumMonoIsotopicMassErrorMinus;
//    }
//
//    @Override
//    public float getSpectrumHomologyError()
//    {
//        return m_SpectrumHomologyError;
//    }
//
    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    @Override
    public void configure(IParameterHolder params)
    {
        super.configure(params);

        Boolean bval = null;
        MassType type = params.getEnumParameter("spectrum, fragment mass type", MassType.class);
        if (type != null)
            setMassType(type);
//
//        Integer val = params.getIntParameter("scoring, minimum ion count");
//        if (val != null)
//            setMinimumIonCount(val);
//
//        val = params.getIntParameter("scoring, maximum missed cleavage sites");
//        if (val != null)
//            setMaximumMissedCleavageSites(val);
//
//        Boolean bval = params.getBooleanParameter("scoring, x ions");
//        if (bval != null)
//            setScoringXIons(bval);
//
//        bval = params.getBooleanParameter("scoring, y ions");
//        if (bval != null)
//            setScoringYIons(bval);
//
//        bval = params.getBooleanParameter("scoring, a ions");
//        if (bval != null)
//            setScoringAIons(bval);
//
//        bval = params.getBooleanParameter("scoring, b ions");
//        if (bval != null)
//            setScoringBIons(bval);
//
//        bval = params.getBooleanParameter("scoring, c ions");
//        if (bval != null)
//            setScoringCIons(bval);
//
//        bval = params.getBooleanParameter("scoring, cyclic permutation");
//        if (bval != null)
//            setScoringCyclicPermutation(bval);
//
//        bval = params.getBooleanParameter("scoring, include reverse");
//        if (bval != null)
//            setScoringIncludeReverse(bval);

        bval = params.getBooleanParameter("protein, cleavage semi");
        if (bval != null)
            setSemiTryptic(bval);
//
//        m_RefineSpectrumSynthesis = params.getBooleanParameter("refine, spectrum synthesis", false);

        /*
 * check the m_xmlValues parameters for a set of known input parameters, and
 * substitute default values if they are not found
 */
        m_SpectrumMassError = params.getFloatParameter("spectrum, fragment mass error", 0.40F);
        // try the older name
        m_SpectrumMassError = params.getFloatParameter("spectrum, fragment monoisotopic mass error",
                m_SpectrumMassError);
        if (m_SpectrumMassError <= 0)
            m_SpectrumMassError = 0.4F;

//        // allow 100 ppm error
//        m_SpectrumMonoIsotopicMassError = params.getFloatParameter(
//                "spectrum, parent monoisotopic mass error plus", 100);
//        // allow 100 ppm error
//        m_SpectrumMonoIsotopicMassErrorMinus = params.getFloatParameter(
//                "spectrum, parent monoisotopic mass error minus", 100);
//
//
//        m_pScore - > set_parent_error(fErrorValue, false);
//        strKey = "spectrum, m_ParentStream monoisotopic mass isotope error";
//        m_xmlValues.get(strKey, strValue);
//        if (strValue == "yes") {
//            m_errValues.m_bIsotope = true;
//            m_pScore - > set_isotope_error(true);
//        }
//        else {
//            m_pScore - > set_isotope_error(false);
//            m_errValues.m_bIsotope = false;
//        }
//        strKey = "protein, cleavage N-terminal limit";
//        if (m_xmlValues.get(strKey, strValue)) {
//            if (atoi(strValue.c_str()) > 0) {
//                m_lStartMax = atoi(strValue.c_str());
//            }
//        }
//        strKey = "protein, modified residue mass file";
//        if (m_xmlValues.get(strKey, strValue)) {
//            m_pScore - > m_seqUtil.set_aa_file(strValue);
//            m_pScore - > m_seqUtilAvg.set_aa_file(strValue);
//        }
//        strKey = "protein, cleavage site";
//        m_xmlValues.get(strKey, strValue);
//        m_Cleave.load(strValue);
//        strKey = "protein, cleavage semi";
//        m_xmlValues.get(strKey, strValue);
//        if (strValue == "yes") {
//            m_semiState.activate(true);
//        }
//        strKey = "scoring, minimum ion count";
//        m_xmlValues.get(strKey, strValue);
//        m_lIonCount = (unsigned long)atoi(strValue.c_str()) - 1;
//        strKey = "scoring, maximum missed cleavage sites";
//        m_xmlValues.get(strKey, strValue);
//        m_tMissedCleaves = atoi(strValue.c_str());
//        strKey = "spectrum, sequence batch size";
//        m_xmlValues.get(strKey, strValue);
//        size_t tBatch = atoi(strValue.c_str());
//        if (tBatch < 1) {
//            tBatch = 1000;
//        }
//        m_svrSequences.initialize(tBatch);
//        strKey = "protein, use annotations";
//        m_xmlValues.get(strKey, strValue);
//        m_bAnnotation = false;
//        m_strLastMods.clear();
//        if (strValue == "yes") {
//            m_bAnnotation = true;
//        }
//        size_t tLength = 0;
//        size_t a = 0;
//        m_tProteinCount = 0;
//        m_tPeptideCount = 0;
//        m_tPeptideScoredCount = 0;
//        strKey = "output, http";
//        m_xmlValues.get(strKey, strValue);
//        a = 0;
    }

/*
* dot is the fundamental logic for scoring a peptide with a mass spectrum.
* the mass spectrum is determined by the value of m_lId, which is its index
* number in the m_vsmapMI vector. the sequence is represented by the values
* that are currently held in m_plSeq (integer masses) and m_pfSeq (scoring
* weight factors).
* convolution scores are the sum of the products of the spectrum intensities
* and the scoring weight factors for a spectrum.
* hyper scores are the product of the products of the spectrum intensities
* and the scoring weight factors for a spectrum.
* the number of predicted ions that correspond to non-zero intensities in
* the spectrum are tallied and returned.
*/


    public int indexFromMass(double mass)
    {
        final float v = getSpectrumMassError();
        return (int) ((mass / v) + 0.5);
    }

    /**
     * get the mass represented by an index
     *
     * @param index
     * @return convert an in to a mass
     */
     public double massFromIndex(int index)
    {
        return getSpectrumMassError() * index;
    }


    /**
     * Cheat by rounding mass to the nearest int and limiting to MAX_MASS
     * then just generate arrays of the masses and multiply them all together
     *
      * @param measured  !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter    !null use counter
     * @param holder    !null holder to capture scored peaks
     * @param otherData   anything else the algorithm needs
      * @return the dot product
      */

    public double dot_product (IMeasuredSpectrum measured, ITheoreticalSpectrum theory,
                              IonUseCounter counter, List<DebugMatchPeak> holder,  Object... otherData)
    {

        final double[] theoreticalHolder = getTheoreticalMassesHolder();
        Arrays.fill(theoreticalHolder, 0);
        final double[] measuredHolder = getMeasuredMassesHolder();
        Arrays.fill(measuredHolder, 0);
        final ITheoreticalPeak[] theoreticalPeaks = getTheoreticalPeakHolder();
        Arrays.fill(theoreticalPeaks, null);

        final ISpectrumPeak[] mps = measured.getPeaks();
        if (mps.length == 0)
            return 0;
        final ITheoreticalPeak[] tps = theory.getTheoreticalPeaks();
        if (tps.length == 0)
            return 0;
        int charge = theory.getCharge();

        // debuggin gwhy are we not handling higher charges right
        if (charge > 1)
            XTandemUtilities.breakHere();


        setTheoreticalPeaks(charge,theoreticalHolder, theoreticalPeaks, tps);
        int start = 1;
        int end = MAX_MASS;
        if (mps.length > 0) {
            start = Math.max(1, indexFromMass(mps[0].getMassChargeRatio()));
            end = Math.min(MAX_MASS, 2 + indexFromMass(mps[mps.length - 1].getMassChargeRatio()));
        }
        // todo - lift this piece out
        for (int i = 0; i < mps.length; i++) {
            ISpectrumPeak mp = mps[i];
            final double mass = mp.getMassChargeRatio();
            int massIndex = indexFromMass(mass);
            if (massIndex < MAX_MASS)
                measuredHolder[massIndex] += mp.getPeak();
        }

        if (XTandemDebugging.isDebugging()) {
           DebugValues cvs = XTandemDebugging.getComparisonValues();
           DebugDotProduct[] dds = cvs.getDebugDotProductsWithId(measured.getId());
           for (int i = 0; i < dds.length; i++) {
               DebugDotProduct dd = dds[i];
               ISpectrumPeak[] peaks = dd.getMeasuredPeaks();
               for (int j = 0; j < peaks.length; j++) {
                   ISpectrumPeak peak = peaks[j];

               }
               peaks = dd.getTheoreticalPeaks();
               for (int j = 0; j < peaks.length; j++) {
                   ISpectrumPeak peak = peaks[j];

               }
           }
       }


        double product = computeDotProduce(start, end, counter, theoreticalHolder, measuredHolder,
                theoreticalPeaks, holder);
        if (false && counter.getNumberMatchedPeaks() > 0)
            XMLUtilities.outputLine("dot_product=" + XTandemUtilities.formatDouble(product, 3) +
                    " id=" + measured.getId() +
                    " sequence=" + theory.getPeptide().getSequence() +
                    " charge=" + charge +
                    " Y_Count=" + counter.getCount(IonType.Y) +
                    " Y_Score=" + XTandemUtilities.formatDouble(counter.getScore(IonType.Y), 3) +
                    " B_Count=" + counter.getCount(IonType.B) +
                    " B_Score=" + XTandemUtilities.formatDouble(counter.getScore(IonType.B), 3)
            );
        if (XTandemDebugging.isDebugging()) {
            DebugMatchPeak[] matches = new DebugMatchPeak[holder.size()];
            holder.toArray(matches);
            final DebugValues debugValues = XTandemDebugging.getComparisonValues();
            DebugDotProduct dtxt = debugValues.getValuesWithId(measured.getId());

         //   final DebugValues meValues = XTandemDebugging.getLocalValues();
        //    DebugDotProduct dtme = meValues.getValuesWithId(measured.getId());
        }
        return (product);
    }

    protected boolean setTheoreticalPeaks(int charge,final double[] pTheoreticalHolder, final ITheoreticalPeak[] pTheoreticalPeaks, final ITheoreticalPeak[] pTps) {
        boolean addedPeak = false;
        for (int i = 0; i < pTps.length; i++) {
            ITheoreticalPeak tp = pTps[i];
            if (tp.getPeptide().getNumberPeptideBonds() == 0)
                continue; // drop single amino acids
            final double mass = tp.getMassChargeRatio();
            int massIndex = indexFromMass(mass);
            if (massIndex < MAX_MASS) {
                pTheoreticalHolder[massIndex] += tp.getPeak();
                pTheoreticalPeaks[massIndex] = tp;
                addedPeak = true;
            }
        }
        return   addedPeak;
    }

    protected double computeDotProduce(int start, int end, IonUseCounter counter,
                                       final double[] theoreticalHolder,
                                       final double[] measuredHolder,
                                       ITheoreticalPeak[] pTheoreticalPeak,
                                       List<DebugMatchPeak> matches
    )
    {
        double product = 0;
        for (int i = start; i < end; i++) {
            final double theoryPk = theoreticalHolder[i];
            final double measuredPeak = measuredHolder[i];
            if (theoryPk > 0 && measuredPeak > 0) {
                // This section is for debugging
                final ITheoreticalPeak theoryPeak = pTheoreticalPeak[i];
                double mass = massFromIndex(i);
                double peakMass = theoryPeak.getMassChargeRatio();


                final double value = theoryPk * measuredPeak;
                final IonType type = theoryPeak.getType();
                counter.addCount(type);
                product += value;
                counter.addScore(type, value);
                if (ScoringUtilities.SHOW_SCORING)
                    XMLUtilities.outputLine(
                            "matched " + peakMass + "theory " + XTandemUtilities.formatDouble(
                                    theoryPk, 4) +
                                    " measured " + XTandemUtilities.formatDouble(measuredPeak, 4));
            }

        }
        return product;
    }


    /**
     * return the produyct of the factorials of the counts
     *
     * @param counter - !null holding counts
     * @return as above
     */
    @Override
    public double getCountFactor(IonUseScore counter)
    {
        double ret = 1;
        for (IonType it : IonType.values()) {
            int count = counter.getCount(it);
            ret *= factorial(count);
        }

        return ret;
    }

    /**
     * score the two spectra
     *
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @return value of the score
     */
    @Override
    public double scoreSpectrum(IMeasuredSpectrum measured, ITheoreticalSpectrum theory, Object... otherdata)
    {
        IonUseCounter counter = new IonUseCounter();
         List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();
        double dot = dot_product(measured, theory, counter,holder);
        double factor = getCountFactor(counter);

        return dot * factor;

    }

    /**
     * some residues are assigned higher theoretical peaks presumably representing
     * a greater chance of fragmentation
     *
     * @param type !null peal type
     * @param c    aminoAcid value
     * @return multiplier - usually 1
     */
    public static float theoreticalPeakMultiplier(IonType type, char c)
    {
        switch (type) {
            case Y:
                switch (c) {
                    case 'p':
                    case 'P':
                        return 5.0F;
                    default:
                        return 1.0F;
                }
            case B:

                switch (c) {
                    case 'd':
                    case 'D':
                        return 5.0F;
                    case 'n':
                    case 'N':
                        return 2.0F;
                    case 'q':
                    case 'Q':
                        return 2.0F;
                    case 'v':
                    case 'V':
                    case 'e':
                    case 'E':
                    case 'i':
                    case 'I':
                    case 'l':
                    case 'L':
                        return 3.0F;
                    default:
                        return 1.0F;
                }

        }
        return 1.0F;
    }

    @Override
    public ITheoreticalSpectrum buildScoredScan(ITheoreticalSpectrum pTs)
    {
        ITheoreticalSpectrum ret = filterTheoreticalScan(  pTs);
          return ret;
    }

     protected ITheoreticalSpectrum filterTheoreticalScan(ITheoreticalSpectrum pTs)
    {
         return pTs;
    }
}
