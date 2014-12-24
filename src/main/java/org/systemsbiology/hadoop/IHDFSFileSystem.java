package org.systemsbiology.hadoop;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.*;
import org.systemsbiology.common.*;

import java.io.*;

/**
 * org.systemsbiology.hadoop.IHDFSFileSystem
 *
 * @author Steve Lewis
 * @date 18/05/13
 */
public interface IHDFSFileSystem extends IFileSystem {
    public static IHDFSFileSystem[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IHDFSFileSystem.class;

    public static final FsPermission FULL_ACCESS = new FsPermission(  FsAction.ALL,FsAction.READ_EXECUTE,FsAction.READ_EXECUTE);
    public static final FsPermission FULL_FILE_ACCESS = new FsPermission(  FsAction.READ_WRITE,FsAction.READ_WRITE,FsAction.READ_WRITE);

    /**
     * there are issues with hdfs and running as a remote user
     * InputFormats should be running in the cluster and should alerday be the
     * right user
     * @return  true is you are running on the cluster
     */
    public boolean isRunningAsUser();

    /**
     * if you are running as the user - i.e. on the cluster
     * this should work
     * @param path
     * @return
     */
    public InputStream openFileForRead(Path path);

    /**
     * if you are running as the user - i.e. on the cluster
     * this should work
     * @param path
     * @return
     */
    public OutputStream openFileForWrite(Path path);



}
