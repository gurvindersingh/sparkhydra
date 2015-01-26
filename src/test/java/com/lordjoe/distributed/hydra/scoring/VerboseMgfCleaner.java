package com.lordjoe.distributed.hydra.scoring;

import org.systemsbiology.xtandem.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.scoring.VerboseMgfCleaner
 * User: Steve
 * Date: 1/21/2015
 */
public class VerboseMgfCleaner {

    public static void main(String[] args) throws Exception {
        File in = new File(args[0]);
        File outFile = new File(args[1]);
        PrintWriter out = new PrintWriter(new FileWriter(outFile));

        InputStream is = new FileInputStream(in);
        MassSpecRun[] massSpecRuns = XTandemUtilities.parseMgfFile(is, "");


        int scanCount = 0;
        for (int i = 0; i < massSpecRuns.length; i++) {
            MassSpecRun massSpecRun = massSpecRuns[i];

            RawPeptideScan[] scans = massSpecRun.getScans();
            for (int j = 0; j < scans.length; j++) {
                RawPeptideScan scan = scans[j];
                String id = scan.getId();
                  scan.appendAsMGF(out);
                scanCount++;
            }
            out.close();
        }
    }

}
