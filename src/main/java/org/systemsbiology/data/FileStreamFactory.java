package org.systemsbiology.data;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.data.FileStreamFactory
 * written by Steve Lewis
 * on Apr 8, 2010
 */
public class FileStreamFactory implements IStreamSource,IStreamSink, IStreamFactory
{
    public static final FileStreamFactory[] EMPTY_ARRAY = {};
    public static final Class THIS_CLASS = FileStreamFactory.class;


    private final File m_BaseDirectory;

    public FileStreamFactory(File pBaseDirectory) {
        m_BaseDirectory = pBaseDirectory;
    }

    /**
     * make a factory in a subdirectory given by the url
     * @param url
     * @return
     */
    public IStreamFactory getStreamFactory (String url)
    {
        File next = new File(getBaseDirectory(),url);
        if(next.exists()) {
            if(next.isFile())
                throw new IllegalArgumentException("Cannot overwrite file to directory" + next.getAbsolutePath());
        }
        else {
            if(!next.mkdirs())
                throw new IllegalArgumentException("Cannot create directory " + next.getAbsolutePath());
        }
        return new FileStreamFactory(next);
    }


    public String getName() {
        return getBaseDirectory().getName();
    }

    public File getBaseDirectory() {
        return m_BaseDirectory;
    }

    public InputStream openStream(String url) {
        File directory = getBaseDirectory();
        try {
            if(directory.isFile() && directory.exists() )
                return new FileInputStream(directory) ;
        }
        catch (FileNotFoundException e) {
            return null;
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public OutputStream openOutputStream(String url) {
         File out = new File(getBaseDirectory(),url);
         if(!out.getParentFile().exists() && !out.getParentFile().mkdirs())
             return null;
         try {
             System.out.println("Opening output file at " + out.getAbsolutePath());
             return new FileOutputStream(out);
         }
         catch (FileNotFoundException e) {
             return null;
         }

     }
  

    public IStreamSource getSubStreamSource(String path) {
        File out = new File(getBaseDirectory(),path);
        if(!out.getParentFile().mkdirs())
            return null;
        return new FileStreamFactory(out);
    }


    public IStreamSink getSubStreamSink(String path) {
        File out = new File(getBaseDirectory(),path);
        if(!out.getParentFile().mkdirs())
            return null;
        return new FileStreamFactory(out);
    }


    public IStreamSource[] getSubStreamSourcesOfType(String extension) {
        File directory = getBaseDirectory();
        String[] files = directory.list();
        if(files == null)
            return new IStreamSource[0];
        List<IStreamSource> holder = new ArrayList<IStreamSource>();
        accumulateSubStreamSourcesOfType( holder,extension);
        return holder.toArray(new IStreamSource[holder.size()]);

    }

    protected void accumulateSubStreamSourcesOfType(List<IStreamSource> pHolder, String pExtension) {
        IStreamSource[] sources = getSubStreamSources();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < sources.length; i++) {
            IStreamSource source = sources[i];
            if(source.isPrimarySource()) {
                if(source.getName().endsWith(pExtension))
                    pHolder.add(source);
            }
            else {
                ((FileStreamFactory)source).accumulateSubStreamSourcesOfType(pHolder,pExtension);
            }
        }

    }


    public IStreamSource[] getSubStreamSources() {
        File directory = getBaseDirectory();
        String[] files = directory.list();
        if(files == null)
            return new IStreamSource[0];
        List<IStreamSource> holder = new ArrayList<IStreamSource>();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            File nf = new File(directory,file);
            holder.add(new FileStreamFactory(nf));
        }
        return holder.toArray(new IStreamSource[holder.size()]);

    }

    public boolean isPrimarySource() {
        return false;
    }
}
