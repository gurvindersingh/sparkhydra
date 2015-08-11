package com.lordjoe.distributed.spectrum;

import com.lordjoe.distributed.spark.accumulators.*;
import org.systemsbiology.xtandem.*;
import scala.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spectrum.MGFStringTupleToSpectrumTuple
 * User: Steve
 * Date: 9/24/2014
 */
public class MGFStringTupleToSpectrumTuple extends AbstractLoggingPairFlatMapFunction<Tuple2<String, String>, String, IMeasuredSpectrum> {
    private final ITandemScoringAlgorithm scorer;
    private final SpectrumCondition spectrumParameters;
    private final String url;

    public MGFStringTupleToSpectrumTuple(XTandemMain application) {
        if (application == null) {
            scorer = null;
            spectrumParameters = null;
            url = "";
        }
        else {
            scorer = application.getScorer();
            spectrumParameters = application.getSpectrumParameters();
            url = application.getSpectrumPath() ;

        }
    }

    public static final double MINIMUM_MASS = 150;

    /**
     * do work here
     *
     * @param t@return
     */
    @Override
    public Iterable<Tuple2<String, IMeasuredSpectrum>> doCall(final Tuple2<String, String> kv) throws Exception {
        List<Tuple2<String, IMeasuredSpectrum>> ret = new ArrayList<Tuple2<String, IMeasuredSpectrum>>();

        String s = kv._2(); // .toString();   // _2 is really a StringBuffer
        LineNumberReader inp = new LineNumberReader(new StringReader(s));
        IMeasuredSpectrum spectrum = XTandemUtilities.readMGFScan(inp, url);
        if (scorer != null) {
            // added spectral conditioning and normalization
              double minMass = MINIMUM_MASS;
            if(spectrumParameters.getfLowestMass() > 0)
                minMass = spectrumParameters.getfLowestMass();
            RawPeptideScan raw = (RawPeptideScan) spectrum;
            if (raw == null)
                return ret;

            IMeasuredSpectrum spec = spectrumParameters.normalizeSpectrum(raw, minMass);
           if (spec == null) {
               inp = new LineNumberReader(new StringReader(s));
               spectrum = XTandemUtilities.readMGFScan(inp, url);
                spec = spectrumParameters.normalizeSpectrum(raw, minMass);    // repeat to look at why
               return ret;
           }
     //       IMeasuredSpectrum conditioned = scorer.conditionSpectrum(spec, raw);
     //       IMeasuredSpectrum normalized = spectrumParameters.normalizeSpectrum(conditioned, minMass);

            ret.add(new Tuple2<String, IMeasuredSpectrum>(kv._1(), spec));
        }
        else {
            ret.add(new Tuple2<String, IMeasuredSpectrum>(kv._1(), spectrum));

        }
        return ret;
    }
}
