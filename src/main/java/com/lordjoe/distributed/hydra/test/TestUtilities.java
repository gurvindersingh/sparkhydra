package com.lordjoe.distributed.hydra.test;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.api.java.function.Function;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import com.lordjoe.distributed.hydra.comet.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;
import scala.Long;

import java.io.*;
import java.lang.Boolean;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.test.TestUtilities
 * User: Steve
 * Date: 2/3/2015
 */
public class TestUtilities {

    private final static List<Tuple2<BinChargeKey, IPolypeptide>> peptideKeys = new ArrayList<Tuple2<BinChargeKey, IPolypeptide>>();
    private final static List<Tuple2<BinChargeKey, IMeasuredSpectrum>> spectrumKeys = new ArrayList<Tuple2<BinChargeKey, IMeasuredSpectrum>>();

    public static final String LOG_CALCULATIONS_PROPERTY = "com.lordjoe.distributed.hydra.test.TestUtilities.LogCalculations";


    private static PrintWriter caseLogger;

    public static void setCaseLogger(PrintWriter wtr) {
        if (caseLogger != null)
            throw new IllegalStateException("can only set caselogger once");
        caseLogger = wtr;
    }

    public static void closeCaseLoggers() {
        if (caseLogger != null)
            caseLogger.close();
    }

    public static boolean isCaseLogging() {
        return (caseLogger != null);
    }

    public static void logCase(String data) {
        if (caseLogger != null)
            caseLogger.println(data);

    }

    /**
     * set logging on or off and if set off return the log and clear it
     * Logging is only used for debugginh
     *
     * @param application application affected
     * @param log         tru if logging is turned on
     * @return null unless setting log off from an on state
     */
    public static String setLogCalculations(XTandemMain application, boolean log) {
        String s = application.getParameter(LOG_CALCULATIONS_PROPERTY);
        if (log) {
            if (s != null)
                return null;
            application.setParameter(LOG_CALCULATIONS_PROPERTY, "true");
            return null;
        } else {
            if (s == null)
                return null;
            application.setParameter(LOG_CALCULATIONS_PROPERTY, "true");
            String ret = application.getLog();
            application.clearLog();
            return ret;
        }
    }


    public static boolean isLogCalculations(IParameterHolder application) {
        String s = application.getParameter(LOG_CALCULATIONS_PROPERTY);
        return s != null;
    }

    public static void appendLog(IParameterHolder application, String added) {
        ((XTandemMain) application).appendLog(added); // todo cast is bad but Scorer only has a IParameterHolder
    }


    public static void savePeptideKey(final List<Tuple2<BinChargeKey, IPolypeptide>> pHolder) {
        peptideKeys.addAll(pHolder);
    }

    public static <T extends IMeasuredSpectrum> void saveSpectrumKey(final List<Tuple2<BinChargeKey, T>> holder) {
        for (Tuple2<BinChargeKey, T> added : holder) {
            spectrumKeys.add((Tuple2<BinChargeKey, IMeasuredSpectrum>) added);
        }
    }

    public static void writeSavedKeysAndSpectra() {
        if (peptideKeys.isEmpty() && spectrumKeys.isEmpty())
            return;
        for (Tuple2<BinChargeKey, IPolypeptide> peptideKey : peptideKeys) {
            System.out.println(peptideKey._1() + " => " + peptideKey._2());
        }
        System.out.println("==================================================");
        for (Tuple2<BinChargeKey, IMeasuredSpectrum> spectrumKey : spectrumKeys) {
            System.out.println(spectrumKey._1() + " => " + spectrumKey._2());

        }
    }


    public static final String[] INTERESTING_PROTEIN_IDS = {
            "Q29RL1-2_REVERSED",


    };
    public static final Set<String> INTERESTING_PROTEINS = new HashSet<String>(Arrays.asList(INTERESTING_PROTEIN_IDS));


    public static final String[] INTERESTING_PEPTIDES_STRS = {
            //   "FCYVTEEGDWITKPLPFKK",
            "YSKKSSEDGSPTPGK",
            "RLEWENWEYSR",
            "LDEIDMSQELFKEK",
//            "QEPERNECFLSHKDDSPDLPK",
//            "NECFLSHKDDSPDLPK",
//            "LKPDPNTLCDEFK",
//            "RHPYFYAPELLYYANK",
//            "YNGVFQECCQAEDK",


    };
    public static final Set<String> INTERESTING_PEPTIDES = new HashSet<String>(Arrays.asList(INTERESTING_PEPTIDES_STRS));


    public static final String[] INTERESTING_SPECTRUM_STRS = {
            //      "131104_Berit_BSA2.16056.16056.4",
            "000000009075",
//            "131104_Berit_BSA2.8368.8368.4",
//            "31104_Berit_BSA2.10734.10734.2",
//            "131104_Berit_BSA2.15845.15845.4",
//            "131104_Berit_BSA2.9243.9243.3",


    };

    public static final Set<String> INTERESTING_SPECTRUMS = new HashSet<String>(Arrays.asList(INTERESTING_SPECTRUM_STRS));


    public static boolean isInterestingScoringPair(IPolypeptide pp, IMeasuredSpectrum spec) {
        boolean b = isInterestingPeptide(pp) && isInterestingSpectrum(spec);
        if (b)
            return b;
        return false;
    }

    public static boolean isInterestingProtein(IProtein prot) {
        final String id = prot.getId();
        if (INTERESTING_PROTEINS.contains(id))
            return true;
        return false;
    }


    public static boolean isInterestingPeptide(IPolypeptide... pp) {
        for (int i = 0; i < pp.length; i++) {
            IPolypeptide ppx = pp[i];
            String completeString = ppx.toString();
            if (ppx.isModified()) {
                String sequence = ppx.getSequence();
                if (INTERESTING_PEPTIDES.contains(sequence))
                    return true;

            }
            if (INTERESTING_PEPTIDES.contains(completeString))
                return true;
        }
        return false;
    }

    public static boolean isInterestingSpectrum(IMeasuredSpectrum... spec) {
        for (int i = 0; i < spec.length; i++) {
            IMeasuredSpectrum sp = spec[i];
            String id = sp.getId();
            if (INTERESTING_SPECTRUMS.contains(id))
                return true;
        }
        return false;

    }

    public static JavaRDD<IPolypeptide> findInterestingPeptides(JavaRDD<IPolypeptide> inp, final List<IPolypeptide> found) {
        inp = SparkUtilities.persist(inp);
        JavaRDD<IPolypeptide> filter = inp.filter(new AbstractLoggingFunction<IPolypeptide, Boolean>() {
            @Override
            public Boolean doCall(final IPolypeptide v1) throws Exception {
                return TestUtilities.isInterestingPeptide(v1);
            }
        });
        found.addAll(filter.collect());
        return inp;
    }

    public static JavaRDD<IMeasuredSpectrum> findInterestingSpectra(JavaRDD<IMeasuredSpectrum> inp, final List<IMeasuredSpectrum> found) {
        inp = SparkUtilities.persist(inp);
        JavaRDD<IMeasuredSpectrum> filter = inp.filter(new AbstractLoggingFunction<IMeasuredSpectrum, Boolean>() {
            @Override
            public Boolean doCall(final IMeasuredSpectrum v1) throws Exception {
                return TestUtilities.isInterestingSpectrum(v1);
            }
        });
        found.addAll(filter.collect());
        return inp;
    }

    public static void examineInteresting(final List pCollect) {
        for (Object o : pCollect) {
            if (o instanceof IPolypeptide) {
                isInterestingPeptide((IPolypeptide) o);
                continue;
            }
            if (o instanceof IMeasuredSpectrum) {
                isInterestingSpectrum((IMeasuredSpectrum) o);
                continue;
            }
        }

    }

    public static List<SpectrumBinnedScore> activeValues(float[] all) {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        for (int i = 0; i < all.length; i++) {
            float v = all[i];
            if (Math.abs(v) > 0.001) {
                holder.add(new SpectrumBinnedScore(i, v));
            }
        }
        return holder;

    }

    /**
     * call this function when you want a good place to break
     */
    public static void breakHere() {
        int i = 0;
        i++;
    }

    public static JavaPairRDD<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>> saveInterestingPairs(JavaPairRDD<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>> bySpectrumId) {
        bySpectrumId = SparkUtilities.persist(bySpectrumId);

        final Map<String, Object> counts = bySpectrumId.countByKey();

        long maxCount = -1;
        String biggestKey = null;
        for (String s : counts.keySet()) {
            final Object o = counts.get(s);
            if (o instanceof java.lang.Long) {
                long test = ((java.lang.Long) o);
                if (test > maxCount) {
                    maxCount = test;
                    biggestKey = s;
                }
            }
        }

        if (biggestKey != null) {
            saveBiggestKey(bySpectrumId, biggestKey);
        }
        return bySpectrumId;
    }

    public static void saveBiggestKey(JavaPairRDD<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>> bySpectrumId, String biggestKey) {
        final String acceptKey = biggestKey;

        JavaPairRDD<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>> onlyBiggest = bySpectrumId.filter(new Function<Tuple2<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>>, Boolean>() {
            @Override
            public Boolean call(Tuple2<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>> x) throws Exception {
                return x._1().equals(acceptKey);
            }
        });
        final List<Tuple2<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>>> collect = onlyBiggest.collect();

        Set<IPolypeptide> peptides = new HashSet<IPolypeptide>();
        Map<String, IMeasuredSpectrum> spectraMap = new HashMap<String, IMeasuredSpectrum>();
        for (Tuple2<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>> v1 : collect) {
            final Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan> v2 = v1._2();
            IPolypeptide pp = v2._1().getPeptide();
            peptides.add(pp);
            IScoredScan scan = v2._2();
            spectraMap.put(scan.getId(), scan.getRaw());
        }

        saveSamplePeptides(acceptKey, peptides);
        saveSampleSpectrums(acceptKey, spectraMap.values());
    }

    private static void saveSampleSpectrums(String acceptKey, Collection<IMeasuredSpectrum> values) {
        String fileName = acceptKey + ".mgf";
        PrintWriter out = SparkUtilities.getHadoopPrintWriter(fileName);
        for (IMeasuredSpectrum value : values) {
            if (value instanceof RawPeptideScan) {
                RawPeptideScan rs = (RawPeptideScan) value;
                rs.appendAsMGF(out);
            }
        }
        out.close();
    }

    private static void saveSamplePeptides(String acceptKey, Set<IPolypeptide> peptides) {
        String fileName = acceptKey + ".peptide";
        PrintWriter out = SparkUtilities.getHadoopPrintWriter(fileName);
        for (IPolypeptide peptide : peptides) {
            out.println(peptide);
        }
         out.close();
    }


}
