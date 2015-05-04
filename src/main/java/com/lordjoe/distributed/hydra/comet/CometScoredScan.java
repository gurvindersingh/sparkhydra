package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.sax.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoredScan
 * User: steven
 * made this implement IMeasured spectrum
 * Date: 12/5/11
 */
public class CometScoredScan implements IScoredScan, IAddable<IScoredScan>, IMeasuredSpectrum {
    public static final int MAX_SERIALIZED_MATCHED = 8;
    public static final String TAG = "score";
    public static final int TEST_BIN = 7653;

    public static final Comparator<IScoredScan> ID_COMPARISON = new Comparator<IScoredScan>() {
        @Override
        public int compare(IScoredScan o1, IScoredScan o2) {
            if (o1.equals(o2))
                return 0;
            return o1.getId().compareTo(o2.getId());
        }
    };

    public static final String DEFAULT_VERSION = "1.0";
    public static final String DEFAULT_ALGORITHM = CometScoringAlgorithm.ALGORITHM_NAME;
    /**
     * It is expensice to compute expected value and until we are at the end of computations not worth reporting
     */
    private static boolean gReportExpectedValue = false;
    private int maxArraySize;
    private IMeasuredSpectrum m_Raw;
    private CometScoringAlgorithm.BinnedMutableSpectrum m_BinnedSpectrum;
    private double m_NormalizationFactor = 1;
    private final IonUseScore m_IonUse = new IonUseCounter();
    //    private List<ISpectralMatch> m_Matches;
    private BoundedMatchSet m_Matches = new BoundedMatchSet();
    private final HyperScoreStatistics m_HyperScores = new HyperScoreStatistics();
    protected final VariableStatistics m_ScoreStatistics = new VariableStatistics();
    private double m_ExpectedValue = Double.NaN;
    private String m_Version = DEFAULT_VERSION;
    private String m_Algorithm = DEFAULT_ALGORITHM;
    private boolean normalizationDone;
    private transient CometScoringData scoringData;
    private transient Map<Integer, Float> fastScoringMap = new HashMap<Integer, Float>();
    private transient Map<Integer, Float> fastScoringMapNL = new HashMap<Integer, Float>();

    public CometScoredScan(IMeasuredSpectrum pRaw) {
        this();
        if (pRaw instanceof ScoringMeasuredSpectrum) {
            ScoringMeasuredSpectrum sm = (ScoringMeasuredSpectrum) pRaw;
            m_Raw = sm;
        } else {
            m_Raw = pRaw;

        }
    }

    public CometScoredScan() {
    }

    public void setAlgorithm(CometScoringAlgorithm alg) {
        double mass = m_Raw.getPrecursorMass();    // todo is this peptide or
        maxArraySize = alg.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize

        generateBinnedPeaks(alg);
        List<SpectrumBinnedScore> weights1 = getWeights();
        //   populateWeights(alg);
        windowedNormalize(alg);

        List<SpectrumBinnedScore> weights2 = getWeights();

        normalizeBinnedPeaks(alg);

        normalizeForNL(alg);

        return;

    }

    protected void populateWeights(CometScoringAlgorithm alg) {
        CometScoringData scoringData = getScoringData(alg);
        float[] wts = scoringData.getWeights();
        Arrays.fill(wts, 0);
        if (m_Raw instanceof CometScoringAlgorithm.BinnedMutableSpectrum) {
            ((CometScoringAlgorithm.BinnedMutableSpectrum) m_Raw).populateWeights(wts);
            return;
        }
        if (true)
            throw new UnsupportedOperationException("Never get here");
//        ISpectrumPeak[] nonZeroPeaks = ms.getNonZeroPeaks();
//        for (int i = 0; i < nonZeroPeaks.length; i++) {
//            ISpectrumPeak pk = nonZeroPeaks[i];
//            int bin = asBin(pk.getMassChargeRatio());
//            float peak = pk.getPeak();
//            wts[bin] = peak;
//            m_TotalIntensity += peak;
//            wts[bin - 1] = peak / 2;
//            wts[bin + 1] = peak / 2;
//        }
    }


    /**
     * normalize windows to a max of 50
     *
     * @param peaks
     */
    public void windowedNormalize(CometScoringAlgorithm alg) {
        CometScoringData scoringData = getScoringData(alg);
        float[] peaks = scoringData.getWeights();
        float[] pdTmpCorrelationData = scoringData.getTmpFastXcorrData();

        int highestPeak = 0;
        double maxPeak = 0;
        for (int i = 0; i < peaks.length; i++) {
            float peak = peaks[i];
            if (Math.abs(peak) < 0.001)
                continue;
            highestPeak = i;
            maxPeak = Math.max(maxPeak, peak);
        }

        // normalize to 100
        float factor = (float) (100 / maxPeak);
        for (int i = 0; i < peaks.length; i++) {
            if (peaks[i] > 0)
                peaks[i] *= factor;
        }

        // peaks are in order
        int windowWidth = (highestPeak / CometScoringAlgorithm.NUMBER_WINDOWS) + 1;
        double[] maxWindow = new double[CometScoringAlgorithm.NUMBER_WINDOWS];
        double[] windowFactor = new double[CometScoringAlgorithm.NUMBER_WINDOWS];
        for (int i = 0; i < peaks.length; i++) {
            float pk = peaks[i];
            if (Math.abs(pk) < 0.001)
                continue;
            int nWindow = i / windowWidth;
            if (nWindow >= maxWindow.length)
                throw new IllegalStateException("problem"); // ToDo change
            maxWindow[nWindow] = Math.max(maxWindow[nWindow], pk);
        }

        for (int i = 0; i < maxWindow.length; i++) {
            if (maxWindow[i] > 0)
                windowFactor[i] = CometScoringAlgorithm.WINDOW_MAXIMUM / maxWindow[i];
            else
                windowFactor[i] = 1;
        }

        double dTmp2 = 5;   // 0.05 * dMaxOverallInten;
        for (int i = 0; i < peaks.length; i++) {
            double pk = peaks[i];
            if (pk <= dTmp2)
                continue;
            int nWindow = i / windowWidth;
            float v = (float) (pk * windowFactor[nWindow]);
            pdTmpCorrelationData[i] = v;
        }
    }


    public void normalizeForNL(CometScoringAlgorithm alg) {
        CometScoringData scoringData = getScoringData(alg);
        int i;
        float[] pPScoringFastXcorrData = scoringData.getScoringFastXcorrData();
        float[] pPdTmpFastXcorrData = scoringData.getTmpFastXcorrData();
        float[] pdTmpCorrelationData = scoringData.getTmpFastXcorrData2();
        float[] pPfFastXcorrDataNL = scoringData.getFastXcorrDataNL();

        for (i = 1; i < Math.min(maxArraySize, pPScoringFastXcorrData.length); i++) {

            float pBinnedPeak = pPdTmpFastXcorrData[i];
            if (pBinnedPeak > 0)
                TestUtilities.breakHere();
            float scoringPeak = pPScoringFastXcorrData[i];
            if (scoringPeak > 0)
                TestUtilities.breakHere();

            float dTmp = pBinnedPeak - scoringPeak;
            //      pPdTmpFastXcorrData2X[i] = (float)dTmp;
            if (Math.abs(dTmp) > 0.001) {
                fastScoringMap.put(i, dTmp);
                pdTmpCorrelationData[i] = dTmp;
            }

             /*if (g_staticParams.ionInformation.bUseNeutralLoss
                        && (g_staticParams.ionInformation.iIonVal[ION_SERIES_A]
                           || g_staticParams.ionInformation.iIonVal[ION_SERIES_B]
                           || g_staticParams.ionInformation.iIonVal[ION_SERIES_Y])) */

            if (true) {
                int iTmp1 = i - alg.iMinus17;
                int iTmp2 = i - alg.iMinus18;

                if (i == 8504)
                    iTmp2 = i - alg.iMinus18;

                pPfFastXcorrDataNL[i] = (float) dTmp;
                if (iTmp1 >= 0) {
                    pBinnedPeak = pPdTmpFastXcorrData[iTmp1];
                    scoringPeak = pPScoringFastXcorrData[iTmp1];
                    float dp = pBinnedPeak - scoringPeak;
                    if (dp != 0) {
                        float offset = (float) (dp * 0.2);
                        pPfFastXcorrDataNL[i] += offset;
                    }
                }

                if (iTmp2 >= 0) {
                    pBinnedPeak = pPdTmpFastXcorrData[iTmp2];
                    scoringPeak = pPScoringFastXcorrData[iTmp2];
                    float dp = pBinnedPeak - scoringPeak;
                    if (dp != 0) {
                        float offset = (float) (dp * 0.2);
                        pPfFastXcorrDataNL[i] += offset;
                    }
                }

            }

        }

        for (int j = 0; j < pPfFastXcorrDataNL.length; j++) {
            float v = pPfFastXcorrDataNL[j];
            if (Math.abs(v) > 0.001)
                fastScoringMapNL.put(j, v);
        }
    }

    /**
     * // Make fast xcorr spectrum.
     *
     * @param alg
     * @return
     */
    public double normalizeBinnedPeaks(CometScoringAlgorithm alg) {
        CometScoringData scoringData = getScoringData(alg);
        float[] pBinnedPeaks = scoringData.getTmpFastXcorrData();
        float[] scoring = scoringData.getScoringFastXcorrData();
        double sum = 0;
        int i = 0;
        for (; i < CometScoringAlgorithm.DEFAULT_CROSS_CORRELATION_PROCESSINGG_OFFSET; i++) {  //    DEFAULT_CROSS_CORRELATION_PROCESSINGG_OFFSET = 75
            sum += pBinnedPeaks[i];
        }
        for (; i < Math.min(maxArraySize + CometScoringAlgorithm.DEFAULT_CROSS_CORRELATION_PROCESSINGG_OFFSET, pBinnedPeaks.length); i++) {
            if (i < maxArraySize)
                sum += pBinnedPeaks[i];
            if (i >= (2 * CometScoringAlgorithm.DEFAULT_CROSS_CORRELATION_PROCESSINGG_OFFSET + 1))
                sum -= pBinnedPeaks[i - (2 * CometScoringAlgorithm.DEFAULT_CROSS_CORRELATION_PROCESSINGG_OFFSET + 1)];
            int indexOffset = i - CometScoringAlgorithm.DEFAULT_CROSS_CORRELATION_PROCESSINGG_OFFSET;
            float binnedPeak = pBinnedPeaks[indexOffset];
            double newPeak = (sum - binnedPeak) * 0.02;
            if (Math.abs(newPeak) > 0.001)
                scoring[indexOffset] = (float) newPeak;
            else
                scoring[indexOffset] = (float) newPeak;

        }

        return sum;
    }

    public List<SpectrumBinnedScore> getFastScoringData() {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        for (Integer key : fastScoringMap.keySet()) {
            holder.add(new SpectrumBinnedScore(key, fastScoringMap.get(key)));
        }
        Collections.sort(holder);
        return holder;
    }

    public List<SpectrumBinnedScore> getWeights() {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        float[] weights = scoringData.getWeights();
        int length = weights.length;
        for (int i = 0; i < length; i++) {
            float weight = weights[i];
            if (Math.abs(weight) > 0.001)
                holder.add(new SpectrumBinnedScore(i, weight));
        }
        return holder;
    }

    public List<SpectrumBinnedScore> getTmpFastXcorrData() {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        float[] weights = scoringData.getTmpFastXcorrData();
        int length = weights.length;
        for (int i = 0; i < length; i++) {
            float weight = weights[i];
            if (Math.abs(weight) > 0.001)
                holder.add(new SpectrumBinnedScore(i, weight));
        }
        return holder;
    }

    public List<SpectrumBinnedScore> getFastScoringDataArray() {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        float[] weights = scoringData.getScoringFastXcorrData();
        int length = weights.length;
        for (int i = 0; i < length; i++) {
            float weight = weights[i];
            if (Math.abs(weight) > 0.001)
                holder.add(new SpectrumBinnedScore(i, weight));
        }
        return holder;
    }

    public List<SpectrumBinnedScore> getTmpFastXcorrData2() {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        float[] weights = scoringData.getTmpFastXcorrData2();
        int length = weights.length;
        for (int i = 0; i < length; i++) {
            float weight = weights[i];
            if (Math.abs(weight) > 0.001)
                holder.add(new SpectrumBinnedScore(i, weight));
        }
        return holder;
    }


    public List<SpectrumBinnedScore> getNLScoringData() {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        for (Integer key : fastScoringMapNL.keySet()) {
            holder.add(new SpectrumBinnedScore(key, fastScoringMapNL.get(key)));
        }
        Collections.sort(holder);
        return holder;
    }

    public CometScoringData getScoringData(CometScoringAlgorithm alg) {
        if (scoringData == null)
            scoringData = new CometScoringData(alg);
        return scoringData;
    }

    public void clearScoringData() {
        scoringData = null;
     }

    public boolean isNormalizationDone() {
        return normalizationDone;
    }

    public void setNormalizationDone(final boolean pIsNormalizationDone) {
        normalizationDone = pIsNormalizationDone;
    }

    public void guaranteeScoringData(CometScoringAlgorithm alg) {
        if (isNormalizationDone())
            return;
        IMeasuredSpectrum scan = getRaw();

        // double sum = normalizeBinnedPeaks(binnedPeaks, MaxArraySize, pdTmpFastXcorrData);
        normalizeBinnedPeaks(alg);
        normalizeForNL(alg);
        setNormalizationDone(true);
    }

    public float getScoredData(Integer index, int charge) {
        if (charge == 1) {
            if (fastScoringMapNL.containsKey(index))
                return fastScoringMapNL.get(index);
            else
                return 0;
        } else {
            if (fastScoringMap.containsKey(index))
                return fastScoringMap.get(index);
            else
                return 0;

        }
    }

    /**
     * same as Comet LoadIOns
     *
     * @param binned
     */
    protected void generateBinnedPeaks(CometScoringAlgorithm algorithm) {
        // get space
        float[] binned = getScoringData(algorithm).getWeights();
        ISpectrumPeak[] peaks = getRaw().getPeaks();
        double maxPeak = 0;
        double maxMass = 0;
        for (ISpectrumPeak peak : peaks) {
            double massChargeRatio = peak.getMassChargeRatio();
            double intensity = peak.getPeak();
            if (intensity > 0) {
                maxMass = Math.max(massChargeRatio, maxMass);
                maxPeak = Math.max(intensity, maxPeak);
            }

        }
        int maxBin = algorithm.asBin(maxMass);
        int lastBin = 0;
        // put in bins
        for (ISpectrumPeak peak : peaks) {
            double intensity = peak.getPeak();
            if (intensity > 0) {
                float massChargeRatio = (float) peak.getMassChargeRatio();
                int bin = algorithm.asBin(massChargeRatio);
                if (bin > lastBin) { // look at when the bin changes really debigging code
                    lastBin = algorithm.asBin(massChargeRatio);
                }
                double sqrtIntensity = Math.sqrt(intensity);
                float value = (float) (sqrtIntensity);
                if (value > binned[bin])
                    binned[bin] = value;
            }
        }

    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(final IMeasuredSpectrum test) {
        return false;
    }

    /**
     * return true if the spectrum is immutable
     *
     * @return
     */
    @Override
    public boolean isImmutable() {
        return true;
    }

    /**
     * if the spectrum is not immutable build an immutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public IMeasuredSpectrum asImmutable() {
        return this;
    }

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public MutableMeasuredSpectrum asMmutable() {
        throw new UnsupportedOperationException("DOn't do this");
    }

    /**
     * get the charge of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public int getPrecursorCharge() {
        return getRaw().getPrecursorCharge();
    }

    /**
     * get the mass of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public double getPrecursorMass() {
        return getRaw().getPrecursorMass();
    }

    /**
     * get the mz of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public double getPrecursorMassChargeRatio() {
        return getRaw().getPrecursorMassChargeRatio();
    }

    /**
     * Mass spec characteristics
     *
     * @return as above
     */
    @Override
    public ISpectralScan getScanData() {
        return getRaw().getScanData();
    }

    /**
     * get the number of peaks without returning the peaks
     *
     * @return as above
     */
    @Override
    public int getPeaksCount() {
        return getRaw().getPeaksCount();
    }

    /**
     * spectrum - this might have been adjusted
     *
     * @return 1=!null array
     */
    @Override
    public ISpectrumPeak[] getPeaks() {
        return getRaw().getPeaks();
    }

    /**
     * get all peaks with non-zero intensity
     *
     * @return
     */
    @Override
    public ISpectrumPeak[] getNonZeroPeaks() {
        return getRaw().getNonZeroPeaks();
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final ISpectrum o) {
        return false;
    }

    public void windowedNormalize() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    /**
     * return algorithm name
     *
     * @return as above
     */
    @Override
    public String getAlgorithm() {
        return CometScoringAlgorithm.ALGORITHM_NAME;
    }

    @Override
    public IMeasuredSpectrum getNormalizedRawScan() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public IMeasuredSpectrum getConditionedScan() {
        return getRaw();
    }

    @Override
    public IMeasuredSpectrum conditionScan(final IScoringAlgorithm alg, final SpectrumCondition sc) {
        return getRaw();
    }

    @Override
    public String getVersion() {
        return m_Version;
    }

    public void setVersion(final String pVersion) {
        if (pVersion != null)
            m_Version = pVersion;
    }

    public void clearMatches() {
        m_Matches = null;
    }

    public void buildMatches(Collection<ISpectralMatch> matches) {
        m_Matches = new BoundedMatchSet(matches);
    }

    public int getMatchCount() {
        return m_Matches.size();
    }

    public double getNormalizationFactor() {
        return m_NormalizationFactor;
    }

    public void setNormalizationFactor(final double pNormalizationFactor) {
        m_NormalizationFactor = pNormalizationFactor;
    }

    public IonUseScore getIonUse() {
        return m_IonUse;
    }


    public BoundedMatchSet getMatches() {
        return m_Matches;
    }

    public static boolean isReportExpectedValue() {
        return gReportExpectedValue;
    }

    public static void setReportExpectedValue(final boolean pReportExpectedValue) {
        gReportExpectedValue = pReportExpectedValue;
    }


    @Override
    public double getMassDifferenceFromBest() {
        final ISpectralMatch bestMatch = getBestMatch();
        final double mass = getMassPlusHydrogen();
        double pm = bestMatch.getPeptide().getMatchingMass();
        double del = Math.abs(pm - mass);

        return del;
    }

    /**
     * get the total peaks matched for all ion types
     *
     * @return
     */
    @Override
    public int getNumberMatchedPeaks() {
        return getIonUse().getNumberMatchedPeaks();
    }

    /**
     * get the score for a given ion type
     *
     * @param type !null iontype
     * @return score for that type
     */
    @Override
    public double getScore(IonType type) {
        return getIonUse().getScore(type);
    }

    /**
     * get the count for a given ion type
     *
     * @param type !null iontype
     * @return count for that type
     */
    @Override
    public int getCount(IonType type) {
        return getIonUse().getCount(type);
    }


    /**
     * @return
     */
    public boolean isValid() {
        final IMeasuredSpectrum raw = getRaw();
        if (raw == null)
            return false;
//        if (raw.getPrecursorMassChargeRatio() == null)
//            return false;
        if (raw.getPeaksCount() < 8) // 20)  // todo make it right
            return false;
        return true;
    }

    public ISpectralMatch[] getSpectralMatches() {
        guaranteeNormalized();
        ISpectralMatch[] ret = m_Matches.getMatches();
        Arrays.sort(ret);
        return ret;
    }

    /**
     * true if some match is scored
     *
     * @return as above
     */
    @Override
    public boolean isMatchPresent() {
        return m_Matches.getMatches().length == 0;
    }

    /**
     * return true if a mass such as that of a throretical peak is
     * within the range to scpre
     *
     * @param mass positive testMass
     * @return as above
     */
    public boolean isMassWithinRange(double mass, int charge, IScoringAlgorithm scorer) {
        final IMeasuredSpectrum raw = getRaw();
        ScanPrecursorMz mz = new ScanPrecursorMz(1, raw.getPrecursorCharge(), raw.getPrecursorMassChargeRatio(), FragmentationMethod.ECD);
        return mz.isMassWithinRange(mass, charge, scorer);
    }


    /**
     * return the scan identifier
     *
     * @return as above
     */
    @Override
    public String getId() {
        IMeasuredSpectrum raw = getRaw();
        if (raw == null)
            return null;
        return raw.getId();
    }

    /**
     * return the scan identifier
     *
     * @return as above
     */
    @Override
    public int getIndex() {
        IMeasuredSpectrum raw = getRaw();
        if (raw == null)
            return 0;
        return raw.getIndex();
    }

    @Override
    public IMeasuredSpectrum getRaw() {
        return m_Raw;
    }

    @Override
    public String getRetentionTimeString() {
        IMeasuredSpectrum raw = getRaw();
        if (raw instanceof RawPeptideScan) {
            RawPeptideScan rawPeptideScan = (RawPeptideScan) raw;
            return rawPeptideScan.getRetentionTime();
        }

        return null;
    }


    /**
     * rention time as a seconds
     *
     * @return possibly null 0
     */
    @Override
    public double getRetentionTime() {
        String str = getRetentionTimeString();
        if (str != null) {
            str = str.replace("S", "");   // handle PT5.5898S"
            str = str.replace("PT", "");
            str = str.trim();
            if (str.length() > 0) {
                try {
                    double ret = Double.parseDouble(str);
                    return ret;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public HyperScoreStatistics getHyperScores() {
        guaranteeNormalized();
        return m_HyperScores;
    }

    @Override
    public VariableStatistics getScoreStatistics() {
        guaranteeNormalized();
//        throw new UnsupportedOperationException("Fix This"); // ToDo
        return m_ScoreStatistics;
    }


    //    @Override
    public ISpectralMatch getBestMatch() {
        guaranteeNormalized();
        return m_Matches.getBest();
    }


    @Override
    public ISpectralMatch getNextBestMatch() {
        guaranteeNormalized();
        // m_Matches is now sorted
        if (m_Matches.size() < 2)
            return null;
        return m_Matches.getNextbest();
    }


    /**
     * key may be id: charge
     *
     * @param in
     * @return
     */
    public static int idFromKey(String in) {
        if (in.contains(":")) {
            return Integer.parseInt(in.substring(0, in.indexOf(":")));
        } else {
            return Integer.parseInt(in);
        }
    }

    public void setRaw(final RawPeptideScan pRaw) {
        m_Raw = pRaw;
    }

    @Override
    public int getNumberScoredPeptides() {
        guaranteeNormalized();
        return m_Matches.size();
    }

    @Override
    public void setExpectedValue(final double pExpectedValue) {
        m_ExpectedValue = pExpectedValue;
    }

//    /**
//     * return true if a mass such as that of a throretical peak is
//     * within the range to scpre
//     *
//     * @param mass positive testMass
//     * @return as above
//     */
//    public boolean isMassWithinRange(double mass) {
//        final RawPeptideScan raw = getRaw();
//        return raw.isMassWithinRange(mass);
//    }

    public static final int ID_LENGTH = 12;

    public String getKey() {
        String id = getId();
        String s = id + ":" + this.getCharge();
        while (s.length() < ID_LENGTH)
            s = "0" + s; // this allows better alphabetical sort
        return s;
    }

    /**
     * combine two scores
     *
     * @param added
     */
    public void addTo(IScoredScan added) {
        if (!added.getId().equals(getId()))
            throw new IllegalArgumentException("incompatable scan");

        ISpectralMatch newMatch = added.getBestMatch();
        ISpectralMatch myBest = getBestMatch();
//        if (myBest == null) {
//            setBestMatch(newMatch);
//            setExpectedValue(added.getExpectedValue());
//        }
//        else {
//            if (newMatch != null && newMatch.getHyperScore() > myBest.getHyperScore())
//                setBestMatch(newMatch);
//            setExpectedValue(added.getExpectedValue());
//        }
//
        HyperScoreStatistics hyperScores = added.getHyperScores();
        getHyperScores().add(hyperScores);
        VariableStatistics scoreStatistics = added.getScoreStatistics();
        getScoreStatistics().add(scoreStatistics);
//         setNumberScoredPeptides(getNumberScoredPeptides() + added.getNumberScoredPeptides());
        ISpectralMatch[] sms = added.getSpectralMatches();
        for (int i = 0; i < sms.length; i++) {
            ISpectralMatch sm = sms[i];
            addSpectralMatch(sm);
        }
    }
//
//    public double lowestHyperscoreToAdd() {
//        if (!isAtMaxCapacity())
//            return Double.MIN_VALUE;
//        return m_Matches.last().getHyperScore();
//    }
//
//
//    public boolean isAtMaxCapacity() {
//        return m_Matches.size() == m_Matches.getMaxItems();
//    }
//
//
//    public int getMaxItems() {
//        return m_Matches.getMaxItems();
//    }

    /**
     * in this version there is no overlap
     *
     * @param added
     */
    public void addSpectralMatch(ISpectralMatch added) {
//        double hyperScore = added.getHyperScore();
//        if (hyperScore <= lowestHyperscoreToAdd())
//            return; // drop unscored matches

        // Test for add with wrong spectral id
        ISpectralMatch bestMatch = getBestMatch();
        if (bestMatch != null && bestMatch.getMeasured() != null) {
            String originalId = bestMatch.getMeasured().getId();
            if (added.getMeasured() != null) {
                String matchId = added.getMeasured().getId();
                if (originalId != null && !originalId.equals(matchId))
                    throw new IllegalStateException("Trying to add " + matchId + " to scores from " + originalId);

            }

        }

        m_Matches.addMatch(added);
    }

    /**
     * added de novo but not when reading fron XML
     *
     * @param pHyperScore
     */
    public void addHyperscore(final double pHyperScore) {
        HyperScoreStatistics hyperScores = getHyperScores();
        hyperScores.add(pHyperScore);       // if reading xml we already added the hyperscore
    }


    protected void guaranteeNormalized() {
        // nothing here this class is always normalized
    }


    /**
     * return the base ion charge
     *
     * @return as above
     */
    @Override
    public int getCharge() {
        return getRaw().getPrecursorCharge();
    }

    /**
     * return the base mass plus  the mass of a proton
     *
     * @return as above
     */
    @Override
    public double getMassPlusHydrogen() {
        IMeasuredSpectrum ns = getNormalizedRawScan();
        final double mass = ns.getPrecursorMass();
        return mass;
    }

    /**
     * return
     *
     * @return as above
     */
    @Override
    public double getExpectedValue() {
        HyperScoreStatistics hyperScores = getHyperScores();
        if (m_ExpectedValue != 0 && !Double.isNaN(m_ExpectedValue) && !Double.isInfinite(m_ExpectedValue))
            return m_ExpectedValue;
        ISpectralMatch bestMatch = getBestMatch();
        if (!hyperScores.isEmpty()) {
            if (bestMatch == null)
                return 1.0; // should not happen
            double hyperScore = bestMatch.getHyperScore();
            double expectedValue = hyperScores.getExpectedValue(hyperScore);
            if (expectedValue != 0 && !Double.isNaN(expectedValue) && !Double.isInfinite(expectedValue))
                return expectedValue;
            return 0;
            // when we have not set this (typical case) we get it from the hyperscores
        }
        if (m_ExpectedValue != 0 && !Double.isNaN(m_ExpectedValue) && !Double.isInfinite(m_ExpectedValue))
            return m_ExpectedValue;
        return 0;
    }

    @Override
    public double getSumIntensity() {
        final double factor = getNormalizationFactor();
        final IMeasuredSpectrum spectrum = getNormalizedRawScan();
        final double sumPeak = XTandemUtilities.getSumPeaks(spectrum) / factor;
        return sumPeak;
    }

    @Override
    public double getMaxIntensity() {
        final double factor = getNormalizationFactor();
        final IMeasuredSpectrum spectrum = getNormalizedRawScan();
        final double sumPeak = XTandemUtilities.getMaxPeak(spectrum) / factor;
        return sumPeak;
    }

    @Override
    public double getFI() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public String toString() {
        return "scan " + getId() + " charge " + getCharge() +
                "  precursorMass " + getMassPlusHydrogen();

    }

    @Override
    public int compareTo(final IScoredScan o) {
        if (o == this)
            return 0;
        return getId().compareTo(o.getId());

    }

    public boolean equivalent(IScoredScan scan) {
        if (scan == this)
            return true;
        if (getCharge() != scan.getCharge())
            return false;
        if (!getVersion().equals(scan.getVersion()))
            return false;
        XMLUtilities.outputLine("For Now forgiving expected value differences");
        //    if (!XTandemUtilities.equivalentDouble(getExpectedValue(), scan.getExpectedValue()))
        //        return false;
        final IMeasuredSpectrum raw1 = getRaw();
        final IMeasuredSpectrum raw2 = scan.getRaw();
        if (!raw1.equivalent(raw2))
            return false;
        final ISpectralMatch[] sm1 = getSpectralMatches();
        final ISpectralMatch[] sm2 = scan.getSpectralMatches();
        if (sm1.length != sm2.length)
            return false;
        for (int i = 0; i < sm2.length; i++) {
            ISpectralMatch m1 = sm1[0];
            ISpectralMatch m2 = sm2[i];
            // really just look
            if (m2.getPeptide().equivalent(m1.getPeptide())) {
                double score1 = m1.getScore();
                double score2 = m2.getScore();

                double rescore1 = 0;
                double rescore2 = 0;
                ITheoreticalIonsScoring[] is1 = ((ExtendedSpectralMatch) m1).getIonScoring();
                ITheoreticalIonsScoring[] is2 = ((ExtendedSpectralMatch) m2).getIonScoring();
                if (is1.length != is2.length)
                    return false;
                for (int j = 0; j < is2.length; j++) {
                    ITheoreticalIonsScoring ts1 = is1[j];
                    ITheoreticalIonsScoring ts2 = is2[j];
                    DebugMatchPeak[] scm1 = ts1.getScoringMasses();
                    DebugMatchPeak[] scm2 = ts2.getScoringMasses();
                    for (int k = 0; k < scm2.length; k++) {
                        DebugMatchPeak dm1 = scm1[k];
                        rescore1 += dm1.getAdded();
                        DebugMatchPeak dm2 = scm2[k];
                        rescore2 += dm2.getAdded();
                        if (!dm1.equivalent(dm2))
                            throw new IllegalStateException("problem"); // ToDo change
                    }
                }
                double diff = rescore1 - rescore2;

            }
        }
        for (int i = 0; i < sm2.length; i++) {
            ISpectralMatch m1 = sm1[i];
            ISpectralMatch m2 = sm2[i];
            if (!m1.equivalent(m2))
                return m1.equivalent(m2);
        }
        return true;
    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder) {
        guaranteeNormalized(); // make sort order correct

        String tag = TAG;
        adder.openTag(tag);
        adder.appendAttribute("id", getId());
        adder.appendAttribute("version", getVersion());
        adder.appendAttribute("algorithm ", getAlgorithm());
        adder.appendAttribute("charge", getCharge());
        if (isReportExpectedValue()) {
            double expectedValue = getExpectedValue();
            adder.appendAttribute("expectedValue", expectedValue);
        }
        adder.appendAttribute("numberScoredPeptides", getNumberScoredPeptides());
        adder.endTag();
        adder.cr();

        final IMeasuredSpectrum raw = getRaw();
        if (raw != null)
            raw.serializeAsString(adder);
        final IMeasuredSpectrum conditioned = getConditionedScan();
        if (conditioned != null) {
            adder.openEmptyTag("ConditionedScan");
            adder.cr();
            conditioned.serializeAsString(adder);
            adder.closeTag("ConditionedScan");
        }
        IMeasuredSpectrum scan = getNormalizedRawScan();
        if (scan != null) {
            adder.openEmptyTag("NormalizedRawScan");
            adder.cr();
            scan.serializeAsString(adder);
            adder.closeTag("NormalizedRawScan");
        }
        HyperScoreStatistics hyperScores = getHyperScores();
        if (!hyperScores.isEmpty()) {
            adder.openEmptyTag("HyperScoreStatistics");
            hyperScores.serializeAsString(adder);
            adder.closeTag("HyperScoreStatistics");

        }

        final ISpectralMatch[] matches = getSpectralMatches();
        Arrays.sort(matches);

        for (int i = 0; i < Math.min(matches.length, MAX_SERIALIZED_MATCHED); i++) {
            ISpectralMatch match = matches[i];
            match.serializeAsString(adder);
        }
        adder.closeTag(tag);

    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IonTypeScorer test) {
        if (test == this)
            return true;


        for (IonType type : IonType.values()) {
            if (getCount(type) != test.getCount(type))
                return false;
            if (!XTandemUtilities.equivalentDouble(getScore(type), test.getScore(type)))
                return false;

        }
        return true;
    }


}
