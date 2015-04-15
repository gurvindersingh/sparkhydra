package org.systemsbiology.xtandem.comet;

import com.lordjoe.distributed.hydra.*;
import org.junit.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;
import java.util.*;

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


        InputStream is = new StringBufferInputStream(CometTestData.TANDEM_XML);
        SparkXTandemMain application = new SparkXTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);

        IMeasuredSpectrum spectrum = generateTestSpectrum();
        testSpectrumPeaks(spectrum);

        int charge = spectrum.getPrecursorCharge();
        double precursorMass = spectrum.getPrecursorMass();

        IPolypeptide pp = Polypeptide.fromString("NECFLSHKDDSPDLPK");

        double ppMass = pp.getMatchingMass();
        double pmass = pp.getMass();

        Assert.assertTrue(comet.isWithinLimits(precursorMass, ppMass, charge));

        Scorer scorer = application.getScoreRunner();

        Assert.assertEquals(CometScoringAlgorithm.class, scorer.getAlgorithm().getClass());

        ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);

        CometParameterTests.testCometTheoreticalSet((CometTheoreticalBinnedSet) ts,"/egoParameters.txt");


        IPolypeptide[] pps = {pp};
        ITheoreticalSpectrumSet[] tts = {ts};
        CometScoredScan scoring = new CometScoredScan(spectrum);
        scoring.setAlgorithm(comet);
        // this test passes
        //CometTesting.testUnNormalizedWeightsState(comet);

        //  CometTesting.testWeightsState(comet);
        //comet.windowedNormalize();

        // this test passes
        CometTesting.testWindowedIntensities(comet);

        // ============================================
        // checking a scoring step
        double mass = spectrum.getPrecursorMass();    // todo is this peptide or
        int MaxArraySize = comet.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize

        // these pass
        boolean doScoringOutside = true;
        if (doScoringOutside) {
            double sum = comet.normalizeBinnedPeaks(MaxArraySize);
            CometTesting.testNormalizedBinnedIntensities(comet);

            comet.normalizeForNL(MaxArraySize);
            CometTesting.testNLValues(comet);

        }
        IonUseCounter counter = new IonUseCounter();
        List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

        CometTesting.testFastXcorrDataAtXCorr(comet);

        double xcorr = comet.doXCorr((CometTheoreticalBinnedSet) ts, counter, scoring, used);

        Assert.assertEquals(3.1184, xcorr, 0.001);

        CometTestData.testUsedXCorrData(used);

        if (true)
            return;
        int numberDotProducts = comet.scoreScan(scorer, counter, tts, scoring);

        if (scoring == null)
            Assert.assertNotNull(scoring); // good place to look around and see why
    }


    @Test
    public void testCometEg3() throws Exception {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters


        InputStream is = new StringBufferInputStream(CometTestData.TANDEM_XML);
        SparkXTandemMain application = new SparkXTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);

        IMeasuredSpectrum spectrum = generateTestSpectrumEg3();

        int charge = spectrum.getPrecursorCharge();
        double precursorMass = spectrum.getPrecursorMass();

        IPolypeptide pp = Polypeptide.fromString("LKPDPNTLCDEFK");

        double ppMass = pp.getMatchingMass();
        double pmass = pp.getMass();

        Assert.assertTrue(comet.isWithinLimits(precursorMass, ppMass, charge));

        Scorer scorer = application.getScoreRunner();

        Assert.assertEquals(CometScoringAlgorithm.class, scorer.getAlgorithm().getClass());

        ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);

        CometParameterTests.testCometTheoreticalSet((CometTheoreticalBinnedSet) ts,"/eg3Parameters.txt");


        IPolypeptide[] pps = {pp};
        ITheoreticalSpectrumSet[] tts = {ts};
        CometScoredScan scoring = new CometScoredScan(spectrum);
        scoring.setAlgorithm(comet);
        // this test passes
        //CometTesting.testUnNormalizedWeightsState(comet);

        //  CometTesting.testWeightsState(comet);
        //comet.windowedNormalize();

        // this test passes
        CometTesting.testWindowedIntensities(comet);

        // ============================================
        // checking a scoring step
        double mass = spectrum.getPrecursorMass();    // todo is this peptide or
        int MaxArraySize = comet.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize

        // these pass
        boolean doScoringOutside = true;
        if (doScoringOutside) {
            double sum = comet.normalizeBinnedPeaks(MaxArraySize);
            CometTesting.testNormalizedBinnedIntensities(comet);

            comet.normalizeForNL(MaxArraySize);
            CometTesting.testNLValues(comet);

        }
        IonUseCounter counter = new IonUseCounter();
        List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

        CometTesting.testFastXcorrDataAtXCorr(comet);

        double xcorr = comet.doXCorr((CometTheoreticalBinnedSet) ts, counter, scoring, used);

        Assert.assertEquals(3.1184, xcorr, 0.001);

        CometTestData.testUsedXCorrData(used);

        if (true)
            return;
        int numberDotProducts = comet.scoreScan(scorer, counter, tts, scoring);

        if (scoring == null)
            Assert.assertNotNull(scoring); // good place to look around and see why
    }


    public void testSpectrumPeaks(IMeasuredSpectrum spectrum) {
        ISpectrumPeak[] peaks = spectrum.getPeaks();
        //        double testIntensity = pBinnedPeaks[CometScoredScan.TEST_BIN];
        Assert.assertEquals(153.07, peaks[0].getMassChargeRatio(), 0.01);
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            if (Math.abs(peak.getMassChargeRatio() - 167.075229) < 0.001)
                return;
        }
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

    public static String readResourceAsText(String resource) {
        InputStream inputStream = CometParameterTests.class.getResourceAsStream(resource);
        return readInputStreamAsText(inputStream);
    }

    public static String readInputStreamAsText(final InputStream pInputStream) {
        try {
            StringBuilder sb = new StringBuilder();
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pInputStream));
            String line = rdr.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = rdr.readLine();
            }
            pInputStream.close();
            return sb.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public IMeasuredSpectrum generateTestSpectrumEg3() throws Exception {
        String scanText = readResourceAsText("/eg3Scan7.xml");

        RawPeptideScan spectrum = XTandemHadoopUtilities.readScan(scanText, null);
        Assert.assertEquals(50, spectrum.getPeaksCount());
        Assert.assertEquals(2, spectrum.getPrecursorCharge());
        Assert.assertEquals(788.8887, spectrum.getPrecursorMassChargeRatio(), 0.001);

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

    public static Set<BinnedChargeIonIndex> readTestBins(String resourceName) {
        HashSet<BinnedChargeIonIndex> ret = new HashSet<BinnedChargeIonIndex>();
        String paramsFromComet = readResourceAsText(resourceName) ;
        String[] lines = paramsFromComet.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            String[] items = line.split("\t");
            int index = 0;
            final int pIndex = Integer.parseInt(items[index++]);
            final int pCharge = Integer.parseInt(items[index++]);
            final IonType pType = IonType.valueOf(items[index++]);
            final int pPeptidePosition = Integer.parseInt(items[index++]);

            BinnedChargeIonIndex added = new BinnedChargeIonIndex(pIndex, pCharge, pType, pPeptidePosition);
            if (ret.contains(added))
                throw new IllegalStateException("problem"); // ToDo change
            ret.add(added);
        }
        return ret;
    }

    public static void testCometTheoreticalSet(CometTheoreticalBinnedSet data,String resourceName) {
        List<BinnedChargeIonIndex> binnedIndexX = data.getBinnedIndex();

        // binnedIndexX is immutable ^&)*&%*&^$%^$
        List<BinnedChargeIonIndex> binnedIndexY = new ArrayList<BinnedChargeIonIndex>(binnedIndexX);
        Collections.sort(binnedIndexY, BinnedChargeIonIndex.BY_BIN);   // order


        Set<BinnedChargeIonIndex> binnedIndex = new HashSet<BinnedChargeIonIndex>(binnedIndexX);
        Set<BinnedChargeIonIndex> expected = readTestBins(resourceName);

        if (false) {   // this part works
            List<BinnedChargeIonIndex> binnedExpected = new ArrayList<BinnedChargeIonIndex>(expected);
            Collections.sort(binnedExpected, BinnedChargeIonIndex.BY_BIN);   // order
            for (BinnedChargeIonIndex binnedChargeIonIndex : binnedIndexY) {
                System.out.println(binnedChargeIonIndex);
            }
            for (BinnedChargeIonIndex binnedChargeIonIndex : binnedExpected) {
                System.out.println(binnedChargeIonIndex);
            }
        }


        //   Assert.assertEquals(expected.size(), binnedIndex.size());
        expected.removeAll(binnedIndex);
        List<BinnedChargeIonIndex> badValues = new ArrayList<BinnedChargeIonIndex>(expected);
        Collections.sort(badValues);

        Assert.assertTrue(expected.isEmpty());

    }

    public static void main(String[] args) throws Exception {
        CometParameterTests tests = new CometParameterTests();
        tests.testCometEg0();
    }

}
