package org.systemsbiology.hadoop;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.hadoop.HDFSStreamOpener
 * User: steven
 * Date: 3/9/11
 */
public class HDFSStreamOpener implements IStreamOpener {
    public static final HDFSStreamOpener[] EMPTY_ARRAY = {};



    private final HDFSAccessor m_Accesor;
    private  String m_BaseDirectory;

    @SuppressWarnings("UnusedDeclaration")
    public HDFSStreamOpener(final HDFSAccessor pAccesor, final String pBaseDirectory) {
        m_Accesor = pAccesor;
        m_BaseDirectory = pBaseDirectory;
    }

    public HDFSStreamOpener(Configuration config) {
        try {
            FileSystem fs = FileSystem.get(config);
       //     String host = config.get(HadoopUtilities.HOST_KEY);
      //      String portStr = config.get(HadoopUtilities.HOST_PORT_KEY);
      //       if(host == null)    {
                m_Accesor = new HDFSAccessor(fs);
//            }
//            else {
//                 int port = 0;
//                   if(portStr != null)
//                       port = Integer.parseInt(portStr);
//                 m_Accesor = new HDFSAccessor(host,port);
//            }
            m_BaseDirectory = config.get(DefaultParameterHolder.PATH_KEY);
            if(m_BaseDirectory == null)
              //  m_BaseDirectory = ".";
               throw new IllegalStateException(DefaultParameterHolder.PATH_KEY + "not defined");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public HDFSAccessor getAccesor() {
        return m_Accesor;
    }

    public String getBaseDirectory() {
        if(m_BaseDirectory != null)
            return m_BaseDirectory;
        return System.getProperty("user.dir"); // useful in local mode
    }

    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(final String fileName, final Object... otherData) {
        String hdsfPath = buildFilePath(fileName);
        Path path = new Path(hdsfPath);
        HDFSAccessor accesor = getAccesor();
        return accesor.openFileForRead(path);
    }

       /**
     * open a file from a string for writing
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */

       @SuppressWarnings("UnusedDeclaration")
    public OutputStream openForWrite(final String fileName, final Object... otherData) {
         String hdsfPath = buildFilePath(fileName);
         Path path = new Path(hdsfPath);
        HDFSAccessor accesor = getAccesor();
        return accesor.openFileForWrite(path);
    }

    public String buildFilePath(  String fileName) {
        fileName = fileName.replace("\\","/");
        String hdsfPath = getBaseDirectory();
        if(!hdsfPath.endsWith("/"))
            hdsfPath += "/";
        if(fileName.startsWith(hdsfPath))   {
            return fileName; // absolute
        }
        if(fileName.startsWith("/"))
             hdsfPath = fileName;
        else {
            if(fileName.contains(":"))  {
                // pc absolute
                fileName = HadoopUtilities.asLocalFile(fileName);
            }
            hdsfPath += fileName;
        }
        return hdsfPath;
    }
}
