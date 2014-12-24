package com.lordjoe.distributed;


import com.lordjoe.utilities.*;
import org.junit.*;
import org.systemsbiology.common.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.remotecontrol.*;

import java.io.*;


/**
 * com.lordjoe.distributed.SparkFileSystemTests
 * User: steven
 * Date: 3/9/11
 */
public class SparkFileSystemTests {

    //    public static final String NAME_NODE = RemoteUtilities.getHost();
    //   public static final int HDFS_PORT = RemoteUtilities.getPort();
    //  public static final String BASE_DIRECTORY = RemoteUtilities.getDefaultPath() + "/test/";
    public static final String FILE_NAME = "little_lamb2.txt";
    public static final String FILE_NAME2 = "little_lamb_stays2.txt";


    public static final String TEST_CONTENT =
            "Mary had a little lamb,\n" +
                    "little lamb, little lamb,\n" +
                    "Mary had a little lamb,\n" +
                    "whose fleece was white as snow.\n" +
                    "And everywhere that Mary went,\n" +
                    "Mary went, Mary went,\n" +
                    "and everywhere that Mary went,\n" +
                    "the lamb was sure to go." +
                    "He followed her to schoool one day";



//    @Before
    public void setVersion2()
    {
      }

    @Test
    public void versionTest() {
        HadoopMajorVersion mv = HadoopMajorVersion.CURRENT_VERSION;
        Assert.assertEquals(HadoopMajorVersion.Version2, mv);
    }

    public static boolean isSparkAccessible() {

        try {
            final String host = RemoteUtilities.getHost();
            final String user = RemoteUtilities.getUser();
            String password = RemoteUtilities.getPassword();
            final String baseDirectory = RemoteUtilities.getDefaultPath();


            IFileSystem fs = new FTPWrapper(user,password,host);  // probably failure throws an exception

            String[] ls = fs.ls(baseDirectory);
            if(ls.length == 0)
                return false;

            fs.disconnect();

            return true;
        }
        catch (Exception e) {
            return false;

        }
    }

    @Test
    public void SparkReadTest() {

        boolean canread = false;
        try {
            canread = isSparkAccessible();
        }
        catch (Exception e) {
            return;

        }
        if(!canread)
            return;
        Assert.assertTrue(canread );

        Assert.assertEquals(HadoopMajorVersion.CURRENT_VERSION,HadoopMajorVersion.Version2);
        // We can tell from the code - hard wired to use security over 0.2

        final String host = RemoteUtilities.getHost();
         final String user = RemoteUtilities.getUser();
         String password = RemoteUtilities.getPassword();

         IFileSystem access = new FTPWrapper(user,password,host);

        try {
                Assert.assertTrue(!access.isLocal());
            String BASE_DIRECTORY = RemoteUtilities.getDefaultPath() + "/test/";
            access.guaranteeDirectory(BASE_DIRECTORY);

            String filePath = BASE_DIRECTORY + FILE_NAME;
            access.writeToFileSystem(filePath, TEST_CONTENT);
            String result = access.readFromFileSystem(filePath);

            Assert.assertEquals(result, TEST_CONTENT);

            Assert.assertTrue(access.exists(filePath));

            access.deleteFile(filePath);
            Assert.assertFalse(access.exists(filePath));

            filePath = BASE_DIRECTORY + FILE_NAME2;
            access.writeToFileSystem(filePath, TEST_CONTENT);
            Assert.assertTrue(access.exists(filePath));


        } catch (Exception e) {
            Throwable cause = ExceptionUtilities.getUltimateCause(e);
            if (cause instanceof EOFException) {   // Spark not available
                return;
            }
            throw new RuntimeException(e);

        }

    }

}
