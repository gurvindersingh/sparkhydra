package org.systemsbiology.hadoop;

import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.systemsbiology.remotecontrol.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * org.systemsbiology.hadoop.HDFSAccessor
 * Code for accessing a remote file system
 *
 * @author Steve Lewis
 * @date Nov 15, 2010
 */
public class HDFSAccessor implements IHDFSFileSystem {
   // FsPermission FULL_PERMISSION = new FsPermission((short)0777);

    /**
     * Note the use of reflection will cause things work even under 0.2 when  HDFSAsUserAccessor
     * will not compile
     *
     * @return
     */
    public static IHDFSFileSystem getFileSystem() {
        if (HadoopMajorVersion.CURRENT_VERSION != HadoopMajorVersion.Version0 && HDFSAccessor.isHDFSHasSecurity()) {
            try {
                Class<? extends IHDFSFileSystem> cls = (Class<? extends IHDFSFileSystem>) Class.forName("org.systemsbiology.hadoop.HDFWithNameAccessor");
                IHDFSFileSystem ret = cls.newInstance();
                return ret;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new HDFSAccessor();
        }
    }

    public static IHDFSFileSystem getFileSystem(Configuration config) {
        if (HadoopMajorVersion.CURRENT_VERSION != HadoopMajorVersion.Version0 && HDFSAccessor.isHDFSHasSecurity()) {
            try {
                Class<? extends IHDFSFileSystem> cls = (Class<? extends IHDFSFileSystem>) Class.forName("org.systemsbiology.hadoop.HDFWithNameAccessor");

                Class[] argType = {Configuration.class};
                Constructor<? extends IHDFSFileSystem> constructor = cls.getDeclaredConstructor(argType);
                IHDFSFileSystem ret = constructor.newInstance(config);
                return ret;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return new HDFSAccessor(config);
        }


    }

    public static IHDFSFileSystem getFileSystem(final String host, final int port) {
        if (false && HadoopMajorVersion.CURRENT_VERSION != HadoopMajorVersion.Version0 && HDFSAccessor.isHDFSHasSecurity()) {
            try {
                Class<? extends IHDFSFileSystem> cls = (Class<? extends IHDFSFileSystem>) Class.forName("org.systemsbiology.hadoop.HDFWithNameAccessor");

                Class[] argType = {String.class, int.class};
                Constructor<? extends IHDFSFileSystem> constructor = cls.getDeclaredConstructor(argType);
                IHDFSFileSystem ret = constructor.newInstance(host, new Integer(port));
                return ret;
            } catch (Exception e) {
                ExceptionUtilities.printUltimateCause(e);
                throw new RuntimeException(e);
            }
        } else {
            return new HDFSAccessor(host, port);
        }

    }


    private static Configuration g_SharedConfiguration = new Configuration();

    public static Configuration getSharedConfiguration() {
        return g_SharedConfiguration;
    }

    public static void setSharedConfiguration(Configuration sharedConfiguration) {
        g_SharedConfiguration = sharedConfiguration;
    }

    /**
     * all versions beyond 02 have security at EBI
     */
    private static boolean g_HDFSHasSecurity = HadoopMajorVersion.CURRENT_VERSION != HadoopMajorVersion.Version0;

    public static boolean isHDFSHasSecurity() {
        return g_HDFSHasSecurity;
    }

    public static void setHDFSHasSecurity(boolean HDFSHasSecurity) {
        g_HDFSHasSecurity = HDFSHasSecurity;
    }

    private FileSystem m_DFS;


    protected HDFSAccessor() {
        this(RemoteUtilities.getHost(), RemoteUtilities.getPort(), RemoteUtilities.getUser());
    }


    protected HDFSAccessor(Configuration config) {
        try {
            m_DFS = FileSystem.get(config);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    protected HDFSAccessor(final String host, final int port) {
        this(host, port, RemoteUtilities.getUser());
    }

    /**
     * true of you aer running on a local disk
     *
     * @return as above
     */
    @Override
    public boolean isLocal() {
        if (m_DFS instanceof org.apache.hadoop.fs.LocalFileSystem)
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


//    public HDFSAccessor( String host) {
//          this(host,RemoteUtilities.getPort());
//      }

    protected HDFSAccessor(final String host, final int port, final String user) {
        if (port <= 0)
            throw new IllegalArgumentException("bad port " + port);
        String connectString = "hdfs://" + host + ":" + port + "/";
        final String userDir = "/user/" + user;


        try {
            Configuration config = new Configuration();
            config.set("fs.default.name", "hdfs://" + host + ":" + port + userDir);
            config.set("fs.defaultFS", "hdfs://" + host + ":" + port + userDir);
            m_DFS = FileSystem.get(config);
            if (isLocal())
                throw new IllegalStateException("HDFS should not be local");
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect on " + connectString + " because " + e.getMessage() +
                    " exception of class " + e.getClass(), e);
        }
    }

    /**
     * some file systems simply delete emptydirectories - others allow them
     *
     * @return
     */
    public boolean isEmptyDirectoryAllowed() {
        return true;
    }


    public HDFSAccessor(FileSystem fs) {
        m_DFS = fs;
    }


    public FileSystem getDFS() {
        return m_DFS;
    }

    protected void setDFS(FileSystem DFS) {
        m_DFS = DFS;
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
        if(hdfsPath.startsWith("hdfs://"))
            return false;
        if(hdfsPath.startsWith("s3n://"))
               return false;
        if(hdfsPath.startsWith("res:"))
               return false;
        if(hdfsPath.contains(":") )
                  return true;
        if(hdfsPath.startsWith("/"))
               return true;

        return false;
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
        InputStream is = openPath(hdfsPath);
        return FileUtilities.readInFile(is);
    }


    /**
     * read a remote file as text
     *
     * @param hdfsPath !null remote path to an existing file
     * @return content as text
     */
    @Override
    public InputStream openPath(String hdfsPath) {
        InputStream is = openFileForRead(new Path(hdfsPath));
        return is;
    }

    /**
     * open a stream to a file
     *
     * @param hdfsPath !null remote path to an existing file
     * @return input stream
     */
    @Override
    public OutputStream openPathForWrite(final String hdfsPath) {
        return  openFileForWrite(hdfsPath);
     }

    public static void main(String[] args) {
        HDFSAccessor dfs = new HDFSAccessor("glados", 9000, "slewis");
        final Path path = dfs.getDFS().getHomeDirectory();
        System.out.println(path);
        String[] ls = dfs.ls("/user/howdah/JXTandem/data/HoopmanSample/");
        for (int i = 0; i < ls.length; i++) {
            String l = ls[i];
            System.out.println(l);
        }
        //      dfs.expunge(RemoteUtilities.getDefaultPath() + "/JXTandem/data/largerSample");
        //     dfs.expunge(RemoteUtilities.getDefaultPath() + "/JXTandem/data/largeSample");

        //       dfs.writeToFileSystem( "/user/howdah/moby/",new File("E:/moby"));
        dfs.copyFromFileSystem("/user/howdah/JXTandem/data/HoopmanSample/Only_yeast.2012_04_115_15_32_53.t.xml.scans", new File("E:/ForSteven/HoopmanSample/Only_yeast.2012_04_115_15_32_53.t.xml.scans"));
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
