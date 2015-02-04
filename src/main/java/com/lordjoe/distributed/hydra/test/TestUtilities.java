package com.lordjoe.distributed.hydra.test;

import com.lordjoe.distributed.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.test.TestUtilities
 * User: Steve
 * Date: 2/3/2015
 */
public class TestUtilities {

    public static final String[] INTERESTING_PEPTIDES_STRS = { "VSYAVYNR"};
    public static final Set<String> INTERESTING_PEPTIDES = new HashSet<String>(Arrays.asList(INTERESTING_PEPTIDES_STRS));


    public static final String[] INTERESTING_SPECTRUM_STRS = { "131104_Berit_BSA2.17729.17729.2"};
    public static final Set<String> INTERESTING_SPECTRUMS = new HashSet<String>(Arrays.asList(INTERESTING_SPECTRUM_STRS));


    public static boolean isInterestingPeptide(IPolypeptide... pp) {
        for (int i = 0; i < pp.length; i++) {
            IPolypeptide ppx = pp[i];
             if(INTERESTING_PEPTIDES.contains(ppx.toString()))
                 return true;
        }
        return false;
    }

    public static boolean isInterestingSpectrum(IMeasuredSpectrum... spec) {
        for (int i = 0; i < spec.length; i++) {
            IMeasuredSpectrum sp = spec[i];
              if(INTERESTING_SPECTRUMS.contains(sp.getId()))
                  return true;
         }
         return false;

    }

    public static JavaRDD<IPolypeptide> findInterestingPeptides(JavaRDD<IPolypeptide> inp,final List<IPolypeptide> found)
    {
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

    public static JavaRDD<IMeasuredSpectrum> findInterestingSpectra(JavaRDD<IMeasuredSpectrum> inp,final List<IMeasuredSpectrum> found)
     {
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
            if(o instanceof IPolypeptide) {
                isInterestingPeptide((IPolypeptide)o);
                continue;
            }
            if(o instanceof IMeasuredSpectrum) {
                   isInterestingSpectrum((IMeasuredSpectrum) o);
                   continue;
               }
           }

    }
}
