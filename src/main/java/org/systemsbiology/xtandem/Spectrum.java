package org.systemsbiology.xtandem;

import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.Spectrum
 *
 * @author Steve Lewis
 * @date Dec 22, 2010
 */
public class Spectrum
{
    public static Spectrum[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = Spectrum.class;


    private int m_tId; // an identification number
    private int m_tCurrentSequence; // an identifier for the current sequence (used in mprocess)
    private float m_fScore; // the convolution score
    private float m_fHyper; // the hyper score
    private float m_fScoreNext; // next best convolution score
    private float m_fHyperNext; // next best hyper score
    private double m_dExpect; // the expectation value
    private double m_dProteinExpect; // the expectation value for the associated protein
    private double m_dMH; // the m_ParentStream ion mass + a proton
    private float m_fI; // the m_ParentStream ion intensity (if available)
    private float m_fZ; // the m_ParentStream ion charge
    private boolean m_bRepeat; // a flag indicating that a better match for an individual peptide has already been found
    private boolean m_bActive; // a flag indicating that a spectrum is available for scoring
    private final List<ISpectrumPeak> m_vMI = new ArrayList<ISpectrumPeak>(); // a vector containing the m/z - intensity information for fragment ions
    private final List<ISpectrumPeak> m_vMINeutral = new ArrayList<ISpectrumPeak>(); // a vector containing the m/z - intensity information for fragment ions
    private final List<ProteinSequence> m_vseqBest = new ArrayList<ProteinSequence>(); // a vector containing the highest scoring msequence objects
    private final List<Double> m_vdStats = new ArrayList<Double>();
    private String m_strDescription;
    private String m_strRt;
    private SpectrumHistogram m_hHyper = new SpectrumHistogram(); // the histogram of hyper scores
    private SpectrumHistogram m_hConvolute = new SpectrumHistogram(); // the histogram of convolution scores
    private CountHistogram m_chBCount = new CountHistogram(); // the histogram of b-ion counts
    private CountHistogram m_chYCount = new CountHistogram(); // the histogram of y-ion counts

    public Spectrum()
    {
        m_fI = 1.0F;
        m_fZ = 1.0F;
        m_tId = 0;
        m_fHyper = 100.0F;
        m_fHyperNext = 100.0F;
        m_dExpect = 1000.0;
        m_dProteinExpect = 1000.0;
        m_bActive = true;
        m_tCurrentSequence = (int) 0xAFFFFFFF;
    }

/*
* a simple copy operator using the = operator
*/

    public Spectrum(Spectrum rhs)
    {
        m_vdStats.addAll(rhs.m_vdStats);
        m_hHyper = rhs.m_hHyper;
        m_hConvolute = rhs.m_hConvolute;
        m_chBCount = rhs.m_chBCount;
        m_chYCount = rhs.m_chYCount;
        m_vMI.clear();
        m_vMINeutral.clear();
        int a = 0;
        m_vMI.clear();
        m_vMI.addAll(rhs.m_vMI);
        m_vMINeutral.clear();
        m_vMINeutral.addAll(rhs.m_vMINeutral);
        m_dMH = rhs.m_dMH;
        m_fI = rhs.m_fI;
        m_fZ = rhs.m_fZ;
        m_tId = rhs.m_tId;
        m_fScore = rhs.m_fScore;
        m_fHyper = rhs.m_fHyper;
        m_fScoreNext = rhs.m_fScoreNext;
        m_fHyperNext = rhs.m_fHyperNext;
        m_dExpect = rhs.m_dExpect;
        m_dProteinExpect = rhs.m_dProteinExpect;
        m_bRepeat = rhs.m_bRepeat;
        m_vseqBest.clear();
        m_vseqBest.addAll(rhs.m_vseqBest);
        m_tCurrentSequence = rhs.m_tCurrentSequence;
        m_strDescription = rhs.m_strDescription;
        m_strRt = rhs.m_strRt;
        m_bActive = rhs.m_bActive;
    }

    public Spectrum multiply(final Spectrum rhs)
    {
        m_hHyper = rhs.m_hHyper;
        m_hConvolute = rhs.m_hConvolute;
        m_chBCount = rhs.m_chBCount;
        m_chYCount = rhs.m_chYCount;
        m_bActive = rhs.m_bActive;
        m_dMH = rhs.m_dMH;
        m_fI = rhs.m_fI;
        m_fZ = rhs.m_fZ;
        m_tId = rhs.m_tId;
        m_fScore = rhs.m_fScore;
        m_fHyper = rhs.m_fHyper;
        m_fScoreNext = rhs.m_fScoreNext;
        m_fHyperNext = rhs.m_fHyperNext;
        m_dExpect = rhs.m_dExpect;
        m_dProteinExpect = rhs.m_dProteinExpect;
        m_bRepeat = rhs.m_bRepeat;
        m_vseqBest.clear();
        m_vseqBest.addAll(rhs.m_vseqBest);
        m_tCurrentSequence = rhs.m_tCurrentSequence;
        return this;
    }

    public Spectrum add(final Spectrum rhs)
    {
        m_vdStats.addAll(rhs.m_vdStats);
  //      m_hHyper.add(rhs.m_hHyper);
  //      m_hConvolute.add(rhs.m_hConvolute);
        m_chBCount.add(rhs.m_chBCount);
        m_chYCount.add(rhs.m_chYCount);
        int a = 0;
        int tLength = 0;
        m_bRepeat = false;
        if (rhs.m_fHyper == m_fHyper) {
            m_dMH = rhs.m_dMH;
            m_fI = rhs.m_fI;
            m_fZ = rhs.m_fZ;
            m_tId = rhs.m_tId;
            m_fScore = rhs.m_fScore;
            m_fHyper = rhs.m_fHyper;
            m_fScoreNext = rhs.m_fScoreNext;
            m_fHyperNext = rhs.m_fHyperNext;
            a = 0;
            m_vseqBest.clear();
            m_vseqBest.addAll(rhs.m_vseqBest);
            m_tCurrentSequence = rhs.m_tCurrentSequence;
        }
        else if (rhs.m_fHyper > m_fHyper) {
            m_dMH = rhs.m_dMH;
            m_fI = rhs.m_fI;
            m_fZ = rhs.m_fZ;
            m_tId = rhs.m_tId;
            m_fScore = rhs.m_fScore;
            m_fHyper = rhs.m_fHyper;
            m_fScoreNext = rhs.m_fScoreNext;
            m_fHyperNext = rhs.m_fHyperNext;
            m_vseqBest.clear();
            m_vseqBest.addAll(rhs.m_vseqBest);
            m_tCurrentSequence = rhs.m_tCurrentSequence;
        }
        m_dExpect = rhs.m_dExpect;
        m_dProteinExpect = rhs.m_dProteinExpect;
        return this;
    }

    public void clearVDStats()
    {
        m_vdStats.clear();
    }

    public void addVDStat(double added)
    {
        m_vdStats.add(added);
    }

    public void addVMI(ISpectrumPeak added)
    {
        m_vMI.add(added);
    }

    public void addVMINeutral(ISpectrumPeak added)
    {
        m_vMINeutral.add(added);
    }

    public void clearNeutral()
    {
        m_vMINeutral.clear();
    }

    public int getSpectrumCount()
    {
        return m_vMI.size();
    }

    public double getMaximumIntensity()
    {
        double ret = 0;
        for (ISpectrumPeak peak : m_vMI) {
            ret = Math.max(ret, peak.getPeak());
        }
        return ret;
    }


    public double getTotalIntensity()
    {
        double ret = 0;
        for (ISpectrumPeak peak : m_vMI) {
            ret += peak.getPeak();
        }
        return ret;
    }


    public void normalizeIntensity(float maxValue)
    {
        double ret = 0;
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak peak : m_vMI) {
            ISpectrumPeak normalized = new SpectrumPeak(peak.getMassChargeRatio(), peak.getPeak() / maxValue);
            holder.add(normalized);
        }
        m_vMI.clear();
        m_vMI.addAll(holder);
    }

    /**
     * move too neutral ions to neutral collection
     *
     * @param neutralLoss
     * @param neutralLossWidth
     */
    public void removeNeutral(float neutralLoss, float neutralLossWidth)
    {
        double ret = 0;
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        final double mh = getdMH();
        for (ISpectrumPeak peak : m_vMI) {
            if (Math.abs((mh - peak.getMassChargeRatio()) - neutralLoss) <= neutralLossWidth) {
                holder.add(peak);
            }
        }
        m_vMI.removeAll(holder);
        m_vMINeutral.clear();
        m_vMINeutral.addAll(holder);
    }

/*
 * remove isotopes removes multiple entries within 0.95 Da of each other, retaining
 * the highest value. this is necessary because of the behavior of some peak
 * finding routines in commercial software
 */

    public void removeIsotopes(float minDaltonSeparatation, float minMass)
    {
        int number = getSpectrumCount();
        if (number < 2)
            return; // no duplicated possible
        Collections.sort(m_vMI); // sort my mass
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();

        ISpectrumPeak current = m_vMI.get(0);
        for (int i = 1; i < number; i++) {
            ISpectrumPeak spectrum = m_vMI.get(i);
            // low mass separation
            final double testMass = spectrum.getMassChargeRatio();
            if (testMass > minMass && Math.abs(
                    current.getMassChargeRatio() - testMass) < minDaltonSeparatation) {
                // - minDaltonSeparatation) <= neutralLossWidth) {
                holder.add(current); // drop use highest mass
            }
            else {
                XTandemUtilities.showDroppedPeak(current);

            }
            current = spectrum;
        }
        // drop all masses
        m_vMI.removeAll(holder);
    }

/*
 * set up m/z regions to ignore: those immediately below the m/z of the m_ParentStream ion
 * which will contain uninformative neutral loss ions, and those immediately above
 * the m_ParentStream ion m/z, which will contain the m_ParentStream ion and its isotope pattern
 */

    public void removeParent(double parentLower)
    {
        final float fZ = m_fZ;
        float fParentMz = (float) (1.00727 + (m_dMH - 1.00727) / fZ);
        final double mh = getdMH();
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak peak : m_vMI) {
            final double testMass = peak.getMassChargeRatio();
            if (fParentMz - testMass >= 0.0 && fParentMz - testMass < (parentLower / fZ)) {
                holder.add(peak);
            }
            else {
                XTandemUtilities.showDroppedPeak(peak);

            }

        }
        m_vMI.removeAll(holder);
    }

    /**
     * remove any masses < lowestMass
     *
     * @param lowestMass
     */
    public void removeLowMasses(double lowestMass)
    {
        final float fZ = m_fZ;
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak peak : m_vMI) {
            final double testMass = peak.getMassChargeRatio();
            if (testMass < lowestMass) {
                holder.add(peak);
            }
            else {
                XTandemUtilities.showDroppedPeak(peak);

            }

        }
        m_vMI.removeAll(holder);
    }

    /**
     * test wheth th espectrum is noise
     *
     * @return
     */
    public boolean isNoise()
    {
        final float fZ = m_fZ;
        final double dmh = getdMH();
        float fMax = (float) (dmh / fZ);
        if (fZ == 1) {
            fMax = (float) (dmh - 600.0);
        }
        if (fZ == 2) {
            fMax = (float) (dmh - 600.0);
        }
        for (ISpectrumPeak peak : m_vMI) {
            final double testMass = peak.getMassChargeRatio();
            if (testMass > fMax) {
                return false;
            }
        }
        return true;
    }

    public boolean format()
    {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        int tStart = m_strDescription.find('&');
//        while (tStart != m_strDescription.npos) {
//            m_strDescription.replace(tStart, 1, "&amp;");
//            tStart = m_strDescription.find('&', tStart + 1);
//        }
//        tStart = m_strDescription.find('<');
//        while (tStart != m_strDescription.npos) {
//            m_strDescription.replace(tStart, 1, "&lt;");
//            tStart = m_strDescription.find('<', tStart + 1);
//        }
//        tStart = m_strDescription.find('>');
//        while (tStart != m_strDescription.npos) {
//            m_strDescription.replace(tStart, 1, "&gt;");
//            tStart = m_strDescription.find('<', tStart + 1);
//        }
//        tStart = m_strDescription.find('\"');
//        while (tStart != m_strDescription.npos) {
//            m_strDescription.replace(tStart, 1, "&quot;");
//            tStart = m_strDescription.find('\"', tStart + 1);
//        }
//        return true;
    }

    public void clear_intensity_values()
    {  // clear intensities and associated stats
        m_vMI.clear();
        m_vdStats.clear();
    }

    public ProteinSequence getBestSequence()
    {
        if (m_vseqBest.isEmpty())
            return null;
        return m_vseqBest.get(0);
    }

    public boolean reset()
    {
        m_fScore = 0.0F;
        m_fHyper = 100.0F;
        m_dExpect = 1000.0;
        m_dProteinExpect = 1000.0;
        m_tCurrentSequence = (int) 0xAFFFFFFF;
        m_vseqBest.clear();
        return true;
    }

    public void clearStatistics()
    {
        m_vdStats.clear();
    }

    public void addStatistic(double d)
    {
        m_vdStats.add(d);

    }

    public double getStatistic(int i)
    {
        return m_vdStats.get(i);

    }

    public int gettId()
    {
        return m_tId;
    }

    public void settId(int pTId)
    {
        m_tId = pTId;
    }

    public int gettCurrentSequence()
    {
        return m_tCurrentSequence;
    }

    public void settCurrentSequence(int pTCurrentSequence)
    {
        m_tCurrentSequence = pTCurrentSequence;
    }

    public float getfScore()
    {
        return m_fScore;
    }

    public void setfScore(float pFScore)
    {
        m_fScore = pFScore;
    }

    public float getfHyper()
    {
        return m_fHyper;
    }

    public void setfHyper(float pFHyper)
    {
        m_fHyper = pFHyper;
    }

    public float getfScoreNext()
    {
        return m_fScoreNext;
    }

    public void setfScoreNext(float pFScoreNext)
    {
        m_fScoreNext = pFScoreNext;
    }

    public float getfHyperNext()
    {
        return m_fHyperNext;
    }

    public void setfHyperNext(float pFHyperNext)
    {
        m_fHyperNext = pFHyperNext;
    }

    public double getdExpect()
    {
        return m_dExpect;
    }

    public void setdExpect(double pDExpect)
    {
        m_dExpect = pDExpect;
    }

    public double getdProteinExpect()
    {
        return m_dProteinExpect;
    }

    public void setdProteinExpect(double pDProteinExpect)
    {
        m_dProteinExpect = pDProteinExpect;
    }

    public double getdMH()
    {
        return m_dMH;
    }

    public void setdMH(double pDMH)
    {
        m_dMH = pDMH;
    }

    public float getfI()
    {
        return m_fI;
    }

    public void setfI(float pFI)
    {
        m_fI = pFI;
    }

    public float getfZ()
    {
        return m_fZ;
    }

    public void setfZ(float pFZ)
    {
        m_fZ = pFZ;
    }

    public boolean isbRepeat()
    {
        return m_bRepeat;
    }

    public void setbRepeat(boolean pBRepeat)
    {
        m_bRepeat = pBRepeat;
    }

    public boolean isbActive()
    {
        return m_bActive;
    }

    public void setbActive(boolean pBActive)
    {
        m_bActive = pBActive;
    }

    public String getStrDescription()
    {
        return m_strDescription;
    }

    public void setStrDescription(String pStrDescription)
    {
        m_strDescription = pStrDescription;
    }

    public String getStrRt()
    {
        return m_strRt;
    }

    public void setStrRt(String pStrRt)
    {
        m_strRt = pStrRt;
    }

    public SpectrumHistogram gethHyper()
    {
        return m_hHyper;
    }

    public void sethHyper(SpectrumHistogram pHHyper)
    {
        m_hHyper = pHHyper;
    }

    public SpectrumHistogram gethConvolute()
    {
        return m_hConvolute;
    }

    public void sethConvolute(SpectrumHistogram pHConvolute)
    {
        m_hConvolute = pHConvolute;
    }

    public CountHistogram getChBCount()
    {
        return m_chBCount;
    }

    public void setChBCount(CountHistogram pChBCount)
    {
        m_chBCount = pChBCount;
    }

    public CountHistogram getChYCount()
    {
        return m_chYCount;
    }

    public void setChYCount(CountHistogram pChYCount)
    {
        m_chYCount = pChYCount;
    }
}
