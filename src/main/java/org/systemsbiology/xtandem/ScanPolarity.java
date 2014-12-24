package org.systemsbiology.xtandem;

import java.util.*;

/**
 * org.systemsbiology.xtandem.ScanPolarity
 * User: steven
 * Date: Jan 3, 2011
 */
public enum ScanPolarity {
    plus,minus,any;
    public static final ScanPolarity[] EMPTY_ARRAY = {};

    public static final Map<String,ScanPolarity> STRING_MAPPING = new HashMap<String,ScanPolarity>();
    // Should always work SLewis
    static {
        STRING_MAPPING.put(plus.toString(),plus);
        STRING_MAPPING.put(minus.toString(),minus);
        STRING_MAPPING.put(any.toString(),any);
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
        switch(this) {
            case plus:
                return "+";
            case minus:
                return "-";
        }
        return super.toString();
    }
}
