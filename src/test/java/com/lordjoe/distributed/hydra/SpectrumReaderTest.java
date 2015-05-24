package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.hydra.comet.CometScoringAlgorithm;
import com.lordjoe.distributed.hydra.comet.CometTestData;
import com.lordjoe.distributed.hydra.comet.CometTestingUtilities;
import com.lordjoe.distributed.spectrum.SparkSpectrumUtilities;
import junit.framework.Assert;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Test;
import org.systemsbiology.xtandem.IMeasuredSpectrum;
import org.systemsbiology.xtandem.ISpectrumPeak;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.scoring.Scorer;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

/**
 * com.lordjoe.distributed.hydra.SpectrumReaderTest
 *  Test whether spectra using mgf and mzXML are the same
 * @author Steve Lewis
 * @date 5/16/2015
 */
public class SpectrumReaderTest {

 //   @Test
    public void testSpectrumRead() throws Exception {


    }

    public static void main(String[] args) {
        XTandemMain application = CometTestingUtilities.getDefaultApplication();

        String baseDir = "data/test_data/";
        String spectra;

        spectra = baseDir +  "000000006774.mgf";
        JavaPairRDD<String, IMeasuredSpectrum> scans = SparkSpectrumUtilities.parseSpectrumFile(spectra, application);

        scans = SparkUtilities.persist(scans);
        List<IMeasuredSpectrum> mgfSpectra = scans.values().collect();

       Assert.assertEquals(1, mgfSpectra.size());

 
        spectra = baseDir +  "000000006774.mzXML";
        JavaPairRDD<String, IMeasuredSpectrum>  scansmz = SparkSpectrumUtilities.parseSpectrumFile(spectra, application);
        scansmz = SparkUtilities.persist(scansmz);

        List<IMeasuredSpectrum> mzSpectra = scansmz.values().collect();

        Assert.assertEquals(1, mzSpectra.size());

        IMeasuredSpectrum mgfSpectrum =  mgfSpectra.get(0);
        IMeasuredSpectrum mzSpectrum  =  mzSpectra.get(0);

        // we read the same spectra
        Assert.assertTrue(mgfSpectrum.equivalent(mzSpectrum));

        ISpectrumPeak[] mgfSpectrumPeaks = mgfSpectrum.getPeaks();
        ISpectrumPeak[] mzSpectrumPeaks = mzSpectrum.getPeaks();

        for (int i = 0; i < mzSpectrumPeaks.length; i++) {
            ISpectrumPeak mzSpectrumPeak = mzSpectrumPeaks[i];
            ISpectrumPeak mgSpectrumPeak = mgfSpectrumPeaks[i];
            Assert.assertTrue(mzSpectrumPeak.equivalent(mgSpectrumPeak));

        }


    }
}
