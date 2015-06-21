package org.systemsbiology.xtandem.pepxml;

import com.lordjoe.utilities.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.pepxml.PepXMLWriter
 * User: Steve
 * Date: 1/26/12
 */
public class PepXMLWriter implements Serializable {
    public static final PepXMLWriter[] EMPTY_ARRAY = {};

    private static   int gMatchesToPrint = OriginatingScoredScan.MAX_SERIALIZED_MATCHED;

    public static int getMatchesToPrint() {
        return gMatchesToPrint;
    }

    public static void setMatchesToPrint(final int matchesToPrint) {
        gMatchesToPrint = matchesToPrint;
    }

    private final IMainData m_Application;
    private String m_Path = "";

    public PepXMLWriter(final IMainData pApplication) {
        m_Application = pApplication;
    }

    public IMainData getApplication() {
        return m_Application;
    }

    public String getPath() {
        return m_Path;
    }

    public void setPath( String pPath) {
        int extIndex = pPath.lastIndexOf(".");
        if(extIndex > 1)
             pPath = pPath.substring(0,extIndex);
        m_Path = pPath;
    }

    public void writePepXML(IScoredScan scan, File out) {
        OutputStream os = null;
        try {
            System.setProperty("line.separator","\n"); // linux style cr
            os = new FileOutputStream(out);
            writePepXML(scan, out.getAbsolutePath().replace("\\", "/"), os);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    public void writePepXML(IScoredScan scan, String path, OutputStream out) {

        try {
            PrintWriter pw = new PrintWriter(out);
            setPath(path);
            writePepXML(scan, pw);
        }
        finally {
            try {
                out.close();
            }
            catch (IOException e) {   // ignore
            }
        }

    }

    public void writePepXML(IScoredScan scan, Appendable out) {
        try {
            writeSummaries(scan, out);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    public static final String PEPXML_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<?xml-stylesheet type=\"text/xsl\" href=\"pepXML_std.xsl\"?>\n";
//                    "<msms_pipeline_analysis date=\"%DATE%\"" +
//                    " summary_xml=\"%PATH%\"" +
//                    " xmlns=\"http://regis-web.systemsbiology.net/pepXML\"" +
//                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
//                    " xsi:schemaLocation=\"http://sashimi.sourceforge.net/schema_revision/pepXML/pepXML_v117.xsd\"" +
//                    ">";

    public static final String TTRYPSIN_XML =
            "      <sample_enzyme name=\"trypsin\">\n" +
                    "         <specificity cut=\"KR\" no_cut=\"P\" sense=\"C\"/>\n" +
                    "      </sample_enzyme>";

    public static final String ANALYSIS_HEADER =
            "<msms_pipeline_analysis date=\"%DATE%\"  " +
                    //             "summary_xml=\"c:\\Inetpub\\wwwroot\\ISB\\data\\HighResMS2\\c:/Inetpub/wwwroot/ISB/data/HighResMS2/noHK-centroid.tandem.pep.xml\" " +
                    "xmlns=\"http://regis-web.systemsbiology.net/pepXML\"" +
                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                    " xsi:schemaLocation=\"http://sashimi.sourceforge.net/schema_revision/pepXML/pepXML_v115.xsd\">\n" +
                    "   <msms_run_summary base_name=\"%PATH%\" search_engine=\"Hydra(%ALGO%)\" msManufacturer=\"Thermo Scientific\" msModel=\"LTQ\" msIonization=\"NSI\" msMassAnalyzer=\"ITMS\" msDetector=\"unknown\" raw_data_type=\"raw\" raw_data=\".mzXML\">" +
                    "";


    public static final String SEARCH_SUMMARY_TEXT =
            "<search_summary base_name=\"%FULL_FILE_PATH%\" search_engine=\"Hydra(%ALGO%)\" precursor_mass_type=\"monoisotopic\" fragment_mass_type=\"monoisotopic\" search_id=\"1\">";


    public void writePepXMLHeader(String path,String algo, Appendable out)    {
        try {
            String now = XTandemUtilities.xTandemNow();
            String header = PEPXML_HEADER.replace("%PATH%", path);
            header = header.replace("%DATE%", now);
            out.append(header);
            out.append("\n");

            header = ANALYSIS_HEADER.replace("%PATH%", path);
            header = header.replace("%DATE%", now);
            header = header.replace("%ALGO%", algo);
            out.append(header);
            out.append("\n");

            out.append(TTRYPSIN_XML);  // tod stop hard coding
            out.append("\n");

            String ss = SEARCH_SUMMARY_TEXT.replace("%FULL_FILE_PATH%",path);
            ss = ss.replace("%ALGO%", algo);
            out.append(ss);  // tod stop hard coding
            out.append("\n");

            showDatabase(out);
            showEnzyme(out);
            showModifications(out);
            showParameters(out);
            out.append("      </search_summary>");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public String getPepXMLHeader()    {
        String path = getApplication().getParameter("spectrum, path");
        ITandemScoringAlgorithm[] algorithms = getApplication().getAlgorithms();
        String algo = "";
        if (algorithms.length > 0)
            algo = algorithms[0].getName();
        String out;
        String now = XTandemUtilities.xTandemNow();
        String header = PEPXML_HEADER.replace("%PATH%", path);
        header = header.replace("%DATE%", now);
        out = header;
        out += "\n";

        header = ANALYSIS_HEADER.replace("%PATH%", path);
        header = header.replace("%DATE%", now);
        header = header.replace("%ALGO%", algo);
        out += header;
        out += "\n";

        out += TTRYPSIN_XML;  // tod stop hard coding
        out += "\n";

        String ss = SEARCH_SUMMARY_TEXT.replace("%FULL_FILE_PATH%",path);
        ss = ss.replace("%ALGO%", algo);
        out += ss;  // tod stop hard coding
        out += "\n";

        out += showDatabase();
        out += showEnzyme();
        out += showModifications();
        out += showParameters();
        out += "      </search_summary>";
        out += "\n";
        return out;
    }
    protected void showModifications(Appendable out)  throws IOException  {
        IMainData application = getApplication();
        ScoringModifications scoringMods = application.getScoringMods();
        PeptideModification[] modifications = scoringMods.getModifications();
        Arrays.sort(modifications);
        for(PeptideModification pm : modifications)     {
            showModification(pm,out);
        }
        PeptideModification.getTerminalModifications();
        Arrays.sort(modifications);
        for(PeptideModification pm : PeptideModification.getTerminalModifications())     {
             showModification(pm,out);
         }
    }

    protected String showModifications() {
        String out = "";
        IMainData application = getApplication();
        ScoringModifications scoringMods = application.getScoringMods();
        PeptideModification[] modifications = scoringMods.getModifications();
        Arrays.sort(modifications);
        for(PeptideModification pm : modifications)     {
            out += showModification(pm);
        }
        PeptideModification.getTerminalModifications();
        Arrays.sort(modifications);
        for(PeptideModification pm : PeptideModification.getTerminalModifications())     {
             out += showModification(pm);
         }
        return out;
    }

    /*
       <aminoacid_modification aminoacid="C" massdiff="57.0215" mass="160.0306" variable="N" />
         <aminoacid_modification aminoacid="C" massdiff="-17.0265" mass="143.0041" variable="Y" symbol="^" /><!--X! Tandem n-terminal AA variable modification-->
         <aminoacid_modification aminoacid="E" massdiff="2-18.0106" mass="111.0320" variable="Y" symbol="^" /><!--X! Tandem n-terminal AA variable modification-->
         <aminoacid_modification aminoacid="K" massdiff="8.0142" mass="136.1092" variable="Y" />
         <aminoacid_modification aminoacid="M" massdiff="15.9949" mass="147.0354" variable="Y" />
         <aminoacid_modification aminoacid="Q" massdiff="-17.0265" mass="111.0321" variable="Y" symbol="^" /><!--X! Tandem n-terminal AA variable modification-->
         <aminoacid_modification aminoacid="R" massdiff="10.0083" mass="166.1094" variable="Y" />

     */
    protected void showModification(PeptideModification pm, Appendable out)  throws IOException  {
        double massChange = pm.getMassChange();
        double pepideMass = pm.getPepideMass();
        String variable = "Y";
        if(pm.isFixed())
            variable = "N";

        out.append(" <aminoacid_modification");
        out.append(" aminoacid=\"" +  pm.getAminoAcid() + "\"");
        out.append(" massdiff=\"" + String.format("%10.4f",massChange).trim() + "\"");
        out.append(" mass=\"" + String.format("%10.4f",pepideMass).trim() + "\"");
        out.append("variable=\"" + variable + "\"");
         out.append(" />");
        if(pm.getRestriction() == PeptideModificationRestriction.CTerminal)
              out.append("<!--X! Tandem c-terminal AA variable modification-->");
        if(pm.getRestriction() == PeptideModificationRestriction.NTerminal)
              out.append("<!--X! Tandem n-terminal AA variable modification-->");

          out.append("\n");
    }

    protected String showModification(PeptideModification pm)  {
        String out = "";
        double massChange = pm.getMassChange();
        double pepideMass = pm.getPepideMass();
        String variable = "Y";
        if(pm.isFixed())
            variable = "N";

        out += " <aminoacid_modification";
        out += " aminoacid=\"" +  pm.getAminoAcid() + "\"";
        out += " massdiff=\"" + String.format("%10.4f",massChange).trim() + "\"";
        out += " mass=\"" + String.format("%10.4f",pepideMass).trim() + "\"";
        out += "variable=\"" + variable + "\"";
        out += " />";
        if(pm.getRestriction() == PeptideModificationRestriction.CTerminal)
              out += "<!--X! Tandem c-terminal AA variable modification-->";
        if(pm.getRestriction() == PeptideModificationRestriction.NTerminal)
              out += "<!--X! Tandem n-terminal AA variable modification-->";

        out += "\n";
        return out;
    }


    protected void showEnzyme(Appendable out)  throws IOException  {
        IMainData application = getApplication();

        out.append("        <enzymatic_search_constraint enzyme=\"trypsin\" max_num_internal_cleavages=\"" +
                        application.getDigester().getNumberMissedCleavages() +
                        "\" />"
        );
        out.append("\n");
    }

    protected String showEnzyme()  {
        String out = "";
        IMainData application = getApplication();

        out += "        <enzymatic_search_constraint enzyme=\"trypsin\" max_num_internal_cleavages=\"" +
                        application.getDigester().getNumberMissedCleavages() +
                        "\" />";
        out += "\n";
        return out;
    }

    protected void showDatabase(Appendable out)  throws IOException  {
        IMainData application = getApplication();
        String[] parameterKeys = application.getParameterKeys();
        out.append("         <search_database local_path=\"" +
                        application.getDatabaseName() +
                        "\" type=\"AA\"" +
                        " />"
        );
        out.append("\n");
    }

    protected String showDatabase() {
        String out = "";
        IMainData application = getApplication();
        String[] parameterKeys = application.getParameterKeys();
        out += "         <search_database local_path=\"" +
                        application.getDatabaseName() +
                        "\" type=\"AA\"" +
                        " />";
        out += "\n";
        return out;
    }

    protected void showParameters(Appendable out)  throws IOException  {
        IMainData application = getApplication();
        String[] parameterKeys = application.getParameterKeys();
        out.append("        <!-- Input parameters -->");
        out.append("\n");
         for (int i = 0; i < parameterKeys.length; i++) {
            String parameterKey = parameterKeys[i];
            String value = application.getParameter(parameterKey);
            out.append("        <parameter name=\"" +
                            parameterKey + "\"" +
                            " value=\"" +
                            value +
                            "\" />"
            );
            out.append("\n");
        }
    }

    protected String showParameters()  {
        String out = "";
        IMainData application = getApplication();
        String[] parameterKeys = application.getParameterKeys();
        out += "        <!-- Input parameters -->";
        out += "\n";
         for (int i = 0; i < parameterKeys.length; i++) {
            String parameterKey = parameterKeys[i];
            String value = application.getParameter(parameterKey);
            out += "        <parameter name=\"" +
                            parameterKey + "\"" +
                            " value=\"" +
                            value +
                            "\" />";
            out += "\n";
        }
        return out;
    }

    public void writePepXMLFooter(Appendable out)   {
        try {
            out.append("     </msms_run_summary>");
            out.append("\n");
            out.append("</msms_pipeline_analysis>");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public String getPepXMLFooter()   {
        String out =  "     </msms_run_summary>";
        out += "\n";
        out += "</msms_pipeline_analysis>";
        out += "\n";
        return out;
    }

    protected void writeSummaries(IScoredScan scan, Appendable out) throws IOException {

        String algorithm = scan.getAlgorithm();
        ISpectralMatch[] spectralMatches = scan.getSpectralMatches();
        if (!"KScore".equals(algorithm))
            XTandemUtilities.breakHere();

        if (spectralMatches.length == 0)
            return;
        writeScanHeader(scan, out);
        switch (spectralMatches.length) {
            case 0:
                return;
            case 1:
                printMatch(scan, spectralMatches[0], null,  out);
                break;

            default:
                printLimitedMatches(scan, out, spectralMatches, getMatchesToPrint() );
        }
        out.append("      </spectrum_query>");
        out.append("\n");
    }

    private void printLimitedMatches(final IScoredScan scan, final Appendable out, final ISpectralMatch[] pSpectralMatches, int matchesToPrint) throws IOException {
        out.append("         <search_result>");
        out.append("\n");
        for (int i = 0; i < Math.min(matchesToPrint, pSpectralMatches.length); i++) {
            ISpectralMatch match = pSpectralMatches[i];
            int rank = i + 1;
            ISpectralMatch next = null;
            if (i < pSpectralMatches.length - 2)
                next = pSpectralMatches[i + 1];
            internalPrintMatch(scan, match, next, rank, out);
        }
          out.append("         </search_result>");
        out.append("\n");
    }

    protected void writeScanHeader(IScoredScan scan, Appendable out) throws IOException {
        String id = scan.getId();
        String idString = XTandemUtilities.asAlphabeticalId(id);   // will be safew for parsing as int
        int charge = scan.getCharge();

        out.append("      <spectrum_query ");
        double precursorMass = scan.getRaw().getPrecursorMass() - XTandemUtilities.getProtonMass();
        String path = getPath().replace("\\","/");
        int dirIndex = path.lastIndexOf("/");
        if(dirIndex > -1)
            path = path.substring(dirIndex + 1);
         out.append(" spectrum=\"" + path +  "." + idString +"." + idString + "." + charge + "\"");
        out.append(" start_scan=\""   + id + "\" end_scan=\""   + id + "\" ");
        out.append(" precursor_neutral_mass=\"" + String.format("%10.4f", precursorMass).trim() + "\"");
        double rt = scan.getRetentionTime();
        if(rt != 0)
            out.append(" retention_time_sec=\"" + String.format("%10.3f",rt).trim() + "\"");
        out.append(" assumed_charge=\"" + scan.getCharge() + "\"");
        out.append(" >");
        out.append("\n");
    }


    protected void printMatch(IScoredScan scan, ISpectralMatch match, ISpectralMatch nextmatch,  Appendable out) throws IOException {
        out.append("         <search_result>");
        out.append("\n");
        internalPrintMatch(scan, match, nextmatch, 1, out);
        out.append("         </search_result>");
        out.append("\n");
     }

    private void internalPrintMatch(IScoredScan scan, ISpectralMatch match, ISpectralMatch nextmatch, int hitNum, Appendable out) throws IOException {
        out.append("            <search_hit hit_rank=\"" +
                hitNum +
                "\" peptide=\"");
        IPolypeptide peptide = match.getPeptide();
        out.append(peptide.getSequence());
        out.append("\"");
        IMeasuredSpectrum conditionedScan = scan.getConditionedScan();
        int totalPeaks = conditionedScan.getPeaksCount();
        IMeasuredSpectrum raw = scan.getRaw();
        double precursorMass = raw.getPrecursorMass(); // raw.getPrecursorMass(scan.getCharge());
        double pepMass = peptide.getMatchingMass();
        double delMass = precursorMass - pepMass;
        int numberMatchedPeaks = match.getNumberMatchedPeaks();
        //     out.append(" peptide_prev_aa=\"K\" ");
        //     out.append("peptide_next_aa=\"I\" " );
        // Let Refresh parser analyze for now
        IProteinPosition[] proteinPositions = peptide.getProteinPositions();
        if (proteinPositions.length > 0) {
            IProteinPosition pp = proteinPositions[0];
            showProteinPosition( pp,out);
        }
        out.append("");
        out.append(" num_tot_proteins=\"" + proteinPositions.length + "\" ");

        out.append(" num_matched_ions=\"" + numberMatchedPeaks + "\"");
        out.append(" tot_num_ions=\"" + totalPeaks + "\"");
        out.append(" calc_neutral_pep_mass=\"" + totalPeaks + "\" ");
        out.append(" massdiff=\"" + String.format("%10.4f", delMass).trim() + "\" ");
        ////      out.append("num_tol_term=\"2\" ");
        int missed_cleavages = peptide.getMissedCleavages();
        out.append("num_missed_cleavages=\"" + missed_cleavages + "\" ");
        //     out.append("is_rejected=\"0\">\n");
        out.append(" >");
        out.append("\n");

        for (int i = 1; i < proteinPositions.length; i++) {
            showAlternateiveProtein(proteinPositions[i], out);

        }

        if (peptide.isModified()) {
            showModificationInfo((IModifiedPeptide) peptide, out);

        }

        double value = 0;
        value = match.getHyperScore();
        out.append("             <search_score name=\"hyperscore\" value=\"" +
                String.format("%10.4f", value).trim() + "\"/>");
        out.append("\n");

        if (nextmatch != null) {
            value = nextmatch.getHyperScore();
            out.append("             <search_score name=\"nextscore\" value=\"" +
                    String.format("%10.4f", value).trim() + "\"/>");
            out.append("\n");
        }
        double bvalue = match.getScore(IonType.B);
        out.append("             <search_score name=\"bscore\" value=\"" +
                String.format("%10.4f", bvalue).trim() + "\"/>");
        out.append("\n");

        double yvalue = match.getScore(IonType.Y);
        out.append("             <search_score name=\"yscore\" value=\"" +
                String.format("%10.4f", yvalue).trim() + "\"/>");
        out.append("\n");

        HyperScoreStatistics hyperScores = scan.getHyperScores();
        double expected = hyperScores.getExpectedValue(match.getScore());
        out.append("             <search_score name=\"expect\" value=\"" +
                String.format("%10.4f", expected).trim() + "\"/>");
        out.append("\n");
        out.append("              </search_hit>");
        out.append("\n");
    }

    private void showProteinPosition( final IProteinPosition pPp,final Appendable out)  throws IOException {
        out.append(" protein=\"" + getId(pPp.getProtein()) + "\"");
        out.append("\n");
        out.append("                       protein_descr=\"" + Util.xmlEscape(pPp.getProtein()) + "\"");
        out.append("\n");
        out.append("                        ");
          FastaAminoAcid before = pPp.getBefore();
        if (before != null)
            out.append(" peptide_prev_aa=\"" + before + "\"");
        else
            out.append(" peptide_prev_aa=\"-\"");
        FastaAminoAcid after = pPp.getAfter();
        if (after != null)
            out.append(" peptide_next_aa=\"" + after + "\"");
        else
             out.append(" peptide_next_aa=\"-\"");
         out.append("\n");
        out.append("                                     ");
    }

    protected void showModificationInfo(final IModifiedPeptide peptide, final Appendable out)  throws IOException {
        String totalModifiedSequence = peptide.getModifiedSequence();
        out.append("             <modification_info modified_peptide=\"" + totalModifiedSequence + "\" >");
        out.append("\n");
        PeptideModification[] modifications = peptide.getModifications();
        for (int i = 0; i < modifications.length; i++) {
            PeptideModification modification = modifications[i];
            if (modification != null) {
                showModification(modification, i, out);
            }
        }
        out.append("             </modification_info>");
        out.append("\n");

    }

    protected void showModification(final PeptideModification pModification, final int index, final Appendable out)  throws IOException {
        out.append("             <mod_aminoacid_mass position=\"" + (index + 1) + "\" mass=\"" + String.format("%10.3f", pModification.getPepideMass()).trim() + "\" />");
        out.append("\n");
    }

    protected void showAlternateiveProtein(final IProteinPosition pp, final Appendable out)   throws IOException  {
        out.append("             <alternative_protein ");
        showProteinPosition(pp, out);
        out.append(" />");
        out.append("\n");
    }

    public static String getId(String descr)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < descr.length(); i++) {
             char ch = descr.charAt(i);
            if(ch == ' ')
                break;
             if(Character.isJavaIdentifierPart(ch))
                 sb.append(ch);

         }

        return sb.toString();

    }


}
