package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.IEquivalent;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.Polypeptide;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.lordjoe.distributed.hydra.comet.UsedSpectrum
 *
 * @author Steve Lewis
 * @date 5/19/2015
 */
public class UsedSpectrum implements IEquivalent<UsedSpectrum> {

    public static Map<Integer, List<UsedSpectrum>> readUsedSpectra(String fileName) {
        try {
            InputStream is = new FileInputStream(fileName);
            return readUsedSpectra(is);
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static Map<Integer, List<UsedSpectrum>> readUsedSpectra(InputStream is) {
        try {
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(is));
            Map<Integer, List<UsedSpectrum>> ret = new HashMap<Integer, List<UsedSpectrum>>();

            String line = rdr.readLine();
            while (line != null) {
                UsedSpectrum read = new UsedSpectrum(line);
                int spectrumIndex = read.spectrumIndex;
                if (!ret.containsKey(spectrumIndex)) {
                    ret.put(spectrumIndex, new ArrayList<UsedSpectrum>());
                }
                ret.get(spectrumIndex).add(read);
                line = rdr.readLine();
            }
            rdr.close();
            return ret;
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public final int spectrumIndex;
    public final IPolypeptide peptide;
    public final double peptideMass;
    public final double score;
    public final double spectrumMass;

    public UsedSpectrum(int spectrumIndex, IPolypeptide peptide, double spectrumMass, double score, double peptideMass) {
        this.spectrumIndex = spectrumIndex;
        this.peptide = peptide;
        this.peptideMass = peptideMass;
        this.score = score;
        this.spectrumMass = spectrumMass;
    }

    public UsedSpectrum(String line) {
        String[] items = line.split("\t");
        int indes = 0;
        this.spectrumIndex = Integer.parseInt(items[indes++]);
        this.peptide = Polypeptide.fromString(items[indes++]);
        this.spectrumMass = Double.parseDouble(items[indes++]);
        this.score = Double.parseDouble(items[indes++]);
        this.peptideMass = Double.parseDouble(items[indes++]);
    }

    @Override
    public String toString() {
        return spectrumIndex +
                " " + peptide +
                 " " + score;
    }

    @Override
    public boolean equivalent(UsedSpectrum o) {
        if (spectrumIndex != o.spectrumIndex)
            return false;
        if (Math.abs(peptideMass - o.peptideMass) > 0.01)
            return false;
        if (Math.abs(spectrumMass - o.spectrumMass) > 0.01)
            return false;
        if (Math.abs(score - o.score) > 0.01)
            return false;

        if (!equivalentPeptide(peptide, o.peptide))
            return false;
        return true;
    }

    public static boolean equivalentPeptide(IPolypeptide peptide, IPolypeptide peptide1) {
        String ps1 = adjustPeptideString(peptide.toString());
        String ps2 = adjustPeptideString(peptide1.toString());
        return ps1.equals(ps2);
    }

    /**
     * convert strings like RYTAIEVDLAM[15.995]K to RYTAIEVDLAM[15]K    since this is the form of
     * comet output
     *
     * @param s
     * @return
     */
    public static String adjustPeptideString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                while (c != ']') {
                    c = s.charAt(i++);
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Map<Integer, List<UsedSpectrum>> spectra = readUsedSpectra(args[0]);
        for (Integer k : spectra.keySet()) {
            List<UsedSpectrum> usedSpectrums = spectra.get(k);
            for (UsedSpectrum usedSpectrum : usedSpectrums) {

            }
        }
    }

}
