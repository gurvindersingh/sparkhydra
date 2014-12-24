package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.IModifiedPeptide
 * User: steven
 * Date: 6/30/11
 */
public interface IModifiedPeptide extends IPolypeptide {
    public static final IModifiedPeptide[] EMPTY_ARRAY = {};


    /**
     * get the modification from the classic mass
      * @return
     */
    public double getMassModification();
    /**
      * add m[-18] for a modification
      * @return
      */
     public String getModifiedSequence();
    /**
      * add m[123.5] for a modification
      * @return
      */
     public String getTotalModifiedSequence();

    /**
       * describe the modifications
       * @return
       */
       public String getModificationString();
    /**
       * return applied modifications
       * @return
       */
       public PeptideModification[] getModifications();


}
