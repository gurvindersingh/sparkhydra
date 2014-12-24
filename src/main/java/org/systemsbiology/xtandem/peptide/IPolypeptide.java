package org.systemsbiology.xtandem.peptide;


import org.systemsbiology.xtandem.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.peptide.IPolypeptide
 * User: steven
 * Date: Jan 10, 2011
 */
public interface  IPolypeptide extends Serializable{
    public static final IPolypeptide[] EMPTY_ARRAY = {};

    public static final String KEY_SEPARATOR = "|";

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    public boolean equivalent(IPolypeptide test);

    /**
     * !null validity may be unknown
     * @return
     */
    public PeptideValidity getValidity();

    public String getId();

    /**
      * return the reversed peptide marked as a decoy unless this is already a decoy
      * @return
      */
     public IPolypeptide asDecoy();


    /**
     * true is the polypaptide is known to be a decoy
     * @return
     */
    public boolean isDecoy();


    /**
     * true is the polypaptide is known to be a protein
     * @return
     */
    public boolean isProtein();

    /**
     * true if the peptide is SewmiTryptic but may
     * miss instance where K or R is followed by aP which
     * are semitryptic
     * @return
     */
    public boolean isProbablySemiTryptic();

    /**
     * count the occurrance of an amino acid in the sequence
     * @param aa  !null amino acid
     * @return   count of presence
     */
    public int getAminoAcidCount(FastaAminoAcid aa);

    /**
      * is the Amino Acid Present
      * @param aa  !null amino acid
      * @return true if present
      */
     public boolean hasAminoAcid(FastaAminoAcid aa);

    /**
     * return the N Terminal amino acid
     * @return
     */
     public FastaAminoAcid getNTerminal() ;

    /**
     * return the C Terminal amino acid
     * @return
     */
     public FastaAminoAcid getCTerminal() ;

    /**
      * is the Amino Acid Present and not modified
      * @param aa  !null amino acid
      * @return true if present
      */
     public boolean hasUnmodifiedAminoAcid(FastaAminoAcid aa);
     /**
     * count the occurrance of an amino acid in the sequence
     * @param aa  !null amino acid  letter
     * @return   count of presence
     */
    public int getAminoAcidCount(String aa) ;

    /**
     * true if there is at least one modification
      * @return
     */
    public boolean  isModified();


    /**
     * check for common errors like * in AA seqience
     *
     * @return
     */
    public boolean isValid();

    /**
     * check for ambiguous peptides like *
     *
     * @return
     */
    public boolean isUnambiguous();

    /**
     * return the mass calculated with the default calculator
     * monoisotopic or average
     *
     * @return as above
     */
    public double getMass();

    /**
     * mass used to see if scoring rowks
     *
     * @return
     */
    public double getMatchingMass();
 

    /**
     * return the length of the sequence
     *
     * @return !null String
     */
    public int getSequenceLength();

    /**
     * return the sequence as a set of characters
     *
     * @return !null String
     */
    public String getSequence();

    /**
     * return the unmodified version
     * @return
     */
    public IPolypeptide getUnModified();


    /**
     * return a list of contained proteins
     * @return !null array
     */
    public IProteinPosition[] getProteinPositions();

    /**
     * return the number of bionds in the sequence
     *
     * @return as above
     */
    public int getNumberPeptideBonds();

    /**
     * return the amino acids as chars on the N and C sides of the bond
     *
     * @param bond
     * @return
     */
    public char[] getBondPeptideChars(int bond);

    /**
     * build a ploypeptide by putting the two peptides together
     *
     * @param added !null added sequence
     * @return !null merged peptide
     */
    public IPolypeptide concat(IPolypeptide added);

    /**
     * delibrately hide the manner a peptide is cleaved to
     * support the possibility of the sequence pointing to the protein as
     * Java substring does
     *
     * @param bond non-negative bond
     * @return !null array of polypeptides
     * @throws IndexOutOfBoundsException on bad bond
     */
    public IPolypeptide[] cleave(int bond) throws IndexOutOfBoundsException;

    /**
     * deibbrately hide the manner a peptide is cleaved to
     * support the possibility of the sequence pointing to the protein as
     * Java substring does
     *
     * @param bond non-negative bond
     * @return !null array of polypeptides
     * @throws IndexOutOfBoundsException on bad bond
     */
    public IPolypeptide subsequence(int start, int end) throws IndexOutOfBoundsException;

    /**
     * return the number of missed cleavages
     *
     * @return as above
     */
    public int getMissedCleavages();

    /**
       * get the number of modified peptides
       * @return
       */
      public int getNumberModifications();


    public double getRetentionTime();

    public void setRetentionTime(final double pRetentionTime);

}
