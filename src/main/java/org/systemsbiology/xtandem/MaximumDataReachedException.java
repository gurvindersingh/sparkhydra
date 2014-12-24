package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.MaximumDataReachedException
 *   not a real exception but a great way top  limit data size during debugging
 * User: steven
 * Date: 1/31/11
 */
public class MaximumDataReachedException extends RuntimeException{
    public static final MaximumDataReachedException[] EMPTY_ARRAY = {};

    /**
     * Constructs a new runtime exception with <code>null</code> as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public MaximumDataReachedException() {
        super("Maximum Dta is reached");
    }
}
