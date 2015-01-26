package org.systemsbiology.xtandem.fdr;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.fdr.FilteredFDRParser
 *
 * @author Steve Lewis
 * @date 8/26/13
 */
public class FilteredFDRParser {

    private final String m_InputFileName;
    private final String m_OutputFileName;
    private final FDRParser m_Parser;
    private final Properties m_Props;
    private final OrSpectrumDataFilter m_Filters = new OrSpectrumDataFilter();

    public FilteredFDRParser(String propertiesFile) {
        m_Props = new Properties();
        File file = new File(propertiesFile);
        if (!file.exists() || !file.isFile())
            throw new IllegalStateException("properties file " + propertiesFile + " does not exist");
        try {
            m_Props.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        m_InputFileName = m_Props.getProperty("input_file");
        m_OutputFileName = m_Props.getProperty("output_file");

        String minScoreStr = m_Props.getProperty("hyperscore");
        if (minScoreStr != null) {
            double minScore = Double.parseDouble(minScoreStr);
            m_Filters.addFilter(new KScoreSpectrumFilter(minScore));
        }
        m_Parser = new FDRParser(m_InputFileName);
    }

    public void parse() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(m_OutputFileName));
            m_Parser.readFileAndGenerateFDR(pw, m_Filters);
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String SAMPLE_PROPERTY_FILE =
            "#\n" +
                    "# Sample and Test file for FIltered FDR Parser\n" +
                    "#\n" +
                    "# file to read as input\n" +
                    "input_file=A5decoy_v1.2013_08_213_11_35_03.t.xml.pep.xml\n" +
                    "#\n" +
                    "# file to write output\n" +
                    "output_file=A5decoy_v1.2013_08_213_11_35_03Filtered_300.pep.xml\n" +
                    "#\n" +
                    "# ignore values less than this score\n" +
                    "minimum_kscore=300.0\n";


    protected static void usage() {
        System.out.println("Usage <property files> ");
        System.out.println("Which look like the following:");
        System.out.println(SAMPLE_PROPERTY_FILE);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            FilteredFDRParser fdr = new FilteredFDRParser(arg);
            fdr.parse();
        }
    }
}
