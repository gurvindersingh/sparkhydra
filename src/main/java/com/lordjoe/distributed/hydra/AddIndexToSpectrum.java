package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import org.systemsbiology.xtandem.*;
import scala.*;

import java.lang.*;

/**
* com.lordjoe.distributed.hydra.AddIndexToSpectrum
* User: Steve
* Date: 3/11/2015
*/
public class AddIndexToSpectrum extends AbstractLoggingFunction<Tuple2<IMeasuredSpectrum, java.lang.Long>, IMeasuredSpectrum> {
    @Override
    public IMeasuredSpectrum doCall(final Tuple2<IMeasuredSpectrum, java.lang.Long> v1) throws Exception {
        IMeasuredSpectrum spec = v1._1();
        long index = v1._2();
        setIndex(  spec,(int)( index + 1));

        if(spec.getIndex() != index + 1) throw new IllegalStateException("index not set");
        return spec;
    }

    public static void setIndex(IMeasuredSpectrum spec,int index)    {
        if(spec instanceof ScoringMeasuredSpectrum)  {
            ScoringMeasuredSpectrum realSpectrum = (ScoringMeasuredSpectrum)spec;
            ISpectralScan scanData = realSpectrum.getScanData();
            RawPeptideScan rs = (RawPeptideScan)scanData;
            rs.setScanNumber(index);
            return;
        }
        throw new IllegalStateException("cannot handle spectrum of class " + spec.getClass());
    }
}
