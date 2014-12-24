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
public interface IStreamSink
{
    public static final IStreamSink[] EMPTY_ARRAY = {};
    public static final Class THIS_CLASS = IStreamSink.class;

    public String getName();
 
    public OutputStream openOutputStream(String url);


}