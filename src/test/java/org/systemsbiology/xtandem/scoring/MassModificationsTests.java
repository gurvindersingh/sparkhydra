package org.systemsbiology.xtandem.scoring;

import org.junit.*;

/**
 * org.systemsbiology.xtandem.scoring.MassModificationsTests
 *
 * @author Steve Lewis
 * @date Jan 11, 2011
 */
public class MassModificationsTests
{
    public static MassModificationsTests[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MassModificationsTests.class;

    @Test
    public void simpleTest()
    {
        /// modify masses or C and D
        MassModifications.setModifications("57.022@C,45.67@D");
        double test = MassModifications.getModification('C');
        Assert.assertEquals(57.022, test, 0.00001);

        // test string version
        test = MassModifications.getModification("D");
        Assert.assertEquals(45.67, test, 0.00001);

        for (char i = 0; i < 128; i++) {
            if ('C' == i || 'D' == i)
                continue;
            test = MassModifications.getModification(i);

            Assert.assertEquals(0.0, test, 0.00001);


        }
    }

}
