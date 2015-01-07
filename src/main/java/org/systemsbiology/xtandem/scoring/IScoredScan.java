package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

/**
 * org.systemsbiology.xtandem.scoring.IScoredScan
 * User: steven
 * Date: 1/28/11
 */
public interface IScoredScan extends IonTypeScorer , Comparable<IScoredScan>
{


      /**
     * combine two scores
     *
     * @param added
     */
    public void addTo(IScoredScan added);

    /**
      * make a form suitable to
      *    1) reconstruct the original given access to starting conditions
      *
      * @param adder !null where to put the data
      */
     public void serializeAsString(IXMLAppender adder);

    /**
     * return the scan identifier
     * @return as above
     */
    public String getId();

    /**
     * return version
     * @return as above
     */
    public String getVersion();

    /**
     * return algorithm name
     * @return as above
     */
    public String getAlgorithm();

    /**
     * return true of the scan is OK
     * @return as above
     */
    public boolean isValid();

    /**
      * true if some match is scored
      *
      * @return as above
      */
       public boolean isMatchPresent();


    public int getNumberScoredPeptides();

    public IMeasuredSpectrum getRaw();

    /**
     * rention time as a string
     * @return  possibly null string representation
     */
    public String getRetentionTimeString();

    /**
      * rention time as a seconds
      * @return  possibly null 0
      */
     public double getRetentionTime();

    public IMeasuredSpectrum getNormalizedRawScan();

    public IMeasuredSpectrum getConditionedScan();

    public IMeasuredSpectrum conditionScan(IScoringAlgorithm alg,final SpectrumCondition sc);
    /**
     * return the base ion charge
     * @return as above
     */
    public int getCharge();

    /**
     * return the base mass pluss the mass of a proton
     * @return as above
     */
    public double getMassPlusHydrogen();

    /**
     * return
     * @return as above
     */
    public double getExpectedValue();

    public void setExpectedValue(double pExpectedValue);

    public double getSumIntensity();

    public double getMaxIntensity();

    public double getFI();

    public HyperScoreStatistics getHyperScores();

    public VariableStatistics getScoreStatistics();

    /**
     * get all matches
     * @return
     */
    public ISpectralMatch[] getSpectralMatches();

    public ISpectralMatch getBestMatch();

    public ISpectralMatch getNextBestMatch();

    public double getNormalizationFactor();

    public double getMassDifferenceFromBest();

}
