package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.hydra.fragment.*;
import org.junit.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.HashCodeTests
 * verify uniformity of hash for peptides and BinChargeKey
 * User: Steve
 * Date: 1/12/2015
 */
public class HashCodeTests {
    public static final Random RND = new Random();
    public static final int NUMBER_TEST_OBJECTS_PER_BIN = 500;
    public static final int NUMBER_BINS = 1000;
    public static final int MIN_PEPTIDE_LENGTH = 6;
    public static final int MAX_PEPTIDE_LENGTH = 20 + MIN_PEPTIDE_LENGTH;


    public static String randomAminoAcid() {
        FastaAminoAcid[] choices = FastaAminoAcid.UNIQUE_AMINO_ACIDS;
        return choices[RND.nextInt(choices.length)].toString();
    }

    public IPolypeptide generatePolypeptide() {
        int length = MIN_PEPTIDE_LENGTH + RND.nextInt(MAX_PEPTIDE_LENGTH - MIN_PEPTIDE_LENGTH);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(randomAminoAcid());
        }

        return Polypeptide.fromString(sb.toString());
    }

    /**
     * verify uniformity of hash for peptides and BinChargeKey
     * @throws Exception
     */
    @Test
    public void testHashCodes() throws Exception {
        int[] peptide_bins = new int[NUMBER_BINS];
        int[] charge_mz_bins = new int[NUMBER_BINS];
        for (int i = 0; i < NUMBER_TEST_OBJECTS_PER_BIN * NUMBER_BINS; i++) {
            IPolypeptide pp = generatePolypeptide();
            int index = Math.abs(pp.hashCode()) % NUMBER_BINS;
            peptide_bins[index]++;
            double matchingMass = pp.getMatchingMass();
                  for (int charge = 1; charge < 4; charge++) {
                    BinChargeKey key = BinChargeMapper.oneKeyFromChargeMz(charge, matchingMass / charge);
                      index = Math.abs(key.hashCode()) % NUMBER_BINS;
                      charge_mz_bins[index]++;
                }
        }

        for (int i = 0; i < NUMBER_BINS; i++) {
            int hashCount = peptide_bins[i];
            int testMin = (int) (NUMBER_TEST_OBJECTS_PER_BIN * 0.8);
            if(hashCount <= testMin)
                Assert.assertTrue(hashCount > testMin);
            int testMax = (int) (NUMBER_TEST_OBJECTS_PER_BIN * 1.2);
            if(hashCount >= testMax)
                 Assert.assertTrue(hashCount < testMax);

            hashCount = charge_mz_bins[i];
             testMin = (int) ((NUMBER_TEST_OBJECTS_PER_BIN * 0.8) * 3);   // 3 charges per peptide
              if(hashCount <= testMin)
                  Assert.assertTrue(hashCount > testMin);
              testMax = (int) ((NUMBER_TEST_OBJECTS_PER_BIN * 1.2) * 3);
              if(hashCount >= testMax)
                   Assert.assertTrue(hashCount < testMax);

        }


    }
}
