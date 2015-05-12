package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;
import org.xml.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.XTandemScoringHandler
 * User: steven
 * Date: 6/22/11
 */
public class XTandemScoringHandler extends AbstractXTandemElementSaxHandler<List<ScoredScan>> implements ITopLevelSaxHandler {
    public static final XTandemScoringHandler[] EMPTY_ARRAY = {};


    public static final String TAG = "bioml";

    private ScanScoringReport m_Report;
    private final Map<String, ScoredScan> m_Scans = new HashMap<String, ScoredScan>();

    public XTandemScoringHandler(ScoringProcesstype type) {
        super(TAG, (DelegatingSaxHandler) null);
        m_Report = new ScanScoringReport(ScoringProcesstype.XTandem);
        setElementObject(new ArrayList<ScoredScan>());
    }

    public XTandemScoringHandler() {
        this(ScoringProcesstype.XTandem);
    }

    public ScanScoringReport getReport() {
        return m_Report;
    }

    public void setReport(final ScanScoringReport report) {
        m_Report = report;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if ("group".equals(qName)) {
            BiomlScanReportHandler handler = new BiomlScanReportHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        if ("dot_product".equals(qName)) {
            List<ScoredScan> elementObject = getElementObject();
            DotProductScoringHandler handler = new DotProductScoringHandler(this, getReport());
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        if ("total".equals(qName)) {
            return;
        }
        if ("file".equals(qName)) {
            return;
        }

        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("dot_product".equals(el)) {
            ISaxHandler handler1 = getHandler().popCurrentHandler();
            if (handler1 instanceof DotProductScoringHandler) {
                DotProductScoringHandler handler = (DotProductScoringHandler) handler1;
            }
            return;
        }
        if ("group".equals(el)) {
            ISaxHandler iSaxHandler = getHandler().popCurrentHandler();
            BiomlScanReportHandler handler = (BiomlScanReportHandler) iSaxHandler;
            ScoredScan scan = handler.getElementObject();

            List<ScoredScan> report = getElementObject();
            report.add(scan);
            return;
        }

        if ("total".equals(el)) {
            return;
        }
        if ("file".equals(el)) {
            return;
        }
        super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        ScanScoringReport report = getReport();
        List<ScoredScan> scans = getElementObject();
        for (ScoredScan scan : scans) {
            String id = scan.getId();
            if (id != null) {
                if (m_Scans.containsKey(id)) {
                    possiblyReplaceBestScanatId(id, scan);
                } else {
                    m_Scans.put(id, scan);
                }
            } else {
                showBadScan(scan);
            }
        }
    }

    protected void possiblyReplaceBestScanatId(final String id, final ScoredScan scan) {
        ScoredScan current = m_Scans.get(id);
        ISpectralMatch currentBest = current.getBestMatch();
        if (currentBest == null) {
            m_Scans.put(id, scan);
            return;
        }
        ISpectralMatch scanBest = scan.getBestMatch();
        if (scanBest == null) {
            return;
        }
        double score = currentBest.getHyperScore();
        double scanScore = scanBest.getHyperScore();
        if (scanScore > score)
            m_Scans.put(id, scan);
    }

    /**
     * who knows how we got here
     *
     * @param scan
     */
    private void showBadScan(final ScoredScan scan) {

        if (false) {
            throw new UnsupportedOperationException("Fix This"); // ToDo
        }


    }

    public Map<String, ScoredScan> getScans() {
        return m_Scans;
    }


    /**
     * @param in
     * @param ids
     * @return
     */
    public static RawPeptideScan[] readMGFScans(File in, Set<String> ids) {

        try {
            InputStream is = new FileInputStream(in);
            return readMGFScans(ids, is);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    public static RawPeptideScan[] readMGFScans(Set<String> ids, InputStream is) {
        List<RawPeptideScan> holder = new ArrayList<RawPeptideScan>();
        String url = "";
        LineNumberReader inp = new LineNumberReader(new InputStreamReader(is));
        RawPeptideScan scan = XTandemUtilities.readMGFScan(inp, url);
        while (scan != null) {
            //         if (line.startsWith("BEGIN IONS")) {
            if (ids.contains(scan.getId()))
                holder.add(scan);
            //           }
            scan = XTandemUtilities.readMGFScan(inp, url);
        }
        RawPeptideScan[] ret = new RawPeptideScan[holder.size()];
        holder.toArray(ret);
        return ret;

    }


    public static RawPeptideScan[] readMGFScans(InputStream is) {
        List<RawPeptideScan> holder = new ArrayList<RawPeptideScan>();
        String url = "";
        LineNumberReader inp = new LineNumberReader(new InputStreamReader(is));
        RawPeptideScan scan = XTandemUtilities.readMGFScan(inp, url);
        while (scan != null) {
            holder.add(scan);
            scan = XTandemUtilities.readMGFScan(inp, url);
        }
        RawPeptideScan[] ret = new RawPeptideScan[holder.size()];
        holder.toArray(ret);
        return ret;

    }


    /**
     * When passed an XTandem Score writes out a mgf file with ".mgf" appended to the original name
     *
     * @param args 1 XTandem scoring file
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String xTandemFile = null;
        String mgfFile = null;

        Set<String> ids = new HashSet<String>();

        if (args.length < 2) {
            System.out.println("Usage <tandem file> <input mgf file>");
            return;
        }

        //       if (args.length > 0)
        xTandemFile = args[0];
        mgfFile = args[1];
        //     else
        //           xTandemFile = FileUtilities.getLatestFileWithExtension(".t.txt").getName();
        XTandemScoringHandler handler = new XTandemScoringHandler();
        InputStream is = new FileInputStream(xTandemFile);
        XTandemUtilities.parseFile(is, handler, xTandemFile);

        // read the scans
        List<ScoredScan> elementObjectx = handler.getElementObject();
        // filter bad
        List<ScoredScan> scans = new ArrayList<ScoredScan>();
        for (ScoredScan s : elementObjectx) {
            if (s.getRaw() != null && s.getId() != null) {
                scans.add(s);
                ids.add(s.getId());
            }
        }

        RawPeptideScan[] realScans = readMGFScans(new File(mgfFile), ids);
        File outFile = new File(xTandemFile + ".mgf");
        PrintWriter out = new PrintWriter(new FileWriter(outFile));

        for (RawPeptideScan raw : realScans) {
            raw.serializeMGF(out);
        }

        out.close();
        if (true)
            return;


//        ScoredScan[] ret = new ScoredScan[scans.size()];
//        scans.toArray(ret);
//        // sort by id
//        Collections.sort(scans, OriginatingScoredScan.ID_COMPARISON);
//
//
//
//        for (ScoredScan scn : scans) {
//            RawPeptideScan raw = scn.getRaw();
//            if (raw != null)
//                raw.serializeMGF(out);
//        }
//
//        out.close();
//        if (true)
//            return;

        ScanScoringReport report = handler.getReport();
        //    if(true)
        //          throw new UnsupportedOperationException("Need to patch report"); // ToDo

        if (!report.equivalent(report))
            throw new IllegalStateException("problem"); // ToDo change
        int totalScores = report.getTotalScoreCount();
        IScanScoring[] scanScoring = report.getScanScoring();


        for (int j = 0; j < scanScoring.length; j++) {
            IScanScoring scoring = scanScoring[j];
            XMLUtilities.outputLine("Scored " + scoring.getId());
            ITheoreticalScoring[] tss = scoring.getScorings();
            TheoreticalScoring.sortByKScore(tss);
            if (tss.length > 0) {
                XMLUtilities.outputLine("Scored   " + tss[0] + " best Score " + tss[0].getTotalKScore());
            }
            if (tss.length > 1) {
                XMLUtilities.outputLine("Scored Next   " + tss[1] + " next best Score " + tss[1].getTotalKScore());
            }
        }


    }
}
