package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.hydra.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.fdr.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.taxonomy.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.GoodSampleConstructor
 * User: Steve
 * Date: 1/24/2015
 */
public class GoodSampleConstructor {

    public static List<ProteinPepxmlParser.SpectrumHit> readOnePepXML(ProteinPepxmlParser pp) {
        Collection<ProteinPepxmlParser.SpectrumHit> values = pp.getSpectrumHits().values();
        List<ProteinPepxmlParser.SpectrumHit> sortedHits = new ArrayList<ProteinPepxmlParser.SpectrumHit>(values);
        Collections.sort(sortedHits, new SpectrumHitHyperscoreComparator());
        return sortedHits;
    }


    public static ProteinPepxmlParser readOnePepXML(String file) {
        boolean onlyUniquePeptides = false;
        ProteinPepxmlParser fdrParser = new ProteinPepxmlParser(file);
        fdrParser.readFileAndGenerate(onlyUniquePeptides);
        return fdrParser;
    }

    public static void writeProteins(final String pOutProteinName, final Set<IProtein> pProteins) throws IOException {
        PrintWriter fastaOut = new PrintWriter(new FileWriter(pOutProteinName));
        for (IProtein protein : pProteins) {
            fastaOut.append(">");
            fastaOut.append(protein.getAnnotation());
            fastaOut.append("\n");
            String s = SparkHydraUtilities.asFastaString(protein.getSequence());
            fastaOut.append(s);
        }
        fastaOut.close();
    }

    public static void peptidesToProteins(final String pFastaFileName, final Set<String> pPeptides, final Set<IProtein> pProteins) throws FileNotFoundException {

        SeekingFastaHandler handler = new SeekingFastaHandler(pPeptides, pProteins);
        FastaParser fp = new FastaParser();
        fp.addHandler(handler);
        FileInputStream is = new FileInputStream(pFastaFileName);
        fp.parseFastaFile(is, "");
        handler.showHandled();
        if (pPeptides.size() > 0) {
            throw new UnsupportedOperationException("There are " + pPeptides.size() + " Unfound peptides");
        }
    }

    public static void writeSelectedSpectra(final String mgfFileName, final String pOutMgfName, final Set<String> pSpectralIds) throws IOException {
        PrintWriter mgfOut = new PrintWriter(new FileWriter(pOutMgfName));
        InputStream is = new FileInputStream(mgfFileName);
        MassSpecRun[] runs = XTandemUtilities.parseMgfFile(is, "");
        for (int i = 0; i < runs.length; i++) {
            MassSpecRun run = runs[i];
            for (String spectralId : pSpectralIds) {
                RawPeptideScan scan = run.getScan(spectralId);
                if (scan != null) {
                    scan.appendAsMGF(mgfOut);
                }
            }

        }
        mgfOut.close();
    }

    public static class SpectrumHitHyperscoreComparator implements Comparator<ProteinPepxmlParser.SpectrumHit> {
        @Override
        public int compare(final ProteinPepxmlParser.SpectrumHit o1, final ProteinPepxmlParser.SpectrumHit o2) {
            double h1 = o1.hypderscore;
            double h2 = o2.hypderscore;
            return Double.compare(h2, h1);
        }
    }

    public static class SeekingFastaHandler implements IFastaHandler {
        private final Set<String> m_peptides;
        private final Set<IProtein> m_proteins;
        private int handledPeptides;
        private int unhandledPeptides;

        public SeekingFastaHandler(final Set<String> pPeptides, Set<IProtein> proteins) {
            m_peptides = pPeptides;
            handledPeptides = pPeptides.size();
            m_proteins = proteins;
        }

        @Override
        public void handleProtein(final String annotation, final String sequence) {
            Set<String> removed = new HashSet<String>();
            for (String peptide : m_peptides) {
                if (sequence.contains(peptide)) {
                    Protein protein = Protein.getProtein(annotation, annotation, sequence, "");
                    m_proteins.add(protein);
                    removed.add(peptide);
                }
            }
            m_peptides.removeAll(removed);

        }


        public int getHandledPeptides() {
            return handledPeptides;
        }

        public int getUnhandledPeptides() {
            return unhandledPeptides;
        }

        public void showHandled() {
            System.out.println("Handled " + (handledPeptides - m_peptides.size()) + " UnHandled " + m_peptides.size() + " Proteins " + m_proteins.size());

        }
    }


    public static void main(String[] args) throws Exception {
        int index = 0;
        String inFileName = args[index++];
        int keepBest = Integer.parseInt(args[index++]);

        String mgfFileName = args[index++];
        String fastaFileName = args[index++];
        String outMgfName = "selectedMGF" + keepBest + ".mgf";
        String outProteinName = "selectedProtein" + keepBest + ".fasta";

        ProteinPepxmlParser pp = readOnePepXML(inFileName);

        List<ProteinPepxmlParser.SpectrumHit> sortedHits = readOnePepXML(pp);

        sortedHits = sortedHits.subList(0, Math.min(sortedHits.size(), keepBest));

        showBestHits(sortedHits, keepBest);

        Set<String> peptides = new HashSet<String>();
        Set<String> spectralIds = new HashSet<String>();
        Set<IProtein> proteins = new HashSet<IProtein>();
        Map<String, IMeasuredSpectrum> spectra = new HashMap<String, IMeasuredSpectrum>();

        for (ProteinPepxmlParser.SpectrumHit sortedHit : sortedHits) {
            spectralIds.add(sortedHit.id);
            IPolypeptide peptide = sortedHit.peptide;
            peptides.add(peptide.getSequence());
        }

        writeSelectedSpectra(mgfFileName, outMgfName, spectralIds);

        peptidesToProteins(fastaFileName, peptides, proteins);
        writeProteins(outProteinName, proteins);
    }

    private static void showBestHits(final List<ProteinPepxmlParser.SpectrumHit> hits, int number) {
        try {
            PrintWriter mgfOut = new PrintWriter(new FileWriter("FoundHits" + number + ".txt"));
            for (ProteinPepxmlParser.SpectrumHit hit : hits) {
                mgfOut.append(hit.id);
                mgfOut.append("\t");
                mgfOut.append(hit.peptide.toString());
                mgfOut.append("\t");
                mgfOut.append(String.format("%10.1f", hit.hypderscore));
                mgfOut.append("\n");
         }

            mgfOut.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

}
