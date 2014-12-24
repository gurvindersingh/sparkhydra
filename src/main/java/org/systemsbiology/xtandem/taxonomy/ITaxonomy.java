package org.systemsbiology.xtandem.taxonomy;

import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.taxonomy.ITaxonomy
 * User: Steve
 * Date: Apr 6, 2011
 */
public interface ITaxonomy  extends Serializable {
    public static final ITaxonomy[] EMPTY_ARRAY = {};

    /**
     * given an id get the corresponding protein
     *
     * @param key
     * @return
     */
   public  IProtein getProteinById(String key);

    /**
     * find the first protein with this sequence and return the corresponding id
     *
     * @param sequence
     * @return
     */
    public String sequenceToID(String sequence);

    public void setDigester(IPeptideDigester digester );

    public IPeptideDigester getDigester( );

    /**
     * retrieve all peptides matching a specific mass
     * @param scanmass
     * @return
     */
    public IPolypeptide[] getPeptidesOfMass(double scanmass);

    /**
     * retrieve all peptides matching a specific mass
     * @param scanmass
     * @param isSemi if true get semitryptic masses
     * @return
     */
    public IPolypeptide[] getPeptidesOfMass(double scanmass,boolean isSemi);

    /**
     * retrieve all peptides matching a specific mass
     * @param scanmass
     * @param isSemi if true get semitryptic masses
     * @return
     */
    public IPolypeptide[] getPeptidesOfExactMass(int scanmass,boolean isSemi);

    /**
     * retrieve all peptides matching a specific mass interval
     * @param scanmass
     * @param isSemi if true get semitryptic masses
     * @return
     */
    public IPolypeptide[] getPeptidesOfExactMass(MassPeptideInterval interval,boolean isSemi);


    /**
     * retrieve all peptides matching a specific mass as an int
     * @param scanmass
     * @param isSemi if true get semitryptic masses
     * @return
     */
    public IPolypeptide[] getPeptidesIntegerOfMZ(int scanmass,boolean isSemi);

    /**
     * given an id get the corresponding IPolypeptide
     *
     * @param key
     * @return
     */
    public IPolypeptide getPeptideById(String key);

    public IProtein[] getProteins();

    public IProtein[] getValidProteins();

    public String getOrganism();
}
