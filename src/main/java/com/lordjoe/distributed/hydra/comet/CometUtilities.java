package com.lordjoe.distributed.hydra.comet;

//import org.proteios.io.*;

import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static List<IPolypeptide> getPeptidesInBins(List<IPolypeptide> peptides,Set<Integer> bins)
    {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (IPolypeptide peptide : peptides) {
            BinChargeKey binChargeKey = BinChargeMapper.keyFromPeptide(peptide);
            if(bins.contains(binChargeKey.getMzInt()))
                holder.add(peptide);
        }
        return holder;
    }

    public static List<IPolypeptide> getPeptidesInKeyBins(List<IPolypeptide> peptides,Set<BinChargeKey> bins)
    {
         return getPeptidesInBins(peptides,getSpectrumBinsIntegers(bins));
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
}
