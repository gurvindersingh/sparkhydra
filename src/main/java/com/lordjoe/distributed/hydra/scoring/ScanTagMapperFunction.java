package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.tandem.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import javax.annotation.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.ScanTagMapper
 * User: steven
 * Date: 3/7/11
 */
public class ScanTagMapperFunction extends AbstractTandemFunction implements IMapperFunction<String, IMeasuredSpectrum, String, IMeasuredSpectrum> {
    public static final ScanTagMapperFunction[] EMPTY_ARRAY = {};

    /// Some debugging hooks when we walk interesting cases
    public static final int[] INTERESTING_MASSES = {2336, 2318};

    public static final int REPORT_INTERVAL_SCANS = 1000;

    public static final Random RND = new Random();


    /// Some debugging hooks when we walk interesting cases
    public static boolean isMassInteresting(int mass) {
        for (int i = 0; i < INTERESTING_MASSES.length; i++) {
            if (mass == INTERESTING_MASSES[i])
                return true;

        }
        return false;

    }

    //  private final Map<Integer, List<Path>> m_DatabaseFiles = new HashMap<Integer, List<Path>>();
    private Map<Integer, Integer> m_DatabaseSizes = new HashMap<Integer, Integer>();
    private int m_MaxScoredPeptides;
    private SpectrumCondition m_Condition;
    private int m_TotalScansProcessed;
    private long m_CohortProcessingTime;
    private long m_KeyWriteTime;
    private int m_KeyWriteCount;
    private long m_ParsingTime;

    public ScanTagMapperFunction(final XTandemMain pMain) {
        super(pMain);
    }

    public int getTotalScansProcessed() {
        return m_TotalScansProcessed;
    }

    public void incrementTotalScansProcessed() {
        m_TotalScansProcessed++;
    }

    @Override
    public void setup(JavaSparkContext context) {
        super.setup(context);

        XTandemMain app = getApplication();
        app.loadScoring();
        Configuration config = context.hadoopConfiguration();
        m_DatabaseSizes = XTandemHadoopUtilities.readDatabaseSizes(app, config);
        m_MaxScoredPeptides = XTandemHadoopUtilities.getMaxScoredPeptides(config);


        boolean doHardCoded = app.getBooleanParameter(ScoringReducer.HARDCODED_MODIFICATIONS_PROPERTY, true);
        PeptideModification.setHardCodeModifications(doHardCoded);


        // sneaky trick to extract the version
        String version = VersionInfo.getVersion();
        incrementCounter("Performance", "Version-" + version);
        // sneaky trick to extract the user
        String uname = System.getProperty("user.name");
        incrementCounter("Performance", "User-" + uname);

//        // Only do this once
//        if (XTandemHadoopUtilities.isFirstMapTask(context)) {
//            long maxOnePeptide = XTandemHadoopUtilities.maxDatabaseSizes(m_DatabaseSizes);
//            incrementCounter("Performance", "MaxFragmentsAtMass", maxOnePeptide);
//            long totalDatabaseFragments = XTandemHadoopUtilities.sumDatabaseSizes(m_DatabaseSizes);
//            incrementCounter("Performance", "TotalDatabaseFragments", totalDatabaseFragments);
//        }

        m_Condition = new SpectrumCondition();
        m_Condition.configure(app);
        getElapsed().reset(); // start a clock
    }

    public int getMaxScoredPeptides() {
        return m_MaxScoredPeptides;
    }

//    public void addDatabaseFiles(Integer key, List<Path> added) {
//        m_DatabaseFiles.put(key, added);
//    }
//
//    public List<Path> getDatabaseFiles(Integer key) {
//        return m_DatabaseFiles.get(key);
//    }


    public SpectrumCondition getCondition() {
        return m_Condition;
    }


    protected boolean isMassScored(double mass) {
        return getCondition().isMassScored(mass);
    }

    public int getDatabaseSize(Integer key) {
        Integer size = m_DatabaseSizes.get(key);
        if (size == null)
            return 0;
        return size;
    }


    /**
     * this is what a Mapper does
     *
     * @param keyin
     * @param valuein
     * @return iterator over mapped key values
     */
    @Nonnull
    @Override
    public Iterable<KeyValueObject<String, IMeasuredSpectrum>> mapValues(@Nonnull final String fileName, @Nonnull IMeasuredSpectrum ascan) {
        List<KeyValueObject<String, IMeasuredSpectrum>> ret = new ArrayList<KeyValueObject<String, IMeasuredSpectrum>>();
        long startTime = System.currentTimeMillis();
        // ignore level 1 scans
        // if (textv.get("msLevel=\"1\""))
        //     return ret;
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        // handle gz files
        if (".gz".equals(extension)) {
            String realName = fileName;
            realName = realName.substring(0, realName.length() - 3); // drop .gz
            extension = realName.substring(fileName.lastIndexOf("."));
        }

        RawPeptideScan scan = (RawPeptideScan) ascan;

        String id = scan.getId();

        // debugging code
//        if(XTandemHadoopUtilities.isNotScored(scan))
//            XTandemUtilities.breakHere();

        //       if("8840".equals(id))
//            XTandemUtilities.breakHere();
        // top level nested scans lack this
        if (scan.getPrecursorMz() == null)
            return ret;

        scan.setUrl(fileName);
        //     textv = scan.toMzMLFragment();
        IScanPrecursorMZ mz = scan.getPrecursorMz();
        int charge = scan.getPrecursorCharge();
        if (charge == 0) {
            int guess = XTandemUtilities.guessCharge(scan, mz.getMassChargeRatio());
            mz = new ScanPrecursorMz(mz, guess);
            scan.setPrecursorMz(mz);
            charge = mz.getPrecursorCharge();
        }

        // cost of parsing
        m_ParsingTime += System.currentTimeMillis() - startTime;

        incrementCounter("Performance", "TotalScoredScans");

//        if ("42".equals(id))
//            XTandemUtilities.breakHere();

        // throw out ridiculous values
        if (charge > XTandemUtilities.MAX_CHARGE)
            return ret;



        if (charge == 0) {
            for (int i = 1; i <= 3; i++) {
                double mass = scan.getPrecursorMass(i);
                writeScansToMassAtCharge(scan, ret, id, mass, i, fileName);
            }
        }
        else {
            double mass = scan.getPrecursorMass(charge);
            writeScansToMass(scan, ret, id, mass);
        }

        // give performance statistics
        incrementTotalScansProcessed();
        long elapsedProcessingTime = System.currentTimeMillis() - startTime;
        m_CohortProcessingTime += elapsedProcessingTime;
        if (getTotalScansProcessed() % REPORT_INTERVAL_SCANS == 0) {
            showStatistics();
        }

        getApplication().clearRetainedData();
        return ret;
    }

    private String buildUrlNameValue(final String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        sb.append("<nameValue name=\"url\" ");
        sb.append("value=\"" + fileName + "\" ");
        sb.append("/>");
        return sb.toString();
    }

    private void showStatistics() {
        ElapsedTimer elapsed = getElapsed();
        elapsed.showElapsed("Processed " + REPORT_INTERVAL_SCANS + " scans at " + XTandemUtilities.nowTimeString());
        // how much timeis in my code
        XMLUtilities.outputLine("Parsing time " + String.format("%7.2f", m_ParsingTime / 1000.0));
        XMLUtilities.outputLine("processing time " + String.format("%7.2f", m_CohortProcessingTime / 1000.0));
        XMLUtilities.outputLine("writing time " + String.format("%7.2f", m_KeyWriteTime / 1000.0) +
                " for " + m_KeyWriteCount + " writes");

        // how much timeis in my code

        elapsed.reset();

        m_CohortProcessingTime = 0;
        m_ParsingTime = 0;
        m_KeyWriteTime = 0;
        m_KeyWriteCount = 0;
        elapsed.reset();
    }

    protected void writeScansToMassAtCharge(final IMeasuredSpectrum value, final List<KeyValueObject<String, IMeasuredSpectrum>> holder,
                                            final String pId, double mass, int charge, String filename) {
        if (!isMassScored(mass))
            return;
        IScoringAlgorithm scorer = getApplication().getScorer();

        int[] limits = scorer.allSearchedMasses(mass);
        for (int j = 0; j < limits.length; j++) {
            int limit = limits[j];
            writeScanToMassAtCharge(limit, pId, value, charge, filename, holder);
        }
    }

    public static final String CHARGE_0_STRING = "precursorCharge=\"0\"";
    public static final String PRECURSOR_MZ_TAG_STRING = "<precursorMz ";

    protected void writeScanToMassAtCharge(int mass, String id, IMeasuredSpectrum value, int charge, String filename,
                                           List<KeyValueObject<String, IMeasuredSpectrum>> holder) {

        // Special code to store scans at mass for timing studies
//        if(ScoringReducer.STORING_SCANS ) {
//            if(!TestScoringTiming.SAVED_MASS_SET.contains(mass))
//                return;
//        }

        XTandemMain application = getApplication();
        SpectrumCondition sp = application.getSpectrumParameters();
        // if mass is too low or too high forget it
        if (sp != null && !sp.isMassScored(mass)) {
            incrementCounter("Performance", "Not Scored Mass");
            return;
        }
        long startTime = System.currentTimeMillis();
        String keyStr = String.format("%06d", mass);
        //    String valueStr = value.toString();
        int numberEntries = getDatabaseSize(mass);
        int maxScored = getMaxScoredPeptides();

//        // patch the charge to a known state matching the masses
//        String chargeString = "precursorCharge=\"" + charge + "\" ";
//        if (valueStr.contains(CHARGE_0_STRING)) {
//            valueStr = valueStr.replace(CHARGE_0_STRING, chargeString);
//        }
//        else {
//            valueStr = valueStr.replace(PRECURSOR_MZ_TAG_STRING, PRECURSOR_MZ_TAG_STRING + chargeString);
//        }
//

        int numberSeparateReducers = 1 + (numberEntries / maxScored);

        if (numberSeparateReducers == 1) {

            //   System.err.println("Sending mass " + mass + " for id " + id );
            holder.add(new KeyValueObject<String, IMeasuredSpectrum>(keyStr, value));
            m_KeyWriteCount++;
        }
        else {
            int chosen = RND.nextInt(numberSeparateReducers); // choose one reducer

            // too big split into multiple tasks
            int start = maxScored * chosen;
            int end = (maxScored + 1) * chosen;
            // while (numberEntries > 0) {
            String key = keyStr + ":" + start + ":" + end;
            start += maxScored;
            numberEntries -= maxScored;
            holder.add(new KeyValueObject<String, IMeasuredSpectrum>(keyStr, value));
            m_KeyWriteCount++;
            //  }
        }
        long elapsedProcessingTime = System.currentTimeMillis() - startTime;
        m_KeyWriteTime += elapsedProcessingTime;
    }

    protected void writeScansToMass(final IMeasuredSpectrum value, final List<KeyValueObject<String, IMeasuredSpectrum>> holder, final String pId, double mass) {
        IScoringAlgorithm scorer = getApplication().getScorer();
        int[] limits = scorer.allSearchedMasses(mass);
        for (int j = 0; j < limits.length; j++) {
            int limit = limits[j];

            if (isMassInteresting(limit))
                XTandemUtilities.breakHere();

            writeScanToMass(limit, pId, value, holder);
        }
    }

    private void saveMeasuredSpectrum(final RawPeptideScan scan) {
        final XTandemMain app = getApplication();
        final Scorer scorer = app.getScoreRunner();
        SpectrumCondition sc = scorer.getSpectrumCondition();
        final IScoringAlgorithm sa = scorer.getAlgorithm();
        ScoredScan ssc = new ScoredScan(scan);
        IMeasuredSpectrum scn = ssc.conditionScan(sa, sc);
        if (scn == null)
            return;
        ISpectrumPeak[] peaks = scn.getPeaks();
        if (peaks == null || peaks.length < 4)
            return;
        String id = scan.getId();
    }


    protected void writeScanToMass(int mass, String id, IMeasuredSpectrum value, List<KeyValueObject<String, IMeasuredSpectrum>> holder) {

        int numberEntries = getDatabaseSize(mass);

        int maxScored = getMaxScoredPeptides();
        numberEntries = maxScored - 1; // todo remove this forces one key
        // todo app back - we need to pick up this later
        if (false && numberEntries == 0)
            return; // we will find no peptides to score

        String keyStr = String.format("%06d", mass);
        long startTime = System.currentTimeMillis();
        if (numberEntries < maxScored) {    // few entries score in one task

            //   System.err.println("Sending mass " + mass + " for id " + id );
            holder.add(new KeyValueObject<String, IMeasuredSpectrum>(keyStr, value));
            m_KeyWriteCount++;
        }
        else {
            // too big split into multiple tasks
            int start = 0;
            while (numberEntries > 0) {
                String key = keyStr + ":" + start + ":" + (start + maxScored);
                start += maxScored;
                numberEntries -= maxScored;
                holder.add(new KeyValueObject<String, IMeasuredSpectrum>(key, value));
                m_KeyWriteCount++;
            }
        }
        // cost of writing
        long elapsedProcessingTime = System.currentTimeMillis() - startTime;
        m_KeyWriteTime += elapsedProcessingTime;

    }

    @Override
    public void cleanup(JavaSparkContext context) {
        super.cleanup(context);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
