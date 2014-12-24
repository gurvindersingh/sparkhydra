package org.systemsbiology.hadoop;

import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.api.java.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.hadoop.HDFSAccessor
 * Code for accessing a remote file system
 *
 * @author Steve Lewis
 * @date Nov 15, 2010
 */
public class HDFSMountedAccessor implements IHDFSFileSystem {
   // FsPermission FULL_PERMISSION = new FsPermission((short)0777);


    public static IHDFSFileSystem getFileSystem(JavaSparkContext ctx) {
            return new HDFSMountedAccessor(ctx.hadoopConfiguration());
     }




     private FileSystem m_DFS;



    protected HDFSMountedAccessor(Configuration config) {
        try {
            m_DFS = FileSystem.get(config);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

     /**
     * true of you aer running on a local disk
     *
     * @return as above
     */
    @Override
    public boolean isLocal() {
        if (m_DFS instanceof LocalFileSystem)
            return true;

        return false;
    }


    /**
     * shut down all running sessions   on local file systems
     * this may be a noop but for remote systems shut all connections
     */
    @Override
    public void disconnect() {
        //  noop

    }

     /**
     * some file systems simply delete emptydirectories - others allow them
     *
     * @return
     */
    public boolean isEmptyDirectoryAllowed() {
        return true;
    }


    public FileSystem getDFS() {
        return m_DFS;
    }


    @Override
    public void copyFromFileSystem(String hdfsPath, File localPath) {
        final FileSystem fileSystem = getDFS();
        Path src = new Path(hdfsPath);

        Path dst = new Path(localPath.getAbsolutePath());


        try {
            fileSystem.copyToLocalFile(src, dst);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void writeToFileSystem(String hdfsPath, File localPath) {
        final FileSystem fileSystem = getDFS();
        Path dst = new Path(hdfsPath);
        try {
            final Path parent = dst.getParent();
            if (!fileSystem.exists(parent)) {
                fileSystem.mkdirs(parent);
                fileSystem.setPermission(parent,FULL_ACCESS);
            }

            Path src = new Path(localPath.getAbsolutePath());

            fileSystem.copyFromLocalFile(src, dst);
            fileSystem.setPermission(src,FULL_ACCESS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * create a directory if ot does not exist
     *
     * @param hdfsPath !null path
     * @return true on success
     */
    @Override
    public boolean mkdir(final String hdfsPath) {
        return false;
    }

    /**
     * true if the file exists and is a directory
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return
     */
    @Override
    public boolean isDirectory(final String hdfsPath) {
        final FileSystem fileSystem = getDFS();
        Path dst = new Path(hdfsPath);
        try {
            return fileSystem.isDirectory(dst);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * true if the file exists and is a file
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return
     */
    @Override
    public boolean isFile(final String hdfsPath) {
        final FileSystem fileSystem = getDFS();
        Path dst = new Path(hdfsPath);
        try {
            return fileSystem.isFile(dst);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * write text to a remote file system
     *
     * @param hdfsPath !null remote path
     * @param content  !null file to write
     */
    @Override
    public void writeToFileSystem(final String hdfsPath, final InputStream content) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    /**
     * delete a directory and all enclosed files and directories
     *
     * @param hdfsPath !null path
     * @return true on success
     */
    @Override
    public boolean expunge(String hdfsPath) {
        final FileSystem fs = getDFS();
        Path src = new Path(hdfsPath);


        try {
            if (!fs.exists(src))
                return true;
            // break these out
            if (fs.getFileStatus(src).isDir()) {
                boolean doneOK = fs.delete(src, true);
                doneOK = !fs.exists(src);
                return doneOK;
            }
            if (fs.isFile(src)) {
                boolean doneOK = fs.delete(src, false);
                return doneOK;
            }
            throw new IllegalStateException("should be file of directory if it exists");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * true if the file esists
     *
     * @param hdfsPath
     * @return
     */
    @Override
    public boolean exists(String hdfsPath) {
        final FileSystem fs = getDFS();
        try {
            Path src = new Path(hdfsPath);
            return fs.exists(src);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * true if the file exists
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return
     */
    @Override
    public long fileLength(String hdfsPath) {
        final FileSystem fs = getDFS();
        try {
            if (!exists(hdfsPath))
                return 0;
            Path src = new Path(hdfsPath);
            ContentSummary contentSummary = fs.getContentSummary(src);
            if (contentSummary == null)
                return 0;
            return contentSummary.getLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * delete the file
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return true if file is deleted
     */
    @Override
    public boolean deleteFile(String hdfsPath) {
        final FileSystem fs = getDFS();
        try {
            Path src = new Path(hdfsPath);
            fs.delete(src, false);
            return fs.exists(src);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * guarantee the existance of a file on the remote system
     *
     * @param hdfsPath !null path - on the remote system
     * @param file     !null exitsing file
     */
    @Override
    public void guaranteeFile(String hdfsPath, File file) {
        final FileSystem fs = getDFS();
        Path src = new Path(hdfsPath);


        try {
            if (fs.exists(src))
                return;
            this.writeToFileSystem(hdfsPath, file);
         } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * guarantee the existance of a directory on the remote system
     *
     * @param hdfsPath !null path - on the remote system
     */
    @Override
    public void guaranteeDirectory(String hdfsPath) {
        Path src = new Path(hdfsPath);


        guaranteeDirectory(src);

    }

    private void guaranteeDirectory(Path src) {
        final FileSystem fs = getDFS();
        try {
            if (fs.exists(src)) {
                if (!fs.isFile(src)) {
                    return;
                } else {
                    fs.delete(src, false);   // drop a file we want a directory
                }
                fs.setPermission(src, FULL_ACCESS);
            }
            fs.mkdirs(src, FULL_ACCESS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * open a file for reading
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return !null stream
     */

    public InputStream openFileForRead(Path src) {
        String hdfsPath = src.getName();
        if (isFileNameLocal(hdfsPath)) {
            try {
                return new FileInputStream(hdfsPath); // better be local
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);

            }
        }
        final FileSystem fs = getDFS();
        try {
            return fs.open(src);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static boolean isFileNameLocal(final String hdfsPath) {
        return hdfsPath.contains(":") && !hdfsPath.startsWith("s3n:") && !hdfsPath.startsWith("res:");
    }

    /**
     * open a stream from a file
     *
     * @param hdfsPath !null remote path to an existing file
     * @return input stream
     */
    @Override
    public InputStream openPath(final String hdfsPath) {
        return openFileForRead(new Path(hdfsPath));
    }

    /**
     * open a stream to a file
     *
     * @param hdfsPath !null remote path to an existing file
     * @return input stream
     */
    @Override
    public OutputStream openPathForWrite(final String hdfsPath) {
        return openFileForWrite(new Path(hdfsPath));
     }

    /**
     * open a file for writing
     *
     * @param hdfsPath !null path -
     * @return !null stream
     */

    protected OutputStream openFileForWrite(String hdfsPath) {
        if (isFileNameLocal(hdfsPath)) {
            try {
                return new FileOutputStream(hdfsPath); // better be local
            } catch (IOException e) {
                throw new RuntimeException(e);

            }
        }

        Path src = new Path(hdfsPath);
        return openFileForWrite(src);
    }

    @Override
    public OutputStream openFileForWrite(Path src) {
        final FileSystem fs = getDFS();
        try {
            Path parent = src.getParent();
            guaranteeDirectory(parent);
            return FileSystem.create(fs, src, FULL_FILE_ACCESS);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * print the current file system location
     *
     * @return !null string
     */
    @Override
    public String pwd() {
        final FileSystem fs = getDFS();
        return absolutePath(fs.getWorkingDirectory());
    }

    public static String absolutePath(Path p) {
        if (p == null)
            return "";
        StringBuilder sb = new StringBuilder();
        Path parentPath = p.getParent();
        if (parentPath == null)
            return "/";
        sb.append(absolutePath(parentPath));
        if (sb.length() > 1)
            sb.append("/");
        sb.append(p.getName());
        return sb.toString();
    }

    /**
     * list subfiles
     *
     * @param hdfsPath !null path probably exists
     * @return !null list of enclused file names - emptu if file of not exists
     */
    @Override
    public String[] ls(final String hdfsPath) {
        final FileSystem fs = getDFS();
        try {
            FileStatus[] statuses = fs.listStatus(new Path(hdfsPath));
            if (statuses == null)
                return new String[0];
            List<String> holder = new ArrayList<String>();
            for (int i = 0; i < statuses.length; i++) {
                FileStatus statuse = statuses[i];
                String s = statuse.getPath().getName();
                holder.add(s);
            }
            String[] ret = new String[holder.size()];
            holder.toArray(ret);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * write text to a remote file system
     *
     * @param hdfsPath !null remote path
     * @param content  !null test content
     */
    @Override
    public void writeToFileSystem(String hdfsPath, String content) {
        OutputStream os = openFileForWrite(hdfsPath);
        FileUtilities.writeFile(os, content);
    }

    /**
     * read a remote file as text
     *
     * @param hdfsPath !null remote path to an existing file
     * @return content as text
     */
    @Override
    public String readFromFileSystem(String hdfsPath) {
        InputStream is = openFileForRead(new Path(hdfsPath));
        return FileUtilities.readInFile(is);
    }



    /**
     * there are issues with hdfs and running as a remote user
     * InputFormats should be running in the cluster and should alerday be the
     * right user
     *
     * @return true is you are running on the cluster
     */
    @Override
    public boolean isRunningAsUser() {
        if (isLocal())
            return true;
        if (true)
            throw new UnsupportedOperationException("Fix This");
        return false;
    }
}
