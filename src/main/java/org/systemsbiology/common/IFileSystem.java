package org.systemsbiology.common;

import java.io.*;

/**
 * org.systemsbiology.common.IFileSystem
 * a file system - possibly remote or distributed
 * User: Steve
 * Date: May 13, 2011
 */
public interface IFileSystem {
    public static final IFileSystem[] EMPTY_ARRAY = {};

    /**
     * true of you aer running on a local disk
     * @return  as above
     */
    public boolean isLocal();

   public  void copyFromFileSystem(String hdfsPath, File localPath);

    /**
     * some file systems simply delete emptydirectories - others allow them
     * @return
     */
    public boolean isEmptyDirectoryAllowed();


    /**
     * delete a directory and all enclosed files and directories
     *
     * @param hdfsPath !null path
     * @return true on success
     */
    public boolean expunge(String hdfsPath);

    /**
     * true if the file esists
     *
     * @param hdfsPath
     * @return
     */
    public boolean exists(String hdfsPath);

    /**
     * length of the file
     * @param hdfsPath !null path - probably of an existing file
     * @return
     */
    public long fileLength(String hdfsPath);

    /**
     * print the current file system location
     * @return !null string
     */
    public String pwd();

    /**
      * create a directory if ot does not exist
      * @param hdfsPath !null path
      * @return  true on success
      */
     public boolean mkdir(String hdfsPath);

    /**
      * list subfiles
      * @param hdfsPath !null path probably exists
      * @return  !null list of enclused file names - emptu if file of not exists
      */
     public String[] ls(String hdfsPath);

      /**
      * true if the file exists and is a directory
      * @param hdfsPath !null path - probably of an existing file
      * @return
      */
     public boolean isDirectory(String hdfsPath);

    /**
     * true if the file exists and is a file
     * @param hdfsPath !null path - probably of an existing file
     * @return
     */
    public boolean isFile(String hdfsPath);

    /**
     * delete the file
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return true if file is deleted
     */
    public boolean deleteFile(String hdfsPath);

    /**
     * guarantee the existance of a file on the remote system
     *
     * @param hdfsPath !null path - on the remote system
     * @param file     !null exitsing file
     */
    public void guaranteeFile(String hdfsPath, File file);

    /**
     * guarantee the existance of a directory on the remote system
     *
     * @param hdfsPath !null path - on the remote system
     */
    public void guaranteeDirectory(String hdfsPath);
//
//    /**
//     * open a file for reading
//     * @param hdfsPath !null path - probably of an existing file
//     * @return !null stream
//     */
//    public InputStream openFileForReadX(String hdfsPath);
//
//    /**
//      * open a file for writing
//      * @param hdfsPath !null path -
//      * @return !null stream
//      */
//    public OutputStream openFileForWriteX(String hdfsPath);

    /**
      * write text to a remote file system
      * @param hdfsPath !null remote path
      * @param content !null test content
      */
     public void writeToFileSystem(String hdfsPath, String content);

    /**
      * write text to a remote file system
      * @param hdfsPath !null remote path
      * @param content !null file to write
      */
     public void writeToFileSystem(String hdfsPath, File content);

    /**
      * write text to a remote file system
      * @param hdfsPath !null remote path
      * @param content !null file to write
      */
     public void writeToFileSystem(String hdfsPath, InputStream content);

    /**
     * read a remote file as text
     * @param hdfsPath !null remote path to an existing file
     * @return  content as text
     */
    public String readFromFileSystem(String hdfsPath);

    /**
       * open a stream from a file
       * @param hdfsPath !null remote path to an existing file
       * @return  input stream
       */
      public InputStream openPath(String hdfsPath);


    /**
       * open a stream to a file
       * @param hdfsPath !null remote path to an existing file
       * @return  input stream
       */
      public OutputStream openPathForWrite(String hdfsPath);


    /**
     * shut down all running sessions   on local file systems
     * this may be a noop but for remote systems shut all connections
     */
    public void disconnect();
}
