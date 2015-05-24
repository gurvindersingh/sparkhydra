package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.utilities.FileUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.IMeasuredSpectrum;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.ionization.IonType;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.PeptideModification;
import org.systemsbiology.xtandem.peptide.PeptideModificationRestriction;
import org.systemsbiology.xtandem.peptide.Polypeptide;
import org.systemsbiology.xtandem.testing.MZXMLReader;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometTestingUtilities
 *
 * @author Steve Lewis
 * @date 5/22/2015
 */
public class CometTestingUtilities {
    public static CometTestingUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = CometTestingUtilities.class;

    public static final PeptideModification[] M_ONLY =
            {
                    PeptideModification.fromString("15.9949@M", PeptideModificationRestriction.Global, false)
            };
    public static final PeptideModification[] MSTV_ONLY =
            {
                    PeptideModification.fromString("15.9949@M", PeptideModificationRestriction.Global, false),
                    PeptideModification.fromString("79.966331@T", PeptideModificationRestriction.Global, false),
                    PeptideModification.fromString("79.966331@S", PeptideModificationRestriction.Global, false),
                    PeptideModification.fromString("79.966331@Y", PeptideModificationRestriction.Global, false),
            };
    public static final PeptideModification[] MS_ONLY =
            {
                    PeptideModification.fromString("15.9949@M", PeptideModificationRestriction.Global, false),
                    PeptideModification.fromString("79.966331@S", PeptideModificationRestriction.Global, false),
            };

    public static Map<Integer, RawPeptideScan> getScanMapFromResource(String resource) {
        List<RawPeptideScan> scans = CometTestingUtilities.getAllScanFromMZXMLResource(resource); //"/eg3_20/eg3_20.mzXML");
        Map<Integer, RawPeptideScan> mapped = new HashMap<Integer, RawPeptideScan>();
        for (RawPeptideScan scan : scans) {
            String id = scan.getId();
            mapped.put(new Integer(id), scan);
        }
        return mapped;
    }

    public static XTandemMain getDefaultApplication() {

        //   FileUtilities.writeFile("BadParametersX,xml",CometTestData.USED_PARAMETERS);
        //    FileUtilities.writeFile("GoodParameters,xml",CometTestData.COMET_XML);

        XTandemMain.setShowParameters(false);  // I do not want to see parameters

        InputStream is = new StringBufferInputStream(CometTestData.COMET_XML); //USED_PARAMETERS); // old was COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);

        return application;
    }

    public static CometScoringAlgorithm getComet(XTandemMain application) {
        return (CometScoringAlgorithm) application.getAlgorithms()[0];
    }

    public static Map<Integer, List<UsedSpectrum>> readUsedSpectraFromResource() {
        return readUsedSpectraFromResource("/UsedSpectraComet.txt");
    }


    public static Map<Integer, List<UsedSpectrum>> readUsedSpectraFromResource(String resource) {
        Class cls = CometTestingUtilities.class;
        InputStream istr;
        istr = cls.getResourceAsStream(resource);
        return UsedSpectrum.readUsedSpectra(istr);
    }

    public static List<UsedSpectrum> getSpectrumUsed(Integer id) {
        return readUsedSpectraFromResource().get(id);
    }

    public static RawPeptideScan getScanFromMZXMLResource(String mzXMLResource) {
        Class cls = CometTestingUtilities.class;

        InputStream istr = cls.getResourceAsStream(mzXMLResource);

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);
        return rp;
    }

    /**
     * read a resource mxXML file and return a set of scans
     *
     * @param mzXMLResource
     * @return
     */
    public static List<RawPeptideScan> getAllScanFromMZXMLResource(String mzXMLResource) {
        Class cls = CometTestingUtilities.class;

        InputStream istr = cls.getResourceAsStream(mzXMLResource);

        List<RawPeptideScan> holder = new ArrayList<RawPeptideScan>();
        final String scanTag = FileUtilities.readInFile(istr);
        List<String> scans = breakmzXMLIntoScans(scanTag);
        for (String scan : scans) {
            RawPeptideScan rawPeptideScan = MZXMLReader.handleScan(scan);
            holder.add(rawPeptideScan);
        }
        return holder;
    }

    protected static List<String> breakmzXMLIntoScans(String mzXML) {
        String[] lines = mzXML.split("\n");
        StringBuilder sb = new StringBuilder();
        List<String> holder = new ArrayList<String>();

        boolean inScan = false;
        int i = 0;
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("</msRun>"))
                break;
            if (line.contains("<scan"))
                inScan = true;
            if (line.contains("</scan>")) {
                sb.append(line);
                holder.add(sb.toString());
                sb.setLength(0);
                inScan = false;
            }
            if (inScan) {
                sb.append(line);
                sb.append("\n");
            }
        }
        return holder;

    }


    @Test
    public void testMxXMLParsing() throws Exception {
        List<RawPeptideScan> scans = getAllScanFromMZXMLResource("/eg3_20.mzXML");
        Assert.assertEquals(20, scans.size());

        RawPeptideScan scan = scans.get(0);
        String id = scan.getId();
        Assert.assertEquals("000000000001", id);
        Assert.assertEquals(1, Integer.parseInt(id));
        Assert.assertEquals(225, scan.getPeaksCount());
        String retentionTime = scan.getRetentionTime();
        retentionTime = retentionTime.replace("PT", ""); // drop PT
        retentionTime = retentionTime.replace("S", ""); // drop S
        Assert.assertEquals(2.17135, Double.parseDouble(retentionTime), 0.001);


    }

    public static void doBinTest(List<UsedSpectrum> spectrumUsed, IMeasuredSpectrum spec) {
        Set<BinChargeKey> spectrumBins = BinChargeMapper.getSpectrumBins(spec);

        for (UsedSpectrum usedSpectrum : spectrumUsed) {
            BinChargeKey pepKey = BinChargeMapper.keyFromPeptide(usedSpectrum.peptide);
            Assert.assertTrue(spectrumBins.contains(pepKey));
        }
    }

    public static Map<IPolypeptide, List<BinnedChargeIonIndex>> readCometBinsFromResource(String res) {
        Class cls = CometTestingUtilities.class;
        InputStream istr = cls.getResourceAsStream(res);
        return readCometBins(istr);
    }


    public static Map<IPolypeptide, List<BinnedChargeIonIndex>> readCometBins(InputStream is) {
        try {
            Map<IPolypeptide, List<BinnedChargeIonIndex>> ret = new HashMap<IPolypeptide, List<BinnedChargeIonIndex>>();
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(is));
            String line = rdr.readLine();
            while (line != null) {
                addCometBin(ret, line);
                line = rdr.readLine();
            }
            rdr.close();
            return ret;
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static void addCometBin(Map<IPolypeptide, List<BinnedChargeIonIndex>> ret, String line) {
        // copied of the console ^&)(&^(*&^(*%*&^
        while (line.contains("  "))
            line = line.replace("  ", " ");
        line = line.replace(" ", "\t");

        String[] split = line.split("\t");
        if (split.length != 6)
            return;
        int index = 0;
        IPolypeptide pp = Polypeptide.fromString(split[index++]);
        List<BinnedChargeIonIndex> list = ret.get(pp);
        if (list == null) {
            list = new ArrayList<BinnedChargeIonIndex>();
            ret.put(pp, list);
        }
        int bin = Integer.parseInt(split[index++]);
        int pos = Integer.parseInt(split[index++]);
        int charge = Integer.parseInt(split[index++]);
        IonType type = IonType.valueOf(split[index++]);
        BinnedChargeIonIndex bx = new BinnedChargeIonIndex(bin, charge, type, pos);
        list.add(bx);

    }

}
