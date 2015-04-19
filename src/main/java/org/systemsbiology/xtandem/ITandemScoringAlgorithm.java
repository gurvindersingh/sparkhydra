package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import javax.annotation.*;

/**
 * org.systemsbiology.xtandem.ITandemScoringAlgorithm
 * User: steven
 * Date: 1/14/11
 */
public interface ITandemScoringAlgorithm extends IScoringAlgorithm {
    public static final ITandemScoringAlgorithm[] EMPTY_ARRAY = {};



    public float getSpectrumMassError();


    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    @Override
    public void configure(IParameterHolder params);


    /**
     * return the product of the factorials of the counts
     *
     * @param counter - !null holding counts
     * @return as above
     */
    public double getCountFactor(IonUseScore counter);

    /**
     * an algorithm may choose not to score a petide - for example high resolution algorithms may
     * choose not to score ppetides too far away
     * @param ts !null peptide spectrum
     * @param pScan  !null scan to score
     * @return true if scoring is desired
     */
    public boolean isTheoreticalSpectrumScored(@Nonnull ITheoreticalSpectrum ts,@Nonnull IMeasuredSpectrum pScan);

    /**
     * test whether so score a pair if not we can save a lot of time
     * @param pSpec
     * @param pPp
     * @return
     */
    public  boolean isPairScored(@Nonnull final IMeasuredSpectrum pSpec,@Nonnull  final IPolypeptide pPp);

    /**
     * score the two spectra
     *
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @return value of the score
     */
    public double scoreSpectrum(@Nonnull IMeasuredSpectrum measured,@Nonnull  ITheoreticalSpectrum theory, Object... otherdata);

    /**
     * actually do the scorring
     * @param scorer  !null scorrer
     * @param scan  !null scan to score
     * @param pPps  !null set of peptides ot score
     * @param tss  !null set of Theoretical Spectra ot score
      * @return !null score
     */
    public IScoredScan handleScan(@Nonnull final Scorer scorer, @Nonnull  final IMeasuredSpectrum scan, final IPolypeptide[] pPps, ITheoreticalSpectrumSet[] tss );


    public int scoreScan(@Nonnull final Scorer scorer, final IonUseCounter pCounter, final ITheoreticalSpectrumSet[] pSpectrums, final IScoredScan pConditionedScan);




 }
