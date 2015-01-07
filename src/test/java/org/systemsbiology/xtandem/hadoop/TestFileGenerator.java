package org.systemsbiology.xtandem.hadoop;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.remotecontrol.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.hadoop.TestFileGenerator
 * User: Steve
 * Date: 1/20/12
 */
public class TestFileGenerator {
    public static final TestFileGenerator[] EMPTY_ARRAY = {};

    public static final String DEFAULT_NAME = "RandomLines.txt";

    public static final long ONE_MEG = 1024 * 1024;
    public static final long ONE_GIG = 1024 * ONE_MEG;
    public static final int LINE_LENGTH = 100;
    public static final Random RND = new Random();


    // NOTE - edit this line to be a sensible location in the current file system
    public static final String INPUT_FILE_PATH = "BigInputLines.txt";
    // NOTE - edit this line to be a sensible location in the current file system
    public static final long DESIRED_LENGTH = 6 * ONE_GIG;
    // NOTE - limits on substring length
    public static final int MINIMUM_LENGTH = 5;
    public static final int MAXIMUM_LENGTH = 32;


    /**
     * create an input file to read
     *
     * @param fs !null file system
     * @param p  !null path
     * @throws java.io.IOException om error
     */
    public static void guaranteeInputFile(File fs, long desiredLength) throws IOException {
        if (fs.exists() && fs.isFile()) {
            if (fs.length() >= desiredLength)
                return;
        }
        PrintWriter ps = new PrintWriter(new FileWriter(fs));
        long showInterval = LINE_LENGTH * (desiredLength / 10000);
        for (long i = 0; i < desiredLength; i += LINE_LENGTH) {
            writeRandomLine(ps, LINE_LENGTH);
            // show progress
            if (i % showInterval == 0) {
                System.err.print(".");

            }
        }
        System.err.println("");
        ps.close();
    }

    /**
     * write a line with a random string of capital letters
     *
     * @param pPs         -  output
     * @param pLineLength length of the line
     */
    public static void writeRandomLine(final PrintWriter pPs, final int pLineLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pLineLength; i++) {
            char c = (char) ('A' + RND.nextInt(26));
            sb.append(c);

        }
        String s = sb.toString();
        pPs.println(s);
    }

    public static final String NAME_NODE = RemoteUtilities.getHost();
    public static final int HDFS_PORT = RemoteUtilities.getPort();
    public static final String BASE_DIRECTORY = "/user/howdah/";

    public static void writeToHDFS(File myFile) {
        IHDFSFileSystem access =   HDFSAccessor.getFileSystem(NAME_NODE, HDFS_PORT);
        String filePath = BASE_DIRECTORY + myFile.getName();
        access.guaranteeDirectory(BASE_DIRECTORY);
        access.writeToFileSystem(filePath, myFile);

    }

    public static void main(String[] args) throws IOException {
        String fileName = DEFAULT_NAME;
        File myFile = new File(fileName);
        if(!myFile.exists())
            return;
        writeToHDFS(myFile);
        if(true)
            return;
        long desiredLength = DESIRED_LENGTH;
        if (args.length > 0)
            fileName = args[0];
        if (args.length > 1)
            desiredLength = Long.parseLong(args[1]);

        guaranteeInputFile(new File(fileName), desiredLength);
    }
}
