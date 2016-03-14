package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringResult
 * A low mwmory version os a scoring result
 *
 * @author Steve Lewis
  */
public class CometScoringResult implements IScoredScan, IAddable<IScoredScan>, IMeasuredSpectrum {

    public static final int MAX_RETURNED_MATCHES = 2;

    private String m_id;
    private IMeasuredSpectrum m_Raw;
    private boolean matchesSorted;
 //   private final Set<IPolypeptide> usedPeptides = new HashSet<IPolypeptide>();
    private  List<PeptideMatchScore> matches = new ArrayList<PeptideMatchScore>();
    //    private final IonUseScore m_IonUse = new IonUseCounter();
//    //    private List<ISpectralMatch> m_Matches;
//    private final HyperScoreStatistics m_HyperScores = new HyperScoreStatistics();
//    protected final VariableStatistics m_ScoreStatistics = new VariableStatistics();
    private int numberZeroResults;

    /**
     * make an empty result
     */
    public CometScoringResult() {
    }


    /**
     * make a result with a spectrum
     */
    public CometScoringResult(IMeasuredSpectrum raw) {
        this();
       setRaw(raw);

    }


    public void setId(String id) {
        m_id = id;
    }

    public boolean isMatchesSorted() {
        return matchesSorted;
    }

    public void setMatchesSorted(boolean matchesSorted) {
        this.matchesSorted = matchesSorted;
    }

    public List<PeptideMatchScore> getMatches() {
        return matches;
    }

    public void setMatches(List<PeptideMatchScore> matches) {
        this.matches = matches;
    }

    public int getNumberZeroResults() {
        return numberZeroResults;
    }

    public void setNumberZeroResults(int numberZeroResults) {
        this.numberZeroResults = numberZeroResults;
    }

    public void setRaw(IMeasuredSpectrum raw) {
        m_Raw = raw;
        m_id = raw.getId();
    }


    private void addMatch(PeptideMatchScore pm) {
        matchesSorted = false;
        final IPolypeptide peptide = pm.peptide;

        if (TestUtilities.isInterestingPeptide(peptide))
            TestUtilities.breakHere();

        matches.add(pm);
//        if (!usedPeptides.contains(peptide)) {
//            usedPeptides.add(peptide);
//        } else {
//            TestUtilities.breakHere(); // duplicate this is bad
//        }
    }


    public void addSpectralMatch(ISpectralMatch bestMatch) {
        final IPolypeptide peptide = bestMatch.getPeptide();
        final double hyperScore = bestMatch.getHyperScore();
        if (hyperScore <= CometScoringAlgorithm.XCORR_CUTOFF) {
            numberZeroResults++;
        } else {
            IonTypeScorer ions = new LowMemoryIonScorer(bestMatch);
            addMatch(new PeptideMatchScore(peptide, hyperScore, ions,getId()));
        }
        keepOnlyHighestMatches();


    }

    protected void keepOnlyHighestMatches() {
        // keep only top   MAX_RETURNED_MATCHES matches
        if(matches.size() > 2 * MAX_RETURNED_MATCHES) {
            // highest scores first
            Collections.sort(matches);
            //,new Comparator<PeptideMatchScore>() {
//                @Override
//                public int compare(PeptideMatchScore o1, PeptideMatchScore o2) {
//                    return Double.compare(o2.score, o1.score);
//                }
//            });
            List<PeptideMatchScore> savedMatches = new ArrayList<PeptideMatchScore>(matches.subList(0, MAX_RETURNED_MATCHES));
            matches.clear();
            matches.addAll(savedMatches);
        }
    }


    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IMeasuredSpectrum test) {
        if (m_Raw == null)
            return false;

        return m_Raw.equivalent(test);
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
        if (true) throw new UnsupportedOperationException("Fix This"); // we are not mutable and do not need to be
        return null;
    }

    /**
     * get the charge of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public int getPrecursorCharge() {
        if (m_Raw == null)
            return 0;
        return m_Raw.getPrecursorCharge();
    }

    /**
     * get the mass of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public double getPrecursorMass() {
        if (m_Raw == null)
            return 0;
        return m_Raw.getPrecursorMass();
    }

    /**
     * get the mz of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public double getPrecursorMassChargeRatio() {
        if (m_Raw == null)
            return 0;
        return m_Raw.getPrecursorMassChargeRatio();
    }

    /**
     * Mass spec characteristics
     *
     * @return as above
     */
    @Override
    public ISpectralScan getScanData() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(ISpectrum o) {
        if (m_Raw == null)
            return false;
        return m_Raw.equivalent(o);
    }

    /**
     * combine two scores
     *
     * @param added
     */
    @Override
    public void addTo(IScoredScan added) {
        if (m_Raw == null) {
            m_Raw = added.getRaw();
        } else {
            if (!added.getId().equals(getId()))
                throw new IllegalArgumentException("add scan with id " + added.getId() +
                        " to reault with id " + getId());
        }
        if (added instanceof CometScoringResult) {
            CometScoringResult addedResult = (CometScoringResult) added;
            numberZeroResults += addedResult.numberZeroResults;
            matches.addAll(addedResult.matches);
            matchesSorted = false;
        } else {
            throw new UnsupportedOperationException("Fix This"); // ToDo
        }
        keepOnlyHighestMatches();

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
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }

    /**
     * get the score for a given ion type
     *
     * @param type !null iontype
     * @return score for that type
     */
    @Override
    public double getScore(IonType type) {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }

    /**
     * get the count for a given ion type
     *
     * @param type !null iontype
     * @return count for that type
     */
    @Override
    public int getCount(IonType type) {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
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

    /**
     * return the scan identifier
     *
     * @return as above
     */
    @Override
    public String getId() {
        if (m_Raw == null)
            return null;
        return m_Raw.getId();
    }

    /**
     * return the scan index
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

    /**
     * return version
     *
     * @return as above
     */
    @Override
    public String getVersion() {
        return CometScoringAlgorithm.DEFAULT_VERSION;
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

    /**
     * return true of the scan is OK
     *
     * @return as above
     */
    @Override
    public boolean isValid() {
        if (m_Raw == null)
            return false;
        return true;
    }

    /**
     * return true of the scan is OK  and there is a best match
     * This means we do not have to construct a best match
     *
     * @return as above
     */
    @Override
    public boolean isValidMatch() {
        if (!isValid())
            return false;
        if (!isMatchPresent())
            return false;
        return true;
    }

    /**
     * true if some match is scored
     *
     * @return as above
     */
    @Override
    public boolean isMatchPresent() {
        return !matches.isEmpty();
    }

    @Override
    public int getNumberScoredPeptides() {
        return matches.size();
    }

    @Override
    public IMeasuredSpectrum getRaw() {
        return m_Raw;
    }

    /**
     * rention time as a string
     *
     * @return possibly null string representation
     */
    @Override
    public String getRetentionTimeString() {
        if (m_Raw == null)
            return null;
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
    public IMeasuredSpectrum getNormalizedRawScan() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    @Override
    public IMeasuredSpectrum getConditionedScan() {
        return getRaw();
    }

    @Override
    public IMeasuredSpectrum conditionScan(IScoringAlgorithm alg, SpectrumCondition sc) {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    /**
     * return the base ion charge
     *
     * @return as above
     */
    @Override
    public int getCharge() {
        if (m_Raw == null)
            return 0;
        return m_Raw.getPrecursorCharge();
    }

    /**
     * return the base mass pluss the mass of a proton
     *
     * @return as above
     */
    @Override
    public double getMassPlusHydrogen() {
        if (m_Raw == null)
            return 0;
        return m_Raw.getPrecursorMass();
    }

    /**
     * return
     *
     * @return as above
     */
    @Override
    public double getExpectedValue() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }

    @Override
    public void setExpectedValue(double pExpectedValue) {
        if (true) throw new UnsupportedOperationException("Fix This");

    }

    @Override
    public double getSumIntensity() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }

    /**
     * get the number of peaks without returning the peaks
     *
     * @return as above
     */
    @Override
    public int getPeaksCount() {
        if (m_Raw == null)
            return 0;
        return m_Raw.getPeaksCount();
    }

    /**
     * spectrum - this might have been adjusted
     *
     * @return 1=!null array
     */
    @Override
    public ISpectrumPeak[] getPeaks() {
        if (m_Raw == null)
            return ISpectrumPeak.EMPTY_ARRAY;
        return m_Raw.getPeaks();
    }

    /**
     * get all peaks with non-zero intensity
     *
     * @return
     */
    @Override
    public ISpectrumPeak[] getNonZeroPeaks() {
        if (m_Raw == null)
            return ISpectrumPeak.EMPTY_ARRAY;
        return m_Raw.getNonZeroPeaks();
    }

    @Override
    public double getMaxIntensity() {
        if (m_Raw == null)
            return 0;
        return m_Raw.getMaxIntensity();
    }

    @Override
    public double getFI() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }

    @Override
    public HyperScoreStatistics getHyperScores() {
        HyperScoreStatistics ret = new HyperScoreStatistics();
        guaranteeMatchesSorted();
        for (PeptideMatchScore match : matches) {
            ret.add(match.score);
        }
        for (int i = 0; i < numberZeroResults; i++) {
            ret.add(0);
        }
        return ret;
    }

    @Override
    public VariableStatistics getScoreStatistics() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }


    /**
     * get all matches
     *
     * @return
     */
    @Override
    public ISpectralMatch[] getSpectralMatches() {
        if (matches.isEmpty())
            return ISpectralMatch.EMPTY_ARRAY;
        guaranteeMatchesSorted();
        List<ISpectralMatch> holder = new ArrayList<ISpectralMatch>();
        for (PeptideMatchScore match : matches) {
            final ISpectralMatch sm = buildMatch(match);
            holder.add(sm);
            if (holder.size() >= MAX_RETURNED_MATCHES)
                break;
        }
        ISpectralMatch[] ret = new ISpectralMatch[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    @Override
    public ISpectralMatch getBestMatch() {
        if (matches.isEmpty())
            return null;
        guaranteeMatchesSorted();
        final PeptideMatchScore peptideMatchScore = matches.get(0);
        return buildMatch(peptideMatchScore);
    }

    private ISpectralMatch buildMatch(PeptideMatchScore peptideMatchScore) {
        SpectralMatch sm = new SpectralMatch(
                peptideMatchScore.peptide,
                getRaw(),
                peptideMatchScore.score,
                peptideMatchScore.score,
                peptideMatchScore.score,
                peptideMatchScore.ions,   // IonTyoeScorer
                null    // ITheoreticalSpectrumSet
        );
        return sm;
    }

    private void guaranteeMatchesSorted() {
        synchronized (matches) {
            if (!matchesSorted) {
                Collections.sort(matches);
                matchesSorted = true;
            }
        }

    }

    @Override
    public ISpectralMatch getNextBestMatch() {
        if (matches.size() < 2)
            return null;
        guaranteeMatchesSorted();
        final PeptideMatchScore peptideMatchScore = matches.get(1);
        return buildMatch(peptideMatchScore);
    }

    @Override
    public double getNormalizationFactor() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }

    @Override
    public double getMassDifferenceFromBest() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return 0;
    }

    @Override
    public int compareTo(final IScoredScan o) {
        if (o == this)
            return 0;
        return getId().compareTo(o.getId());

    }

}
