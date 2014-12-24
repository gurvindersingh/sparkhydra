package com.lordjoe.utilities;

import org.junit.*;

import java.io.*;

/**
 * com.lordjoe.utilities.FileUtilitiesTests
 * User: Steve
 * Date: Apr 5, 2011
 */
public class FileUtilitiesTests {
    public static final FileUtilitiesTests[] EMPTY_ARRAY = {};

    public static final String TEST_MESSAGE =
            "";

    @Test
    public void testMD5() {
        byte[] bytes = TEST_MESSAGE.getBytes();
        ByteArrayInputStream inp = new ByteArrayInputStream(bytes);

        byte[] md51 = FileUtilities.buildMD5Digest(inp);
        byte[] md52 = FileUtilities.buildMD5Digest(bytes);

        Assert.assertEquals(md51.length, 16); // md5 hash is 16 bytes

        Assert.assertTrue(FileUtilities.equivalent(md51, md52));

    }

}
