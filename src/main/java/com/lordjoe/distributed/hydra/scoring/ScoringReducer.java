package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.tandem.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.common.*;
import org.systemsbiology.sax.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.taxonomy.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.ScoringReducer
 * User: Steve
 * Date: 10/6/2014
 */
public class ScoringReducer extends AbstractTandemFunction implements ISingleOutputReducerFunction<String, IMeasuredSpectrum, String, IScoredScan>,
        IReducerFunction<String, IMeasuredSpectrum, String, IScoredScan>, SpectrumGenerationListener, INoticationListener {

    public static final String USE_SEPARATE_FILES_STRING = "full_tandem_output_path";
      public static final String ALGORITHMS_PROPERTY = "org.systemsbiology.algorithm";
      public static final String TURN_ON_SCAN_OUTPUT_PROPERTY = "org.systemsbiology.xtandem.SaveScansData";
      public static final String MULTIPLE_OUTPUT_FILES_PROPERTY = "org.systemsbiology.xtandem.MultipleOutputFiles";
      public static final String DO_NOT_COPY_FILES_PROPERTY = "org.systemsbiology.xtandem.DoNotCopyFilesToLocalMachine";
      public static final String HARDCODED_MODIFICATIONS_PROPERTY = "org.systemsbiology.xtandem.HardCodeModifications";
      public static final String INPUT_FILES_PROPERTY = "org.systemsbiology.xtandem.InputFiles";
      public static final String NUMBER_REMEMBERED_MATCHES = "org.systemsbiology.numberRememberedMatches";

    private ITaxonomy m_Taxonomy;
    private long m_MaxPeptides;
    private int m_Notifications;
    private Set<PeptideModification> m_Modifications;
    private boolean m_CreateDecoyPeptides;

    public ScoringReducer(XTandemMain app) {
        super(app);
    }

    public boolean isCreateDecoyPeptides() {
        return m_CreateDecoyPeptides;
    }

    public void setCreateDecoyPeptides(boolean createDecoyPeptides) {
        m_CreateDecoyPeptides = createDecoyPeptides;
    }


    @Override
    public void setup(JavaSparkContext context) {
        // read configuration lines

//        IAnalysisParameters ap = AnalysisParameters.getInstance();
//        ap.setJobName(context.getJobName());
        super.setup(context);

        XTandemMain application = getApplication();
        m_Taxonomy = application.getTaxonomy();

        boolean makeDecoys = application.getBooleanParameter(XTandemUtilities.CREATE_DECOY_PEPTIDES_PROPERTY, false);
        //setCreateDecoyPeptides(makeDecoys);

        boolean doHardCoded = application.getBooleanParameter( HARDCODED_MODIFICATIONS_PROPERTY, true);
        PeptideModification.setHardCodeModifications(doHardCoded);

        int NMatches = application.getIntParameter( NUMBER_REMEMBERED_MATCHES, XTandemHadoopUtilities.DEFAULT_CARRIED_MATCHES);
        XTandemHadoopUtilities.setNumberCarriedMatches(NMatches);


        Scorer scoreRunner = application.getScoreRunner();
        scoreRunner.addINoticationListener(this);
        scoreRunner.addSpectrumGenerationListener(this);
        // maybe do some logging

        //  scoreRunner.digest();

        String dir = application.getDatabaseName();
        if (dir != null) {
            Configuration entries = context.hadoopConfiguration();

            if(LibraryBuilder.USE_PARQUET_DATABASE)
                m_Taxonomy = new ParquetDatabaseTaxonomy(application, m_Taxonomy.getOrganism() );
            else {
                throw new UnsupportedOperationException("Fix This"); // ToDo
                //m_Taxonomy = new HadoopFileTaxonomy(application, m_Taxonomy.getOrganism() );
            }
        }
        else {
            // make sure the latest table is present
            throw new UnsupportedOperationException("we dropped databases for now"); // ToDo
            //guaranteeModifiedPeptideTable();
        }
        ScoringModifications scoringMods = application.getScoringMods();
        PeptideModification[] modifications = scoringMods.getModifications();
        m_Modifications = new HashSet<PeptideModification>(Arrays.asList(modifications));
    }

    /**
     * we may refactor the key in different ways int mass will always be encoded
     *
     * @param keyStr
     * @return
     */
    public static int keyStringToMass(String keyStr) {
        return Integer.parseInt(keyStr);
    }

    @Override
    public void onSpectrumGeneration(final ITheoreticalSpectrumSet spec) {
        incrementCounter("Performance", "TotalSpectra");
    }

    /**
     * notification of something
     *
     * @param data
     */
    @Override
    public void onNotification(final Object... data) {

    }

    /**
     * implement this A reducer is quaranteed to return one Key Value pair for every Key
     *
     * @param key
     * @param scan
     * @return
     */
    @Nonnull
    @Override
    public KeyValueObject<String, IScoredScan> handleValue(@Nonnull final String keyStr, @Nonnull final IMeasuredSpectrum scan) {

        MassPeptideInterval interval = new MassPeptideInterval(keyStr);
        int mass = interval.getMass();
        if (!interval.isUnlimited())
            System.err.println(interval.toString());

        // Special code to store scans at mass for timing studeies
        //        if (STORING_SCANS) {
        //            if (!TestScoringTiming.SAVED_MASS_SET.contains(mass))
        //                return;
        //        }

        int numberScans = 0;
        int numberScored = 0;
        int numberNotScored = 0;

        XTandemMain application = getApplication();

        // if we do not score this mass continue
        SpectrumCondition sp = application.getSpectrumParameters();

        int numberScoredPeptides = 0;

        ElapsedTimer et = new ElapsedTimer();
        try {
            IPolypeptide[] pps = getPeptidesOfExactMass(interval);


            pps = filterPeptides(pps); // drop non-complient peptides

            System.err.println(keyStr + " Number peptides = " + pps.length);

            if (isCreateDecoyPeptides()) {
                pps = addDecoyPeptides(pps);
            }

            //            pps = interval.filterPeptideList(pps);  // we may not use all peptides depending on the size
            numberScoredPeptides = pps.length;

            if (numberScoredPeptides == 0) {
                //noinspection SimplifiableIfStatement,PointlessBooleanExpression,ConstantConditions,RedundantIfStatement
                return new KeyValueObject(keyStr, new ScoredScan((RawPeptideScan) scan));
            }

            if (m_MaxPeptides < numberScoredPeptides) {
                m_MaxPeptides = numberScoredPeptides;
                System.err.println("Max peptides " + m_MaxPeptides + " for mass " + mass);
            }

            final XTandemMain app = application;
            final Scorer scorer = app.getScoreRunner();


            scorer.clearSpectra();
            scorer.clearPeptides();

            ITandemScoringAlgorithm[] alternateScoring = application.getAlgorithms();


            scorer.generateTheoreticalSpectra(pps);


            if (scan == null)
                return new KeyValueObject(keyStr, new ScoredScan((RawPeptideScan) scan));
            numberScans++;
            String id = scan.getId();


            incrementCounter("Performance", "TotalScans");

            if(alternateScoring.length != 1)
                throw new IllegalStateException("we can only handle one scoring Algorithm");


            ITandemScoringAlgorithm algorithm = alternateScoring[0];
            IScoredScan scoredScan =  algorithm.handleScan(scorer,(RawPeptideScan)scan, pps );

//            MultiScorer ms = new MultiScorer();
//            //              ms.addAlgorithm(scoredScan);
//            // run any other algorithms
//            for (int i = 0; i < alternateScoring.length; i++) {
//                ITandemScoringAlgorithm algorithm = alternateScoring[i];
//                scoredScan = algorithm.handleScan(scorer,(RawPeptideScan)scan, pps );
//                ms.addAlgorithm(scoredScan);
//            }

//
//            if (ms.isMatchPresent()) {
//                StringBuilder sb = new StringBuilder();
//                IXMLAppender appender = new XMLAppender(sb);
//
//                ms.serializeAsString(appender);
//                //     scoredScan.serializeAsString(appender);
//
//                @SuppressWarnings("ConstantConditions")
//                String outKey = ((OriginatingScoredScan) scoredScan).getKey();
//                while (outKey.length() < 8)
//                    outKey = "0" + outKey; // this causes order to be numeric
//                String value = sb.toString();
//                if (true)
//                    throw new UnsupportedOperationException("Fix This"); // ToDo
//                // writeKeyValue(outKey, scan, context);
//                numberScored++;
//            }
//            else {
//                // debug repeat
//                //    scoredScan = handleScan(scorer, scan, pps);
//                //   bestMatch = scoredScan.getBestMatch();
//                numberNotScored++;
//                // XTandemUtilities.outputLine("No score for " + id + " at mass " + mass);
//            }

            return new KeyValueObject(keyStr, scoredScan);

        }
        catch (RuntimeException e) {
            // look at any issues
            XTandemUtilities.breakHere();
            String message = e.getMessage();
            e.printStackTrace(System.err);
            throw e;

        }

    }

    /**
     * this is what a reducer does
     *
     * @param key
     * @param values
     * @param consumer @return iterator over mapped key values
     */
    @Nonnull
    @Override
    public void handleValues(@Nonnull final String keyStr, @Nonnull final Iterable<IMeasuredSpectrum> values, final IKeyValueConsumer<String, IScoredScan>... consumer) {
        int charge = 1;

        MassPeptideInterval interval = new MassPeptideInterval(keyStr);
        int mass = interval.getMass();
        if (!interval.isUnlimited())
            System.err.println(interval.toString());

        // Special code to store scans at mass for timing studeies
        //        if (STORING_SCANS) {
        //            if (!TestScoringTiming.SAVED_MASS_SET.contains(mass))
        //                return;
        //        }

        int numberScans = 0;
        int numberScored = 0;
        int numberNotScored = 0;

        XTandemMain application = getApplication();

        // if we do not score this mass continue
        SpectrumCondition sp = application.getSpectrumParameters();
        if (!sp.isMassScored(mass))
            return;


        // Special code to store scans at mass for timing stueies
        SequenceFile.Writer writer = null;

        int numberScoredPeptides = 0;

        ElapsedTimer et = new ElapsedTimer();
        try {
            IPolypeptide[] pps = getPeptidesOfExactMass(interval);


            pps = filterPeptides(pps); // drop non-complient peptides

            System.err.println("Number peptides = " + pps.length);

            if (isCreateDecoyPeptides()) {
                pps = addDecoyPeptides(pps);
            }

            //            pps = interval.filterPeptideList(pps);  // we may not use all peptides depending on the size
            numberScoredPeptides = pps.length;

            if (numberScoredPeptides == 0) {
                //noinspection SimplifiableIfStatement,PointlessBooleanExpression,ConstantConditions,RedundantIfStatement
                return;  // nothing to score
            }

            if (m_MaxPeptides < numberScoredPeptides) {
                m_MaxPeptides = numberScoredPeptides;
                System.err.println("Max peptides " + m_MaxPeptides + " for mass " + mass);
            }

            final XTandemMain app = application;
            final Scorer scorer = app.getScoreRunner();


            scorer.clearSpectra();
            scorer.clearPeptides();

            ITandemScoringAlgorithm[] alternateScoring = application.getAlgorithms();


            scorer.generateTheoreticalSpectra(pps);


            Iterator<IMeasuredSpectrum> textIterator = values.iterator();
            while (textIterator.hasNext()) {
                IMeasuredSpectrum scan = textIterator.next();


                if (scan == null)
                    return; // todo or is an exception proper
                numberScans++;
                String id = scan.getId();


                incrementCounter("Performance", "TotalScans");

                IScoredScan scoredScan = null; // handleScan(scorer, scan, pps);
                MultiScorer ms = new MultiScorer();
                //              ms.addAlgorithm(scoredScan);
                // run any other algorithms
                for (int i = 0; i < alternateScoring.length; i++) {
                    ITandemScoringAlgorithm algorithm = alternateScoring[i];
                    if (true)
                        throw new UnsupportedOperationException("Fix This"); // ToDo
                    //       scoredScan = algorithm.handleScan(scorer, scan, pps, context1);
                    ms.addAlgorithm(scoredScan);
                }


                if (ms.isMatchPresent()) {
                    StringBuilder sb = new StringBuilder();
                    IXMLAppender appender = new XMLAppender(sb);

                    ms.serializeAsString(appender);
                    //     scoredScan.serializeAsString(appender);

                    @SuppressWarnings("ConstantConditions")
                    String outKey = ((OriginatingScoredScan) scoredScan).getKey();
                    while (outKey.length() < 8)
                        outKey = "0" + outKey; // this causes order to be numeric
                    String value = sb.toString();
                    if (true)
                        throw new UnsupportedOperationException("Fix This"); // ToDo
                    // writeKeyValue(outKey, value, context);
                    numberScored++;
                }
                else {
                    // debug repeat
                    //    scoredScan = handleScan(scorer, scan, pps);
                    //   bestMatch = scoredScan.getBestMatch();
                    numberNotScored++;
                    // XTandemUtilities.outputLine("No score for " + id + " at mass " + mass);
                }


            }

            long elapsedMillisec = et.getElapsedMillisec();
            double millisecPerScan = elapsedMillisec / (double) (numberScans * numberScoredPeptides);
            String rateStr = String.format(" scans pre msec %6.2f", millisecPerScan);
            et.showElapsed("Finished mass " + mass +
                            " numberPeptides " + numberScoredPeptides +
                            " scored " + numberScored +
                            " not scored " + numberNotScored +
                            rateStr +
                            " at " + XTandemUtilities.nowTimeString()
            );

            XMLUtilities.outputLine(XMLUtilities.freeMemoryString());
            scorer.clearPeptides();
            scorer.clearSpectra();
            application.clearRetainedData();

            // special code to store scans fo rtiming later
            //            if (STORING_SCANS) {
            //                writer.close();
            //            }

        }
        catch (RuntimeException e) {
            // look at any issues
            XTandemUtilities.breakHere();
            String message = e.getMessage();
            e.printStackTrace(System.err);
            throw e;

        }

    }

    /**
     * add decoy peptides
     *
     * @param pps
     * @return
     */
    protected IPolypeptide[] addDecoyPeptides(IPolypeptide[] pps) {
        IPolypeptide[] ret = new IPolypeptide[pps.length * 2];
        for (int i = 0; i < pps.length; i++) {
            IPolypeptide pp = pps[i];
            ret[2 * i] = pp;
            ret[2 * i + 1] = pp.asDecoy();

        }
        return ret;
    }

    /**
     * this method removes any peptides that do not meet the current search criteria
     *
     * @param pPps
     * @return
     */
    protected IPolypeptide[] filterPeptides(final IPolypeptide[] pPps) {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        XTandemMain application = getApplication();
        IPeptideDigester digester = application.getDigester();
        for (int i = 0; i < pPps.length; i++) {
            IPolypeptide pp = pPps[i];
            if (isAcceptablePeptide(application, digester, pp))   // the work here
                holder.add(pp);
            //            else {
            //                if (!XTandemUtilities.isProbablySemitryptic(pp)) {
            //                    isAcceptablePeptide(application, digester, pp);
            //                    System.out.println("Dropped peptide " + pp);
            //                }
            //            }
        }

        IPolypeptide[] ret = new IPolypeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * test if a peptide matches the current search criteria
     *
     * @param pApplication
     * @param digester
     * @param pp
     * @return
     */
    protected boolean isAcceptablePeptide(final XTandemMain pApplication, final IPeptideDigester digester, final IPolypeptide pp) {
        boolean semiTryptic = digester.isSemiTryptic();
        //        if (!semiTryptic) {
        //            if (XTandemUtilities.isProbablySemitryptic(pp))
        //                return false;
        //        }
        // are there optional modifications
        if (pp.isModified()) {
            IModifiedPeptide mp = (IModifiedPeptide) pp;
            // if we have no mofidications then reject
            if (m_Modifications.isEmpty())
                return false;
            // if any modifications in the peptide are NOT in the current search set then reject
            return XTandemUtilities.isModificationsCompatable(mp, m_Modifications);
            //  pApplication.getDigestandModificationsString()
        }
        return true;
    }
    //
    //    protected IScoredScan handleScan(final Scorer scorer, final ITandemScoringAlgorithm pAlgorithm, final RawPeptideScan scan, final IPolypeptide[] pPps) {
    //        String id = scan.getId();
    //        OriginatingScoredScan scoring = new OriginatingScoredScan(scan);
    //        scoring.setAlgorithm(pAlgorithm.getName());
    //        IonUseCounter counter = new IonUseCounter();
    //        final ITheoreticalSpectrumSet[] tss = scorer.getAllSpectra();
    //
    //        int numberDotProducts = scoreScan(scorer, pAlgorithm, counter, tss, scoring);
    //        getContext().getCounter("Performance", "TotalDotProducts").increment(numberDotProducts);
    //        return scoring;
    //    }

    public static int gNumberNotScored = 0;


    public static void showPeaks(final ISpectrum pConditionedScan, final PrintWriter out) {
        ISpectrumPeak[] peaks = pConditionedScan.getPeaks();
        int lastPeak = 0;
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            double mz = peak.getMassChargeRatio();
            int ipeak = (int) (mz + 0.5);
            if (ipeak == lastPeak)
                continue;
            if (i > 0)
                out.print(",");
            out.print(Integer.toString(ipeak));
        }
        out.println();
    }


//    protected boolean doNotHandleScans(final Iterable<Text> values) {
//        //     JXTandemLauncher.logMessage("Nothing to score at " + mass);
//        Iterator<Text> textIterator = values.iterator();
//        while (textIterator.hasNext()) {
//            Text text = textIterator.next();
//            String textStr = text.toString();
//            RawPeptideScan scan = XTandemHadoopUtilities.readScan(textStr, null);
//            if (scan == null)
//                return true;
//
//            String id = scan.getId();
//            //          JXTandemLauncher.logMessage("Not scoring scan " + id + " at mass" + mass);
//        }
//        return false;
//    }
//
//    public void writeKeyValue(String key, String value, Reducer.Context context) {
//        Text onlyKey = getOnlyKey();
//        Text onlyValue = getOnlyValue();
//        onlyKey.set(key);
//        onlyValue.set(value);
//        try {
//            context.write(onlyKey, onlyValue);
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//

    protected IScoredScan handleScan(Scorer scorer, RawPeptideScan scan, IPolypeptide[] pPps) {
        String id = scan.getId();
        OriginatingScoredScan scoring = new OriginatingScoredScan(scan);
        IonUseCounter counter = new IonUseCounter();
        final ITheoreticalSpectrumSet[] tss = scorer.getAllSpectra();

        int numberDotProducts = scorer.scoreScan(counter, tss, scoring);
        incrementCounter("Performance", "TotalDotProducts", numberDotProducts);

        //        if (m_Logger != null) {
        //            for (int i = 0; i < tss.length; i++) {
        //                ITheoreticalSpectrumSet ts = tss[i];
        //                if (scorer.isTheoreticalSpectrumScored(scoring, ts))
        //                    appendScan(scan, ts, scoring);
        //
        //            }
        //        }

        return scoring;
    }


    protected IPolypeptide[] getPeptidesOfExactMass(MassPeptideInterval interval) {
        final XTandemMain application = getApplication();
        boolean semiTryptic = application.isSemiTryptic();
        IPolypeptide[] st = m_Taxonomy.getPeptidesOfExactMass(interval, semiTryptic);
        //        if (m_Taxonomy instanceof JDBCTaxonomy) {
        //            IPolypeptide[] not_st = ((JDBCTaxonomy) m_Taxonomy).findPeptidesOfMassIndex( interval, semiTryptic);
        //            if (st.length != not_st.length)
        //                XTandemUtilities.breakHere();
        //
        //        }
        return st;
    }

    protected void cleanup() {
    }
}
