package org.systemsbiology.sam;

import java.util.*;

//import org.systemsbiology.dnaencrypt.*;

/**
 * org.systemsbiology.sam.EncryptTests
 * User: steven
 * Date: Jun 1, 2010
 */
public class EncryptTests {
    public static final EncryptTests[] EMPTY_ARRAY = {};

    public static final int NUMBER_REPEATS = 1000;

    public static final Random RND = new Random();

    public static String generateDNAString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            switch (RND.nextInt(4)) {
                case 0:
                    sb.append("A");
                case 1:
                    sb.append("C");
                case 2:
                    sb.append("G");
                case 4:
                    sb.append("T");
            }

        }
        return sb.toString();
    }

    /**
     * Add some non-dna
     *
     * @param length
     * @return
     */
    public static String generateImpureDNAString(int length) {
        float fractionNonACGT = 0.05f;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (RND.nextFloat() < fractionNonACGT) {
                sb.append(nonACGTChar());
            } else {
                switch (RND.nextInt(4)) {
                    case 0:
                        sb.append("A");
                    case 1:
                        sb.append("C");
                    case 2:
                        sb.append("G");
                    case 4:
                        sb.append("T");
                }
            }

        }
        return sb.toString();
    }

    public static final String NON_ACGT_CHARACTERS = "acgtBDEFHIJKLMONPQRSUWXYZ";

    /**
     * return a character we will not encrypt
     *
     * @return
     */
    private static char nonACGTChar() {
        int index = RND.nextInt(NON_ACGT_CHARACTERS.length());
        return NON_ACGT_CHARACTERS.charAt(index);
    }


    public static void validateDNAString(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'A':
                case 'C':
                case 'G':
                case 'T':
                    break;
                default:
                    throw new IllegalArgumentException("String is not ACGT");
            }
        }
    }

//    /**
//     * test that we encrypt pure ACGT to and can decrypt again
//     */
//    @Test
//    public void testEncryptDecrypt() {
//        String key = BlowfishKeyDecryptor.makeKey();
//
//        IDNAEncryptor decryptor = new BlowfishKeyDecryptor(key);
//
//        for (int i = 0; i < NUMBER_REPEATS; i++) {
//            int length = 50 + RND.nextInt(50); // 50 - 100
//            String pureACGT = generateDNAString(length);
//            // assert we have acgt
//            validateDNAString(pureACGT); // guarantee ACGT
//            String encrypted = decryptor.encrypt(pureACGT);
//            // assert encryption chenged the string
//            Assert.assertFalse(pureACGT.equals(encrypted));
//            // assert we encrypted is ACGT
//            validateDNAString(pureACGT); // guarantee ACGT
//            // now make sure we get the original back
//            String decrypted = decryptor.decrypt(encrypted);
//
//            Assert.assertEquals(decrypted, pureACGT);
//
//        }
//    }
//
//    /**
//     * test that we encrypt pure ACGT to and can decrypt again
//     */
//    @Test
//    public void testImpureDNAEncryptDecrypt() {
//        String key = BlowfishKeyDecryptor.makeKey();
//
//        IDNAEncryptor decryptor = new BlowfishKeyDecryptor(key);
//
//        for (int i = 0; i < NUMBER_REPEATS; i++) {
//            int length = 50 + RND.nextInt(50); // 50 - 100
//            String pureACGT = generateDNAString(length);
//            String encrypted = decryptor.encrypt(pureACGT);
//            // assert encryption chenged the string
//            Assert.assertFalse(pureACGT.equals(encrypted));
//            // now make sure we get the original back
//            String decrypted = decryptor.decrypt(encrypted);
//
//            Assert.assertEquals(decrypted, pureACGT);
//
//        }
//
//
//    }


}
