package com.lordjoe.distributed.util;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.util.PathUtilities
 * User: Steve
 * Date: 10/8/2014
 */
public class PathUtilities {

    private static  IPathReader reader = new FilePathReader();

    public static IPathReader getReader() {
        return reader;
    }

    public static void setReader(final IPathReader pReader) {
        reader = pReader;
    }

    public static class FilePathReader implements IPathReader {
        /**
         * return the lines of text in the file
         *
         * @param path path to the file
         * @return !null array of lines
         */
        @Nonnull
        @Override
        public String[] readTextLines(@Nonnull final String path) {
            List<String> holder = new ArrayList<String>();
            try {
                LineNumberReader rdr = new LineNumberReader(new FileReader(path)) ;
                String line = rdr.readLine();
                while(line != null)  {
                    holder.add(line);
                    line = rdr.readLine();
                }
            }
            catch (IOException e) {
               // return what we have - may be empty
            }
            String[] ret = new String[holder.size()];
            holder.toArray(ret);
            return ret;
         }

        /**
         * is path a directory
         *
         * @param path path to the directory
         * @return
         */
        @Override
        public boolean isDirectory(@Nonnull final String path) {
            return new File(path).isDirectory();
        }

        /**
         * assume the path represents a directory
         *
         * @param path path to the directory
         * @return !null array of paths to files in the directory
         */
        @Nonnull
        @Override
        public String[] readSubPaths(@Nonnull final String path) {
            String[] list = new File(path).list();
              if(list == null)
                  return new String[0];
              for (int i = 0; i < list.length; i++) {
                  list[i] = path + "/" + list[i];
                }
              return list;
         }
    }
}
