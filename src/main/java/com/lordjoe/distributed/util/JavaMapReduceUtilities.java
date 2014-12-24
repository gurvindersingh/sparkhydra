package com.lordjoe.distributed.util;

import com.lordjoe.distributed.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.util.StreamUtilitiesTest
 * User: Steve
 * Date: 8/27/2014
 */
public class JavaMapReduceUtilities {

    /**
     * read a stream as a set of lines
     * @param is
     * @return
     */
    public static String[] readStreamLines(InputStream is) {
        return readReaderLines(new InputStreamReader(is));
    }

    /**
     * read a readers as a set of lines
     * @param rdr
     * @return
     */
    public static String[] readReaderLines(Reader rdr) {
        final BufferedReader br = new BufferedReader(rdr);
        List<String> holder = new ArrayList<String>();
        try {
            String line = br.readLine();
            while(line != null)   {
                holder.add(line);
                line = br.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    /**
     * Drop the %^(%^&*%$ IOException
     * @param br
     * @return
     */
    public static String readLineRuntimeException(BufferedReader br)   {
        try {
            return br.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

      public static Iterable<KeyValueObject<String,String>>  fromPath(final String pathname,InputStream is) {
           final  BufferedReader br = new BufferedReader(new InputStreamReader(is));



           return new Iterable<KeyValueObject<String,String>>() {
                /**
                * Returns an iterator over a set of elements of type T.
                *
                * @return an Iterator.
                */
               @Override public Iterator<KeyValueObject<String,String>> iterator() {
                   return new Iterator<KeyValueObject<String,String>>() {
                       String line = readLineRuntimeException(br);

                       public boolean hasNext() {
                           return line != null;
                       }

                       /**
                        * Returns the next element in the iteration.
                        *
                        * @return the next element in the iteration
                        * @throws java.util.NoSuchElementException if the iteration has no more elements
                        */
                       @Override public KeyValueObject<String,String> next() {
                           String ret = line;
                           line = readLineRuntimeException(br);
                           return new KeyValueObject<String,String>(pathname,ret);
                       }


                       @Override public void remove() {
                           throw new UnsupportedOperationException("Remove not supported");
                       }
                   };
               }
           };
    }


}
