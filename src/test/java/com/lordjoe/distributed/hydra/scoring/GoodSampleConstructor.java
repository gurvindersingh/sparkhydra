package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.hydra.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.*;
import org.systemsbiology.xtandem.fdr.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.taxonomy.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.GoodSampleConstructor
 * User: Steve
 * Date: 1/24/2015
 */
public class GoodSampleConstructor {



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
        FileInputStream is = new FileInputStream(pFastaFileName);

        SeekingFastaHandler handler = new SeekingFastaHandler(pPeptides, pProteins);
        FastaParser fp = new FastaParser();
        fp.addHandler(handler);
        fp.parseFastaFile(is, pFastaFileName);
        handler.showHandled();
        if (pPeptides.size() > 0) {
            throw new UnsupportedOperationException("There are " + pPeptides.size() + " Unfound peptides");
        }
    }



    public static void writeSelectedSpectra(final String mgfFileName, final String pOutMgfName, final Set<String> pSpectralIds) throws IOException {
        PrintWriter mgfOut = new PrintWriter(new FileWriter(pOutMgfName));
        InputStream is = new FileInputStream(mgfFileName);
        MassSpecRun[] runs = XTandemUtilities.parseMgfFile(is, mgfFileName);
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
            handleProtein( annotation, sequence,  "");
        }


        @Override
        public void handleProtein(final String annotation, final String sequence,String url) {
            Set<String> removed = new HashSet<String>();
            for (String peptide : m_peptides) {
                if (sequence.contains(peptide)) {
                    Protein protein = Protein.getProtein(annotation, annotation, sequence, url);
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



    public static void samplePepXML(final String[] args) throws IOException {
        int index = 0;
        String inFileName = args[index++];
        String inFileBase = inFileName.substring(0, inFileName.length() - ".xml".length());
        int keepBest = Integer.parseInt(args[index++]);

        String mgfFileName = args[index++];
        String fastaFileName = args[index++];
        String outMgfName = inFileBase + "selectedMGF" + keepBest + ".mgf";
        String outProteinName = inFileBase + "selectedProtein" + keepBest + ".fasta";

        ProteinPepxmlParser pp = readOnePepXML(inFileName);



        List<ProteinPepxmlParser.SpectrumHit> sortedHits = pp.getAllHits();

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

    public static void sampleTandemOutput(final String[] args) throws IOException {
        int index = 0;
        String inFileName = args[index++];
        XTandemScoringReport report1 = XTandemUtilities.readXTandemFile(inFileName);


        String inFileBase = inFileName.substring(0, inFileName.length() - ".xml".length());
        int keepBest = Integer.parseInt(args[index++]);

        String mgfFileName = args[index++];
        String fastaFileName = args[index++];
        String outMgfName = inFileBase + "selectedMGF" + keepBest + ".mgf";
        String outProteinName = inFileBase + "selectedProtein" + keepBest + ".fasta";

        List<ScoredScan> scans = Arrays.asList(report1.getScans());

        Collections.sort(scans, new ScoredScanComparator());

        scans = scans.subList(0, Math.min(scans.size(), keepBest));

        showBestScans(scans, keepBest);

        Set<String> peptides = new HashSet<String>();
        Set<String> spectralIds = new HashSet<String>();
        Set<IProtein> proteins = new HashSet<IProtein>();
        Map<String, IMeasuredSpectrum> spectra = new HashMap<String, IMeasuredSpectrum>();

        for (ScoredScan sortedHit : scans) {
            ISpectralMatch bestMatch = sortedHit.getBestMatch();
            if(bestMatch == null)
                continue;
            String id= ((RawPeptideScan)sortedHit.getRaw()).getLabel();
            IMeasuredSpectrum measured = bestMatch.getMeasured();
            spectralIds.add(id);
            IPolypeptide peptide = bestMatch.getPeptide();
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
    private static void showBestScans(final List<ScoredScan> hits, int number) {
        try {
            PrintWriter mgfOut = new PrintWriter(new FileWriter("FoundHits" + number + ".txt"));
            for (ScoredScan hit : hits) {
                ISpectralMatch bestMatch = hit.getBestMatch();
                mgfOut.append(hit.getId());
                mgfOut.append("\t");
                mgfOut.append(bestMatch.getPeptide().toString());
                mgfOut.append("\t");
                mgfOut.append(String.format("%10.1f", bestMatch.getHyperScore()));
                mgfOut.append("\n");
            }

            mgfOut.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static List<String>   readPeptidesFromPepXML(String file)
    {
        List<String> holder = new ArrayList<String>();

         try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(file)) ;
            String line = rdr.readLine();
            while(line != null)  {
                int index = line.indexOf("\" peptide=\"");
                if(index > 0)   {
                   index +=  "\" peptide=\"".length();
                    int index2 = line.indexOf("\"",index) ;
                    String sequence = line.substring(index,index2);
                    holder.add( sequence);
                }

                line = rdr.readLine();
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
        return holder;
    }

    public static void getProteins(String[] args)  {
        try {
            int index = 0;
            String fastaFileName = args[index++];
            String outProteinName = args[index++];

            Set<IProtein> proteins = new HashSet<IProtein>();
            Set<String> peptides = new HashSet<String>();


            for (; index < args.length; index++) {
                List<String> pps =  readPeptidesFromPepXML(args[index]);
                peptides.addAll(pps);

            }
            peptidesToProteins(fastaFileName, peptides, proteins);
            writeProteins(outProteinName, proteins);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        getProteins(  args);
 //        samplePepXML(args);
  //      sampleTandemOutput(args);
     }


}
