package org.systemsbiology.xtandem.reporting;

import com.lordjoe.utilities.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.reporting.BiomlReporter
 *
 * @author Steve Lewis
 * @date Jan 11, 2011
 * Class responsible for writing a report -
 * similar responsibilities to mreport in the C++ version
 */
public class BiomlReporter implements Serializable {

    public static final String[] PERFORMANCE_PARAMETER_KEYS =
            {
                    "list path, sequence source #1",   //yeast_orfs_pruned.fasta</note>
                    "list path, sequence source description #1",   //no description</note>
                    "modelling, spectrum noise suppression ratio",   //0.00</note>
                    "modelling, total peptides used",   //350</note>
                    "modelling, total proteins used",   //10</note>
                    "modelling, total spectra used",   //8</note>
                    "process, start time",   //2011:06:20:11:36:25</note>
                    "process, version",   //x! tandem 2010.10.01.1 (LabKey, Insilicos and ISB)</note>
                    "quality values",   //2 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0</note>
                    "refining, # input models",   //0</note>
                    "refining, # input spectra",   //0</note>
                    "refining, # partial cleavage",   //0</note>
                    "refining, # point mutations",   //0</note>
                    "refining, # potential C-terminii",   //0</note>
                    "refining, # potential N-terminii",   //0</note>
                    "refining, # unanticipated cleavage",   //0</note>
                    "timing, initial modelling total (sec)",   //0.68</note>
                    "timing, initial modelling/spectrum (sec)",   //0.085</note>
                    "timing, load sequence models (sec)",   //0.00</note>
                    "timing, refinement/spectrum (sec)",   //0.000</note>

            };
    public static final String FORCED_OUTPUT_NAME_PARAMETER = "org.systemsbiology.xtandem.outputfile";

    public static final String MODEL_LONE_STRING = "MYG_HORSE MYOGLOBIN - Equus caballus (Horse), and Equus burchelli (Plains zebra)";
    public static final int TOO_LONG = MODEL_LONE_STRING.length() + 1;

    public static String truncateString(String s) {
        if (s.length() < TOO_LONG)
            return s;
        return s.substring(0, TOO_LONG) + "...";
    }

    public static String buildDefaultFileName(IParameterHolder pParameters) {
        // force output name so it is known to hadoop caller
        String absoluteName = pParameters.getParameter(FORCED_OUTPUT_NAME_PARAMETER);
        if (absoluteName != null)
            return absoluteName;

        String name = pParameters.getParameter("output, path");
        // little hack to separate real tandem and hydra results
        if (name != null)
            name = name.replace(".tandem.xml", ".hydra.xml");
        if ("full_tandem_output_path".equals(name)) {
            return "xtandem_" + getDateHash() + ".xml"; // todo do better
        }

      //  System.err.println("No File name forced building file name");
        if ("yes".equals(pParameters.getParameter("output, path hashing"))) {
            assert name != null;
            final int index = name.lastIndexOf(".");
            String extension = name.substring(index);
            String dataHash = getDateHash();
            name = name.substring(0, index) + "." + dataHash + ".t" + extension;
        }
        return name;
    }

    // make sure that the output date is hashed only once
    private static String gDateHash;

    public static synchronized void clearDateHash() {
        gDateHash = null;
    }

    protected static synchronized String getDateHash() {
        if (gDateHash == null)
            gDateHash = buildDateHash();
        return gDateHash;
    }

    private static String buildDateHash() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_DD_HH_mm_ss");
        return df.format(new Date());
    }

    public static InputStream buildDebugFileStream(IParameterHolder pParameters) {
        String name = pParameters.getParameter("output, path") + "_debug";
        if ("yes".equals(pParameters.getParameter("output, path hashing"))) {
            final int index = name.lastIndexOf(".");
            String extension = name.substring(index);
            SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_DD_HH_mm_ss");
            name = name.substring(0, index) + "." + df.format(new Date()) + ".t" + extension;
        }
        File theFile = new File(name);
        if (!theFile.exists())
            return null;
        try {
            return new FileInputStream(theFile);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    private final IMainData m_Parameters;
    private final OutputStream m_Out;
    private int m_HistogramColumnWidth;
    private boolean m_SpectraShown;
    private boolean m_PerformanceShown;
    private boolean m_HistogramsShown;
    private boolean m_SequencesShown;
    private boolean m_ProteinsShown;
    private boolean m_ParametersShown;
    private boolean m_Compressed;
    private double m_MaxValidExpectedValue;
    private double m_MaxExpectedProteinValue;
    private int m_NumberScoredScans;
    private int m_NumberProteins;
    private int m_NumberPeptidess;

    private String m_OutputResultsType; // todo should this be an enum
    private final IScoredScan[] m_Scans;

    public BiomlReporter(IMainData pParameters, IScoredScan[] scans, File out) throws IOException {
        this(pParameters, scans, new FileOutputStream(out));
    }

    public BiomlReporter(IMainData pParameters, Scorer scores, OutputStream out) {
        this(pParameters, scores.getScans(), out);

    }

    public BiomlReporter(IMainData pParameters, IScoredScan[] scans, OutputStream out) {
        m_Parameters = pParameters;
        setReportParameters();
        m_Out = out;
        m_Scans = scans;
    }

    public BiomlReporter(XTandemMain pParameters, OutputStream out) {
        this(pParameters, pParameters.getScoreRunner(), out);
    }

    public int getNumberScoredScans() {
        return m_NumberScoredScans;
    }

    public void setNumberScoredScans(final int pNumberScoredScans) {
        m_NumberScoredScans = pNumberScoredScans;
    }

    public int getNumberProteins() {
        return m_NumberProteins;
    }

    public void setNumberProteins(final int pNumberProteins) {
        m_NumberProteins = pNumberProteins;
    }

    public int getNumberPeptidess() {
        return m_NumberPeptidess;
    }

    public void setNumberPeptidess(final int pNumberPeptidess) {
        m_NumberPeptidess = pNumberPeptidess;
    }

    public IScoredScan[] getScans() {
        return m_Scans;
    }

    public int getHistogramColumnWidth() {
        return m_HistogramColumnWidth;
    }

    public boolean isSpectraShown() {
        return m_SpectraShown;
    }

    public boolean isPerformanceShown() {
        return m_PerformanceShown;
    }

    public boolean isHistogramsShown() {
        return m_HistogramsShown;
    }

    public boolean isSequencesShown() {
        return m_SequencesShown;
    }

    public boolean isProteinsShown() {
        return m_ProteinsShown;
    }

    /**
     * true if we are showing something in the output report
     *
     * @return
     */
    public boolean isDataShown() {
        return isSpectraShown() || isHistogramsShown() || isSequencesShown();
    }

    public boolean isParametersShown() {
        return m_ParametersShown;
    }

    public boolean isCompressed() {
        return m_Compressed;
    }

    public double getMaxValidExpectedValue() {
        return m_MaxValidExpectedValue;
    }

    public double getMaxExpectedProteinValue() {
        return m_MaxExpectedProteinValue;
    }

    protected void setReportParameters() {
        final IParameterHolder pars = getParameters();
        m_HistogramColumnWidth = pars.getIntParameter("output, histogram column width", 30);

        m_SpectraShown = pars.getBooleanParameter("output, spectra", false);

        m_HistogramsShown = pars.getBooleanParameter("output, histograms", false);

        m_SequencesShown = pars.getBooleanParameter("output, sequences", false);

        m_ProteinsShown = pars.getBooleanParameter("output, proteins", false);

        m_ParametersShown = pars.getBooleanParameter("output, parameters", false);

        m_PerformanceShown = pars.getBooleanParameter("output, performance", false);

        m_Compressed = pars.getBooleanParameter("output, one sequence copy", false);

        m_MaxExpectedProteinValue = pars.getDoubleParameter(
                "output, maximum valid expectation value", 0.01);

        m_MaxExpectedProteinValue = pars.getDoubleParameter(
                "output, maximum valid protein expectation value",
                Math.pow(10, 4));

        m_OutputResultsType = pars.getParameter("output, results", "all");
    }


    public IMainData getParameters() {
        return m_Parameters;
    }

    public void writeReport() {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(m_Out));
        writeHeader(out, 0);

        writeScores(out, 0);

        writeReportEnd(out);
        out.close();
    }

    public void writeReportEnd(PrintWriter out) {
        if (isParametersShown()) {
            writeParameters(out, 0);
            writeUnusedParameters(out, 0);
        }
        if (isPerformanceShown())
            writePerformanceParameters(out, 0);
        writeFooter(out, 0);
    }


    protected void indent(Appendable out, int indent) {
        try {
            for (int i = 0; i < indent; i++) {
                out.append("    ");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }
    }


    public void writeHeader(Appendable out, int indent) {
        try {
            out.append("<?xml version=\"1.0\"?>");
            out.append("\n" );
            IParameterHolder ph = getParameters();
            String xsl = ph.getParameter("output, xsl path");
            if (xsl != null) {
                out.append("<?xml-stylesheet type=\"text/xsl\" href=\"" + xsl + "\"?>");
                out.append("\n" );
            }

            String title = ph.getParameter("output, title");
            if (title != null) {
                out.append(
                        "<bioml xmlns:GAML=\"http://www.bioml.com/gaml/\"   label=\"" + title + "'\">");
                out.append("\n" );
            }
            else {
                title = ph.getParameter("spectrum, path");
                out.append(
                        "<bioml xmlns:GAML=\"http://www.bioml.com/gaml/\"   label=\"models from '" + title + "'\">");
                out.append("\n" );
            }
            // note it is from us
            out.append("<!-- generated by jxtandem -->");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    protected void writeScores(PrintWriter out, int indent) {
        IScoredScan[] scans = getScans();
        for (int i = 0; i < scans.length; i++) {
            IScoredScan scan = scans[i];
            writeScanScores(scan, out, indent);
        }
    }

    public void writeScanScores(IScoredScan scan, Appendable out, int indent) {

        try {
            IMeasuredSpectrum raw = scan.getRaw();
            ISpectralScan scanData = raw.getScanData();
            final ISpectralMatch nextmatch = scan.getNextBestMatch();
            final ISpectralMatch bestMatch = scan.getBestMatch();
            if (bestMatch == null)
                return;
            //   final ITheoreticalSpectrumSet theory = bestMatch.getTheory();
            final IPolypeptide polypeptide = bestMatch.getPeptide();
            //   final IPolypeptide theoryPeptide = theory.getPeptide();
            // note it is from us
            out.append("<!--   sequence=" + polypeptide.toString() + " -->");
            out.append("\n");

            // todo handle m_ParentStream protein
            IProtein parentProtein = null;
            if (isProteinsShown()) {
                IProteinPosition[] proteinPositions = polypeptide.getProteinPositions();
                if (proteinPositions.length > 0) {
                    String annotation = proteinPositions[0].getProtein();
                    String id = proteinPositions[0].getProteinId();
                    parentProtein = Protein.getProtein(id,annotation,"","");
                 }

            }
            indent(out, indent);
            out.append("<group ");
            out.append("id=\"" + raw.getId() + "\" ");
            out.append("mh=\"" + XTandemUtilities.formatDouble(raw.getPrecursorMass(), 6) + "\" ");
            out.append("z=\"" + scan.getCharge() + "\" ");
            out.append("rt=\"" + scanData.getRetentionTime() + "\" ");
            double expected = scan.getExpectedValue();
            out.append("expect=\"" + XTandemUtilities.formatScientific(expected, 1) + "\" ");
            if (parentProtein != null)
                out.append("label=\"" + truncateString(parentProtein.getId()) + "\" ");
            out.append("type=\"model\" ");
            final double factor = scan.getNormalizationFactor();
            final IMeasuredSpectrum spectrum = scan.getNormalizedRawScan();
            //      final double sumPeak = XTandemUtilities.getSumPeaks(spectrum) / factor ;
            //      final double maxPeak = XTandemUtilities.getMaxPeak(spectrum) / factor ;
            double sumIntensity = scan.getSumIntensity();
            double maxIntensity = scan.getMaxIntensity();
            out.append("sumI=\"" + XTandemUtilities.formatDouble(Math.log10(sumIntensity), 2) + "\" ");
            out.append("maxI=\"" + XTandemUtilities.formatDouble(maxIntensity, 1) + "\" ");
            out.append("fI=\"" + XTandemUtilities.formatDouble(maxIntensity / 100, 3) + "\" ");

            out.append(" >");
            out.append("\n");


            indent(out, indent);
            out.append("<protein ");
            out.append("expect=\"" + XTandemUtilities.formatDouble(Math.log10(expected), 1) + "\" ");
            String ident = raw.getId() + ".1";
            out.append("id=\"" + ident + "\" ");
            out.append("uid=\"" + ident + "\" ");  // added slewis to keep tandem parser happy
            if (parentProtein != null) {
                //      out.append("uid=\"" + parentProtein.getUUid() + "\" ");
                out.append("label=\"" + truncateString(parentProtein.getId()) + "\" ");
            }
            out.append("sumI=\"" + XTandemUtilities.formatDouble(Math.log10(sumIntensity), 2) + "\" ");
            out.append(" >");
             out.append("\n");


            if (parentProtein != null) {
                writeDescriptionNote(parentProtein.getAnnotation(), out, indent, true);
            }

            indent(out, indent);
            out.append("<file ");
            out.append("type=\"" + "peptide" + "\" ");
            if (parentProtein != null) {
                out.append("URL=\"" + parentProtein.getURL() + "\" ");
            }
            out.append(" />");
            out.append("\n" );
     
            indent(out, indent);
            out.append("<peptide ");
            out.append("start=\"1\" ");
            if (parentProtein != null)
                out.append("end=\"" + parentProtein.getSequenceLength() + "\" ");
            else
                out.append("end=\"" + polypeptide.getSequenceLength() + "\" ");   // ok there is no protein

            out.append("  >");
            out.append("\n");

            outputDomain(scan, out, indent);


            indent(out, indent);
            out.append("</peptide>");
            out.append("\n");
            indent(out, indent);
            out.append("</protein>");
            out.append("\n");


            outputHistogram(scan, out, indent);


            //       writeDescriptionNote(parentProtein.getAnnotation(), out, indent);

            //    writeSupportingValues(out, indent, raw.getId(), scan);
//        indent(out, indent);
//        out.append("</group>");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    protected void outputHistogram(IScoredScan scan, Appendable out, int indent) {
        try {
            IMeasuredSpectrum raw = scan.getRaw();
            final HyperScoreStatistics hist = scan.getHyperScores();
            final ISpectralMatch bestMatch = scan.getBestMatch();
            final ITheoreticalSpectrumSet theory = bestMatch.getTheory();

            int numvalues = 50;
            indent(out, indent);
            out.append("<group type=\"support\" label=\"fragment ion mass spectrum\">");
            out.append("\n");
            IParameterHolder ph = getParameters();
            String title = ph.getParameter("output, title");
            if (title == null)
                title = ph.getParameter("spectrum, path");
            writeDescriptionNote(title + " scan " + scan.getId() + " (charge " + scan.getCharge() + ")",
                    out, indent, false);
            //  <note label="Description">myocontrolSmall.mzXML scan 1676 (charge 3)</note>
            out.append(
                    "<GAML:trace id=\"" + scan.getId() + "\" label=\"" + scan.getId() + ".spectrum\" type=\"tandem mass spectrum\">");
            out.append("\n");
            writeGAMLAttribute(out, indent, "M+H",
                    XTandemUtilities.formatDouble(raw.getPrecursorMass(), 2));
            writeGAMLAttribute(out, indent, "charge", Integer.toString(raw.getPrecursorCharge()));
            outputSpectrumData(out, indent, scan);


            out.append("</GAML:trace>");
            out.append("\n");
            out.append("</group></group>");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private void outputSpectrumData(final Appendable out, final int indent, IScoredScan scan) {

        try {
            IMeasuredSpectrum cs = scan.getConditionedScan();
            ISpectrumPeak[] peaks = cs.getPeaks();

            indent(out, indent);
            out.append("<GAML:Xdata ");
            out.append("label=\"" + scan.getId() + ".spectrum" + "\" ");
            out.append("units=\"" + "MASSTOCHARGERATIO" + "\" ");
            out.append(">");
            out.append("\n" );
            indent(out, indent);
            out.append(
                    "<GAML:values byteorder=\"INTEL\" format=\"ASCII\" numvalues=\"" + peaks.length + "\">");
            out.append("\n" );
            indent(out, indent);
            for (int i = 0; i < peaks.length; i++) {
                ISpectrumPeak peak = peaks[i];
                double mass = peak.getMassChargeRatio();
                out.append(XTandemUtilities.formatDouble(mass, 3));
                if ((i + 1) % 30 == 0) {
                    indent(out, indent);
                     out.append("\n" );   ;
                }
                else {
                    out.append(" ");
                }
            }
            out.append("\n" );
            ;
            out.append("\n" );
            indent(out, indent);
            out.append("</GAML:values>");
            out.append("\n" );
            indent(out, indent);
            out.append("</GAML:Xdata>");
            out.append("\n" );

            indent(out, indent);
            out.append("<GAML:Ydata ");
            out.append("label=\"" + scan.getId() + ".spectrum" + "\" ");
            out.append("units=\"" + "UNKNOWN" + "\" ");
            out.append(">");
            out.append("\n" );
            indent(out, indent);
            out.append(
                    "<GAML:values byteorder=\"INTEL\" format=\"ASCII\" numvalues=\"" + peaks.length + "\">");
            out.append("\n" );
            IMeasuredSpectrum raw = scan.getRaw();
            ISpectrumPeak[] rawPeaks = raw.getPeaks();
            double maxIntensity = raw.getMaxIntensity();
            double factor = scan.getNormalizationFactor();

            int rawIndex = 0;
            boolean lastIsNewLine = false;
            for (int i = 0; i < rawPeaks.length; i++) {
                if (i >= peaks.length)
                    break;
                ISpectrumPeak peak = peaks[i];
                while (rawPeaks[rawIndex].getMassChargeRatio() < peak.getMassChargeRatio()) {
                    rawIndex++;
                    if (rawIndex >= rawPeaks.length)
                        break;
                }
                if (rawIndex >= rawPeaks.length)
                    break;

                double factorPeak = rawPeaks[rawIndex].getPeak() * factor;
                int mass = (int) (factorPeak + 0.5);
                out.append(Integer.toString(mass));
                if ((i + 1) % 30 == 0) {
                    indent(out, indent);
                     out.append("\n" );   ;
                    out.append("\n" );
                    lastIsNewLine = true;
                }
                else {
                    lastIsNewLine = false;
                    out.append(" ");
                }
            }
            if (!lastIsNewLine) {
                out.append("\n" );
              }
            indent(out, indent);
            out.append("</GAML:values>");
            out.append("\n" );
            indent(out, indent);
            out.append("</GAML:Ydata>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    protected void outputDomain(IScoredScan scan, Appendable out, int indent) {
        try {
            IMeasuredSpectrum raw = scan.getRaw();
            final ISpectralMatch match = scan.getBestMatch();
            final ISpectralMatch nextmatch = scan.getNextBestMatch();
            final ISpectralMatch bestMatch = scan.getBestMatch();
            final IPolypeptide polypeptide = bestMatch.getPeptide();
            indent(out, indent);
            out.append("<domain ");
            out.append("id=\"" + raw.getId() + ".1.1" + "\" ");
            //      final int start = polypeptide.getStartPosition() + 1;   // not too sure why I am adding 1 but I am
            //      final int end = start + polypeptide.getSequenceLength() - 1;
            //      out.append("start=\"" + start + "\" ");
            //      out.append("end=\"" + end + "\" ");
            double expect = scan.getExpectedValue();

            out.append("start=\"0\" ");   // not supported
            out.append("end=\"" + polypeptide.getSequenceLength() + "\" ");   // not supported

            out.append("expect=\"" + XTandemUtilities.formatScientific(expect, 1) + "\" ");
            final int charge = scan.getCharge();
            final double mass = polypeptide.getMatchingMass();
            //       double mPlusHPeptide = mass + XTandemUtilities.getProtonMass() + XTandemUtilities.getCleaveCMass() + XTandemUtilities.getCleaveNMass();  // M + h
            double del = scan.getMassDifferenceFromBest();

            out.append("mh=\"" + XTandemUtilities.formatDouble(mass, 3) + "\" ");
            double diff = del;
            out.append(
                    "delta=\"" + XTandemUtilities.formatDouble(diff, 3) + "\" ");
            out.append(
                    "hyperscore=\"" + XTandemUtilities.formatDouble(match.getHyperScore(), 0) + "\" ");
            if (nextmatch != null)
                out.append("nextscore=\"" + XTandemUtilities.formatDouble(nextmatch.getHyperScore(),
                        0) + "\" ");
            else
                out.append("nextscore=\"100\" "); // just copying the XTandem behavior

            //     y_score="2" y_ions="15" b_score="2" b_ions="12" c_score="0" c_ions="0" z_score="0" z_ions="0" a_score="0" a_ions="0" x_score="0" x_ions="0" pre="ILKK" post="HKIP" seq="KGHHEAELKPLAQSHATK" missed_cleavages="1"
            //     missed_cleavages="1"
            outputIonTypeScores(out, bestMatch);

            //      out.append("pre=\"" + polypeptide.getPreSequence(4) + "\" ");
            //      out.append("post=\"" + polypeptide.getPostSequence(4) + "\" ");


            out.append("pre=\"\" ");   // not supported
            out.append("post=\"\" ");   // not supported

            out.append("seq=\"" + polypeptide.getSequence() + "\" ");
            out.append("missed_cleavages=\"" + polypeptide.getMissedCleavages() + "\" ");
            out.append(" >");
            out.append("\n" );
            if (polypeptide.isModified()) {
                outputModifications((IModifiedPeptide) polypeptide, out, indent);
            }
            indent(out, indent);
            out.append("</domain>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    protected void outputModifications(final IModifiedPeptide mp, final Appendable out, int indent) {
        PeptideModification[] modifications = mp.getModifications();
        for (int i = 0; i < modifications.length; i++) {
            PeptideModification modification = modifications[i];
            if (modification == null)
                continue;
            outputModification(mp, modification, i, out, indent);
        }

    }

    protected void outputModification(final IModifiedPeptide mp, PeptideModification modification, int index, final Appendable out, int indent) {
        try {
            indent(out, indent);
            out.append("<aa ");
            FastaAminoAcid aminoAcid = modification.getAminoAcid();
            out.append(" type=\"" + aminoAcid + "\"");
            out.append(" at=\"" + (index + 1) + "\"");   // NOTE! 1 based
            String modString = String.format("%10.3f", modification.getMassChange()).trim();
            out.append(" modified=\"" + modString + "\"");
            out.append(" />");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }


    }

    protected void outputIonTypeScores(Appendable out, ISpectralMatch pBestMatch) {
        try {
            out.append("y_score=\"" + XTandemUtilities.formatDouble(pBestMatch.getScore(IonType.Y),
                    0) + "\" ");
            out.append("y_ions=\"" + pBestMatch.getCount(IonType.Y) + "\" ");

            out.append("b_score=\"" + XTandemUtilities.formatDouble(pBestMatch.getScore(IonType.B),
                    0) + "\" ");
            out.append("b_ions=\"" + pBestMatch.getCount(IonType.B) + "\" ");

            out.append("c_score=\"" + XTandemUtilities.formatDouble(pBestMatch.getScore(IonType.C),
                    2) + "\" ");
            out.append("c_ions=\"" + pBestMatch.getCount(IonType.C) + "\" ");

            out.append("z_score=\"" + XTandemUtilities.formatDouble(pBestMatch.getScore(IonType.Z),
                    2) + "\" ");
            out.append("z_ions=\"" + pBestMatch.getCount(IonType.Z) + "\" ");

            out.append("a_score=\"" + XTandemUtilities.formatDouble(pBestMatch.getScore(IonType.A),
                    2) + "\" ");
            out.append("a_ions=\"" + pBestMatch.getCount(IonType.A) + "\" ");

            out.append("x_score=\"" + XTandemUtilities.formatDouble(pBestMatch.getScore(IonType.X),
                    2) + "\" ");
            out.append("x_ions=\"" + pBestMatch.getCount(IonType.X) + "\" ");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    protected void writeDescriptionNote(String text, Appendable out, int indent) {

        writeDescriptionNote(text, out, indent, true);

    }

    protected void writeDescriptionNote(String text, Appendable out, int indent, boolean onTwoLines) {

        try {
            indent(out, indent);
            out.append("<note ");
            out.append("label=\"Description\">" + text);
            //noinspection StatementWithEmptyBody
            if (onTwoLines) {
                out.append("\n");
                indent(out, indent);
            }
            else {
                //        out.append("  >");
            }
            out.append("</note>");
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    protected byte[] getXValues(RawPeptideScan scan) {
        int chunkSize = 8;
        ISpectrumPeak[] peaks = scan.getPeaks();
        byte[] ret = new byte[peaks.length * chunkSize];
        int index = 0;
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            double mass = peak.getMassChargeRatio();
            Base64Float.float64ToBytes(mass, ret, index);
            index += chunkSize;
        }

        return ret;
    }

    protected byte[] getYValues(RawPeptideScan scan) {
        int chunkSize = 4;
        ISpectrumPeak[] peaks = scan.getPeaks();
        byte[] ret = new byte[peaks.length * chunkSize];
        int index = 0;
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            float intensity = peak.getPeak();
            Base64Float.floatToBytes(intensity, ret, index);
            index += chunkSize;
        }

        return ret;
    }

    protected void writeSupportingData(Appendable out, int indent, int id, double hyperA0,
                                       double hyperA1) {
        try {
            indent(out, indent);
            out.append("<group label=\"supporting data\" type=\"support\">");
            out.append("\n" );
            indent(out, indent);
            out.append(
                    "<GAML:trace label=\"" + id + ".hyper\" type=\"hyperscore expectation function\">");
            out.append("\n" );
            writeGAMLAttribute(out, indent, "a0", XTandemUtilities.formatDouble(hyperA0, 6));
            writeGAMLAttribute(out, indent, "a1", XTandemUtilities.formatDouble(hyperA1, 6));
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    protected void writeGAMLAttribute(Appendable out, int indent, String type, String value) {
        try {
            indent(out, indent);
            out.append("<GAML:attribute type=\"" + type + "\">" + value + "</GAML:attribute>");
            out.append("\n" );
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

    protected void writeSupportingValues(Appendable out, int indent, int id, IScoredScan scan) {
        try {
            IMeasuredSpectrum cs = scan.getConditionedScan();
            ISpectrumPeak[] peaks = cs.getPeaks();
            indent(out, indent);
            out.append("<GAML:Xdata label=\"" + id + ".hyper\" units=\"score\">");
            out.append("\n" );
            indent(out, indent);
            out.append(
                    "<GAML:values byteorder=\"INTEL\" format=\"ASCII\" numvalues=\"" + peaks.length + "\">");
            out.append("\n" );
            indent(out, indent);
            for (int i = 0; i < peaks.length; i++) {
                ISpectrumPeak peak = peaks[i];
                double mass = peak.getMassChargeRatio();
                out.append(XTandemUtilities.formatDouble(mass, 3));
                if ((i + 1) % 30 == 0) {
                    indent(out, indent);
                    out.append("\n" );
                 }
                else {
                    out.append(" ");
                }
            }
            out.append("\n" );

            indent(out, indent);
            out.append("</GAML:values>");
            out.append("\n" );
            indent(out, indent);
            out.append("</GAML:Xdata>");
            out.append("\n" );

            indent(out, indent);
            out.append("<GAML:Ydata label=\"" + id + ".hyper\" units=\"counts\">");
            out.append("\n" );
            indent(out, indent);
            out.append(
                    "<GAML:values byteorder=\"INTEL\" format=\"ASCII\" numvalues=\"" + peaks.length + "\">");
            out.append("\n" );
            for (int i = 0; i < peaks.length; i++) {
                ISpectrumPeak peak = peaks[i];
                int mass = (int) (peak.getPeak() * 100);
                out.append(Integer.toString(mass));
                if ((i + 1) % 30 == 0) {
                    indent(out, indent);
                     out.append("\n" );   ;
                }
                else {
                    out.append(" ");
                }
            }
            out.append("\n" );
             indent(out, indent);
            out.append("</GAML:values>");
            out.append("\n" );
              indent(out, indent);
            out.append("</GAML:Ydata>");
            out.append("\n" );
         }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    protected void writeBytes(Appendable out, int indent, byte[] xdata1) {
        try {
            int colWidth = getHistogramColumnWidth();
            int column = 0;
            DecimalFormat fmt = new DecimalFormat("000");
            for (int i = 0; i < xdata1.length; i++) {
                int b = xdata1[i];
                out.append(fmt.format(b));
                column += 4;
                if (column > colWidth) {
                     out.append("\n" );   ;
                    indent(out, indent);
                    column = 0;
                }
                else {
                    out.append(" ");
                 }

            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    protected void writeParameter(String key, Appendable out, int indent) {
        final String value = getParameters().getParameter(key);
        if (value != null)
            writeNote(key, value, out, indent);
    }

    protected void writePerformanceParameter(String key, Appendable out, int indent) {
        IParameterHolder parameters = getParameters();
        if (parameters instanceof XTandemMain) {
            XTandemMain o = (XTandemMain) parameters;
            final String value = o.getPerformanceParameter(key);
            if (value != null)
                writeNote(key, value, out, indent);

        }
    }

    /**
     * @param label  !null label
     * @param value  !null value
     * @param out    !null Appendable
     * @param indent indent if any
     */
    protected void writeNote(String label, String value, Appendable out, int indent) {
        try {
            indent(out, indent);
            out.append("<note type=\"input\"  label=\"" + xmlEscape(label) + "\" >");
            out.append(xmlEscape(value));
            out.append("</note>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * write a note without a label
     *
     * @param value  !null value
     * @param out    !null Appendable
     * @param indent indent if any
     */
    protected void writeNote(String value, Appendable out, int indent) {
        try {
            indent(out, indent);
            out.append("<note >");
            out.append(xmlEscape(value));
            out.append("</note>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    protected void writeFooter(Appendable out, int indent) {
        try {
            indent(out, indent);
            out.append("</bioml>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * condition a string for xml output
     *
     * @param s
     * @return
     */
    public static String xmlEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                case '\"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /*
 * performance is called to output a group node that contains any
 * performance parameters from the protein modeling session that might be of interest
 * after the protein modeling session. Any number of additional output parameters can
 * be included here. An XmlParameter object is used to supply the information
 * required to create a series of note objects that contain the informtion.
 */

    private void writePerformanceParameters(Appendable out, int indent) {
        String[] params = {
                "list path, sequence source #1",   //myoglobin.fasta</note>
                "list path, sequence source description #1",   //no description</note>
                "modelling, estimated false positives",   //1</note>
                "modelling, spectrum noise suppression ratio",   //0.00</note>
                "modelling, total peptides used",   //302</note>
                "modelling, total proteins used",   //1</note>
                "modelling, total spectra assigned",   //30</note>
                "modelling, total spectra used",   //35</note>
                "modelling, total unique assigned",   //8</note>
                "process, start time",   //2011:01:11:08:53:23</note>
                "process, version",   //x! tandem 2010.10.01.1 (LabKey, Insilicos and ISB)</note>
                "quality values",   //2 1 2 1 2 1 3 2 4 3 2 1 2 2 1 1 1 1 0 0</note>
                "refining, # input models",   //1</note>
                "refining, # input spectra",   //5</note>
                "refining, # partial cleavage",   //0</note>
                "refining, # point mutations",   //0</note>
                "refining, # potential C-terminii",   //0</note>
                "refining, # potential N-terminii",   //0</note>
                "refining, # unanticipated cleavage",   //0</note>
                "timing, initial modelling total (sec)",   //196.41</note>
                "timing, initial modelling/spectrum (sec)",   //5.612</note>
                "timing, load sequence models (sec)",   //12.62</note>
                "timing, refinement/spectrum (sec)"    //0.646</note>
        };

        IMainData parameters = getParameters();
//        if (parameters instanceof HadoopTandemMain) {
//            HadoopTandemMain realParameters = (HadoopTandemMain) parameters;
//            int numberProteins = getNumberProteins();
//            realParameters.setPerformanceParameter("modelling, total proteins used", Integer.toString(numberProteins));
//            int numberPeptidess = getNumberPeptidess();
//            realParameters.setPerformanceParameter("modelling, total peptides used", Integer.toString(numberPeptidess));
//            int numberScoredScans = getNumberScoredScans();
//            realParameters.setPerformanceParameter("modelling, total spectra used", Integer.toString(numberScoredScans));
//
//            realParameters.setPerformanceParameter("process, version", "jxtandem version 0.1");
//        }


        try {
            indent(out, indent);
            out.append("<group label=\"performance parameters\" type=\"parameters\">");
            out.append("\n" );

            for (int j = 0; j < params.length; j++) {
                String param = params[j];
                writePerformanceParameter(param, out, indent + 1);
            }
            indent(out, indent);
            out.append("</group>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * The "unused input parameters" group
     * contains all of the input parameters that were either unsupported, mis-entered or otherwise
     * ignored.
     *
     * @param out
     * @param indent
     */
    private void writeUnusedParameters(Appendable out, int indent) {
        if(!(m_Parameters instanceof ISetableParameterHolder))
            return;
        String[] params = ((ISetableParameterHolder)m_Parameters).getUnusedKeys();

        indent(out, indent);
        try {
            out.append("<group label=\"unused input parameters\" type=\"parameters\">");
            out.append("\n" );
            for (int j = 0; j < params.length; j++) {
                String param = params[j];
                writeParameter(param, out, indent + 1);
            }
            indent(out, indent);
            out.append("</group>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


/*
* info outputs the input parameters derived from the input file and the default input file
* (if used). This record should be sufficient to repeat this protein modeling session exactly, without reference to
* the original input files.
* Two group nodes are used to record this information. The "input parameters" group contains all
* of the input nodes that were used to create this output file. The "unused input parameters" group
* contains all of the input parameters that were either unsupported, mis-entered or otherwise
* ignored.
*/

    private void writeParameters(Appendable out, int indent) {
        String[] params = getParameters().getParameterKeys();
        Set<String> unused = new HashSet<String>( );
        if( m_Parameters instanceof ISetableParameterHolder) {
            List<String> objects = Arrays.asList(((ISetableParameterHolder)m_Parameters).getUnusedKeys());
            unused.addAll(objects);
        }

        indent(out, indent);
        try {
            out.append("<group label=\"input parameters\" type=\"parameters\">");
            out.append("\n" );
            for (int j = 0; j < params.length; j++) {
                String param = params[j];
                if (unused.contains(param))
                    continue;
                writeParameter(param, out, indent + 1);
            }
            indent(out, indent);
            out.append("</group>");
            out.append("\n" );
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

}
