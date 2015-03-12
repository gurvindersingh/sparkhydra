package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;

/**
* com.lordjoe.distributed.hydra.scoring.AppendScanStringToWriter
* User: Steve
* Date: 3/11/2015
*/
public class AppendScanStringToWriter extends AbstractLoggingFunction<IScoredScan, String> {
    private final ScoredScanWriter writer;
    private final XTandemMain application;

    public AppendScanStringToWriter(final ScoredScanWriter pWriter, XTandemMain app) {
        writer = pWriter;
        application = app;
    }

    @Override
    public String doCall(final  IScoredScan  v1) throws Exception {
        StringBuilder sb = new StringBuilder();
        writer.appendScan(sb, application, v1);
        return sb.toString();
    }
}
