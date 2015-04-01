package org.systemsbiology.xtandem.comet;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.comet.CometScoringAlgorithm
 * User: Steve
 * Date: 1/12/12
 */
public class CometScoringAlgorithm extends AbstractScoringAlgorithm {

    public static final CometScoringAlgorithm[] EMPTY_ARRAY = {};
    public static final int NUMBER_ION_TYPES = 2; // B and I

    public static final ITandemScoringAlgorithm DEFAULT_ALGORITHM = new CometScoringAlgorithm();
    public static final ITandemScoringAlgorithm[] DEFAULT_ALGORITHMS = {DEFAULT_ALGORITHM};

    public static final int MAX_MASS = 5000;

    public static final double PPM_UNITS_FACTOR = 1000000;


    public static final double LOG_PI = Math.log(Math.sqrt(2 * Math.PI));
    public static final double NORMALIZATION_MAX = 100.0;
    public static final double DEFAULT_PEPTIDE_ERROR = 1.0;
    public static final double DEFAULT_MASS_TOLERANCE = 1; // tood fix 0.025;
    public static final int DEFAULT_MINIMUM_PEAK_NUMBER = 5; // do not score smaller spectra
    public static final int PEAK_BIN_SIZE = 100;   // the maxium peak is this
    public static final int PEAK_BIN_NUMBER = 3;
    public static final int PEAK_NORMALIZATION_BINS = 5;
    public static final int MINIMUM_SCORED_IONS = 10;

    public static final double DEFAULT_BIN_WIDTH = 0.03;
    public static final double DEFAULT_BIN_OFFSET = 0.00;

    public static final String ALGORITHM_NAME = "Comet";


    private double m_PeptideError = DEFAULT_PEPTIDE_ERROR;
    private double m_MassTolerance = DEFAULT_MASS_TOLERANCE;
    private int m_MinimumNumberPeaks = DEFAULT_MINIMUM_PEAK_NUMBER;
    private double m_UnitsFactor = 1; // change if ppm


    private float[] m_Weightsx;
    private IMeasuredSpectrum m_Spectrum;
    private BinnedMutableSpectrum m_BinnedSpectrum;
    private double m_TotalIntensity;
    private double m_BinTolerance = DEFAULT_BIN_WIDTH;
    private double m_InverseBinWidth = 1.0 / m_BinTolerance;
    private double m_BinStartOffset = DEFAULT_BIN_OFFSET;
    private double m_OneMinusBinOffset = 1.0 - m_BinStartOffset;


//    if (g_StaticParams.tolerances.dFragmentBinSize == 0.0)
//       g_StaticParams.tolerances.dFragmentBinSize = DEFAULT_BIN_WIDTH;
//
//    // Set dInverseBinWidth to its inverse in order to use a multiply instead of divide in BIN macro.
//    g_StaticParams.dInverseBinWidth = 1.0 /g_StaticParams.tolerances.dFragmentBinSize;
//    g_StaticParams.dOneMinusBinOffset = 1.0 - g_StaticParams.tolerances.dFragmentBinStartOffset;
//

    public CometScoringAlgorithm() {
    }

    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    @Override
    public void configure(final IParameterHolder params) {
        super.configure(params);
        final String units = params.getParameter("spectrum, parent monoisotopic mass error units",
                "Daltons");
        if ("ppm".equalsIgnoreCase(units)) {
            m_UnitsFactor = PPM_UNITS_FACTOR;
            setMinusLimit(-0.5);      // the way the databasse works better send a wider limits
            setPlusLimit(0.5);
        }
        m_BinTolerance = params.getDoubleParameter("comet.fragment_bin_tol", DEFAULT_BIN_WIDTH);
        m_InverseBinWidth = 1.0 / m_BinTolerance;

        m_BinStartOffset = params.getDoubleParameter("comet.fragment_bin_offset", DEFAULT_BIN_OFFSET);
        m_OneMinusBinOffset = 1.0 - m_BinStartOffset;


        m_MassTolerance = params.getDoubleParameter("comet.mass_tolerance", DEFAULT_MASS_TOLERANCE);
        //    if (g_StaticParams.tolerances.dFragmentBinSize == 0.0)
        //       g_StaticParams.tolerances.dFragmentBinSize = DEFAULT_BIN_WIDTH;
        //
        //    // Set dInverseBinWidth to its inverse in order to use a multiply instead of divide in BIN macro.
        //    g_StaticParams.dInverseBinWidth = 1.0 /g_StaticParams.tolerances.dFragmentBinSize;
        //    g_StaticParams.dOneMinusBinOffset = 1.0 - g_StaticParams.tolerances.dFragmentBinStartOffset;
        //

    }

    /**
     * actually do the scorring
     *
     * @param scorer !null scorrer
     * @param scan   !null scan to score
     * @param pPps   !null set of peptides ot score
     * @return !null score
     */
    @Override
    public IScoredScan handleScan(final Scorer scorer, final IMeasuredSpectrum scan, final IPolypeptide[] pPps, ITheoreticalSpectrumSet[] tss) {
        String id = scan.getId();
        //        for (int i = 0; i < pPps.length; i++) {
        //            IPolypeptide pp = pPps[i];
        //            System.out.println("      \"" + pp + "\"," + " //" + id);
        //        }
        CometScoredScan scoring = new CometScoredScan(scan, this);
        windowedNormalize(scoring.getBinnedPeaks());
        IonUseCounter counter = new IonUseCounter();

        int numberDotProducts = scoreScan(scorer, counter, tss, scoring);
        return scoring;
    }

    /**
     * @param scorer
     * @param sa
     * @param pCounter
     * @param pSpectrums
     * @param pConditionedScan
     * @return
     */
    @Override
    public int scoreScan(final Scorer scorer, final IonUseCounter pCounter, final ITheoreticalSpectrumSet[] pSpectrums, final IScoredScan conditionedScan) {
        CometScoredScan pConditionedScan = (CometScoredScan) conditionedScan;
        float[] binnedPeaks = pConditionedScan.getBinnedPeaks();


        if(true)
            throw new UnsupportedOperationException("Fix This"); // ToDo

        int numberScoredSpectra = 0;
        SpectrumCondition sc = scorer.getSpectrumCondition();
        IMeasuredSpectrum scan = pConditionedScan.conditionScan(this, sc);

        // DEBUGGING CODE
        IPolypeptide pp = pSpectrums[0].getPeptide();
        IMeasuredSpectrum scn = pConditionedScan.getRaw();
        boolean logCalculations = TestUtilities.isInterestingScoringPair(pp, scn);

        if (scan == null)
            return 0; // not scoring this one
        if (!canScore(scan))
            return 0; // not scoring this one


        // NOTE this is totally NOT Thread Safe
        fillSpectrumPeaks(scan);

        //        DebugDotProduct logDotProductB = null;
        //        DebugDotProduct logDotProductY = null;

        if (!pConditionedScan.isValid())
            return 0;
        String scanid = scan.getId();
        //        if (scanid.equals("7868"))
        //            XTandemUtilities.breakHere();
        int precursorCharge = scan.getPrecursorCharge();

        double testmass = scan.getPrecursorMass();
        if (pSpectrums.length == 0)
            return 0; // nothing to do
        // for debugging isolate one case

        for (int j = 0; j < pSpectrums.length; j++) {
            ITheoreticalSpectrumSet tsSet = pSpectrums[j];
            // debugging test
            IPolypeptide peptide = tsSet.getPeptide();
            double matching = peptide.getMatchingMass();


            if (scorer.isTheoreticalSpectrumScored(pConditionedScan, tsSet)) {
                numberScoredSpectra += scoreOnePeptide(pCounter, pConditionedScan, scan, Scorer.PEAKS_BY_MASS, precursorCharge, tsSet, logCalculations);
            }

            // debugging code -t look at why some specrta are NOT scored
            //             else {
            //                 // for the moment reiterate the code to find out why not scored
            //                 if (XTandemHadoopUtilities.isNotScored(pConditionedScan.getRaw())) {
            //                     XTandemUtilities.breakHere();
            //                     boolean notDone = scorer.isTheoreticalSpectrumScored(pConditionedScan, tsSet);
            //                     ScoringReducer.gNumberNotScored++;
            //
            //                 }
            //             }
        }
        return numberScoredSpectra;
    }


    @Override
    public void setMinusLimit(double pMinusLimit) {
        super.setMinusLimit(pMinusLimit);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public double getPlusLimit() {
        return super.getPlusLimit();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        return getName();
    }

    protected void setMeasuredSpectrum(IMeasuredSpectrum ms) {
        if (ms == m_Spectrum)
            return;
        m_Spectrum = ms;
        populateWeights(ms);
    }


    /**
     * make comet bins
     *
     * @param mz
     * @return
     */
    protected int asBin(double mz) {
        return (int) (mz * m_InverseBinWidth + m_OneMinusBinOffset);
    }

    /**
     * make comet bins as floats so standard sprctrum structures can hold then
     * add 0.5 so they round well
     *
     * @param mz
     * @return
     */
    protected double asFBin(double mz) {
        return (int) (mz * m_InverseBinWidth + m_OneMinusBinOffset) + 0.5;
    }

    protected float[] getWeights() {
        if (m_Weightsx == null) {
            double massTolerance = getBinTolerance();
            int n = (int) (MAX_MASS / massTolerance);
            m_Weightsx = new float[n];
        }
        return m_Weightsx;
    }

    protected void clearWeights() {
        float[] wts = getWeights();
        Arrays.fill(wts, 0);
    }

    protected void populateWeights(IMeasuredSpectrum ms) {
        clearWeights();
        float[] wts = getWeights();
        if (ms instanceof BinnedMutableSpectrum) {
            ((BinnedMutableSpectrum) ms).populateWeights(wts);
            return;
        }
        if (true)
            throw new UnsupportedOperationException("Never get here");
        ISpectrumPeak[] nonZeroPeaks = ms.getNonZeroPeaks();
        for (int i = 0; i < nonZeroPeaks.length; i++) {
            ISpectrumPeak pk = nonZeroPeaks[i];
            int bin = asBin(pk.getMassChargeRatio());
            float peak = pk.getPeak();
            wts[bin] = peak;
            m_TotalIntensity += peak;
            wts[bin - 1] = peak / 2;
            wts[bin + 1] = peak / 2;
        }
    }


    protected double computeProbability(double intensity, double massDifference) {
        double theta = getThetaFactor();
        double denom = 2 * theta * theta;
        double addedDiff = (massDifference * massDifference) / denom;
        double logIntensity = Math.log(intensity);
        double logPi = LOG_PI;
        double logTheta = Math.log(theta);
        double consts = logPi - logTheta;
        double prob = logIntensity - consts - addedDiff;
        return prob;
    }

    protected double getThetaFactor() {
        return getPeptideError() * 0.5;
    }

    public double getPeptideError() {
        return m_PeptideError;
    }

    public void setPeptideError(final double pPeptideError) {
        m_PeptideError = pPeptideError;
    }


    /**
     * return the product of the factorials of the counts
     *
     * @param counter - !null holding counts
     * @return as above
     */
    @Override
    public double getCountFactor(final IonUseScore counter) {
        return 1;
    }

    public IMeasuredSpectrum getSpectrum() {
        return m_Spectrum;
    }

    public double getTotalIntensity() {
        return m_TotalIntensity;
    }

    public int getMinimumNumberPeaks() {
        return m_MinimumNumberPeaks;
    }

    public void setMinimumNumberPeaks(int minimumNumberPeaks) {
        m_MinimumNumberPeaks = minimumNumberPeaks;
    }

    public double getUnitsFactor() {
        return m_UnitsFactor;
    }

    public void setUnitsFactor(double unitsFactor) {
        m_UnitsFactor = unitsFactor;
    }

    public void setTotalIntensity(double totalIntensity) {
        m_TotalIntensity = totalIntensity;
    }

    public double getBinTolerance() {
        return m_BinTolerance;
    }

    public void setBinTolerance(double binTolerance) {
        m_BinTolerance = binTolerance;
    }

    /**
     * return the low and high limits of a mass scan
     *
     * @param scanMass
     * @return as above
     */
    @Override    // todo fix this
    public int[] highAndLowMassLimits(double scanMass) {
        IMZToInteger defaultConverter = XTandemUtilities.getDefaultConverter();
        int[] ret = new int[2];
        // algorithm makes adjustments in the mass - this does the right thing
        // note this only looks backwards SLewis
        ret[0] = defaultConverter.asInteger(scanMass - getPlusLimit());
        ret[1] = defaultConverter.asInteger(scanMass + -getMinusLimit());

        return ret; // break here interesting result
    }

    @Override
    protected void fillSpectrumPeaks(IMeasuredSpectrum pScan) {
        setMeasuredSpectrum(pScan);
    }

    /**
     * score the two spectra
     *
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @return value of the score
     */
    @Override
    public double scoreSpectrum(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory, Object... otherdata) {
        IonUseCounter counter = new IonUseCounter();
        List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();
        double dot = dot_product(measured, theory, counter, holder);
        return dot;
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

    /**
     * are we scoring mono average
     *
     * @return as above
     */
    @Override
    public MassType getMassType() {
        return null;
    }


    public double getMassTolerance() {
        return m_MassTolerance;
    }

    public void setMassTolerance(double massTolerance) {
        m_MassTolerance = massTolerance;
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
    public double conditionScore(final double score, final IMeasuredSpectrum measured, final ITheoreticalSpectrumSet theory, final IonUseScore counter) {
        return score;
    }

    /**
     * find the hyperscore the score from dot_product in algorithm depdndent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return hyperscore
     */
    @Override
    public double buildHyperscoreScore(final double score, final IMeasuredSpectrum measured, final ITheoreticalSpectrumSet theory, final IonUseScore counter) {
        return score;
    }


    /**
     * an algorithm may choose not to score a petide - for example high resolution algorithms may
     * choose not to score ppetides too far away
     *
     * @param ts    !null peptide spectrum
     * @param pScan !null scan to score
     * @return true if scoring is desired
     */
    public boolean isTheoreticalSpectrumScored(ITheoreticalSpectrum ts, IMeasuredSpectrum pScan) {
        int charge = ts.getCharge();
        IPolypeptide peptide = ts.getPeptide();
        double matchingMass = peptide.getMatchingMass();

        double precursorMass = pScan.getPrecursorMass();
        int precursorCharge = pScan.getPrecursorCharge();

        double del = Math.abs(matchingMass - precursorMass);
        double massTolerance = getMassTolerance();
        //noinspection SimplifiableIfStatement
        if (del < massTolerance)
            return true;
        return false;
    }

    @Override
    protected int scoreOnePeptide(IonUseCounter pCounter, IScoredScan pConditionedScan, IMeasuredSpectrum pScan, double[] pPeaksByMass, int pPrecursorCharge, ITheoreticalSpectrumSet pTsSet, boolean logCalculations) {

        double scanMass = pScan.getPrecursorMass();
        double matchingMass = pTsSet.getPeptide().getMatchingMass();

        return super.scoreOnePeptide(pCounter, pConditionedScan, pScan, pPeaksByMass, pPrecursorCharge, pTsSet, logCalculations);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Cheat by rounding mass to the nearest int and limiting to MAX_MASS
     * then just generate arrays of the masses and multiply them all together
     *
     * @param measured  !null measured spectrum
     * @param theory    !null theoretical spectrum
     * @param counter   !null use counter
     * @param holder    !null holder tro receive matched peaks
     * @param otherData anythiing else needed
     * @return comupted dot product
     */
    @Override
    public double dot_product(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory, final IonUseCounter counter, final List<DebugMatchPeak> holder, final Object... otherData) {
        setMeasuredSpectrum(measured);

        IPolypeptide peptide = theory.getPeptide();

        boolean isInteresting = TestUtilities.isInterestingPeptide();
        if (isInteresting)
            System.out.println("Scoring " + peptide);

        int[] items = new int[1];
        double peptideError = getPeptideError();

        double score = 0;
        double TotalIntensity = getTotalIntensity();

        float[] weights = getWeights();
        final ITheoreticalPeak[] tps = theory.getTheoreticalPeaks();
        for (int i = 0; i < tps.length; i++) {
            ITheoreticalPeak tp = tps[i];
            int bin = asBin(tp.getMassChargeRatio());

            IonType type = tp.getType();
            if (17027 == bin || 18211 == bin)
                bin = asBin(tp.getMassChargeRatio()); // break here  track two 'bad" peaks

            float weight = weights[bin];
            if (weight == 0)
                continue;
            counter.addCount(type);
            if (isInteresting)
                System.out.println(" add " + bin + " = " + String.format("%9.4f", weight));

            score += weight; //  * tp.getPeak();
        }
        double originalScore = score;
        if (score <= 0.0)
            score = 0.0;
        else
            score *= 0.005;  // Scale intensities to 50 and divide score by 1E5.


        if (isInteresting)
            System.out.println(" originalScore " + String.format("%9.2f", originalScore) + " = " + String.format("%9.4f", score));
        return (score);
    }


    /**
     * return false if the algorithm will not score the spectrum
     *
     * @param !null spectrum measured
     * @return as above
     */
    public boolean canScore(IMeasuredSpectrum measured) {
        if (!super.canScore(measured))
            return false;
        //noinspection SimplifiableIfStatement
        if (measured.getPeaksCount() < getMinimumNumberPeaks())
            return false;

        return true; // override if some spectra are not scored
    }


    /**
     * test for acceptability and generate a new conditioned spectrum for
     * scoring
     * See Spectrum.truncatePeakList()
     *
     * @param in !null spectrum
     * @return null if the spectrum is to be ignored otherwise a conditioned spectrum
     */
    @Override
    public IMeasuredSpectrum conditionSpectrum(IScoredScan inx, final SpectrumCondition sc) {
        OriginatingScoredScan scan = (OriginatingScoredScan) inx;
        IMeasuredSpectrum in = scan.getNormalizedRawScan();
        if (in instanceof RawPeptideScan) {
            in = sc.normalizeSpectrum(in, scan.getNormalizationFactor());
            scan.setNormalizedRawScan(in);
        }
        in = normalize(in.asMmutable());
        return in;
    }


    @Override
    public IMeasuredSpectrum conditionSpectrum(final IMeasuredSpectrum pIn, final IMeasuredSpectrum pRaw) {
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    public static double getMassDifference(ISpectrum spec) {
        ISpectrumPeak[] peaks = spec.getPeaks();
        double lowMZ = peaks[0].getMassChargeRatio();
        double highMZ = peaks[peaks.length - 1].getMassChargeRatio();

        //System.out.println("lowMZ "+lowMZ+" highMZ "+highMZ);
        double diffMZ = 0;

        if (lowMZ == highMZ)
            diffMZ = 1f;
        else
            diffMZ = highMZ - lowMZ;


        return diffMZ;
    }


    public static double getMissScore(int numMissIons, ISpectrum spec) {
        double diffMZ = getMassDifference(spec);
        return numMissIons * (-Math.log(diffMZ));
    }


    /**
     * is this correct or should all spectra do this????? todo
     *
     * @param in
     * @return
     */
    protected IMeasuredSpectrum asFBinned(final MutableMeasuredSpectrum in) {
        ISpectrumPeak[] peaks = in.getPeaks();
        MutableSpectrumPeak[] newpeaks = new MutableSpectrumPeak[peaks.length];
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            newpeaks[i] = new MutableSpectrumPeak(asFBin(peak.getMassChargeRatio()), peak.getPeak());
        }
        MutableMeasuredSpectrum out = new MutableMeasuredSpectrum(in);
        out.setPeaks(newpeaks);
        return out; // I do not see normalization
    }


    /**
     * is this correct or should all spectra do this????? todo
     *
     * @param in
     * @return
     */
    protected IMeasuredSpectrum normalize(final MutableMeasuredSpectrum in) {
        double proton = MassCalculator.getDefaultCalculator().calcMass("H");
        ISpectrumPeak[] peaks = in.getPeaks();
        int charge = in.getPrecursorCharge();

        MutableSpectrumPeak[] newpeaks = new MutableSpectrumPeak[peaks.length];
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            double mz = peak.getMassChargeRatio();
            float pk = peak.getPeak();
            newpeaks[i] = new MutableSpectrumPeak(mz, pk);
        }
        normalizePeaks(newpeaks);
        windowedNormalize(newpeaks);
        BinnedMutableSpectrum out = new BinnedMutableSpectrum(in.getPrecursorCharge(), in.getPrecursorMassChargeRatio(),
                in.getScanData(), fbinPeaks(newpeaks));
        return out; // I do not see normalization
    }

    public static final int NUMBER_WINDOWS = 10;
    public static final double WINDOW_NORMALIZATION = 50;

    private void windowedNormalize(MutableSpectrumPeak[] peaks) {
        double maxPeak = 0;
        // peaks are in order
        double maxMz = peaks[peaks.length - 1].getMassChargeRatio();

        double windowWidth = (maxMz + 0.5) / NUMBER_WINDOWS;
        double[] maxWindow = new double[NUMBER_WINDOWS];
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            double mz = peak.getMassChargeRatio();
            int nWindow = (int) (mz / windowWidth);

            float pk = peak.getPeak();
            maxPeak = Math.max(maxPeak, pk);
            maxWindow[nWindow] = Math.max(maxWindow[nWindow], pk);
        }


        double minAffected = 0.05 * maxPeak;  // better be 5
        for (int i = 0; i < peaks.length; i++) {
            MutableSpectrumPeak peak = peaks[i];
            double mz = peak.getMassChargeRatio();
            int nWindow = (int) (mz / windowWidth);

            float pk = peak.getPeak();
            if (pk < minAffected)
                pk = 0;
            int bin = asBin(mz);
            double maxWindowPk = maxWindow[nWindow];
            if (maxWindowPk <= 0)
                continue;
            float normalizationFactor = (float) (WINDOW_NORMALIZATION / maxWindowPk);
            float normalizedValue = normalizationFactor * pk;
            peak.setPeak(normalizedValue);
            maxPeak = Math.max(maxPeak, pk);
        }


    }

    /**
     * normalize windows to a max of 50
     * @param peaks
     */
    public void windowedNormalize(float[] peaks) {
        double maxPeak = 0;
        // peaks are in order
        int windowWidth = (peaks.length + NUMBER_WINDOWS / 2) / NUMBER_WINDOWS;
        double[] maxWindow = new double[NUMBER_WINDOWS];
        double[] windowFactor = new double[NUMBER_WINDOWS];
         for (int i = 0; i < peaks.length; i++) {
            float pk = peaks[i];
            if(pk == 0)
                continue;
            int nWindow = i / windowWidth;
             maxWindow[nWindow] = Math.max(maxWindow[nWindow], pk);
        }
        for (int i = 0; i < maxWindow.length; i++) {
            if(maxWindow[i] > 0)
                windowFactor[i] = 50 / maxWindow[i];
            else
                windowFactor[i] = 1;
        }


         for (int i = 0; i < peaks.length; i++) {
            double pk = peaks[i];
            if(pk == 0)
                continue;
            int nWindow = i / windowWidth;
            peaks[i] *= windowFactor[nWindow];
        }
    }


    public static MutableSpectrumPeak[] asMutable(final ISpectrumPeak[] peaks) {
        MutableSpectrumPeak[] ret = new MutableSpectrumPeak[peaks.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new MutableSpectrumPeak(peaks[i]);

        }
        return ret;
    }

    public static void normalizePeaks(MutableSpectrumPeak[] myPeaks) {
        // Take the square root of the peak
        for (int i = 0; i < myPeaks.length; i++) {
            MutableSpectrumPeak myPeak = myPeaks[i];
            myPeak.setPeak((float) Math.sqrt(myPeak.getPeak()));
        }

        normalizePeakRange(myPeaks, 0, myPeaks.length);
        //noinspection UnnecessaryReturnStatement
        return; // so we can break
    }

    public static void normalizePeakRange(MutableSpectrumPeak[] myPeaks, int start, int end) {
        double maxValue = Double.MIN_VALUE;
        for (int i = start; i < Math.min(end, myPeaks.length); i++) {
            ISpectrumPeak peak = myPeaks[i];
            maxValue = Math.max(maxValue, peak.getPeak());
        }
        double factor = NORMALIZATION_MAX / maxValue;
        for (int i = start; i < Math.min(end, myPeaks.length); i++) {
            MutableSpectrumPeak myPeak = myPeaks[i];
            myPeak.setPeak((float) (factor * myPeak.getPeak()));

        }
    }


    public static double getMaximumIntensity(ISpectrumPeak[] peaks) {
        double ret = Double.MIN_VALUE;
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            ret = Math.max(ret, peak.getPeak());
        }
        return ret;
    }

    protected void addHighestPeaks(final MutableMeasuredSpectrum pOut, final ISpectrumPeak[] pPeaks, final double pStart, final double pEnd) {
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (int i = 0; i < pPeaks.length; i++) {
            ISpectrumPeak peak = pPeaks[i];
            double mass = peak.getMassChargeRatio();
            if (Math.abs(mass - 850) < 2)
                XTandemUtilities.breakHere();

            if (mass >= pStart && mass < pEnd)
                holder.add(peak);
        }
        ISpectrumPeak[] used = new ISpectrumPeak[holder.size()];
        holder.toArray(used);
        // sort by size
        Arrays.sort(used, ScoringUtilities.PeakIntensityComparatorHiToLowINSTANCE);
        // add the top PEAK_BIN_SIZE peaks in this bin
        for (int i = 0; i < Math.min(PEAK_BIN_SIZE, used.length); i++) {
            ISpectrumPeak added = used[i];
            pOut.addPeak(added);

        }

    }


    /**
     * return the expected value for the best score
     *
     * @param scan !null scan
     * @return as above
     */
    @Override
    public double getExpectedValue(final IScoredScan scan) {
        return 0;
    }

    /**
     * modify the theoretical spectrum before scoring - this may
     * involve reweighting or dropping peaks
     *
     * @param pTs !null spectrum
     * @return !null modified spectrum
     */
    @Override
    public ITheoreticalSpectrum buildScoredScan(final ITheoreticalSpectrum pTs) {
        return pTs;
    }

    /**
     * convert peaks to binned peaks
     *
     * @param pks
     * @return
     */
    protected ISpectrumPeak[] fbinPeaks(ISpectrumPeak[] pks) {
        ISpectrumPeak[] ret = new ISpectrumPeak[pks.length];
        for (int i = 0; i < pks.length; i++) {
            ISpectrumPeak pk = pks[i];
            double fbin = asFBin(pk.getMassChargeRatio());
            ret[i] = new MutableSpectrumPeak(fbin, pk.getPeak());
        }
        return ret;
    }

    /**
     * largely to mark a spectrum as already binned
     */
    public static class BinnedMutableSpectrum extends MutableMeasuredSpectrum {

        public BinnedMutableSpectrum(int precursorCharge, double precursorMassChargeRatio, ISpectralScan scan, ISpectrumPeak[] peaks) {
            super(precursorCharge, precursorMassChargeRatio, scan, peaks);
        }

        public void populateWeights(float[] peaks) {
            Arrays.fill(peaks, 0);
            for (ISpectrumPeak pk : getPeaks()) {
                int index = (int) pk.getMassChargeRatio();
                float peak = pk.getPeak();
                peaks[index] = peak;
                peaks[index - 1] = Math.max(peaks[index - 1], peak / 2);
                peaks[index + 1] = Math.max(peaks[index + 1], peak / 2);
            }

        }
    }
}
