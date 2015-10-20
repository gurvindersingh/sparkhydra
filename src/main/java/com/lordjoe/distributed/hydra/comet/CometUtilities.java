package com.lordjoe.distributed.hydra.comet;

//import org.proteios.io.*;

import com.lordjoe.distributed.hydra.fragment.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometUtilities
 * User: Steve
 * Date: 4/5/13
 */
public class CometUtilities implements Serializable {

    public static RawPeptideScan[] readMzXML(String f) {
        File file = new File(f);
        return readMzXML(file);

    }

    private static RawPeptideScan[] readMzXML(final File pFile) {
        return MZXMLReader.processFile(pFile);

    }

    public static void writeSpectra(final String pArg, final RawPeptideScan[] pSpectra) {
        try {
            PrintWriter pw = new PrintWriter(pArg + ".mgf");
            for (int j = 0; j < pSpectra.length; j++) {
                RawPeptideScan rs = pSpectra[j];
                rs.appendAsMGF(pw);
            }
            pw.close();
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * normalize an array to a maximum of maximum also set negative values to 0
     *
     * @param values
     * @param maximum
     */
    public static void normalizeTo(float[] values, float maximum) {
        float max = Float.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.max(0, values[i]);   // might as well drop negative values
            max = Math.max(max, values[i]);
        }
        if (max == 0)
            return;
        if (max < 0)
            throw new IllegalStateException("normalizeTo assumes poisitc data");
        float factor = maximum / max;
        for (int i = 0; i < values.length; i++) {
            values[i] *= factor;
        }

    }

    public static List<IPolypeptide> getPeptidesInBins(List<IPolypeptide> peptides, Set<Integer> bins) {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (IPolypeptide peptide : peptides) {
            BinChargeKey binChargeKey = BinChargeMapper.keyFromPeptide(peptide);
            if (bins.contains(binChargeKey.getMzInt()))
                holder.add(peptide);
        }
        return holder;
    }

    public static List<IPolypeptide> getPeptidesInKeyBins(List<IPolypeptide> peptides, Set<BinChargeKey> bins) {
        return getPeptidesInBins(peptides, getSpectrumBinsIntegers(bins));
    }


    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            RawPeptideScan[] spectra = readMzXML(arg);
            writeSpectra(arg, spectra);

        }
    }


    public static Set<Integer> getSpectrumBinsIntegers(Set<BinChargeKey> used) {
        Set<Integer> ret = new HashSet<Integer>();
        for (BinChargeKey binChargeKey : used) {
            ret.add(binChargeKey.getMzInt());
        }
        return ret;
    }

    public static void fromCometParameters(XTandemMain holder, String text) {
        try {
            fromCometParameters(holder,new ByteArrayInputStream(text.getBytes("UTF-8")));
          }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);

        }
    }

    public static void fromCometParameters(XTandemMain holder, InputStream is) {
          try {
                LineNumberReader inp = new LineNumberReader(new InputStreamReader(is));
              fromCometParameters(holder, inp);
          }
          catch ( Exception e) {
              throw new RuntimeException(e);

          }
      }



    public static void fromCometParameters(XTandemMain holder, LineNumberReader rdr) {
        String line = null;
        try {
              line = rdr.readLine();
            while (line != null) {
                handleLine(holder, line);
                line = rdr.readLine();
            }
        }
        catch (UnsupportedOperationException e) {
            throw new RuntimeException(e);

        }
        catch (IOException e) {
              throw new RuntimeException(e);

          }
          finally {
            try {
                rdr.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);

            }
        }
    }

    private static void handleLine(final XTandemMain pHolder, String pLine) {
        pLine = pLine.trim();
        if (pLine.length() == 0)
            return; // empty line
        if (pLine.startsWith("#"))
            return; // comment
        // drop terminal comments
        int hashIndex = pLine.indexOf("#");
        if(hashIndex > -1)  {
            pLine = pLine.substring(0,hashIndex).trim();
        }
        if (pLine.startsWith("["))  // like  [COMET_ENZYME_INFO]
             return; // comment
        char firstChar = pLine.charAt(0);
        if(Character.isDigit(firstChar))
            return;    // 0.  No_enzyme              0      -           -

         String[] items = pLine.split("=");
        if (items.length < 2)
              return; // empty line
        String prop = items[0].trim();
        String value = items[1].trim();

        CometProperties.PropertyHandler handler = CometProperties.getHandler(prop);
        if( handler == null)    {
            throw new UnsupportedOperationException("no handler for " + prop); // ToDo
        }
        try {
            handler.handleProperty(pHolder,value);
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }

    }


}
