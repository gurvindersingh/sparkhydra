package com.lordjoe.distributed.hydra.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.pepxml.*;
import org.systemsbiology.xtandem.scoring.*;

/**
 * com.lordjoe.distributed.hydra.scoring.PepXMLScoredScanWriter
 * User: Steve
 * Date: 10/20/2014
 */
public class PepXMLScoredScanWriter implements ScoredScanWriter {

    private final PepXMLWriter writer;



    public PepXMLScoredScanWriter(final PepXMLWriter pWriter) {
        writer = pWriter;
    }

    public PepXMLWriter getWriter() {
        return writer;
    }

    /**
     * write the start of a file
     *
     * @param out where to append
     * @param app appliaction data
     */
    @Override
    public void appendHeader(final Appendable out, final XTandemMain app) {
        String spectrumPath = app.getParameter("spectrum, path");
        String algo = "";
        ITandemScoringAlgorithm[] algorithms = app.getAlgorithms();
        if (algorithms.length > 0)
            algo = algorithms[0].getName();
        PepXMLWriter writer1 = getWriter();
        writer1.writePepXMLHeader(spectrumPath, algo, out);

    }

    /**
     * write the end of a file
     *
     * @param out where to append
     * @param app appliaction data
     */
    @Override
    public void appendFooter(final Appendable out, final XTandemMain app) {
        PepXMLWriter writer1 = getWriter();
        writer1.writePepXMLFooter(out);
     }

    /**
     * write one scan
     *
     * @param out  where to append
     * @param app  appliaction data
     * @param scan one scan
     */
    @Override
    public void appendScan(final Appendable out, final XTandemMain app, final IScoredScan scan) {
        PepXMLWriter writer1 = getWriter();
        writer1.writePepXML(scan, out);
    }
}
