package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.spectrum.*;
import org.systemsbiology.xtandem.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.VerboseMgfCleaner
 * User: Steve
 * Date: 1/21/2015
 */
public class VerboseMgfCleaner {

    public static final int BIG_SPECTRUM_SIZE = 10000;
    public static final int MAX_SPECTRA = 1000;
    public static final int MAX_PROTEINS = 20000;

    public static final String[] INTERESTING_SCANS = {
            "131104_Berit_BSA2.11685.11685.3",
            "131104_Berit_BSA2.16405.16405.3",
            "131104_Berit_BSA2.13178.13178.3",
            "131104_Berit_BSA2.10673.10673.3"
    };

    public static final Set<String>  INTERESTING_SCAN_SET = new HashSet<String>(Arrays.asList(INTERESTING_SCANS));

    private static void writeCleanMgfFile(final PrintWriter pOut, final InputStream pIs) {
        MassSpecRun[] massSpecRuns = XTandemUtilities.parseMgfFile(pIs, "");


        int scanCount = 0;
        for (int i = 0; i < massSpecRuns.length; i++) {
            MassSpecRun massSpecRun = massSpecRuns[i];

            RawPeptideScan[] scans = massSpecRun.getScans();
            for (int j = 0; j < scans.length; j++) {
                RawPeptideScan scan = scans[j];
                String id = scan.getId();
                scan.appendAsMGF(pOut);
                scanCount++;
            }
            pOut.close();
        }
    }

    private static void writeShorterMgfFile(final PrintWriter pOut, final InputStream pIs, int MaxSpectra) throws Exception {
        int linesPerItem = 0;
        int totalLines = 0;
        int totalWritten = 0;
        int totalBig = 0;
        LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pIs));
        int scanCount = 0;
        String line = rdr.readLine();
         while (line != null) {
            if (line.contains("BEGIN IONS")) {
                scanCount++;
                int average = totalLines / scanCount;
                if (linesPerItem > (5 * average)) {
                    totalBig++;
                    System.out.println("large " + linesPerItem + " average " + average);
                }
                linesPerItem = 0;
            }
            if (scanCount < MaxSpectra) {
                totalWritten++;
                pOut.println(line);
                System.out.println(line);
            }
            else {
                totalWritten++;
                totalWritten--;

            }
            line = rdr.readLine();
            linesPerItem++;
            totalLines++;
        }
        System.out.println("Read " + scanCount + " scans  wrote " + MaxSpectra +
                        " total big " + totalBig +
                        " total lines " + totalLines +
                        " total written " + totalWritten
        );
        rdr.close();
        pOut.close();
    }


    private static void writeShorterMgfWithBigSpectraFile(final PrintWriter pOut, final InputStream pIs, int MaxSpectra) throws Exception {
        int linesPerItem = 0;
        int totalLines = 0;
        int totalWritten = 0;
        int totalBig = 0;
        List<String> holder = new ArrayList<String>();


        LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pIs));
        boolean retainScan = false;
        int scanCount = 0;
        String line = rdr.readLine();
        while (line != null) {
            if (line.contains("BEGIN IONS")) {
                if (!holder.isEmpty()) {
                    if (retainScan) {
                        scanCount++;
                        for (String s : holder) {
                            pOut.println(s);
                        }
                    }
                    holder.clear();
                    retainScan = false;
                }
                linesPerItem = 0;
            }
            if(line.startsWith("TITLE="))  {
                retainScan = INTERESTING_SCAN_SET.contains(line.substring("TITLE=".length()).trim());
            }
            if (scanCount < MaxSpectra) {
                totalWritten++;
                holder.add(line);

            }
            line = rdr.readLine();
            linesPerItem++;
            totalLines++;
        }
        System.out.println("Read " + scanCount + " scans  wrote " + MaxSpectra +
                        " total big " + totalBig +
                        " total lines " + totalLines +
                        " total written " + totalWritten
        );
        rdr.close();
        pOut.close();
    }

    private static void writeInterestingScans(final PrintWriter pOut, final InputStream pIs) throws Exception {
          int linesPerItem = 0;
          int totalLines = 0;
          int totalWritten = 0;
          int totalBig = 0;
          List<String> holder = new ArrayList<String>();
           int numberWritten = 0;

          LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pIs));
          boolean retainScan = false;
          int scanCount = 0;
          String line = rdr.readLine();
          while (line != null) {
              if (line.contains("BEGIN IONS")) {
                  if (!holder.isEmpty()) {
                      if (retainScan) {
                          scanCount++;
                          for (String s : holder) {
                              pOut.println(s);
                          }
                          numberWritten++;
                      }
                      holder.clear();
                      retainScan = false;
                  }
                  linesPerItem = 0;
              }
              if(line.startsWith("TITLE="))  {
                  String testTitle = line.substring("TITLE=".length()).trim();
                  if(INTERESTING_SCAN_SET.contains(testTitle))
                        retainScan = true;
              }
               holder.add(line);
               line = rdr.readLine();
              linesPerItem++;
              totalLines++;
          }
          System.out.println("Read " + scanCount + " scans  wrote " + numberWritten +
                           " total lines " + totalLines
            );
          rdr.close();
          pOut.close();
      }

    private static void appendHolder(List<String> holder, final PrintWriter pOut) {
        if (!holder.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (holder.size() > 10) {
                for (String s : holder) {
                    sb.append(s);
                    sb.append("\n");
                }
                String realString = SparkSpectrumUtilities.cleanMGFRepresentation(sb.toString());
                pOut.println(realString);
            }
            holder.clear();
        }

    }

    private static Set<String> readSelectedTitles(File template) throws Exception {

        Set<String> selectedTitles = new HashSet<String>();
        LineNumberReader rdr = new LineNumberReader(new FileReader(template));
        String line = rdr.readLine();
        while (line != null) {
            if (line.contains("TITLE=")) {
                selectedTitles.add(line);
            }
            line = rdr.readLine();
        }
        rdr.close();

        return selectedTitles;
    }


    private static void writeSelectedFullMGFS(final PrintWriter pOut, final InputStream pIs, File template) throws Exception {

        Set<String> titles = readSelectedTitles(template);

        int linesPerItem = 0;
        int totalLines = 0;
        int totalWritten = 0;
        int totalBig = 0;
        List<String> holder = new ArrayList<String>();

        boolean saveHolder = false;
        LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pIs));
        int scanCount = 0;
        String line = rdr.readLine();
        while (line != null) {
            if (titles.contains(line))
                saveHolder = true;
            if (line.contains("BEGIN IONS")) {
                if (saveHolder) {
                    appendHolder(holder, pOut);
                    totalWritten++;
                    saveHolder = false;
                }
                holder.clear();
                linesPerItem = 0;

            }
            holder.add(line);
            line = rdr.readLine();
            linesPerItem++;
            totalLines++;
        }
        System.out.println("Read " + scanCount + " scans  wrote " + totalWritten +
                        " total big " + totalBig +
                        " total lines " + totalLines +
                        " total written " + totalWritten
        );
        rdr.close();
        pOut.close();
    }


    private static void writeCleanMGFFile(final PrintWriter pOut, final InputStream pIs) throws Exception {
        int linesPerItem = 0;
        int totalLines = 0;
        int totalWritten = 0;
        int totalBig = 0;
        List<String> holder = new ArrayList<String>();


        LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pIs));
        int scanCount = 0;
        String line = rdr.readLine();
        while (line != null) {
            if (line.contains("BEGIN IONS")) {
                appendHolder(holder, pOut);
                linesPerItem = 0;
                totalWritten++;
            }
            holder.add(line);
            line = rdr.readLine();
            linesPerItem++;
            totalLines++;
        }
        System.out.println("Read " + scanCount + " scans  wrote " + totalWritten +
                        " total big " + totalBig +
                        " total lines " + totalLines +
                        " total written " + totalWritten
        );
        rdr.close();
        pOut.close();
    }

    /**
     * @param pOut
     * @param pIs
     * @param MaxSpectra
     * @throws Exception
     */
    private static void writeShorterFastaFile(final PrintWriter pOut, final InputStream pIs, int MaxItems) throws Exception {

        LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pIs));
        int scanCount = 0;
        String line = rdr.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                scanCount++;
            }
            if (scanCount < MaxItems) {
                pOut.println(line);
            }
            pOut.println(line);
            line = rdr.readLine();
        }
        System.out.println("Read " + scanCount + " proteins  wrote " + MaxItems);
        rdr.close();
        pOut.close();
    }

    public static void main(String[] args) throws Exception {
        File in = new File(args[0]);
        File outFile = new File(args[1]);
        File templateFile = null;
        if (args.length > 2)
            templateFile = new File(args[2]);
        PrintWriter out = new PrintWriter(new FileWriter(outFile));

        InputStream is = new FileInputStream(in);

        // this routine comply combines spectra and writes a reduces file
        writeCleanMgfFile(out, is);

        // this routine writes scans in INTERESTING_SCANS
        // writeInterestingScans(out, is );

       // this routine chops the number of spectra at  MAX_SPECTRA
        //writeShorterMgfFile(out, is, MAX_SPECTRA);

        // this routine chops the number of spectra at  MAX_SPECTRA
        //writeShorterMgfWithBigSpectraFile(out, is, MAX_SPECTRA);

        // this routine chops the number of proteins at  MAX_PROTEINS
        // writeShorterFastaFile(out, is, MAX_PROTEINS);

        // writeCleanMGFFile(out, is);

        // writeSelectedFullMGFS(out, is,templateFile);
    }


}
