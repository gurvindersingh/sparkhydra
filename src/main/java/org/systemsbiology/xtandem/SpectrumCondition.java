package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.SpectrumCondition
 *
 * @author Steve Lewis
 * @date Dec 22, 2010
 */

public class SpectrumCondition implements Serializable {
    public static SpectrumCondition[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SpectrumCondition.class;

    private Boolean m_SpectrumConditioned; // enables the use of all conditioning methods
    //protected: let serialization get at these bpratt 9/20/2010
    private boolean m_bUseChargeSuppression; // enables the rejection of highly charge m_ParentStream ions
    //    private boolean m_bUseDynamicRange; // enables using the dynamic range
    //   private boolean m_bUseLowestMass; // enables the removal of very low m/z peaks from a spectrum
//    private boolean m_bUseMaxPeaks; // enables removing low intensity peaks
//    private boolean m_bUseMinMass; // sets the minimum m_ParentStream ion mass allowed
    //   private boolean m_bUseMaxMass; // sets the maximum m_ParentStream ion mass allowed
//    private boolean m_bUseMinSize; // enables using the minimum number of peaks to exclude spectra
    private boolean m_bUseNoiseSuppression; // enables the detection of purely noise spectra
    private boolean m_bUseParent; // enables the exclusion of spectra by the m_ParentStream ion mass
    private boolean m_bUseNeutralLoss;
    private boolean m_bUsePhosphoDetection;
    private Integer m_tMaxPeaks; // the maximum number of peaks in a spectrum
    private Float m_fDynamicRange; // the normalized intensity of the most intense peak in a spectrum
    private Float m_fLowestMass; // the lowest m/z in a spectrum
    private Integer m_lMinSize; // the minimum number of peaks in a spectrum
    private Float m_fMinMass; // the minimum m_ParentStream ion mass in a spectrum
    private Float m_fMaxMass; // the maximum m_ParentStream ion mass in a spectrum
    private Float m_fParentLower; // the low end of the mass window for excluding m_ParentStream ion neutral losses
    private Float m_fParentUpper; // the high end of the mass window for excluding m_ParentStream ion neutral losses (just passed the last C13 isotope peak)
    private Float m_fMaxZ; // the maximum m_ParentStream ion charge allowed
    private Float m_fNeutralLoss;
    private Float m_fNeutralLossWidth;
    private Float m_fFactor;


    /*
    * mspectrumcondition performs a series of operations on a spectrum list to make
    * compatible with the scoring methods. the spectrum can be normalized, adjacent
    * peaks removed, m_ParentStream ion masses excluded, low mass parents excluded, highly
    * charged parents excluded, low mass fragments excluded, and low intensity
    * fragment ions excluded.
    */

    public SpectrumCondition() {
//        m_bUseMaxPeaks = true;
//        m_bUseDynamicRange = true;
//        m_bUseMinMass = true;
        m_bUseParent = true;
//        m_bUseLowestMass = true;
//        m_bUseMinSize = true;
        m_bUseNoiseSuppression = true;
        m_bUseChargeSuppression = true;
        m_SpectrumConditioned = true;
        m_bUseNeutralLoss = false;
        m_bUsePhosphoDetection = false;

        m_tMaxPeaks = 50;
        m_fDynamicRange = 100.0F;
        m_fMinMass = 100.0F;  // todo was 500
        m_fMaxMass = 6000.0F;
        m_fLowestMass = 150.0F;
        m_fParentLower = 2.0F;
        m_fParentUpper = 2.0F;
        m_lMinSize = 5;
        m_fNeutralLoss = 0.0F;
        m_fNeutralLossWidth = 0.0F;
        m_fFactor = 1.0F;
        m_fMaxZ = 4.0F;
    }


    public boolean isUseMinMass() {
        return m_fMinMass != null;
    }


    public boolean isUseMaxMass() {
        return m_fMaxMass != null;
    }


    public boolean isUseMinSize() {
        return m_lMinSize != null;
    }


    public boolean isUseLowestMass() {
        return m_fLowestMass != null;
    }


    public boolean isUseMaxPeaks() {
        return m_tMaxPeaks != null;
    }

    /**
     * finter an dcondition spectra
     *
     * @param in !null
     * @return
     */
    public IMeasuredSpectrum[] conditionSpectra(IScoredScan[] in) {
        List<IMeasuredSpectrum> holder = new ArrayList<IMeasuredSpectrum>();
        for (int i = 0; i < in.length; i++) {
            IScoredScan test = in[i];
            IMeasuredSpectrum out = conditionSpectrum(test, 200);
            if (out != null)
                holder.add(out);
        }
        IMeasuredSpectrum[] ret = new IMeasuredSpectrum[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    public boolean isMassScored(double mass) {
        if (isUseMinMass()) {
            final float requiredMin = getfMinMass();
            if (mass < requiredMin)
                return false;
        }
        if (isUseMaxMass()) {
              final float requiredMax = getfMaxMass();
            if (mass > requiredMax)
                return false;
        }
        return true;

    }

    public boolean isSpectrumScored(MutableMeasuredSpectrum in) {

        SpectrumStatistics stat = new SpectrumStatistics(in);
        if (isbUsePhosphoDetection()) {
            if (!find_loss(in, 98.0F, 3.0F)) {
                return false;
            }
        }
        if (isbUseChargeSuppression()) {
            final int precursorCharge = in.getPrecursorCharge();
            final float maxZ = getfMaxZ();
            if (precursorCharge > maxZ) {
                return false;
            }
        }

/*
	 * check the m_ParentStream ion mass against the minimum mass allowed (about 600.0 is usually safe)
*/
        final double myMass = in.getPrecursorMass();
        if(!isMassScored(myMass))
            return false;
        return true;
    }

    /**
     * test for acceptability and generate a new conditioned spectrum for
     * scoring
     *
     * @param in !null spectrum
     * @return null if the spectrum is to be ignored otherwise a conditioned spectrum
     */
    public IMeasuredSpectrum conditionSpectrum(IScoredScan pScanx, double minMass) {
        OriginatingScoredScan scan = (OriginatingScoredScan) pScanx;
        IMeasuredSpectrum raw = scan.getRaw();
        IMeasuredSpectrum ret = normalizeSpectrum(raw, minMass);
        scan.setNormalizedRawScan(ret);
        return ret;
    }
///*
//* normalize the spectrum
//*/
//        double dSum = _s.getTotalIntensity();
//        double dMax = _s.getMaximumIntensity();
//        _s.clearStatistics();
//        _s.addStatistic(dSum);
//        _s.addStatistic(dMax);
//        _s.addStatistic(m_fFactor);
//        if (isUseDynamicRange()) {
//            dynamic_range(_s);
//        }
//        if (m_bUseNeutralLoss) {
//            remove_neutral(_s);
//        }

///*
//* check to see if the spectrum has the characteristics of noise
//*/
//        if (m_bUseNoiseSuppression) {
//            if (is_noise(_s)) {
//                return false;
//            }
//        }
/////*
////* retrieve the N most intense peaks
////*/
////        clean_isotopes(_s);
////        if (m_bUseMaxPeaks) {
////            sort(_s.m_vMI.begin(), _s.m_vMI.end(), lessThanMI);
////            remove_small(_s);
////        }
////        sort(_s.m_vMI.begin(), _s.m_vMI.end(), lessThanMImz);
////        itMI = _s.m_vMI.begin();
////        itEnd = _s.m_vMI.end();
////        dSum = 0.0;
////        dMax = 0.0;
////        while (itMI != itEnd) {
////            if (dMax < itMI - > m_fI) {
////                dMax = itMI - > m_fI;
////            }
////            dSum += itMI - > m_fI;
////            itMI++;
////        }
////        _s.m_vdStats[0] = dSum * m_fFactor;
////        _s.m_vdStats[1] = dMax * m_fFactor;
////        _s.m_vdStats[2] = m_fFactor;
//        return true;




    public IMeasuredSpectrum normalizeSpectrum(IMeasuredSpectrum raw, double minMass )
    {
         /*
        * if conditioning is turned off, then simply accept the spectrum.
        */
        if (!isSpectrumConditioned()) {
                return raw;
        }
        MutableMeasuredSpectrum in = new MutableMeasuredSpectrum(raw);
        if (!isSpectrumScored(in))
            return null;


        /*
        * this method doesn't really remove isotopes: it cleans up multiple intensities within one Dalton
        * of each other.
        */
//	sort(_s.m_vMI.begin(),_s.m_vMI.end(),lessThanMImz);
        remove_isotopes(in, minMass);

        if (XTandemDebugging.isDebugging()) {
            XTandemDebugging.getLocalValues().addMeasuredSpectrums(in.getId(), "remove_isotopes", in.asImmutable());
        }
/*
 * remove ions near the m_ParentStream ion m/z
 */
        if (isbUseParent()) {
            remove_parent(in);

            if (XTandemDebugging.isDebugging()) {
                XTandemDebugging.getLocalValues().addMeasuredSpectrums(in.getId(), "remove_parent", in.asImmutable());
            }
        }
/*
* remove low mass ammonium ions prior to normalization
*/
        if (isUseLowestMass()) {
            final float maxMass = getfMaxMass();
            final IPeakFilter peakFilter = ScoringUtilities.buildMassLessThanFilter(maxMass);
            ScoringUtilities.applyFilter(in, peakFilter);
            if (XTandemDebugging.isDebugging()) {
                XTandemDebugging.getLocalValues().addMeasuredSpectrums(in.getId(), "isUseLowestMass", in.asImmutable());
            }
        }
        // now that we have dropped the highest (maybe) peaks regather statistics
        SpectrumStatistics stat = new SpectrumStatistics(in);
          if (isbUseDynamicRange()) {
            double maxPeak = XTandemUtilities.getMaxPeak(in);
          //  scan.setNormalizationFactor(100 / maxPeak);
            normalize(in, stat);
            SpectrumStatistics stat2 = new SpectrumStatistics(in);
            if (XTandemDebugging.isDebugging()) {
                XTandemDebugging.getLocalValues().addMeasuredSpectrums(in.getId(), "normalize", in.asImmutable());
            }

        }
        if (isbUseNeutralLoss()) {

        }

/*
* reject the spectrum if there aren't enough peaks
*/
        if (isUseMinSize()) {
            int nPeaks = in.getPeaks().length;
            final long minSIze = getlMinSize();
            if (nPeaks < minSIze)
                return null; // ignore
        }
/*
* check to see if the spectrum has the characteristics of noise
*/
        if (isbUseNoiseSuppression()) {
            if (is_noise(in)) {
                return null;
            }
        }

        // Drop peaks from Carbon 13
        clean_isotopes(in);
        if (XTandemDebugging.isDebugging()) {
            XTandemDebugging.getLocalValues().addMeasuredSpectrums(in.getId(), "clean_isotopes", in.asImmutable());
        }

///*
//* retrieve the N most intense peaks
//*/
        if (isbUseMaxPeaks()) {
            final int maxAllowedPeaks = gettMaxPeaks();
            ScoringUtilities.getMaxNPeaks(in, maxAllowedPeaks);
            if (XTandemDebugging.isDebugging()) {
                XTandemDebugging.getLocalValues().addMeasuredSpectrums(in.getId(), "isbUseMaxPeaks", in.asImmutable());
            }
        }
        IMeasuredSpectrum ret = in.asImmutable();

        return ret;
      }


    /*
    * Perform mix-range modification to input spectrum
    * this is done in mkscore
    */
    protected void doWindowedNormalization(MutableMeasuredSpectrum pIn) {
        final ISpectrumPeak[] spectrumPeaks = pIn.asImmutable().getPeaks();
        final ISpectrumPeak[] mutablePeaks = pIn.getPeaks();
        if (spectrumPeaks.length == 0)
            return;
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        int[] lowerAndUpperIndices = new int[2];

        // populate the list
//         MutableSpectrumPeak first = (MutableSpectrumPeak) spectrumPeaks[lowerAndUpperIndices[0]];
//        double start = first.getMassChargeRatio();
//        double sum = first.getPeak();
//          while (lowerAndUpperIndices[1] < spectrumPeaks.length) {
//            ISpectrumPeak next = spectrumPeaks[lowerAndUpperIndices[1]++];
//            if (next.getMassChargeRatio() > start + 50)
//                break;
//             sum += next.getPeak();
//        }

        double sum = 0;
        for (int i = 0; i < spectrumPeaks.length; i++) {
            MutableSpectrumPeak next = (MutableSpectrumPeak) mutablePeaks[i];
            sum = makeWindowedSum(i, lowerAndUpperIndices, sum, spectrumPeaks);
            final double decrease = sum / 101;
            final double newValue = next.getPeak() - decrease;
            next.setPeak((float) newValue);
            if (newValue > 0) {
                // XTandemUtilities.outputLine("keeping peak " + next.getMassChargeRatio() + " decrease " + decrease)  ;
                holder.add(next);
            }
            else {
                //    XTandemUtilities.outputLine("dropping peak " + next.getMassChargeRatio());
            }
        }

        ISpectrumPeak[] newPeaks = new ISpectrumPeak[holder.size()];
        holder.toArray(newPeaks);
        pIn.setPeaks(newPeaks);
    }

    private static double makeWindowedSum(int pI, int[] lowerAndUpperIndices, double oldSum,
                                          ISpectrumPeak[] pSpectrumPeaks) {
        ISpectrumPeak peak = pSpectrumPeaks[pI];
        double mz = peak.getMassChargeRatio();
        while (lowerAndUpperIndices[0] < pI) {
            ISpectrumPeak firstPeak = pSpectrumPeaks[lowerAndUpperIndices[0]];
            if (firstPeak.getMassChargeRatio() > mz - 50)
                break;
            oldSum -= firstPeak.getPeak();
            lowerAndUpperIndices[0]++;
        }
        lowerAndUpperIndices[1] = Math.max(lowerAndUpperIndices[1], pI);
        while (lowerAndUpperIndices[1] < pSpectrumPeaks.length) {
            ISpectrumPeak next = pSpectrumPeaks[lowerAndUpperIndices[1]];
            if (next.getMassChargeRatio() > mz + 50)
                break;
            oldSum += next.getPeak();
            lowerAndUpperIndices[1]++;

        }


        return oldSum;
    }

    /**
     * make maximum 1 and drop low peaks
     *
     * @param pIn
     * @param pStat
     * @return
     */
    protected void normalize(MutableMeasuredSpectrum spectrum, SpectrumStatistics pStat) {
        float factor = (float) pStat.getMaxPeak() / getfDynamicRange();

        int precursorCharge = spectrum.getPrecursorCharge();
        final double mass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak test : spectrum.getPeaks()) {
            float intensity = test.getPeak() / factor;
            if (intensity < 1) {
                continue; // drop peaks < 1% of largest
            }
            holder.add(new SpectrumPeak(test.getMassChargeRatio(), intensity));
        }
        ISpectrumPeak[] peaks = new ISpectrumPeak[holder.size()];
        holder.toArray(peaks);

        spectrum.setPeaks(peaks);
    }


    /**
     * remove any masses < lowestMass
     *
     * @param lowestMass
     */
    public void removeLowMasses(double lowestMass) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//         final float fZ = m_fZ;
//         List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
//         for (ISpectrumPeak peak : m_vMI) {
//             final double testMass = peak.getMass();
//             if (testMass < lowestMass) {
//                 holder.add(peak);
//             }
//
//         }
//         m_vMI.removeAll(holder);
    }

    public boolean condition(Spectrum _s, MScore _score) {
        //    throw new UnsupportedOperationException("Fix This"); // ToDo
        return true;
    }
/*
* condition is the method called by mprocess to condition a particular mspectrum object.
* by checking the appropriate flags, the various properties of a spectrum can be altered,
* and the spectrum rejected if it does not pass the appropriate tests.
* NOTE: the order of operations is important here: do not change it unless you feel you
*       fully understand why things were done in this order.
*/

    public boolean condition(IMeasuredSpectrum _s, MScore _score) {
/*
 * first do simple spectrum screening.
 */
        if (m_bUsePhosphoDetection) {
            if (!find_loss(_s, 98.0F, 3.0F)) {
                return false;
            }
        }

//        _s.clearNeutral();
//        sort(_s.m_vMI.begin(), _s.m_vMI.end(), lessThanMImz);
//        if (_s.m_vdStats.size() == 0) {
//            vector<mi>::iterator itMI = _s.m_vMI.begin();
//            vector<mi>::iterator itEnd = _s.m_vMI.end();
//            double dSum = 0.0;
//            double dMax = 0.0;
//            while (itMI != itEnd) {
//                if (dMax < itMI - > m_fI) {
//                    dMax = itMI - > m_fI;
//                }
//                dSum += itMI - > m_fI;
//                itMI++;
//            }
//            _s.m_vdStats.push_back(dSum);
//            _s.m_vdStats.push_back(dMax);
//            _s.m_vdStats.push_back(m_fFactor);
//        }
        /*
       * check the m_ParentStream charge against maximum charge allowed
      */
        if (m_bUseChargeSuppression) {
            if (_s.getPrecursorCharge() > m_fMaxZ) {
                return false;
            }
        }
        throw new UnsupportedOperationException("Fix This"); // ToDo
///*
//	 * check the m_ParentStream ion mass against the minimum mass allowed (about 600.0 is usually safe)
//*/
//        if (isUseMinMass()) {
//            if (_s.getdMH() < m_fMinMass)
//                return false;
//        }
///*
//	 * check the m_ParentStream ion mass against the maximum mass allowed
//*/
//        if (isUseMaxMass()) {
//            if (_s.getdMH() > m_fMaxMass)
//                return false;
//        }
//        /*
//      * reject the spectrum if there aren't enough peaks even before conditioning.
//          */
//        if (isUseMinSize()) {
//            if ((long) _s.getSpectrumCount() < m_lMinSize)
//                return false;
//        }
//        /*
//        * give the score object a chance to perform score specific modifications
//        * or veto this spectrum.
//        */
//        if (!_score.precondition(_s)) {
//            return false;
//        }
//
//
//        /*
//      * if conditioning is turned off, then simply accept the spectrum.
//      */
//        if (!m_bCondition) {
//            return true;
//        }
//
//        int tSize = _s.getSpectrumCount();
//        int a = 0;
//        float fMaxI = 0;
///*
//* this method doesn't really remove isotopes: it cleans up multiple intensities within one Dalton
//* of each other.
//*/
////	sort(_s.m_vMI.begin(),_s.m_vMI.end(),lessThanMImz);
//        remove_isotopes(_s);
///*
//* remove ions near the m_ParentStream ion m/z
//*/
//        if (m_bUseParent) {
//            remove_parent(_s);
//        }
///*
//* remove low mass immonium ions prior to normalization
//*/
//        if (isUseLowestMass()) {
//            remove_low_masses(_s);
//        }
///*
//* normalize the spectrum
//*/
//        double dSum = _s.getTotalIntensity();
//        double dMax = _s.getMaximumIntensity();
//        _s.clearStatistics();
//        _s.addStatistic(dSum);
//        _s.addStatistic(dMax);
//        _s.addStatistic(m_fFactor);
//        if (isUseDynamicRange()) {
//            dynamic_range(_s);
//        }
//        if (m_bUseNeutralLoss) {
//            remove_neutral(_s);
//        }
///*
//* reject the spectrum if there aren't enough peaks
//*/
//        if (isUseMinSize()) {
//            if ((long) _s.getSpectrumCount() < m_lMinSize)
//                return false;
//        }
///*
//* check to see if the spectrum has the characteristics of noise
//*/
//        if (m_bUseNoiseSuppression) {
//            if (is_noise(_s)) {
//                return false;
//            }
//        }
/////*
////* retrieve the N most intense peaks
////*/
////        clean_isotopes(_s);
////        if (m_bUseMaxPeaks) {
////            sort(_s.m_vMI.begin(), _s.m_vMI.end(), lessThanMI);
////            remove_small(_s);
////        }
////        sort(_s.m_vMI.begin(), _s.m_vMI.end(), lessThanMImz);
////        itMI = _s.m_vMI.begin();
////        itEnd = _s.m_vMI.end();
////        dSum = 0.0;
////        dMax = 0.0;
////        while (itMI != itEnd) {
////            if (dMax < itMI - > m_fI) {
////                dMax = itMI - > m_fI;
////            }
////            dSum += itMI - > m_fI;
////            itMI++;
////        }
////        _s.m_vdStats[0] = dSum * m_fFactor;
////        _s.m_vdStats[1] = dMax * m_fFactor;
////        _s.m_vdStats[2] = m_fFactor;
//        return true;
    }
/*
 * check_neutral looks for the loss of water or ammonia in a spectrum. this
 * method is not used in this implementation: it is for further experimentation
 */

    public boolean find_loss(ISpectrum _s, final float _d, final float _t) {
        return true;
//        sort(_s.m_vMI.begin(), _s.m_vMI.end(), lessThanMI);
//        vector<mi>::iterator itMI = _s.m_vMI.begin();
//        vector<mi>::iterator itEnd = _s.m_vMI.end();
//        vector<mi>::iterator itLast = itMI;
//        unsigned
//        long a = 0;
//        float fPlus = (float) (_s.m_dMH * 0.001) / _s.m_fZ;
//        float fD = (float) (1.00727 + (_s.m_dMH - 1.00727 - _d) / _s.m_fZ);
//        while (a < 2 && itMI != itEnd) {
//            if (fabs(itMI - > m_fM - fD) < fPlus) {
//                return true;
//            }
//            itMI++;
//            a++;
//        }
//        return false;
    }

/*
 * check_neutral looks for the loss of water or ammonia in a spectrum. this
 * method is not used in this implementation: it is for further experimentation
 */

    public boolean check_neutral(Spectrum _s) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        sort(_s.m_vMI.begin(), _s.m_vMI.end(), lessThanMI);
//        vector<mi>::iterator itMI = _s.m_vMI.begin();
//        vector<mi>::iterator itEnd = _s.m_vMI.end();
//        vector<mi>::iterator itLast = itMI;
//        unsigned
//        long a = 0;
//        unsigned
//        long lCount = 0;
//        float fM = 0.0;
//        while (a < 10) {
//            itMI = itLast;
//            while (itMI != itEnd && itMI - > m_fM < 300.0) {
//                itMI++;
//            }
//            if (itMI == itEnd)
//                break;
//            fM = (float) (itMI - > m_fM - 18.0);
//            itMI++;
//            itLast = itMI;
//            while (itMI < itEnd) {
//                if (fabs(fM - itMI - > m_fM) < 2.5) {
//                    lCount++;
//                    break;
//                }
//                itMI++;
//            }
//            a++;
//        }
//        if (lCount < 1)
//            return false;
//        return true;
    }
    /*
    * remove isotopes removes multiple entries within 0.95 Da of each other, retaining
    * the highest value. this is necessary because of the behavior of some peak
    * finding routines in commercial software
    */

    public void remove_isotopes(MutableMeasuredSpectrum spectrum, double minimumMass) {
        final ISpectrumPeak[] spectrumPeaks = spectrum.getPeaks();
        if (spectrumPeaks.length < 1)
            return; // throw new IllegalArgumentException("empty spectrum");
        int precursorCharge = spectrum.getPrecursorCharge();
        final double mass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();

        // should already be in mass order
        //    Arrays.sort(spectrumPeaks,ScoringUtilities.PeakMassComparatorINSTANCE);
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();

        // pick up the first peak > 1000;
        ISpectrumPeak lastPeak = null;
        int i = 0;
        for (ISpectrumPeak test : spectrumPeaks) {
            i++;
            if (test.getMassChargeRatio() > minimumMass) {
                lastPeak = test;
                //    XTandemUtilities.showDroppedPeak(test);
                break;
            }
        }
        if (lastPeak == null)
            return;


        //
        holder.add(lastPeak);
        for (; i < spectrumPeaks.length; i++) {
            ISpectrumPeak test = spectrumPeaks[i];
            double testMass = test.getMassChargeRatio();

            if (testMass < (lastPeak.getMassChargeRatio() + 0.95)) {
                // keep the bigger peak
                if (lastPeak.getPeak() > test.getPeak()) {
                    XTandemUtilities.showDroppedPeak(test);
                    continue;
                }
                else {
                    holder.add(test);
                    holder.remove(lastPeak);
                    lastPeak = test;
                    continue;
                }

            }
            else {
                holder.add(test);
                lastPeak = test;
            }
        }
        ISpectrumPeak[] newPeaks = new ISpectrumPeak[holder.size()];
        holder.toArray(newPeaks);

        spectrum.setPeaks(newPeaks);
    }


/*
 * clean_isotopes removes peaks that are probably C13 isotopes
 */

    public void clean_isotopes(MutableMeasuredSpectrum spectrum) {
        final ISpectrumPeak[] spectrumPeaks = spectrum.getPeaks();
        if (spectrumPeaks.length < 1)
            return; // throw new IllegalArgumentException("empty spectrum");
        int precursorCharge = spectrum.getPrecursorCharge();
        final double mass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();


        // should already be in mass order
        //    Arrays.sort(spectrumPeaks,ScoringUtilities.PeakMassComparatorINSTANCE);
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();

        // pick up the first peak > 1000;
        ISpectrumPeak lastPeak = spectrumPeaks[0];
        double lastMass = lastPeak.getMassChargeRatio();

        for (int i = 1; i < spectrumPeaks.length; i++) {
            // we update the peak but not the mass
            ISpectrumPeak test = spectrumPeaks[i];
            double testMass = test.getMassChargeRatio();
            if ((testMass - lastMass) < 1.5 && testMass > 200) {
                if (test.getPeak() > lastPeak.getPeak()) {
                    lastPeak = test;   // drop last peak
                }
                else { // drop test peak

                }
            }
            else {
                holder.add(lastPeak);
                lastPeak = test;
                lastMass = lastPeak.getMassChargeRatio();
            }

        }
        if (lastPeak != null)
            holder.add(lastPeak);
        ISpectrumPeak[] newPeaks = new ISpectrumPeak[holder.size()];
        holder.toArray(newPeaks);

        spectrum.setPeaks(newPeaks);
        //       throw new UnsupportedOperationException("Fix This"); // ToDo
//        if (_s.m_vMI.size() < 2)
//            return true;
//
//        vector<mi>::iterator itMI_ecriture = _s.m_vMI.begin();
//        vector<mi>::iterator itMI_lecture = _s.m_vMI.begin() + 1;
//        vector<mi>::final _iterator itMI_fin = _s.m_vMI.end();
//        vector<mi> miTemp;
//        float fEcriture = itMI_ecriture - > m_fM;
//        while (itMI_lecture != itMI_fin) {
//            if ((itMI_lecture - > m_fM - fEcriture) >= 1.5 || itMI_lecture - > m_fM < 200.0) {
//                miTemp.push_back( * itMI_ecriture);
//                itMI_ecriture = itMI_lecture;
//                fEcriture = itMI_ecriture - > m_fM;
//            }
//            else if (itMI_lecture - > m_fI > itMI_ecriture - > m_fI) {
//                *itMI_ecriture =*itMI_lecture;
//            }
//            itMI_lecture++;
//        }
//        miTemp.push_back( * itMI_ecriture);
//        _s.m_vMI = miTemp;
//        return true;

    }
/*
 * is_noise attempts to determine if the spectrum is simply noise. if the spectrum
 * does not have any peaks within a window near the m_ParentStream ion mass, it is considered
 * noise.
 */

    public boolean is_noise(IMeasuredSpectrum in) {
        final ISpectrumPeak[] peaks = in.getPeaks();
        int charge = in.getPrecursorCharge();
        double mass = in.getPrecursorMass();
        double fMax = (float) (mass / charge);
        if (charge < 3) {
            fMax = mass - 600.0;
        }
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            if (peak.getMassChargeRatio() > fMax)
                return false;
        }
        //        int a = 0;
//        int tSize = _s.m_vMI.size();
//        float fZ = _s.m_fZ;
//        float fMax = (float) (_s.m_dMH / fZ);
//        if (fZ == 1) {
//            fMax = (float) (_s.m_dMH - 600.0);
//        }
//        if (fZ == 2) {
//            fMax = (float) (_s.m_dMH - 600.0);
//        }
//        while (a < tSize) {
//            if (_s.m_vMI[a].m_fM > fMax)
//                return false;
//            a++;
//        }
        return true;
    }
/*
 * load makes internal copies of the relavent parameters found in the XmlParameter object
 */

    public void configure(IParameterHolder params) {
        String strKey;
        String strValue;


        strKey = "spectrum, dynamic range";
        m_fDynamicRange = params.getFloatParameter(strKey, 100.0F);

        strKey = "spectrum, total peaks";
        m_tMaxPeaks = params.getIntParameter(strKey, 50);


        strKey = "spectrum, minimum peaks";
        m_lMinSize = params.getIntParameter(strKey);

        strKey = "spectrum, minimum m_ParentStream m+h";
        m_fMinMass = params.getFloatParameter(strKey, 500.0F);

        strKey = "spectrum, maximum m_ParentStream m+h";
        m_fMaxMass = params.getFloatParameter(strKey, 6000);

        strKey = "spectrum, minimum fragment mz";
        m_fLowestMass = params.getFloatParameter(strKey, 150.0F);

        strKey = "spectrum, use conditioning";
        m_SpectrumConditioned = params.getBooleanParameter(strKey, m_SpectrumConditioned);

        strKey = "spectrum, use noise suppression";
        m_bUseNoiseSuppression = params.getBooleanParameter(strKey,true);

        strKey = "spectrum, use neutral loss window";
        m_bUseNeutralLoss = params.getBooleanParameter(strKey,m_bUseNeutralLoss);

        if (m_bUseNeutralLoss) {
            strKey = "spectrum, neutral loss window";
            m_fNeutralLossWidth = params.getFloatParameter(strKey, 0.0F);

            strKey = "spectrum, neutral loss mass";
            m_fNeutralLoss = params.getFloatParameter(strKey, 0.0F);
        }

        strKey = "spectrum, maximum m_ParentStream charge";
        m_fMaxZ = params.getFloatParameter(strKey, 4.0F);

    }

    public boolean isUseDynamicRange() {
        return m_fDynamicRange != null;
    }
/*
 * use the dynamic range parameter to set the maximum intensity value for the spectrum.
 * then remove all peaks with a normalized intensity < 1
 */

    public boolean dynamic_range(Spectrum _s) {
        if (!isUseDynamicRange()) {
            return false;
        }
        m_fFactor = (float) (_s.getMaximumIntensity() / m_fDynamicRange);
        _s.normalizeIntensity(m_fFactor);
//        int tSize = _s.getSpectrumCount();
//        float fI = 1.0F;
//        if (tSize > 0) {
//            fI = _s.m_vMI[0].m_fI;
//        }
//        int a = 0;
//        while (a < tSize) {
//            if (_s.m_vMI[a].m_fI > fI) {
//                fI = _s.m_vMI[a].m_fI;
//            }
//            a++;
//        }
//        m_fFactor = fI / m_fDynamicRange;
//        vector<mi>::iterator itMI = _s.m_vMI.begin();
//        while (itMI != _s.m_vMI.end()) {
//            itMI - > m_fI /= m_fFactor;
//            if (itMI - > m_fI < 1.0) {
//                itMI = _s.m_vMI.erase(itMI);
//            }
//            else {
//                itMI++;
//            }
//        }
        return true;
    }
/*
 * limit the total number of peaks used
 */

    public void remove_small(MutableMeasuredSpectrum _s) {
        ScoringUtilities.getMaxNPeaks(_s, gettMaxPeaks());
    }
/*
 * set up m/z regions to ignore: those immediately below the m/z of the m_ParentStream ion
 * which will contain uninformative neutral loss ions, and those immediately above
 * the m_ParentStream ion m/z, which will contain the m_ParentStream ion and its isotope pattern
 */

    public void remove_parent(MutableMeasuredSpectrum spectrum) {
        final ISpectrumPeak[] spectrumPeaks = spectrum.getPeaks();
        int precursorCharge = Math.max(1, spectrum.getPrecursorCharge());
        final double precursorMass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();
        final float lower = -getfParentLower() / precursorCharge;
        final float upper = getfParentUpper() / precursorCharge;
        float fParentMz = (float) (1.00727 + (precursorMass - 1.00727) / precursorCharge);

//        while(itMI != _s.m_vMI.end())	{
//            if(fParentMz - itMI->m_fM >= 0.0 && fParentMz - itMI->m_fM < m_fParentLower/_s.m_fZ)	{
//                itMI = _s.m_vMI.erase(itMI);
//            }
//            else if(itMI->m_fM - fParentMz > 0.0 && itMI->m_fM - fParentMz < m_fParentUpper/_s.m_fZ)	{
//                itMI = _s.m_vMI.erase(itMI);
//            }
//            else	{
//                itMI++;
//            }
//        }
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (int i = 0; i < spectrumPeaks.length; i++) {
            ISpectrumPeak test = spectrumPeaks[i];

            final double del = fParentMz - test.getMassChargeRatio();
            if (del < 0 && del > lower) {
                XTandemUtilities.showDroppedPeak(test);
                continue; // drop the peak
            }
            else if (del > 0 && del < upper) {
                XTandemUtilities.showDroppedPeak(test);

                continue;   // drop the peak
            }
            holder.add(test); // all OK
        }

        ISpectrumPeak[] newPeaks = new ISpectrumPeak[holder.size()];
        holder.toArray(newPeaks);

        spectrum.setPeaks(newPeaks);
    }
/*
 * remove_low_masses deletes peaks with m/z values below the m_fLowestMass member value
 */

    public boolean remove_low_masses(Spectrum _s) {
        if (!isUseLowestMass())
            return false;
        _s.removeLowMasses(m_fLowestMass);
//        vector<mi>::iterator itMI = _s.m_vMI.begin();
//        while (itMI != _s.m_vMI.end()) {
//            if (itMI - > m_fM > m_fLowestMass) {
//                break;
//            }
//            itMI++;
//        }
//        _s.m_vMI.erase(_s.m_vMI.begin(), itMI);
        return true;
    }
/*
 * remove_neutral
 */

    public boolean remove_neutral(Spectrum _s) {
        if (!m_bUseNeutralLoss)
            return false;
        _s.removeNeutral(m_fNeutralLoss, m_fNeutralLossWidth);
//        throw new UnsupportedOperationException("Fix This"); // ToDo
//        vector<mi>::iterator itMI = _s.m_vMI.begin();
//        while (itMI != _s.m_vMI.end()) {
//            if (fabs((_s.m_dMH - itMI - > m_fM) - m_fNeutralLoss) <= m_fNeutralLossWidth) {
//                _s.m_vMINeutral.push_back( * itMI);
//                itMI = _s.m_vMI.erase(itMI);
//            }
//            else {
//                itMI++;
//            }
//        }
        return true;
    }
/*
 * get_noise_suppression gets the current value of the m_bUseNoiseSuppression member
 */

    public boolean get_noise_suppression() {
        return m_bUseNoiseSuppression;
    }
/*
 * set_max_peaks sets the current value of the m_tMaxPeaks member
 */

    public boolean set_max_peaks(final int _p) {
        m_tMaxPeaks = _p;
        return true;
    }
/*
 * set_min_size sets the current value of the m_lMinSize member
 */

    public boolean set_min_size(final int _p) {
        m_lMinSize = _p;
        return true;
    }
/*
 * set_dynamic_range sets the current value of the m_fDynamicRange member
 */

    public boolean set_dynamic_range(final float _p) {
        m_fDynamicRange = _p;
        return true;
    }
/*
 * set_parent_exclusion sets the current value of the m_fParentLower & m_fParentUpper members
 */

    public boolean set_parent_exclusion(final float _l, final float _u) {
        m_fParentLower = _l;
        m_fParentUpper = _u;
        return true;
    }
/*
 * set_min_mass sets the current value of the m_fMinMass member
 */

    public boolean set_min_mass(final float _m) {
        m_fMinMass = _m;
        return true;
    }
/*
 * set_max_mass sets the current value of the m_fMaxMass member
 */

    public boolean set_max_mass(final float _m) {
        m_fMaxMass = _m;
        return true;
    }
/*
 * set_lowest_mass sets the current value of the m_fLowestMass member
 */

    public boolean set_lowest_mass(final float _m) {
        m_fLowestMass = _m;
        return true;
    }
/*
 * use_condition sets the current value of the m_bCondition member
 */

    public boolean use_condition(final boolean _f) {
        m_SpectrumConditioned = _f;
        return m_SpectrumConditioned;
    }
/*
 * use_min_size sets the current value of the isUseMinSize() member
 */

    public boolean use_min_size(final boolean _f) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }
/*
 * use_max_peaks sets the current value of the isUseMaxPeaks() member
 */

    public boolean use_max_peaks(final boolean _f) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }
/*
 * use_charge_suppression sets the current value of the m_bUseChargeSuppression member
 */

    public boolean use_charge_suppression(final boolean _f) {
        m_bUseChargeSuppression = _f;
        return m_bUseChargeSuppression;
    }
/*
 * use_dynamic_range sets the current value of the isUseDynamicRange() member
 */

    public boolean use_dynamic_range(final boolean _f) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }
/*
 * use_noise_suppression sets the current value of the m_bUseNoiseSuppression member
 */

    public boolean use_noise_suppression(final boolean _f) {
        m_bUseNoiseSuppression = _f;
        return m_bUseNoiseSuppression;
    }
/*
 * use_lowest_mass sets the current value of the isUseLowestMass() member
 */

    public boolean use_lowest_mass(final boolean _f) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }
/*
 * use_min_mass sets the current value of the isUseMinMass() member
 */

    public boolean use_min_mass(final boolean _f) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }
/*
 * use_parent_exclusion sets the current value of the m_bUseParent member
 */

    public boolean use_parent_exclusion(final boolean _f) {
        m_bUseParent = _f;
        return m_bUseParent;
    }

    public boolean isSpectrumConditioned() {
        return m_SpectrumConditioned;
    }

    public void setSpectrumConditioned(boolean pSpectrumConditioned) {
        m_SpectrumConditioned = pSpectrumConditioned;
    }

    public boolean isbUseChargeSuppression() {
        return m_bUseChargeSuppression;
    }

    public void setbUseChargeSuppression(boolean pBUseChargeSuppression) {
        m_bUseChargeSuppression = pBUseChargeSuppression;
    }

    public boolean isbUseDynamicRange() {
        return isUseDynamicRange();
    }

    public void setbUseDynamicRange(boolean pBUseDynamicRange) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public boolean isbUseLowestMass() {
        return isUseLowestMass();
    }

    public void setbUseLowestMass(boolean pBUseLowestMass) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public boolean isbUseMaxPeaks() {
        return isUseMaxPeaks();
    }

    public void setbUseMaxPeaks(boolean pBUseMaxPeaks) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public boolean isbUseMinMass() {
        return isUseMinMass();
    }

    public void setbUseMinMass(boolean pBUseMinMass) {
        if (!pBUseMinMass)
            m_fMinMass = null;
    }

    public boolean isbUseMaxMass() {
        return isUseMaxMass();
    }

    public void setbUseMaxMass(boolean pBUseMaxMass) {
        if (!pBUseMaxMass)
            m_fMaxMass = null;
    }

    public boolean isbUseMinSize() {
        return isUseMinSize();
    }

    public void setbUseMinSize(boolean pBUseMinSize) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public boolean isbUseNoiseSuppression() {
        return m_bUseNoiseSuppression;
    }

    public void setbUseNoiseSuppression(boolean pBUseNoiseSuppression) {
        m_bUseNoiseSuppression = pBUseNoiseSuppression;
    }

    public boolean isbUseParent() {
        return m_bUseParent;
    }

    public void setbUseParent(boolean pBUseParent) {
        m_bUseParent = pBUseParent;
    }

    public boolean isbUseNeutralLoss() {
        return m_bUseNeutralLoss;
    }

    public void setbUseNeutralLoss(boolean pBUseNeutralLoss) {
        m_bUseNeutralLoss = pBUseNeutralLoss;
    }

    public boolean isbUsePhosphoDetection() {
        return m_bUsePhosphoDetection;
    }

    public void setbUsePhosphoDetection(boolean pBUsePhosphoDetection) {
        m_bUsePhosphoDetection = pBUsePhosphoDetection;
    }

    public int gettMaxPeaks() {
        return m_tMaxPeaks;
    }

    public void settMaxPeaks(int pTMaxPeaks) {
        m_tMaxPeaks = pTMaxPeaks;
    }

    public float getfDynamicRange() {
        return m_fDynamicRange;
    }

    public void setfDynamicRange(float pFDynamicRange) {
        m_fDynamicRange = pFDynamicRange;
    }

    public float getfLowestMass() {
        return m_fLowestMass;
    }

    public void setfLowestMass(float pFLowestMass) {
        m_fLowestMass = pFLowestMass;
    }

    public long getlMinSize() {
        return m_lMinSize;
    }

    public void setlMinSize(int pLMinSize) {
        m_lMinSize = pLMinSize;
    }

    public float getfMinMass() {
        return m_fMinMass;
    }

    public void setfMinMass(float pFMinMass) {
        m_fMinMass = pFMinMass;
    }

    public float getfMaxMass() {
        return m_fMaxMass;
    }

    public void setfMaxMass(float pFMaxMass) {
        m_fMaxMass = pFMaxMass;
    }

    public float getfParentLower() {
        return m_fParentLower;
    }

    public void setfParentLower(float pFParentLower) {
        m_fParentLower = pFParentLower;
    }

    public float getfParentUpper() {
        return m_fParentUpper;
    }

    public void setfParentUpper(float pFParentUpper) {
        m_fParentUpper = pFParentUpper;
    }

    public float getfMaxZ() {
        return m_fMaxZ;
    }

    public void setfMaxZ(float pFMaxZ) {
        m_fMaxZ = pFMaxZ;
    }

    public float getfNeutralLoss() {
        return m_fNeutralLoss;
    }

    public void setfNeutralLoss(float pFNeutralLoss) {
        m_fNeutralLoss = pFNeutralLoss;
    }

    public float getfNeutralLossWidth() {
        return m_fNeutralLossWidth;
    }

    public void setfNeutralLossWidth(float pFNeutralLossWidth) {
        m_fNeutralLossWidth = pFNeutralLossWidth;
    }

    public float getfFactor() {
        return m_fFactor;
    }

    public void setfFactor(float pFFactor) {
        m_fFactor = pFFactor;
    }
}
