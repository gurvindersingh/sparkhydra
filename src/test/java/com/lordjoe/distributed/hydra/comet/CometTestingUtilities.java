package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.utilities.FileUtilities;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.testing.MZXMLReader;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * com.lordjoe.distributed.hydra.comet.CometTestingUtilities
 *
 * @author Steve Lewis
 * @date 5/22/2015
 */
public class CometTestingUtilities {
    public static CometTestingUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = CometTestingUtilities.class;

    public static Map<Integer, List<UsedSpectrum>> readUsedSpectraFromResource( ) {
        Class cls = CometTestingUtilities.class;
        InputStream istr;
        istr = cls.getResourceAsStream("/UsedSpectraComet.txt");
        return UsedSpectrum.readUsedSpectra(istr);
    }

    public static  List<UsedSpectrum> getSpectrumUsed(Integer id) {
        return readUsedSpectraFromResource().get(id);
    }

    public static RawPeptideScan getScanFromMZXMLResource(String mzXMLResource)
    {
        Class cls = CometTestingUtilities.class;

        InputStream istr = cls.getResourceAsStream(mzXMLResource);

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);
        return rp;
    }

}
