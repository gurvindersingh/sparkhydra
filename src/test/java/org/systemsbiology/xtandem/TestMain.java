package org.systemsbiology.xtandem;

import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.taxonomy.*;

/**
 * org.systemsbiology.xtandem.TestMain
 * This class may be used as a substitute for a full XTamdem implementation when writing unit tests
 * User: Steve
 * Date: Apr 15, 2011
 */
public class TestMain extends AbstractParameterHolder implements IMainData {
    public static final TestMain[] EMPTY_ARRAY = {};

    private boolean m_SemiTryptic;
    private ITaxonomy m_Taxonomy;
    private IPeptideDigester m_Digester;

    public TestMain() {
    }

    /**
     * use monoisotopic or average mass
     *
     * @return !null masstype
     */
    @Override
    public MassType getMassType() {
        return MassType.monoisotopic;
    }

      public RawPeptideScan[] getRawScans() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public ITandemScoringAlgorithm[] getAlgorithms() {
        return new ITandemScoringAlgorithm[0];
    }


    /**
     * get the first an I presume the default scorer
     *
     * @return
     */
    @Override
    public ITandemScoringAlgorithm getScorer() {
        return null;
    }

    /**
     * what do we call the database or output directory
     *
     * @return !null name
     */
    @Override
    public String getDatabaseName() {
        throw new UnsupportedOperationException("Not Implemented");
     }

    /**
     * how are we digesting the fragmensts
     *
     * @return !null name
     */
    @Override
    public String getDigestandModificationsString() {
        throw new UnsupportedOperationException("Not Implemented");
      }

    @Override
    public ScoringModifications getScoringMods() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * remove all retained data
     */
    @Override
    public void clearRetainedData() {
      }



    public boolean isSemiTryptic() {
        return m_SemiTryptic;
    }

    public void setSemiTryptic(final boolean pSemiTryptic) {
        m_SemiTryptic = pSemiTryptic;
    }

    @Override
    public RawPeptideScan getRawScan(final String key) {
        throw new UnsupportedOperationException("Not Implemented");
    }

     public IScoredScan[] getScorings() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public IScoredScan getScoring(final String key) {
        throw new UnsupportedOperationException("Not Implemented");
     }

    @Override
    public MassSpecRun[] getRuns() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public ITaxonomy getTaxonomy() {
        return m_Taxonomy;
    }

    /**
     * return the digester used in scoring
     *
     * @return !numm digester
     */
    @Override
    public IPeptideDigester getDigester() {
        return m_Digester;
    }

    public void setTaxonomy(final ITaxonomy pTaxonomy) {
        m_Taxonomy = pTaxonomy;
    }

    public void setDigester(final IPeptideDigester pDigester) {
        m_Digester = pDigester;
    }
}
