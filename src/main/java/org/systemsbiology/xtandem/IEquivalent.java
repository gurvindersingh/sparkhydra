package org.systemsbiology.xtandem;

import java.io.*;

/**
 * org.systemsbiology.xtandem.IEquivalent
 *   a weaker test the equals - this ii used alot in testing
 * @author Steve Lewis
  */
public interface IEquivalent<T> extends Serializable
{
    public static IEquivalent[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IEquivalent.class;

    /**
     * return true if this and o are 'close enough'
     * @param o !null test object
     * @return as above
     */
    public boolean equivalent(T o);

}
