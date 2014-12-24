package org.systemsbiology.xtandem;

import org.junit.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.MassCalculatorTest
 *
 * @author Steve Lewis
 * @date Jan 7, 2011
 */
public class MassCalculatorTest {
    public static MassCalculatorTest[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MassCalculatorTest.class;
    public static final double DELTA = 0.0000001;

    @Test
    public void simpleCalculatorTest() {
        MassCalculator mc = MassCalculator.getCalculator(MassType.average);

        double total = mc.calcMass("H");
        Assert.assertEquals(total, mc.getMass("H"), DELTA);

        total = mc.calcMass("C");
        Assert.assertEquals(total, mc.getMass("C"), DELTA);
        Assert.assertEquals(total, 12.01, 0.001);

        total = mc.calcMass("N");
        Assert.assertEquals(total, mc.getMass("N"), DELTA);

        total = mc.calcMass("S");
        Assert.assertEquals(total, mc.getMass("S"), DELTA);

        total = mc.calcMass("O");
        Assert.assertEquals(total, mc.getMass("O"), DELTA);

        total = mc.calcMass("P");
        Assert.assertEquals(total, mc.getMass("P"), DELTA);

    }

   // @Test
    public void peptideTest() {
        MassCalculator mc = MassCalculator.getCalculator(MassType.average);
        double total = 0;
        double del = 0.01;
        IPolypeptide pp = null;

        pp =   Polypeptide.fromString("EALNTTETSRECLKLAI");
        total = pp.getMatchingMass();

        Assert.assertEquals(1949.0009, total, del);

        pp = Polypeptide.fromString("CIITWAVDINLR");
        total = pp.getMatchingMass();
        Assert.assertEquals(1473.787692385, total, del);

        pp =  Polypeptide.fromString("EKAAKMPIK");
        total = pp.getMatchingMass();
        Assert.assertEquals(1015.59630943, total, del);

        //       Assert.assertEquals(1930.9896994, total, DELTA);

        pp =  Polypeptide.fromString("CIELSTMNLVRR");
    //    ModifiedPolypeptide.buildAllModifications(pp,)
          total = pp.getMatchingMass();
          Assert.assertEquals(1491.776, total, del);


        pp =  Polypeptide.fromString("EATLCPVNGTSNTK");
          total = pp.getMatchingMass();
          Assert.assertEquals(1491.710, total, del);


        pp = Polypeptide.fromString("ECLKLAIKMCKP");
          total = pp.getMatchingMass();
          Assert.assertEquals(1490.788, total, del);



        pp = Polypeptide.fromString("QIIHGYIGLLNSY");
          total = pp.getMatchingMass();
          Assert.assertEquals(1490.799, total, del);

        IPolypeptide pp2 = null;
        pp = Polypeptide.fromString("EKAAKMPIK");
          pp2 = buildModifiedPeptide(pp);
          total = pp.getMatchingMass();
          Assert.assertEquals(1015.6, total, del);


        pp = Polypeptide.fromString("EKLKFSSSS");
          pp2 = buildModifiedPeptide(pp);
           total = pp.getMatchingMass();
          Assert.assertEquals(1012.53, total, del);

        pp = Polypeptide.fromString("QAVRLFKR");
          pp2 = buildModifiedPeptide(pp);
           total = pp.getMatchingMass();
          Assert.assertEquals(1017.63, total, del);


        pp = Polypeptide.fromString("QERGSVVSR");
          pp2 = buildModifiedPeptide(pp);
          total = pp.getMatchingMass();
          Assert.assertEquals(1017.54, total, del);


    }

    public static IPolypeptide buildModifiedPeptide(IPolypeptide pp)
    {
        PeptideModification[] pms = ModifiedPolypeptide.buildTerminalModifications(pp);
        if(pms.length == 0)
            return pp;
        IModifiedPeptide[] mps = ModifiedPolypeptide.buildAllModifications(pp, pms);
        return mps[0];

    }

    @Test
    public void aminoAcidTest() {
        MassCalculator mc = MassCalculator.getCalculator(MassType.average);
        double total = 0;
        double del = DELTA;

        total = mc.getAminoAcidMass('A');
        Assert.assertEquals(71.0788, total, DELTA);

        total = mc.getAminoAcidMass('B');
        Assert.assertEquals(114.1038, total, DELTA);

        total = mc.getAminoAcidMass('C');
     //  total += PeptideModification.CYSTEIN_MODIFICATION_MASS;
        // this change is hard coded


        Assert.assertEquals(160.1602, total, 0.1);      //  PeptideModification.CYSTEIN_MODIFICATION_MASS preadded
     //   Assert.assertEquals(103.1388, total, 0.1);

        total = mc.getAminoAcidMass('D');
        Assert.assertEquals(115.0886, total, DELTA);

        total = mc.getAminoAcidMass('E');
        Assert.assertEquals(129.1155, total, DELTA);

        total = mc.getAminoAcidMass('F');
        Assert.assertEquals(147.1766, total, DELTA);

        total = mc.getAminoAcidMass('G');
        Assert.assertEquals(57.0519, total, DELTA);

        total = mc.getAminoAcidMass('H');
        Assert.assertEquals(137.1411, total, DELTA);

        total = mc.getAminoAcidMass('I');
        Assert.assertEquals(113.1594, total, DELTA);

        total = mc.getAminoAcidMass('K');
        Assert.assertEquals(128.1741, total, DELTA);

        total = mc.getAminoAcidMass('L');
        Assert.assertEquals(113.1594, total, DELTA);


        total = mc.getAminoAcidMass('M');
        Assert.assertEquals(131.1926, total, DELTA);

        total = mc.getAminoAcidMass('N');
        Assert.assertEquals(114.1038, total, DELTA);

        total = mc.getAminoAcidMass('O');
        Assert.assertEquals(114.1038, total, DELTA);

        total = mc.getAminoAcidMass('P');
        Assert.assertEquals(97.1167, total, DELTA);

        total = mc.getAminoAcidMass('Q');
        Assert.assertEquals(128.1307, total, DELTA);

        total = mc.getAminoAcidMass('R');
        Assert.assertEquals(156.1875, total, DELTA);

        total = mc.getAminoAcidMass('P');
        Assert.assertEquals(97.1167, total, DELTA);

        total = mc.getAminoAcidMass('S');
        Assert.assertEquals(87.0782, total, DELTA);

        total = mc.getAminoAcidMass('T');
        Assert.assertEquals(101.1051, total, DELTA);

        total = mc.getAminoAcidMass('V');
        Assert.assertEquals(99.1326, total, DELTA);

        total = mc.getAminoAcidMass('W');
        Assert.assertEquals(186.2132, total, DELTA);

        total = mc.getAminoAcidMass('X');
        Assert.assertEquals(113.1594, total, DELTA);

        total = mc.getAminoAcidMass('Y');
        Assert.assertEquals(163.1760, total, DELTA);

        total = mc.getAminoAcidMass('Z');
        Assert.assertEquals(128.1307, total, DELTA);


    }

    @Test
    public void complexCalculatorTest() {
        MassCalculator mc = MassCalculator.getCalculator(MassType.average);

        double total = mc.calcMass("H2C4");
        Assert.assertEquals(total, 2 * mc.getMass("H") + 4 * mc.getMass("C"), DELTA);

        total = mc.calcMass("C6H12ON2");
        Assert.assertEquals(total,
                12 * mc.getMass("H") + 6 * mc.getMass("C") + 1 * mc.getMass("O") + 2 * mc.getMass(
                        "N"), DELTA);

        total = mc.calcMass("C5H9ONS");
        Assert.assertEquals(total,
                9 * mc.getMass("H") + 5 * mc.getMass("C") + 1 * mc.getMass("O") + 1 * mc.getMass(
                        "N") +
                        +1 * mc.getMass("S"), DELTA);

    }


    @Test
    public void complexCalculatorTestMonoIsotopic() {
        MassCalculator mc = MassCalculator.getCalculator(MassType.monoisotopic);

        double total = mc.calcMass("H2C4");
        Assert.assertEquals(total, 2 * mc.getMass("H") + 4 * mc.getMass("C"), DELTA);

        total = mc.calcMass("C6H12ON2");
        Assert.assertEquals(total,
                12 * mc.getMass("H") + 6 * mc.getMass("C") + 1 * mc.getMass("O") + 2 * mc.getMass(
                        "N"), DELTA);

        total = mc.calcMass("C5H9ONS");
        Assert.assertEquals(total,
                9 * mc.getMass("H") + 5 * mc.getMass("C") + 1 * mc.getMass("O") + 1 * mc.getMass(
                        "N") +
                        +1 * mc.getMass("S"), DELTA);

    }

    /**
     * test the  XTandemUtilities.getAminoAcidMass function which uses a lookup table
     */
    @Test
    public void precalculatedMassTest() {
        MassCalculator mc = MassCalculator.getCalculator(MassType.monoisotopic);
        Assert.assertEquals(XTandemUtilities.getAminoAcidMass(FastaAminoAcid.A, MassType.average),
                71.0788, DELTA);

        final double aminoAcidMass = XTandemUtilities.getAminoAcidMass(FastaAminoAcid.A,
                MassType.monoisotopic);
        final double v = mc.calcMass("C3H5ON");
        Assert.assertEquals(aminoAcidMass, v, 0.1);

        // make sure the monoisotopic mass is close to the averals
        final FastaAminoAcid[] aminoiAcidses = FastaAminoAcid.values();
        for (int i = 0; i < aminoiAcidses.length; i++) {
            FastaAminoAcid a = aminoiAcidses[i];
            if(a == FastaAminoAcid.UNKNOWN  || a == FastaAminoAcid.X )
                continue;
            try {
                double m1 = XTandemUtilities.getAminoAcidMass(a, MassType.average);
                double m2 = XTandemUtilities.getAminoAcidMass(a, MassType.monoisotopic);
                double del = Math.abs(m1 - m2);
                Assert.assertEquals(m1, m2, 0.2);
            }
            catch (Exception e) {
                throw new RuntimeException(e);

            }

        }

    }

    /**
     * test peptide sequence mass calculation
     */

    @Test
    public void calculatePeptideMassTest() {
        MassCalculator mc = MassCalculator.getCalculator(MassType.monoisotopic);

         double total = 129.1155 + 128.1307;
         Assert.assertEquals(total, mc.getAminoAcidMass('E') + mc.getAminoAcidMass('Q'), 0.2);
        double eq = mc.getSequenceMass("EQ");
        Assert.assertEquals(total, eq, 0.2);

    }


}
