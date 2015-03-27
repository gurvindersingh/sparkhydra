package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.hydra.*;
import com.lordjoe.distributed.hydra.test.*;
import com.lordjoe.distributed.spectrum.*;
import org.junit.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.BinChargeAssignmentTest
 * User: Steve
 * Date: 3/25/2015
 */
public class BinChargeAssignmentTest {
    public static final int NUMBER_SPECTRA = 20;
    public static final double SPECTRUM1_MZ = 549.27;
    public static final double SPECTRUM1_MASS = 2194.058;
    public static final String TEST_PEPTIDE = "KGGELASAVHAYTKTGDPSMR";
    public static final String TEST_PEPTIDE1 = "KGGELASAVHAYTK";
     public static final double PEPTIDE_MASS = 2176.08;
    public static final double PEPTIDE_MASS_PART1 = 1431.759;
     public static final double REQUIRED_PRECISION = 0.05;

    public static final String INTERESTING_SPECTRUM1 = "131104_Berit_BSA2.11526.11526.4";

    private List<IMeasuredSpectrum> specHolder;
    private Map<String, IMeasuredSpectrum> idToSpectrum = new HashMap<String, IMeasuredSpectrum>();

     public void setUpHolders() throws Exception {
        XTandemMain.setShowParameters(false);
        List<Tuple2<String, String>> holder = readSpectrumTuples();
        specHolder = new ArrayList<IMeasuredSpectrum>();
        storeSpectra(holder);

    }

    public void storeSpectra(final List<Tuple2<String, String>> pHolder) throws Exception {
        InputStream is = new StringBufferInputStream(PeptideScoringTest.TANDEM_XML);
        XTandemMain application = new SparkXTandemMain(is, "TANDEM_XML");
        Assert.assertNotNull(application);
        MGFStringTupleToSpectrumTuple func = new MGFStringTupleToSpectrumTuple(application);
        for (Tuple2<String, String> s : pHolder) {
            Iterable<Tuple2<String, IMeasuredSpectrum>> call = func.call(s);
            for (Tuple2<String, IMeasuredSpectrum> value : call) {
                IMeasuredSpectrum e = value._2();
                Assert.assertNotNull(e);
                specHolder.add(e);
                String id = e.getId();
                idToSpectrum.put(id, e);
            }
        }
    }

    public List<Tuple2<String, String>> readSpectrumTuples() throws IOException {
        InputStream is = BinChargeAssignmentTest.class.getResourceAsStream("/eg3PeaksOnlyTop20.mgf");
        Assert.assertNotNull(is);
        LineNumberReader rdr = new LineNumberReader(new InputStreamReader(is));

        List<Tuple2<String, String>> holder = new ArrayList<Tuple2<String, String>>();
        int index = 1;
        while (true) {
            String spectrum = ParseMGFTest.readOneSpectrum(rdr);
            if (spectrum == null)
                break;
            holder.add(new Tuple2(Integer.toString(index++), spectrum));
        }
        is.close();
        Assert.assertEquals(NUMBER_SPECTRA, holder.size());
        return holder;
    }

    @Test
    public void testBinChargeSpectrumAssignment() throws Exception {

        setUpHolders();

        Assert.assertEquals(NUMBER_SPECTRA, specHolder.size());
        Assert.assertEquals(NUMBER_SPECTRA, idToSpectrum.size());

        IMeasuredSpectrum spectrum1 = idToSpectrum.get(INTERESTING_SPECTRUM1);
        Assert.assertNotNull(spectrum1);

        Assert.assertEquals(4, spectrum1.getPrecursorCharge());
        double mz = spectrum1.getPrecursorMassChargeRatio();
        Assert.assertEquals(SPECTRUM1_MZ, mz, REQUIRED_PRECISION);

        double mass = spectrum1.getPrecursorMass();
        Assert.assertEquals(SPECTRUM1_MASS, mass, REQUIRED_PRECISION);

    }

    @Test
    public void testPeptideMass() throws Exception {
        IPolypeptide pp1 = Polypeptide.fromString(TEST_PEPTIDE1);
        double matchingMass1 = pp1.getMatchingMass();
         Assert.assertEquals(PEPTIDE_MASS_PART1,matchingMass1,REQUIRED_PRECISION);

        IPolypeptide pp = Polypeptide.fromString(TEST_PEPTIDE);
        double matchingMass = pp.getMatchingMass();
        Assert.assertEquals(PEPTIDE_MASS,matchingMass,REQUIRED_PRECISION);

    }


}
