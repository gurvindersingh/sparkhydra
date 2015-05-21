package org.systemsbiology.xtandem.peptide;

import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.XTandemUtilities;
import org.systemsbiology.xtandem.ionization.IonType;
import org.systemsbiology.xtandem.ionization.PeptideIon;

/**
 * org.systemsbiology.xtandem.peptide.PeptideIonTest
 *
 * @author Steve Lewis
 * @date 5/20/2015
 */
public class PeptideIonTest {
    public static PeptideIonTest[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = PeptideIonTest.class;

    @Test
    public void testIndex() throws Exception {
        IPolypeptide pp = Polypeptide.fromString("NIKPECP");
        double matchingMass = pp.getMass();
        matchingMass += XTandemUtilities.getProtonMass();
       Assert.assertEquals(839.39339591687997,matchingMass,0.001);
        PeptideIon ion = new PeptideIon(pp, IonType.B,1, matchingMass,0,0);
        Assert.assertEquals(839.39339591687997,ion.getMass(),0.001);
    }
}
