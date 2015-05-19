package com.lordjoe.distributed.hydra.pepxml;

import com.lordjoe.lib.xml.XMLUtil;
import org.systemsbiology.xtandem.IEquivalent;
import org.systemsbiology.xtandem.fdr.IDiscoveryDataHolder;
import org.systemsbiology.xtandem.fdr.ISpectrumDataFilter;
import org.systemsbiology.xtandem.fdr.SpectrumData;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;


/**
 * com.lordjoe.distributed.hydra.pepxml.PepxmlParser
 * This class parses a PepXML file and returns a structure suitable for
 * output comparisin - it is [primarily used for testing
 *
 * @author slewis
 * @date 09/05/13
 */
public class PepxmlParser implements IEquivalent<PepxmlParser> {

    private final Map<String, SpectrumQuery> queries = new HashMap<String, SpectrumQuery>();

    private String scan_id;

    public PepxmlParser(String filename) {
        this(new File(filename));


    }

    public PepxmlParser(File file) {
        processFile(file);
    }



    @Override
    public boolean equivalent(PepxmlParser o) {
        if(queries.size() !=  o.queries.size())
            return false;
        for (String s : queries.keySet()) {
            SpectrumQuery query1 = queries.get(s);
            SpectrumQuery query2 = o.queries.get(s);
            if(query2 == null)
                return false;
            if(!query1.equivalent(query2)) {
               return query1.equivalent(query2);
            }
        }

        return true;
    }
    /**
     *
     */
    public void processFile(File file) {
        @SuppressWarnings("UnusedDeclaration")
        int numberProcessed = 0;
        @SuppressWarnings("UnusedDeclaration")
        SpectrumQuery currentQuery = null;
        double lastRetentionTime = 0;

        @SuppressWarnings("UnusedDeclaration")
        int numberUnProcessed = 0;
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
            String line = rdr.readLine();
            while (line != null) {
                if (line.contains("<spectrum_query")) {
                    String spectrum = XMLUtil.extractAttribute(line, "spectrum");
                    double retention_time_sec = XMLUtil.extractDoubleValue("retention_time_sec", line);
                    double mass = XMLUtil.extractDoubleValue("precursor_neutral_mass", line);
                    int charge = XMLUtil.extractIntegerValue("assumed_charge", line);
                    scan_id = XMLUtil.extractAttribute(line, "start_scan");
                    currentQuery = new SpectrumQuery(spectrum,
                            mass,
                            charge,
                            retention_time_sec
                    );
                }
                if (line.contains("</spectrum_query>")) {
                    queries.put(currentQuery.spectrum, currentQuery);
                    currentQuery = null;
                }


                //noinspection StatementWithEmptyBody,StatementWithEmptyBody
                if (line.contains("<search_result")) {
                    String[] searchHitLines = readSearchHitLines(line, rdr);
                    //              System.out.println(line);
                    handleSearchHit(searchHitLines, currentQuery);
                }
                line = rdr.readLine();

            }

            //noinspection UnnecessaryReturnStatement
            return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected String[] readSearchHitLines(String line, LineNumberReader rdr, @SuppressWarnings("UnusedParameters") ISpectrumDataFilter... filters) {
        List<String> holder = new ArrayList<String>();

        try {
            while (line != null) {
                holder.add(line);
                if (line.contains("</search_result")) {
                    break; // done
                }
                line = rdr.readLine();  // read next line

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    @SuppressWarnings({"UnusedParameters", "UnusedAssignment"})
    protected void handleSearchHit(String[] lines, SpectrumQuery query) {
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        Double expectedValue = null;
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        Double hyperScoreValue = null;
        int index = 0;
        String line = lines[index++];   // handle first line
        while (!line.contains("<search_hit")) {
            line = lines[index++];
            if (index >= lines.length)
                return;
        }
        String id = scan_id;
        List<PositionModification> modifications = new ArrayList<PositionModification>();

        if ("".equals(id))
            throw new UnsupportedOperationException("Fix This"); // ToDo

        boolean trueHit = !line.contains("protein=\"DECOY_");
        boolean processSpectrum = parseHitValue(line) <= 2;
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        boolean isUnique = true;
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        boolean isModified = false;
        int rank = parseHitValue(line);

        IdentifiedPSM peptide = processPeptide(line, query.retentionTime, id);

        String proteinId = parseQuotedValue(line, "protein");


        for (; index < lines.length; index++) {
            line = lines[index];

            if (line.contains("</search_hit"))
                break;         // we are done

            if (line.contains("</modification_info>")) {
                peptide = buildFromModification(peptide, modifications);

            }

            if (line.contains(" modified_peptide="))
                peptide = processModifiedPeptide(line, query.retentionTime, id);

            if (line.contains("<mod_aminoacid_mass ")) {
                double delMass = parseNamedValue(line, "mass");
                int position = parsePositionValue(line);
                modifications.add(new PositionModification(position, delMass));
            }

            if (line.contains("<alternative_protein")) {
                isUnique = false;
            }

            if (line.contains("<search_score name=\"xcorr\" value=\"")) {
                hyperScoreValue = parseValue(line);
            }
            if (line.contains("<search_score name=\"expect\" value=\"")) {
                expectedValue = parseValue(line);
            }
            if (line.contains("protein=\"DECOY_")) {  // another protein
                if (trueHit)
                    processSpectrum = false; // one decoy one not
            }
            if (line.contains("<alternative_protein")) {  // another protein
                if (!trueHit && !line.contains("protein=\"DECOY_")) // we start as decoy and fit to a real
                    processSpectrum = false; // one decoy one not
            }

            if (line.contains("protein_descr=\"")) {
                //    protein = processProtein(line);

            }
        }

        IPolypeptide peptide1 = peptide.getPeptide();
        SpectrumHit hit = new SpectrumHit(id, hyperScoreValue, rank, peptide1);
        query.addSpectrumHit(hit);


    }

    public static IdentifiedPSM buildFromModification(IdentifiedPSM peptide, List<PositionModification> modifications) {
        Polypeptide unmodified = (Polypeptide) peptide.getPeptide();
        String id = unmodified.getId();
        String s = unmodified.getSequence();
        int charNumber = 0;
        StringBuilder sequence = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sequence.append(c);
            charNumber++;
            PositionModification current = null;
            for (PositionModification modification : modifications) {
                if (modification.position == charNumber) {
                    current = modification;
                    break;
                }
            }
            if (current != null) {
                modifications.remove(current);
                sequence.append(current.toModString());
            }
        }

        return processPeptide(sequence.toString(), unmodified.getRetentionTime(), id);
    }

    public static IdentifiedPSM processPeptide(final String line, double retentionTime, String id) {
        String peptide = XMLUtil.extractAttribute(line, "peptide");
        if (peptide == null)
            throw new IllegalArgumentException("bad line " + line);
        Polypeptide polypeptide = Polypeptide.fromString(peptide);
        polypeptide.setRetentionTime(retentionTime);
        return new IdentifiedPSM(id, polypeptide);
    }

    public static IdentifiedPSM processModifiedPeptide(final String line, double retentionTime, String id) {
        String peptide = XMLUtil.extractAttribute(line, "modified_peptide");
        if (peptide == null)
            throw new IllegalArgumentException("bad line " + line);
        Polypeptide polypeptide = Polypeptide.fromString(peptide);
        polypeptide.setRetentionTime(retentionTime);
        return new IdentifiedPSM(id, polypeptide);
    }

    public static IProtein processProtein(final String line) {
        String peptide = XMLUtil.extractAttribute(line, "protein_descr");
        if (peptide == null)
            throw new IllegalArgumentException("bad line " + line);
        return Protein.getProtein(peptide, "", "", null);
    }


    public static double parseValue(String line) {
        return parseNamedValue(line, "value");
    }

    public static double parseNamedValue(String line, String name) {
        String s = parseQuotedValue(line, name);
        if (s.length() == 0)
            return 0;
        return Double.parseDouble(s);
    }

    public static boolean parseIsModifiedValue(String line) {
        String s = parseQuotedValue(line, "peptide");
        //noinspection SimplifiableIfStatement
        if (s.length() == 0)
            return false;
        return s.contains("[");    // modification string
    }

    public static int parseHitValue(String line) {
        String s = parseQuotedValue(line, "hit_rank");
        if (s.length() == 0)
            return 0;
        return Integer.parseInt(s);
    }

    public static int parsePositionValue(String line) {
        String s = parseQuotedValue(line, "position");
        if (s.length() == 0)
            return 0;
        return Integer.parseInt(s);
    }

    /**
     * return a section of
     *
     * @param line
     * @param start
     * @return
     */
    public static String parseQuotedValue(String line, String start) {
        final String str = start + "=\"";
        int index = line.indexOf(str);
        if (index == -1)
            return "";
        index += str.length();
        int endIndex = line.indexOf("\"", index);
        if (endIndex == -1)
            return "";
        return line.substring(index, endIndex);
    }


    public static void main(String[] args) throws Exception {
        List<PepxmlParser> items = new ArrayList<PepxmlParser>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            PepxmlParser pp = new PepxmlParser(new File(arg));
            items.add(pp);
        }

        if(args.length > 1)    {
            PepxmlParser pp1 = items.get(0);
            PepxmlParser pp2 = items.get(1);
            if(!pp1.equivalent(pp2)) {
                pp1.equivalent(pp2);
                throw new IllegalStateException("problem"); // todo fix

            }
            System.out.println("Files are the same");
        }

    }


}
