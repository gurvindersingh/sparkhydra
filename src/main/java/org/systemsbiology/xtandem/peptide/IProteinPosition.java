package org.systemsbiology.xtandem.peptide;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.peptide.IProteinPosition
 * User: steven
 * Date: 2/8/12
 */
public interface IProteinPosition extends Serializable {
    public static final IProteinPosition[] EMPTY_ARRAY = {};

    /**
     * retutn the id of the protein - cleaned up
     * @return  !null string
     */
    public String getProtein();

    /**
      * unique id - descr up to first space
      * @return
      */
     public String getProteinId();

    /**
     * start in the protein
     * @return non-negative int
     */
    public int getStartPosition();

    /**
     * return next aa or null
     * @return   as above
     */
    public FastaAminoAcid getAfter();

    /**
      * return previous aa or null
      * @return   as above
      */
     public FastaAminoAcid getBefore();

    /**
       * return containing peptide
       * @return !null peptide
       */
    public IPolypeptide getPeptide();

    /**
     * serialize without the peptide
     * @return  !null string
     */
    public String asPeptidePosition();

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     * 2) serialize
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder);

}
