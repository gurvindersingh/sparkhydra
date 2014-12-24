package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.ScoreError
 * @author Steve Lewis
 * @date Dec 30, 2010
 */
public class ScoreError
{
    public static ScoreError[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ScoreError.class;


  //  enum	{
       public static final int  T_PARENT_DALTONS = 0x01;
       public static final int  T_PARENT_PPM = 0x02;
       public static final int  T_FRAGMENT_DALTONS = 0x04;
       public static final int  T_FRAGMENT_PPM =	0x08;
//   } mscore_error; // enum for referencing information about ion mass measurement accuracy.

}
