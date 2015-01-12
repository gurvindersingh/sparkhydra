package org.systemsbiology.xtandem.scoring;

import org.junit.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.*;

/**
 * org.systemsbiology.xtandem.scoring.XTandemUtilitiesTests
 * User: Steve
 * Date: 1/9/2015
 */
public class XTandemUtilitiesTests {
    public static final XTandemUtilitiesTests[] EMPTY_ARRAY = {};

    /**
     * in directory E:\sparkhydra\data\tandem_output
     * argument  eg3.out.2014_10_18_16_37_26.t
     *
     * @param args
     */
    public static void main(String[] args) {
        XTandemScoringReport report = XTandemUtilities.readXTandemFile(args[0]);
         if(report == null)
            Assert.assertNotNull(report); // good place to look around and see why
    }
}
