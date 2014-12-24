package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.IAddable
 * represents an object which can have a similar type added to it producing a combioned
 * object
 * User: Steve
 * Date: 9/5/11
 */
public interface IAddable<T> {
    public static final IAddable[] EMPTY_ARRAY = {};

    /**
      * combine objects storin gthe results in the caller
      * @param added
      */
     public void addTo(T added);


}
