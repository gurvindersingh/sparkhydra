package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.*;
import org.junit.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometParameterTests
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

        CometParameterTests.testCometTheoreticalSet((CometTheoreticalBinnedSet) ts, "/egoParameters.txt",comet,scorer);


        IPolypeptide[] pps = {pp};
        ITheoreticalSpectrumSet[] tts = {ts};
        CometScoredScan scoring = new CometScoredScan(spectrum);
        scoring.setAlgorithm(comet);

        // This works for EG0
        testValues(scoring, "/eg0");

        // this test passes
        //CometTesting.testUnNormalizedWeightsState(comet);

        //  CometTesting.testWeightsState(comet);
        //comet.windowedNormalize();

//        // this test passes
//        CometTesting.testWindowedIntensities(comet,scoring);
//
//        // ============================================
//        // checking a scoring step
//        double mass = spectrum.getPrecursorMass();    // todo is this peptide or
//        int MaxArraySize = comet.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize
//
//        // these pass
//        boolean doScoringOutside = true;
//        if (doScoringOutside) {
//            double sum = comet.normalizeBinnedPeaks(MaxArraySize);
//            CometTesting.testNormalizedBinnedIntensities(comet);
//
//            comet.normalizeForNL(MaxArraySize);
//            CometTesting.testNLValues(comet);
//
//        }
        IonUseCounter counter = new IonUseCounter();
        List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

        CometTesting.testFastXcorrDataAtXCorr(comet, scoring);
            double xcorr = comet.doXCorr((CometTheoreticalBinnedSet) ts,scorer, counter, scoring, used);

        Assert.assertEquals(2.870912, xcorr, 0.001);

        CometTestData.testUsedXCorrData(used);

        if (true)
            return;
        int numberDotProducts = comet.scoreScan(scorer, counter, tts, scoring);

        if (scoring == null)
            Assert.assertNotNull(scoring); // good place to look around and see why
    }

    public static void testValues(final CometScoredScan pScoring, String header) {
        List<SpectrumBinnedScore> tmp1 = pScoring.getTmpFastXcorrData();
        List<SpectrumBinnedScore> cometPeaks = SpectrumBinnedScore.fromResource(header + "/pdTmpCorrelationDataAfterMakeCorrData.data");
        Collections.sort(cometPeaks);
        CometTesting.comparePeakSets(cometPeaks, tmp1);
        List<SpectrumBinnedScore> tmp2 = pScoring.getFastScoringDataArray();
        cometPeaks = SpectrumBinnedScore.fromResource(header + "/pdTmpFastXcorrData.data");
        Collections.sort(cometPeaks);
        CometTesting.comparePeakSets(cometPeaks, tmp2);
          // look for debugging
        List<SpectrumBinnedScore> fast = pScoring.getFastScoringData();
        List<SpectrumBinnedScore> nl = pScoring.getNLScoringData();
        List<SpectrumBinnedScore> weights = pScoring.getWeights();
        List<SpectrumBinnedScore> tmp = pScoring.getTmpFastXcorrData();
    }


    @Test
    public void testCometEg3() throws Exception {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters


        InputStream is = new StringBufferInputStream(CometTestData.TANDEM_XML);
        SparkXTandemMain application = new SparkXTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);

        InputStream istr = getClass().getResourceAsStream("/eg3/eg3Sample.mzXML");
        RawPeptideScan[] rawPeptideScans = MZXMLReader.processStream(istr);
        Map<String, CometScoredScan> scanById = new HashMap<String, CometScoredScan>();
        for (int i = 0; i < rawPeptideScans.length; i++) {
            RawPeptideScan rawPeptideScan = rawPeptideScans[i];
            CometScoredScan scan = new CometScoredScan(rawPeptideScan);
            scanById.put(scan.getId(), scan);
        }
        Scorer scorer = application.getScoreRunner();

        // CometScoredScan scan = scanById.get("000000008852");
        CometScoredScan scan = scanById.get("000000009075");
        IPolypeptide pp = Polypeptide.fromString("DAFLGSFLYEYSR");
        ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);

        double ppMass = pp.getMatchingMass();
        double pmass = pp.getMass();


        double precursorMass = scan.getPrecursorMass();
        int charge = scan.getCharge();
        Assert.assertTrue(comet.isWithinLimits(precursorMass, ppMass, charge));


        Assert.assertEquals(CometScoringAlgorithm.class, scorer.getAlgorithm().getClass());
        assertScanScore(application, scanById, "DAFLGSFLYEYSR", "9075", 1.2996);
        assertScanScore(application, scanById, "AIQAAFFYLEPR", "8514", 1.209);
        assertScanScore(application, scanById, "MPCTEDYLSLILNR", "9100", 1.023);
        assertScanScore(application, scanById, "M[15.995]PCTEDYLSLILNR", "8852", 1.858);


        double xcorr = CometScoringHandler.doRealScoring(scan, pp, application);

        Assert.assertEquals(1.2996, xcorr, 0001);
    }


    protected void assertScanScore(SparkXTandemMain application, Map<String, CometScoredScan> scanById, String ppStr, String scanId, double score) {
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        Scorer scorer = application.getScoreRunner();
        CometScoredScan scan = scanById.get("00000000" + scanId);
        IPolypeptide pp = Polypeptide.fromString(ppStr);
        ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);

        double ppMass = pp.getMatchingMass();
        double pmass = pp.getMass();


        double precursorMass = scan.getPrecursorMass();
        int charge = scan.getCharge();
        Assert.assertTrue(comet.isWithinLimits(precursorMass, ppMass, charge));

         double xcorr = CometScoringHandler.doRealScoring(scan,scorer, ts, application);

        Assert.assertEquals(score, xcorr, 0001);

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
     * mape sure masses of peptides agree with those Comet uses
     */
    @Test
    public void testPeptideMasses()
    {
        validatePeptideMass("DAFLGSFLYEYSR",1567.743);
        validatePeptideMass("MPCTEDYLSLILNR",1724.835);
        validatePeptideMass("M[15.995]PCTEDYLSLILNR",1740.830);
        validatePeptideMass("QQPPQAQFPQTR",1425.723);
        validatePeptideMass("M[15.995]TDDPM[15.995]NNKDVKK",1567.709);
        validatePeptideMass("VTM[15.995]PKNPNQPKR",1425.763);
        validatePeptideMass("TYEWSSEEEEPVKK",1740.796);
        validatePeptideMass("KVPEEEESSWEYTK",1740.796);
        validatePeptideMass("ALRSSRLQEEGHSDR",1740.874);
        validatePeptideMass("DKNFVQKLFPNSM",1567.794);
        validatePeptideMass("MERDPETGRFMAK",1567.736);
        validatePeptideMass("AMFRGTEPDREMK",1567.736);
      }



    public void validatePeptideMass(String peptide,double value)   {
        IPolypeptide pp = Polypeptide.fromString(peptide);
        double pepMass = CometScoringAlgorithm.getCometMetchingMass(pp);
        if(Math.abs(pepMass - value ) > 0.01)
            Assert.assertEquals(value,pepMass,0.001);
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
        String paramsFromComet = readResourceAsText(resourceName);
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

    public static void testCometTheoreticalSet(CometTheoreticalBinnedSet data, String resourceName,CometScoringAlgorithm comet,Scorer scorer) {
        List<BinnedChargeIonIndex> binnedIndexX = data.getBinnedIndex(comet,  scorer);

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
