package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.ionization.IPeptideIon
 * User: steven
 * Date: Jan 12, 2011
 */
public interface IPeptideIon {
    public static final IPeptideIon[] EMPTY_ARRAY = {};

    public String getSequence();

   public  IonType getType();

    public AminoTerminalType getTerminalType();

    public IPolypeptide getPeptide();

   public  int getCharge();

    public double getMassChargeRatio();


    /**
     * return the mass - which depends on charge
     * @param su  - calculator - has monoisotopic or average
     * @param charge positive charge
     * @return  as above
     */
     public double getMass(SequenceUtilities su,int charge);

    /**
      * return the mass/ charge ratio
      * @param su  - calculator - has monoisotopic or average
      * @param charge positive charge
      * @return  mass charge ratio
      */
       public double getMassChargeRatio(SequenceUtilities su,int charge);


}
