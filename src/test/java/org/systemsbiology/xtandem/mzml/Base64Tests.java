package org.systemsbiology.xtandem.mzml;

import org.junit.*;

/**
 * org.systemsbiology.xtandem.mzml.Base64Tests
 * test Base 64 ancoding
 * User: Steve
 * Date: May 3, 2011
 */
public class Base64Tests {
    public static final Base64Tests[] EMPTY_ARRAY = {};

    public static final String TEST_DATA_1 = "AAAAwNfia0AAAACApoJuQAAAAABF7nBAAAAAABhecUAAAACAIZJxQAAAAACjWnJAAAAAwF96c0AAAAAAOYxzQAAAAIBHkHRAAAAAwBbRdEAAAADA/wl1QAAAAIABL3VAAAAAgMdOdUAAAACAyl91QAAAAADCz3VAAAAAABPxdUAAAABAQy12QAAAAIBwUHZAAAAAwJxqdkAAAABA5nl2QAAAAIC7g3ZAAAAAQL0nd0AAAADAJOF3QAAAAMCS+ndAAAAAwEcVeEAAAAAA+SV4QAAAAECeRnhAAAAAwI3teEAAAACAzAx5QAAAAIAEH3lAAAAAgGOheUAAAAAAiUZ6QAAAAMAvonpAAAAAQCKvekAAAAAAKcF6QAAAAAB6znpAAAAAAMPeekAAAACAie16QAAAAMDfantAAAAAAFG/e0AAAACAOcx7QAAAAMCC6ntAAAAAgKCYgEA=";

    public static final String TEST_DATA_2 = "AAAAAAD+p0AAAAAAAKyTQAAAAAAAoIFAAAAAAACGokAAAAAAALiZQAAAAAAAKJhAAAAAAAAUkUAAAAAAAGiXQAAAAAAAWJFAAAAAAAB0n0AAAAAAAPyXQAAAAAAA1J5AAAAAAECn20AAAAAAAM6vQAAAAAAAwIhAAAAAAAAwo0AAAAAAAL6oQAAAAAAAJJpAAAAAAMhtCkEAAAAAwGDdQAAAAAAAAABAAAAAAACgkkAAAAAAAOSTQAAAAAAALKBAAAAAAABgqEAAAAAAgMPGQAAAAAAAmKBAAAAAAACoiUAAAAAAAMiJQAAAAAAAfrJAAAAAAAAcqUAAAAAAAEyTQAAAAAAA2blAAAAAAADCskAAAAAAAPCpQAAAAADUXxJBAAAAAMDH+EAAAAAAANCEQAAAAAAAkJ1AAAAAAABUlEAAAAAAAAyeQAAAAAAAWIVAAAAAAABgnUA=";

    public double[] TEST_ANSWER1 =
            {
                    223.08883666992188,
                    244.08282470703125,
                    270.891845703125,
                    277.880859375,
                    281.1331787109375,
                    293.664794921875,
                    311.64837646484375,
                    312.763916015625,
                    329.0174560546875,
                    333.06805419921875,
                    336.62493896484375,
                    338.9378662109375,
                    340.9237060546875,
                    341.9869384765625,
                    348.98486328125,
                    351.067138671875,
                    354.82891845703125,
                    357.0274658203125,
                    358.66326904296875,
                    359.61871337890625,
                    360.2332763671875,
                    370.48370361328125,
                    382.07147216796875,
                    383.66082763671875,
                    385.33001708984375,
                    386.373291015625,
                    388.41363525390625,
                    398.84710693359375,
                    400.7999267578125,
                    401.9385986328125,
                    410.0867919921875,
                    420.408447265625,
                    426.13665771484375,
                    426.94586181640625,
                    428.072509765625,
                    428.90478515625,
                    429.922607421875,
                    430.8460693359375,
                    438.67962646484375,
                    443.957275390625,
                    444.7640380859375,
                    446.65692138671875,
                    531.078369140625,
            };

    public double[] TEST_ANSWER2 =
            {
                    3071.0,
                    1259.0,
                    564.0,
                    2371.0,
                    1646.0,
                    1546.0,
                    1093.0,
                    1498.0,
                    1110.0,
                    2013.0,
                    1535.0,
                    1973.0,
                    28317.0,
                    4071.0,
                    792.0,
                    2456.0,
                    3167.0,
                    1673.0,
                    216505.0,
                    30083.0,
                    2.0,
                    1192.0,
                    1273.0,
                    2070.0,
                    3120.0,
                    11655.0,
                    2124.0,
                    821.0,
                    825.0,
                    4734.0,
                    3214.0,
                    1235.0,
                    6617.0,
                    4802.0,
                    3320.0,
                    301045.0,
                    101500.0,
                    666.0,
                    1892.0,
                    1301.0,
                    1923.0,
                    683.0,
                    1880.0,

            };

    @Test
    public void testBase64() {
        String dataString = TEST_DATA_1;
        double[] realData = MzMlUtilities.decodeDataString(dataString,false);
        Assert.assertEquals(43, realData.length);
        for (int i = 0; i < realData.length; i++) {
            double v = realData[i];
            Assert.assertEquals(TEST_ANSWER1[i], v, 0.0001);
        }
        String reencode = MzMlUtilities.encode(realData);
        Assert.assertEquals(TEST_DATA_1, reencode);

        double[] realData2 = MzMlUtilities.decodeDataString(TEST_DATA_2,false);
        Assert.assertEquals(43, realData2.length);
        for (int i = 0; i < realData2.length; i++) {
            double v = realData2[i];
            Assert.assertEquals(TEST_ANSWER2[i], v, 0.0001);
        }
        reencode = MzMlUtilities.encode(realData2);
        Assert.assertEquals(TEST_DATA_2, reencode);

    }

   
}
