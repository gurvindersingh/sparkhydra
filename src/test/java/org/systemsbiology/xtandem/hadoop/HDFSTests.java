package org.systemsbiology.xtandem.hadoop;


import org.junit.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.remotecontrol.*;
import org.systemsbiology.xtandem.*;

import java.io.*;
import java.net.UnknownHostException;

/**
 * org.systemsbiology.hadoop.HDFSTests
 * User: steven
 * Date: 3/9/11
 */
public class HDFSTests {
    public static final HDFSTests[] EMPTY_ARRAY = {};

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




    @Test
     public void versionTest()
     {
         HadoopMajorVersion mv = HadoopMajorVersion.CURRENT_VERSION;
 //        Assert.assertEquals(HadoopMajorVersion.Version1,mv);
         Assert.assertEquals(HadoopMajorVersion.Version2,mv);
        }

    public static boolean isHDFSAccessible() {

         IHDFSFileSystem access = null;
         final String host = RemoteUtilities.getHost();
         final int port = RemoteUtilities.getPort();
         final String user = RemoteUtilities.getUser();

         access = HDFSAccessor.getFileSystem(host,port) ;
         return true;
    }
//
//     RemoteUtilities.getPassword()
//     String connStr = host + ":" + port + ":" + user + ":" + RemoteUtilities.getPassword();
//    // do we already know
//       Boolean ret = gConditionToAvailability.get(connStr);
//         if (ret == null) {
 //
 //        final String userDir =  "/user/" + user;
 //        try {
 //            UserGroupInformation ugi
 //                    = UserGroupInformation.createRemoteUser(user);
 //
 //            ugi.doAs(new PrivilegedExceptionAction<Void>() {
 //
 //                public Void run() throws Exception {
 //
 //                    Configuration conf = new Configuration();
 //                    conf.set("fs.defaultFS", "hdfs://" + host + ":" + port + userDir);
 //                    conf.set("hadoop.job.ugi", user);
 //
 //                    FileSystem fs = FileSystem.get(conf);
 //
 ////                    fs.createNewFile(new Path(userDir + "/test"));
 ////
 ////                    FileStatus[] status = fs.listStatus(new Path("/user/" + user));
 ////                    for (int i = 0; i < status.length; i++) {
 ////                        System.out.println(status[i].getPath());
 ////                    }
 //                    return null;
 //
 //                }
 //            });
 //            ret = true;
 //             gConditionToAvailability.put(connStr,ret);
 //        } catch (Exception e) {
 //             ret = false;
 //            gConditionToAvailability.put(connStr,ret);
 //
 //        }
 //         gConditionToAvailability.put(connStr,Boolean.TRUE);
 //
 //        }
 //        return ret;
 //        // never get here


   @Test
    public void HDFSReadTest()
    {


       // We can tell from the code - hard wired to use security over 0.2
        boolean isVersion1 = HadoopMajorVersion.CURRENT_VERSION != HadoopMajorVersion.Version0;
//        HDFSAccessor.setHDFSHasSecurity(isVersion1);

//        if(isVersion1) {
//            RemoteUtilities.setPort(8020); // todo make better
//            RemoteUtilities.setHost("hadoop-master-01.ebi.ac.uk");   // todo make not hard coded
//            RemoteUtilities.setUser("slewis");
//            String user = RemoteUtilities.getUser();
//            RemoteUtilities.setDefaultPath("/user/" + user + "/foobar" );
//              String defaultPath = RemoteUtilities.getDefaultPath();
//          }

//        if(!isHDFSAccessible())  {
//            System.out.println("Not running HDFS tests");
//            return;
//        }
        try {
            String NAME_NODE = RemoteUtilities.getHost();
            int HDFS_PORT = RemoteUtilities.getPort();

            IHDFSFileSystem access = HDFSAccessor.getFileSystem(NAME_NODE,HDFS_PORT);

            Assert.assertTrue(!access.isLocal());
            String BASE_DIRECTORY = RemoteUtilities.getDefaultPath() + "/test/";
            access.guaranteeDirectory(BASE_DIRECTORY);

            String filePath = BASE_DIRECTORY + FILE_NAME;
             access.writeToFileSystem(filePath,TEST_CONTENT);
            String result = access.readFromFileSystem(filePath );

            Assert.assertEquals(result, TEST_CONTENT);

            Assert.assertTrue(access.exists(filePath));

            access.deleteFile(filePath);
            Assert.assertFalse(access.exists(filePath));

            filePath = BASE_DIRECTORY + FILE_NAME2;
            access.writeToFileSystem(filePath,TEST_CONTENT);
            Assert.assertTrue(access.exists(filePath));


         }
        catch (Exception e) {
            Throwable cause = XTandemUtilities.getUltimateCause(e);
            if (cause instanceof EOFException) {   // hdfs not available
                return;
            }
            if (cause instanceof UnknownHostException) {   // host not available
                return;
            }
            throw new RuntimeException(e);

        }

    }

}
