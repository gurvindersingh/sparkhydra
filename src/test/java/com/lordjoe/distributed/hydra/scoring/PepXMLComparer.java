package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.hydra.test.TestUtilities;
import org.systemsbiology.xtandem.fdr.*;
import org.systemsbiology.xtandem.peptide.IPolypeptide;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.PepXMLComparer
 * User: Steve
 * Date: 1/24/2015
 */
public class PepXMLComparer {

    public static ProteinPepxmlParser readOnePepXML(String file) {
        boolean onlyUniquePeptides = false;
        ProteinPepxmlParser fdrParser = new ProteinPepxmlParser(file);
        fdrParser.readFileAndGenerate(onlyUniquePeptides);
        return fdrParser;
    }


    public static void main(String[] args) {
        List<ProteinPepxmlParser> holder = new ArrayList<ProteinPepxmlParser>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            holder.add(readOnePepXML(arg));
        }

        if (holder.size() <= 1)
            return;

        int oneNotScored = 0;
        int score1Better = 0;
        int score1Eventually = 0;
        int notSame = 0;
        int samePeptide = 0;
        List<DifferentScoringResult>  differences = new ArrayList<DifferentScoringResult>();
        ProteinPepxmlParser p1 = holder.get(0);
        ProteinPepxmlParser p2 = holder.get(1);
        Map<String, List<ProteinPepxmlParser.SpectrumHit>> spectrumHits1 = p1.getSpectrumHits();
        Map<String, List<ProteinPepxmlParser.SpectrumHit>> spectrumHits2 = p2.getSpectrumHits();
        List<String> keys = new ArrayList<String>(spectrumHits1.keySet());
        Collections.sort(keys);
        for (String s : keys) {
            List<ProteinPepxmlParser.SpectrumHit> spectruRanks = spectrumHits1.get(s);
            List<ProteinPepxmlParser.SpectrumHit> spectruRanks2 = spectrumHits2.get(s);
            if(spectruRanks!= null && spectruRanks2 != null)       {
                ProteinPepxmlParser.SpectrumHit  hit1 = spectruRanks.get(0);   // our best
                ProteinPepxmlParser.SpectrumHit  hit2 = spectruRanks2.get(0);
                IPolypeptide pp1 = hit1.peptide;
                IPolypeptide pp2 = hit2.peptide;
                if(pp1.equivalent(pp2))
                    samePeptide++;
                else {
                    if(!pp1.isModified() && !pp2.isModified())
                        System.out.println(pp1 + "\t" + pp2);
                    double score1 = hit1.hypderscore;
                    double score2 = hit2.hypderscore;
                    if(score1 > score2)
                        score1Better++;
                    DifferentScoringResult dif =  handleDifferentScores(  hit2, spectruRanks);
                    differences.add(dif) ;
                    if(dif.peptideScored)
                        score1Eventually++;
                    notSame++;
                }
            }
            else {
                oneNotScored++;
            }
//            if (spectruRanks != null)
//                for (ProteinPepxmlParser.SpectrumHit spectrumHit : spectruRanks) {
//                    System.out.println(spectrumHit);
//
//                }
//            System.out.println();
//            spectrumHits = spectrumHits2.get(s);
//            if (spectrumHits != null)
//                for (ProteinPepxmlParser.SpectrumHit spectrumHit : spectrumHits) {
//                    System.out.println(spectrumHit );
//
//                }
//            System.out.println();
//            System.out.println();
        }

        int lowerScoreSeen = 0;
        int dissimilar = 0;

        for (DifferentScoringResult difference : differences) {
            if(difference.lowerscoreSeen)
                lowerScoreSeen++;
            if(difference.peptideScored && !difference.scoreSimilar)
                dissimilar++;
        }

    }

    public static DifferentScoringResult handleDifferentScores(  ProteinPepxmlParser.SpectrumHit hit1,List<ProteinPepxmlParser.SpectrumHit> spectruRanks2) {
         IPolypeptide peptide1 = hit1.peptide;
        double score = hit1.hypderscore;
        boolean lowerscoreseen = false;
        for (ProteinPepxmlParser.SpectrumHit spectrumHit : spectruRanks2) {
            if(spectrumHit.hypderscore < score)
                lowerscoreseen = true;
             if(peptide1.equivalent(spectrumHit.peptide))   {
                 boolean scoreSimilar = Math.abs(score - spectrumHit.hypderscore) < 0.05;
                 if(!scoreSimilar)
                     TestUtilities.breakHere();

                 return new DifferentScoringResult(peptide1, scoreSimilar,true,false);
            }
        }
        return new DifferentScoringResult(peptide1,false,false,lowerscoreseen);
    }

    public static class DifferentScoringResult
    {
        public final IPolypeptide peptide;
        public final boolean scoreSimilar;
        public final boolean peptideScored;
        public final boolean lowerscoreSeen;

        public DifferentScoringResult(IPolypeptide peptide, boolean scoreSimilar, boolean peptideScored,boolean lowerscoreSeen) {
            this.peptide = peptide;
            this.scoreSimilar = scoreSimilar;
            this.peptideScored = peptideScored;
            this.lowerscoreSeen = lowerscoreSeen;
        }
    }

}
