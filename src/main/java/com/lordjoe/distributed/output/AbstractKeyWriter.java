package com.lordjoe.distributed.output;

import java.io.*;

/**
 * com.lordjoe.distributed.output.AbstractKeyWriter
 * <p/>
 * User: Steve
 * Date: 10/14/2014
 */
public abstract class AbstractKeyWriter<K> implements Closeable, Serializable {

     private String outPath;
    private transient PrintWriter writer;

    protected AbstractKeyWriter(final String pOutPath) {
        outPath = pOutPath;
    }


    protected abstract String buildOutPath(final K pPp);


    protected PrintWriter getWriter() {
        if (writer == null)
            writer = buildWriter();
        return writer;
    }

    protected PrintWriter buildWriter() {

        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public String getOutPath() {
        return outPath;
    }

    /**
     * uses when appending the output of one writer onto another
     * @return
     */
    protected LineNumberReader readContents()
    {
        if (writer == null)
            return new LineNumberReader(new StringReader("")); // empty reader

        throw new UnsupportedOperationException("Fix This"); // ToDo

    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if (writer != null)
            writer.close();
        if (true)
            throw new UnsupportedOperationException("Rename out path"); // ToDo
        outPath = null;
    }

}
