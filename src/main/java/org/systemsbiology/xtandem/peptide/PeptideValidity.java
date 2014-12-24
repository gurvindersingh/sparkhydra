package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.PeptideValidity
 * User: Steve
 * Date: 3/9/12
 */
public enum PeptideValidity {
    Unknown,Real,Decoy,Real_and_Decoy;
    public static final PeptideValidity[] EMPTY_ARRAY = {};

    public PeptideValidity combine(PeptideValidity also)
    {
        if(Unknown == this)
             return also;
        if(Unknown == also)
             return this;
        if(also == this)
             return this;
        // not unknown and not us so we need real and decoy
        return Real_and_Decoy;

    }

    public static PeptideValidity fromString(final String pId) {
       if(pId == null)
           return Unknown;
        String uc = pId.toUpperCase().trim();
        if(uc.length()  < 3)
            return Unknown;
        if(uc.startsWith("DECOY") || uc.startsWith("RANDOM"))
            return Decoy;
         return Real;
    }
}
