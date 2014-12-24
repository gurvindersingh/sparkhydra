package org.systemsbiology.data;

import java.io.*;

/**
 * org.systemsbiology.data.IStreamFactory
 * Absracts access to the files system to allow access from files or
 * server/url
 * written by Steve Lewis
 * on Apr 14, 2010
 */
public interface IStreamFactory
{
    public static final IStreamFactory[] EMPTY_ARRAY = {};
    public static final Class THIS_CLASS = IStreamFactory.class;

    /**
     * build a sub factory
     * @param url path
     * @return
     */
    public IStreamFactory getStreamFactory (String url);

    /**
     * open a stream to read a url
     * @param url !nukll existing url
     * @return !numm stream
     */
    public InputStream openStream(String url);

    public OutputStream openOutputStream(String url);

    public IStreamSink getSubStreamSink(String path);

    /**
     * StreamSource is like a file
     * @param path
     * @return
     */
    public IStreamSource getSubStreamSource(String path);

    /**
     * list all files or available data  of a known type or extension
     * @return
     */
    public IStreamSource[] getSubStreamSourcesOfType(String extension);

    /**
     * list all files or available data
     * @return
     */
    public IStreamSource[] getSubStreamSources();

    /**
     *
     * @return
     */
    public boolean isPrimarySource();
}