package com.lordjoe.distributed.hydra.comet;

import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.peptide.IPolypeptide;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

/**
 * com.lordjoe.distributed.hydra.comet.CometBinningTest
 *
 * @author Steve Lewis
 * @date 5/22/2015
 */
public class CometBinningTest {

    public static final double REQUIRED_PRECISION = 0.001;

    @Test
    public void testMasses() throws Exception {
        List<UsedSpectrum> spectrumUsed = CometTestingUtilities.getSpectrumUsed(8852);
        Assert.assertEquals(311, spectrumUsed.size());
        for (UsedSpectrum usedSpectrum : spectrumUsed) {
            validatePeptideMass(usedSpectrum);
        }

        UsedSpectrum one = spectrumUsed.get(0);
        RawPeptideScan spec = CometTestingUtilities.getScanFromMZXMLResource("/000000008852.mzXML");
        Assert.assertEquals(one.spectrumMass, spec.getPrecursorMass(), REQUIRED_PRECISION);
    }

    @Test
    public void testBins() throws Exception {
        CometTesting.validateOneKey(); // We are hunting for when this stops working

        List<UsedSpectrum> spectrumUsed = CometTestingUtilities.getSpectrumUsed(8852);
        RawPeptideScan spec = CometTestingUtilities.getScanFromMZXMLResource("/000000008852.mzXML");
        CometTestingUtilities.doBinTest(spectrumUsed, spec);
    }

    @Test
    public void testWithInit() throws Exception {
        List<UsedSpectrum> spectrumUsed = CometTestingUtilities.getSpectrumUsed(8852);
        RawPeptideScan spec = CometTestingUtilities.getScanFromMZXMLResource("/000000008852.mzXML");
        CometTestingUtilities.doBinTest(spectrumUsed, spec);
         InputStream is = new StringBufferInputStream(CometTestData.COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometTestingUtilities.doBinTest(spectrumUsed, spec);

        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];

        CometTestingUtilities.doBinTest(spectrumUsed, spec);

        comet.configure(application);

        CometScoredScan scan = new CometScoredScan(spec, comet);

        CometTestingUtilities.doBinTest(spectrumUsed, spec);
        CometTestingUtilities.doBinTest(spectrumUsed, scan);


        RawPeptideScan spec2 = CometTestingUtilities.getScanFromMZXMLResource("/000000008852.mzXML");
        CometTestingUtilities.doBinTest(spectrumUsed, spec2);
        CometScoredScan scan2 = new CometScoredScan(spec, comet);

        CometTestingUtilities.doBinTest(spectrumUsed, scan2);

    }

    private void validatePeptideMass(UsedSpectrum spc) {
        double expected = spc.peptideMass;
        double seen = getMass(spc.peptide);
        Assert.assertEquals(expected, seen, REQUIRED_PRECISION);

    }

    private double getMass(IPolypeptide pp) {
        return CometScoringAlgorithm.getCometMatchingMass(pp);

    }
}
