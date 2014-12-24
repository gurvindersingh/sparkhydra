package com.lordjoe.distributed.util;

import com.lordjoe.distributed.*;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.util.WordNumberSerializer
 * Sample serializer for a simple object
 * -- NOTE any object which can be expressed as one line of test can
 * work i this way
 * User: Steve
 * Date: 8/25/2014
 */
public class WordNumberSerializer implements IStringSerializer<WordNumber> {

    /**
     * convert a String to a T
     *
     * @param src source string
     * @return possibly null T
     */
    @Override public WordNumber fromString(@Nonnull final String src) {
        return new WordNumber(src);
    }

    @Override public String serialize(@Nonnull final WordNumber src) {
        return src.toString();
    }

    /**
     * if this was Java this would be a default out.append(serialize(src); out.append("\n");
     *
     * @param src source object
     * @param out sink - all exceptions convert to runtimes
     */
    @Override public void append(@Nonnull final WordNumber src, @Nonnull final Appendable out) {
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
    @Override public WordNumber fromReader(@Nonnull final LineNumberReader rdr) {
        try {
            String line = rdr.readLine();
            if (line == null || line.length() == 0)
                return null;
            return fromString(line);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
}
