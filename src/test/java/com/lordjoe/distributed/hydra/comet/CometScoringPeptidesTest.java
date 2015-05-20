package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.MZPartitioner;
import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.hydra.SparkScanScorer;
import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.distributed.hydra.fragment.BinChargeMapper;
import com.lordjoe.distributed.hydra.scoring.PepXMLScoredScanWriter;
import com.lordjoe.distributed.hydra.scoring.SparkConsolidator;
import com.lordjoe.distributed.hydra.test.TestUtilities;
import com.lordjoe.distributed.spark.SparkAccumulators;
import com.lordjoe.utilities.ElapsedTimer;
import com.lordjoe.utilities.FileUtilities;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.IMeasuredSpectrum;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.ionization.ITheoreticalSpectrum;
import org.systemsbiology.xtandem.ionization.ITheoreticalSpectrumSet;
import org.systemsbiology.xtandem.ionization.IonUseCounter;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.pepxml.PepXMLWriter;
import org.systemsbiology.xtandem.scoring.IScoredScan;
import org.systemsbiology.xtandem.scoring.Scorer;
import org.systemsbiology.xtandem.testing.MZXMLReader;
import scala.Tuple2;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringSpeedTest
 *
 * @author Steve Lewis
 * @date 5/12/2015
 */
public class CometScoringPeptidesTest {

    public static void validatePolypeptideSet(Set<IPolypeptide> foundPeptides, List<UsedSpectrum> usedSpectrums) {
        Set<IPolypeptide> unused = new HashSet<IPolypeptide>(foundPeptides);
//        for (UsedSpectrum usedSpectrum : usedSpectrums) {
//            unused.add(usedSpectrum.peptide);
//        }
        Set<IPolypeptide> unfound = new HashSet<IPolypeptide>();
        for (UsedSpectrum usedSpectrum : usedSpectrums) {
            IPolypeptide peptide = usedSpectrum.peptide;
            IPolypeptide found = null;
            for (IPolypeptide test : foundPeptides) {
                if(peptide.equivalent(test)) {
                    found = test;
                    break;
                }
            }
            if(found == null)
                unfound.add(peptide);
            else
                unused.remove(found);
            //YITMTAQVMMKGYR
            //YITM[15.995]TAQVM[15.995]M[15.995]KGYR

            /*
            Theoretical ones
            VGTHTRQHTIFNSSR
            YITM[15.995]TAQVM[15.995]M[15.995]KGYR
            RFKLDHSVSSTNGHR
            NMFDQIAQHLPLWK
            LSEHCRLYFGALFK
             */
          }
        int numberUnfound = unfound.size();
        int numberUnused = unused.size();
        System.out.println("Not found: "+numberUnfound+" Not used: "+numberUnused);
    }

    public static void main(String[] args) {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters
        ElapsedTimer timer = new ElapsedTimer();

        if (args.length < SparkCometScanScorer.TANDEM_CONFIG_INDEX + 1) {
            System.out.println("usage sparkconfig configFile");
            return;
        }

        SparkCometScanScorer.buildDesiredScoring(args);

        SparkUtilities.readSparkProperties(args[SparkCometScanScorer.SPARK_CONFIG_INDEX]);

        CometScoringHandler handler = SparkCometScanScorer.buildCometScoringHandler(args[SparkCometScanScorer.TANDEM_CONFIG_INDEX]);

        XTandemMain scoringApplication = handler.getApplication();
        CometScoringAlgorithm comet = (CometScoringAlgorithm) scoringApplication.getAlgorithms()[0];
        Scorer scorer = scoringApplication.getScoreRunner();


        Properties sparkProperties = SparkUtilities.getSparkProperties();


        final Class<CometScoringPeptidesTest> cls = CometScoringPeptidesTest.class;
        InputStream istr = cls.getResourceAsStream("/UsedSpectraComet.txt");
        Map<Integer, List<UsedSpectrum>> spectraMap = UsedSpectrum.readUsedSpectra(istr);

        List<UsedSpectrum> usedSpectrums = spectraMap.get(8852);


        istr = cls.getResourceAsStream("/000000008852.mzXML");

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);
        CometScoredScan spec = new CometScoredScan(rp, comet);

        // debugging code set to  check data
        if (SparkUtilities.isLocal()) {
            String usedSpactra = SparkUtilities.buildPath("UsedSpectra.txt");
            CometTesting.readCometScoredSpectra(usedSpactra);
        }

        BinChargeKey[] keys = BinChargeMapper.keysFromSpectrum(spec);

        Set<Integer> usedBins = new HashSet<Integer>();
        for (int i = 0; i < keys.length; i++) {
            BinChargeKey key = keys[i];
            usedBins.add(key.getMzInt());
        }


        Set<IPolypeptide> foundPeptides = new HashSet<IPolypeptide>();


        JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptides = SparkCometScanScorer.getBinChargePeptideHash(sparkProperties, usedBins, handler);

        List<Tuple2<BinChargeKey, HashMap<String, IPolypeptide>>> collect = keyedPeptides.collect();
        for (Tuple2<BinChargeKey, HashMap<String, IPolypeptide>> tpl : collect) {
            Map<String, IPolypeptide> hm = tpl._2();
            for (String s : hm.keySet()) {
                foundPeptides.add(hm.get(s));
            }
        }

        System.out.println("Hydra peptides: "+foundPeptides.size()+" Comet peptides: "+usedSpectrums.size());

        Set<IPolypeptide> toScore = new HashSet<IPolypeptide>();
        for (IPolypeptide pp : foundPeptides) {
            CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
            ITheoreticalSpectrum[] spectra = ts.getSpectra();
            for (int i = 0; i < spectra.length; i++) {
                if (comet.isTheoreticalSpectrumScored(spectra[i], spec))
                    toScore.add(pp);
            }
        }

        validatePolypeptideSet(foundPeptides,usedSpectrums);

        validatePolypeptideSet(toScore,usedSpectrums);

        System.out.println("we finished the test");
        /*for (IPolypeptide foundPeptide : foundPeptides) {
            System.out.println(foundPeptide);
        }*/


    }


}

