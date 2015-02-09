package com.lordjoe.distributed.spectrum;

import com.lordjoe.distributed.*;
import org.systemsbiology.xtandem.*;
import scala.*;

import java.io.*;

/**
 * com.lordjoe.distributed.spectrum.MGFStringTupleToSpectrumTuple
 * User: Steve
 * Date: 9/24/2014
 */
public class MGFStringTupleToSpectrumTuple extends AbstractLoggingPairFunction<Tuple2<String, String>, String, IMeasuredSpectrum> {
    private final ITandemScoringAlgorithm scorer;
    private final SpectrumCondition spectrumParameters;

    public MGFStringTupleToSpectrumTuple(XTandemMain application) {
        if (application == null) {
            scorer = null;
            spectrumParameters = null;
        }
        else {
            scorer = application.getScorer();
            spectrumParameters = application.getSpectrumParameters();

        }
    }

    @Override
    public Tuple2<String, IMeasuredSpectrum> doCall(final Tuple2<String, String> kv) throws Exception {
        String s = kv._2(); // .toString();   // _2 is really a StringBuffer
        LineNumberReader inp = new LineNumberReader(new StringReader(s));
        IMeasuredSpectrum spectrum = XTandemUtilities.readMGFScan(inp, "");
        if (scorer != null) {
            // added spectral conditioning and normalization
            final double minMass = 150;
            RawPeptideScan raw = (RawPeptideScan) spectrum;
            IMeasuredSpectrum spec = spectrumParameters.normalizeSpectrum(raw, minMass);
            IMeasuredSpectrum conditioned = scorer.conditionSpectrum(spec, raw);

            return new Tuple2<String, IMeasuredSpectrum>(kv._1(), conditioned);
        }
        else {
            return new Tuple2<String, IMeasuredSpectrum>(kv._1(), spectrum);

        }
    }
}
