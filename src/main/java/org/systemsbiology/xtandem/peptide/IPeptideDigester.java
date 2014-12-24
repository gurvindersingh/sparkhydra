package org.systemsbiology.xtandem.peptide;

import java.io.*;

/**
 * org.systemsbiology.xtandem.peptide.IPeptideDigester
 * User: steven
 * Date: Jan 10, 2011
 */
public interface IPeptideDigester extends Serializable {
    public static final IPeptideDigester[] EMPTY_ARRAY = {};

      /**
     * allow sequences where only one end is at the boundary
     * @return
     */
    public boolean isSemiTryptic();

    /**
     * allow sequences where only one end is at the boundary
     * @return
     */
    public void setSemiTryptic(boolean isSo);

    /**
     * according to the rules are missed cleavages seen - may miss a few because
     * of terminat colditions
     * @param sequence !null peptide
     * @return  as above
     */
    public int probableNumberMissedCleavages(IPolypeptide pp);
    /**
     * maximum number missed cleavages allowed
     * @return as above
     */
    public int getNumberMissedCleavages();

    /**
     * maximum number missed cleavages allowed
     * @param pNumberMissedCleavages as above
     */
    public void setNumberMissedCleavages(final int pNumberMissedCleavages);

    /**
     * split a polypeptied into a set of fragments
     * @param in !null polypeptide
     * @param addedData  any other data we need
     * @return !null array of fragments
     */
    IPolypeptide[] digest(IPolypeptide in,Object... addedData);

    /**
      * split a polypeptied into a set of fragments  including semitryptic fragments
      * @param in !null polypeptide
      * @param addedData  any other data we need
      * @return !null array of fragments
      */
    public IPolypeptide[] addSemiCleavages(final IPolypeptide in, final Object... addedData);

}
