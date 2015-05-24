package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.hydra.comet_spark.CometScoringHandler;
import com.lordjoe.distributed.hydra.comet_spark.SparkCometScanScorer;
import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.distributed.hydra.test.TestUtilities;
import com.lordjoe.distributed.protein.ProteinParser;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.IScoredScan;
import org.systemsbiology.xtandem.scoring.ISpectralMatch;
import org.systemsbiology.xtandem.scoring.Scorer;
import scala.Tuple2;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringDoneTest
 * test whether a set of peptides will score against a specific spectrum
 * using the list from comet as a gold standard
 *
 * @author Steve Lewis
 * @date 5/12/2015
 */
public class CometScoringDoneTest {

    /**
     * find the spectgrum matching the polypeptide
     *
     * @param pp
     * @param specrta
     * @return
     */
    public static UsedSpectrum getUsedSpectrum(IPolypeptide pp, List<UsedSpectrum> specrta) {
        for (UsedSpectrum usedSpectrum : specrta) {
            if (UsedSpectrum.equivalentPeptide(usedSpectrum.peptide, pp))
                return usedSpectrum;
        }
        return null;
    }

    public static List<IPolypeptide> notProcessedInComet(List<IPolypeptide> items, List<UsedSpectrum> cometSees) {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (UsedSpectrum cometSee : cometSees) {
            boolean seen = false;
            for (IPolypeptide pp : items) {
                if (UsedSpectrum.equivalentPeptide(cometSee.peptide, pp)) {
                    seen = true;
                    break;
                }
            }
            if (!seen)
                holder.add(cometSee.peptide); // this is a problem
        }

        return holder;
    }

    public static boolean willHydraScorePeptide(final Scorer scorer, CometTheoreticalBinnedSet ts, CometScoredScan spec, Set<BinChargeKey> usedBins) {
        BinChargeKey key = BinChargeMapper.keyFromPeptide(ts.getPeptide());
        if (!usedBins.contains(key))
            return false;

        if (!scorer.isTheoreticalSpectrumScored(spec, ts))
            return false;

        return true;
    }

    public static List<IPolypeptide> getFromBinnedHash(List<Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>>> collect1) {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>> tx : collect1) {
            Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>> txx = tx._2();
            Iterable<HashMap<String, IPolypeptide>> hashMaps = txx._2();
            for (HashMap<String, IPolypeptide> txxx : hashMaps) {
                for (IPolypeptide iPolypeptide : txxx.values()) {
                    holder.add(iPolypeptide);
                }
            }
        }
        return holder;
    }


    @Test
    public void testeg3_20ScoringDone() throws Exception {

        XTandemMain application = CometTestingUtilities.getDefaultApplication();
        CometScoringAlgorithm comet = CometTestingUtilities.getComet(application);
        Scorer scorer = application.getScoreRunner();

        List<RawPeptideScan> scans = CometTestingUtilities.getAllScanFromMZXMLResource("/eg3_20/eg3_20.mzXML");
        Map<Integer, RawPeptideScan> mapped = new HashMap<Integer, RawPeptideScan>();
        for (RawPeptideScan scan : scans) {
            String id = scan.getId();
            mapped.put(new Integer(id), scan);
        }
        Map<Integer, List<UsedSpectrum>> cometUses = CometTestingUtilities.readUsedSpectraFromResource("/eg3_20/UsedSpectra_20.txt");

        IPeptideDigester digester = PeptideBondDigester.getDefaultDigester();
        digester.setNumberMissedCleavages(2);
        List<IPolypeptide> originalPeptides = ProteinParser.getPeptidesFromResource("/eg3_20/select_20.fasta", digester,
                CometTestingUtilities.MS_ONLY);

        for (Integer id : cometUses.keySet()) {
            RawPeptideScan rp = mapped.get(id);
            List<UsedSpectrum> usedSpectrums = cometUses.get(id);
            validatePeptidesScored(usedSpectrums, rp, comet, scorer,originalPeptides);
        }

    }


    @Test
    public void testScoringDone() throws Exception {
        // there are issues with this one
        IPolypeptide badTest = Polypeptide.fromString("YITM[15.995]TAQVM[15.995]M[15.995]KGYR");

        // read what comet scored
        List<UsedSpectrum> used = CometTestingUtilities.getSpectrumUsed(8852);
        Assert.assertEquals(311, used.size());

        XTandemMain application = CometTestingUtilities.getDefaultApplication();
        CometScoringAlgorithm comet = CometTestingUtilities.getComet(application);
        Scorer scorer = application.getScoreRunner();

        RawPeptideScan rp = CometTestingUtilities.getScanFromMZXMLResource("/000000008852.mzXML");

        IPeptideDigester digester = PeptideBondDigester.getDefaultDigester();
        digester.setNumberMissedCleavages(2);
        List<IPolypeptide> originalPeptides = ProteinParser.getPeptidesFromResource("/SmallSampleProteins.fasta", digester,
                CometTestingUtilities.M_ONLY);

        validatePeptidesScored(used, rp, comet, scorer, originalPeptides);

        //   Assert.assertEquals(0,weScoreNotComet.size());

    }

    public static void validatePeptidesScored(List<UsedSpectrum> used, RawPeptideScan rp,
                                              CometScoringAlgorithm comet, Scorer scorer,
                                              List<IPolypeptide> originalPeptides) {
        CometScoredScan spec = new CometScoredScan(rp, comet);

        Set<BinChargeKey> usedBins = BinChargeMapper.getSpectrumBins(rp);




        List<IPolypeptide> processed = CometUtilities.getPeptidesInKeyBins(originalPeptides, usedBins);

        List<IPolypeptide> notComet = new ArrayList<IPolypeptide>();
        List<IPolypeptide> matchesComet = new ArrayList<IPolypeptide>();


        // first cut - how many peptides tit comet not score
        for (IPolypeptide pp : processed) {
            if (getUsedSpectrum(pp, used) == null) {  // comet did not score
                notComet.add(pp); // comet did not score this
            } else {
                matchesComet.add(pp); // comet did not score this
            }
        }

        // we score everything they do
        List<IPolypeptide> cometScoresNotUs = notProcessedInComet(processed, used);
        Assert.assertEquals(0, cometScoresNotUs.size());


        // first cut - how many peptides tit comet not score
        List<IPolypeptide> weScoreNotComet = new ArrayList<IPolypeptide>();
        for (IPolypeptide pp : notComet) {
            if (getUsedSpectrum(pp, used) == null) {  // comet did not score
                CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
                if (willHydraScorePeptide(scorer, ts, spec, usedBins))
                    weScoreNotComet.add(pp); // comet did not score this
            }
        }
    }

}

