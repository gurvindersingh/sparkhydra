package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.IXMLAppender;
import org.systemsbiology.xtandem.IMeasuredSpectrum;
import org.systemsbiology.xtandem.XTandemUtilities;
import org.systemsbiology.xtandem.ionization.ITheoreticalSpectrumSet;
import org.systemsbiology.xtandem.ionization.IonType;
import org.systemsbiology.xtandem.ionization.IonTypeScorer;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.IProteinPosition;

/**
 * org.systemsbiology.xtandem.scoring.SimpleSpectralMatch
 * result of scoring set as a bean
 * User: steven
 * Date: 1/28/11
 */
public class SimpleSpectralMatch implements   ISpectralMatch,Comparable<ISpectralMatch> {

    private   IPolypeptide m_Peptide;
    private   String spectrumId;
    private   double m_Score;
    private   double m_RawScore;
    private   double m_HyperScore;
    private   IonTypeScorer m_IonScorer;


    /**
     * @param pp
     * @param pMeasured
     * @param pScore
     * @param pHyperScore
     * @param scorer
     */
    public SimpleSpectralMatch(final IPolypeptide pp, final String pMeasured,
                               final double pScore, final double pHyperScore, final double rawScore,
                               IonTypeScorer scorer, ITheoreticalSpectrumSet theory) {
        m_Peptide = pp;
        spectrumId = pMeasured;
        m_Score = pScore;
        m_RawScore = rawScore;
        m_HyperScore = pHyperScore;
        m_IonScorer = new LowMemoryIonScorer(scorer); //scorer;   // todo is this OK
      }

    public IPolypeptide getPeptide() {
        return m_Peptide;
    }

    public void setPeptide(IPolypeptide m_Peptide) {
        this.m_Peptide = m_Peptide;
    }

    public String getSpectrumId() {
        return spectrumId;
    }

    public void setSpectrumId(String spectrumId) {
        this.spectrumId = spectrumId;
    }

    public double getScore() {
        return m_Score;
    }

    public void setScore(double m_Score) {
        this.m_Score = m_Score;
    }

    public double getRawScore() {
        return m_RawScore;
    }

    public void setRawScore(double m_RawScore) {
        this.m_RawScore = m_RawScore;
    }

    public double getHyperScore() {
        return m_HyperScore;
    }

    public void setHyperScore(double m_HyperScore) {
        this.m_HyperScore = m_HyperScore;
    }

    public IonTypeScorer getIonScorer() {
        return m_IonScorer;
    }

    public void setIonScorer(IonTypeScorer m_IonScorer) {
        this.m_IonScorer = m_IonScorer;
    }

    /**
     * get the total peaks matched for all ion types
     *
     * @return
     */
    @Override
    public int getNumberMatchedPeaks() {
        return m_IonScorer.getNumberMatchedPeaks();
    }

    /**
     * get the score for a given ion type
     *
     * @param type !null iontype
     * @return score for that type
     */
    @Override
    public double getScore(IonType type) {
        return m_IonScorer.getScore(type);
    }

    /**
     * get the count for a given ion type
     *
     * @param type !null iontype
     * @return count for that type
     */
    @Override
    public int getCount(IonType type) {
        return m_IonScorer.getCount(type);
    }


    @Override
    public int compareTo(final ISpectralMatch o) {
        if (o == this)
            return 0;
        // sort to put highest first
        return -ScoringUtilities.compareDoubles(getHyperScore(), o.getHyperScore());
    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    @Override
    public void serializeAsString(final IXMLAppender adder) {
        IPolypeptide pp = getPeptide();
        boolean isDecoy = pp.isDecoy();
        adder.openTag("match");
        String pps = pp.toString();
        if(isDecoy && pps.startsWith("DECOY_"))
            pps = pps.substring("DECOY_".length()) ; // drop decoy from string
        adder.appendAttribute("peak", pps);  // include modifications
        adder.appendAttribute("score", getScore());
        adder.appendAttribute("hyperscore", getScore());
          if(isDecoy)
            adder.appendAttribute("decoy", "yes");
        adder.endTag();
        adder.cr();
        getUsage().serializeAsString(adder);
          IProteinPosition[] proteinPositions = pp.getProteinPositions();
        for (int i = 0; i < proteinPositions.length; i++) {
            IProteinPosition proteinPosition = proteinPositions[i];
             proteinPosition.serializeAsString(adder);
        }
        adder.closeTag("match");


    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(ISpectralMatch test) {
        if (test == this)
            return true;

        if (test instanceof IonTypeScorer) {
            if (!equivalent((IonTypeScorer) test))
                return equivalent((IonTypeScorer) test);
        }
        final IMeasuredSpectrum tmeasured = test.getMeasured();
        final IMeasuredSpectrum measured = getMeasured();
        if (measured != null) {
            if (tmeasured == null || !measured.equivalent(tmeasured))
                return measured.equivalent(tmeasured);
        }

        return true;
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
            double score = getScore(type);
            double score1 = test.getScore(type);
            if (!XTandemUtilities.equivalentDouble(score, score1))
                return XTandemUtilities.equivalentDouble(getScore(type), score1);

        }
        return true;
    }

    @Override
    public SpectralPeakUsage getUsage() {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setActive(boolean pActive) {

    }

    @Override
    public ITheoreticalSpectrumSet getTheory() {
        return null;
    }

    @Override
    public IMeasuredSpectrum getMeasured() {
        return null;
    }

    @Override
    public void addTo(ISpectralMatch added) {

    }
}
