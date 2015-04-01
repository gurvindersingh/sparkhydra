package org.systemsbiology.xtandem.comet;

import com.lordjoe.distributed.hydra.*;
import org.junit.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.comet.CometParameterTests
 * User: steven
 * Date: 4/2/13
 */
public class CometParameterTests {

    public static String[] getTestParameters() {
        return CometTestData.TEST_PARAMS_FILE.split("\n");
    }

    @Test
    public void testCometEg0() throws Exception {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters
        CometScoringAlgorithm comet = new CometScoringAlgorithm();
        InputStream is = new StringBufferInputStream(CometTestData.TANDEM_XML);
        SparkXTandemMain application = new SparkXTandemMain(is, "TANDEM_XML");
        comet.configure(application);
        IMeasuredSpectrum spectrum = generateTestSpectrum();
        testSpectrumPeaks(spectrum);

        IPolypeptide pp = Polypeptide.fromString("NECFLSHKDDSPDLPK");
        Scorer scorer = application.getScoreRunner();

        ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);

        IPolypeptide[] pps = {pp};
        ITheoreticalSpectrumSet[] tts = {ts};
        CometScoredScan scoring = new CometScoredScan(spectrum, comet);
        float[] binnedPeaks = scoring.getBinnedPeaks();
        comet.windowedNormalize(binnedPeaks);

        testWindowedIntensities(binnedPeaks);

        IonUseCounter counter = new IonUseCounter();

        int numberDotProducts = comet.scoreScan(scorer, counter, tts, scoring);

        if (scoring == null)
            Assert.assertNotNull(scoring); // good place to look around and see why
    }

    public void testSpectrumPeaks(IMeasuredSpectrum spectrum) {
        ISpectrumPeak[] peaks = spectrum.getPeaks();
 //        double testIntensity = pBinnedPeaks[CometScoredScan.TEST_BIN];
        Assert.assertEquals(153.07, peaks[0].getMassChargeRatio(), 0.01);
    }

    public void testWindowedIntensities(final float[] pBinnedPeaks) {
        double testIntensity = pBinnedPeaks[CometScoredScan.TEST_BIN];
        Assert.assertEquals(50, testIntensity, 0.01);
        double testIntensity1 = pBinnedPeaks[CometScoredScan.TEST_BIN + 1];
        Assert.assertEquals(33.283268, testIntensity1, 0.01);
        double testIntensity2 = pBinnedPeaks[CometScoredScan.TEST_BIN + 2];
        Assert.assertEquals(0, testIntensity2, 0.01);
    }

    public IMeasuredSpectrum generateTestSpectrum() throws Exception {
        InputStream inputStream = CometParameterTests.class.getResourceAsStream("/ego.mgf");
        LineNumberReader inp = new LineNumberReader(new InputStreamReader(inputStream));
        IMeasuredSpectrum spectrum = XTandemUtilities.readRawMGFScan(inp, "");
        inputStream.close();
        Assert.assertEquals(1602, spectrum.getPeaksCount());
        Assert.assertEquals(3, spectrum.getPrecursorCharge());
        Assert.assertEquals(634.6281, spectrum.getPrecursorMassChargeRatio(), 0.001);

        return spectrum;
    }


    /**
     *
     */
    @Test
    public void testCometParamsReport() {
        String[] lines = getTestParameters();
        ISetableParameterHolder hdr = new TestMain();
        CometParameterUtilities.parseParameters(hdr, lines);
        String[] parameterKeys = hdr.getParameterKeys();
        Assert.assertEquals(CometTestData.PARAM_KEY_SET.size(), parameterKeys.length);
        for (int i = 0; i < parameterKeys.length; i++) {
            String parameterKey = parameterKeys[i];
            if (!CometTestData.PARAM_KEY_SET.contains(parameterKey))
                Assert.fail();

        }
        // these should have value 0
        for (int i = 0; i < CometTestData.ZERO_TEST_PARAM_NAMES.length; i++) {
            String parameterKey = CometTestData.ZERO_TEST_PARAM_NAMES[i];
            String value = hdr.getParameter(parameterKey);
            Double doubleParameter = null;
            try {
                doubleParameter = hdr.getDoubleParameter(parameterKey);
            }
            catch (Exception e) {
                throw new RuntimeException(e);

            }
            Assert.assertEquals(parameterKey, 0.0, doubleParameter, 0.000001);

        }
        // now test specific parameters
        Assert.assertEquals("database_name", "Homo_sapiens_non-redundant.GRCh37.68.pep.all_FPKM_SNV-cRAP_targetdecoy.fasta", hdr.getParameter("database_name"));
        Assert.assertEquals("activation_method", "ALL", hdr.getParameter("activation_method"));
        Assert.assertEquals("variable_mod1", "15.994915 M 0 3", hdr.getParameter("variable_mod1"));

        Assert.assertEquals("peptide_mass_tolerance", 20, (int) hdr.getIntParameter("peptide_mass_tolerance"));
        Assert.assertEquals("num_threads", 8, (int) hdr.getIntParameter("num_threads"));
        Assert.assertEquals("peptide_mass_units", 2, (int) hdr.getIntParameter("peptide_mass_units"));

    }

}
