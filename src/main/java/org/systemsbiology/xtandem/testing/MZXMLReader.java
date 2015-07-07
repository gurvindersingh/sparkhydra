package org.systemsbiology.xtandem.testing;

import com.lordjoe.utilities.*;
import org.apache.hadoop.fs.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.MZXMLReader
 * basic timing to parse am mzxml file EXACTLY like a mapper
 * User: steven
 * Date: 10/25/11
 */
public class MZXMLReader {

    private static int gNestedTotalScans;
    private static int gTotalScans;
    private static ElapsedTimer gTimer = new ElapsedTimer();

    public static RawPeptideScan[] processFile(final File pFile)   {
        try {
            String name = pFile.getAbsolutePath();
            XMLUtilities.outputLine(name);
            InputStream is = XTandemUtilities.getDescribedStream(name);
            return processStream(is);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    public static RawPeptideScan[] processStream(final InputStream pIs) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<RawPeptideScan> holder = new ArrayList<RawPeptideScan>();
        
         LineNumberReader rdr = new LineNumberReader(new InputStreamReader(pIs));
        String line = rdr.readLine();
        int scanLevel = 0;
          while (line != null) {
            if (line.contains("<scan")) {
                scanLevel++;
                break;
            }
            line = rdr.readLine();
        }
         if(scanLevel > 0)
             sb.append(line);

          do {

            line = rdr.readLine();
            sb.append(line);
            sb.append("\n");
            if (line.contains("<scan")) {
                gTotalScans++;
                scanLevel++;
            }
            if (line.contains("</scan")) {
                scanLevel--;
                RawPeptideScan scn = handleScan(sb, scanLevel);
                if(scn != null)
                    holder.add(scn);
            }
        }
        while (!line.contains("</msRun>"));
        RawPeptideScan[] ret = new RawPeptideScan[holder.size()];
         holder.toArray(ret);
         return ret;
    }

    private static void handleLocalFile(final String pArg) throws IOException {
        File indir = new File(pArg);
        if (indir.isDirectory()) {
            File[] files = indir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                processFile(file);
            }
        }
        else {
            processFile(indir);

        }
        gTimer.showElapsed("Handled " + gTotalScans + " nested " + gNestedTotalScans);
    }


    private static void handleHDFSFile(final String pArg) throws IOException {
        IHDFSFileSystem fs = HDFSAccessor.getFileSystem();

        if (fs.isDirectory(pArg)) {
            throw new UnsupportedOperationException("Fix This"); // ToDo
        }
        else {
            String name = pArg;
            XMLUtilities.outputLine(name);
            Path path = new Path(name);
            InputStream is = fs.openFileForRead(path);
            processStream(is);
        }
        gTimer.showElapsed("Handled " + gTotalScans + " nested " + gNestedTotalScans);
    }


    public static RawPeptideScan handleScan(final String pSb ) {
         return handleScan(new StringBuilder(pSb),2);
    }


    public static RawPeptideScan handleScan(final StringBuilder pSb, final int pScanLevel) {
        String scn = pSb.toString().trim();
        if (scn.startsWith("<scan")) {
            RawPeptideScan scan = XTandemHadoopUtilities.readScan(scn,null);
            pSb.setLength(0);
            gTotalScans++;
            return scan;
          }
        else {
            gNestedTotalScans++;
        }
        pSb.setLength(0);
        return null;
    }



    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String arg = args[0];
        if (arg.startsWith("hdfs:/")) {
             handleHDFSFile(arg.substring("hdfs:/".length()));
        }
        else {
            handleLocalFile(arg);
        }
    }

}
