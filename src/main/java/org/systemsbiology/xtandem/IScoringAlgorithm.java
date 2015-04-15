package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.IScoringAlgorithm
 * an interface implemented by the oiece of code that does scoring
 * todo consider a major refactor
 *
 * @author Steve Lewis
 *         how scoring is to work
 */
public interface IScoringAlgorithm  extends Serializable {
    public static IScoringAlgorithm[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IScoringAlgorithm.class;

    /**
     * return a unique algorithm name
     *
     * @return
     */
    public String getName();

    /**
     * are we scoring mono average
     *
     * @return as above
     */
    public MassType getMassType();

    public boolean isWithinLimits(double scanMass, double mass2, int charge);

    /**
     * return the low and high limits of a mass scan
     *
     * @param scanMass
     * @return as above
     */
    public int[] highAndLowMassLimits(double scanMass);

    /**
     * return the low and high limits of a mass scan
     *
     * @param scanMass
     * @return as above
     */
    public int[] allSearchedMasses(double scanMass);


    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    public void configure(IParameterHolder params);

    /**
     * alter the score from dot_product in algorithm depdndent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return new score
     */
    public double conditionScore(double score, IMeasuredSpectrum measured, ITheoreticalSpectrumSet theory,
                                 IonUseScore counter);

    /**
     * return false if the algorithm will not score the spectrum
     * @param !null spectrum measured
     * @return   as above
     */
    public boolean canScore(IMeasuredSpectrum measured);

    /**
     * find the hyperscore the score from dot_product in algorithm depdndent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return hyperscore
     */
    public double buildHyperscoreScore(double score, IMeasuredSpectrum measured, ITheoreticalSpectrumSet theory,
                                       IonUseScore counter);


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

    public double dot_product(IMeasuredSpectrum measured, ITheoreticalSpectrum theory,
                              IonUseCounter counter, List<DebugMatchPeak> holder, Object... otherData);


    /**
     * test for acceptability and generate a new conditioned spectrum for
     * scoring
     *
     * @param in !null spectrum
     * @return null if the spectrum is to be ignored otherwise a conditioned spectrum
     */
    public IMeasuredSpectrum conditionSpectrum(IScoredScan scan, final SpectrumCondition sc);


    public IMeasuredSpectrum conditionSpectrum(  final IMeasuredSpectrum pIn, final IMeasuredSpectrum pRaw);

    /**
     * return the expected value for the best score
     *
     * @param scan !null scan
     * @return as above
     */
    public double getExpectedValue(final IScoredScan scan);

    /**
     * modify the theoretical spectrum before scoring - this may
     * involve reweighting or dropping peaks
     *
     * @param pTs !null spectrum
     * @return !null modified spectrum
     */
    public ITheoreticalSpectrum buildScoredScan(ITheoreticalSpectrum pTs);



    /**
      * best leave in the air what the theoretical set is
      * @param scorer
      * @param pPeptide
      * @return
      */
     public ITheoreticalSpectrumSet generateSpectrum(Scorer scorer,final IPolypeptide pPeptide);

}
