package com.lordjoe.distributed.util;

import com.lordjoe.distributed.*;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.util.XYPointSerializer
 * Sample serializer for a simple object
 *  -- NOTE any object which can be expressed as one line of test can
 *     work i this way
 * User: Steve
 * Date: 8/25/2014
 */
public class XYPointSerializer implements IStringSerializer<XYPoint> {

    /**
     * convert a String to a T
     *
     * @param src source string
     * @return possibly null T
     */
    @Override public XYPoint fromString(@Nonnull final String src) {
        return new XYPoint(src);
    }

    @Override public String serialize(@Nonnull final XYPoint src) {
        return src.toString();
    }

    /**
     * if this was Java this would be a default out.append(serialize(src); out.append("\n");
     *
     * @param src source object
     * @param out sink - all exceptions convert to runtimes
     */
    @Override public void append(@Nonnull final XYPoint src, @Nonnull final Appendable out) {
        try {
            out.append(serialize(src));
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param rdr reader
     * @return possibly null T - also set the value of line to the current position
     */
    @Override public XYPoint fromReader(@Nonnull final LineNumberReader rdr) {
        try {
            String line = rdr.readLine();
            if(line == null || line.length() == 0)
                return null;
            return fromString(line);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
}
