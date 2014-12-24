package com.lordjoe.utilities;

import java.io.*;
import java.util.jar.*;
import java.util.*;

/**
 * com.lordjoe.utilities.JarUtilities
 *
 * @author Steve Lewis
 * @date 5/14/13
 */
public class JarUtilities {
    public static JarUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = JarUtilities.class;

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");


    private final File m_JarFile;
    private final Map<String, JarEntry> m_NameToEntry = new HashMap<String, JarEntry>();

    public JarUtilities(File jarFile) {
        m_JarFile = jarFile;
    }

    public File getJarFile() {
        return m_JarFile;
    }

    protected JarEntry getJarEntry(String name) {
        if (m_NameToEntry.containsKey(name))
            return m_NameToEntry.get(name);
        JarEntry ret = new JarEntry(name);
        m_NameToEntry.put(name, ret);
        return ret;

    }

    @SuppressWarnings("UnusedDeclaration")
    public void jarClassPath() {
        File[] directorys = getClassPathDirectories();
        jarDirectories(getJarFile(), directorys);
    }

    public File[] getClassPathDirectories() {
        String classpath = System.getProperty("java.class.path");
        String[] pathItems = classpath.split(PATH_SEPARATOR);
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        File[] pathDirs = getPathDirectories(pathItems);
        return pathDirs;
    }

    protected File[] getPathDirectories(String[] pathItems) {
        List<File> holder = new ArrayList<File>();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < pathItems.length; i++) {
            String pathItem = pathItems[i];
            File test = new File(pathItem);
            if (test.exists() && test.isDirectory())
                holder.add(test);
        }
        File[] ret = new File[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * make a jar file from a list of directories
     *
     * @param jarFile
     * @param directorys
     */
    public void jarDirectories(File jarFile, File... directorys) {
        try {
            File parentDir = jarFile.getParentFile();
            if(parentDir != null)
                //noinspection ResultOfMethodCallIgnored
                parentDir.mkdirs();
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile, false), manifest);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < directorys.length; i++) {
                File directory = directorys[i];
                String baseName = directory.getAbsolutePath().replace("\\", "/");
                add(directory, baseName, target);

            }
            target.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * make a jar file from a list of directories
     *
     * @param jarFile
     * @param directorys
     */

    @SuppressWarnings("UnusedDeclaration")
    public void jarDirectories(File jarFile,Set<String> usedFiles, File... directorys) {
        try {
            File parentDir = jarFile.getParentFile();
            if(parentDir != null)
                //noinspection ResultOfMethodCallIgnored
                parentDir.mkdirs();
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile, false), manifest);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < directorys.length; i++) {
                File directory = directorys[i];
                String baseName = directory.getAbsolutePath().replace("\\", "/");
                add(directory, baseName, target);

            }
            target.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(File source, String baseName, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getAbsolutePath().replace("\\", "/");
                name = name.substring(baseName.length());
                if (!name.isEmpty()) {
                    if (name.startsWith("/"))
                        name = name.substring(1);
                    if (!name.endsWith("/"))
                        name += "/";
                    if (!m_NameToEntry.containsKey(name)) {
                        JarEntry entry = getJarEntry(name);
                        entry.setTime(source.lastModified());
                        target.putNextEntry(entry);
                        target.closeEntry();

                    }
                }
                //noinspection ConstantConditions
                for (File nestedFile : source.listFiles())
                    add(nestedFile, baseName, target);
                return;
            }

            String entryName = source.getPath().replace("\\", "/");
            entryName = entryName.substring(baseName.length());
            if (!m_NameToEntry.containsKey(entryName)) {
                JarEntry entry = getJarEntry(entryName);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                in = new BufferedInputStream(new FileInputStream(source));

                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1)
                        break;
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        } finally {
            if (in != null)
                in.close();
        }
    }
}
