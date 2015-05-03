package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.SpectralMatch
 * User: steven
 * Date: 1/28/11
 */
public class SpectralMatch implements ISpectralMatch, Comparable<ISpectralMatch> {


    public static final SpectralMatch[] EMPTY_ARRAY = {};

    private final IPolypeptide m_Peptide;
    private ITheoreticalSpectrumSet m_Theory;
    private   IMeasuredSpectrum m_Measured;
    private   double m_Score;
    private   double m_RawScore;
    private   double m_HyperScore;
    private   IonTypeScorer m_IonScorer;
    private final SpectralPeakUsage m_Usage = new SpectralPeakUsage(XTandemUtilities.getDefaultConverter()); // todo handle migher precision masses
    private boolean m_Active;

    /**
     * @param pp
     * @param pMeasured
     * @param pScore
     * @param pHyperScore
     * @param scorer
     */
    public SpectralMatch(final IPolypeptide pp, final IMeasuredSpectrum pMeasured,
                         final double pScore, final double pHyperScore, final double rawScore,
                         IonTypeScorer scorer,ITheoreticalSpectrumSet theory) {
        m_Peptide = pp;
        m_Measured = pMeasured;
        m_Score = pScore;
        m_RawScore = rawScore;
        m_HyperScore = pHyperScore;
        m_IonScorer = new LowMemoryIonScorer(scorer); //scorer;   // todo is this OK
        m_Theory =  theory;
    }

    public static class LowMemoryIonScorer implements IonTypeScorer,Serializable {

        private final int numberMatchedPeaks;
         private final Map<IonType,Integer>  ionCounts = new HashMap<IonType,Integer>();
        private final Map<IonType,Double>  ionScores = new HashMap<IonType,Double>();

        public LowMemoryIonScorer(IonTypeScorer source) {
            numberMatchedPeaks = source.getNumberMatchedPeaks();
            for (IonType byIonType : IonType.BY_ION_TYPES) {
                ionCounts.put(byIonType,source.getCount(byIonType));
                ionScores.put(byIonType,source.getScore(byIonType));
            }
            }

        /**
         * weak test for equality
         *
         * @param test !null test
         * @return true if equivalent
         */
        @Override
        public boolean equivalent(IonTypeScorer test) {
            if (true) throw new UnsupportedOperationException("Fix This");
            return false;
        }

        /**
         * get the total peaks matched for all ion types
         *
         * @return
         */
        @Override
        public int getNumberMatchedPeaks() {
             return numberMatchedPeaks;
        }

        /**
         * get the score for a given ion type
         *
         * @param type !null iontype
         * @return score for that type
         */
        @Override
        public double getScore(IonType type) {
             return ionScores.get(type);
        }

        /**
         * get the count for a given ion type
         *
         * @param type !null iontype
         * @return count for that type
         */
        @Override
        public int getCount(IonType type) {
             return ionCounts.get(type);
        }

        /**
         * make a form suitable to
         * 1) reconstruct the original given access to starting conditions
         *
         * @param adder !null where to put the data
         */
        @Override
        public void serializeAsString(IXMLAppender adder) {
            if (true) throw new UnsupportedOperationException("Fix This");

        }
    }

    public SpectralPeakUsage getUsage() {
        return m_Usage;
    }

    public ITheoreticalSpectrumSet getTheory() {
        return m_Theory;
    }

    public void setTheory(final ITheoreticalSpectrumSet pTheory) {
        m_Theory = pTheory;
    }

    public boolean isActive() {
        return m_Active;
    }

    public void setActive(final boolean pActive) {
        m_Active = pActive;
    }

    public IPolypeptide getPeptide() {
        return m_Peptide;
    }

    public IMeasuredSpectrum getMeasured() {
        return m_Measured;
    }

    public double getScore() {
        return m_Score;
    }

    public double getHyperScore() {
        return m_HyperScore;
    }

    public IonTypeScorer getIonScorer() {
        return m_IonScorer;
    }

    public double getRawScore() {
        return m_RawScore;
    }

    public void setMeasured(final IMeasuredSpectrum pMeasured) {
        m_Measured = pMeasured;
    }

    public void setScore(final double pScore) {
        m_Score = pScore;
    }

    public void setRawScore(final double pRawScore) {
        m_RawScore = pRawScore;
    }

    public void setHyperScore(final double pHyperScore) {
        m_HyperScore = pHyperScore;
    }

    public void addTo(ISpectralMatch added)  {
        getUsage().addTo(added.getUsage());
     //     throw new UnsupportedOperationException("Fix This"); // ToDo
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
    public String toString() {
        return "Match " + getPeptide().getSequence() +
                " score=" + XTandemUtilities.formatDouble(getScore(), 4) +
                " raw_score=" + XTandemUtilities.formatDouble(getRawScore(), 4) +
                " hyperscore=" + XTandemUtilities.formatDouble(getHyperScore(), 1)

                ;

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
        adder.appendAttribute("raw_score", getRawScore());
        if(isDecoy)
            adder.appendAttribute("decoy", "yes");
        adder.endTag();
        adder.cr();
        getUsage().serializeAsString(adder);
        getIonScorer().serializeAsString(adder);
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
}
