package org.systemsbiology.xtandem.scoring;

import java.io.*;

/**
 * org.systemsbiology.xtandem.scoring.IMZToInteger
 * peaks are represented as doubles but in many scoring algorithms it is easier to
 * handle them as ints because doubles do noe compare easily - this handles
 * the process of converting a double to an int. With one dlaton resolution simple
 * round off is good enough
 * User: Steve
 * Date: 9/4/11
 */
public interface IMZToInteger extends Serializable {
    public static final IMZToInteger[] EMPTY_ARRAY = {};



    public static final IMZToInteger DALTON_RESOLUTION = new DaltonResolution();

    /**
     * convera a double - usually an mz to an integer
     * @param d  the double - should be positive
     * @return the integer
     */
    public int asInteger(double d);

    public static class DaltonResolution implements IMZToInteger {
        private DaltonResolution() {
        }

        /**
         * convera a double - usually an mz to an integer
         *
         * @param d the double - should be positive
         * @return the integer
         */
        @Override
        public int asInteger(final double d) {
            return (int)(d + 0.5);
        }
    }

}
