package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.hydra.comet_spark.CometScoringHandler;
import com.lordjoe.distributed.hydra.comet_spark.SparkCometScanScorer;
import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.utilities.ElapsedTimer;
import org.apache.spark.api.java.JavaPairRDD;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.ionization.ITheoreticalSpectrum;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.Polypeptide;
import org.systemsbiology.xtandem.scoring.Scorer;
import scala.Tuple2;

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


          List<UsedSpectrum> usedSpectrums = CometTestingUtilities.getSpectrumUsed(8852);

        RawPeptideScan rp = CometTestingUtilities.getScanFromMZXMLResource("/000000008852.mzXML");
        CometScoredScan spec = new CometScoredScan(rp, comet);

        // debugging code set to  check data
        if (SparkUtilities.isLocal()) {
            String usedSpactra = SparkUtilities.buildPath("UsedSpectra.txt");
            CometTesting.readCometScoredSpectra(usedSpactra);
        }

        Set<BinChargeKey> keys = BinChargeMapper.keysFromSpectrum(spec);

        Set<Integer> usedBins = new HashSet<Integer>();
        for (BinChargeKey key : keys) {
            usedBins.add(key.getMzInt());
        }

        IPolypeptide testPeptide = Polypeptide.fromString("YITMTAQVMMKGYR");


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

