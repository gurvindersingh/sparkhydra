package com.lordjoe.distributed.tandem;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.tandem.XTandemComparator
 * class to compare and report on two different XTandem output files
 * ( could work for other formats as well
 * User: Steve
 * Date: 3/17/2015
 */
public class XTandemComparator {
    public static final XTandemComparator[] EMPTY_ARRAY = {};

    public final XTandemScoringReport report1;
    public final XTandemScoringReport report2;
    private final Map<String, MultiScanComparison> scanById = new HashMap<String, MultiScanComparison>();
    private final List<MultiScanComparison> scanByScore = new ArrayList<MultiScanComparison>();
    private final List<ScoredScan> scanByScore1Only = new ArrayList<ScoredScan>();
    private final List<ScoredScan> scanByScore2Only = new ArrayList<ScoredScan>();


    public XTandemComparator(final XTandemScoringReport pReport1, final XTandemScoringReport pReport2) {
        report1 = pReport1;
        report2 = pReport2;
        initialize();
    }

    protected Map<String, ScoredScan> toLabels(Map<String, ScoredScan> scans1) {
        Map<String, ScoredScan> byLabel1 = new HashMap<String, ScoredScan>();
        for (ScoredScan scoredScan : scans1.values()) {
            IMeasuredSpectrum raw = scoredScan.getRaw();
            if (raw instanceof RawPeptideScan) {
                RawPeptideScan rs = (RawPeptideScan) raw;
                String label = rs.getId();
         //       String label = rs.getLabel();
                 if (label != null)
                    byLabel1.put(label, scoredScan);
            }
        }
        return byLabel1;
    }

    protected void initialize() {
        Map<String, ScoredScan> scans1X = report1.getScoredScansMap();
        Map<String, ScoredScan> scans1 = toLabels(scans1X);

        Map<String, ScoredScan> scans2X = report2.getScoredScansMap();
        Map<String, ScoredScan> scans2 = toLabels(scans2X);

        List<String> commonKeys = new ArrayList<String>(scans1.keySet());
        commonKeys.retainAll(scans2.keySet());

        List<String> scan1OnlyKeys = new ArrayList<String>(scans1.keySet());
        scan1OnlyKeys.removeAll(scans2.keySet());

        List<String> scan2OnlyKeys = new ArrayList<String>(scans2.keySet());
        scan2OnlyKeys.removeAll(scans1.keySet());

        for (String key : scan2OnlyKeys) {
            ScoredScan scan2 = scans2.get(key);
            ISpectralMatch bestMatch = scan2.getBestMatch();
        }
        Set<String> commonKeySet = new HashSet<String>(commonKeys);

        int size1 = scans1.size();
        int size2 = scans2.size();
        int commonSize = commonKeys.size();
        for (String commonKey : scans1.keySet()) {
            if (commonKeySet.contains(commonKey)) {
                ScoredScan scan1 = scans1.get(commonKey);
                ScoredScan scan2 = scans2.get(commonKey);
                MultiScanComparison ms = new MultiScanComparison(scan1, scan2);
                scanById.put(ms.id, ms);
                scanByScore.add(ms);
            }
            else {
                ScoredScan scan1 = scans1.get(commonKey);
                String id1 = scan1.getRaw().getId();
                scanByScore1Only.add(scans1.get(commonKey));
            }
        }
        for (String key : scans2.keySet()) {
            if (!commonKeySet.contains(key)) {
                scanByScore2Only.add(scans2.get(key));
            }
        }

        MultiScanComparison.setNormalization(scanByScore);
        Collections.sort(scanByScore);

    }

    public void report(Appendable out) {
        for (String s : scanById.keySet()) {
            System.out.println(s);
        }

        int numberComparisons = scanByScore.size();
        int[] precentiles = new int[10];
        for (int i = 0; i < 10; i++) {
             precentiles[i] = ((1 + i) * numberComparisons) / 10;
          }

        int numberSame = 0;
        int numberDifferent = 0;
        int precentileIndex = 0;
        int index = 0;
        for (MultiScanComparison ms : scanByScore) {
            if (ms.isPeptideSame())
                  numberSame++;
            else {
                numberDifferent++;
            }
           if(index++ >= precentiles[precentileIndex])  {
               System.out.println("pctile " + precentileIndex + " number " + index + " same " + numberSame + " different " + numberDifferent);
               precentileIndex++;
           }
        }
        System.out.println("pctile " + precentileIndex + " number " + index + " same " + numberSame + " different " + numberDifferent);
        System.out.println("Number same " + numberSame);

        for (ScoredScan scoredScan : scanByScore1Only) {
            numberDifferent++;

        }
        for (ScoredScan scoredScan : scanByScore2Only) {
            numberDifferent++;
        }
    }

    public static void usage() {
        System.out.println("usage XTandemComparator <file1> <file2>");
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            return;
        }

        File f1 = new File(args[0]);
        File f2 = new File(args[1]);
        XTandemScoringReport report1 = XTandemUtilities.readXTandemFile(args[0]);
        XTandemScoringReport report2 = XTandemUtilities.readXTandemFile(args[1]);

        XTandemComparator me = new XTandemComparator(report1, report2);

        me.report(System.out);
    }
}
