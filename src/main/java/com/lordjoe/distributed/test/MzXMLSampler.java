package com.lordjoe.distributed.test;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.test.MzXMLSampler
 * Sample spectra from am mzXML file selectign those with ids in the set ids writing the results to an
 * output file name
 * User: Steve
 * Date: 4/14/2015
 */
public class MzXMLSampler {

    public static void makeSampledMZXML(File input, Set<Integer> ids, String outFileName) throws Exception {
        PrintWriter out = new PrintWriter(new FileWriter(outFileName));
        LineNumberReader rdr = new LineNumberReader(new FileReader(input));
        copySelectedSpectra(rdr, ids, out);
    }

    private static void copySelectedSpectra(final LineNumberReader rdr, final Set<Integer> ids, final PrintWriter out) throws Exception {
        copyHeader(rdr, ids, out);
        copySpectra(rdr, ids, out);
        copyFooter(rdr, ids, out);

    }

    private static void copyHeader(final LineNumberReader rdr, final Set<Integer> ids, final PrintWriter out) throws Exception {
        String line = rdr.readLine();
        while (!line.contains("</dataProcessing>")) {
            out.println(line);
            line = rdr.readLine();
        }

        out.println(line);

    }

    private static void copySpectra(final LineNumberReader rdr, final Set<Integer> ids, final PrintWriter out) throws Exception {
        String line = rdr.readLine();
        while (!line.contains("</msRun>")) {
            if (line.contains("<scan num=")) {
                processSpectrum(line, rdr, ids, out);
            }
            line = rdr.readLine();
        }
        out.println(line);
    }

    private static void processSpectrum(String line, final LineNumberReader rdr, final Set<Integer> ids, final PrintWriter out) throws Exception {
        String idStr = line.replace("<scan num=\"", "").trim();
        idStr = idStr.substring(0, idStr.indexOf("\"")).trim();
        Integer id = Integer.parseInt(idStr);
        boolean useSpectrum = ids.contains(id);
        while (!line.contains("</scan>")) {
            if (useSpectrum)
                out.println(line);
            line = rdr.readLine();
        }
        if (useSpectrum) {
            out.println(line);
            ids.remove(id);
        }
    }

    private static void copyFooter(final LineNumberReader rdr, final Set<Integer> ids, final PrintWriter out) throws Exception {
        out.println("</mzXML>");
        out.close();
    }

    public static void main(String[] args) throws Exception {
        Set<Integer> ids = new HashSet<Integer>() ;
        for (int i = 0; i < 100; i++) {
            ids.add(20 * i);
         }
        File inp = new File("eg3.mzXML");
        makeSampledMZXML(inp,ids,"eg3Test.mzXML");
    }
}
