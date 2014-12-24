package com.lordjoe.distributed.util;

import javax.annotation.*;
import java.io.*;

/**
 * org.systemsbiology.xtandem.hadoop.IPathReader
 * Solves a generic problem of I have a path and am not sure
 * if it is Hadoop,Spark, file system ...
 * User: Steve
 * Date: 10/8/2014
 */
public interface IPathReader extends Serializable {

    /**
     * return the lines of text in the file
     * @param path path to the file
     * @return  !null array of lines
     */
     public @Nonnull String[] readTextLines(@Nonnull String path);


    /**
     * is path a directory
     * @param path   path to the directory
     * @return
     */
     public boolean isDirectory(@Nonnull String path);

    /**
     * assume the path represents a directory
     * @param path path to the directory
     * @return  !null array of paths to files in the directory
     */
     public @Nonnull String[] readSubPaths(@Nonnull String path);

}
