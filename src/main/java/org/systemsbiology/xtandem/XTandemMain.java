package org.systemsbiology.xtandem;

import com.lordjoe.distributed.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.sax.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.taxonomy.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.XTandemMain
 * User: steven
 * Date: Jan 5, 2011
 * Singleton representing a JXTandem job -
 * This has the program main
 */
public class XTandemMain extends AbstractParameterHolder implements IMainData {
    public static final IMainData[] EMPTY_ARRAY = {};
    public static final String HARDCODED_MODIFICATIONS_PROPERTY = "org.systemsbiology.xtandem.HardCodeModifications";
    public static final String NUMBER_REMEMBERED_MATCHES = "org.systemsbiology.numberRememberedMatches";

    private static boolean gShowParameters = true;

    public static boolean isShowParameters() {
        return gShowParameters;
    }

    public static void setShowParameters(final boolean pGShowParameters) {
        gShowParameters = pGShowParameters;
    }

    private static final List<IStreamOpener> gPreLoadOpeners =
            new ArrayList<IStreamOpener>();

    public static void addPreLoadOpener(IStreamOpener opener) {
        gPreLoadOpeners.add(opener);
    }

    public static IStreamOpener[] getPreloadOpeners() {
        return gPreLoadOpeners.toArray(new IStreamOpener[gPreLoadOpeners.size()]);
    }

    public static final int MAX_SCANS = Integer.MAX_VALUE;

    public static int getMaxScans() {
        return MAX_SCANS;
    }


    private static String gRequiredPathPrefix;

    public static String getRequiredPathPrefix() {
        return gRequiredPathPrefix;
    }

    public static void setRequiredPathPrefix(final String pRequiredPathPrefix) {
        gRequiredPathPrefix = pRequiredPathPrefix;
    }


    private MassType m_MassType = MassType.monoisotopic;
    private boolean m_SemiTryptic;
    private String m_DefaultParameters;
    private String m_TaxonomyInfo;
    private String m_SpectrumPath;
    private String m_OutputPath;
    private String m_OutputResults;
    private String m_TaxonomyName;
    private final StringBuffer m_Log = new StringBuffer();

    //    private IScoringAlgorithm m_Scorer;
    private SpectrumCondition m_SpectrumParameters;
    private MassSpecRun[] m_Runs;
    private ITaxonomy m_Taxonomy;
    private IPeptideDigester m_Digester;
    private TaxonomyProcessor m_ProteinHandler;
    private ScoringModifications m_ScoringMods;
    private final List<ITandemScoringAlgorithm> m_Algorithms = new ArrayList<ITandemScoringAlgorithm>();

    private Scorer m_ScoreRunner;
    private final SequenceUtilities[] m_SequenceUtilitiesByMasssType = new SequenceUtilities[2];
    // private ElapsedTimer m_Elapsed = new ElapsedTimer();

    private Map<String, IScoredScan> m_Scorings = new HashMap<String, IScoredScan>();
    private Map<String, RawPeptideScan> m_RawScans = new HashMap<String, RawPeptideScan>();
    private final Map<String, String> m_PerformanceParameters = new HashMap<String, String>();
    private final DelegatingFileStreamOpener m_Openers = new DelegatingFileStreamOpener();

    // used by Map Reduce

    protected XTandemMain() {
        //     Protein.resetNextId();
        initOpeners();
    }


    public XTandemMain(final File pTaskFile) {
        String m_TaskFile = pTaskFile.getAbsolutePath();
        //      Protein.resetNextId();
        initOpeners();
        Properties predefined = XTandemHadoopUtilities.getHadoopProperties();
        for (String key : predefined.stringPropertyNames()) {
            setPredefinedParameter(key, predefined.getProperty(key));
        }
        try {
            InputStream is = new FileInputStream(m_TaskFile);
            handleInputs(is, pTaskFile.getName());
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
        //       if (gInstance != null)
        //           throw new IllegalStateException("Only one XTandemMain allowed");
    }


    public static final String DO_DEBUGGING_KEY = "org.systemsbiology.xtandem.XTandemMain.DO_DEBUGGING"; // fix this

    public XTandemMain(final InputStream is, String url) {
        //     Protein.resetNextId();
        initOpeners();
        Properties predefined = XTandemHadoopUtilities.getHadoopProperties();
        for (String key : predefined.stringPropertyNames()) {
            setPredefinedParameter(key, predefined.getProperty(key));
         }
        handleInputs(is, url);
        //     if (gInstance != null)
        //        throw new IllegalStateException("Only one XTandemMain allowed");
      }


    private void setPredefinedParameter(String key, String value) {
        setParameter(key, value);
        if (key.equals("org.systemsbiology.algorithms")) {
            addAlternateParameters(value);

        }
    }

    public void appendLog(String added) {
        m_Log.append(added);
    }

    public void clearLog( ) {
        m_Log.setLength(0);
    }

    public String getLog( ) {
        return m_Log.toString();
    }


    public void setPerformanceParameter(String key, String value) {
        m_PerformanceParameters.put(key, value);
    }

    /**
     * return a parameter configured in  default parameters
     *
     * @param key !null key
     * @return possibly null parameter
     */
    public String getPerformanceParameter(String key) {
        return m_PerformanceParameters.get(key);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getPerformanceKeys() {
        String[] ret = m_PerformanceParameters.keySet().toArray(new String[0]);
        Arrays.sort(ret);
        return ret;
    }

    /**
     * add new ways to open files
     */
    protected void initOpeners() {
        addOpener(new FileStreamOpener());
        addOpener(new StreamOpeners.ResourceStreamOpener(XTandemUtilities.class));
        for (IStreamOpener opener : getPreloadOpeners())
            addOpener(opener);
    }


    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(String fileName, Object... otherData) {
        return m_Openers.open(fileName, otherData);
    }

    public void addOpener(IStreamOpener opener) {
        m_Openers.addOpener(opener);
    }

    public void addRawScan(RawPeptideScan added) {
        m_RawScans.put(added.getId(), added);
    }


    public boolean isSemiTryptic() {
        return m_SemiTryptic;
    }

    public void setSemiTryptic(final boolean pSemiTryptic) {
        m_SemiTryptic = pSemiTryptic;
    }



    @Override
    public RawPeptideScan getRawScan(String key) {
        return m_RawScans.get(key);
    }


    public void addScoring(IScoredScan added) {
        m_Scorings.put(added.getId(), added);
    }


    public void removeScoring(Integer removed) {
        m_Scorings.remove(removed);
    }

       /**
     * remove all retained data
     */
    @Override
    public void clearRetainedData() {
        m_Scorings.clear();
        m_RawScans.clear();
    }

    @Override
    public IScoredScan getScoring(String key) {
        IScoredScan ret = m_Scorings.get(key);
        if (ret == null) {
            RawPeptideScan rawScan = getRawScan(key);
            if (rawScan == null)
                throw new IllegalArgumentException("bad scan key " + key);
            ret = new ScoredScan(rawScan);
            addScoring(ret);
        }
        return ret;
    }


    public IPeptideDigester getDigester() {
        return m_Digester;
    }

    public void setDigester(final IPeptideDigester pDigester) {
        m_Digester = pDigester;
    }

    /**
     * what do we call the database or output directory
     *
     * @return !null name
     */
    @Override
    public String getDatabaseName() {
        String ret = m_TaxonomyName; //getParameter("protein, taxon");
        //  System.err.println("database name = " + m_TaxonomyName);
        return conditionDatabaseName(ret);
    }

    protected String conditionDatabaseName(String s) {
        if (s == null)
            return "database";
        s = s.replace(".fasta", "");
        s = s.replace(":", "");
        s = s.replace(".;", "");
        return s;
    }

    /**
     * how are we digesting the fragmensts
     *
     * @return !null name
     */
    @Override
    public String getDigestandModificationsString() {
        IPeptideDigester digester = getDigester();

        //    if(true)
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    /**
     * parse the initial file and get run parameters
     *
     * @param is
     */
    public void handleInputs(final InputStream is, String url) {
        Map<String, String> notes = XTandemUtilities.readNotes(is, url);

        if(isShowParameters())  {
            for (String key : notes.keySet()) {
                setParameter(key, notes.get(key));
                System.err.println(key + " = " + notes.get(key));
            }
          }
         m_DefaultParameters = notes.get(
                "list path, default parameters"); //, "default_input.xml");
        m_TaxonomyInfo = notes.get(
                "list path, taxonomy information"); //, "taxonomy.xml");
        m_TaxonomyName = notes.get("protein, taxon");
        m_SpectrumPath = notes.get("spectrum, path"); //, "test_spectra.mgf");
        m_OutputPath = notes.get("output, path"); //, "output.xml");
        // little hack to separate real tandem and hydra results
        if (m_OutputPath != null)
            m_OutputPath = m_OutputPath.replace(".tandem.xml", ".hydra.xml");

        m_OutputResults = notes.get("output, results");

        String requiredPrefix = getRequiredPathPrefix();
        if (requiredPrefix != null) {
            if (m_DefaultParameters != null && !m_DefaultParameters.startsWith(requiredPrefix))
                m_DefaultParameters = requiredPrefix + m_DefaultParameters;
            if (m_TaxonomyInfo != null && !m_TaxonomyInfo.startsWith(requiredPrefix))
                m_TaxonomyInfo = requiredPrefix + m_TaxonomyInfo;
            if (m_OutputPath != null && !m_OutputPath.startsWith(requiredPrefix))
                m_OutputPath = requiredPrefix + m_OutputPath;
            if (m_SpectrumPath != null && !m_SpectrumPath.startsWith(requiredPrefix))
                m_SpectrumPath = requiredPrefix + m_SpectrumPath;
        }

        try {
            readDefaultParameters(notes);
        }
        catch (Exception e) {
            // forgive
            System.err.println("Cannot find file " + m_DefaultParameters);
        }

        XTandemUtilities.validateParameters(this);

        m_ScoringMods = new ScoringModifications(this);

        m_MassType = this.getEnumParameter("spectrum, fragment mass type", MassType.class, MassType.monoisotopic);
        MassCalculator.setDefaultMassType(m_MassType);

        m_SequenceUtilitiesByMasssType[0] = new SequenceUtilities(MassType.monoisotopic, this);
        m_SequenceUtilitiesByMasssType[0].config(this);
        m_SequenceUtilitiesByMasssType[1] = new SequenceUtilities(MassType.average, this);
        m_SequenceUtilitiesByMasssType[1].config(this);

        // maybe limit the number of scans to save memory
        XTandemUtilities.setMaxHandledScans(getIntParameter("org.systemsbiology.xtandem.MaxScoredScans", Integer.MAX_VALUE));

        String digesterSpec = getParameter("protein, cleavage site", "trypsin");
        int missedCleavages = getIntParameter("scoring, maximum missed cleavage sites", 0);


        IPeptideDigester digester = PeptideBondDigester.getDigester(digesterSpec);
        digester.setNumberMissedCleavages(missedCleavages);

        boolean bval = getBooleanParameter("protein, cleavage semi", false);
        setSemiTryptic(bval);
        digester.setSemiTryptic(bval);
        setDigester(digester);

//        String parameter = getParameter(JXTandemLauncher.ALGORITHMS_PROPERTY);
//        if (parameter != null)
//            addAlternateParameters(parameter);

        int maxMods = getIntParameter(ModifiedPolypeptide.MAX_MODIFICASTIONS_PARAMETER_NAME, ModifiedPolypeptide.DEFAULT_MAX_MODIFICATIONS);
        ModifiedPolypeptide.setMaxPeptideModifications(maxMods);

        if(getBooleanParameter(DO_DEBUGGING_KEY,false))  {
               XTandemDebugging.setDebugging(true, this);
           }


    }


    protected void addAlternateParameters(final String pParameter) {
        String[] items = pParameter.split(";");
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            addAlternateParameter(item);
        }
    }

    protected void addAlternateParameter(final String pItem) {

        try {
            Class<?> cls = Class.forName(pItem);
            ITandemScoringAlgorithm algorithm = (ITandemScoringAlgorithm) cls.newInstance();
            algorithm.configure(this);
            addAlgorithm(algorithm);
        }
        catch (RuntimeException e) {
            throw e;

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addAlgorithm(ITandemScoringAlgorithm added) {
        added.configure(this);
        if (m_Algorithms.size() > 0) {
            ITandemScoringAlgorithm[] existing = getAlgorithms();
            for (int i = 0; i < existing.length; i++) {
                ITandemScoringAlgorithm present = existing[i];
                if (added.getClass() == present.getClass())
                    return; // one algorithm per class
               }
        }
        m_Algorithms.add(added);
    }

    /**
     * get the first an I presume the default scorer
     *
     * @return
     */
    @Override
    public ITandemScoringAlgorithm getScorer() {
        return getAlgorithms()[0];
    }


    @Override
    public ITandemScoringAlgorithm[] getAlgorithms() {
        if (m_Algorithms.size() == 0) {
                return TandemKScoringAlgorithm.DEFAULT_ALGORITHMS;
           // return CometScoringAlgorithm.DEFAULT_ALGORITHMS;
         }
        return m_Algorithms.toArray(ITandemScoringAlgorithm.EMPTY_ARRAY);
    }


    /**
     * use monoisotopic or average mass
     *
     * @return !null masstype
     */
    public MassType getMassType() {
        return m_MassType;
    }

    /**
     * find the first protien with this sequwence and return the correcponding id
     *
     * @param sequence
     * @return
     */
    public String seqenceToID(String sequence) {
        ITaxonomy iTaxonomy = getTaxonomy();
        return iTaxonomy.sequenceToID(sequence);
    }


    protected static File getInputFile(Map<String, String> notes, String key) {
        File ret = new File(notes.get(key));
        if (!ret.exists() || !ret.canRead())
            throw new IllegalArgumentException("cannot access file " + ret.getName());
        return ret;
    }

    protected static File getOutputFile(Map<String, String> notes, String key) {
        String athname = notes.get(key);
        File ret = new File(athname);
        File parentFile = ret.getParentFile();
        if ((parentFile != null && (!parentFile.exists() || parentFile.canWrite())))
            throw new IllegalArgumentException("cannot access file " + ret.getName());
        if (ret.exists() && !ret.canWrite())
            throw new IllegalArgumentException("cannot rewrite file file " + ret.getName());

        return ret;
    }

    public SequenceUtilities getSequenceUtilities() {
        return getSequenceUtilities(MassCalculator.getDefaultMassType());
    }

    public SequenceUtilities getSequenceUtilities(MassType type) {
        switch (type) {
            case monoisotopic:
                return m_SequenceUtilitiesByMasssType[0];
            case average:
                return m_SequenceUtilitiesByMasssType[1];

        }
        throw new UnsupportedOperationException("Never get here");
    }


    public String getDefaultParameters() {
        return m_DefaultParameters;
    }

    public String getTaxonomyInfo() {
        return m_TaxonomyInfo;
    }

    public String getSpectrumPath() {
        return m_SpectrumPath;
    }

    public String getOutputPath() {
        return m_OutputPath;
    }

    public String getOutputResults() {
        return m_OutputResults;
    }

    public String getTaxonomyName() {
        return m_TaxonomyName;
    }

//    public void setScorer(IScoringAlgorithm pScorer) {
//        m_Scorer = pScorer;
//    }

    public synchronized SpectrumCondition getSpectrumParameters() {
        if (m_SpectrumParameters == null) {
            m_SpectrumParameters = new SpectrumCondition();
            m_SpectrumParameters.configure(this);
        }
        return m_SpectrumParameters;
    }

    public void setSpectrumParameters(SpectrumCondition pSpectrumParameters) {
        m_SpectrumParameters = pSpectrumParameters;
    }

    @Override
    public MassSpecRun[] getRuns() {
        return m_Runs;
    }

    public void setRuns(MassSpecRun[] pRuns) {

        m_Runs = new MassSpecRun[pRuns.length];
        System.arraycopy(pRuns, 0, m_Runs, 0, pRuns.length);
        for (int i = 0; i < pRuns.length; i++) {
            MassSpecRun run = pRuns[i];
            RawPeptideScan[] scans = run.getScans();
            for (int j = 0; j < scans.length; j++) {
                RawPeptideScan scan = scans[j];
                addRawScan(scan);
            }
        }
    }

    public synchronized ITaxonomy getTaxonomy() {
        if (m_Taxonomy == null)
            loadTaxonomy();
        return m_Taxonomy;
    }


     public void loadScoring() {
        //  loadTaxonomy(); // read the taxonomy files

        buildScoringAlgorithm();
        getSpectrumParameters();
        m_ProteinHandler = new TaxonomyProcessor();
        m_ProteinHandler.configure(this);

        getScoreRunner();
    }

    public synchronized Scorer getScoreRunner() {
        if (m_ScoreRunner == null || m_ScoreRunner.getAlgorithm() == null) {
            IScoringAlgorithm algorithm = getScorer();
            SequenceUtilities su = getSequenceUtilities();
            SpectrumCondition sp = getSpectrumParameters();
            m_ScoreRunner = new Scorer(this, su, sp, algorithm );

        }
        return m_ScoreRunner;
    }

    public ScoringModifications getScoringMods() {
        return m_ScoringMods;
    }

        /*
 * modify checks the input parameters for known parameters that are use to modify
 * a protein sequence. these parameters are stored in the m_pScore member object's
 * msequenceutilities member object
 */

    protected String[] readModifications() {
        List<String> holder = new ArrayList<String>();
        String value;

        String strKey = "residue, modification mass";

        value = getParameter(strKey);
        if (value != null)
            holder.add(value);

        String strKeyBase = "residue, modification mass ";
        int a = 1;
        value = getParameter(strKeyBase + (a++));
        while (value != null) {
            holder.add(value);
            value = getParameter(strKeyBase + (a++));
        }

        strKeyBase = "residue, potential modification mass";
        value = getParameter(strKeyBase + (a++));
        value = getParameter(strKey);
        if(true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
//        if (m_xmlValues.get(strKey, strValue)) {
//           m_pScore - > m_seqUtil.modify_maybe(strValue);
//           m_pScore - > m_seqUtilAvg.modify_maybe(strValue);
//       }
//        strKey = "residue, potential modification motif";
//        if (m_xmlValues.get(strKey, strValue)) {
//            m_pScore - > m_seqUtil.modify_motif(strValue);
//            m_pScore - > m_seqUtilAvg.modify_motif(strValue);
//        }
//        strKey = "protein, N-terminal residue modification mass";
//        if (m_xmlValues.get(strKey, strValue)) {
//            m_pScore - > m_seqUtil.modify_n((float) atof(strValue.c_str()));
//            m_pScore - > m_seqUtilAvg.modify_n((float) atof(strValue.c_str()));
//        }
//        strKey = "protein, C-terminal residue modification mass";
//        if (m_xmlValues.get(strKey, strValue)) {
//            m_pScore - > m_seqUtil.modify_c((float) atof(strValue.c_str()));
//            m_pScore - > m_seqUtilAvg.modify_c((float) atof(strValue.c_str()));
//        }
//        strKey = "protein, cleavage N-terminal mass change";
//        if (m_xmlValues.get(strKey, strValue)) {
//            m_pScore - > m_seqUtil.m_dCleaveN = atof(strValue.c_str());
//            m_pScore - > m_seqUtilAvg.m_dCleaveN = atof(strValue.c_str());
//        }
//        strKey = "protein, cleavage C-terminal mass change";
//        if (m_xmlValues.get(strKey, strValue)) {
//            m_pScore - > m_seqUtil.m_dCleaveC = atof(strValue.c_str());
//            m_pScore - > m_seqUtilAvg.m_dCleaveC = atof(strValue.c_str());
//        }
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    public final String DEFAULT_SCORING_CLASS = "org.systemsbiology.xtandem.TandemKScoringAlgorithm";

    /**
     * use configuration information to construct an  object
     * that will do scoring
     * default is an instance of
     */
    protected void buildScoringAlgorithm() {
        String defaultScoringClass = DEFAULT_SCORING_CLASS;
        final String configuredAlgorithm = getParameter("scoring, algorithm");
        if (configuredAlgorithm != null) {
            buildScoringAlgorithm(configuredAlgorithm); // do configurable scoring here
            return;
        }

        ITandemScoringAlgorithm scorer = XTandemUtilities.buildObject(ITandemScoringAlgorithm.class, defaultScoringClass);
        scorer.configure(this); // let the scorer ste its parameters
        addAlgorithm(scorer);
        return;
    }


    private void buildScoringAlgorithm(final String configuredAlgorithm) {
        if ("k-score".equals(configuredAlgorithm)) {
            ITandemScoringAlgorithm scorer = XTandemUtilities.buildObject(ITandemScoringAlgorithm.class, "org.systemsbiology.xtandem.TandemKScoringAlgorithm");
            scorer.configure(this); // let the scorer ste its parameters
            addAlgorithm(scorer);
            return;
        }
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

/*
 * taxonomy uses the taxonomy information in the input XML file to load
 * the  ProteinSequenceServer member object with file path names to the required
 * sequence list files (FASTA format only in the initial release). If these
 */

    public void loadTaxonomy() {
        if (m_Taxonomy != null)
            return; // already done
        String strKey = "list path, taxonomy information";
        String path = getParameter(strKey);
//        strKey = "protein, taxon";
//        String taxonomyName = getParameter(strKey);


        final String taxonomyName = getTaxonomyName();
        final String descriptiveFile = getTaxonomyInfo();

        // Database version
//        String hostname = getParameter(SpringJDBCUtilities.DATA_HOST_PARAMETER);
//        if (hostname != null) {
//            // using files
//            m_Taxonomy = new JDBCTaxonomy(this);
//        }
//        else {
        // using files
        m_Taxonomy = new Taxonomy(this, taxonomyName, descriptiveFile);

        //      }


        strKey = "org.systemsbiology.xtandem.TaxonomyTranch";
        String tranchData = getParameter(strKey);
        if (tranchData != null) {
            if (m_Taxonomy instanceof Taxonomy)
                setTranch((Taxonomy) m_Taxonomy, tranchData);
        }

        TaxonHandler taxonHandler = new TaxonHandler(null, "peptide", taxonomyName);

        if (path != null) {
            InputStream is = open(path);
            String[] peptideFiles = XTandemUtilities.parseFile(is, taxonHandler, path);
            taxonHandler = new TaxonHandler(null, "saps", taxonomyName);
            is = open(path);
            String[] sapFiles = XTandemUtilities.parseFile(is, taxonHandler, path);

            // This step is called load annotation in XTandem
            taxonHandler = new TaxonHandler(null, "mods", taxonomyName);
            is = open(path);
            String[] annotationfiles = XTandemUtilities.parseFile(is, taxonHandler, path);

        }
        else {

        }

    }

    protected void setTranch(final Taxonomy pTaxonomy, final String pTranchData) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        String[] items = pTranchData.split(",");
//        switch (items.length) {
//            case 2:
//                int index = Integer.parseInt(items[0]);
//                int repeat = Integer.parseInt(items[1]);
//                pTaxonomy.setTranch(new TaxonomyTranch(index, repeat));
//                break;
//            case 4:
//                int index1 = Integer.parseInt(items[0]);
//                int repeat1 = Integer.parseInt(items[1]);
//                int start = Integer.parseInt(items[2]);
//                int end = Integer.parseInt(items[3]);
//                pTaxonomy.setTranch(new TaxonomyTranch(index1, repeat1, start, end));
//                break;
//            default:
//                throw new IllegalArgumentException("bad TranchData " + pTranchData);
//        }
    }

    /**
     * read the parameters dscribed in the bioml file
     * listed in "list path, default parameters"
     * These look like
     * <note>spectrum parameters</note>
     * <note type="input" label="spectrum, fragment monoisotopic mass error">0.4</note>
     * <note type="input" label="spectrum, parent monoisotopic mass error plus">100</note>
     * <note type="input" label="spectrum, parent monoisotopic mass error minus">100</note>
     * <note type="input" label="spectrum, parent monoisotopic mass isotope error">yes</note>
     */
    protected void readDefaultParameters(Map<String, String> inputParameters) {
        Map<String, String> parametersMap = getParametersMap();
        if (m_DefaultParameters != null) {
            String paramName;
            InputStream is;
            if (m_DefaultParameters.startsWith("res://")) {
                is = XTandemUtilities.getDescribedStream(m_DefaultParameters);
                paramName = m_DefaultParameters;
            }
            else {
                String defaults = SparkUtilities.buildPath(m_DefaultParameters);
                 File f = new File(defaults);
                if (f.exists() && f.isFile() && f.canRead()) {
                    try {
                        is = new FileInputStream(f);
                    }
                    catch (FileNotFoundException e) {
                        throw new RuntimeException(e);

                    }
                    paramName = f.getName();
                }
                else {
                    paramName = XMLUtilities.asLocalFile(m_DefaultParameters);
                    is = open(paramName);
                }
            }
            if (is == null) {
                // maybe this is a resource
                is = XTandemMain.class.getResourceAsStream(m_DefaultParameters);
                if (is == null) {
                    if (paramName.toLowerCase().contains(DefaultKScoreProperties.IMPLEMENTED_DEFAULT_FILE)) {
                        DefaultKScoreProperties.addDefaultProperties(parametersMap);
                        return;
                    }
                    else {    // give up
                        throw new IllegalArgumentException("the default input file designated by \"list path, default parameters\" " + m_DefaultParameters + "  does not exist"); // ToDo change

                    }
                }
            }
            else {
                Map<String, String> map = XTandemUtilities.readNotes(is, paramName);
                for (String key : map.keySet()) {
                    if (!parametersMap.containsKey(key)) {
                        String value = map.get(key);
                        parametersMap.put(key, value);
                    }
                }
            }
        }
        // parameters in the input file override parameters in the default file
        parametersMap.putAll(inputParameters);
        inputParameters.putAll(parametersMap);
    }

    public static void usage() {
        XMLUtilities.outputLine("Usage - JXTandem <inputfile>");
    }


}
