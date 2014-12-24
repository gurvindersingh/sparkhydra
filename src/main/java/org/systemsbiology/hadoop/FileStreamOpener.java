package org.systemsbiology.hadoop;

import java.io.*;

/**
 * org.systemsbiology.hadoop.FileStreamOpener
 *   implement file opener by opening a file
 * @author Steve Lewis
 * @date Mar 8, 2011
 */
public class FileStreamOpener  implements IStreamOpener
{
    public static FileStreamOpener[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = FileStreamOpener.class;

    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(String fileName, Object... otherData)
    {
        File f = new File(fileName);
        String path = f.getAbsolutePath();
        if(!f.exists() || !f.canRead() || f.isDirectory())
            return null;
        try {
            return new FileInputStream(f);
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }
}
