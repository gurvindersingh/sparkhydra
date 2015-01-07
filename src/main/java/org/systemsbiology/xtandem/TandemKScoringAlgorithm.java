package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.TandemKScoringAlgorithm
 * User: steven
 * Date: 2/2/11
 */
public class TandemKScoringAlgorithm extends TandemScoringAlgorithm {
    public static final TandemKScoringAlgorithm[] EMPTY_ARRAY = {};

    public static final String ALGORITHM_NAME = "KScore";

    public static final double MONOISOTOPIC_CORRECTION = 1.0005;
    public static final double AVERAGE_CORRECTION = 1.0011;
    public static final ITandemScoringAlgorithm DEFAULT_ALGORITHM = new TandemKScoringAlgorithm();
    public static final ITandemScoringAlgorithm[] DEFAULT_ALGORITHMS = { DEFAULT_ALGORITHM };


    protected static final IMZToInteger K_SCORING_CONVERTER = new KScoringConverter();
    private static double gdIsotopeCorrection = MONOISOTOPIC_CORRECTION;

    /**
     * implementation of how kScoring converts mz to an integer value
     */
    public static class KScoringConverter implements IMZToInteger {
        private KScoringConverter() {
        }


         /**
         * convera a double - usually an mz to an integer
         *
         * @param d the double - should be positive
         * @return the integer
         */
        @Override
        public int asInteger(final double pM1) {
            if (XTandemUtilities.isInteger(pM1))
                return (int) pM1;
            double m2 = pM1 / gdIsotopeCorrection;
            return (int) (m2 + 0.5);
        }

    }


    public static final int SLIDING_WINDOW_WIDTH_DALTONS = 50;
    public static final int MAX_MASS = 5000;

    private double m_HyperScale = 1.0; // 0.05;

    public TandemKScoringAlgorithm() {
    }

    @Override
     public String toString() {
         return  getName();
     }

    /**
     * return a unique algorithm name
     *
     * @return
     */
    @Override
    public String getName() {
        return ALGORITHM_NAME;
    }

    public double getHyperScale() {
        return m_HyperScale;
    }

    public void setHyperScale(double pHyperScale) {
        m_HyperScale = pHyperScale;
    }

    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    @Override
    public void configure(IParameterHolder params) {
        super.configure(params);


        setHyperScale(params.getDoubleParameter("k-score, histogram scale", getHyperScale()));


        switch (getMassType()) {
            case monoisotopic:
                gdIsotopeCorrection = MONOISOTOPIC_CORRECTION;
                break;
            case average:
                gdIsotopeCorrection = AVERAGE_CORRECTION;
                break;
        }


    }

    /**
     * k score uses a dalton identity
     *
     * @return as above
     */
    @Override
    public float getSpectrumMassError() {
        return (float) gdIsotopeCorrection;

    }

    /**
     * return the low and high limits of a mass scan
     *
     * @param scanMass
     * @return as above
     */
    @Override
    public int[] highAndLowMassLimits(double scanMass) {
        IMZToInteger defaultConverter = XTandemUtilities.getDefaultConverter();
        int[] ret = new int[2];
        // algorithm makes adjustments in the mass - this does the right thing
        // note this only looks backwards SLewis
        double plusLimit = getPlusLimit();
        double minusLimit = getMinusLimit();
        double d = scanMass - Math.abs(plusLimit) - 0.5;
        ret[0] = defaultConverter.asInteger(d);
        double d1 = scanMass + Math.abs( minusLimit) + 0.4;
        ret[1] = defaultConverter.asInteger(d1);

        return ret; // break here interesting result
    }


    /**
     * @param peak return a peak an an int with Isotope correction
     * @return as above
     */
    public static int massChargeRatioAsInt(ISpectrumPeak peak) {
        double m1 = peak.getMassChargeRatio();
        return K_SCORING_CONVERTER.asInteger(m1);
    }


    /**
     * alter the score from dot_product in algorithm depdndent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return new score
     */
    @Override
    public double conditionScore(double score, IMeasuredSpectrum measured,
                                 ITheoreticalSpectrumSet theory, IonUseScore counter) {
        /*
      * Multiply by log(length) to remove length dependence on dot product score
      * Divide by 3.0 to scale score to 1000
      */
        String sequence = theory.getPeptide().getSequence();
        return conditionRawScore(score, sequence);
    }

    /**
     * these are factors KScore applies to a score
     *
     * @param score
     * @param pSequence
     * @return
     */
    public static double conditionRawScore(final double score, final String pSequence) {
        if (score == 0)
            return 0;
        int sequenceLength = pSequence.length();
        double dFactor = Math.log((double) sequenceLength) * 1.0 /
                (3.0 * Math.sqrt(
                        (double) sequenceLength)); /* change iLenPeptide to tot # of fragment ions? */
        dFactor *= 1000.0;
        return score * dFactor;
    }

    /**
     * find the hyperscore the score from dot_product in algorithm depdndent manner
     *
     * @param score    original score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return hyperscore
     */
    @Override
    public double buildHyperscoreScore(double score, IMeasuredSpectrum measured,
                                       ITheoreticalSpectrumSet theory, IonUseScore counter) {
        double factor = getHyperScale();
        return score * factor;
    }

    public static final double DEFAULT_EXPECTED_VALUE = 100;

    /**
     * return the expected value for the best score
     *
     * @param scan !null scan
     * @return as above
     */
    public double getExpectedValue(final IScoredScan scan) {
        final ISpectralMatch bestMatch = scan.getBestMatch();
        if (bestMatch == null)
            return DEFAULT_EXPECTED_VALUE;
        double hyper = bestMatch.getHyperScore();
        double convertedScore = hconvert(hyper);
        final HyperScoreStatistics scores = scan.getHyperScores();
//        final double slope = scores.getSlope();
//        final double intercept = scores.getYIntercept();
//        final double proteinFactor = 1.0; // I cannot see other values set;
        double ret = scores.getExpectedValue(hyper); // Math.pow(10.0, (intercept + slope * convertedScore)) * proteinFactor;
        scan.setExpectedValue(ret);
        return ret;
    }

    /*
    * hconvert is use to convert a hyper score to the value used in a score
    * histogram, since the actual scoring distribution may not lend itself
    * to statistical analysis through use of a histogram.
    */

    public double hconvert(double _f) {
        if (_f <= 0.0)
            return 0.0;
        return (float) (0.05 * _f);
    }


    protected int getMaxMass(IMeasuredSpectrum sp, int iWindowCount) {
        int precursorCharge = sp.getPrecursorCharge();   // 2
        double precursorMass = sp.getPrecursorMass();
        if (precursorCharge == 0) {
            precursorCharge = 2;
        }
        int chargeM1 = precursorCharge - 1;
        int endMassMax = (int) (((precursorMass + chargeM1 * XTandemUtilities.getProtonMass()) / precursorCharge) * 2.0 + 0.5) + iWindowCount;
        return endMassMax;  // 2203
    }

    /**
     * test for acceptability and generate a new conditioned spectrum for
     * scoring
     *
     * @param in !null spectrum
     * @return null if the spectrum is to be ignored otherwise a conditioned spectrum
     */
    public IMeasuredSpectrum conditionSpectrum(IScoredScan scan, final SpectrumCondition sc) {
        IMeasuredSpectrum in = sc.conditionSpectrum(scan, 150);
        IMeasuredSpectrum raw = scan.getRaw();

        if (in == null) {
            in = sc.conditionSpectrum(scan, 150); // debug the issue
            return null;
        }
        IMeasuredSpectrum ret =  conditionSpectrum(in, raw);
        if (scan instanceof OriginatingScoredScan) {
            OriginatingScoredScan o = (OriginatingScoredScan) scan;
            o.setNormalizedRawScan(ret);
          }
         return ret;
    }

    public IMeasuredSpectrum conditionSpectrum(  final IMeasuredSpectrum pIn, final IMeasuredSpectrum pRaw) {
        final ISpectrumPeak[] spectrumPeaks = pIn.getPeaks();
        int precursorCharge = pIn.getPrecursorCharge();
        final double mass = pIn.getPrecursorMass();
        if (spectrumPeaks.length < 1) {
           return new ScoringMeasuredSpectrum(precursorCharge, pIn.getPrecursorMassChargeRatio(), pRaw.getScanData(), ISpectrumPeak.EMPTY_ARRAY);
       }

        //       double maxMass = spectrumPeaks[spectrumPeaks.length - 1].getMassChargeRatio();
        //       double minMass = spectrumPeaks[0].getMassChargeRatio();

        MutableSpectrumPeak[] mutablePeaks = new MutableSpectrumPeak[spectrumPeaks.length];


        // pick up the first peak > 1000;
        ISpectrumPeak lastPeak = null;

        // intensity = sqrt intensity

        double maxIntensity = 0;
        double totalIntensity = 0;
        for (int i = 0; i < spectrumPeaks.length; i++) {
            ISpectrumPeak test = spectrumPeaks[i];
            float sqrtIntensity = (float) Math.sqrt(test.getPeak());
            totalIntensity += sqrtIntensity;
            maxIntensity = Math.max(maxIntensity, sqrtIntensity);
            mutablePeaks[i] = new MutableSpectrumPeak(test.getMassChargeRatio(), sqrtIntensity);
        }

        float fMinCutoff = (float) (0.05 * maxIntensity);


        String id = pIn.getId();
        //   XTandemUtilities.outputLine("Normalizing " + id);
        mutablePeaks = normalizeWindows(pIn, mutablePeaks, maxIntensity, fMinCutoff);

        // should already be in mass order
        //    Arrays.sort(spectrumPeaks,ScoringUtilities.PeakMassComparatorINSTANCE);
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (int i = 0; i < mutablePeaks.length; i++) {
            MutableSpectrumPeak test = mutablePeaks[i];
            if (test.getPeak() > 0) {
                holder.add(test);
            }
            else {
                XTandemUtilities.breakHere();
            }
        }

        ISpectrumPeak[] newPeaks = new ISpectrumPeak[holder.size()];
        holder.toArray(newPeaks);

        final MutableMeasuredSpectrum spectrum = pIn.asMmutable();
        spectrum.setPeaks(newPeaks);
//             sc.doWindowedNormalization(spectrum);
//         if (XTandemDebugging.isDebugging()) {
//             XTandemDebugging.getLocalValues().addMeasuredSpectrums(ret.getId(), "after Perform mix-range modification", ret);
//         }
        final IMeasuredSpectrum ret = spectrum.asImmutable();

        return new ScoringMeasuredSpectrum(precursorCharge, spectrum.getPrecursorMassChargeRatio(), pRaw.getScanData(), ret.getPeaks());
    }

    /*
    * Perform mix-range modification to input spectrum
    * this is done in mkscore
    */

    protected static MutableSpectrumPeak[] doWindowedNormalization(
            MutableSpectrumPeak[] mutablePeaks) {
        if (mutablePeaks.length == 0)
            return mutablePeaks;
        List<MutableSpectrumPeak> holder = new ArrayList<MutableSpectrumPeak>();
        int[] lowerAndUpperIndices = new int[2];

        double sum = 0;
        double[] decreases = new double[mutablePeaks.length];
        for (int i = 0; i < mutablePeaks.length; i++) {
            sum = makeWindowedSum(i, lowerAndUpperIndices, mutablePeaks);
            final double decrease = sum / 101.0;
            decreases[i] = decrease;
        }


        for (int i = 0; i < mutablePeaks.length; i++) {
            MutableSpectrumPeak next = mutablePeaks[i];
            final float old = next.getPeak();
            final double newValue = old - decreases[i];
            if (newValue > 0) {
                MutableSpectrumPeak newPeak = new MutableSpectrumPeak(next.getMassChargeRatio(),
                        (float) newValue);
                //                   XTandemUtilities.outputLine("keeping peak " + next.getMassChargeRatio() + " decrease " + decrease)  ;
                holder.add(newPeak);
            }
            else {
                XTandemUtilities.breakHere();
                //                XTandemUtilities.outputLine("dropping peak " + next.getMassChargeRatio());
            }
        }

        MutableSpectrumPeak[] newPeaks = new MutableSpectrumPeak[holder.size()];
        holder.toArray(newPeaks);
        return newPeaks;
    }

    private static double makeWindowedSum(int pI, int[] lowerAndUpperIndices,
                                          ISpectrumPeak[] pSpectrumPeaks) {
        ISpectrumPeak peak = pSpectrumPeaks[pI];
        double mz = peak.getMassChargeRatio();
        while (lowerAndUpperIndices[0] < pI) {
            ISpectrumPeak firstPeak = pSpectrumPeaks[lowerAndUpperIndices[0]];
            if (firstPeak.getMassChargeRatio() > mz - 50)
                break;
            lowerAndUpperIndices[0]++;
        }
        lowerAndUpperIndices[1] = Math.max(lowerAndUpperIndices[1], pI);
        while (lowerAndUpperIndices[1] < pSpectrumPeaks.length) {
            ISpectrumPeak next = pSpectrumPeaks[lowerAndUpperIndices[1]];
            if (next.getMassChargeRatio() > mz + 50)
                break;
            lowerAndUpperIndices[1]++;

        }
        lowerAndUpperIndices[1] = Math.min(pSpectrumPeaks.length, lowerAndUpperIndices[1]);
        double sum = 0;
        for (int i = lowerAndUpperIndices[0]; i < lowerAndUpperIndices[1]; i++) {
            sum += pSpectrumPeaks[i].getPeak();

        }
        return sum;
    }


    /*
    * sfactor returns a factor applied to the final convolution score.
    * multiply the dot product output by this
    */

    protected double getScoreFactor(int seqLength) {
        /*
        * Multiply by log(length) to remove length dependence on dot product score
        * Divide by 3.0 to scale score to 1000
        */
        double dFactor = Math.log((double) seqLength) * 1.0 /
                (3.0 * Math.sqrt(
                        (double) seqLength)); /* change iLenPeptide to tot # of fragment ions? */
        dFactor *= 1000.0;
        return dFactor;
    }


    protected MutableSpectrumPeak[] normalizeWindows(IMeasuredSpectrum spec,
                                                     MutableSpectrumPeak[] allPeaks,
                                                     double maxIntensity,
                                                     float fMinCutoff) {
        double minMass = allPeaks[0].getMassChargeRatio();


        if (XTandemDebugging.isDebugging()) {
            XTandemDebugging.getLocalValues().addMeasuredSpectrums(spec, "sqrt modification",
                    allPeaks);
        }

        // Normalize windows
        int iWindowCount = 10;

        // drop masses too close for quantization
        allPeaks = dropMassDuplicates(allPeaks);
        // Screen peeks on upper end.
        allPeaks = dropTooHighMasses(allPeaks, spec, iWindowCount);

        if (allPeaks.length == 0)
            return MutableSpectrumPeak.EMPTY_ARRAY;

        // Should this be the filtered or the unfiltered max mass
        double maxMass = allPeaks[allPeaks.length - 1].getMassChargeRatio();

        int range = iWindowCount + (int) (maxMass - minMass);
        iWindowCount = findWindowCount(range);
        int iWindowSize = (int) ((((double) range) / iWindowCount));

        int startValue = -1;
        int endValue = -1;
        int lastEnd = 0;
        int startMass = (int) (minMass + 0.5);
        // define a window
        while (startMass < maxMass) {
            final int endMass = startMass + iWindowSize;
            double endmass = 0;
            for (int i = lastEnd; i < allPeaks.length; i++) {
                MutableSpectrumPeak allPeak = allPeaks[i];
                double mz = allPeak.getMassChargeRatio();
                int iMz = massChargeRatioAsInt(allPeak);
                if (startValue == -1 && iMz >= startMass)
                    startValue = i;
                endValue = i;
                if (iMz >= endMass) {
                    endmass = mz;
                    break;
                }
            }
            if ((endValue == allPeaks.length - 1) && allPeaks[endValue].getMassChargeRatio() <= endMass) {
                endValue = allPeaks.length;
            }

            if (startValue < 0)
                return MutableSpectrumPeak.EMPTY_ARRAY;

            double startmass = allPeaks[startValue].getMassChargeRatio();
            //     XTandemUtilities.outputLine("StartMass=" + startMass + " EndMass=" + endMass);
            normalizeWindow(allPeaks, startValue, endValue, fMinCutoff,
                    maxIntensity);
            lastEnd = endValue;
            startValue = -1;
            endValue = -1;
            startMass = endMass;
        }
        /*
        * Reduce intensity and make unit vector by dividing
        * every point by sqrt(sum(x^2))
        */
        double dSpectrumArea = 0.0;
        for (int i = 0; i < allPeaks.length; i++) {
            final MutableSpectrumPeak allPeak = allPeaks[i];
            final double thisPeak = allPeak.getPeak();
            dSpectrumArea += thisPeak * thisPeak;
            // to debug a tough issue with the results
            if (false)
                XMLUtilities.outputLine("mass=" + (int) allPeak.getMassChargeRatio() +
                        " peak=" + XTandemUtilities.formatDouble(thisPeak, 5) +
                        " number=" + i +
                        " sum=" + XTandemUtilities.formatDouble(dSpectrumArea, 5)
                );
        }

        double areaSqrt = Math.sqrt(dSpectrumArea);
        //      XTandemUtilities.outputLine("dSpectrumArea=" + dSpectrumArea + " areaSqrt"  + areaSqrt);

        for (int i = 0; i < allPeaks.length; i++) {
            final MutableSpectrumPeak allPeak = allPeaks[i];
            final float thisPeak = allPeak.getPeak();
            float newf = (float) Math.max(0, thisPeak / areaSqrt);
            allPeak.setPeak(newf);
        }

        if (XTandemDebugging.isDebugging()) {
            XTandemDebugging.getLocalValues().addMeasuredSpectrums(spec,
                    "before Perform mix-range modification", allPeaks);
        }

        /*
      * Perform mix-range modification to input spectrum
      */
        allPeaks = doWindowedNormalization(allPeaks);
        if (XTandemDebugging.isDebugging()) {
            XTandemDebugging.getLocalValues().addMeasuredSpectrums(spec, "add_mi_conditioned",
                    allPeaks);
        }

        // XCorr sliding window normalization
        // this is NOT part of k scoring
        //performSlidingWindowNormalization(allPeaks, SLIDING_WINDOW_WIDTH_DALTONS);

        return allPeaks;

    }

    /**
     * drop high masses
     *
     * @param allPeaks     original peak list
     * @param spec         spectrum for precursor charge and mass
     * @param iWindowCount seems to be 10 always
     * @return new filtered peak list
     */
    protected MutableSpectrumPeak[] dropTooHighMasses(final MutableSpectrumPeak[] allPeaks, final IMeasuredSpectrum spec, final int iWindowCount) {
        int endMassMax = getMaxMass(spec, iWindowCount);
        List<MutableSpectrumPeak> holder = new ArrayList<MutableSpectrumPeak>();
        for (int i = 0; i < allPeaks.length; i++) {
            MutableSpectrumPeak allPeak = allPeaks[i];
            if (allPeak.getMassChargeRatio() <= endMassMax)
                holder.add(allPeak);
        }

        MutableSpectrumPeak[] ret = new MutableSpectrumPeak[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    private MutableSpectrumPeak[] dropMassDuplicates(final MutableSpectrumPeak[] pks) {
        List<MutableSpectrumPeak> holder = new ArrayList<MutableSpectrumPeak>();
        Map<Integer, MutableSpectrumPeak> seenMasses = new HashMap<Integer, MutableSpectrumPeak>();
        for (int i = 0; i < pks.length; i++) {
            MutableSpectrumPeak k = pks[i];
            int value = this.indexFromMass(k.getMassChargeRatio());
            float pk = k.getPeak();
            float peak = pk; // (float)Math.sqrt(pk);
            //         float peak =  k.getPeak();
            if (seenMasses.containsKey(value)) {
                MutableSpectrumPeak oldPeak = seenMasses.get(value);
                float newPeak = Math.max(oldPeak.getPeak(), k.getPeak());
                oldPeak.setPeak(newPeak);
                // this is what XTandem does - replace a duplicate peak
                //  holder.get(holder.size() - 1).setPeak(peak);
            }
            else {
                k.setPeak(peak);
                holder.add(k);
                seenMasses.put(value, k);
            }

        }
        MutableSpectrumPeak[] ret = new MutableSpectrumPeak[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * this is a sliding window normalization similar to that used by sequest
     *
     * @param allPeaks the peaks
     * @param width    - window width - k-score uses 50 apparently seaquest uses 75
     */
    protected void performSlidingWindowNormalization(MutableSpectrumPeak[] allPeaks,
                                                     int width) {
        double[] peakAtMass = new double[MAX_MASS];
        for (int i = 0; i < allPeaks.length; i++) {
            final MutableSpectrumPeak pk = allPeaks[i];
            int index = indexFromMass(pk.getMassChargeRatio());
            if (index < MAX_MASS)
                peakAtMass[index] = pk.getPeak();
        }
        // subtract the intensity  at near masses
        for (int i = 0; i < allPeaks.length; i++) {
            final MutableSpectrumPeak pk = allPeaks[i];
            int index = indexFromMass(pk.getMassChargeRatio());
            int start = Math.max(0, index - width);
            int end = Math.min(MAX_MASS, index + width + 1);
            double sum = 0;
            for (int j = Math.max(0, start); j < Math.min(peakAtMass.length, end); j++) {
                sum += peakAtMass[j];

            }
            sum /= ((2 * width) + 1);
            double value = pk.getPeak();
            value -= sum;
            value = Math.max(0, value);
            if (false)
                XMLUtilities.outputLine("" + index +
                        " " + XTandemUtilities.formatDouble(value, 4) +
                        " old " + XTandemUtilities.formatDouble(pk.getPeak(), 4) +
                        " decrease " + sum
                );

            pk.setPeak((float) value);
        }

    }

    /**
     * find the size of a window in peaks
     *
     * @param pRange
     * @return
     */
    private int findWindowCount(int pRange) {
        int iWindowCount;
        if (pRange > 3000)
            iWindowCount = 10;
        else if (pRange > 2500)
            iWindowCount = 9;
        else if (pRange > 2000)
            iWindowCount = 8;
        else if (pRange > 1500)
            iWindowCount = 7;
        else if (pRange > 1000)
            iWindowCount = 6;
        else
            iWindowCount = 5;
        return iWindowCount;
    }


    protected void normalizeWindow(MutableSpectrumPeak[] allPeaks, int startValue, int endValue,
                                   float fMinCutoff, double maxIntensity) {
        float maxPeak = 0;

        /*
        * Get maximum intensity within window
        */
        double mass = 0;
        for (int i = startValue; i < endValue; i++) {
            final MutableSpectrumPeak allPeak = allPeaks[i];
            final float thisPeak = allPeak.getPeak();
            mass = allPeak.getMassChargeRatio(); // for debugging
            maxPeak = Math.max(maxPeak, thisPeak);
        }


        if (maxPeak > 0.0 && maxPeak > fMinCutoff) {
            double dFactor = maxIntensity / maxPeak;

            /*
             * Normalize within window
             */
            for (int i = startValue; i < endValue; i++) {
                final MutableSpectrumPeak allPeak = allPeaks[i];

                // debug code
                double onemass = allPeak.getMassChargeRatio();  // I need to look at this
                if (onemass > 294 && onemass < 296)
                    XTandemUtilities.breakHere();

                float peak = allPeak.getPeak();
                final float normalizedPeak = (float) (peak * dFactor);
                //     XTandemUtilities.outputLine("i=" + i + " mass=" + onemass  + " original=" + peak +  " adjusted=" + normalizedPeak +  " dFactor=" + dFactor  );
                allPeak.setPeak(normalizedPeak);

            }
        }

    }

    /**
     * sometimes we do not score against all peaks
     *
     * @param pTs filter a theoretical spectrum in an  algprithm specific way
     * @return !null filtered spectrum
     */
    protected ITheoreticalSpectrum filterTheoreticalScan(ITheoreticalSpectrum pTs) {
        boolean yOnly = pTs.getCharge() > 1; // this is what XTandem does

        // not sure why IO did this but it seems XTandem is not
        if (true)
            return pTs;

        ITheoreticalPeak[] pTps = pTs.getTheoreticalPeaks();
        int firstSavedPeak = 0;
        // See line 560 in mscore.cpp - for these high charges only the   last 4 peaks are scored
        // Slewis
        if (yOnly) {
            // save only the largest Y peaks if charge > 1
            // This is what k_score on XTandem does
            int numberYPeaks = 0;
            for (int i = pTps.length - 1; i >= 0; i--) {
                ITheoreticalPeak tp = pTps[i];
                if (tp.getType() == IonType.Y) {
                    numberYPeaks++;
                    if (numberYPeaks >= 4) {
                        firstSavedPeak = i;
                        break;
                    }
                }

            }
        }
        List<ITheoreticalPeak> holder = new ArrayList<ITheoreticalPeak>();
        for (int i = 0; i < pTps.length; i++) {
            ITheoreticalPeak pTp = pTps[i];
            if (yOnly && pTp.getType() != IonType.Y)
                continue;
            // only use the last 4 peaks in high charge states
            // See line 560 in mscore.cpp - for these high charges only the   last 4 peaks are scored
            if (i < firstSavedPeak)
                continue;
            holder.add(pTp);

        }
        ITheoreticalPeak[] newPeaks = new ITheoreticalPeak[holder.size()];
        holder.toArray(newPeaks);
        return new MutableScoringSpectrum(pTs.getCharge(), pTs.getSpectrumSet(), newPeaks);
    }


    public static final double SCORED_MASS_DIFFERENCE = 2.0;


    /**
     * Cheat by rounding mass to the nearest int and limiting to MAX_MASS
     * then just generate arrays of the masses and multiply them all together
     *
     * @param measured  !null measured spectrum
     * @param theory    !null theoretical spectrum
     * @param counter   !null use counter
     * @param holder    !null holder to receive matched peaks
     * @param otherData anythiing else needed   in this case [] peaksByMass,  SpectralPeakUsage usage
     * @return comupted dot product
     */
    @Override
    public double dot_product(IMeasuredSpectrum measured, ITheoreticalSpectrum theory,
                              IonUseCounter counter, List<DebugMatchPeak> holder,
                              Object... otherData) {
        double[] peaksByMass = (double[]) otherData[0];
        SpectralPeakUsage usage = (SpectralPeakUsage) otherData[1];    // marks peaks as being already used in this calculation
//        double[] used = (double[]) otherData[1];    // marks peaks as being already used in this calculation
//        Set<ISpectrumPeak> alreadyUsed = (Set<ISpectrumPeak>) otherData[2]; // used peaks
        final ITheoreticalPeak[] tps = theory.getTheoreticalPeaks();
        if (tps.length == 0)
            return 0;
        int charge = theory.getCharge();

        // debugging why are we not handling higher charges right
        if (charge > 1)
            XTandemUtilities.breakHere();


        double product = 0;
        // for each theoretical peak
        for (int i = 0; i < tps.length; i++) {
            ITheoreticalPeak tp = tps[i];
            double mz = tp.getMassChargeRatio();
            int imass = massChargeRatioAsInt(tp);  // convert to index in peaksByMass array

            // if the index is too big do not handle the peak
            if (imass >= peaksByMass.length - 1)      // because we use imass + 1
                continue;

            double directValue = peaksByMass[imass];
            IonType type = tp.getType();

            if (charge > 1 && type == IonType.B) {
                XTandemUtilities.breakHere();
//                continue; // todo why
            }

            double added = 0;
            int del = 0;
            boolean directPeakAdded = false;
            if (directValue != 0) {
                del = 0;  // no offset
                added = usage.getAddedAfterUsageCorrection(mz, directValue);
                product += scoreAddPeakScore(counter, holder, imass, type, added, del);
                 directPeakAdded = true;
            }
            // test off by  -1
            double delPlus = peaksByMass[imass + 1];
            if (delPlus != 0) {
                added = delPlus * 0.5; // off by 1 counts as half
                del = -1;
                added = usage.getAddedAfterIntegerUsageCorrection(mz, added, imass + 1);
                product += scoreAddPeakScore(counter, holder, imass, type, added, del);
            }


            // test off by   1
            double delMinus1 = peaksByMass[imass - 1];
            if (delMinus1 != 0) {
                added = delMinus1 * 0.5;
                del = 1;       // off by 1 counts as half
                added = usage.getAddedAfterIntegerUsageCorrection(mz, added, imass - 1);
                product += scoreAddPeakScore(counter, holder, imass, type, added, del);
            }

        }

        return (product);
    }

    protected double scoreAddPeakScore(final IonUseCounter counter, final List<DebugMatchPeak> holder, final int pImass, final IonType pType, final double pAdded, final int pDel) {
        if (pAdded == 0)
            return 0;

        DebugMatchPeak match = new DebugMatchPeak(pDel, pAdded, pImass - pDel, pType);


        holder.add(match);

        counter.addScore(pType, pAdded);
        if (pDel == 0)  // only count direct hits
            counter.addCount(pType);
        return pAdded;
    }
//
//    protected double possiblyScoreMatch2(ITheoreticalPeak tp, ISpectrumPeak mp,
//                                         IonUseCounter counter, List<DebugMatchPeak> pMatches) {
//        double product = 0;
//
//        // well I thought they did not score single amino acids but they do
//        //  if (tp.getPeptide().getNumberPeptideBonds() == 0)
//        //       return 0;
//
//
//        // for higher charges only y peaks are scored
//        // See line 560 in mscore.cpp - for these high charges only the   last 4 peaks are scored
//        // Slewis
//        final double theoryMass = tp.getMassChargeRatio();
//        final double measuredMass = mp.getMassChargeRatio();
//
//        //     XTandemUtilities.outputLine("measured=" + XTandemUtilities.formatDouble(measuredMass, 2) +
//        //         "theory=" + XTandemUtilities.formatDouble(theoryMass, 2)
//        //    );
//
//        if (theoryMass > 1875 && theoryMass < 1885 && measuredMass > 1875 && measuredMass < 1885)
//            XTandemUtilities.breakHere();
//
//        int theoryIndex = indexFromMass(theoryMass);
//        int measuredIndex = indexFromMass(measuredMass);
//
//        int diff = theoryIndex - measuredIndex;
//        final int absDiff = Math.abs(diff);
//        if (absDiff > 1)
//            return 0; // too far apart;
//
//        product = mp.getPeak();
//        final IonType type = tp.getType();
//        if (diff != 0) {
//            product *= 0.5; // off by 1 counts half
//
//        }
//        else {
//            counter.addCount(type);
//
//        }
//        counter.addScore(type, product);
//        if (pMatches != null) {
//            DebugMatchPeak match = new DebugMatchPeak(diff, product, measuredIndex, type);
//            pMatches.add(match);
//        }
//
//        return product;
//    }
//
//    /**
//     * Cheat by rounding mass to the nearest int and limiting to MAX_MASS
//     * then just generate arrays of the masses and multiply them all together
//     *
//     * @param measured  !null measured spectrum
//     * @param theory    !null theoretical spectrum
//     * @param counter   !null use counter
//     * @param holder    !null holder tro receive matched peaks
//     * @param otherData anythiing else needed
//     * @return comupted dot product
//     */
//    // @Override
//    public double dot_productOld(IMeasuredSpectrum measured, ITheoreticalSpectrum theory,
//                                 IonUseCounter counter, List<DebugMatchPeak> holder,
//                                 Object... otherData) {
//        double[] peaksByMass = (double[]) otherData[0];
//        final ISpectrumPeak[] mps = measured.getPeaks();
//        if (mps.length == 0)
//            return 0;
//        final ITheoreticalPeak[] tps = theory.getTheoreticalPeaks();
//        if (tps.length == 0)
//            return 0;
//        int charge = theory.getCharge();
//
//        // debugging why are we not handling higher charges right
//        if (charge > 1)
//            XTandemUtilities.breakHere();
//
//
//        double product = 0;
//        int measuredIndex = 0;
//        for (int theoryIndex = 0; theoryIndex < tps.length; theoryIndex++) {
//            ITheoreticalPeak tp = tps[theoryIndex];
//            final double theoreticalMass = tp.getMassChargeRatio();
//            ISpectrumPeak measuredStart = mps[measuredIndex];
//            double measuredMass = measuredStart.getMassChargeRatio();
//            while (measuredMass < theoreticalMass - SCORED_MASS_DIFFERENCE) {
//                measuredIndex++;
//                if (measuredIndex >= mps.length)
//                    break;   // out of data
//                measuredStart = mps[measuredIndex];
//                measuredMass = measuredStart.getMassChargeRatio();
//                //   XTandemUtilities.outputLine("measured=" + XTandemUtilities.formatDouble(measuredMass, 2));
//            }
//
//            for (int i = measuredIndex; i < mps.length; i++) {
//                ISpectrumPeak someMeasured = mps[i];
//                measuredMass = someMeasured.getMassChargeRatio();
//                if (measuredMass > theoreticalMass + SCORED_MASS_DIFFERENCE)
//                    break;
//                product += possiblyScoreMatch(tp, someMeasured, counter, holder);
//            }
//            if (measuredIndex >= mps.length)
//                break;   // out of data
//        }
//
//
//        if (false && counter.getNumberMatchedPeaks() > 0)
//            XTandemUtilities.outputLine("dot_product=" + XTandemUtilities.formatDouble(product, 3) +
//                    " id=" + measured.getId() +
//                    " sequence=" + theory.getPeptide().getSequence() +
//                    " charge=" + charge +
//                    " Y_Count=" + counter.getCount(IonType.Y) +
//                    " Y_Score=" + XTandemUtilities.formatDouble(counter.getScore(IonType.Y), 3) +
//                    " B_Count=" + counter.getCount(IonType.B) +
//                    " B_Score=" + XTandemUtilities.formatDouble(counter.getScore(IonType.B), 3)
//            );
//        return (product);
//    }

    protected double possiblyScoreMatch(ITheoreticalPeak tp, ISpectrumPeak mp,
                                        IonUseCounter counter, List<DebugMatchPeak> pMatches) {
        double product = 0;

        // well I thought they did not score single amino acids but they do
        //  if (tp.getPeptide().getNumberPeptideBonds() == 0)
        //       return 0;


        // for higher charges only y peaks are scored
        // See line 560 in mscore.cpp - for these high charges only the   last 4 peaks are scored
        // Slewis
        final double theoryMass = tp.getMassChargeRatio();
        final double measuredMass = mp.getMassChargeRatio();

        //     XTandemUtilities.outputLine("measured=" + XTandemUtilities.formatDouble(measuredMass, 2) +
        //         "theory=" + XTandemUtilities.formatDouble(theoryMass, 2)
        //    );

        if (theoryMass > 1875 && theoryMass < 1885 && measuredMass > 1875 && measuredMass < 1885)
            XTandemUtilities.breakHere();

        int theoryIndex = indexFromMass(theoryMass);
        int measuredIndex = indexFromMass(measuredMass);

        int diff = theoryIndex - measuredIndex;
        final int absDiff = Math.abs(diff);
        if (absDiff > 1)
            return 0; // too far apart;

        product = mp.getPeak();
        final IonType type = tp.getType();
        if (diff != 0) {
            product *= 0.5; // off by 1 counts half

        }
        else {
            counter.addCount(type);

        }
        counter.addScore(type, product);
        if (pMatches != null) {
            DebugMatchPeak match = new DebugMatchPeak(diff, product, measuredIndex, type);
            pMatches.add(match);
        }

        return product;
    }


    @Override
    protected boolean setTheoreticalPeaks(final int charge, final double[] pTheoreticalHolder,
                                          final ITheoreticalPeak[] pTheoreticalPeaks,
                                          final ITheoreticalPeak[] pTps) {
        boolean addedPeak = false;


        for (int i = 0; i < pTps.length; i++) {

            ITheoreticalPeak tp = pTps[i];
            if (tp.getPeptide().getNumberPeptideBonds() == 0)
                continue; // drop single amino acids

            // for higher charges only y peaks are scored
            // See line 560 in mscore.cpp - for these high charges only the   last 4 peaks are scored
            // Slewis
            final double mass = tp.getMassChargeRatio();
            int massIndex = indexFromMass(mass);
            if (massIndex < MAX_MASS) {
                pTheoreticalHolder[massIndex] += tp.getPeak();
                pTheoreticalPeaks[massIndex] = tp;
                addedPeak = true;
            }
        }
        return addedPeak;
    }

    public static final double OFF_BY_ONE_SCORE_FACTOR = 0.5;

    /**
     * unused by the sparse algorithm
     *
     * @param start
     * @param end
     * @param counter
     * @param theoreticalHolder
     * @param measuredHolder
     * @param pTheoreticalPeak
     * @param matches
     * @return dot product
     */
    @Override
    protected double computeDotProduce(int start, int end, IonUseCounter counter,
                                       final double[] theoreticalHolder,
                                       final double[] measuredHolder,
                                       ITheoreticalPeak[] pTheoreticalPeak,
                                       List<DebugMatchPeak> matches
    ) {
        boolean showScoring = ScoringUtilities.SHOW_SCORING || true;
        int numberMatches = 0;
        double product = 0;
        StringBuilder[] matchStrings = new StringBuilder[IonType.NUMBER_ION_TYPES];
        for (int q = 0; q < matchStrings.length; q++) {
            matchStrings[q] = new StringBuilder();
        }

        ITheoreticalPeak usedPeak;
        for (int i = start; i < end; i++) {
            usedPeak = null;
            double scoreFactor = 1; // full count for score - off by 1 reduces by half
            boolean useCount = true; // do not use count on near misses

            ITheoreticalPeak testPeak = pTheoreticalPeak[i];
            if (testPeak == null)
                continue;
            double measuredPeak = measuredHolder[i];    // k score does not weigh measured peaks


            // look for a match +/- one mass
            if (measuredPeak != 0) {
                usedPeak = testPeak;
            }
            else {
                useCount = false; // off by 1 is not a hit
                scoreFactor = OFF_BY_ONE_SCORE_FACTOR; // derate the score

                // allow off -1
                if (pTheoreticalPeak[i - 1] == null && measuredHolder[i - 1] != 0) {
                    usedPeak = testPeak;
                    measuredPeak = measuredHolder[i - 1];    // k score does not weigh measured peaks
                }
                else {
                    // allow off +1
                    if (pTheoreticalPeak[i + 1] == null && measuredHolder[i + 1] != 0) {
                        usedPeak = testPeak;
                        measuredPeak = measuredHolder[i + 1];    // k score does not weigh measured peaks
                    }
                }
            }
            if (usedPeak == null)
                continue; // no match

            numberMatches++;
            // This section is for debugging
            double mass = massFromIndex(i);
            double peakMass = usedPeak.getMassChargeRatio();


            //     final double value = theoryPeak * measuredPeak;
            // all theoretical values are 1
            double value = measuredPeak * scoreFactor;

            // track type and score for each ion type
            final IonType type = usedPeak.getType();
            if (useCount)
                counter.addCount(type);
            counter.addScore(type, value);
            product += value;
            int offset = 0;

            if (showScoring /* && IonType.B == type */) {
                DebugMatchPeak mp = new DebugMatchPeak(offset, value, i, type);
                matches.add(mp);
                matchStrings[IonType.asIndex(type)].append(
                        "matched " + i + " " + usedPeak + " value=" + XTandemUtilities.formatDouble(
                                value, 5) + "\n");
            }

            // if we have a real match retest for close
            if (useCount) {
                ITheoreticalPeak addedUsedPeak = null;
                // allow off -1
                if (pTheoreticalPeak[i - 1] == null && measuredHolder[i - 1] != 0) {
                    addedUsedPeak = testPeak;
                    measuredPeak = measuredHolder[i - 1];
                    measuredHolder[i - 1] = 0;    // clear used value
                    offset = -1;
                }
                else {
                    // allow off +1
                    if (pTheoreticalPeak[i + 1] == null && measuredHolder[i + 1] != 0) {
                        addedUsedPeak = testPeak;
                        measuredPeak = measuredHolder[i + 1];
                        measuredHolder[i + 1] = 0;    // clear used value
                        offset = 1;
                    }
                }
                if (addedUsedPeak != null) {
                    value = measuredPeak * OFF_BY_ONE_SCORE_FACTOR;
                    counter.addScore(type, value);
                    product += value;
                    if (showScoring /* && IonType.B == type */) {
                        DebugMatchPeak mp = new DebugMatchPeak(offset, value, i, type);
                        matches.add(mp);
                        matchStrings[IonType.asIndex(type)].append(
                                "matched special " + i + " " + usedPeak + " value=" + XTandemUtilities.formatDouble(
                                        value, 5) + "\n");
                    }
                }

            }


        }
        if (showScoring /* && IonType.B == type */) {
            for (IonType type : IonType.values()) {
                final StringBuilder sb = matchStrings[IonType.asIndex(type)];
                if (sb.length() > 0) {
                    XMLUtilities.outputLine(type + "Ions");
                    XMLUtilities.outputLine(sb.toString());
                }
            }

        }

        return product;
    }

}
