package org.systemsbiology.xtandem.scoring;

/**
 * org.systemsbiology.xtandem.scoring.MassModifications
 *     Global class holding modifications to AnimoCaids
 * @author Steve Lewis
 * @date Jan 11, 2011
 */
public class MassModifications
{
    public static MassModifications[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MassModifications.class;

    private static double[] gModification = new double[128];

     public static double getModification(String aminoAcid)   {
        return getModification(aminoAcid.charAt(0));
    }

    public static double getModification(char aminoAcid)   {
        return gModification[aminoAcid & 0x7f];
    }


    public static void setModification(String aminoAcid,double mod)   {
        int index =  aminoAcid.charAt(0) & 0x7f;
         gModification[index] = mod;
    }

    /**
     * imports a list of complete residue modifications, using a string with the following format:
     * m1@r1,m2@r2,m3@r3, ...
     * where mX is the mass of the modification, in Daltons, and rX is the single letter code for the
     * modified residue. there is no limit to the length of the string (but there are only 20 amino acids)
     * NOTE: lower case index values are reserved for potential modifications and may not be used here
     * @param modificationString  as above
     */
    public static void setModifications(String modificationString)   {
         String[] items = modificationString.split(",");
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
             addModification(item);
        }
    }

    public static void addModification(String modificationString)   {
        String[] items = modificationString.split("@");
        double value = Double.valueOf(items[0]);
        setModification(items[1].toUpperCase(),value);
     }


}
