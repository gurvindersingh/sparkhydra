package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.spark.accumulators.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

/**
* com.lordjoe.distributed.hydra.scoring.AppendScanStringToWriter
* User: Steve
* Date: 3/11/2015
*/
public class AppendScanTupleStringToWriter extends AbstractLoggingFunction<Tuple2<Integer,IScoredScan>, String> {
    private final ScoredScanWriter writer;
    private final XTandemMain application;

    public AppendScanTupleStringToWriter(final ScoredScanWriter pWriter, XTandemMain app) {
        writer = pWriter;
        application = app;
    }

    @Override
    public String doCall(final Tuple2<Integer,IScoredScan> v1) throws Exception {
        StringBuilder sb = new StringBuilder();
        writer.appendScan(sb, application, v1._2());
        return sb.toString();
    }
}
