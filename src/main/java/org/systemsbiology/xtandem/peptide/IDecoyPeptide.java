package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.IDecoyPeptide
 * Used to mark a polypeptide as a decoy
 * User: steven
 * Date: 4/11/13
 */
public interface IDecoyPeptide {
    public static final IDecoyPeptide[] EMPTY_ARRAY = {};

    /**
     * the prptide is a reversed non-decoy - this returns the non-reversed form
     * used in testing
     * @return
     */
    public IPolypeptide asNonDecoy();

}
