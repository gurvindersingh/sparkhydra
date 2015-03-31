package com.lordjoe.distributed.hydra.test;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import scala.*;

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

    /**
     *   set logging on or off and if set off return the log and clear it
     *   Logging is only used for debugginh
      *
     * @param application  application affected
     * @param log   tru if logging is turned on
     * @return null unless setting log off from an on state
     */
    public static String setLogCalculations(XTandemMain application,boolean log)
    {
         String s = application.getParameter(LOG_CALCULATIONS_PROPERTY) ;
        if(log)  {
            if(s != null)
                return null;
            application.setParameter(LOG_CALCULATIONS_PROPERTY,"true");
            return null;
        }
        else{
            if(s == null)
                return null;
            application.setParameter(LOG_CALCULATIONS_PROPERTY,"true");
            String ret =  application.getLog();
            application.clearLog();
            return ret;
        }
    }


    public static boolean isLogCalculations(IParameterHolder application )
      {
          String s = application.getParameter(LOG_CALCULATIONS_PROPERTY) ;
          return s != null;
    }

    public static void appendLog(IParameterHolder application,String added)
    {
        ((XTandemMain)application).appendLog(added); // todo cast is bad but Scorer only has a IParameterHolder
    }



    public static void savePeptideKey(final List<Tuple2<BinChargeKey, IPolypeptide>> pHolder) {
        peptideKeys.addAll(pHolder);
    }

    public static void saveSpectrumKey(final List< Tuple2<BinChargeKey, IMeasuredSpectrum>> holder) {
        for (Tuple2<BinChargeKey, IMeasuredSpectrum> added : holder) {
             spectrumKeys.add(added);
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


    public static final String[] INTERESTING_PEPTIDES_STRS = {
            //   "FCYVTEEGDWITKPLPFKK",
            //    "EWDFSEEQPEITIDEKKLAK",
            "KGGELASAVHAYTKTGDPSMR",

    };
    public static final Set<String> INTERESTING_PEPTIDES = new HashSet<String>(Arrays.asList(INTERESTING_PEPTIDES_STRS));


    public static final String[] INTERESTING_SPECTRUM_STRS = {
      //      "131104_Berit_BSA2.16056.16056.4",
            "131104_Berit_BSA2.11526.11526.4",
//            "131104_Berit_BSA2.8368.8368.4",
//            "31104_Berit_BSA2.10734.10734.2",
//            "131104_Berit_BSA2.15845.15845.4",
//            "131104_Berit_BSA2.9243.9243.3",
//            "131104_Berit_BSA2.15907.15907.4",
//            "131104_Berit_BSA2.8402.8402.4",
//            "131104_Berit_BSA2.22243.22243.3",
//            "131104_Berit_BSA2.8404.8404.4",   //10
//            "131104_Berit_BSA2.15512.15512.4",
//            "131104_Berit_BSA2.12537.12537.4",
//            "131104_Berit_BSA2.8734.8734.4",
//            "131104_Berit_BSA2.9292.9292.3",
//            "131104_Berit_BSA2.11975.11975.2",   // 15
//            "131104_Berit_BSA2.15755.15755.4",
//            "131104_Berit_BSA2.9991.9991.4",
//            "131104_Berit_BSA2.18064.18064.3",
//            "131104_Berit_BSA2.10106.10106.2",
//            "131104_Berit_BSA2.15516.15516.4",


            //     "131104_Berit_BSA2.13178.13178.3",
            //    "131104_Berit_BSA2.17729.17729.2",     //  sequence=AVPGAIVR score 550   ;
            //     "13697",     //  sequence=AVPGAIVR score 550   131104_Berit_BSA2.17729.17729.2";
            //      "62963", // sequence=SVLRPDVDLR   score 720
            //    "30788" //   sequence=Q[-17.026]TLVAQGTLR    hyperscore="629"

    };

    public static final Set<String> INTERESTING_SPECTRUMS = new HashSet<String>(Arrays.asList(INTERESTING_SPECTRUM_STRS));


    public static boolean isInterestingScoringPair(IPolypeptide  pp,IMeasuredSpectrum  spec) {
        boolean b = isInterestingPeptide(pp) && isInterestingSpectrum(spec);
        if(b)
            return b;
        return false;
    }


    public static boolean isInterestingPeptide(IPolypeptide... pp) {
        for (int i = 0; i < pp.length; i++) {
            IPolypeptide ppx = pp[i];
            String completeString = ppx.toString();
            if(ppx.isModified())    {
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

    /**
     * call this function when you want a good place to break
     */
    public static void breakHere() {
        int i = 0;i++;
     }
}
