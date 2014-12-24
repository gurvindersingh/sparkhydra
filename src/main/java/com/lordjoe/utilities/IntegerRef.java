/**{ file
 @name IntegerRef.java
 @function IntegerRef allows a function to return a int
 @author> Steven Lewis
 @copyright>
 ************************
 *  Copyright (c) 1996,97,98
 *  Steven M. Lewis
 *  www.LordJoe.com
 ************************

 @date> Mon Jun 22 21:48:24 PDT 1998
 @version> 1.0
 }*/
package com.lordjoe.utilities;

/**{ class
 @name IntegerRef
 @function this class allows a integer to be passed as a reference
 @see Overlord.BoleanRef
 @see Overlord.DoubleRef
 @see Overlord.StringRef
 }*/
public class IntegerRef  {

    //- *******************
    //- Fields
    /**{ field
     @name value
     @function - real data
     }*/
    public int value;


    //- *******************
    //- Methods
    /**{ constructor
     @name IntegerRef
     @function Constructor of IntegerRef
     @param d initial value
     }*/
    public IntegerRef(int d) {
        value = d;
    }

    /**{ constructor
     @name IntegerRef
     @function Constructor of IntegerRef
     }*/
    public IntegerRef() {
    }


//- *******************
//- End Class IntegerRef
}
