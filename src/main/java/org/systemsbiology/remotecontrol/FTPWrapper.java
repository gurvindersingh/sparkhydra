package org.systemsbiology.remotecontrol;

import com.jcraft.jsch.*;
import com.lordjoe.utilities.*;
import org.systemsbiology.common.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.remotecontrol.FTPWrapper
 * Object wrapping an ftp connection
 * User: steven
 * Date: 5/12/11
 */
public class FTPWrapper implements IFileSystem {
    public static final FTPWrapper[] EMPTY_ARRAY = {};


    private final JSch m_JSch;
    private final Session m_Session;
    private final ChannelSftp m_FtpChannel;
    private final UserInfo m_UserInfo;
    private boolean m_Connected;


    public FTPWrapper(String user, String password, String host) {
        FileInputStream fis = null;


        try {
            m_JSch = new JSch();
            m_Session = m_JSch.getSession(user, host, 22);

            // username and password will be given via UserInfo interface.
            m_UserInfo = new MyUserInfo(user, password);
            m_Session.setUserInfo(m_UserInfo);
            m_Session.connect();
            Channel channel = m_Session.openChannel("sftp");
            channel.connect();
            m_FtpChannel = (ChannelSftp) channel;
            setConnected(true);
        } catch (JSchException e) {
            setConnected(false);
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
        return false;
    }

    /**
     * shut down all running sessions   on local file systems
     * this may be a noop but for remote systems shut all connections
     */
    @Override
    public void disconnect() {
        setConnected(false);

    }


    /**
     * some file systems simply delete emptydirectories - others allow them
     *
     * @return
     */
    public boolean isEmptyDirectoryAllowed() {
        return true;
    }


    public boolean isConnected() {
        return m_Connected;
    }

    public void setConnected(final boolean pConnected) {
        m_Connected = pConnected;
    }

    public void guaranteeConnected() {
        if (!isConnected())
            throw new IllegalStateException("not connected");
    }

    public ChannelSftp getFtpChannel() {
        return m_FtpChannel;
    }


    public void exit() {
        ChannelSftp channel = getFtpChannel();
        channel.exit();
        setConnected(false);
    }


    public void cd(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            channel.cd(path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public String[] ls(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            Vector ls = channel.ls(path);
            List<String> holder = new ArrayList<String>();
            for (Iterator<ChannelSftp.LsEntry> iterator = ls.iterator(); iterator.hasNext(); ) {
                ChannelSftp.LsEntry next = iterator.next();
                String s = next.getFilename();
                if (".".equals(s) || "..".equals(s))
                    continue;
                holder.add(s);
            }
            String[] ret = new String[holder.size()];
            holder.toArray(ret);
            return ret;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public void chown(int values, String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            channel.chown(values, path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public void chmod(int values, String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            channel.chmod(values, path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public void guaranteeDirectory(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            if (!exists(path))
                channel.mkdir(path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void copyFromFileSystem(final String hdfsPath, final File localPath) {
        ChannelSftp channel = getFtpChannel();
        try {
            InputStream in = new FileInputStream(localPath);
            channel.put(in, hdfsPath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (SftpException e) {
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
    public void writeToFileSystem(final String hdfsPath, final File content) {
        try {
            writeToFileSystem(hdfsPath, new FileInputStream(content));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * write text to a remote file system
     *
     * @param path    !null remote path
     * @param content !null file to write
     */
    @Override
    public void writeToFileSystem(final String path, final InputStream in) {
        OutputStream os = put(path);
        try {
            FileUtilities.copyStream(in, os);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * delete a directory and all enclosed files and directories
     *
     * @param hdfsPath !null path
     * @return true on success
     */
    @Override
    public boolean expunge(final String hdfsPath) {
        if (!exists(hdfsPath)) {
            return true;
        }
        if (isFile(hdfsPath)) {
            return deleteFile(hdfsPath);
        }
        if (isDirectory(hdfsPath)) {

            ChannelSftp channel = getFtpChannel();
            String[] strings = ls(hdfsPath);
            while (strings.length > 0) {
                for (int i = 0; i < strings.length; i++) {

                    String string = strings[i];
                    if ("..".equals(string))
                        continue;
                    if (".".equals(string))
                        continue;
                    expunge(hdfsPath + "/" + string);
                }
                strings = ls(hdfsPath);
            }
            try {
                channel.rmdir(hdfsPath);
                return true;
            } catch (SftpException e) {
                throw new RuntimeException(e);

            }
        }
        throw new IllegalStateException("never get here");
    }


    /**
     * true if the file exists
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return
     */
    @Override
    public long fileLength(final String hdfsPath) {
        ChannelSftp channel = getFtpChannel();
        try {
            SftpATTRS attrs = channel.lstat(hdfsPath);
            return attrs.getSize();
        } catch (SftpException e) {
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
    public boolean deleteFile(final String hdfsPath) {
        ChannelSftp channel = getFtpChannel();
        try {
            channel.rm(hdfsPath);
            return true;
        } catch (SftpException e) {
            return false;

        }
    }

    /**
     * guarantee the existance of a file on the remote system
     *
     * @param hdfsPath !null path - on the remote system
     * @param file     !null exitsing file
     */
    @Override
    public void guaranteeFile(final String hdfsPath, final File file) {
        if (!exists(hdfsPath)) {
            writeToFileSystem(hdfsPath, file);
            return;
        }
        // todo handle older versions
    }

    /**
     * open a file for reading
     *
     * @param path !null path - probably of an existing file
     * @return !null stream
     */
    protected InputStream openFileForRead(final String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            return channel.get(path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * open a file for writing
     *
     * @param hdfsPath !null path -
     * @return !null stream
     */

    protected OutputStream openFileForWrite(final String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            return channel.put(path);
        } catch (SftpException e) {
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
    public void writeToFileSystem(final String path, final String content) {
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        writeToFileSystem(path, in);
    }

    /**
     * read a remote file as text
     *
     * @param hdfsPath !null remote path to an existing file
     * @return content as text
     */
    @Override
    public String readFromFileSystem(final String hdfsPath) {
        ChannelSftp channel = getFtpChannel();
        try {
            InputStream is = channel.get(hdfsPath);
            return FileUtilities.readInFile(is);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * open a stream from a file
     *
     * @param hdfsPath !null remote path to an existing file
     * @return input stream
     */
    @Override
    public InputStream openPath(final String hdfsPath) {
        ChannelSftp channel = getFtpChannel();
        try {
            InputStream is = channel.get(hdfsPath);
            return is;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
     }

    /**
     * open a stream to a file
     *
     * @param hdfsPath !null remote path to an existing file
     * @return input stream
     */
    @Override
    public OutputStream openPathForWrite(final String hdfsPath) {
        return put(hdfsPath);
    }

    public boolean mkdir(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            channel.mkdir(path);
            return true;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public void put(String path, File in) {
        try {
            put(path, new FileInputStream(in));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    public void get(OutputStream os, String path) {
        InputStream in = get(path);
        try {
            FileUtilities.copyStream(in, os);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    public InputStream get(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            return channel.get(path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }


    public void get(File in, String path) {
        try {
            get(new FileOutputStream(in), path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    public void put(String path, InputStream in) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    public OutputStream put(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            return channel.put(path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }


    public String pwd() {
        ChannelSftp channel = getFtpChannel();
        try {
            return channel.pwd();
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public void rm(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            channel.rm(path);
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    protected SftpATTRS stat(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            SftpATTRS stat = channel.stat(path);
            return stat;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }


    public boolean isDirectory(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            SftpATTRS stat = channel.stat(path);
            if (stat != null)
                return stat.isDir();
            return false;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public boolean isFile(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            SftpATTRS stat = channel.stat(path);
            if (stat != null)
                return !stat.isDir();
            return false;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }

    public boolean exists(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            SftpATTRS stat = channel.stat(path);
            return stat != null;
        } catch (SftpException e) {
            return false;

        }
    }

    public long lastModified(String path) {
        ChannelSftp channel = getFtpChannel();
        try {
            SftpATTRS stat = channel.stat(path);
            int mTime = stat.getMTime();
            return mTime;
        } catch (SftpException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * dummy progress monitor
     */
    public static class MyProgressMonitor implements SftpProgressMonitor {
        public void init(int op, String src, String dest, long max) {

        }

        private long percent = -1;

        public boolean count(long count) {
            return true;
        }

        public void end() {
        }
    }

    public static class MyUserInfo implements UserInfo {
        private final String m_User;
        private final String m_PassPhrase;

        public MyUserInfo(final String pUser, final String pPassPhrase) {
            m_User = pUser;
            m_PassPhrase = pPassPhrase;
        }

        @Override
        public String getPassphrase() {
            return m_PassPhrase;
        }

        @Override
        public String getPassword() {
            return m_PassPhrase;
        }

        @Override
        public boolean promptPassword(final String s) {
            return true;
            //      throw new UnsupportedOperationException("No UI Here");
        }

        @Override
        public boolean promptPassphrase(final String s) {
            throw new UnsupportedOperationException("No UI Here");
        }

        @Override
        public boolean promptYesNo(final String s) {
            return true;
            //throw new UnsupportedOperationException("No UI Here");
        }

        @Override
        public void showMessage(final String s) {
            throw new UnsupportedOperationException("No UI Here");

        }
    }

}
