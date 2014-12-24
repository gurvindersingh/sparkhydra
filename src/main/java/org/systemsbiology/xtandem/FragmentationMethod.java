package org.systemsbiology.xtandem;

import java.util.*;

/**
 * org.systemsbiology.xtandem.FragmentationMethod
 * User: steven
 * Date: Jan 3, 2011
 */
public enum FragmentationMethod {
    ETD,
    ECD,
    CID,
    HCD,
    PQD,
    PSD,
    ETD_PLUS_SA;
    public static final FragmentationMethod[] EMPTY_ARRAY = {};

    public static final Map<String, FragmentationMethod> STRING_MAPPING = new HashMap<String, FragmentationMethod>();
    public static final Map<String, FragmentationMethod> MZML_MAPPING = new HashMap<String, FragmentationMethod>();

       // Should always work SLewis
    static {
        STRING_MAPPING.put(ETD.toString(), ETD);
        STRING_MAPPING.put(ECD.toString(), ECD);
        STRING_MAPPING.put(CID.toString(), CID);
        STRING_MAPPING.put(HCD.toString(), HCD);
        STRING_MAPPING.put(PQD.toString(), PQD);
        STRING_MAPPING.put(PSD.toString(), PSD);
          STRING_MAPPING.put(ETD_PLUS_SA.toString(), ETD_PLUS_SA);

        MZML_MAPPING.put("electron transfer dissociation", ETD);
        MZML_MAPPING.put("electron capture dissociation", ECD);
        MZML_MAPPING.put("collision-induced dissociation", CID);
        MZML_MAPPING.put("high-energy collision-induced dissociation", HCD);
        MZML_MAPPING.put("pulsed-q dissociation", PQD);
        MZML_MAPPING.put( "post-source decay", PSD);
        MZML_MAPPING.put("electron transfer dissociation plus sa", ETD_PLUS_SA);
    }

    public static FragmentationMethod fromSpectrumPrecursorString(String s)
    {
        return MZML_MAPPING.get(s);
    }

    /**
     * Returns the name of this enum constant, as contained in the
     * declaration.  This method may be overridden, though it typically
     * isn't necessary or desirable.  An enum type should override this
     * method when a more "programmer-friendly" string form exists.
     *
     * @return the name of this enum constant
     */
    @Override
    public String toString() {
        switch (this) {
            case ETD_PLUS_SA:
                return "ETD+SA";
            default:
                return super.toString();
        }
    }

}
