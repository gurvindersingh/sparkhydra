package com.lordjoe.distributed.hydra;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.MgfTitleTester
 * User: Steve
 * Date: 1/14/2015
 */
public class MgfTitleTester {

    private static void saveTitles(final String pArg) throws IOException {
        String fileName = pArg;
        LineNumberReader rdr = new LineNumberReader(new FileReader(fileName));
        String line = rdr.readLine();
        PrintWriter out = new PrintWriter(new FileWriter(fileName + "Titles.txt"));
        while (line != null) {
            if (line.startsWith("TITLE="))
                out.println(line.substring("TITLE=".length()));
            line = rdr.readLine();
        }
        out.close();
    }

    public static final int NUMBER_BINS = 100;

    private static void testTitles(final String pArg) throws IOException {
        int[] bins = new int[NUMBER_BINS];
        int numberSpectra = 0;
        String fileName = pArg;
        LineNumberReader rdr = new LineNumberReader(new FileReader(fileName));
        String line = rdr.readLine();
        numberSpectra++;
        while (line != null) {
            int hash = Math.abs(line.hashCode()) % NUMBER_BINS;
            bins[hash]++;
            numberSpectra++;
            line = rdr.readLine();
        }
        System.out.println("Number Spectra " + numberSpectra);
        for (int i = 0; i < bins.length; i++) {
            int bin = bins[i];
            System.out.println(i + " " + bin);
        }

    }


    public static void main(String[] args) throws Exception {
        // saveTitles(args[0]);
        testTitles(args[0]);
    }


}
