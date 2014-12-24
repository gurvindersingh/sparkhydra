package org.systemsbiology.data;

import java.io.*;

/**
 * org.systemsbiology.data.IStreamSource
 * written by Steve Lewis
 * on Apr 8, 2010
 * general interface implemented by a class which can source
 * data as streams
 * @see FileStreamFactory
 */
public interface IStreamSource
{
    public static final IStreamSource[] EMPTY_ARRAY = {};
    public static final Class THIS_CLASS = IStreamSource.class;

    public String getName();


    public InputStream openStream(String url);


    public IStreamSource getSubStreamSource(String path);

    public IStreamSource[] getSubStreamSourcesOfType(String extension);

    public IStreamSource[] getSubStreamSources();

    public boolean isPrimarySource();


}