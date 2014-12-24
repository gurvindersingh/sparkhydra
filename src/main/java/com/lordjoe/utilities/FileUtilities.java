// in order to read files in internal Explorer we need to assert
// make a ms Security manager call
// import com.ms.security.*;

/**{ file
 @name FileUtilities.java
 @function FileUtilities offer a number of services relating to files including
 file dialog, directory enumerators and keeping track of open files
 @author> Steven M. Lewis
 @copyright>
  ************************
  *  Copyright (c) 1996,97,98
  *  Steven M. Lewis
  *  www.LordJoe.com
 ************************
 @date> Mon Jun 22 21:48:27 PDT 1998
 @version> 1.0
 }*/
package com.lordjoe.utilities;


import com.google.common.io.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.List;


/**
 * { class
 *
 * @name FileUtilities
 * @function FileUtilities offer a number of services relating to files including
 * file dialog, directory enumerators and keeping track of open files
 * }
 */
public abstract class FileUtilities {

    //- *******************
    //- Methods
    public static final File[] EMPTY_FILES = new File[0];

    /**
     * read a file into an array of bytes
     * see http://www.java-tips.org/java-se-tips/java.io/reading-a-file-into-a-byte-array.html
     * see http://oreilly.com/catalog/javacrypt/chapter/ch06.html
     *
     * @param theFile !null readable file of length <  Integer,MAX_VALUE
     * @return !null array of bytes
     */
    public static byte[] readFileBytes(File theFile) {
        try {
            // Get the size of the file
            long length = theFile.length();

            if (length > Integer.MAX_VALUE) {
                // File is too large
            }
            InputStream is = new FileInputStream(theFile);

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + theFile.getName());
            }

            // Close the input stream and return bytes
            is.close();
            return bytes;


        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static final int BUFFER = 8192;

    /**
     * read the bytes from an input stream
     *
     * @param fileStream !null open input stream
     * @return !null byte array
     */
    public static byte[] readInBytes(final InputStream fileStream) {

        BufferedInputStream buffer = null;
        ByteArrayOutputStream byteOut = null;
        byte data[] = new byte[BUFFER];

        try {

            buffer = new BufferedInputStream(fileStream);
            byteOut = new ByteArrayOutputStream();

            int count;
            while ((count = buffer.read(data, 0, BUFFER)) != -1) {
                byteOut.write(data, 0, count);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        finally {
            guaranteeClosed(buffer);
            guaranteeClosed(byteOut);
        }
        return byteOut.toByteArray();
    }

    /**
     * guarantee an input stream is closed - deal with all exceptions
     *
     * @param is possibly null inputstream
     */
    public static void guaranteeClosed(OutputStream is) {
        if (is == null)
            return;
        try {
            is.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * guarantee an input stream is closed - deal with all exceptions
     *
     * @param is possibly null inputstream
     */
    public static void guaranteeClosed(InputStream is) {
        if (is == null)
            return;
        try {
            is.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * guarantee an input stream is closed - deal with all exceptions
     *
     * @param is possibly null inputstream
     */
    public static void guaranteeClosed(Reader is) {
        if (is == null)
            return;
        try {
            is.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * make sure a file f exists, is  a file and can be read
     *
     * @param f !null file
     */
    public static void guaranteeExistingFile(File f) throws IllegalArgumentException {
        if (f == null)
            throw new IllegalArgumentException("null argument to guaranteeExistingFile");
        if (!f.exists())
            throw new IllegalArgumentException("File " + f.getAbsolutePath() + " does not exist");
        if (!f.isFile())
            throw new IllegalArgumentException("File " + f.getAbsolutePath() + " is not a file");
        if (!f.canRead())
            throw new IllegalArgumentException("File " + f.getAbsolutePath() + " cannot be read");
    }

    /**
     * generate an MD5 digest of a specific file
     *
     * @param theFile !null readable file of length <  Integer,MAX_VALUE
     * @return !null MD5 bytes
     */
    public static byte[] buildMD5Digest(File theFile) {
        try {
            // Calculate the digest for the given file.
            FileInputStream is = new FileInputStream(theFile);
            return buildMD5Digest(is);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * generate an MD5 digest of a specific file
     *
     * @param pIs !null open input stream
     * @return !null MD5 bytes
     */
    public static byte[] buildMD5Digest(final InputStream pIs) {
        DigestInputStream in = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            in = new DigestInputStream(
                    pIs, md);
            byte[] buffer = new byte[8192];
            while (in.read(buffer) != -1)
                ;

            byte[] thedigest = md.digest();

            return thedigest;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);

        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        finally {
            guaranteeClosed(in);
        }
    }

    /**
     * generate an MD5 digest frmy bytes
     *
     * @param pIs !null byte array
     * @return !null MD5 bytes
     */
    public static byte[] buildMD5Digest(final byte[] pIs) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(pIs);

            return thedigest;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * test the equivalence of two arrays
     *
     * @param t1  !null array1
     * @param t2  !null array2
     * @param <T> array  type
     * @return true if lenght and membera are equal
     */
    public static <T> boolean equivalent(T[] t1, T[] t2) {
        if (t1.length != t2.length)
            return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i])
                return false;

        }
        return true;
    }

    /**
     * test the equivalence of two arrays
     *
     * @param t1 !null array1
     * @param t2 !null array2
     * @return true if lenght and membera are equal
     */
    public static boolean equivalent(byte[] t1, byte[] t2) {
        if (t1.length != t2.length)
            return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i])
                return false;

        }
        return true;
    }

    /**
     * test the equivalence of two arrays
     *
     * @param t1  !null array1
     * @param t2  !null array2
     * @param <T> array  type
     * @return true if lenght and membera are equal
     */
    public static boolean equivalent(int[] t1, int[] t2) {
        if (t1.length != t2.length)
            return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i])
                return false;

        }
        return true;
    }

    /**
     * test the equivalence of two arrays
     *
     * @param t1  !null array1
     * @param t2  !null array2
     * @param <T> array  type
     * @return true if lenght and membera are equal
     */
    public static boolean equivalent(long[] t1, long[] t2) {
        if (t1.length != t2.length)
            return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i])
                return false;

        }
        return true;
    }

    /**
     * test the equivalence of two arrays
     *
     * @param t1  !null array1
     * @param t2  !null array2
     * @param <T> array  type
     * @return true if lenght and membera are equal
     */
    public static boolean equivalent(short[] t1, short[] t2) {
        if (t1.length != t2.length)
            return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i])
                return false;

        }
        return true;
    }

    /**
     * test the equivalence of two arrays
     *
     * @param t1  !null array1
     * @param t2  !null array2
     * @param <T> array  type
     * @return true if lenght and membera are equal
     */
    public static boolean equivalent(double[] t1, double[] t2) {
        if (t1.length != t2.length)
            return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i])
                return false;

        }
        return true;
    }

    /**
     * test the equivalence of two arrays
     *
     * @param t1  !null array1
     * @param t2  !null array2
     * @param <T> array  type
     * @return true if lenght and membera are equal
     */
    public static boolean equivalent(float[] t1, float[] t2) {
        if (t1.length != t2.length)
            return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i])
                return false;

        }
        return true;
    }

    /**
     * { method
     *
     * @param directory <Add Comment Here>
     * @return the files
     * }
     * @name getAllFiles
     * @function get all the files in the directory
     * @UnusedParam> FileName starting directory
     */
    public static File[] getAllFiles(File directory) {
        List holder = new ArrayList();
        accumulateFiles(directory, holder);
        File[] ret = new File[holder.size()];
        holder.toArray(ret);
        return (ret);

    }


    /**
     * return the user directory as a file
     *
     * @return
     */

    public static File getUserDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    /**
     * read a properties file
     *
     * @param propName name of an existing and readable  properties file
     * @return non-null properties
     * @throws IllegalArgumentException if the name is not a readable file
     */
    public static Properties readProperties
    (String
             propName) throws IllegalArgumentException {
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(propName);
            Properties ret = new Properties();
            ret.load(fs);
            return ret;
        }
        catch (RuntimeException ex) // tempdt Why do we need this catch block?  It also is doing a poor job and not using a logger.
        {
            // ex.printStackTrace();
            throw ex;
        }
        catch (IOException ex) // tempdt This really isn't an IllegalArgumentException.  Dave thinks this should be fixed.  Dave can explain if desired.
        {
            throw new IllegalArgumentException("Cannot load properties from " + propName);
        }
        finally {
            try {
                if (fs != null)
                    fs.close();
            }
            catch (IOException e) {

            }
        }
    }

    /**
     * save a properties file
     *
     * @param propName      name of an existing and readable  properties file
     * @param propsnun-null properties file
     * @throws IllegalArgumentException if the name is not a readable file
     */
    public static void saveProperties
    (String
             propName, Properties
             props)
            throws IllegalArgumentException {
        try {
            FileOutputStream fs = new FileOutputStream(propName);
            props.store(fs, null);
        }
        catch (RuntimeException ex) // tempdt Why do we need this catch block?  It also is doing a poor job and not using a logger.
        {
            // ex.printStackTrace();
            throw ex;
        }
        catch (IOException ex) // tempdt This really isn't an IllegalArgumentException.  Dave thinks this should be fixed.  Dave can explain if desired.
        {
            throw new IllegalArgumentException("Cannot save properties to " + propName);
        }
    }


    public static File makeTempFile(File in) {
        if (!in.exists())
            throw new IllegalArgumentException("File " + in.getAbsolutePath() + " does not exist");
        File ret = new File(in.getAbsolutePath() + ".tmp");
        if (ret.exists())
            ret.delete();
        return ret;
    }

    /**
     * rename in to a temp holder
     *
     * @param in
     * @return
     */
    private static File makeTempHoldingFile(File in) {
        if (!in.exists())
            throw new IllegalArgumentException("File " + in.getAbsolutePath() + " does not exist");
        File ret = new File(in.getAbsolutePath() + ".tmpHolding");
        if (ret.exists())
            ret.delete();
        if (!in.renameTo(ret))
            throw new IllegalStateException("cannot rename " + in.getAbsolutePath() + " to " + ret);
        return ret;
    }

    /**
     * internam method to rename tmp to in and remove in
     *
     * @param in
     * @param tmp
     */
    private static void safeRenameFromTmp(File in, File tmp) {
        if (!tmp.exists())
            throw new IllegalArgumentException("Temp File " + tmp.getAbsolutePath() + " does not exist");
        File holding = makeTempHoldingFile(in);
        if (!tmp.renameTo(in))
            throw new IllegalStateException("cannot rename " + tmp.getAbsolutePath() + " to " + in);
        holding.delete();
    }


    public static void removeWhiteSpace(File in) {
        PrintWriter out = null;
        try {
            File tmp = makeTempFile(in);
            LineNumberReader rdr = new LineNumberReader(new FileReader(in));
            out = new PrintWriter(tmp);
            String line = rdr.readLine();
            while (line != null) {
                line = line.trim();
                out.print(line);
                line = rdr.readLine();
            }
            rdr.close();
            out.close();
            out = null;
            safeRenameFromTmp(in, tmp);  // now rename the tmp file as the original

        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        finally {
            if (out != null) {
                out.close();
            }
        }

    }

    private static String[] gRequiredFiles;

    public static void addRequiredFile(String s) {
        synchronized (gRequiredFiles) {
            String[] newReqFiles = new String[gRequiredFiles.length + 1];
            System.arraycopy(gRequiredFiles, 0, newReqFiles, 0, gRequiredFiles.length);
            newReqFiles[gRequiredFiles.length] = s;
            gRequiredFiles = newReqFiles;
        }
    }

    public static void addRequiredFiles
            (String[] s) {
        synchronized (gRequiredFiles) {
            String[] newReqFiles = new String[gRequiredFiles.length + s.length];
            System.arraycopy(gRequiredFiles, 0, newReqFiles, 0, gRequiredFiles.length);
            System.arraycopy(s, 0, newReqFiles, gRequiredFiles.length, s.length);
            gRequiredFiles = newReqFiles;
        }
    }


    /**
     * make sure all files registering as being required are present
     *
     * @throws IllegalStateException if a required file is not present
     */
    public static void verifyRequiredFiles() {
        StringBuilder sb = new StringBuilder();


        synchronized (gRequiredFiles) {
            for (int i = 0; i < gRequiredFiles.length; i++) {
                String requiredFile = gRequiredFiles[i];
                String error = locateRequiredFile(requiredFile);
                if (!Util.isEmptyString(error)) {
                    if (sb.length() > 0)
                        sb.append("\n");
                    sb.append(error);
                }
            }
        }
        if (sb.length() == 0)
            return;
        throw new IllegalStateException(" Cannot find Required File " +
                sb.toString());

    }

    protected static String locateRequiredFile(String s) {
        String testStr = buildFileName(s);
        File testFile = new File(testStr);
        if (testFile.exists() && testFile.canRead())
            return null;
        return s + " as " + testStr;  // not found
    }

    protected static String buildFileName(String s) {
        int index = s.lastIndexOf("%");
        if (index == -1)
            return s;
        String propPart = s.substring(0, index + 1);
        String FilePart = s.substring(index + 1);
        String resolved = resolveProp(propPart);
        return resolved + FilePart;
    }

    protected static String resolveProp
            (String
                     s) {
        if (!s.startsWith("%") || !s.endsWith("%"))
            throw new IllegalArgumentException("resolved shoule be like %user.dir%");
        String PropName = s.substring(1, s.length() - 1);
        String propValue = System.getProperty(PropName);
        if (propValue == null)
            throw new IllegalArgumentException("Cannnot find sytem property " + PropName);
        propValue = propValue.replace('\\', '/');
        return propValue;
    }

    // tempdt Don't use this method without checking with Dave first.
    // tempdt Dave is trying to deprecate this because it has an ex.printStackTrace() call (developers should use loggers instead).

    // tempdt This method is not following Dave's I/O handling framework conventions.  Hence, I discourage using this API.  Dave is happy to discuss why (and/or how to mod this API to bring it up to speed).

    /**
     * Recursively creates directories hierarchy of any depth
     *
     * @param filePath
     * @throws IOException
     */
    public static void createDirectores
    (String
             filePath) throws IOException {
        File file = new File(filePath);
        File absFile = file.getAbsoluteFile();
        File parentFile = absFile.getParentFile();
        if (parentFile.exists() && parentFile.isDirectory()) {
            absFile.mkdir();
        }
        else if (parentFile.exists() && !parentFile.isDirectory()) {
            throw new IOException(
                    "File with name " + parentFile.getAbsolutePath() + " already exists " +
                            "specify a different name for directory.");
        }
        else {
            createDirectores(parentFile.getAbsolutePath());
            absFile.mkdir();
        }
    }

    /**
     * convert an array of file names  into an array of files
     *
     * @param fileNames non-null list of good file names
     * @return non-null list of files
     */
    public static File[] stringsToFiles
    (String[] fileNames) {
        File[] ret = new File[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            ret[i] = new File(fileName);
        }
        return ret;
    }


    protected static void accumulateFiles(File directory, List holder) {
        File[] files = directory.listFiles();
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory())
                accumulateFiles(file, holder);
            else
                holder.add(file);
        }
    }


    public static File getTemporaryDirectory
            () {
        String tempDirName = System.getProperty("java.io.tmpdir");
        File ret = new File(tempDirName);
        if (!ret.exists())
            ret.mkdirs();
        return ret;
    }

    /**
     * make a file in the system temporary directory
     *
     * @param name
     * @return
     */
    public static File makeTemporaryFile
    (String
             name) {
        return new File(getTemporaryDirectory(), name);
    }

    /**
     * convert a file to a URL
     *
     * @param f non-null file
     * @return non-null url
     * @throws RuntimeException on error
     */
    public static URL toURL(File f) {
        URI uri = f.toURI();
        try {
            return uri.toURL();
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @return the files
     * }
     * @name getAllFiles
     * @function get all the files in the directory
     * @UnusedParam> FileName starting directory
     */
    public static String[] getAllFiles(String DirectoryName) {
        return (getAllFilesWithFilter(DirectoryName, null));
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @return the files
     * }
     * @name getFiles
     * @function get all the files in the directory
     * @UnusedParam> FileName starting directory
     */
    public static String[] getFiles
    (String
             DirectoryName) {
        return (getFilesWithFilter(DirectoryName, null));
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     *                      }
     * @name expungeDirectory
     * @function delete a directory and all its contents
     */
    public static void expungeDirectory(String DirectoryName) {
        expungeDirectory(new File(DirectoryName));
    }

    /**
     * { method
     *
     * @param TheDir <Add Comment Here>
     *               }
     * @name expungeDirectory
     * @function delete a directory and all its contents
     */
    public static void expungeDirectory(File TheDir) {
        if (TheDir.exists()) {
            expungeDirectoryContents(TheDir);
            TheDir.delete();
        }

    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     *                      }
     * @name expungeDirectory
     * @function delete a directory and all its contents
     */
    public static void expungeDirectoryContents(File TheDir) {
        String[] items = TheDir.list();
        for (int i = 0; i < items.length; i++) {
            File Test = new File(TheDir, items[i]);
            if (Test.isFile()) {
                Test.delete();
            }
            else {
                expungeDirectory(Test);
            }
        }

    }


    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     test extension
     * @return the Files
     * }
     * @name getAllDirectoriesWithExtension
     * @function get all files ending in ext at current level
     * @UnusedParam> FileName start directory
     */
    public static String[] getAllDirectoriesWithExtension(String DirectoryName, String Extension) {
        File TestFile = new File(DirectoryName);
        if (TestFile.isDirectory()) {
            Vector ret = new Vector();
            getAllDirectoriesWithExtension(TestFile, "", Extension, ret);
            String out[] = new String[ret.size()];
            for (int i = 0; i < ret.size(); i++) {
                String TestString = (String) ret.elementAt(i);
                if (TestString.equals(""))
                    out[i] = DirectoryName;
                else
                    out[i] = TestString;
            }
            return (out);
        }
        else {
            return (new String[0]);
        }
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     test extension
     * @return the Files
     * }
     * @name getAllDirectoriesWithExtension
     * @function get all files ending in ext at current level
     * @UnusedParam> FileName start directory
     */
    protected static void getAllDirectoriesWithExtension
    (File
             Base, String
             DirectoryName,
     String
             Extension, Vector
             holder) {
        File TestFile = new File(Base, DirectoryName);
        if (!TestFile.isDirectory()) {
            return;
        }
        if (hasFileWithExtension(TestFile, Extension))
            holder.addElement(DirectoryName);

        String[] Files = TestFile.list();
        for (int j = 0; j < Files.length; j++) {
            String test = pathConcat(DirectoryName, Files[j]);
            getAllDirectoriesWithExtension(Base, test, Extension, holder);
        }
    }

    /**
     * { method
     *
     * @param DirectoryName non-null name of the base directory
     * @param Name          non-null requested file name
     * @return the Files
     * }
     * @name getAllFilesWithName
     * @function get all files ending in ext at current level
     */
    public static String[] getAllFilesWithName
    (String
             DirectoryName, String
             Name) {
        return (getAllFilesWithName(new File(DirectoryName), Name));
    }

    /**
     * { method
     *
     * @param DirectoryName non-null File of the base directory
     * @param Name          non-null requested file name
     * @return the Files
     * }
     * @name getAllFilesWithName
     * @function get all files ending in ext at current level
     */
    public static String[] getAllFilesWithName(File
                                                       DirectoryName, String Name) {
        FilenameFilter TheFilter = (FilenameFilter) (new EndsWithFilter(Name));
        return (getAllFilesWithFilter(DirectoryName, TheFilter));
    }

    /**
     * read up to MaxLines fron the file
     *
     * @param f        existing readable text file
     * @param maxLines maximim lines to read
     * @return !null array of line contents
     * @throws RuntimeException on error
     */
    public static String[] readNLines(File f, int maxLines) throws RuntimeException {
        try {
            return readNLines(new FileInputStream(f), maxLines);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * read up to MaxLines fron the file
     *
     * @param f        open Stream
     * @param maxLines maximim lines to read
     * @return !null array of line contents
     * @throws RuntimeException on error
     */
    public static String[] readNLines(InputStream f, int maxLines) {
        try {
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(f));
            List<String> holder = new ArrayList<String>();
            String line = rdr.readLine();
            int nLines = 0;
            while (line != null) {
                holder.add(line);
                if (++nLines > maxLines)
                    break;
                line = rdr.readLine();
            }
            String[] ret = new String[holder.size()];
            holder.toArray(ret);
            return ret;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        finally {
            try {
                f.close();
            }
            catch (IOException e) {
                // ignore issues

            }
        }
    }

    /**
     * return the last modified matching file in the user directory
     *
     * @param extension - end of the file name
     * @return pssibly null file
     */
    public static File getLatestFileWithExtension(String extension) {
        return getLatestFileWithExtension(getUserDirectory(), extension);
    }

    /**
     * return the last modified matching file
     *
     * @param dir       !null existing dirctory
     * @param extension - end of the file name
     * @return pssibly null file
     */
    public static File getLatestFileWithExtension(File dir, String extension) {
        File ret = null;
        String[] names = getAllFilesWithName(dir, extension);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            File test = new File(name);
            if (ret == null) {
                ret = test;
            }
            else {
                boolean testExists = test.exists();
                boolean retExists = ret.exists();

                long testLm = test.lastModified();
                long myLm = ret.lastModified();
                if (myLm < testLm)
                    ret = test;
            }
        }
        return ret;
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     test extension
     * @return the Files
     * }
     * @name getAllFilesWithExtension
     * @function get all files ending in ext at current level
     * @UnusedParam> FileName start directory
     */
    public static String[] getAllFilesWithExtension(String DirectoryName, String Extension) {
        if (Extension == null || Extension.length() == 0) {
            return (getAllFilesWithFilter(DirectoryName, null));
        }
        else {
            FilenameFilter TheFilter = (FilenameFilter) (new HasExtensionFilter(Extension));
            return (getAllFilesWithFilter(DirectoryName, TheFilter));
        }
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     test extension
     * @return the Files
     * }
     * @name getAllFilesWithExtension
     * @function get all files ending in ext at current level
     * @UnusedParam> FileName start directory
     */
    public static String[] getAllFilesWithExtension
    (File
             Directory, String
             Extension) {
        if (Extension != null && Extension.length() > 0) {
            FilenameFilter TheFilter = (FilenameFilter) (new HasExtensionFilter(Extension));
            return (getAllFilesWithFilter(Directory, TheFilter));
        }
        else {
            return (getAllFilesWithFilter(Directory, null));
        }
    }

    public static void renameAllFilesWithExtension
            (String
                     DirectoryName, String
                     Extension,
             String
                     NewExtension) {
        String[] files = getAllFilesWithExtension(DirectoryName, Extension);
        for (int i = 0; i < files.length; i++)
            setExtension(files[i], NewExtension);
    }

    public static void setExtension
            (String
                     FileName, String
                     NewExtension) {
        File TheFile = new File(FileName);
        String NewName = getBaseName(FileName) + "." + NewExtension;
        TheFile.renameTo(new File(NewName));
    }

    public static String getBaseName
            (String
                     FileName) {
        int index = FileName.lastIndexOf(".");
        if (index == -1)
            return (FileName);
        int sepIndex = FileName.lastIndexOf(File.pathSeparatorChar);
        if (index < sepIndex)
            return (FileName); // well I suppose directories can have .
        return (FileName.substring(0, index));
    }

    /**
     * remove all directories even if more then one
     *
     * @param FileName
     * @return
     */
    public static String dropDirectories
    (String
             FileName) {
        FileName = FileName.replace('\\', '/');
        int index = FileName.lastIndexOf('/');
        if (index == -1)
            return FileName;
        if (index == FileName.length() - 1)
            return "";
        return FileName.substring(index + 1);
    }

    /**
     * remove all extensions even if more then one
     *
     * @param FileName
     * @return
     */
    public static String dropExtensions
    (String
             FileName) {
        int index = FileName.lastIndexOf(".");
        while (index > -1) {
            FileName = FileName.substring(0, index);
            index = FileName.lastIndexOf(".");
        }
        return (FileName);
    }


    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     filter to apply
     * @return true if ends in extension
     * }
     * @name hasFileWithExtension
     * @function get all files at the current level passing the filter
     * @UnusedParam> FileName start directory
     */
    public static boolean hasFileWithExtension
    (String
             DirectoryName, String
             Extension) {
        File TestFile = new File(DirectoryName);
        return (hasFileWithExtension(TestFile, Extension));
    }


    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     filter to apply
     * @return true if ends in extension
     * }
     * @name hasFileWithExtension
     * @function get all files at the current level passing the filter
     * @UnusedParam> FileName start directory
     */
    public static boolean hasFileWithExtension
    (File
             TestFile, String
             Extension) {
        if (!TestFile.exists()) {
            return (false);
        }
        FilenameFilter TheFilter = (FilenameFilter) (new HasExtensionFilter(Extension));
        String[] Files = TestFile.list(TheFilter);
        if (Files == null) {
            return (false);
        }
        if (Files.length > 0) {
            return (true);
        }
        return (false);
    }

    /**
     * { method
     *
     * @param Directory the directory
     * @param Extension test extension
     * @return latest modifiaction time
     * }
     * @name getLatestModifiedWithExtension
     * @function return the latest date on a file in the directory having
     * This was written as a utility to help makefile like programs
     * the designated extension
     */
    public static long getLatestModifiedWithExtension(File
                                                              Directory, String Extension) {
        String[] Files = Directory.list();
        long ret = 0;
        for (int i = 0; i < Files.length; i++) {
            if (Files[i].endsWith(Extension)) {
                File test = new File(Directory, Files[i]);
                if (!test.isDirectory())
                    ret = Math.max(ret, test.lastModified());
            }
        }
        return (ret);
    }

    /**
     * { method
     *
     * @param Directory the directory
     * @param testNames File names to test
     * @return latest modifiaction time
     * }
     * @name getLatestFileFromList
     * @function return the latest date on a file in the directory having
     * This was written as a utility to help makefile like programs
     * the designated extension
     */
    public static long getLatestFileFromList
    (File
             Directory, String[] testNames) {
        long ret = 0;
        for (int i = 0; i < testNames.length; i++) {
            File test = new File(Directory, testNames[i]);
            if (test.exists() && !test.isDirectory()) {
                ret = Math.max(ret, test.lastModified());
            }
        }
        return (ret);
    }

    /**
     * { method
     *
     * @param BuildBase directory of build path
     * @return the array of classes present
     * }
     * @name getAllFullClasses
     * @function find classes in BuildBase path and not in CaptureBase path
     */
    public static String[] getAllFullClasses
    (String
             BuildBase) {
        int BuildLength = BuildBase.length() + 1;
        String[] BuildClasses = FileUtilities.getAllFilesWithExtension(BuildBase, "class");
        java.util.List holder = new ArrayList();
        for (int i = 0; i < BuildClasses.length; i++) {
            if (BuildClasses[i].indexOf("$") == -1)
                holder.add(BuildClasses[i].substring(BuildLength).replace('/', '.'));
        }
        String[] ret = Util.collectionToStringArray(holder);
        return (ret);
    }

    /**
     * { method
     *
     * @param IncludedFile   non-null existing file in UpperDirectory
     * @param UpperDirectory non-null existing directory
     * @return string representing the path of the fiel relatove to the directory
     * }
     * @name relativePath
     * @function return a string representing the path of included file ret
     */
    public static String relativePath
    (File
             IncludedFile, File
             UpperDirectory) {
        if (!IncludedFile.exists())
            throw new IllegalArgumentException(
                    "requested file '" + IncludedFile.getAbsolutePath() + "' does not exist");
        if (!UpperDirectory.exists())
            throw new IllegalArgumentException(
                    "requested file '" + UpperDirectory.getAbsolutePath() + "' does not exist");
        if (!UpperDirectory.isDirectory())
            throw new IllegalArgumentException(
                    "requested directory '" + UpperDirectory.getAbsolutePath() + "' is not a directory");
        String ret = IncludedFile.getName();
        File Test = new File(IncludedFile.getParent());
        while (Test != null) {
            if (Test.equals(UpperDirectory))
                break;
            ret = Test.getName() + "/" + ret;
            Test = new File(Test.getParent());
        }
        if (Test == null)
            throw new IllegalArgumentException("directory '" + UpperDirectory.getAbsolutePath() +
                    "' is not a m_ParentStream of file '" + IncludedFile.getAbsolutePath() + "'");
        return (ret);
    }

    /**
     * { method
     *
     * @param BuildBase directory of build path
     * @param BuildBase directory of capture path
     * @return the array of classes present in BuildBase path and not CaptureBase
     * }
     * @name differenceJavaPaths
     * @function find classes in BuildBase path and not in CaptureBase path
     */
    public static String[] differenceJavaPaths
    (String
             BuildBase, String
             CaptureBase) {
        String[] BuildClasses = getAllFullClasses(BuildBase);
        String[] CapturedClasses = getAllFullClasses(CaptureBase);
        String[] UnCaptured = Util.differenceStringArrays(CapturedClasses, BuildClasses);
        return (UnCaptured);
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     test extension
     * @return the array of files or null if none
     * }
     * @name getFilesWithExtension
     * @function recursively get all files ending in ext
     * @UnusedParam> FileName start directory
     */
    public static String[] getFilesWithExtension
    (String
             DirectoryName, String
             Extension) {
        if (Extension != null) {
            FilenameFilter TheFilter = (FilenameFilter) (new EndsWithFilter(Extension));
            return (getFilesWithFilter(DirectoryName, TheFilter));
        }
        else {
            return (getFilesWithFilter(DirectoryName, null));
        }
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Extension     test extension
     * @return the array of files or null if none
     * }
     * @name getFilesWithExtension
     * @function recursively get all files ending in ext
     * @UnusedParam> FileName start directory
     */
    public static String[] getFilesWithExtension
    (File
             dir, String
             Extension) {
        if (Extension != null) {
            FilenameFilter TheFilter = (FilenameFilter) (new EndsWithFilter(Extension));
            return (getFilesWithFilter(dir, dir.getName(), TheFilter));
        }
        else {
            return (getFilesWithFilter(dir, dir.getName(), null));
        }
    }

    /**
     * { method
     *
     * @param DirectoryName    <Add Comment Here>
     * @param SubDirectoryName test name
     * @return the array of files or null if none
     * }
     * @name getDirectoriesWithName the idea is to put all resources in a subdirectory called res and then
     * package all the contents in these subdirectories
     * @function recursively get all files ending in ext
     */
    public static String[] getDirectoriesWithName
    (String
             DirectoryName, String
             SubDirectoryName) {
        if (SubDirectoryName != null) {
            FilenameFilter TheFilter = (FilenameFilter) (new DirectoryNamedFilter(
                    SubDirectoryName));
            return (getFilesWithFilter(DirectoryName, TheFilter));
        }
        else {
            return (getFilesWithFilter(DirectoryName, null));
        }
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Filter        test to apply
     * @return the array of files or null if none
     * }
     * @name getAllFilesWithFilter
     * @function recursively get all files passing the filter
     * @UnusedParam> FileName start directory
     */
    public static String[] getAllFilesWithFilter
    (String
             DirectoryName, FilenameFilter
             Filter) {
        File TestFile = new File(DirectoryName);
        return (getAllFilesWithFilter(TestFile, Filter));
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Filter        test to apply
     * @return the array of files or null if none
     * }
     * @name getAllFilesWithFilter
     * @function recursively get all files passing the filter
     * @UnusedParam> FileName start directory
     */
    public static String[] getAllFilesWithFilter
    (File
             TestFile, FilenameFilter
             Filter) {
        String test;
        File Subfile;
        String SubFiles[];
        String Files[];
        Vector ret = new Vector();
        if (TestFile == null) {
            return (null);
        }
        if (!TestFile.isDirectory()) {
            String rfile[] = new String[1];
            rfile[0] = TestFile.getPath().replace('\\', '/');
            return (rfile);
        }
        if (Filter == null) {
            Files = TestFile.list();
        }
        else {
            Files = TestFile.list(Filter);
        }
        for (int j = 0; j < Files.length; j++) {
            Subfile = new File(TestFile, Files[j]);
            if (Subfile.isDirectory()) {
                SubFiles = getAllFilesWithFilter(Subfile, Filter);
                for (int k = 0; k < SubFiles.length; k++) {
                    ret.addElement(SubFiles[k]);
                }
            }
            else {
                String path = null;
                /*    try {
                    path = Subfile.getCanonicalPath();
                    String base =  new File(".").getCanonicalPath() ;
                    path = path.substring(base.length());
                }
                catch(IOException ex) {
                */
                path = Subfile.getPath();
                //  }

                ret.addElement(path.replace('\\', '/'));
            }
        }
        String out[] = new String[ret.size()];
        for (int i = 0; i < ret.size(); i++) {
            test = (String) ret.elementAt(i);
            out[i] = test;
        }
        return (out);
    }

    /**
     * get subdirectoryies
     *
     * @param dir existing, readable directory
     * @return non-null list of subdirectories
     */
    public static File[] getSubDirectories
    (String
             dir) {
        return getSubDirectories(new File(dir));
    }

    /**
     * get subdirectoryies
     *
     * @param dir existing, readable directory
     * @return non-null list of subdirectories
     */
    public static File[] getSubDirectories
    (File
             dir) {
        if (!dir.isDirectory())
            throw new IllegalArgumentException("File " + dir.getName() + " is not a directory");
        List holder = new ArrayList();
        String[] names = dir.list();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            File test = new File(dir, name);
            if (test.isDirectory())
                holder.add(test);
        }
        File[] ret = new File[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @param Filter        filter to apply
     * @return the array of files or null if none
     * }
     * @name getFilesWithFilter
     * @function get all files at the current level passing the filter
     * @UnusedParam> FileName start directory
     */
    public static String[] getFilesWithFilter
    (String
             DirectoryName, FilenameFilter
             Filter) {
        File TestFile = new File(DirectoryName);
        return getFilesWithFilter(TestFile, DirectoryName, Filter);
    }

    public static String[] getFilesWithFilter
            (File
                     pTestFile, String
                     DirectoryName,
             FilenameFilter
                     Filter) {
        String test;
        String Files[];
        Vector ret = new Vector();
        if (pTestFile == null) {
            return (null);
        }
        if (!pTestFile.isDirectory()) {
            String rfile[] = new String[1];
            rfile[0] = DirectoryName;
            return (rfile);
        }
        if (Filter == null) {
            Files = pTestFile.list();
        }
        else {
            Files = pTestFile.list(Filter);
        }
        for (int j = 0; j < Files.length; j++) {
            test = pathConcat(DirectoryName, Files[j]);
            ret.addElement(test);
        }
        String out[] = new String[ret.size()];
        for (int i = 0; i < ret.size(); i++) {
            test = (String) ret.elementAt(i);
            out[i] = test;
        }
        return (out);
    }

    /**
     * { method
     *
     * @param DirectoryName <Add Comment Here>
     * @return the array of files or null if none
     * }
     * @name getAllSubDirectories
     * @function return the names of all subdirectories at all levels
     * @UnusedParam> FileName starting directory
     */
    public static String[] getAllSubDirectories
    (String
             DirectoryName) {
        File TestFile = new File(DirectoryName);
        return getAllSubDirectories(TestFile);
    }

    public static String[] getAllSubDirectories
            (File
                     pTestFile) {
        String test;
        String DirectoryName = pTestFile.getPath();
        File Subfile;
        String SubFiles[];
        String Files[];
        Vector ret = new Vector();
        if (pTestFile == null || !pTestFile.isDirectory()) {
            return (null);
        }
        Files = pTestFile.list();
        for (int j = 0; j < Files.length; j++) {
            test = pathConcat(DirectoryName, Files[j]);
            Subfile = new File(test);
            if (Subfile.isDirectory()) {
                SubFiles = getAllSubDirectories(test);
                if (SubFiles != null) {
                    for (int k = 0; k < SubFiles.length; k++) {
                        ret.addElement(SubFiles[k]);
                    }
                }
                ret.addElement(test);
            }
        }
        String out[] = new String[ret.size()];
        for (int i = 0; i < ret.size(); i++) {
            test = (String) ret.elementAt(i);
            out[i] = test;
        }
        return (out);
    }

    /**
     * depth first seearch for a sub directory
     *
     * @param pTestFile
     * @param name
     * @return
     */
    public static File findSubDirectory
    (File
             pTestFile, String
             name) {
        File SubFiles[];
        String Files[];
        List ret = new ArrayList();
        if (pTestFile == null || !pTestFile.isDirectory()) {
            return (null);
        }
        Files = pTestFile.list();
        if (Files == null)
            return null;
        List<File> holder = new ArrayList<File>();
        for (int i = 0; i < Files.length; i++) {
            String file = Files[i];
            File testDir = new File(pTestFile, file);
            if (!testDir.isDirectory())
                continue;
            if (name.equals(file))
                return testDir;
            holder.add(testDir);
        }
        SubFiles = new File[holder.size()];
        holder.toArray(SubFiles);
        for (int j = 0; j < SubFiles.length; j++) {
            File ans = findSubDirectory(SubFiles[j], name);
            if (ans != null) {
                return ans;
            }
        }
        return null;

    }


    /**
     * depth first seearch for a sub directory
     *
     * @param pTestFile
     * @param name
     * @return
     */
    public static File findSubDirectoryBreadthFirst
    (File
             pTestFile, String
             name) {
        File[] thislevel = {pTestFile};
        List<File> nextLevel = new ArrayList<File>();
        File ret = findSubDirectoryBreadthFirst(thislevel, nextLevel, name);
        if (ret != null)
            return ret;
        File[] files = new File[nextLevel.size()];
        nextLevel.toArray(files);
        return ret;
    }


    /**
     * depth first seearch for a sub directory
     *
     * @param pTestFile
     * @param name
     * @return
     */
    public static File findSubDirectoryBreadthFirst
    (File[] thislevel, List<File> nextLevel,
     String
             name) {
        for (int i = 0; i < thislevel.length; i++) {
            File test = thislevel[i];
            String tn = test.getName();
            if (tn.equals(name))
                return test;

        }
        for (int i = 0; i < thislevel.length; i++) {
            File test = thislevel[i];
            String[] nls = test.list();
            for (int j = 0; j < nls.length; j++) {
                String nl = nls[j];
                File testDir = new File(test, nl);
                if (!testDir.isDirectory())
                    continue;
                if (name.equals(testDir.getName()))
                    return testDir;
                nextLevel.add(testDir);
            }
        }
        int nextSize = nextLevel.size();
        if (nextSize == 0)
            return null;
        thislevel = new File[nextSize];
        nextLevel.toArray(thislevel);
        nextLevel.clear();
        return findSubDirectoryBreadthFirst(thislevel, nextLevel, name);

    }


    /**
     * { method
     *
     * @param FileName file name - may have wild cards
     * @return the first matching file or null for no match
     * @name findWildCardFile
     * @function Search a directory for files matching a name with wild cards - i.e. *.java
     * @see #findWildCardFile
     * }
     */
    static public String findWildCardFile
    (String
             FileName) {
        String DirectoryName = fileNameToDirectoryName(FileName);
        String FileStub = FileName.substring(DirectoryName.length() + 1, FileName.length());
        return (findWildCardFile(DirectoryName, FileStub));
    }

    /**
     * { method
     *
     * @param DirectoryName directory to search
     * @param FileStub      file name may have wild cards
     * @return the first matching file or null for no match
     * @name findWildCardFile
     * @function Search a directory for files matching a name with wild cards - i.e. *.java
     * @see #findWildCardFile
     * }
     */
    static public String findWildCardFile
    (String
             DirectoryName, String
             FileStub) {
        File Directory = new File(DirectoryName);
        if (!Directory.exists() || !Directory.isDirectory()) {
            return (null);
        }
        String[] AllFiles = Directory.list();
        for (int i = 0; i < AllFiles.length; i++) {
            String test = AllFiles[i].substring(0, FileStub.length());
            if (FileStub.equalsIgnoreCase(test)) {
                return (AllFiles[i]);
            }
        }
        return (null);
    }

    /**
     * { method
     *
     * @param FileName file name to parse
     * @return directory name
     * }
     * @name fileNameToDirectoryName
     * @function get the directory of a file name i.e. C:/foo/bar/myfile.txt -> C:/foo/bar
     */
    static public String fileNameToDirectoryName
    (String
             FileName) {
        int TheIndex = FileName.lastIndexOf(File.pathSeparatorChar);
        if (TheIndex == -1) {
            return (FileName);
        }
        return (FileName.substring(0, TheIndex - 1));
    }

    /**
     * { method
     *
     * @param FileName the file name
     * @return - array or strings - null if file empty or does not exist
     * }
     * @name readInParagraphs
     * @function reads all the data in a file into an array of strings - one per
     * paragraph where paragraphs are a blank line
     */
    public static String[] readInParagraphs
    (String
             FileName) {
        String Text = readInFile(FileName);
        if (Text == null)
            return (null);
        String[] items = Util.parseLinesWithBlanks(Text);
        ArrayList holder = new ArrayList();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            String Text1 = items[i];
            if (Text1.length() == 0) {
                if (sb.length() > 0) {
                    holder.add(sb.toString());
                    sb.setLength(0);
                }
            }
            else {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(items[i]);
            }
        }
        if (sb.length() > 0)
            holder.add(sb.toString());

        return ((String[]) holder.toArray(Util.EMPTY_STRING_ARRAY));
    }

    /**
     * { method
     *
     * @param FileName the file name
     * @return - array or strings - null if file empty or does not exist
     * }
     * @name readInLines
     * @function reads all the data in a file into an array of strings - one per line
     * Empty strings are dropped
     */
    public static String[] readInLines
    (File
             FileName) {
        String s = readInFile(FileName);
        if (s != null) {
            String[] Lines = Util.parseLines(s);
            Lines = nonEmptyStrings(Lines);
            return (Lines);
        }
        return (null);
    }


    /**
     * { method
     *
     * @param FileName the file name
     * @return - array or strings - null if file empty or does not exist
     * }
     * @name readInLines
     * @function reads all the data in a file into an array of strings - one per line
     * Empty strings are dropped
     */
    public static String[] readInLines(URL u) {
        InputStream is = null;
        try {
            is = u.openStream();
            String s = readInFile(is);
            if (s != null) {
                String[] Lines = Util.parseLines(s);
                Lines = nonEmptyStrings(Lines);
                return (Lines);
            }
        }
        catch (IOException e) {

        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
        return (null);
    }

    /**
     * { method
     *
     * @param FileName the file name
     * @return - array or strings - null if file empty or does not exist
     * }
     * @name readInLines
     * @function reads all the data in a file into an array of strings - one per line
     * Empty strings are retained
     */
    public static String[] readInAllLines
    (File
             FileName) {
        String s = readInFile(FileName);
        if (s != null) {
            String[] Lines = Util.parseLines(s);
            return (Lines);
        }
        return (null);
    }

    /**
     * { method
     *
     * @param FileName the file name
     * @return - array or strings - null if file empty or does not exist
     * }
     * @name readInLines
     * @function reads all the data in a file into an array of strings - one per line
     */
    public static String[] readInLines
    (String
             FileName) {
        String s = readInFile(FileName);
        if (s != null) {
            String[] Lines = Util.parseLines(s);
            Lines = nonEmptyStrings(Lines);
            return (Lines);
        }
        return (null);
    }

    /**
     * { method
     *
     * @param FileName the file name
     * @return - array or strings - null if file empty or does not exist
     * }
     * @name readInLines
     * @function reads all the data in a file into an array of strings - one per line
     */
    public static String[] readInLines(Reader TheFile) {
        LineNumberReader r = new LineNumberReader(TheFile);
        java.util.List holder = new ArrayList();
        try {
            String s = r.readLine();
            while (s != null) {
                holder.add(s);
                s = r.readLine();
            }
            String[] ret = Util.collectionToStringArray(holder);
            TheFile.close();
            return (ret);
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
        finally {
            FileUtilities.guaranteeClosed(r);

        }
    }

    public static Reader readerFromLines
            (String[] lines) {
        String allLines = buildString(lines);
        return (new StringReader(allLines));
    }


    public static String buildString
            (String[] lines) {
        StringBuilder sb = new StringBuilder(2048);
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i]);
            sb.append("\n");
        }
        return (sb.toString());
    }

    /**
     * { method
     *
     * @param source   - class for resource
     * @param FileName the file name
     * @return - array or strings - null if file empty or does not exist
     * }
     * @name readInResourceLines
     * @function reads all the data in a file into an array of strings - one per line
     */
    public static String[] readInResourceLines
    (Class
             source, String
             FileName) {
        String s = readInResource(source, FileName);
        if (s != null) {
            String[] Lines = Util.parseLines(s);
            Lines = nonEmptyStrings(Lines);
            return (Lines);
        }
        return (null);
    }

    /**
     * { method
     *
     * @param FileName the file name
     * @return StringBuilder holding file bytes
     * }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    public static String readInFile
    (String
             FileName) {
        File TestFile = new File(FileName);
        return (readInFile(TestFile));
    }


    /**
     * { method
     *
     * @param TestFile non-null Test file - may not exist
     * @return String holding file bytes
     * }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    public static String readInFile(File TestFile) {
        if (!TestFile.exists())
            return (null); // no can find
        if (!TestFile.canRead())
            return (null); // no can read
        int len = (int) TestFile.length();

        try {
            // Microsoft approach only in j++
            // Assert the right to perform file I/O
            // PolicyEngine.assertPermission(PermissionID.FILEIO);
            FileInputStream TheFile = new FileInputStream(TestFile);
            // choose large chunks
            return (readInFile(TheFile, len, 4096).toString());
        }
        catch (SecurityException e) {
            System.out.println(
                    "Security Exception reading File - " + TestFile.getPath().replace('\\', '/'));
            return (null);
        }
        catch (Exception e) {
            return (null);
        }
    }

    /**
     * { method
     *
     * @param Source - class for resource
     * @param name   the file name
     * @return StringBuilder holding file bytes - null if resource not found
     * }
     * @name readInResource
     * @function reads all the data in a resource into a StringBuilder
     */
    public static String readInResource(Class Source, String name) {
        InputStream in = Source.getResourceAsStream(name);
        if (in != null)
            return (readInFile(in));
        return (null);
    }

    /**
     * { method
     *
     * @param TheFile the file stream
     * @return StringBuilder holding file bytes
     * }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    public static String readInFile(Reader TheFile) {
        return (readInFile(TheFile, 4096, 1).toString());
    }

    /**
     * { method
     *
     * @param TheFile the file stream
     * @param len     the file length or a good guess
     * @return StringBuilder holding file bytes
     * }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    public static StringBuilder readInFile
    (Reader
             TheFile, int len,
     int chunk) {
        BufferedReader TheStream = null;
        StringBuilder s = new StringBuilder(len);
        char[] buffer = new char[chunk];
        int NRead = 0;
        try {
            TheStream = new BufferedReader(TheFile);
            NRead = TheStream.read(buffer, 0, chunk);
            while (NRead != -1) {
                s.append(buffer, 0, NRead);
                NRead = TheStream.read(buffer, 0, chunk);
                // ought to look at non-printing chars
            }
            TheStream.close();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return (s);
    }

    /**
     * { method
     *
     * @param TheFile the file stream
     * @return StringBuilder holding file bytes
     * }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    public static String readInFile
    (URL
             TheFile) {
        InputStream is = null;
        try {
            is = TheFile.openStream();
            return readInFile(is);
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * { method
     *
     * @param TheFile the file stream
     * @return StringBuilder holding file bytes
     * }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    public static String readInFile
    (InputStream
             TheFile) {
        return (readInFile(TheFile, 4096, 1).toString());
    }

    /**
     * { method
     *
     * @param TheFile the file stream
     * @param len     the file length or a good guess
     * @return StringBuilder holding file bytes
     * }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    public static StringBuilder readInFile(InputStream TheFile, int len, int chunk) {
        BufferedReader TheStream = null;
        StringBuilder s = new StringBuilder(len);
        char[] buffer = new char[chunk];
        int NRead = 0;
        try {
            InputStreamReader streamReader = new InputStreamReader(TheFile);
            TheStream = new BufferedReader(streamReader);
            NRead = TheStream.read(buffer, 0, chunk);
            while (NRead != -1) {
                s.append(buffer, 0, NRead);
                NRead = TheStream.read(buffer, 0, chunk);
                // ought to look at non-printing chars
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            if (TheStream != null) {
                try {
                    TheStream.close();
                }
                catch (IOException e) {
                    // forgive I guess
                }
            }
        }
        return (s);
    }

    /**
     * { method
     *
     * @param target -  Object holding resource
     * @param name   the file name
     * @return Icon for the resource possibly null
     * }
     * @name getResourceIcon
     * @function read an Icon as a Resource
     */
    public static Icon getResourceIcon(Object target, String name) {
        return (getResourceIcon(target.getClass(), name));
    }

    /**
     * { method
     *
     * @param target -  Class holding resource
     * @param name   the file name
     * @return Icon for the resource possibly null
     * }
     * @name getResourceIcon
     * @function read an Icon as a Resource
     */
    public static Icon getResourceIcon
    (Class
             target, String
             name) {
        try {
            // Netscape appears not to support getResource so this
            // reads an image from a resource
            //

            InputStream is = target.getResourceAsStream(name);
            if (is == null) {
                System.err.println("Resource not found. " + name +
                        " as Reference in class " + target.getName());
                return (null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int c;
            while ((c = is.read()) >= 0)
                baos.write(c);
            is.close();
            Image TheActualImage = Toolkit.getDefaultToolkit().createImage(baos.toByteArray());
            Icon TheImage = new ImageIcon(TheActualImage);
            return (TheImage);
        }
        catch (IOException e) {
            return (null);
        }
        catch (Exception ex) {
            //  return(SecurityUtilities.secureGetResourceIcon(target,name) );
            return (null);
        }
    }

    /**
     * { method
     *
     * @param FileName the file name
     * @param lines    the file data - useing toString method
     * @return - true for success
     * }
     * @name stringsToFile
     * @function reads all the data in a file into an array of strings - one per line
     */
    public static boolean stringsToFile
    (String
             FileName, Object[] lines) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(FileName));
            for (int i = 0; i < lines.length; i++)
                out.println(lines[i]);
            out.close();
            return (true);
        }
        catch (IOException ex) {
            return (false);
        }
    }

    /**
     * { method
     *
     * @param TheStream the stream
     * @return the char
     * }
     * @name readChar
     * @function get one char from an input stream without throwing an exception
     */
    public static int readChar(BufferedInputStream TheStream) {
        int ret = 0;
        try {
            ret = TheStream.read();
        }
        catch (IOException e) {
            ret = -1;
        }
        return (ret);
    }

    /**
     * { method
     *
     * @param Name input name
     * @return Filtered file name
     * }
     * @name UnixNameFilter
     * @function convert pc style filenames foo\bar to unix style foo/bar
     */
    public static String unixNameFilter
    (String
             Name) {
        if (Name == null) {
            return (null);
        }
        return (Name.replace('\\', '/'));
    }

    /**
     * { method
     *
     * @param Name the full path
     * @return the base name
     * }
     * @name baseFileName
     * @function peal off name+extension of a full path i.e. C:/foo/bar/myFile.txt => myFile.txt
     */
    public static String baseFileName
    (String
             Name) {
        int index = Name.lastIndexOf(File.separatorChar);
        if (index >= 0) {
            return (Name.substring(index + 1));
        }
        if (File.separatorChar == '\\') {
            index = Name.lastIndexOf('/');
            if (index >= 0) {
                return (Name.substring(index + 1));
            }
        }
        index = Name.lastIndexOf(File.pathSeparatorChar);
        if (index >= 0) {
            return (Name.substring(index + 1));
        }
        return (Name);
    }

    /**
     * { method
     *
     * @param Name full path
     * @param Base part to drop
     * @return filtered name
     * }
     * @name baseFromName
     * @function given the base directory (part of full path)
     * return the rest of the path - for example if Name = "C:/foo/bar/pickle/Myfile.txt"
     * and Base = "C:/foo" return "bar/pickle/Myfile.txt"
     */
    public static String baseFromName
    (String
             Name, String
             Base) {
        if (Name.indexOf(Base) == -1) {
            return (Name);
        }
        int l = Base.length();
        if (Name.length() == l) {
            return ("");
        }
        char c = Name.charAt(l);
        // drop any separator
        if (c == File.separatorChar || c == '/') {
            l++;
        }
        return (Name.substring(l));
    }

    /**
     * { method
     *
     * @param Name full path
     * @return filtered name
     * }
     * @name pathName
     * @function pull out all directories dropping filename + ext
     * for example if Name = "C:/foo/bar/pickle/Myfile.txt" return
     * "C:/foo/bar/pickle"
     */
    public static String pathName
    (String
             Name) {
        int index = Name.lastIndexOf(File.separatorChar);
        if (index >= 0) {
            return (Name.substring(0, index));
        }
        if (File.separatorChar == '\\') {
            index = Name.lastIndexOf('/');
            if (index >= 0) {
                return (Name.substring(0, index));
            }
        }
        index = Name.lastIndexOf(File.pathSeparatorChar);
        if (index >= 0) {
            return (Name.substring(0, index));
        }
        return ("");
        // no path
    }

    /**
     * { method
     *
     * @param s1 path
     * @param s2 file
     * @return filtered name
     * }
     * @name pathConcat
     * @function add path and file name or path and subpath adding separator
     * if needed For example if s1 = "C:/foo/bar" and s2 = "Myfile.txt"
     * return "C:/foo/bar/Myfile.txt"
     */
    static public String pathConcat
    (String
             s1, String
             s2) {
        if (s1 == null || s1.length() == 0) {
            return (s2);
        }
        if (s2 == null || s2.length() == 0) {
            return (s1);
        }
        char lastchar = s1.charAt(s1.length() - 1);
        if (lastchar == File.separatorChar || lastchar == '/') {
            return (s1 + s2);
        }
        else {
            return (s1 + "/" + s2);
        }
    }

    /**
     * { method
     *
     * @param FileName path with directory
     * @return true for success
     * }
     * @name guaranteeDirectory
     * @function given a path which should represent a directory
     * create as many levels of directory as needed to make the
     * directory exist
     */
    public static boolean guaranteeDirectory(String FileName) {
        String DirName = pathName(FileName);
        if (DirName.length() == 0)
            return (true);
        File testDir = new File(DirName);
        if (testDir.exists()) {
            if (!testDir.isDirectory()) {
                return (false);
            }
            // name is file
            else {
                return (true);
            }
            // exists - is directory
        }
        if (guaranteeDirectory(DirName)) {
            return (testDir.mkdir());
        }
        return (false);
        // no can do
    }

    /**
     * replace extension globally
     *
     * @param dir          directory
     * @param oldExtension oldextension
     * @param newExtension new extension
     */
    public static void changeExtension
    (String
             dir, String
             oldExtension, String
             newExtension) {
        changeExtension(new File(dir), oldExtension, newExtension);
    }

    public static void changeExtension
            (File
                     dir, String
                     oldExtension, String
                     newExtension) {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(oldExtension)) {
                String child = file.getName().replace(oldExtension, newExtension);
                file.renameTo(new File(dir, child));
            }
        }
    }

    /**
     * { method
     *
     * @param name name of file
     * @return the stream - null for failure
     * }
     * @name openPrintWriter
     * @function open a PrintWriter to a file with name
     */
    public static PrintWriter openPrintWriter
    (String
             name) {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(name);
            return new PrintWriter(new BufferedOutputStream(file));
        }
        catch (SecurityException ee) {
            return (null);
        }
        catch (IOException ee) {
            return (null);
        }
    }

    /**
     * { method
     *
     * @param name name of file
     * @return the stream - null for failure
     * }
     * @name openPrintWriter
     * @function open a PrintWriter to a file with name
     */
    public static PrintWriter openPrintWriter
    (File
             name) {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(name);
            return new PrintWriter(new BufferedOutputStream(file));
        }
        catch (SecurityException ee) {
            return (null);
        }
        catch (IOException ee) {
            return (null);
        }
    }

    /**
     * { method
     *
     * @param name name of file
     * @return the stream - null for failure
     * }
     * @name openPrintWriter
     * @function open a PrintWriter to a file with name
     */
    public static PrintWriter openAppendPrintWriter(File name) {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(name, true);
            return new PrintWriter(new BufferedOutputStream(file));
        }
        catch (SecurityException ee) {
            return (null);
        }
        catch (IOException ee) {
            return (null);
        }
    }


    /**
     * { method
     *
     * @param name name of file
     * @return the stream - null for failure
     * }
     * @name openPrintWriter
     * @function open a PrintStream to a file with name
     */
    public static PrintStream openPrintStream(String name) {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(name);
        }
        catch (Exception ee) {
            return (null);
        }
        return new PrintStream(new BufferedOutputStream(file));
    }

    /**
     * { method
     *
     * @param FileName name of file to create
     * @param data     date to write
     * @return true = success
     * }
     * @name writeFile
     * @function write the string data to the file Filename
     */
    public static boolean writeFile(String FileName, String data) {
        try {
            PrintWriter out = openPrintWriter(FileName);
            if (out != null) {
                out.print(data);
                out.close();
                return (true);
            }
            return (false);
            // failure
        }
        catch (SecurityException ex) {
            return (false); // browser disallows
        }
    }


    /**
     * { method
     *
     * @param FileName name of file to create
     * @param data     date to write
     * @return true = success
     * }
     * @name writeFile
     * @function write the string data to the file Filename
     */
    public static boolean writeFile(OutputStream stream, String data) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(stream));
            if (out != null) {
                out.print(data);
                out.close();
                out = null;
                return (true);
            }
            return (false);
            // failure
        }
        catch (SecurityException ex) {
            return (false); // browser disallows
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * { method
     *
     * @param FileName name of file to create
     * @param data     date to write
     * @return true = success
     * }
     * @name writeFile
     * @function write the string data to the file Filename
     */
    public static boolean writeFile(File TheFile, String data) {
        PrintWriter out = null;
        try {
            out = openPrintWriter(TheFile);
            if (out != null) {
                out.print(data);
                out.close();
                return (true);
            }
            return (false);
            // failure
        }
        catch (SecurityException ex) {
            return (false); // browser disallows
        }
        finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * { method
     *
     * @param FileName non-null name of file to create
     * @param data     non-null array of lines to write
     * @return true = success
     * }
     * @name writeFileLines
     * @function write the string data to the file Filename
     */
    public static boolean writeFileLines(String FileName, String[] data) {
        try {
            PrintWriter out = openPrintWriter(FileName);
            if (out != null) {
                for (int i = 0; i < data.length; i++)
                    out.println(data[i]);
                out.close();
                return (true);
            }
            return (false);
            // failure
        }
        catch (SecurityException ex) {
            return (false); // browser disallows
        }
    }

    /**
     * { method
     *
     * @param f    non-null  of file to create
     * @param data non-null array of lines to write
     * @return true = success
     * }
     * @name writeFileLines
     * @function write the string data to the file Filename
     */
    public static boolean writeFileLines(File f, String[] data) {
        try {
            PrintWriter out = openPrintWriter(f);
            if (out != null) {
                for (int i = 0; i < data.length; i++)
                    out.println(data[i]);
                out.close();
                return (true);
            }
            return (false);
            // failure
        }
        catch (SecurityException ex) {
            return (false); // browser disallows
        }
    }

    /**
     * { method
     *
     * @param f    non-null  of file to create
     * @param data non-null array of lines to write
     * @return true = success
     * }
     * @name appendFileLines
     * @function append the string data to the file Filename
     */
    public static boolean appendFileLines(String fileName, String... data) {
        return appendFileLines(new File(fileName), data);
    }

    /**
     * { method
     *
     * @param f    non-null  of file to create
     * @param data non-null array of lines to write
     * @return true = success
     * }
     * @name writeFileLines
     * @function write the string data to the file Filename
     */
    public static boolean appendFileLines(File f, String... data) {
        try {
            PrintWriter out = openAppendPrintWriter(f);
            if (out != null) {
                for (int i = 0; i < data.length; i++)
                    out.println(data[i]);
                out.close();
                return (true);
            }
            return (false);
            // failure
        }
        catch (SecurityException ex) {
            return (false); // browser disallows
        }
    }

    /**
     * { method
     *
     * @param out    output stream
     * @param indent blanks to add
     *               }
     * @name printIndent
     * @function add indent blanks to out - this is more efficient than adding one char
     * at a time (I hope)
     */
    public static void printIndent
    (PrintWriter
             out) {
        printIndent(out, 4);
    }

    /**
     * { method
     *
     * @param out    output stream
     * @param indent blanks to add
     *               }
     * @name printIndent
     * @function add indent blanks to out - this is more efficient than adding one char
     * at a time (I hope)
     */
    public static void printIndent
    (PrintWriter
             out, int indent) {
        if (indent <= 0) {
            return;
        }
        out.print(Util.stringOfBlanks(indent));
    }

    /**
     * { method
     *
     * @param out    output stream
     * @param indent blanks to add
     *               }
     * @name printIndent
     * @function add indent blanks to out - this is more efficient than adding one char
     * at a time (I hope)
     */
    public static void printIndent
    (PrintStream
             out, int indent) {
        if (indent <= 0) {
            return;
        }
        out.print(Util.stringOfBlanks(indent));
    }

    /**
     * { method
     *
     * @param dst destination file name
     * @param src source file name
     * @return true for success
     * }
     * @name copyFile
     * @function copy file named src into new file named dst
     */
    public static boolean copyFile
    (String
             src, String
             dst) {
        int bufsize = 1024;
        try {
            RandomAccessFile srcFile = new RandomAccessFile(src, "r");
            long len = srcFile.length();
            if (len > 0x7fffffff) {
                return (false);
            }
            // too large
            int l = (int) len;
            if (l == 0) {
                return (false);
            }
            // failure - no data
            RandomAccessFile dstFile = new RandomAccessFile(dst, "rw");

            int bytesRead = 0;
            byte[] buffer = new byte[bufsize];
            while ((bytesRead = srcFile.read(buffer, 0, bufsize)) != -1) {
                dstFile.write(buffer, 0, bytesRead);
            }
            srcFile.close();
            dstFile.close();
            return true;
        }
        catch (IOException ex) {
            return (false);
        }
    }

    /**
     * count the number of lines in an input stream
     *
     * @param f existing readable file
     * @return number of lines
     */
    public static int getNumberLines(String fileName) {
        File f = new File(fileName);
        return getNumberLines(f);
    }


    /**
     * count the number of lines in an input stream
     *
     * @param f existing readable file
     * @return number of lines
     */
    public static int getNumberLines(File f) {
        try {
            InputStream str = new FileInputStream(f);
            return getNumberLines(str);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * count the number of lines in an input stream
     *
     * @param s
     * @return
     */
    public static int getNumberLines(InputStream s) {
        LineNumberReader rdr = null;
        try {
            rdr = new LineNumberReader(new InputStreamReader(s));
            int ret = 0;
            String line = rdr.readLine();
            while (line != null) {
                ret++;
                line = rdr.readLine();
            }
            return ret;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        finally {
            if (rdr != null) {
                try {
                    rdr.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);

                }
            }
        }
    }

    /**
     * { method
     *
     * @param dst destination file name
     * @param src source file name
     * @return true for success
     * }
     * @name copyFile
     * @function copy file named src into new file named dst
     */
    public static boolean copyFileToPath(File src, String path, File dst) {
        String srcPath = src.getPath();
        String relPath = srcPath.substring(path.length() + 1);
        File dstFile = new File(dst, relPath);
        FileUtilities.guaranteeDirectory(dstFile.getPath());
        return (copyFile(src, dstFile));
    }


    /**
     * { method
     *
     * @param dst destination file name
     * @param src source file name
     * @return true for success
     * }
     * @name copyFile
     * @function copy file named src into new file named dst
     */
    public static boolean copyFile(File src, File dst) {
        int bufsize = 1024;
        try {
            RandomAccessFile srcFile = new RandomAccessFile(src, "r");
            long len = srcFile.length();
            if (len > 0x7fffffff) {
                return (false);
            }
            // too large
            int l = (int) len;
            if (l == 0) {
                return (false);
            }
            // failure - no data
            if (dst.getParentFile() != null)
                dst.getParentFile().mkdirs();
            RandomAccessFile dstFile = new RandomAccessFile(dst, "rw");

            int bytesRead = 0;
            byte[] buffer = new byte[bufsize];
            while ((bytesRead = srcFile.read(buffer, 0, bufsize)) != -1) {
                dstFile.write(buffer, 0, bytesRead);
            }
            srcFile.close();
            dstFile.close();
            long srcDate = src.lastModified();
            dst.setLastModified(srcDate);
            return true;
        }
        catch (IOException ex) {
            return (false);
        }
    }

    /**
     * Will recursively copy the contents of copyFromDir to copyToDir
     * creating any directories in the copyToDir that are necessary.
     *
     * @param copyFromDir the files and directories to recursively copy.
     * @param copyToDir   the direcory to copy them to, if it doesn't exist, it will be created.
     * @param overwrite   Set to true if an existing file should be overwritten.
     */
    public static void recursiveCopy(String copyFromDir, String
            copyToDir, boolean overwrite) {
        File copyFrom = new File(copyFromDir);
        File copyTo = new File(copyToDir);
        recursiveCopy(copyFrom, copyTo, overwrite);
    }//recursiveCopy()


    /**
     * Will recursively copy the contents of copyFromDir to copyToDir
     * creating any directories in the copyToDir that are necessary.
     *
     * @param copyFromDir the files and directories to recursively copy.
     * @param copyToDir   the direcory to copy them to, if it doesn't exist, it will be created.
     * @param overwrite   set to true if an existing copy of the file should be overwritten.
     */
    public static void recursiveCopy
    (File
             copyFromDir, File
             copyToDir, boolean overwrite) {
        try {
            //Create the copy to directory and any m_ParentStream directories
            //if needed.
            copyToDir.mkdirs();

            File[] filesToCopy = copyFromDir.listFiles();
            File newDirectory;
            String slash = System.getProperty("file.separator");
            File newFile;

            for (int i = 0; i < filesToCopy.length; i++) {
                //If the File object represents a directory,
                //recursively copy all files in that dir.
                if (filesToCopy[i].isDirectory()) {
                    newDirectory = new File(copyToDir + slash + filesToCopy[i].getName());
                    recursiveCopy(filesToCopy[i], newDirectory, overwrite);
                }
                else {
                    newFile = new File(copyToDir.toString() + slash + filesToCopy[i].getName());
                    //Just copy the file to its new home.
                    //If overwrite copy the file,
                    //or if not overright, but the file doesn't exist
                    //copy it over.
                    if (overwrite || (!newFile.exists())) {
                        copyFile(filesToCopy[i].toString(),
                                copyToDir.toString() + slash + filesToCopy[i].getName());
                    }//if
                }//else
            }//for
        }
        catch (Exception e) {
            e.printStackTrace();
        }//catch
    }//recursiveCopy()

    /**
     * writes a file and makes numberCopies backups
     *
     * @param fileName
     * @param text
     * @param numberCopies
     * @throws IllegalArgumentException when the file cannot be written or
     *                                  the directory cannot be created
     */
    public static void safeFileWrite
    (String
             fileName, String
             text, int numberCopies) {
        guaranteeWritable(fileName);
        for (int i = 0; i < numberCopies; i++) {
            guaranteeWritable(fileName + "." + (i + 1));
        }
        File temp = new File(fileName + ".temp");
        writeFile(temp, text);
        boolean renameSucceeded = cascadingRename(fileName, numberCopies);
        if (renameSucceeded) {
            File real = new File(fileName);
            temp.renameTo(real);
        }

    }

    protected static boolean cascadingRename
            (String
                     fileName, int numberCopies) {
        File[] fileItems = new File[numberCopies + 1];
        fileItems[0] = new File(fileName);
        for (int i = 0; i < numberCopies; i++) {
            int i1 = (i + 1);
            fileItems[i1] = new File(fileName + "." + i1);
        }
        boolean success = cascadingRename(fileItems);
        if (!success)
            rollbackName(fileItems);
        return success;
    }

    protected static boolean cascadingRename
            (File[] fileItems) {
        boolean success = true;
        int i = fileItems.length - 1;
        if (fileItems[i].exists())
            success = fileItems[i].delete();
        if (!success)
            return false;
        for (; i > 0; i--) {
            File fileItem = fileItems[i];
            File fileItem2 = fileItems[i - 1];
            if (fileItem2.exists()) {
                success = fileItem2.renameTo(fileItem);
            }

        }
        return success;
    }

    protected static boolean rollbackName
            (File[] fileItems) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    /**
     * guarantee we can write to the file and its directory
     *
     * @param fileName non-null file name
     * @throws IllegalArgumentException when the file cannot be written or
     *                                  the directory cannot be created
     */
    public static void guaranteeWritable
    (String
             fileName) {
        File f = new File(fileName);
        File parentFile = f.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdir())
                throw new IllegalArgumentException(
                        "Cannot create directory  " + parentFile.getAbsolutePath());
        }
        if (f.exists() && !f.canWrite()) {
            throw new IllegalArgumentException("Cannot wrote to file " + fileName);
        }
    }

    /**
     * { method
     *
     * @param oldFile     <Add Comment Here>
     * @param newFileName <Add Comment Here>
     * @param overwrite   <Add Comment Here>
     * @return <Add Comment Here>
     * }
     * @name renameFile
     * @function <Add Comment Here>
     */
    public static boolean renameFile
    (String
             oldFile, String
             newFileName, boolean overwrite) {
        try {
            File realOldFile = new File(oldFile);
            boolean ret = false;
            if (!realOldFile.exists()) {
                return (false);
            }
            // cannot rename non-existant file
            File realNewFile = new File(newFileName);
            if (realNewFile.exists()) {
                if (!overwrite) {
                    return (false);
                }
                // failure overwrite not allowed
                else {
                    try {
                        ret = realNewFile.delete();
                        realNewFile = new File(newFileName);
                    }
                    catch (Exception ex) {
                        // Assertion.doNada();
                    }
                }
            }
            ret = realOldFile.renameTo(realNewFile);
            return (ret);
        }
        catch (Exception ex) {
            return (false);
        }
    }

    /**
     * { method
     *
     * @param inFileName  <Add Comment Here>
     * @param outFileName name of the output file
     *                    }
     * @name alphabetizeFile
     * @function copy contenst of a file called inFileName to
     * a file called outFileName sorting in alphabetical order
     * @UnusedParam> iniFileName name of the input file
     */
    public static void alphabetizeFile
    (String
             inFileName, String
             outFileName) {
        String s = FileUtilities.readInFile(inFileName);
        if (s != null) {
            String[] Lines = Util.parseLines(s);
            Lines = nonEmptyStrings(Lines);
            Util.sort(Lines);
            PrintWriter out = openPrintWriter(outFileName);
            for (int i = 0; i < Lines.length; i++) {
                if (Lines[i].length() > 0) {
                    out.println(Lines[i]);
                }
            }
            out.close();
        }
    }

    public static boolean hasExtension
            (File
                     TheFile, String
                     ext) {
        String Test = ext;
        if (!ext.startsWith("."))
            Test = "." + ext;
        if (osIsCaseSensitive()) {
            return (TheFile.getName().endsWith(Test));
        }
        else {
            String TestName = TheFile.getName().toLowerCase();
            return Boolean.valueOf(TestName.endsWith(Test.toLowerCase()));
        }
    }

    private static Boolean gOSIsCaseSensitive;

    public static synchronized boolean osIsCaseSensitive
            () {
        if (gOSIsCaseSensitive == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            gOSIsCaseSensitive = Boolean.valueOf(osName.indexOf("windows") == -1);
        }
        return Boolean.valueOf(gOSIsCaseSensitive.booleanValue());
    }

    private static Boolean gOSIsWindows;

    public static synchronized boolean osIsWindows
            () {
        if (gOSIsWindows == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            gOSIsWindows = Boolean.valueOf(osName.indexOf("windows") == -1);
        }
        return (gOSIsWindows.booleanValue());
    }
//
// Drop empty strings from lines

    /**
     * { method
     *
     * @param Lines <Add Comment Here>
     * @return <Add Comment Here>
     * }
     * @name nonEmptyStrings
     * @function <Add Comment Here>
     */
    public static String[] nonEmptyStrings
    (String[] Lines) {
        Vector accumulate = new Vector();
        for (int i = 0; i < Lines.length; i++) {
            if (!Util.isEmptyString(Lines[i])) {
                accumulate.addElement(Lines[i]);
            }
        }
        String[] out = new String[accumulate.size()];
        accumulate.copyInto(out);
        return (out);
    }

    public static PrintStream makePrintStream
            (String
                     in) {
        try {
            FileOutputStream outFile = new FileOutputStream(in);
            return (new PrintStream(outFile));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void dupFile
            (String
                     infile, String
                     outfile) throws IOException {
        FileReader in = new FileReader(infile);
        FileWriter out = new FileWriter(outfile);
        copyStream(in, out);
    }

    public static void dupFile
            (String
                     infile, Writer
                     out) throws IOException {
        FileReader in = new FileReader(infile);
        copyStream(in, out);
    }

    public static void dupFile
            (Reader
                     in, String
                     outfile) throws IOException {
        FileWriter out = new FileWriter(outfile);
        copyStream(in, out);
    }

    public static void copyStream
            (Reader
                     in, Writer
                     out) throws IOException {
        char[] buffer = new char[2048];
        int nread;
        int col = 0;
        try {
            nread = in.read(buffer);
            while (nread > 0) {
                out.write(buffer, 0, nread);
                nread = in.read(buffer);
                System.out.print(".");
                if (col++ > 60) {
                    System.out.println();
                    col = 0;
                }
            }
        }
        finally {
            System.out.println();
            in.close();
            out.close();
        }
    }


    public static void copyStream
            (InputStream
                     in, OutputStream
                     out) throws IOException {
        byte[] buffer = new byte[2048];
        int nread;
        int col = 0;
        try {
            nread = in.read(buffer);
            while (nread > 0) {
                out.write(buffer, 0, nread);
                nread = in.read(buffer);
                System.out.print(".");
                if (col++ > 60) {
                    System.out.println();
                    col = 0;
                }
            }
        }
        finally {
            //    System.out.println();
            in.close();
            out.close();
        }
    }

    /**
     * { method
     *
     * @param DirectoryName - non-null Directory name will not recurse
     * @return largest lastModified Date - 0 for empty or nonexistant directories
     * }
     * @name getMostRecentFileDate
     * @function get all the files in the directory
     */
    public static long getMostRecentFileDate
    (String
             DirectoryName) {
        return (getMostRecentFileDate(DirectoryName, false)); // do not recurse
    }

    /**
     * { method
     *
     * @param DirectoryName - non-null Directory name will not recurse
     * @param recurse       - if true recurse to subdirectories - else ignore subdirectories
     * @return largest lastModified Date - 0 for empty or nonexistant directories
     * }
     * @name getMostRecentFileDate
     * @function get all the files in the directory
     */
    public static long getMostRecentFileDate
    (String
             DirectoryName, boolean recurse) {
        File TestFile = new File(DirectoryName);
        String Files[];
        long ret = 0;
        if (!TestFile.exists()) {
            return (ret);
        }
        if (!TestFile.isDirectory())
            throw new IllegalArgumentException("Name " + DirectoryName + " must be a directory");

        Files = TestFile.list();

        for (int i = 0; i < Files.length; i++) {
            File test = new File(TestFile, Files[i]);
            if (!test.isDirectory()) {
                ret = Math.max(ret, test.lastModified());
            }
            else {
                if (recurse)
                    ret = Math.max(ret, getMostRecentFileDate(Files[i], recurse));
            }
        }
        return (ret);
    }

    /**
     * { method
     *
     * @param DirectoryName - non-null Directory name will not recurse
     * @param date          - modified date
     * @return largest lastModified Date - 0 for empty or nonexistant directories
     * }
     * @name getFilesAfterDate
     * @function get all the files in the directory after date
     */
    public static File[] getFilesAfterDate
    (String
             DirectoryName, long date) {
        return (getFilesAfterDate(DirectoryName, date, false)); // do not recurse
    }

    /**
     * { method
     *
     * @param FileName - non-null name of an existing file
     * @throws AssertionFailureException if the file does not exists
     *                                   }
     * @name assertFileEsists
     * @function easy way to handle errors on a file the program needs
     */
    public static void assertFileExists
    (String
             FileName) {
        File Test = new File(FileName);
        if (!Test.exists()) {
            String msg = "File " + FileName + " is required and cannot be found";
            System.err.println(msg);
            throw new RuntimeException(msg);
        }
        if (!Test.canRead()) {
            String msg = "File " + FileName + " is required and cannot be read";
            System.err.println(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * { method
     *
     * @param DirectoryName - non-null Directory name will not recurse
     * @param date          - modified date
     * @param recurse       - if true recurse to subdirectories - else ignore subdirectories
     * @return - non-null array of qualifying files
     * }
     * @name getFilesAfterDate
     * @function get all the files in the directory after date
     */
    public static File[] getFilesAfterDate
    (String
             DirectoryName, long date,
     boolean recurse) {
        File TestFile = new File(DirectoryName);
        if (!TestFile.exists())
            return (EMPTY_FILES);
        if (!TestFile.isDirectory())
            throw new IllegalArgumentException("Name " + DirectoryName + " must be a directory");
        Vector holder = new Vector(100);
        accumulateFilesAfterDate(TestFile, date, recurse, holder); // do not recurse
        return ((File[]) Util.vectorToArray(holder, File.class));
    }

    /**
     * { method
     *
     * @param TestFile - non-null File representing an existing directory
     * @param date     - modified date
     * @param recurse  - if true recurse to subdirectories - else ignore subdirectories
     * @param holder   -  non-null vector accumulating files
     *                 }
     * @name accumulateFilesAfterDate
     * @function add all files after date to holder
     */
    protected static void accumulateFilesAfterDate
    (File
             TestFile, long date,
     boolean recurse,
     Vector holder) {
        String[] Files = TestFile.list();

        for (int i = 0; i < Files.length; i++) {
            File test = new File(TestFile, Files[i]);
            if (!test.isDirectory()) {
                if (test.lastModified() > date)
                    holder.addElement(test);
            }
            else {
                if (recurse) {
                    accumulateFilesAfterDate(test, date, recurse, holder);
                }
            }
        }
    }

    /**
     * write the text renaming all files to backup
     *
     * @param fileName name of a creatable file which may or may not exist
     * @param text     test to write
     */
    public void writeTextWithBackup
    (String
             fileName, String
             text) {
        writeTextWithBackup(new File(fileName), fileName);
    }

    /**
     * write the text renaming all files to backup
     *
     * @param fileName name of a creatable file which may or may not exist
     * @param text     test to write
     */
    public static void writeTextWithBackup
    (File
             file, String
             text) {
        File parentDirectory = file.getParentFile();
        if (!parentDirectory.exists() && !parentDirectory.mkdirs())
            throw new IllegalStateException(
                    "Cannot create m_ParentStream directory for " + file.getAbsolutePath());
        if (!file.exists()) { // no worries about backup
            writeFile(file, text);
            return;
        }
        // make a copy to rename
        File oldFile = new File(parentDirectory, file.getName());
        // data to a temp file
        File temp = new File(parentDirectory, file.getName() + ".tmp");
        if (temp.exists()) { // no worries about backup
            if (!temp.delete())
                throw new IllegalStateException(
                        "Cannot delete temporary file for " + temp.getAbsolutePath());
        }
        writeFile(temp, text);
        File backup = new File(parentDirectory, file.getName() + ".bak");
        if (backup.exists()) { // no worries about backup
            renameBackup(backup);
        }

        if (!oldFile.renameTo(backup))
            throw new IllegalStateException(
                    "Cannot rename  file " + file.getAbsolutePath() +
                            " to " + backup.getAbsolutePath());
        if (!temp.renameTo(file))
            throw new IllegalStateException(
                    "Cannot rename  temporary file " + temp.getAbsolutePath() +
                            " to " + file.getAbsolutePath());


    }

    /**
     * rename a file to name.n where n is 1..2 ..3 ...
     *
     * @param backupFile existing file
     * @throws IllegalStateException on error
     */
    protected static void renameBackup
    (File
             backupFile) {
        File parentDirectory = backupFile.getParentFile();
        int index = 1;
        while (true) {
            // data to a temp file
            File newName = new File(parentDirectory, backupFile.getName() + "." + index++);
            if (newName.exists())   // no worries about backup
                continue; // fina a non-existant file
            if (!backupFile.renameTo(newName))
                throw new IllegalStateException(
                        "Cannot rename  temporary file " + backupFile.getAbsolutePath() +
                                " to " + newName.getAbsolutePath());
            return;
        }

    }


    /**
     * This serializes an object to a file
     *
     * @param in       - non-null object to Serialize
     * @param FileName - non-null file name
     */
    public static void serializeObject
    (Serializable
             in, String
             FileName) {
        try {
            FileOutputStream fout = new FileOutputStream(FileName);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(in);
            out.close();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * This serializes an object to a file
     *
     * @param in       - non-null object to Serialize
     * @param FileName - non-null file name
     */
    public static Object deserializeObject
    (String
             FileName) {
        try {
            FileInputStream fout = new FileInputStream(FileName);
            ObjectInputStream in = new ObjectInputStream(fout);
            Object ret = in.readObject();
            in.close();
            return (ret);
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * This serializes an object to a file
     *
     * @param in       - non-null object to Serialize
     * @param FileName - non-null creatable File
     */
    public static void serializeObject
    (Serializable
             in, File
             TheFile) {
        try {
            FileOutputStream fout = new FileOutputStream(TheFile);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(in);
            out.close();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * This serializes an object to a file
     *
     * @param in       - non-null object to Serialize
     * @param FileName - non-null existing file holding a serialized object
     */
    public static Object deserializeObject
    (File
             TheFile) {
        try {
            FileInputStream fout = new FileInputStream(TheFile);
            ObjectInputStream in = new ObjectInputStream(fout);
            Object ret = in.readObject();
            in.close();
            return (ret);
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    public static FilenameFilter getExtensionFilter
            (String
                     ext) {
        return (new HasExtensionFilter(ext));
    }

    public static String downloadURLText
            (String
                     theURl) {
        try {
            URL TheURL = new URL(theURl);
            URLConnection urc = TheURL.openConnection();

            InputStream inp = urc.getInputStream();
            InputStreamReader in = new InputStreamReader(inp);
            String ret = readInFile(in);
            return (ret);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Cannot download URL");
        }

    }

    public static void downloadBinary
            (URL
                     BaseURL, String
                     Name, File
                     TargetDirectory)
            throws IOException {
        URL TheURL = new URL(BaseURL, Name);
        File TestFile = new File(TargetDirectory, Name);
        downloadBinary(TheURL, TestFile);
    }

    public static void downloadBinary
            (URL
                     TheURL, File
                     TestFile) throws IOException {
        OutputStream outf = new FileOutputStream(TestFile);
        String test = TheURL.toString();
        System.out.println(test);
        InputStream in = TheURL.openStream();
        downloadBinary(in, outf);
    }


    public static void downloadBinary
            (InputStream
                     ins, OutputStream
                     outf) throws IOException {
        byte[] holder = new byte[4096];
        InputStream in = new BufferedInputStream(ins);
        OutputStream out = new BufferedOutputStream(outf);
        int Nread = in.read(holder);
        int col = 0;
        while (Nread > -1) {
            out.write(holder, 0, Nread);
            Nread = in.read(holder);
            System.out.print(".");
            if (col++ > 60) {
                System.out.println();
                col = 0;
            }
        }
        System.out.println();
        in.close();
        out.close();
    }

    /**
     * throws an IllegalArgumentException if t1  is nof as below
     *
     * @param t1 non-null existing readable file
     */
    public static void guaranteeReadableFile
    (File
             t1) {
        if (t1 == null)
            throw new IllegalArgumentException("File in null");
        if (!t1.exists())
            throw new IllegalArgumentException(
                    "File '" + t1.getAbsolutePath() + "' does not exist as required");
        if (!t1.canRead())
            throw new IllegalArgumentException(
                    "File '" + t1.getAbsolutePath() + "' cannot be read as required");
        if (!t1.isFile())
            throw new IllegalArgumentException(
                    "File '" + t1.getAbsolutePath() + "' is not a file as required");
    }

    /**
     * throws an IllegalArgumentException if t1  is nof as below
     *
     * @param t1 non-null existing readable file
     */
    public static void guaranteeReadableDirectory
    (File
             t1) {
        if (t1 == null)
            throw new IllegalArgumentException("File in null");
        if (!t1.exists())
            throw new IllegalArgumentException(
                    "File '" + t1.getAbsolutePath() + "' does not exist as required");
        if (!t1.canRead())
            throw new IllegalArgumentException(
                    "File '" + t1.getAbsolutePath() + "' cannot be read as required");
        if (!t1.isDirectory())
            throw new IllegalArgumentException(
                    "File '" + t1.getAbsolutePath() + "' is not a file as required");
    }


    /**
     * tests two filesa for identical content
     *
     * @param t1 non-null readable File
     * @param t2 non-null readable File
     * @return true if all content is identical
     */
    public static boolean filesAreIdentical
    (File
             t1, File
             t2) {
        guaranteeReadableFile(t1);
        guaranteeReadableFile(t2);
        try {
            byte[] holder1 = new byte[4096];
            InputStream in1 = new BufferedInputStream(new FileInputStream(t1));
            byte[] holder2 = new byte[4096];
            InputStream in2 = new BufferedInputStream(new FileInputStream(t2));

            int Nread1 = in1.read(holder1);
            int Nread2 = in2.read(holder2);
            while (Nread1 > -1 && Nread2 > -1) {
                for (int i = 0; i < holder2.length; i++) {
                    if (holder1[i] != holder2[i])
                        return (false);

                }
                Nread1 = in1.read(holder1);
                Nread2 = in2.read(holder2);
                if (Nread1 != Nread2)
                    return (false);
            }
            return (Nread2 == Nread1);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Cnnot read compared files");
        }

    }

    /**
     * @param name non-null name of an existing readable file
     * @return non-null IpuptStream
     * @throws IllegalArgumentException on error
     */
    public static InputStream getInputStream
    (String
             name) {
        File TheFile = new File(name);
        return getInputStream(TheFile);
    }

    public static InputStream getInputStream
            (File
                     TheFile) {
        validateReadableFile(TheFile);
        try {
            InputStream in = new FileInputStream(TheFile);
            return (in);
        }
        catch (FileNotFoundException ex) {
            String Fullpath = TheFile.getAbsolutePath();
            throw new IllegalStateException(
                    "Requested File '" + Fullpath + "' does not exist -  but has been tested - HUH???");
        }
    }

    public static String getNameWithoutExtension
            (File
                     in) {
        String name = in.getName();
        int index = name.lastIndexOf(".");
        if (index == -1 || index == name.length() - 1)
            return (name); // no extension
        String nameWithout = name.substring(0, index);
        return getNameWithoutExtension(nameWithout);
    }

    public static String getNameWithoutExtension
            (String
                     name) {
        name = name.replace('\\', '/');
        int index = name.lastIndexOf("/");
        if (index > -1)
            name = name.substring(index + 1);
        index = name.lastIndexOf(".");
        if (index == -1 || index == name.length() - 1)
            return (name); // no extension
        String nameWithout = name.substring(0, index);
        return getNameWithoutExtension(nameWithout);
    }


    public static String getExtension
            (File
                     in) {
        String name = in.getName();
        int index = name.lastIndexOf(".");
        if (index == -1 || index == name.length() - 1)
            return (""); // no extension
        return (name.substring(index + 1));
    }

    /**
     * @param name non-null name of an existing readable file
     * @return non-null existing readable file
     * @throws IllegalArgumentException on error
     */
    public static File getReadableFile
    (String
             name) {
        File TheFile = new File(name);
        validateReadableFile(TheFile);
        return TheFile;
    }

    /**
     * Return if the fiel represented by the string in exists
     *
     * @param in non-null file name
     * @return as above
     */
    public static boolean fileExists
    (String
             in) {
        return (new File(in).exists());
    }

    public static void validateReadableFile
            (File
                     TheFile) {
        if (!TheFile.exists()) {
            String Fullpath = TheFile.getAbsolutePath();
            throw new IllegalArgumentException("Requested File '" + Fullpath + "' does not exist");
        }
        if (!TheFile.canRead()) {
            String Fullpath = TheFile.getAbsolutePath();
            throw new IllegalArgumentException("Requested File '" + Fullpath + "' cannot be read");
        }
        if (TheFile.isDirectory()) {
            String Fullpath = TheFile.getAbsolutePath();
            throw new IllegalArgumentException("Requested File '" + Fullpath + "'is a directory");
        }
    }

    /**
     * Send  theMessage to the url dest - return what is sent back
     *
     * @param theMessage possibly null message
     * @param dest       non-null url
     * @return non-null string
     */
    public static String getURLRespouse
    (String
             theMessage, URL
             dest) {
        try {
            HttpURLConnection conn = (HttpURLConnection) dest.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            if (theMessage != null)
                conn.setDoOutput(true);
            conn.connect();
            if (theMessage != null) {
                OutputStream out = conn.getOutputStream();
                OutputStreamWriter wout = new OutputStreamWriter(out, "UTF-8");

                wout.write(theMessage);
                wout.flush();
                wout.close();
            }
            InputStream inp = conn.getInputStream();
            String ret = FileUtilities.readInFile(inp);
            conn.disconnect();
            return (ret);
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
            throw new RuntimeException(ex1);
        }
    }

    /**
     * Send  theMessage to the url dest - return what is sent back
     *
     * @param theMessage possibly null message
     * @param dest       non-null url
     * @return non-null string
     */
    public static void sendURLText
    (String
             theMessage, URL
             dest) {
        try {
            HttpURLConnection conn = (HttpURLConnection) dest.openConnection();
            conn.setDoInput(false);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.connect();
            OutputStream out = conn.getOutputStream();
            OutputStreamWriter wout = new OutputStreamWriter(out, "UTF-8");

            wout.write(theMessage);
            wout.flush();
            wout.close();
            conn.disconnect();
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
            throw new RuntimeException(ex1);
        }
    }

    public static void flattenDirectory(String filename, boolean rename) {
        flattenDirectory(new File(filename), rename);
    }

    public static void flattenDirectory(File theDir, boolean rename) {
        File[] allFiles = getAllFiles(theDir);
        boolean nameIsUnigue = false;
        Set<String> nameSeen = new HashSet<String>();
        for (int i = 0; i < allFiles.length; i++) {
            File allFile = allFiles[i];
            String name = allFile.getName();
            if (nameSeen.contains(name))
                nameIsUnigue = true;
            nameSeen.add(name);
        }
        String newStr;
        DecimalFormat df = new DecimalFormat("0000");
        for (int i = 0; i < allFiles.length; i++) {
            File allFile = allFiles[i];
            if (allFile.isDirectory())
                continue;
            if (allFile.getParent().equals(theDir))
                continue;

            String name = allFile.getName();
            String extension = FileUtilities.getExtension(allFile);
            newStr = df.format((i + 1)) + "." + extension;
            File target = new File(theDir, newStr);
            FileUtilities.copyFile(allFile, target);
            allFile.delete();
        }

    }

    public static void flattenDirectories(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            flattenDirectory(arg, true);
        }
    }

    public static void removeWhiteSpace(String[] args) {
        for (int i = 0; i < args.length; i++) {
            File arg = new File(args[i]);
            removeWhiteSpace(arg);
        }
    }

    public static void removeWhiteSpace(File[] args) {
        for (int i = 0; i < args.length; i++) {
            removeWhiteSpace(args[i]);
        }
    }

    public static void appendToAppendable(final String fileName, final Appendable pOut) {
        appendToAppendable(new File(fileName), pOut);

    }

    public static void appendToAppendable(final File pFile, final Appendable pOut) {
        try {
            System.out.println(pFile.getName());
            LineReader rdr = new LineReader(new FileReader(pFile));
            appendToAppendable(rdr, pOut);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }

    }

    public static void appendToAppendable(final LineReader pRdr, final Appendable pOut) {
        try {
            String line = pRdr.readLine();
            while (line != null) {
                pOut.append(line);
                pOut.append("\n");
                line = pRdr.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * { class
     *
     * @name HasExtensionFilter
     * @function a file name filter which chooses files ending with a
     * string - usually an extension
     * }
     */
    public static class HasExtensionFilter implements FilenameFilter {

        //- *******************
        //- Fields
        /**
         * { field
         *
         * @name extension
         * @function the test string
         * }
         */
        private String extension;

        //- *******************
        //- Methods

        /**
         * { constructor
         *
         * @param e test string
         *          }
         * @name HasExtensionFilter
         * @function Constructor of HasExtensionFilter
         */
        public HasExtensionFilter(String e) {
            if (!e.startsWith(".")) {
                extension = "." + e;
            }
            else {
                extension = e;
            }
        }

        /**
         * { method
         *
         * @param dir  the test directory
         * @param name thes name of file in directory dir
         * @return true if file passes test
         * @name accept
         * @function test if file should pass filter i.e has proper extension or is
         * ending
         * @policy rarely override
         * @primary }
         */
        public boolean accept(File dir, String name) {
            if (name.endsWith(extension))
                return (true);

            File file = new File(dir, name);
            if (file.isDirectory())
                return true;
            return false;
        }

//- *******************
//- End Class HasExtensionFilter
    }

    /**
     * { class
     *
     * @name EndsWithFilter
     * @function a file name filter which chooses files ending with a
     * string - usually an extension
     * }
     */
    public static class EndsWithFilter implements FilenameFilter {

        //- *******************
        //- Fields
        /**
         * { field
         *
         * @name extension
         * @function the test string
         * }
         */
        private String extension;

        //- *******************
        //- Methods

        /**
         * { constructor
         *
         * @param e test string
         *          }
         * @name EndsWithFilter
         * @function Constructor of EndsWithFilter
         */
        public EndsWithFilter(String e) {
            extension = e;
        }

        /**
         * { method
         *
         * @param dir  the test directory
         * @param name thes name of file in directory dir
         * @return true if file passes test
         * @name accept
         * @function test if file should pass filter i.e has proper extension or is
         * ending
         * @policy rarely override
         * @primary }
         */
        public boolean accept(File dir, String name) {
            if (name.endsWith(extension)) {
                return (true);
            }
            return (new File(dir, name).isDirectory());
        }

//- *******************
//- End Class EndsWithFilter
    }

    /**
     * { class
     *
     * @name DirectoryNamedFilter
     * @function a directory with a specific name
     * }
     */
    public static class DirectoryNamedFilter implements FilenameFilter {

        //- *******************
        //- Fields
        /**
         * { field
         *
         * @name extension
         * @function the test string
         * }
         */
        private final String Name;

        //- *******************
        //- Methods

        /**
         * { constructor
         *
         * @param e test string
         *          }
         * @name DirectoryNamedFilter
         * @function Constructor of EndsWithFilter
         */
        public DirectoryNamedFilter(String e) {
            Name = e;
        }

        /**
         * { method
         *
         * @param dir  the test directory
         * @param name thes name of file in directory dir
         * @return true if file passes test
         * @name accept
         * @function test if file should pass filter i.e has proper extension or is
         * ending
         * @policy rarely override
         * @primary }
         */
        public boolean accept(File dir, String name) {
            File test = new File(dir, name);
            if (!test.isDirectory())
                return (false);
            if (name.equals(Name)) {
                return (true);
            }
            return (false);
        }

//- *******************
//- End Class DirectoryNamedFilter
    }

    /**
     * { class
     *
     * @name IsDirectoryFilter
     * @function sorts directories only
     * }
     */
    public static class IsDirectoryFilter implements FilenameFilter {

        //- *******************
        //- Methods

        /**
         * { constructor
         *
         * @name IsDirectoryFilter
         * @function Constructor of IsDirectoryFilter
         * }
         */
        public IsDirectoryFilter() {
        }

        /**
         * { method
         *
         * @param dir  the test directory
         * @param name this name of file in directory dir
         * @return true if file passes test
         * @name accept
         * @function test if file should pass filter i.e is a directory
         * @policy <Add Comment Here>
         * @primary }
         */
        public boolean accept(File dir, String name) {
            return (new File(dir, name).isDirectory());
        }

//- *******************
//- End Class IsDirectoryFilter
    }

    public static void main(String[] args) {
        File ud = getUserDirectory();
        String[] files = getAllFilesWithExtension(ud, "pep.xml");
        removeWhiteSpace(files);
        //   flattenDirectories(args);
        // removeWhiteSpace(args);
    }

    //- *******************
//- End Class FileUtilities
}


