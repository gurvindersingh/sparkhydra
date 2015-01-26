package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.taxonomy.*;

/**
 * org.systemsbiology.xtandem.IMainData
 * Interface implemented by the object used by scoring - this ias parameters and
 * other data - may have a lot less when used in the map reduce context
 * User: steven
  */
public interface IMainData extends IParameterHolder {
    public static final IMainData[] EMPTY_ARRAY = {};

    /**
      * use monoisotopic or average mass
      * @return !null masstype
      */
     public MassType getMassType();

    /**
     * what do we call the database or output directory
     * @return !null name
     */
    public String getDatabaseName();

    /**
     * how are we digesting the fragmensts
     * @return !null name
     */
    public String getDigestandModificationsString();

    public ScoringModifications getScoringMods();


    public RawPeptideScan getRawScan(String key);


    public IScoredScan getScoring(String key);

    /**
     * remove all retained data
     */
    public void clearRetainedData();



    public MassSpecRun[] getRuns();

    public ITaxonomy getTaxonomy();

    /**
     * return the digester used in scoring
     * @return
     */
    public IPeptideDigester getDigester();

    public boolean isSemiTryptic();

    public ITandemScoringAlgorithm[] getAlgorithms();


    /**
     * get the first an I presume the default scorer
     * @return
     */
     public ITandemScoringAlgorithm getScorer();

}
