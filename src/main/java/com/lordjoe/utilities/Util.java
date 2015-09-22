/**
 * { file
 *
 * @name Util.java
 * @function this class - a Nullity implements a large number of
 * useful functions
 * @author> Steven M. Lewis
 * @copyright> ***********************
 * Copyright (c) 1996,97,98
 * Steven M. Lewis
 * www.LordJoe.com
 * ***********************
 * @date> Mon Jun 22 21:48:24 PDT 1998
 * @version> 1.0
 * <p/>
 * }
 */
package com.lordjoe.utilities;

import org.xml.sax.*;

import javax.annotation.*;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;

/**
 * { class
 *
 * @name Util
 * @function this class - a Nullity implements a large number of
 * useful functions
 * }
 */
@SuppressWarnings({"UnusedDeclaration", "AccessStaticViaInstance", "UnnecessaryLocalVariable", "unchecked", "ForLoopReplaceableByForEach", "RedundantIfStatement"})
public abstract class Util {
    public static final String APP_NAME_PROPERTY = "application.name"; //
    public static final String UNKNOWN_APP_NAME = "Unknown Application"; //
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yy");
    public static final SimpleDateFormat DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat("MM/dd/yy hh:mm");

    public static final Random RND = new Random();
    // Probably should use subclasses whivh are immutable
    public static final Vector EMPTY_VECTOR = new Vector();
    public static final Map EMPTY_MAP = new HashMap();
    public static final Set EMPTY_SET = new HashSet();
    public static final List EMPTY_LIST = new ArrayList();
    // ========================
    // Empty arrays - might as well be singletons to save
    // construction
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final Thread[] EMPTY_THREAD_ARRAY = new Thread[0];
    public static final Integer[] EMPTY_INTEGER_ARRAY = {};
    public static final Character[] EMPTY_CHARACTER_ARRAY = {};
    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    public static final Comparator DOUBLE_COMPARE = new DoubleComparator();
    public static final Comparator<? super String> STRING_COMPARE = new StringComparator();
    public static final Comparator<String> STRING_AS_NUMBER_COMPARE = new StringAsNumberComparator();
    public static final Comparator<String> STRING_AS_NUMBER_DESCENDING_COMPARE = new StringAsNumberComparator();
    public static final Comparator INTEGER_COMPARE = new IntegerComparator();
    public static final Comparator DATE_COMPARE = new DateComparator();

    private static String[] gIndentStrings;
    private static String gLocalIPAddress;
    private static final Map gEmptyArrays = new HashMap();

    public static final Random gRandomizer = new Random(System.currentTimeMillis());
    public static final Date gStartTime = new Date();
    public static final DateFormat US_DATE_TIME = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static final DateFormat DB_TIMESTAMP_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd";
    public static final DateFormat DB_DATE_TIME = new SimpleDateFormat(DATE_FORMAT_STRING);
    public static final String gStartTimeString = US_DATE_TIME.format(gStartTime);
    //  public static final Comparator NAME_COMPARATOR = new NameComparator();
    public static final Comparator<Object> OBJECT_COMPARATOR = new BaseJavaComparator();  // what object does
    public static final int DEFAULT_MAX_STRING_LENGTH = 0x4fff;

    protected final static long secondInMs = 1000L;
    protected static final long minuteInMs = 60L * secondInMs;
    protected static final long hourInMs = 60L * minuteInMs;
    protected static final long dayInMs = 24L * hourInMs;

    public static class BaseJavaComparator implements Comparator<Object> {
        @Override
        public int compare(final Object o1, final Object o2) {
            return Util.objectCompareTo(o1, o2);
        }

    }

    public static final int DEFAULT_MAX_VALUE = 4095;

    /**
     * count the occurances of each positive value up to DEFAULT_MAX_VALUE
     *
     * @param values !null array of non-negative values
     * @return array of counts
     */
    public static int[] computeStatistics(int[] values) {
        return computeStatistics(values, DEFAULT_MAX_VALUE);
    }

    /**
     * count the occurances of each positive value up to maxvalue
     *
     * @param values   !null array of non-negative values
     * @param maxValue value to truncate
     * @return
     */
    public static int[] computeStatistics(int[] values, int maxValue) {
        int max = 0;
        int[] temp = new int[maxValue + +1];
        for (int i = 0; i < values.length; i++) {
            int val = Math.min(maxValue, values[i]);
            max = Math.max(val, max);
            if (val < 0)
                throw new IllegalStateException("statistics are fpr positive numbers only ");
            temp[val]++;
        }
        int[] ret = new int[max];
        System.arraycopy(temp, 0, ret, 0, max + 1);
        return ret;
    }


    /**
     * count the occurances of each positive value up to DEFAULT_MAX_VALUE
     *
     * @param values !null array of non-negative values
     * @return array of counts
     */
    public static int[] computeStatistics(short[] values) {
        return computeStatistics(values, DEFAULT_MAX_VALUE);
    }

    /**
     * count the occurances of each positive value up to maxvalue
     *
     * @param values   !null array of non-negative values
     * @param maxValue value to truncate
     * @return
     */
    public static int[] computeStatistics(short[] values, int maxValue) {
        int max = 0;
        int[] temp = new int[maxValue + +1];
        for (int i = 0; i < values.length; i++) {
            int val = Math.min(maxValue, values[i]);
            max = Math.max(val, max);
            if (val < 0)
                throw new IllegalStateException("statistics are fpr positive numbers only ");
            temp[val]++;
        }
        int[] ret = new int[max + 1];
        System.arraycopy(temp, 0, ret, 0, max + 1);
        return ret;
    }

    /**
     * count the occurances of each positive value up to DEFAULT_MAX_VALUE
     *
     * @param values !null array of non-negative values
     * @return array of counts
     */
    public static int[] computeStatistics(long[] values) {
        return computeStatistics(values, DEFAULT_MAX_VALUE);
    }

    /**
     * count the occurances of each positive value up to maxvalue
     *
     * @param values   !null array of non-negative values
     * @param maxValue value to truncate
     * @return
     */
    public static int[] computeStatistics(long[] values, int maxValue) {
        int max = 0;
        int[] temp = new int[maxValue + 1];
        for (int i = 0; i < values.length; i++) {
            int val = Math.min(maxValue, (int) values[i]);
            max = Math.max(val, max);
            if (val < 0)
                throw new IllegalStateException("statistics are fpr positive numbers only ");
            temp[val]++;
        }
        int[] ret = new int[max];
        System.arraycopy(temp, 0, ret, 0, max + 1);
        return ret;
    }


    /**
     * comparason Java uses forObject
     *
     * @param o1
     * @param o2
     * @return
     */
    public static int objectCompareTo(final Object o1, final Object o2) {
        int h1 = System.identityHashCode(o1);
        int h2 = System.identityHashCode(o2);
        if (h1 == h2)
            return 0;
        if (h1 < h2)
            return 1;
        else
            return -1;
    }


    public static String getExceptionStack(Throwable t) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        t.printStackTrace(pout);
        return new String(out.toByteArray());

    }

    public static int maximum(int... values) {
        int ret = values[0];
        for (int i = 1; i < values.length; i++) {
            ret = Math.max(ret, values[i]);

        }
        return ret;
    }

    public static int minimum(int... values) {
        int ret = values[0];
        for (int i = 1; i < values.length; i++) {
            ret = Math.max(ret, values[i]);

        }
        return ret;
    }

    public static int[] convert(Integer... wrapped) {
        int[] ret = new int[wrapped.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = wrapped[i];
        }
        return ret;
    }


    public static short[] convert(Short... wrapped) {
        short[] ret = new short[wrapped.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = wrapped[i];
        }
        return ret;
    }


    public static double[] convert(Double... wrapped) {
        double[] ret = new double[wrapped.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = wrapped[i];
        }
        return ret;
    }


    public static long[] convert(Long... wrapped) {
        long[] ret = new long[wrapped.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = wrapped[i];
        }
        return ret;
    }


    public static float[] convert(Float... wrapped) {
        float[] ret = new float[wrapped.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = wrapped[i];
        }
        return ret;
    }


    public static boolean[] convert(Boolean... wrapped) {
        boolean[] ret = new boolean[wrapped.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = wrapped[i];
        }
        return ret;
    }

    /**
     * filt
     *
     * @param original
     * @return
     */
    public static <T> T[] getKnownValues(T[] original) {
        List<T> holder = new ArrayList();
        for (int i = 0; i < original.length; i++) {
            T t = original[i];
            if (t.toString().startsWith("UNKNOWN"))
                continue;
            holder.add(t);
        }
        T[] ret = (T[]) Array.newInstance(original.getClass().getComponentType(), holder.size());
        holder.toArray(ret);
        return ret;
    }

    public static double maximumDouble(double... values) {
        double ret = values[0];
        for (int i = 1; i < values.length; i++) {
            ret = Math.max(ret, values[i]);

        }
        return ret;
    }

    private static Boolean gRunningOnMac;

    public static synchronized boolean isMac() {
        if (gRunningOnMac == null) {
            String osName = System.getProperty("os.name");
            gRunningOnMac = osName.startsWith("Mac OS");
        }
        return gRunningOnMac;
    }

    public static double minimumDouble(double... values) {
        double ret = values[0];
        for (int i = 1; i < values.length; i++) {
            ret = Math.max(ret, values[i]);

        }
        return ret;
    }
    //- *******************
    //- Methods

    public static Date getStartTime() {
        return ((Date) gStartTime.clone());
    }

    public static String getStartTimeString() {
        return (gStartTimeString);
    }

    public static String getApplicationName() {
        String ret = System.getProperty(APP_NAME_PROPERTY);
        if (ret == null)
            ret = UNKNOWN_APP_NAME;
        return (ret);
    }

    public static void setApplicationName(String in) {
        Properties props = System.getProperties();
        props.setProperty(APP_NAME_PROPERTY, in);
    }

    /**
     * return the original cause of an exception
     *
     * @param ex caught exception
     * @return
     */
    public static Throwable getUltimateCause(Throwable ex) {
        if (ex.getCause() == null)
            return ex;
        if (ex.getCause() == ex)
            return ex;
        return getUltimateCause(ex.getCause());
    }

    public static RuntimeException getRuntimeCause(Throwable ex) {
        Throwable cause = getUltimateCause(ex);
        if (cause instanceof SAXParseException) {
            SAXParseException parseEx = (SAXParseException) cause;
            int col = parseEx.getColumnNumber();
            int line = parseEx.getLineNumber();
            System.err.println("Parse exception at line " + line + " column " + col);
        }
        if (cause instanceof RuntimeException)
            return (RuntimeException) cause;
        return
                new RuntimeException(cause);
    }


    /**
     * return an empty array of the requested class
     *
     * @param in non-null requested class
     * @return non-null empty array of that class
     */
    public static Object emptyArray(Class in) {
        synchronized (gEmptyArrays) {
            Object ret = gEmptyArrays.get(in);
            if (ret == null) {
                ret = Array.newInstance(in, 0);
                gEmptyArrays.put(in, ret);
            }
            return (ret);
        }
    }

    public static String definedLengthNumber(int n, int places) {
        String ret = Integer.toString(n);
        while (ret.length() < places)
            ret = "0" + ret;
        return ret;
    }

    /**
     * return an empty array of the requested class
     *
     * @param in non-null requested class
     * @return non-null CLass of aht array
     */
    public static Class arrayClass(Class in) {
        return (emptyArray(in).getClass());
    }

    public static String replaceText(String s, String pattern, Object replace) {
        if (replace != null)
            return s.replace(pattern, replace.toString());
        else
            return s.replace(pattern, "");
    }

    public static String integerToString(Integer i) {
        if (i == null)
            return ("0");
        return i.toString();
    }

    /**
     * { method
     *
     * @param src - source data
     * @param dst - destination data
     *            }
     * @name copyInto
     * @function copies all elements of src array into dst array up to the
     * minimum common size
     */
    public static void copyInto(Object[] src, Object[] dst) {
        if (src != null && dst != null) {
            int n = Math.min(src.length, dst.length);
            System.arraycopy(src, 0, dst, 0, n);
        }
    }

    /**
     * { method
     *
     * @param src - source data
     * @param dst - destination data
     *            }
     * @name copyInto
     * @function copies all elements of src array into dst array up to the
     * minimum common size
     */
    public static void copyInto(int[] src, int[] dst) {
        if (src != null && dst != null) {
            int n = Math.min(src.length, dst.length);
            System.arraycopy(src, 0, dst, 0, n);
        }
    }

    /**
     * { method
     *
     * @param src - source data
     * @param dst - destination data
     *            }
     * @name copyInto
     * @function copies all elements of src array into dst array up to the
     * minimum common size
     */
    public static void copyInto(char[] src, char[] dst) {
        if (src != null && dst != null) {
            int n = Math.min(src.length, dst.length);
            System.arraycopy(src, 0, dst, 0, n);
        }
    }

    /**
     * { method
     *
     * @param src - source data
     * @param dst - destination data
     *            }
     * @name copyInto
     * @function copies all elements of src array into dst array up to the
     * minimum common size
     */
    public static void copyInto(boolean[] src, boolean[] dst) {
        if (src != null && dst != null) {
            int n = Math.min(src.length, dst.length);
            System.arraycopy(src, 0, dst, 0, n);
        }
    }


// Parsing

    /**
     * { method
     *
     * @param in the string
     * @return collection of 0 or more substrings
     *         }
     * @name parseTabDelimited
     * @function break a StringBuilder containing 0 or more tabe into
     * substrings separated by tabs - the tabs are dropped
     */
    public static String[] parseTabDelimited(StringBuilder in) {
        return (parseTabDelimited(new String(in)));
    }

    /**
     * { method
     *
     * @param OneLine the array
     * @param count   where to start
     * @return collection of 0 or more substrings
     *         }
     * @name parseTabDelimited
     * @function break a char array containing 0 or more tabe into
     * substrings separated by tabs - the tabs are dropped
     * @UnusedParam> in the string
     */
    public static String[] parseTabDelimited(char[] OneLine, int count) {
        String s = new String(OneLine, 0, count);
        return (parseTabDelimited(s));
    }

    /**
     * { method
     *
     * @param s the string
     * @return collection of 0 or more substrings
     *         }
     * @name parseTabDelimited
     * @function break a String containing 0 or more tabe into
     * substrings separated by tabs - the tabs are dropped
     */
    public static String[] parseTabDelimited(String s) {
        if (s == null || s.length() == 0) {
            return (new String[0]);
        }
        return (parseTokenDelimited(s, '\t'));
    }

    /**
     * { method
     *
     * @param s the string
     * @return collection of 0 or more substrings
     *         }
     * @name parseCommaDelimited
     * @function break a String containing 0 or more  into
     * substrings separated by commas - the commas are dropped
     */
    public static String[] parseCommaDelimited(String s) {
        if (s == null || s.length() == 0) {
            return (new String[0]);
        }
        return (parseTokenDelimited(s, ','));
    }


    /**
     * { method
     *
     * @param s     the string
     * @param token delimiter token
     * @return collection of 1 or more substrings
     *         }
     * @name parseTokenDelimited
     * @function break a String containing 0 or more occurances
     * of the char token into
     * substrings separated by token - the tokens are dropped
     */
    public static String[] parseTokenDelimited(String s, char token) {
        String[] ret;
        int SLength = s.length();
        if (SLength == 0)
            return EMPTY_STRING_ARRAY;
        // string does not hold token
        if (s.indexOf(token) == -1) {
            ret = new String[1];
            ret[0] = s;
        }
        else {
            List holder = new ArrayList();
            int n = s.length();
            StringBuilder sb = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                char c = s.charAt(i);
                if (c == '\\') {
                    if (i == (n - 1)) {
                        sb.append(c);
                        break;
                    }
                    else {
                        i++;
                        c = s.charAt(i);
                        sb.append(c);
                    }
                }
                else {
                    if (c == token) {
                        holder.add(sb.toString());
                        sb.setLength(0);
                    }
                    else {
                        sb.append(c);
                    }

                }
            }
            if (sb.length() > 0)
                holder.add(sb.toString());
            ret = Util.collectionToStringArray(holder);
        }
        return (ret);
    }

    /**
     * { method
     *
     * @param s the string
     * @return collection of 1 or more substrings
     *         }
     * @name parseWhiteSpaceDelimited
     * @function break a String containing 0 or more occurances
     * of the char token into
     * substrings separated by any white space
     */
    public static String[] parseWhiteSpaceDelimited(String s) {
        String[] ret;
        Vector tokens = new Vector();
        int SLength = s.length();
        StringBuilder accum = new StringBuilder(SLength);

        for (int i = 0; i < SLength; i++) {
            char c = s.charAt(i);
            if (c > ' ') {
                accum.append(c);
            }
            else { // white space
                if (accum.length() > 0) {
                    tokens.addElement(accum.toString());
                    accum.setLength(0);
                }
            }
        }
        if (accum.length() > 0) {
            tokens.addElement(accum.toString());
        }

        ret = new String[tokens.size()];
        tokens.copyInto(ret);
        return (ret);
    }


    /**
     * Crude expression tokenizer - will not work for complex expresions
     * but may suffice for most
     *
     * @param s non-null expression
     * @return non-null array of tokens
     */
    public static String[] getJavaVariables(String[] items) {
        Set holder = new HashSet();

        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            if (isTokenJavaVariable(item))
                holder.add(item);
        }
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        Arrays.sort(ret);
        return ret;
    }

    private static boolean isTokenJavaVariable(String s) {
        //noinspection SimplifiableIfStatement
        if (s == null || s.length() == 0)
            return false;
        return Character.isJavaIdentifierStart(s.charAt(0));
    }


    public static final int NOT_ACCUMULATING = 0;
    public static final int ACCUM_VARIABLE = 1;
    public static final int ACCUM_NUMBER = 2;
    public static final int ACCUM_PUNCT = 3;

    /**
     * Crude expression tokenizer - will not work for complex expresions
     * but may suffice for most
     *
     * @param s non-null expression
     * @return non-null array of tokens
     */
    public static String[] tokenizeExpression(String s) {
        String[] ret;
        List tokens = new ArrayList();
        int SLength = s.length();
        StringBuilder accum = new StringBuilder(SLength);
        int accumType = NOT_ACCUMULATING;

        for (int i = 0; i < SLength; i++) {
            char c = s.charAt(i);
            if (accumType == NOT_ACCUMULATING) {
                if (Character.isWhitespace(c))
                    continue;
                accumType = findAcumType(c);
                accum.append(c);
            }
            else {
                switch (accumType) {
                    case ACCUM_VARIABLE:
                        if (Character.isJavaIdentifierPart(c)) {
                            accum.append(c);
                        }
                        else {
                            saveaccumulatedToken(accum, tokens);
                            accumType = findAcumType(c);
                            if (accumType != NOT_ACCUMULATING) {
                                accum.append(c);
                            }
                        }
                        break;
                    case ACCUM_NUMBER:
                        if (Character.isDigit(c) || c == '.') {
                            accum.append(c);
                        }
                        else {
                            saveaccumulatedToken(accum, tokens);
                            accumType = findAcumType(c);
                            if (accumType != NOT_ACCUMULATING) {
                                accum.append(c);
                            }
                        }
                        break;
                    case ACCUM_PUNCT:
                        if (isCompatablePunctuation(accum, c)) {
                            accum.append(c);
                        }
                        else {
                            saveaccumulatedToken(accum, tokens);
                            accumType = findAcumType(c);
                            if (accumType != NOT_ACCUMULATING) {
                                accum.append(c);
                            }
                        }
                        break;
                }
            }

        }
        if (accum.length() > 0) {
            tokens.add(accum.toString());
        }

        ret = new String[tokens.size()];
        tokens.toArray(ret);
        return (ret);
    }

    private static boolean isCompatablePunctuation(StringBuilder accumType, char c) {
        if (Character.isDigit(c) || Character.isWhitespace(c) || Character.isJavaIdentifierPart(c))
            return false;
        String s = accumType.toString();
        if (c == '=' && (s.equals("<") || s.equals(">") || s.equals("=")))
            return true;
        return false; // Todo be smarter

    }

    private static void saveaccumulatedToken(StringBuilder pAccum, List pTokens) {
        if (pAccum.length() > 0) {
            pTokens.add(pAccum.toString());
            pAccum.setLength(0);
        }
    }

    private static int findAcumType(char pC) {
        int accumType;
        if (Character.isWhitespace(pC))
            return NOT_ACCUMULATING;
        if (Character.isJavaIdentifierStart(pC))
            return ACCUM_VARIABLE;
        if (Character.isDigit(pC))
            return ACCUM_NUMBER;

        return ACCUM_PUNCT;
    }

    /**
     * like it says - a convenient break point
     */
    public static void breakHere() {
    }

    /**
     * return the sum of a collection of Integers some of which
     * might be null
     *
     * @param added integers possibly null
     * @return as above
     */
    public static int addIntegers(Integer... added) {
        int ret = 0;
        for (Integer i : added) {
            if (i != null)
                ret += i;
        }
        return ret;
    }

    /**
     * { method
     *
     * @param s     the string
     * @param token delimiter token
     * @return collection of 1 or 2 substrings
     *         }
     * @name breakTokenDelimited
     * @function break a String containing 0 or more occurances
     * of the char token into
     * substrings separated by token - the tokens are dropped
     */
    public static String[] breakTokenDelimited(String s, char token) {
        String[] ret;
        int CurrentToken = s.indexOf(token);
        // string does not hold token
        if (s.indexOf(token) == -1) {
            ret = new String[1];
            ret[0] = s;
        }
        else {
            ret = new String[2];
            ret[0] = s.substring(0, CurrentToken);
            ret[1] = s.substring(CurrentToken + 1);
        }
        return (ret);
    }

    /**
     * { method
     *
     * @param s the buffer - must be non-empty
     * @return the collection of lines
     *         }
     * @name parseLinesWithBlanks
     * @function break a StringBuilder to a collection of lines
     * i.e. separate on \n
     */
    public static String[] parseLinesWithBlanks(String s) {
        List TheLines = new ArrayList();
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < s.length()) {
            char c = s.charAt(index++);
            if (c == '\r')
                continue;
            if (c == '\n') {
                TheLines.add(sb.toString());
                sb.setLength(0);
            }
            else {
                sb.append(c);
            }
        }
        if (sb.length() > 0)
            TheLines.add(sb.toString());
        String[] ret = new String[TheLines.size()];
        TheLines.toArray(ret);
        return (ret);
    }

    /**
     * { method
     *
     * @param s the buffer - must be non-empty
     * @return the collection of lines
     *         }
     * @name parseLines
     * @function break a StringBuilder to a collection of lines
     * i.e. separate on \n
     */
    public static String[] parseLines(String s) {
        List TheLines = new ArrayList();
        StringTokenizer t = new StringTokenizer(s, "\n\r");
        while (t.hasMoreTokens()) {
            String st = t.nextToken();
            TheLines.add(st);
        }
        String[] ret = new String[TheLines.size()];
        TheLines.toArray(ret);
        return (ret);
    }

    /**
     * { method
     *
     * @param s the buffer - must be non-empty
     * @return the collection of lines
     *         }
     * @name parseLines
     * @function break a StringBuilder to a collection of lines
     * i.e. separate on \n
     */
    public static String[] parseLines(StringBuilder s) {
        return (parseLines(s.toString()));
    }

    /**
     * { method
     *
     * @param s          text source
     * @param buffer     ???
     * @param StartPoint where to start
     * @return the line
     *         }
     * @name getLine
     * @function retrieve text till \n
     */
    protected static String getLine(StringBuilder s, IntegerRef StartPoint, StringBuilder buffer) {
        int start = StartPoint.value;
        buffer.setLength(0);
        while (start < s.length()) {
            char c = s.charAt(start++);
            int n = (int) c;
            // peek at binary value for debugging
            if (c == '\n') {
                break;
            }
            // count printing chars and tab
            if (c >= ' ' || c == '\t') {
                buffer.append(c);
            }
            //noinspection UnusedAssignment
            n = 0;
        }
        StartPoint.value = start;
        // drop trailing \r
        if (start < s.length()) {
            if (s.charAt(start) == '\r') {
                StartPoint.value++;
            }
        }
        // ; is comment
        if (buffer.length() > 0 && buffer.charAt(0) != ';') {
            return (new String(buffer));
        }
        // valid line
        if (start >= s.length()) {
            return (null);
        }
        return (getLine(s, StartPoint, buffer));
    }

    /**
     * { method
     *
     * @param v vector
     * @return the array or null if v empty
     *         }
     * @name vectorToArray
     * @function turn a vector into an array of objects
     */
    public static Object[] vectorToArray(Vector v) {
        return (vectorToArray(v, java.lang.Object.class));
    }

    /**
     * { method
     *
     * @param v           vector
     * @param TargetClass class used by the array
     * @return a non-null array
     *         }
     * @name vectorToArray
     * @function turn a vector into an array of objects
     */
    public static Object[] vectorToArray(Vector v, Class TargetClass) {
        return (collectionToArray(v, TargetClass));
    }

    /**
     * { method
     *
     * @param v           Thetable - non-null
     * @param TargetClass class for the array - non-null
     * @return and array ot objects ot type TargetClass = non-null
     *         }
     * @name hashtableKeysToArray
     * @function turn a Hashtable keys into an array of objects
     */
    public static Object[] hashtableKeysToArray(Hashtable v, Class TargetClass) {
        Enumeration e = v.keys();
        int count = 0;
        Object[] out = (Object[]) java.lang.reflect.Array.newInstance(TargetClass, v.size());
        while (e.hasMoreElements()) {
            out[count++] = e.nextElement();
        }
        return (out);
    }

    /**
     * { method
     *
     * @param v           Thetable - non-null
     * @param TargetClass class for the array - non-null
     * @return and array ot objects ot type TargetClass = non-null
     *         }
     * @name hashtableValuesToArray
     * @function turn a Hashtable values into an array of objects
     */
    public static Object[] hashtableValuesToArray(Hashtable v, Class TargetClass) {
        Enumeration e = v.elements();
        int count = 0;
        Object[] out = (Object[]) java.lang.reflect.Array.newInstance(TargetClass, v.size());
        while (e.hasMoreElements()) {
            out[count++] = e.nextElement();
        }
        return (out);
    }

    /**
     * { method
     *
     * @param v   Thetable
     * @param out Array of appropriate type sized to v.size
     * @return the array or null if v empty
     *         }
     * @name hashtableFillsArray
     * @function turn a Hashtable into an array of objects
     */
    public static Object[] hashtableFillsArray(Hashtable v, Object[] out) {
        Enumeration e = v.keys();
        int count = 0;
        Object added; // Better debugging on failure
        try {
            while (e.hasMoreElements()) {
                added = e.nextElement();
                out[count++] = added;
            }
        }
        catch (ArrayStoreException ex) {
            //noinspection SimplifiableIfStatement
            // if (true) throw new RuntimeException(ex);
        }
        return (out);
    }

    /**
     * { method
     *
     * @param s non-null test string
     * @return the number
     * @name numberLeadingBlanks
     * @function return number of blanks at the head of a string
     * @policy rarely override
     * @primary }
     */
    public int numberLeadingBlanks(String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                return (n);
            }
            else {
                n++;
            }
        }
        return (n);
    }

    /**
     * { method
     *
     * @param data array 1 to test - null or 0 length OK
     * @param test array 2 to test - null or 0 length OK
     * @return true if equal
     *         }
     * @name compareStringArrays
     * @function test is two string arrays are equal
     */
    public static boolean compareStringArrays(String[] data, String[] test) {
        if (data == null) {
            return (test == null);
        }
        if (test == null) {
            return (false);
        }
        if (data.length != test.length) {
            return (false);
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null || !data[i].equals(test[i])) {
                return (false);
            }
        }
        return (true);
        // perfect match
    }

    /**
     * { method
     *
     * @param data nun-null string array
     * @param test non-null test
     * @return true tes in  data
     *         }
     * @name stringArrayContains
     * @function true of data contains test
     */
    public static boolean stringArrayContains(String[] data, String test) {
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals(test)) {
                return (true);
            }
        }
        return (false);
        // perfect match
    }

    /**
     * { method
     *
     * @param base - non-null -  original array
     * @param drop - non-null array of strings to drop
     * @return non-null array in the order of base
     *         }
     * @name stringsNotContained
     * @function return an array of unique strings in base and not in drop
     */
    public static String[] stringsNotContained(String[] base, String[] drop) {
        Set inSet = arrayToSet(base);
        Set dropSet = arrayToSet(drop);
        inSet.removeAll(dropSet);
        List holder = new ArrayList(inSet.size());
        for (int i = 0; i < base.length; i++) {
            if (inSet.contains(base[i])) {
                inSet.remove(base[i]);
                holder.add(base[i]);
            }
        }
        String[] ret = Util.collectionToStringArray(holder);
        return (ret);
    }

    /**
     * { method
     *
     * @param in - non-null arrsy to place
     * @return non-null set
     *         }
     * @name arrayToSet
     * @function convert an
     */
    public static Set arrayToSet(Object[] in) {
        Set ret = new HashSet();
        Collections.addAll(ret, in);
        return (ret);
    }

    /**
     * return a sorted array using the last equal entru - i.e. favoring lower case
     * Used where upper case is stored in maps for caseless comparisons
     *
     * @param in non-null array of strings
     * @return non-null array of strings sorted alphabetically without duplicates
     */
    public static String[] removeDuplicateEntries(String[] in) {
        Arrays.sort(in);
        List holder = new ArrayList();
        String current = "";
        for (int i = 0; i < in.length; i++) {
            String s = in[i];
            if (!s.equalsIgnoreCase(current)) {
                if (!isEmptyString(current))
                    holder.add(current);
            }
            current = s;
        }

        String[] out = Util.collectionToStringArray(holder);
        return (out);
    }

    /**
     * { method
     *
     * @param in - non-null array of strings
     * @return non-null concatentatd string
     *         }
     * @name concatLines
     * @function concat strings separated by \n
     */
    public static String concatLines(String[] in) {
        return (concatSeparated(in, "\n"));
    }

    /**
     * { method
     *
     * @param in        - non-null array of strings
     * @param separator - non-null separator
     * @return non-null concatentatd string
     *         }
     * @name concatLines
     * @function concat strings separated by \n
     */
    public static String concatSeparated(String[] in, char separator) {
        char[] chars = {separator};
        return (concatSeparated(in, new String(chars)));
    }

    /**
     * { method
     *
     * @param in        - non-null array of strings
     * @param separator - non-null separator
     * @return non-null concatentatd string
     *         }
     * @name concatLines
     * @function concat strings separated by \n
     */
    public static String concatSeparated(String[] in, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length; i++) {
            sb.append(in[i]);
            if (i < in.length - 1)
                sb.append(separator);
        }
        return (sb.toString());
    }

    public static boolean containsNonLetterOrUnderscore(String test) {
        for (int i = 0; i < test.length(); i++) {
            char c = test.charAt(i);
            if (!Character.isLetter(c) && c != '_') {
                return (true);
            }
        }
        return (false);
    }

    /**
     * return a string with punctuation and spaces replaced with _
     *
     * @param test non-null string
     * @return non-null string as above
     */
    public static String replacePunctuationWithUnderScore(String test) {
        StringBuilder sb = new StringBuilder();
        char lastInsert = 0;
        for (int i = 0; i < test.length(); i++) {
            char c = test.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-') {
                if ('_' != lastInsert) {
                    lastInsert = '_';
                    sb.append(lastInsert);
                }
            }
            else {
                lastInsert = c;
                sb.append(lastInsert);
            }
        }
        return sb.toString();
    }

    /**
     * { method
     *
     * @param base - non-null -  original array
     * @return non-null array cleaned
     *         }
     * @name trimNonPrinting
     * @function return an array with leading or trialing nonprinting characters
     */
    public static String[] trimNonPrinting(String[] base) {
        for (int i = 0; i < base.length; i++) {
            base[i] = trimNonPrinting(base[i]);
        }
        return (base);
    }

    /**
     * { method
     *
     * @param base - non-null string
     * @return non-null cleaned result
     *         }
     * @name trimNonPrinting
     * @function return a string starting and ending with isJavaIdentifierPart characters
     */
    public static String trimNonPrinting(String base) {
        int n = base.length();
        if (n == 0)
            return (base);
        char c = base.charAt(0);
        if (c < ' ' || c > 126)
            return (trimNonPrinting(base.substring(1)));
        if (n == 1)
            return (base);
        c = base.charAt(n - 1);
        if (c < ' ' || c > 126)
            return (trimNonPrinting(base.substring(0, n - 1)));
        return (base);
    }

    /**
     * { method
     *
     * @param base - non-null -  original array
     * @return non-null array without duplicated
     *         }
     * @name eliminateDuplicates
     * @function return an array with no duplicate entried
     * but in the original order
     */
    public static String[] eliminateDuplicates(String[] base) {
        List holder = new ArrayList();
        Set test = new HashSet();
        for (int i = 0; i < base.length; i++) {
            if (!test.contains(base[i])) {
                test.add(base[i]);
                holder.add(base[i]);
            }
        }
        return (collectionToStringArray(holder));
    }

    /**
     * { method
     *
     * @param holder - non-null collection holding strings
     * @return non-null array of strings
     *         }
     * @name collectionToStringArray
     * @function return an array of strings from a collection
     */
    public static String[] collectionToStringArray(Collection holder) {
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return (ret);
    }

    /**
     * { method
     *
     * @param enum - non-null Enumeration
     * @return non-null array of strings
     *         }
     * @name EnumerationToStringArray
     * @function return an array of strings from a collection
     */
    public static String[] enumerationToStringArray(Enumeration enumVal) {
        List holder = new ArrayList();
        while (enumVal.hasMoreElements())
            holder.add(enumVal.nextElement());
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return (ret);

    }

    /**
     * { method
     *
     * @param base - non-null -  original array
     * @return non-null sorted set without duplicates
     *         }
     * @name uniqueSort
     * @function return a sorted array of strings without duplicates
     */
    public static String[] uniqueSort(String[] base) {
        if (base.length == 0)
            return (EMPTY_STRING_ARRAY);
        //noinspection SimplifiableIfStatement,ConstantIfStatement
        if (true)
            throw new UnsupportedOperationException("Fix This"); // todo
        String[] BaseClone = null; // Clone.clone(base);
        Vector holder = new Vector();
        sort(BaseClone);
        holder.addElement(BaseClone[0]);
        String Last = BaseClone[0];
        for (int i = 1; i < BaseClone.length; i++) {
            if (!Last.equals(BaseClone[i])) {
                Last = BaseClone[i];
                holder.addElement(BaseClone[i]);
            }
        }
        String[] ret = new String[holder.size()];
        holder.copyInto(ret);
        return (ret);

    }

//    /**
//     * Sort an array according to a property value
//     *
//     * @param data     non-null array of object
//     * @param PropName - name of a property of those objects
//     * @return non-null array of same type and length as data but sorted
//     */
//
//    public static Object[] sortOnProperty(Object[] data, String PropName) {
//
//        Class ArrayClass = data.getClass().getComponentType();
//        ClassProperty TheProp = ClassAnalyzer.getClassProperty(ArrayClass, PropName);
//
//        Method getMethod = TheProp.getGetMethod();
//        Object[] testData = (Object[]) Array.newInstance(getMethod.getReturnType(), data.length);
//        try {
//            for (int i = 0; i < data.length; i++) {
//                Object Added = getMethod.invoke(data[i], EMPTY_OBJECT_ARRAY);
//                testData[i] = Added;
//            }
//        }
//        catch (IllegalAccessException ex) {
//            throw new IllegalArgumentException("IllegalAccessException getter for " + PropName + " not found");
//        }
//        catch (InvocationTargetException ex) {
//            throw new IllegalArgumentException("InvokationTargetException getter for " + PropName + " not found");
//        }
//        //noinspection UnusedAssignment
//        int[] order = getSortOrder(testData);
//
//        //noinspection SimplifiableIfStatement,ConstantIfStatement
//        if (true)
//            throw new UnsupportedOperationException("Fix This"); // todo
//        Object[] ret = null; // Clone.clone(data);
//        for (int i = 0; i < order.length; i++)
//            ret[i] = data[order[i]];
//        return (ret);
//    }

    /**
     * create the union of al types of T
     *
     * @param item0 original collection
     * @param items collection of similar elements
     * @return
     */
    public static <K, T> void insertIntoArrayMap(K key, T item0, Map<K, T[]> map) {
        T[] current = map.get(key);
        T[] next;
        if (current == null) {
            next = (T[]) Array.newInstance(item0.getClass(), 1);
            next[0] = item0;
        }
        else {
            next = (T[]) Array.newInstance(item0.getClass(), current.length + 1);
            System.arraycopy(current, 0, next, 0, current.length);
            next[current.length] = item0;
        }
        map.put(key, next);
    }


    /**
     * create the union of al types of T
     *
     * @param item0 original collection
     * @param items collection of similar elements
     * @return
     */
    public static <T> T[] commonElements(T[] item0, T[]... items) {
        Set<T> holder = new HashSet<T>(Arrays.asList(item0));
        for (T[] item : items) {
            List more = Arrays.asList(item);
            holder.retainAll(more);
        }
        T[] ret = (T[]) Array.newInstance(item0.getClass().getComponentType(), holder.size());
        holder.toArray(ret);
        return ret;
    }


    /**
     * { method
     *
     * @param base  - non-null - the test returns strings in base not in probe
     * @param probe non-null probe array
     * @return true if equal
     *         }
     * @name differenceStringArrays
     * @function the test returns strings in base not in probe
     */
    public static String[] differenceStringArrays(String[] base, String[] probe) {
        String[] BaseClone = uniqueSort(base);
        String[] ProbeClone = uniqueSort(probe);
        Vector holder = new Vector();
        for (int probeIndex = 0, baseIndex = 0; probeIndex < ProbeClone.length; probeIndex++) {
            boolean TerminateSearch = false;
            while (!TerminateSearch) {
                int result = ProbeClone[probeIndex].compareTo(BaseClone[baseIndex]);
                if (result == 0) {
                    TerminateSearch = true; // break
                    baseIndex++;
                }
                else {
                    if (result > 0) {
                        baseIndex++;
                        if (baseIndex >= BaseClone.length) {
                            while (probeIndex < ProbeClone.length)
                                holder.addElement(ProbeClone[probeIndex++]); // not present
                            break;
                        }
                    }
                    else {
                        TerminateSearch = true; // break
                        holder.addElement(ProbeClone[probeIndex]); // not present
                    }
                }
            } // while(!TerminateSearch)
        }

        String[] ret = new String[holder.size()];
        holder.copyInto(ret);
        return (ret);
    }

    public static Object[] vectorToClassArray(Vector holder) {
        return (vectorToClassArray(holder, minimumClassInVector(holder)));
    }

    public static Object[] vectorToClassArray(Vector holder, Class ArrayClass) {
        //noinspection SimplifiableIfStatement,ConstantIfStatement
        if (true)
            throw new UnsupportedOperationException("Fix This"); // todo
        return (null);
    }

    /**
     * { method
     *
     * @param holder non-null vector to test
     * @return non-null class which all members are assignable from
     *         }
     * @name minimumClassInVector
     * @function find the class which all members are assignable from
     */
    public static Class minimumClassInVector(Vector holder) {
        if (holder.size() == 0)
            return (java.lang.Object.class);
        Class ret = holder.elementAt(0).getClass();
        for (int i = 1; i < holder.size(); i++)
            ret = minimumCommonClass(ret, holder.elementAt(i));
        return (ret);
    }

    /**
     * { method
     *
     * @param base non-null class to test
     * @param test non-null object to test
     * @return non-null class which test is assignable from
     *         }
     * @name minimumCommonClass
     * @function find the lowest level class in common with test and base
     */
    public static Class minimumCommonClass(Class base, Object test) {
        if (base.isInstance(test))
            return (base);
        base = base.getSuperclass();
        if (base == null)
            return (java.lang.Object.class);
        return (minimumCommonClass(base, test));
    }

    /**
     * { method
     *
     * @param data array to test - null or 0 length OK
     * @param test string to test - null OK
     * @return true if a member
     *         }
     * @name memberStringArray
     * @function test whether a string is a member of a string array
     */
    public static boolean memberStringArray(String[] data, String test) {
        return (positionStringArray(data, test) >= 0);
    }

    /**
     * { method
     *
     * @param data array to test - null or 0 length OK
     * @param test string to test - null OK
     * @return 0 or more if a member - -1 if not
     *         }
     * @name positionStringArray
     * @function test whether a string is a member of a string array and
     * return its position
     */
    public static int positionStringArray(String[] data, String test) {
        if (data == null || data.length == 0) {
            return (-1);
            // not found - no data
        }
        return (positionStringArray(data, test, 0, data.length));
        // search whole array
    }

    /**
     * { method
     *
     * @param data  array to test - null or 0 length OK
     * @param test  string to test - null OK
     * @param start <Add Comment Here>
     * @param end   <Add Comment Here>
     * @return 0 or more if a member - -1 if not
     *         }
     * @name positionStringArray
     * @function test whether a string is a member of a string array and
     * return its position
     */
    public static int positionStringArray(String[] data, String test, int start, int end) {
        if (data == null || data.length == 0) {
            return (-1);
            // not found
        }
        if (test == null) {
            return (-1);
            // not found
        }
        for (int i = start; i < end; i++) {
            if (data[i] == null || data[i].equals(test)) {
                return (i);
            }
            // match
        }
        return (-1);
        // not found
    }

    /**
     * { method
     *
     * @param src     source buffer
     * @param dst     destination buffer
     * @param size    number bytes to copy
     * @param Current holds start byte at end holds last copied + 1
     *                }
     * @name readBytes
     * @function copy bytes from one String buffer into another at the end
     */
    public static void readBytes(StringBuilder src, StringBuilder dst, int size, IntegerRef Current) {
        dst.setLength(0);
        // clear dst
        for (int i = 0; i < size; i++) {
            dst.append(src.charAt(Current.value++));
        }
    }

    /**
     * { method
     *
     * @param src     source buffer
     * @param dst     destination buffer
     * @param size    number bytes to copy
     * @param Current holds start byte at end holds last copied + 1
     *                }
     * @name readBytes
     * @function copy bytes from one String into a StringBuilder at the end
     */
    public static void readBytes(String src, StringBuilder dst, int size, IntegerRef Current) {
        dst.setLength(0);
        // clear dst
        for (int i = 0; i < size; i++) {
            dst.append(src.charAt(Current.value++));
        }
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return the string extracted
     *         }
     * @name readString
     * @function copy bytes from one StringBuilder a String - triming the result
     */
    public static String readString(StringBuilder s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        return (dst.toString().trim());
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return the string extracted
     *         }
     * @name readStringNoTrim
     * @function copy bytes from one StringBuilder into a String - not triming the result
     */
    public static String readStringNoTrim(StringBuilder s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        return (dst.toString());
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return the int extracted
     *         }
     * @name readInt
     * @function copy bytes from one StringBuilder into an int - the buffer is assumed to hold digits
     */
    public static int readInt(StringBuilder s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        String temp = dst.toString();
        return (Integer.parseInt(temp));
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return the int extracted
     *         }
     * @name readInt
     * @function copy bytes a String into an int - the buffer is assumed to hold digits
     */
    public static int readInt(String s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        String temp = dst.toString().trim();
        if (temp.length() > 0) {
            return (Integer.parseInt(temp));
        }
        else {
            return (0);
        }
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return the int extracted
     *         }
     * @name readHex
     * @function copy bytes from one StringBuilder into an int - the buffer is assumed to hold hex digits
     */
    public static int readHex(StringBuilder s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        String temp = dst.toString();
        return (Integer.parseInt(temp, 16));
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return the int extracted
     *         }
     * @name readHex
     * @function copy bytes from a String into an int - the buffer is assumed to hold hex digits
     */
    public static int readHex(String s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        String temp = dst.toString().trim();
        if (temp.length() > 0) {
            return (Integer.parseInt(temp, 16));
        }
        else {
            return (0);
        }
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return a StringBuilder holding the extracted characters
     *         }
     * @name readChars
     * @function copy bytes from one StringBuilder into a StringBuilder -
     */
    public static StringBuilder readChars(StringBuilder s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        char c;
        while (Current.value < s.length()) {
            c = s.charAt(Current.value++);
            dst.append(c);
            if (dst.length() >= size) {
                break;
            }
        }
        return (dst);
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return a StringBuilder holding the extracted characters
     *         }
     * @name readChars
     * @function copy bytes from a String into a StringBuilder -
     */
//    public static StringBuilder readChars(String s, int size, IntegerRef Current) {
//        StringBuilder dst = new StringBuilder();
//        char c;
//        while (Current.value < s.length()) {
//            c = s.charAt(Current.value++);
//            dst.append(c);
//            if (dst.length() >= size) {
//                break;
//            }
//        }
//        return (dst);
//    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return a String holding the extracted characters - trimmed
     *         }
     * @name readString
     * @function copy bytes from one StringBuilder into an int - the buffer is assumed to hold hex digits
     */
    public static String readString(String s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        return (dst.toString().trim());
    }

    /**
     * { method
     *
     * @param s       source buffer
     * @param Current holds start byte at end holds last copied + 1
     * @param size    number bytes to copy
     * @return a String holding the extracted characters - not trimmed
     *         }
     * @name readStringNoTrim
     * @function copy bytes from one StringBuilder into an int - the buffer is assumed to hold hex digits
     */
    public static String readStringNoTrim(String s, int size, IntegerRef Current) {
        StringBuilder dst = new StringBuilder();
        readBytes(s, dst, size, Current);
        return (dst.toString());
    }

    public static String[] mergeNoDuplicates(String[] in1, String[] in2) {
        Set holder = new HashSet();
        if (in1 != null) {
            Collections.addAll(holder, in1);
        }
        if (in2 != null) {
            Collections.addAll(holder, in2);
        }
        return (collectionToStringArray(holder));
    }

    /**
     * { method
     *
     * @param data array
     * @param test string to test
     * @return true if test is in the array
     *         }
     * @name memberStringArray
     * @function test if a string is in a string array
     */
    public static boolean memberStringArray(String test, String[] data) {
        if (data == null || test == null) {
            return (false);
        }
        for (int i = 0; i < data.length; i++) {
            if (test.equals(data[i])) {
                return (true);
            }
        }
        return (false);
        // not found
    }


    /**
     * { method
     *
     * @param c the char
     * @return the String
     *         }
     * @name toString
     * @function convert a char to a string
     */
    public static String toString(char c) {
        StringBuilder s = new StringBuilder();
        s.append(c);
        return (s.toString());
    }

    /**
     * { method
     *
     * @param s1 string1 cannot be null
     * @param s2 string2 cannot be null
     * @return the answer
     *         }
     * @name caselessCompare
     * @function Compares String s1 to another specified String s2.
     * Returns an integer that is less than, equal to, or greater than zero.
     * The integer's value depends on whether s1 is less than,
     * equal to, or greater than s2. Unlike String.compareTo this
     * ignores case
     */
    public static int caselessCompare(String s1, String s2) {
        int l = Math.min(s1.length(), s2.length());
        for (int i = 0; i < l; i++) {
            char c1 = Character.toUpperCase(s1.charAt(i));
            char c2 = Character.toUpperCase(s2.charAt(i));
            if (c1 != c2) {
                return (c1 - c2);
            }
        }
        if (s1.length() != s2.length()) {
            return (s1.length() - s2.length());
        }
        // all else fails compare with case
        return (s1.compareTo(s2));
    }

    /**
     * { method
     *
     * @param s the starting string
     * @param n the required size
     * @return resultant string
     *         }
     * @name padStringWithBlanks
     * @function add blanks to a String until it is of length N
     */
    public static String padStringWithLeadingBlanks(String s, int n) {
        int added = n - s.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < added; i++)
            sb.append(' ');
        if (added >= 0)
            sb.append(s);
        else
            sb.append(s.substring(0, n));
        return (sb.toString());
    }

    /**
     * { method
     *
     * @param s the starting string
     * @param n the required size
     * @return resultant string
     *         }
     * @name padStringWithBlanks
     * @function add blanks to a String until it is of length N
     */
    public static String padStringWithBlanks(String s, int n) {
        StringBuilder sb = new StringBuilder(s);
        if (sb.length() > n) {
            sb.setLength(n);
        }
        else {
            while (sb.length() < n)
                sb.append(' ');
        }
        return (sb.toString());
    }

    /**
     * { method
     *
     * @param s the starting string
     * @param n the required size
     * @return resultant string
     *         }
     * @name padStringWithBlanks
     * @function add blanks to a String until it is of length N
     */
    public static String padStringWithZeros(String s, int n) {
        while (s.length() < n)
            s = "0" + s;
        return (s);
    }

    /**
     * { method
     *
     * @param in input string
     * @return string without control characters
     *         }
     * @name filterControlCharacters
     * @function Make a string without control characters
     */
    public static String filterControlCharacters(String in) {
        StringBuilder sb = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c >= ' ' && c < 127) {
                sb.append(c);
            }
            else {
                // tab = 4 spaces - other control chars ignored
                if (c == '\t') {
                    sb.append("   ");
                }
            }
        }
        return (sb.toString());
    }

    /**
     * { method
     *
     * @param del increment
     * @return new date
     *         }
     * @name cloneCalendar
     * @function return a copy oif the input Calendar
     */
    public static Calendar cloneCalendar(Calendar in) {
        GregorianCalendar then = new GregorianCalendar(
                in.get(Calendar.YEAR),
                in.get(Calendar.MONTH),
                in.get(Calendar.DATE),
                in.get(Calendar.HOUR_OF_DAY),
                in.get(Calendar.MINUTE),
                in.get(Calendar.SECOND)
        );
        return (then);
    }

    /**
     * geien the enum return the
     * array of all values
     *
     * @param inp
     * @return
     */
    public static Enum[] getValues(Class inp) {
        if (!inp.isEnum())
            throw new IllegalArgumentException("This method only works for enums");
        try {
            Method method = inp.getMethod("values");
            Enum[] ret = (Enum[]) method.invoke(null);
            return ret;
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e); // should never happen
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);   // should never happen
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);  // should never happen
        }
    }

    /**
     * { method
     *
     * @param del increment
     * @return new date
     *         }
     * @name nowPlus
     * @function return the current date plus del microseconds
     */
    public static GregorianCalendar nowPlus(long del) {
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(new Date(now.getTime().getTime() + del));
        return (now);
    }

    /**
     * { method
     *
     * @return the time
     *         }
     * @name currentTime
     * @function return the current time in millisec
     */
    public static long currentTime() {
        Date now = new Date();
        return (now.getTime());
    }


    public static String currentTimeString() {
        Date now = new Date();
        return (DEFAULT_DATE_TIME_FORMAT.format(now));
    }

    public static String currentDateString() {
        Date now = new Date();
        return (DEFAULT_DATE_FORMAT.format(now));
    }

    /**
     * { method
     *
     * @param data <Add Comment Here>
     * @return <Add Comment Here>
     * @name getReverseSortOrder
     * @function <Add Comment Here>
     * @policy <Add Comment Here>
     * }
     */
    public static int[] getReverseSortOrder(Object[] data) {
        int[] ret = getSortOrder(data);
        reverse(ret);
        return (ret);
    }

    /**
     * { method
     *
     * @param data <Add Comment Here>
     * @return <Add Comment Here>
     * @name getSortOrder
     * @function <Add Comment Here>
     * @policy <Add Comment Here>
     * }
     */
    public static int[] getSortOrder(Object[] data) {
        int nrows = data.length;
        String[] realData = new String[nrows];
        int[] ra = new int[nrows];
        for (int k = 0; k < nrows; k++) {
            ra[k] = k;
            realData[k] = data[k].toString();
        }
        indexedSort(realData, ra, STRING_COMPARE);
        return (ra);
    }

    /**
     * { method
     *
     * @param data       non-null Array to sort
     * @param Comparison non-null comparison function
     * @return non-null array of ints showing new sort order
     * @name getSortOrder
     * @function <Add Comment Here>
     * @policy <Add Comment Here>
     * }
     */
    public static int[] getSortOrder(Object[] data, Comparator Comparison) {
        int nrows = data.length;
        Object[] realData = new Object[nrows];
        int[] ra = new int[nrows];
        for (int k = 0; k < nrows; k++) {
            ra[k] = k;
            realData[k] = data[k];
        }
        indexedSort(realData, ra, Comparison);
        return (ra);

    }

    private static int left_index(int i) {
        return 2 * i + 1;
    }

    private static int right_index(int i) {
        return 2 * i + 2;
    }

    /**
     * Sort array, placing an array of integer indices into the
     * array idx.  If idx is smaller than array, an
     * IndexOutOfBoundsException will be thrown.
     * <p/>
     * array[idx[i]] will be the i'th ranked element of array.
     * e.g.  array[idx[0]] is the smallest element,
     * array[idx[1]] is the next smallest and so on.
     * <p/>
     * array is left_index in its original order.
     */
    public static void indexedSort(final Object[] array, int idx[], Comparator Comparison) {
        int sortsize = array.length;
        int i, top, t, largest, l, r, here;
        int temp;

        if (sortsize <= 1) {
            if (sortsize == 1) idx[0] = 0;
            return;
        }

        top = sortsize - 1;
        t = sortsize / 2;

        for (i = 0; i < sortsize; ++i) idx[i] = i;

        do {
            --t;
            largest = t;

            /* heapify */

            do {
                i = largest;
                l = left_index(largest);
                r = right_index(largest);

                if (l <= top) {
                    if (Comparison.compare(array[idx[l]], array[idx[i]]) > 0) largest = l;
                }
                if (r <= top) {
                    if (Comparison.compare(array[idx[r]], array[idx[largest]]) > 0) largest = r;
                }
                if (largest != i) {
                    temp = idx[largest];
                    idx[largest] = idx[i];
                    idx[i] = temp;
                }
            } while (largest != i);
        } while (t > 0);

        t = sortsize;

        do {
            --top;
            --t;

            here = t;

            temp = idx[here];
            idx[here] = idx[0];
            idx[0] = temp;

            largest = 0;

            do {
                i = largest;
                l = left_index(largest);
                r = right_index(largest);

                if (l <= top) {
                    if (Comparison.compare(array[idx[l]], array[idx[i]]) > 0) largest = l;
                }
                if (r <= top) {
                    if (Comparison.compare(array[idx[r]], array[idx[largest]]) > 0) largest = r;
                }
                if (largest != i) {
                    temp = idx[largest];
                    idx[largest] = idx[i];
                    idx[i] = temp;
                }
            } while (largest != i);
        } while (t > 1);
    }

    /**
     * { method
     *
     * @param s strings to be alphabetized
     *          }
     * @name sort
     * @function - alphabetize an array of strings in place
     * using heapsort
     */
    public static void sort(String[] s) {
        if (s == null || s.length < 2) {
            return;
        }
        int l, j, ir, i;
        String rra;
        String[] ra = new String[s.length + 1];
        System.arraycopy(s, 0, ra, 1, s.length);
        // now run the heap	sort
        l = (s.length >> 1) + 1;
        ir = s.length;
        for (; ; ) {
            if (l > 1) {
                rra = ra[--l];
            }
            else {
                rra = ra[ir];
                ra[ir] = ra[1];
                if (--ir == 1) {
                    ra[1] = rra;
                    break;
                }
            }
            i = l;
            j = l << 1;
            while (j <= ir) {
                if (j < ir && ra[j].compareTo(ra[j + 1]) < 0) {
                    ++j;
                }
                if (rra.compareTo(ra[j]) < 0) {
                    ra[i] = ra[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }
            ra[i] = rra;
        }
        System.arraycopy(ra, 1, s, 0, s.length);
    }

    /**
     * { method
     *
     * @param s strings to be alphabetized
     *          }
     * @name objectSort
     * @function - alphabetize an array of strings in place
     * using heapsort
     */
    public static void objectSort(Object[] s) {
        if (s == null || s.length < 2) {
            return;
        }
        int l, j, ir, i;
        Object rra;
        Object[] ra = new Object[s.length + 1];
        System.arraycopy(s, 0, ra, 1, s.length);
        // now run the heap	sort
        l = (s.length >> 1) + 1;
        ir = s.length;
        for (; ; ) {
            if (l > 1) {
                rra = ra[--l];
            }
            else {
                rra = ra[ir];
                ra[ir] = ra[1];
                if (--ir == 1) {
                    ra[1] = rra;
                    break;
                }
            }
            i = l;
            j = l << 1;
            while (j <= ir) {
                if (j < ir && ra[j].toString().compareTo(ra[j + 1].toString()) < 0) {
                    ++j;
                }
                if (rra.toString().compareTo(ra[j].toString()) < 0) {
                    ra[i] = ra[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }
            ra[i] = rra;
        }
        System.arraycopy(ra, 1, s, 0, s.length);
    }

    /**
     * { method
     *
     * @param s objects to be sorted
     *          }
     * @name descendingSort
     * @function - sort a collection of comparable objects
     */
    public static void descendingSort(Comparable[] s) {
        ascendingSort(s);
        reverse(s);
    }

    /**
     * { method
     *
     * @param s objects to be sorted
     *          }
     * @name ascendingSort
     * @function - sort a collection of comparable objects
     */
    public static void ascendingSort(Comparable[] s) {
        if (s == null || s.length < 2) {
            return;
        }
        int l, j, ir, i;
        Comparable rra;
        Comparable[] ra = new Comparable[s.length + 1];
        System.arraycopy(s, 0, ra, 1, s.length);
        // now run the heap	sort
        l = (s.length >> 1) + 1;
        ir = s.length;
        for (; ; ) {
            if (l > 1) {
                rra = ra[--l];
            }
            else {
                rra = ra[ir];
                ra[ir] = ra[1];
                if (--ir == 1) {
                    ra[1] = rra;
                    break;
                }
            }
            i = l;
            j = l << 1;
            while (j <= ir) {
                if (j < ir && ra[j].compareTo(ra[j + 1]) < 0) {
                    ++j;
                }
                if (rra.compareTo(ra[j]) < 0) {
                    ra[i] = ra[j];
                    j += (i = j);
                }
                else {
                    j = ir + 1;
                }
            }
            ra[i] = rra;
        }
        System.arraycopy(ra, 1, s, 0, s.length);
    }

    /**
     * { method
     *
     * @param s strings to be alphabetized
     *          }
     * @name objectReverseSort
     * @function - reverse alphabetize an array of strings in place
     * using heapsort
     */
    public static void objectReverseSort(Object[] s) {
        objectSort(s);
        reverse(s);
    }

    /**
     * { method
     *
     * @param n - number blanks
     * @param c - fil character
     * @return the string
     *         }
     * @name stringOfChar
     * @function return a string containing n copies of the character c
     */
    public static String stringOfChar(int n, char c) {
        StringBuilder ret = new StringBuilder(n);
        for (int i = 0; i < n; i++)
            ret.append(c);
        return (ret.toString());
    }

    /**
     * { method
     *
     * @param n - number blanks
     * @return <Add Comment Here>
     *         }
     * @name stringOfBlanks
     * @function return a string containing n blanks - hopfully this
     * is efficient - especially for small n
     */
    public static String stringOfBlanks(int n) {
        if (n <= 0) {
            return ("");
        }
        switch (n) {
            case 1:
                return (" ");
            case 2:
                return ("  ");
            case 3:
                return ("   ");
            case 4:
                return ("    ");
            case 5:
                return ("     ");
            case 6:
                return ("      ");
            case 7:
                return ("       ");
            case 8:
                return ("        ");
            case 9:
                return ("         ");
            case 10:
                return ("          ");
            case 11:
                return ("           ");
            case 12:
                return ("            ");
            default:
                return ("            " + stringOfBlanks(n - 12));
        }
    }

    /**
     * convert a char to a string
     *
     * @param c non-zero char
     * @return non-null non-empty strung
     */
    public static String charToString(char c) {
        char[] item = {c};
        return (new String(item));
    }

    /**
     * convert an array to displayString
     *
     * @param in non-null array
     * @return non-nullarray of diaplsyStrings
     */
    public static String[] displayStrings(String[] in) {
        String[] ret = new String[in.length];
        for (int i = 0; i < in.length; i++) {
            ret[i] = displayString(in[i]);

        }
        return ret;
    }

    /**
     * { method
     *
     * @param in - the string
     * @return - display string
     *         }
     * @name displayString
     * @function Break up NerdCapitalized words or _ separated words
     * i.e. MyGoodName -> My Good Name
     * JFK_Chair -> JFK Chair
     */
    public static String displayString(String in) {
        int nchars = in.length();
        boolean PrevIsCapitalized = false;
        boolean PrevIsSpace = false;
        StringBuilder s = new StringBuilder(nchars);
        for (int i = 0; i < nchars; i++) {
            char c = in.charAt(i);
            if (c == '_') {
                if (i != 0) {
                    // drop leading _
                    s.append(' ');
                    // convert _ to space
                    PrevIsSpace = true;
                }
            }
            else {
                // always capitalize the first char
                if (i == 0) {
                    s.append(Character.toUpperCase(c));
                }
                // any capitali
                else {
                    // for new upper - NerdCapitalization add a space
                    if (Character.isUpperCase(c) && !PrevIsCapitalized && !PrevIsSpace) {
                        s.append(' ');
                    }
                    s.append(c);
                }
                PrevIsSpace = false;
            }
            // remember if last is upper case so we do not
            // split acronyms such as LCJ
            PrevIsCapitalized = Character.isUpperCase(c);
        }
        return (s.toString());
    }

    /**
     * { method
     *
     * @param in - the string
     * @return - display string
     *         }
     * @name displayString
     * @function Break up NerdCapitalized words or _ separated words
     * i.e. MyGoodName -> MY_GOOD_NAME
     * JFK_Chair -> JFK_CHAIR
     */
    public static String fromDisplayString(String in) {
        int nchars = in.length();
        boolean PrevIsCapitalized = false;
        boolean PrevIsSpace = false;
        boolean IsCapitalized;
        StringBuilder s = new StringBuilder(nchars);
        for (int i = 0; i < nchars; i++) {
            char c = in.charAt(i);
            IsCapitalized = Character.isUpperCase(c);

            if (IsCapitalized && !PrevIsCapitalized) {
                if (i != 0) {
                    // add leading _
                    s.append('_');
                    // remember to space
                    //noinspection UnusedAssignment
                    PrevIsSpace = true;
                }
                s.append(Character.toUpperCase(c));
            }
            else {
                // always capitalize the first char
                s.append(Character.toUpperCase(c));
                //noinspection UnusedAssignment
                PrevIsSpace = false;
            }
            // remember if last is upper case so we do not
            // split acronyms such as LCJ
            PrevIsCapitalized = IsCapitalized;
        }
        return (s.toString());
    }

    /**
     * { method
     *
     * @param s - the string
     * @return - true if the string is null or 0 length
     *         }
     * @name isEmptyString
     * @function test if string null or 0 length
     */
    public static boolean isEmptyString(String s) {
        return (s == null || s.length() == 0);
    }

    /**
     * returnn the text in s before the first occurrence of marker
     *
     * @param s      non-null string holding marker at position > 0
     * @param marker non-null string
     * @return non-null string with all all of S before marker
     * @throws IllegalArgumentException on error
     */
    public static String getTextBefore(String s, String marker) {
        int index = s.indexOf(marker);
        if (index < 1)
            throw new IllegalArgumentException("Text \'" + marker + "\' not found in string \'" + s + "\'");
        return (s.substring(0, index));
    }

    /**
     * { method
     *
     * @param s - the string
     * @return - true as above
     *         }
     * @name isAllUpperCase
     * @function test if string is all upper case - empty returns false
     */
    public static boolean isAllUpperCase(String s) {
        if (isEmptyString(s)) {
            return (false);
        }
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) {
                return (false);
            }
        }
        return (true);
    }

    /**
     * { method
     *
     * @param in - the string
     * @return - display string
     *         }
     * @name toNerdCapsString
     * @function Break up NerdCapitalized words or _ separated words
     * i.e. MyGoodName -> My Good Name
     * FOO_BAR -> FooBar
     */
    public static String toNerdCapsString(String in) {
        int nchars = in.length();
        if (nchars == 0)
            return ("");
        boolean PrevIsCapitalized = false;
        boolean PrevIsSpace = false;
        StringBuilder sb = new StringBuilder(nchars);
        char c = 0;
        int i = 0;
        for (; i < nchars; i++) {
            c = in.charAt(i);
            if (Character.isLetter(c))
                break;
        }
        sb.append(Character.toUpperCase(c));
        i++;
        for (; i < nchars; i++) {
            c = in.charAt(i);
            if (c == '_') {
                if (i >= (nchars - 1))
                    break; // ignore trailing
                i++;
                c = in.charAt(i);
                sb.append(Character.toUpperCase(c));
            }
            else {
                if (Character.isLetterOrDigit(c)) {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return (sb.toString());
    }

    /**
     * { method
     *
     * @param s - the string
     * @return - true as above
     *         }
     * @name isAllLowerCase
     * @function test if string is all lower case - empty returns false
     */
    public static boolean isAllLowerCase(String s) {
        if (isEmptyString(s)) {
            return (false);
        }
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                return (false);
            }
        }
        return (true);
    }

    /**
     * { method
     *
     * @param in     - the string to drop ending
     * @param ending - ending to drop
     * @return - string without the endinf
     *         }
     * @name stripEnding
     * @function drop an end fron a string i.e. foo.class -> foo
     */
    public static String stripEnding(String in, String ending) {
        if (in.endsWith(ending))
            return (in.substring(0, in.length() - ending.length()));
        return (in); // does not have ending
    }

    /**
     * { method
     *
     * @param s - the string
     * @return - true as above
     *         }
     * @name isStringNumber
     * @function test if string is a number can be -123.56
     */
    public static boolean isStringNumber(String s) {
        boolean hasDecimalPoint = false;
        if (isEmptyString(s)) {
            return (false);
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '-':
                    // allow leading -
                    if (i > 0) {
                        return (false);
                    }
                    break;
                case '.':
                    // allow 1 decimal point
                    if (!hasDecimalPoint) {
                        hasDecimalPoint = true;
                    }
                    else {
                        return (false);
                    }
                    break;
                case '0':
                    // allow all digits
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    return (false);
            }
        }
        return (true);
        // nothing but digits and
    }

    /**
     * { method
     *
     * @param i <Add Comment Here>
     * @return - the string
     *         }
     * @name makeCharString
     * @function Make a string with a single character
     * @UnusedParam> - i the character
     */
    public static String makeCharString(char i) {
        StringBuilder s = new StringBuilder(2);
        s.append(i);
        return (s.toString());
    }

    /**
     * { method
     *
     * @param ch <Add Comment Here>
     * @return - true if so
     *         }
     * @name isPrintingChar
     * @function is a character printable
     * @UnusedParam> - ch the character
     */
    public static boolean isPrintingChar(char ch) {
        return (ch >= ' ' && ch <= 126);
    }

    /**
     * { method
     *
     * @param rows <Add Comment Here>
     * @param cols <Add Comment Here>
     * @return - the array
     *         }
     * @name makeStringXYArray
     * @function make a 2 dimensional array of rows and columns - all strings
     * are created and set to "".
     * @UnusedParam> - rows - number or rows
     * @UnusedParam> - cols - number or columns
     */
    public static String[][] makeStringXYArray(int rows, int cols) {
        String[][] ret = new String[rows][];
        for (int i = 0; i < rows; i++) {
            ret[i] = new String[cols];
            for (int j = 0; j < cols; j++) {
                ret[i][j] = "";
            }
        }
        return (ret);
    }

    /**
     * { method
     *
     * @param s input string must be non-null
     * @return a String holding the extracted characters - trimmed may be empty
     *         }
     * @name trimTrailingBlanks
     * @function drop blank chars from the end but not the beginning of a string
     * also filters non-printing characters
     */
    public static String trimTrailingBlanks(String s) {
        int l = s.length() - 1;
        while (l >= 0) {
            if (s.charAt(l) != ' ') {
                break;
            }
            l--;
        }
        if (l < 0) {
            return ("");
        }
        StringBuilder sb = new StringBuilder(l + 1);
        // now accumulate filtering out non-printing characters
        for (int i = 0; i <= l; i++) {
            char c = s.charAt(i);
            if (c >= ' ' && c < 127) {
                sb.append(c);
            }
        }
        return (sb.toString());
    }

    /**
     * method
     * drop white chars from the end but not the beginning of a string
     * also filters non-printing characters
     *
     * @param s input string must be non-null
     * @return a String holding the extracted characters - trimmed may be empty
     */
    public static String trimWhiteSpace(String s) {
        int l = s.length();
        int start = 0;
        int end = l;
        for (; start < l; start++) {
            if (!Character.isWhitespace(s.charAt(start)))
                break;
        }
        for (; end > start; end--) {
            if (!Character.isWhitespace(s.charAt(end - 1)))
                break;
        }
        return (s.substring(start, end));
    }

    /**
     * { method
     *
     * @param s input string may be null
     * @return a String holding the extracted characters - trimmed may be empty
     *         }
     * @name parseInt
     * @function just like Integer.parseInt but will quietly return 0
     * is the string is empty
     */
    public static int parseInt(String s) {
        if (s == null) {
            return (0);
        }
        if (s.length() == 0) {
            return (0);
        }
        return (Integer.parseInt(s));
    }


    /**
     *
     */
    public static <T> T[] invert(T[] data) {
        Class cls = data.getClass().getComponentType();
        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(cls, data.length);
        for (int i = 0; i < data.length; i++) {
            ret[i] = data[data.length - i - 1];

        }
        return ret;
    }

    /**
     * @param data non-null
     * @name reverse
     * @function reverse array in place
     */
    public static void reverse(Object[] data) {
        int n = data.length / 2;
        int src = data.length - 1;
        for (int i = 0; i < n; i++) {
            Object temp = data[i];
            data[i] = data[src];
            data[src] = temp;
            src--;
        }
    }

    /**
     * { method
     *
     * @param data the array
     *             }
     * @name reverse
     * @function - reverse the order of an array in place
     */
    public static void reverse(int[] data) {
        int n = data.length / 2;
        int src = data.length - 1;
        for (int i = 0; i < n; i++) {
            int temp = data[i];
            data[i] = data[src];
            data[src] = temp;
            src--;
        }
    }

    /**
     * { method
     *
     * @param data the array
     *             }
     * @name reverse
     * @function - reverse the order of an array in place
     */
    public static void reverse(double[] data) {
        int n = data.length / 2;
        int src = data.length - 1;
        for (int i = 0; i < n; i++) {
            double temp = data[i];
            data[i] = data[src];
            data[src] = temp;
            src--;
        }
    }

    /**
     * { method
     *
     * @param data the array
     *             }
     * @name reverse
     * @function - reverse the order of an array in place
     */
    public static void reverse(char[] data) {
        int n = data.length / 2;
        int src = data.length - 1;
        for (int i = 0; i < n; i++) {
            char temp = data[i];
            data[i] = data[src];
            data[src] = temp;
            src--;
        }
    }

    /**
     * { method
     *
     * @param data the array
     *             }
     * @name reverse
     * @function - reverse the order of an array in place
     */
    public static void reverse(boolean[] data) {
        int n = data.length / 2;
        int src = data.length - 1;
        for (int i = 0; i < n; i++) {
            boolean temp = data[i];
            data[i] = data[src];
            data[src] = temp;
            src--;
        }
    }

    /**
     * { method
     *
     * @param arg1 first array
     * @param arg2 first array
     * @return - concatenated array
     *         }
     * @name cat
     * @function - concatenate two arrays of objects
     */
    public static Object[] cat(Object[] arg1, Object[] arg2) {
        if (arg1 == null) {
            return (arg2);
        }
        if (arg2 == null) {
            return (arg1);
        }
        Object[] ret = new Object[arg1.length + arg2.length];
        System.arraycopy(arg1, 0, ret, 0, arg1.length);
        System.arraycopy(arg2, 0, ret, arg1.length, arg2.length);
        return (ret);
    }

    /**
     * { method
     *
     * @param arg1 first array
     * @param arg2 first array
     * @return - concatenated array
     *         }
     * @name catStringArrays
     * @function - concatenate two arrays of objects
     */
    public static String[] catStringArrays(String[] arg1, String[] arg2) {
        if (arg1 == null) {
            return (arg2);
        }
        if (arg2 == null) {
            return (arg1);
        }
        String[] ret = new String[arg1.length + arg2.length];
        System.arraycopy(arg1, 0, ret, 0, arg1.length);
        System.arraycopy(arg2, 0, ret, arg1.length, arg2.length);
        return (ret);
    }

    /**
     * { method
     *
     * @param testName   - Property name
     * @param properties - list of all properties
     *                   }
     * @name hidePropertyDescriptor
     * @function - used to search a list of properties and set the property
     * named in testName to hidden
     */
    public static void hidePropertyDescriptor(String testName, PropertyDescriptor[] properties) {
        for (int i = 0; i < properties.length; i++) {
            if (testName.equalsIgnoreCase(properties[i].getName())) {
                properties[i].setHidden(true);
                return;
            }
        }
    }

    /**
     * return any text left in text after encountering after
     *
     * @param text  non-null string to search in
     * @param after non-null string to search for
     * @return null if after not found otherwise the text after after - may be empty
     */
    public static String textAfter(String text, String after) {
        int index = text.indexOf(after);
        if (index > -1) {
            int afterLen = after.length();
            int retIndex = index + afterLen;
            if (retIndex < text.length()) {
                return text.substring(retIndex);
            }
        }
        return null;
    }


    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static Object randomKey(Map in) {
        return (randomElement(in.keySet()));
    }

    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static Object randomValue(Map in) {

        Collection<Object> in1 = in.values();
        return (randomElement(in1));
    }


    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static <T> T randomElement(Collection<T> in) {
        int size = in.size();
        if (size == 0)
            return (null);
        int item = 0;
        if (size > 1)
            item = randomInt(size);
        List<T> TheList = new ArrayList<T>(in);
        T ret = TheList.get(item);
        //noinspection UnusedAssignment
        TheList = null;
        return (ret);
    }

    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static <T> T randomElement(T[] in) {
        if (in.length == 1)
            return (in[0]);
        return (in[randomInt(in.length)]);
    }

    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static int randomIndex(Object[] in) {
        if (in.length == 1)
            return (0);
        return (randomInt(in.length));
    }

    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static double randomElement(double[] in) {
        if (in.length == 1)
            return (in[0]);
        return (in[randomInt(in.length)]);
    }

    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static char randomElement(char[] in) {
        if (in.length == 1)
            return (in[0]);
        return (in[randomInt(in.length)]);
    }

    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static int randomElement(int[] in) {
        if (in.length == 1)
            return (in[0]);
        return (in[randomInt(in.length)]);
    }

    /**
     * { method
     * randomProbability returns true with probabiliy given by its input
     *
     * @param probability - probability double >= 0 and <= 1 which is the probability of true
     * @return - true with probability - probability
     *         }
     * @function - choose an element of an array at random
     */
    public static boolean randomProbability(double probability) {
        if (probability > 1 || probability < 0)
            throw new IllegalArgumentException("randomProbability must take a 0 <= number <= 1");
        if (probability < 0)
            throw new IllegalArgumentException("randomProbability must take a number > 0");
        double test = gRandomizer.nextDouble();
        return (test < probability);
    }

    /**
     * choose a random digit
     *
     * @return '0' ...'9'
     */
    public static char randomDigit() {
        return ((char) ('0' + randomInt(10)));
    }


    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomElement
     * @function - choose an element of an array at random
     */
    public static int randomInt(int in) {
        if (in <= 1) {
            if (in == 1)
                return (0);
            throw new IllegalArgumentException("randomInt must take a number > 1");
        }
        int test = gRandomizer.nextInt(in);
        if (test < 0)
            test = -test;
        return (test);
    }

    /**
     * { method
     *
     * @param in - choices - non-null non-empty
     * @return - one choice
     *         }
     * @name randomString
     * @function - choose an element of an array at random
     */
    public static String randomString() {
        return (randomString(8));
    }

    /**
     * { method
     *
     * @param in - positive int
     * @return - the string length 1..256
     *         }
     * @name randomString
     * @function - create a randomString of letters of length n
     */
    public static String randomString(int in) {
        in = Math.max(1, Math.min(in, 256));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in; i++) {
            char c = (char) ('A' + randomInt(26));
            sb.append(c);
        }
        return (sb.toString());
    }

    /**
     * { method
     *
     * @param in - positive int
     * @return - the string length 1..256
     *         }
     * @name randomNumberString
     * @function - create a randomString of letters of length n
     */
    public static String randomNumberString(int in) {
        in = Math.max(1, Math.min(in, 256));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in; i++) {
            char c = (char) Math.min('9', ('0' + randomInt(10)));
            sb.append(c);
        }
        return (sb.toString());
    }

    /**
     * like math.random but will not be the same on multiple runs
     *
     * @return - random number 0..1
     */
    public static double random() {
        return (gRandomizer.nextDouble());
    }

    /**
     * randomGaussian returna variable with a guassian distribution
     *
     * @param mean - distribution mean
     * @param sd   - distribution sd
     * @return - one variable
     */
    public static double randomGaussian(double mean, double sd) {
        double norm = randomNormalGaussian();
        return ((norm * sd) + mean);
    }

    /**
     * randomGaussian returna variable with a guassian distribution
     * Test program to generate a vector of Gaussian deviates using
     * the Box-Muller method and the system-supplied uniform RNG.
     * K.A.Hawick, 15 July 1994.
     * <p/>
     * H W Yau.  23rd of August, 1996.
     * Northeast Parallel Architectures Center.
     * Syracuse University.
     *
     * @return - one variable
     */
    public static double randomNormalGaussian() {
        double r;
        double f;
        double v1;
        double v2;
        do {
            v1 = gRandomizer.nextDouble();
            v2 = gRandomizer.nextDouble();
            v1 = (2.0 * v1) - 1.0;
            v2 = (2.0 * v2) - 1.0;
            r = ((v1 * v1) + (v2 * v2));
            f = Math.sqrt((-2.0 * Math.log(r)) / r);
        } while (r >= 1.0 || r <= 0);

        double ret = v1 * f;
        if (Double.isNaN(ret))
            Util.breakHere();

        return (ret);
    }

    /**
     * { method
     *
     * @param prop - the properties
     * @param name - base name
     * @return - the list
     *         }
     * @name getPopertiesList
     * @function - A list is stored as Properties called
     * <ListName>.0
     * <ListName>.1
     * <ListName>.2 ...
     * This takes listName and returns an array os Strings with
     * 0 or more elements
     */
    public static String[] getPropertiesList(Properties prop, String Name) {
        Vector holder = new Vector();
        int index = 0;
        String testname = Name + "." + index++;
        String test = (String) prop.get(testname);
        while (test != null) {
            holder.addElement(test);
            test = (String) prop.get(Name + "." + index++);
        }
        String[] ret = Util.collectionToStringArray(holder);
        return (ret);
    }

    /**
     * { method
     *
     * @param prop - the properties
     * @param name - base name
     * @param data - array of 0 or more strings
     *             }
     * @name setPopertiesList
     * @function - A list is stored as Properties called
     * <ListName>.0
     * <ListName>.1
     * <ListName>.2 ...
     * This takes listName and returns an array os Strings with
     * 0 or more elements
     */
    public static void setPropertiesList(Properties prop, String Name, String[] data) {
        int index = 0;
        // Wipe out old values
        String TestName = Name + "." + index++;
        String test = (String) prop.get(TestName);
        while (test != null) {
            prop.remove(TestName);
            TestName = Name + "." + index++;
            test = (String) prop.get(TestName);
        }
        for (int i = 0; i < data.length; i++)
            prop.put(Name + "." + i, data[i]);
    }

    /**
     * { method
     *
     * @param in -the list - may be null
     * @return = non-null list of class names
     *         }
     * @name makeClassList
     * @function - convert a list of Objects to a list of
     * clas names
     */
    public static String[] makeClassList(Object[] in) {
        if (in == null)
            return (new String[0]);
        String[] ret = new String[in.length];
        for (int i = 0; i < in.length; i++)
            ret[i] = in[i].getClass().getName();
        return (ret);
    }

    /**
     * { method
     *
     * @param data           - the int
     * @param RequiredPlaces - the required width
     * @return - resultant string
     *         }
     * @name intString
     * @function - return a string representing an int which is
     * RequiredPlaces long (or longer) padding with 0 s
     * Yes format can to this but this function shuld be 1.02
     * conpatable.
     * NOTE - best used for positive data
     */
    public static String intString(int data, int RequiredPlaces) {
        String out = Integer.toString(data);
        while (out.length() < RequiredPlaces)
            out = "0" + out;
        return (out);
    }

    /**
     * { method
     *
     * @param test   - Object to test
     * @param data   - Array of possibilities
     * @param return - true if included
     *               }
     * @name listMember
     * @function - test of the object test is a member of the list data
     */
    public static boolean listMember(Object test, Object[] data) {
        for (int i = 0; i < data.length; i++) {
            if (test == data[i])
                return (true); // member
        }
        return (false); // not found
    }

    /**
     * { method
     *
     * @param in - Non-null string to capitalize
     * @return - non-null capitailzed string
     *         }
     * @name capitalize
     */
    public static String simpleCapitalize(String in) {
        if (in.length() == 0)
            return in;
        char c = in.charAt(0);
        char uc = Character.toUpperCase(c);
        if (c == uc)
            return in;
        if (in.length() == 1)
            return ("" + uc);
        else
            return uc + in.substring(1);
    }


    /**
     * return the text between beforeStart and  afterEnd
     *
     * @param in          !null String
     * @param beforeStart !null string
     * @param afterEnd    !null string
     * @return possibly null string - null says nothing is there
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String textBetween(String in, String beforeStart, String afterEnd) {
        int start = in.indexOf(beforeStart);
        if (start == -1)
            return null;
        start += beforeStart.length();
        if (start > in.length() - afterEnd.length())
            return null;
        int end = in.indexOf(afterEnd, start);
        if (end == -1)
            return null;
        return in.substring(start, end);

    }


    /**
     * { method
     *
     * @param in     - string to capitalize shouldnot have spaces
     * @param return - broken string
     *               }
     * @name nerdCapsToWords
     * @function - convert FieFieFoe to Fie Fie Foe
     * NOTE Seqiences i.e. XML will be concatinated
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String nerdCapsToWords(String in) {
        in = in.trim();
        if (in.length() == 0)
            return ("");
        StringBuilder s = new StringBuilder(in.length() + 10);
        int i = 0;
        char c = in.charAt(i++);
        boolean LastWasCap = true;
        s.append(Character.toUpperCase(c));
        for (; i < in.length(); i++) {
            c = in.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!LastWasCap)
                    s.append(' '); // add space
            }
            else {
                LastWasCap = false;
            }
            s.append(c);
        }
        return (s.toString());
    }

    /**
     * { method
     *
     * @param in     - string to capitalize shouldnot have spaces
     * @param return - broken string
     *               }
     * @name nerdCapsToWords
     * @function - convert  Fie Fie Foe to FieFieFoe
     * NOTE Seqiences i.e. XML will be concatinated
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String wordsToNerdCaps(String in) {
        in = in.trim();
        StringBuilder s = new StringBuilder(in.length() + 10);
        boolean LastWasSpace = false;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (Character.isSpaceChar(c)) {
                LastWasSpace = true;
            }
            else {
                if (LastWasSpace) {
                    s.append(Character.toUpperCase(c));
                }
                else {
                    s.append(c);
                }
            }
        }
        return (s.toString());
    }

    /**
     * return a string with topic names separated by a delimiter -
     * i.e. "," or "\t"
     *
     * @param items     of options if null an empty string is returned
     * @param delimiter non-null separater string
     * @return the string non-null
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String buildDelimitedString(String[] items, String delimiter) {
        StringBuilder ret = new StringBuilder();
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (i > 0)
                    ret.append(delimiter);
                ret.append(items[i]);
            }
        }
        return (ret.toString());
    }

    /**
     * return a string with topic names separated by a delimiter -
     * i.e. "," or "\t"
     *
     * @param items     of options if null an empty string is returned
     * @param delimiter non-null separater string
     * @return the string non-null
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String buildDelimitedString(String[] items, char delimiter) {
        StringBuilder ret = new StringBuilder();
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (i > 0)
                    ret.append(delimiter);
                ret.append(stringWithoutDelimiter(items[i], delimiter));
            }
        }
        return (ret.toString());
    }


    /*
    * return a string with any occurrances of \ and the delimiter escaped with \
    * @param in non-null string
    * @param delimiter non-zero non \ delimiter
    * @return non-null string
    */

    public static String stringWithoutDelimiter(String in, char delimiter) {
        int index = in.indexOf(delimiter);
        if (index == -1)
            return (in);
        int n = in.length();
        StringBuilder sb = new StringBuilder(n + 4);
        for (int i = 0; i < n; i++) {
            char c = in.charAt(i);
            if (c == '\\') {
                sb.append(c);
                sb.append(c);
            }
            else {
                if (c == delimiter) {
                    sb.append('\\');
                }
                sb.append(c);
            }
        }

        return (sb.toString());
    }
    /**
     *     build indent String
     *     @param indent non-negative every level indents 4 spaces
     */

    /**
     * add indent to a StringBuilder
     *
     * @param sb     non-null  StringBuilder
     * @param indent non-negative indent
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void indentStringBuilder(StringBuilder sb, int indent) {
        sb.append(indentString(indent));

    }

    /**
     * add indent to a Appendable
     *
     * @param sb     non-null  StringBuilder
     * @param indent non-negative indent
     */
    public static void indent(Appendable sb, int indent) throws IOException {
        sb.append(indentString(indent));

    }

    /**
     * add indent to a StringBuilder
     *
     * @param sb     non-null  StringBuilder
     * @param indent non-negative indent
     */
    public static void indent(StringBuilder sb, int indent) {
        sb.append(indentString(indent));

    }


    /**
     * build indent String
     *
     * @param indent non-negative every level indents 4 spaces
     * @return string with indented CR
     */
    public static String indentString(int indent) {
        if (gIndentStrings == null) {
            buildIndentStrings();
        }
        if (indent < gIndentStrings.length)
            return (gIndentStrings[indent]);
        // too long so build
        StringBuilder sb = new StringBuilder(4 * indent);
        for (int i = 0; i < indent; i++)
            sb.append("    ");
        return (sb.toString());

    }

    /**
     * build indent String
     *
     * @return string with indented CR
     */
    protected static synchronized void buildIndentStrings() {
        if (gIndentStrings != null) {
            return;
        }
        String[] TheStrings = new String[16];
        StringBuilder IndentString = new StringBuilder();
        for (int i = 0; i < TheStrings.length; i++) {
            TheStrings[i] = IndentString.toString();
            IndentString.append("    ");
        }
        gIndentStrings = TheStrings;
    }

    /**
     * add indent to a String
     *
     * @param out   non-null PrintStream
     * @param level non-negative every level indents 4 spaces
     * @return string with indented CR
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String indentTransform(String in, int indent) {
        StringBuilder sb = new StringBuilder(in.length() + 64 * indent);
        String IndentString = indentString(indent);
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '\n':
                    sb.append(c);
                    if (i < (in.length() - 1))
                        sb.append(IndentString);
                    break;
                case '\t':
                    sb.append("    ");
                    break;
                default:
                    if (c >= ' ')
                        sb.append(c);
            }
        }
        return (sb.toString());
    }

    /**
     * add indent to a stream
     *
     * @param out   non-null PrintStream
     * @param level non-negative every level indents 4 spaces
     */
    public static void indent(PrintStream out, int level) {
        out.print(indentString(level));
    }

    /**
     * add indent to a stream
     *
     * @param out   non-null PrintWriter
     * @param level non-negative every level indents 4 spaces
     */
    public static void indent(PrintWriter out, int level) {
        out.print(indentString(level));
    }

    /**
     * Sleep without worrying about try .. catch - one line sleep
     *
     * @param time - sleep time
     */
    public void doSleep(int time) {
        try {
            Thread.sleep(time);
        }
        catch (InterruptedException ex) {
            throw new RuntimeException();
        }
    }

    /**
     * Sleep without worrying about try .. catch - one line sleep
     *
     * @param time - sleep time
     */
    public static void pause(int time) {
        try {
            Thread.sleep(time);
        }
        catch (InterruptedException ex) {
            throw new RuntimeException();
        }
    }

    /**
     * Sleep forever or until interrupted
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void sleepForever() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException ex) {
                throw new RuntimeException();
            }
        }
    }

    /**
     * helper function to be used in implementing Comparable
     *
     * @param t1 number to compare
     * @param t2 number to compare
     * @return 0 if t1 == t2 1 if t1 > t2 else -1
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int makeComparison(int t1, int t2) {
        if (t1 == t2)
            return (0);
        if (t1 < t2)
            return (1);
        else
            return (-1);
    }

    /**
     * helper function to be used in implementing Comparable
     *
     * @param t1 number to compare
     * @param t2 number to compare
     * @return 0 if t1 == t2 1 if t1 > t2 else -1
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int makeComparison(double t1, double t2) {
        if (t1 == t2)
            return (0);
        if (t1 < t2)
            return (1);
        else
            return (-1);
    }

    /**
     * helper function to be used in implementing Comparable
     *
     * @param t1 number to compare
     * @param t2 number to compare
     * @return 0 if t1 == t2 1 if t1 > t2 else -1
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int makeComparison(long t1, long t2) {
        if (t1 == t2)
            return (0);
        if (t1 < t2)
            return (1);
        else
            return (-1);
    }

    /**
     * Utility method to test equality on two possibly null objects
     *
     * @param in    non-null string
     * @param token token to break - if null use \n
     * @return non-null
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String[] parseString(String in, String token) {
        if (token == null)
            token = "\n";
        StringTokenizer st = new StringTokenizer(in, token);
        String[] ret = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
            ret[i++] = st.nextToken();
        return (ret);
    }

    /**
     * Utility method to test equality on two possibly null objects
     *
     * @param t1 - possibly null test object
     * @param t2 - possibly null test object
     * @return true if t1 == t2 == null || t1.equals(t2)
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean objectEqual(Object t1, Object t2) {
        if (t1 == null) {
            return (t2 == null);
        }

        if (t2 == null) {
            return (false);
        }

        return (t1.equals(t2));
    }

    /**
     * Utility method to return the hashCode of a possibly null object
     *
     * @param t1 - possibly null test object
     * @return hash code or 0 if t1 == null
     */
    @SuppressWarnings("UnusedDeclaration")
    public static int objectHash(Object t1) {
        if (t1 == null) {
            return (0);
        }

        return (t1.hashCode());
    }

    /**
     * round in up to the next even multiple of del
     *
     * @param in  initial value
     * @param del interval
     * @return next multiple of del >= in
     */
    public static double roundUpTo(double in, double del) {
        int n = (int) (in / del);
        double ret = n * del;
        if (ret == in)
            return (ret);
        return (ret + del);
    }

    /**
     * round in up to the next even multiple of del
     *
     * @param in  initial value
     * @param del interval
     * @return next multiple of del >= in
     */
    public static int roundUpTo(int in, int del) {
        int n = (in / del);
        int ret = n * del;
        if (ret == in)
            return (ret);
        return (ret + del);
    }

    /**
     * remove leading and trailing quotes
     *
     * @param in npn-null string
     * @return new string
     */
    public static String dropQuotes(String in) {
        String ret = in.trim();
        if (ret.length() < 2)
            return (ret);
        char start = ret.charAt(0);
        char end = ret.charAt(ret.length() - 1);
        if (start != end)
            return (ret);
        if (start == '\'' || start == '\"')
            return (ret.substring(1, ret.length() - 1));
        else
            return (ret);
    }

    /**
     * Make a string into a a multiline]
     * quoted string
     *
     * @return new string
     */

    public static String multilineQuotedString(String in, int indent) {
        if (in.length() == 0)
            return ("\"\"");

        StringTokenizer st = new StringTokenizer(in, "\n");
        boolean NotFirstLine = false;
        int tokenCount = st.countTokens();
        if (tokenCount == 1)
            return ("\"" + in + "\"");
        int index = 1;
        StringBuilder holder = new StringBuilder(in.length() + 10);
        while (st.hasMoreTokens()) {
            if (NotFirstLine) {
                holder.append("\n");
                for (int i = 0; i < indent; i++)
                    holder.append("    ");
            }
            else {
                NotFirstLine = true;
            }
            holder.append("\"");
            String added = filterNonPrinting(st.nextToken());
            holder.append(added);
            holder.append("\\n\"");
            if (index++ < tokenCount)
                holder.append(" + ");
        }
        return (holder.toString());
    }

    /**
     * remove nonprinting characters from a string
     *
     * @param in non-null input string
     * @return non-null output string
     */
    public static String filterNonPrinting(String in) {
        int n = in.length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = in.charAt(i);
            if (Character.isISOControl(c))
                continue;
            sb.append(c);
        }
        return (sb.toString());
    }

    /**
     * remove non numeric characters
     *
     * @param in non-null input string
     * @return non-null output string
     */
    public static String filterNonNumber(String in) {
        int n = in.length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = in.charAt(i);
            if (!Character.isDigit(c))
                continue;
            sb.append(c);
        }
        return (sb.toString());
    }

    /**
     * remove nonprinting characters from a string
     *
     * @param in non-null input string
     * @return non-null output string
     */
    public static String filterNonAlpha(String in) {
        int n = in.length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = in.charAt(i);
            if (!Character.isLetterOrDigit(c))
                continue;
            sb.append(c);
        }
        return (sb.toString());
    }

    /**
     * remove nonprinting characters from a string
     *
     * @param in non-null input string
     * @return non-null output string
     */
    public static String filterPunctuation(String in) {
        int n = in.length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = in.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_')
                continue;
            sb.append(c);
        }
        return (sb.toString());
    }

    /**
     * print the lines in CodeString with
     * indentation given of indent
     *
     * @out non-null writer
     * @CodeString non-null possibly multi=line string
     * @indent non-negative indent value
     */
    public static void printIndentedCode(PrintWriter out, String CodeString, int indent) {
        StringTokenizer st = new StringTokenizer(CodeString, "\n");
        while (st.hasMoreTokens()) {
            Util.indent(out, indent);
            out.println(st.nextToken());
        }
    }

    /**
     * Makes an iterator over an array
     *
     * @param in - non-null array of Objects
     * @return - non-null iterator over the array elements
     */
    public static Iterator arrayIterator(Object[] array) {
        List TheList = Arrays.asList(array);
        return (TheList.iterator());
    }

    /**
     * makes a copy of any array returning the same type
     *
     * @param in - non-null array of unknown type
     * @return - non-null copy array of input type
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static Object duplicateArray(Object in) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        int InSize = Array.getLength(in); // size of input array
        Object ret = Array.newInstance(inClass.getComponentType(), InSize);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(in, 0, ret, 0, InSize);
        //   validateArrayNonNull(ret);
        return (ret);
    }

    /**
     * reomve one object from an array - Yes this is inefficient
     * but usually arrays are accessed much more frequently than they grow
     *
     * @param in         - non-null array of unknown type
     * @param removeFrom - non-null object to removeFrom the array
     * @return - non-null array of input type
     */
    public static Object removeFromArray(Object in, Object removeFrom) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        int InSize = Array.getLength(in); // size of input array
        int index = -1;
        for (int i = 0; i < InSize; i++) {
            if (removeFrom.equals(Array.get(in, i))) {
                index = i;
                break;
            }
        }
        if (index == -1)
            return (in); // not found

        Object ret = Array.newInstance(inClass.getComponentType(), InSize - 1);
        if (index == 0) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(in, 1, ret, 0, InSize - 1);
            //      validateArrayNonNull(ret);
            return (ret);
        }
        if (index == InSize - 1) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(in, 0, ret, 0, InSize - 1);
            //    validateArrayNonNull(ret);
            return (ret);
        }
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(in, 0, ret, 0, index);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(in, index + 1, ret, index, InSize - 1 - index);
        //   validateArrayNonNull(ret);
        return (ret);

    }

    /**
     * return true if all elements of the
     * object as an array
     * are not null
     *
     * @return as above
     */
    public static boolean validateArrayNonNull(Object in) {
        for (int i = 0; i < Array.getLength(in); i++) {
            if (Array.get(in, i) == null) {
                return (false);
            }
        }
        return (true);
    }

    /**
     * adds one object to an array - Yes this is inefficient
     * but usually arrays are accessed much more frequently than they grow
     *
     * @param in    - non-null array of unknown type
     * @param addTo - non-null object to add to the array
     * @return - non-null array of input type
     */
    public static Object[] addToArray2(Object[] in, Object addTo) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        int InSize = Array.getLength(in); // size of input array
        Class componentType = inClass.getComponentType();
        Object[] ret = (Object[]) Array.newInstance(componentType, InSize + 1);
        System.arraycopy(in, 0, ret, 0, InSize);
        Array.set(ret, InSize, addTo);
        //     validateArrayNonNull(ret);
        return (ret);
    }

    /**
     * adds one object to an array - Yes this is inefficient
     * but usually arrays are accessed much more frequently than they grow
     *
     * @param in    - non-null array of unknown type
     * @param addTo - non-null object to add to the array
     * @return - non-null array of input type
     */
    public static <T> T[] addToArray(T[] in, T addTo) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        int InSize = Array.getLength(in); // size of input array
        Class componentType = inClass.getComponentType();
        T[] ret = (T[]) Array.newInstance(componentType, InSize + 1);
        System.arraycopy(in, 0, ret, 0, InSize);
        Array.set(ret, InSize, addTo);
        return ret;
    }

    /**
     * adds one object to an array - Yes this is inefficient
     * but usually arrays are accessed much more frequently than they grow
     *
     * @param in    - non-null array of unknown type
     * @param addTo - non-null object to add to the array
     * @return - non-null array of input type
     */
    public static Object[] addToArrayFront(Object[] in, Object addTo) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        int InSize = Array.getLength(in); // size of input array
        Object[] ret = (Object[]) Array.newInstance(inClass.getComponentType(), InSize + 1);
        System.arraycopy(in, 0, ret, 1, InSize);
        Array.set(ret, 0, addTo);
        //     validateArrayNonNull(ret);
        return (ret);
    }

    /**
     * removes all instances of an object from an array
     *
     * @param in    - non-null array of unknown type
     * @param addTo - non-null object to remove from the array
     * @return - non-null array of input type
     */
    public static Object[] removeFromToArray(Object[] in, Object removeTo) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        List holder = new ArrayList();
        for (int i = 0; i < in.length; i++) {
            Object o = in[i];
            if (!o.equals(removeTo))
                holder.add(o);
        }
        Object[] ret = (Object[]) Array.newInstance(inClass.getComponentType(), holder.size());
        holder.toArray(ret);
        //     validateArrayNonNull(ret);
        return (ret);
    }

    /**
     * adds one int to an array - Yes this is inefficient
     * but usually arrays are accessed much more frequently than they grow
     *
     * @param in    - non-null array of int
     * @param addTo - int add to the array
     * @return - non-null array of int
     */
    public static int[] addToArray(int[] in, int addTo) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        if (inClass.getComponentType() != Integer.TYPE)
            throw new IllegalArgumentException("must pass int array");
        int InSize = in.length; // size of input array
        int[] ret = new int[InSize + 1];
        System.arraycopy(in, 0, ret, 0, InSize);
        ret[in.length] = addTo;
        return (ret);
    }

    /**
     * adds one int to an array - Yes this is inefficient
     * but usually arrays are accessed much more frequently than they grow
     *
     * @param in    - non-null array of double
     * @param addTo - double to add to the array
     * @return - non-null array of double
     */
    public static double[] addToArray(double[] in, double addTo) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        if (inClass.getComponentType() != Double.TYPE)
            throw new IllegalArgumentException("must pass double array");
        int InSize = in.length; // size of input array
        double[] ret = new double[InSize + 1];
        System.arraycopy(in, 0, ret, 0, InSize);
        ret[in.length] = addTo;
        return (ret);
    }

    /**
     * create a copy of an original array
     * which is the same type as the original
     *
     * @param in - non-null array of unknown type
     * @return - non-null array of input type
     */
    public static Object[] copyArray(Object[] in) {
        return copyArraySegment(in, 0, in.length);
    }

    /**
     * create e new array holding a segment of an original array
     * which is the same type as the original
     *
     * @param in    - non-null array of unknown type
     * @param begin start of copy segment
     * @param end   end of segment + 1
     * @return - non-null array of input type
     */
    public static Object[] copyArraySegment(Object[] in, int begin, int end) {
        if (end == 0)
            return (in);
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        int InSize = Array.getLength(in); // size of input array
        int RealEnd = Math.min(end, InSize);
        if (begin < 0)
            throw new IllegalArgumentException("begin must be greater than 0");
        int TargetSize = RealEnd - begin;
        if (begin >= RealEnd)
            throw new IllegalArgumentException("begin less than end");

        Object[] ret = (Object[]) Array.newInstance(inClass.getComponentType(), TargetSize);
        System.arraycopy(in, begin, ret, 0, TargetSize);
        return (ret);
    }

    /**
     * doubles the sice if an input array
     *
     * @param in - non-null array of unknown type
     * @return - non-null array of twice size of in
     */
    public static Object doubleArray(Object in) {
        Class inClass = in.getClass();
        if (!inClass.isArray())
            throw new IllegalArgumentException("must pass array");
        int InSize = Array.getLength(in); // size of input array
        Object ret = Array.newInstance(inClass.getComponentType(),
                2 * InSize);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(in, 0, ret, 0, InSize);
        return (ret);
    }

    /**
     * convert a collection into an array of objects
     *
     * @param in   - non-null collection
     * @param type - non-null class must match all members of the collection
     * @return non-null array of type type
     */
    public static Object[] collectionToArray(Collection in,
                                             Class type) {
        //noinspection CaughtExceptionImmediatelyRethrown,CaughtExceptionImmediatelyRethrown
        try {
            Object[] ret = (Object[]) Array.newInstance(
                    type, in.size());
            in.toArray(ret);
            return (ret);
        }
        catch (ArrayStoreException ex) {
            throw ex;
        }
    }

    protected static PrintWriter gSystemWriter;

    /**
     * convert a properties value into an int
     *
     * @param p   non-null properties
     * @param key non-null id of an existing key
     * @return int value - -1 if the key does not exist
     * @throws NumberFormatException if the value is not a number
     */
    public static int getPropertyInt(Properties p, String key) {
        Object test = p.get(key);
        if (test == null)
            return (-1);
        return (Integer.parseInt(test.toString()));
    }

    /**
     * convert a properties value into an int
     *
     * @param p   non-null properties
     * @param key non-null id of an existing key
     * @return double value - MIN_VALUE if the key does not exist
     * @throws NumberFormatException if the value is not a number
     */
    public static double getPropertyDouble(Properties p, String key) {
        Object test = p.get(key);
        if (test == null)
            return (Double.MIN_VALUE);
        return (Double.parseDouble(test.toString()));
    }

    /**
     * code snippet to load Properties from a resource file
     *
     * @param in   non-null object
     * @param name non-null existing resurce name
     * @return non-null properties
     * @throws OnviaException on error
     */
    public static Properties loadResourceProperties(Object in, String name) {
        return (loadResourceProperties(in.getClass(), name));
    }

    /**
     * code snippet to load Properties from a resource file
     *
     * @param in   non-null class
     * @param name non-null existing resurce name
     * @return non-null properties
     * @throws OnviaException on error
     */
    public static Properties loadResourceProperties(Class in, String name) {
        Properties ret = new Properties();
        try {
            InputStream inStream = in.getResourceAsStream(name);
            ret.load(inStream);
            return (ret);
        }
        catch (IOException ex) {
            //noinspection SimplifiableIfStatement,ConstantIfStatement
            if (true) throw new RuntimeException(ex);
            return (null);
        }
    }

    /**
     * get a printwriter to system.out
     *
     * @return non-null writer to system.out
     */
    public static PrintWriter systemWriter() {
        if (gSystemWriter == null)
            buildSystemWriter();
        return (gSystemWriter);
    }

    public static synchronized void buildSystemWriter() {
        if (gSystemWriter != null)
            return;
        PrintWriter TheWriter = new PrintWriter(System.out);
        gSystemWriter = TheWriter;
    }

    /**
     * Wrapper for System.out.println so I can track
     * where output is coming from
     *
     * @param s non-null string to print
     */
    public static void println(Object s) {
        println(s.toString());
    }

    /**
     * Wrapper for System.out.println so I can track
     * where output is coming from
     *
     * @param s non-null string to print
     */
    public static void println(String s) {
        System.out.println(s);
    }

    /**
     * convert a double into a String with a given precision
     * default double formatting is not very pretty
     *
     * @param in  int to convert
     * @param int positive precision
     * @return non-null formatted string
     */
    public static String formatInt(int in, int precision) {
        String ret = Integer.toString(in);
        if (ret.length() > precision)
            throw new IllegalArgumentException("Cannot write " + in + " in " + precision + " digits");
        while (ret.length() < precision)
            ret = "0" + ret;
        return (ret);
    }

    /**
     * convert a double into a String with a given precision
     * default double formatting is not very pretty
     *
     * @param in  non-null Double to convert
     * @param int positive precision
     * @return non-null formatted string
     */
    public static String formatDouble(Double in, int precision) {
        return (formatDouble(in.doubleValue(), precision));
    }

    /**
     * convert a double into a String with a given precision
     * default double formatting is not very pretty
     *
     * @param in  double to convert
     * @param int positive precision
     * @return non-null formatted string
     */
    public static String formatDouble(double in, int precision) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(precision);
        return (nf.format(in));
    }

    /**
     * convert a double into a String with a given precision
     * default double formatting is not very pretty
     * This version defaults precision to 2
     *
     * @param in non-null Double to convert
     * @param in positive precision
     * @return non-null formatted string
     */
    public static String formatDouble(Double in) {
        return (formatDouble(in.doubleValue()));
    }

    /**
     * convert a double into a String with a given precision
     * default double formatting is not very pretty
     * This version defaults precision to 2
     *
     * @param in double to convert
     * @return non-null formatted string
     */
    public static String formatDouble(double in) {
        return (formatDouble(in, 2));
    }

    /**
     * return a string a time interval in millisec
     *
     * @param in positive interval millisec
     * @returb non-null string i.e. 3:40
     */
    public static String formatTimeInterval(long in) {
        int del = (int) (in + 500) / 1000; // drop millisec
        int seconds = del % 60;
        del /= 60;
        int minute = del % 60;
        del /= 60;
        int hour = del % 24;
        del /= 24;
        int days = del;
        StringBuilder holder = new StringBuilder();

        if (days != 0) {
            holder.append(Integer.toString(days));
            holder.append(":");
            holder.append(d02(hour));
            holder.append(":");
            holder.append(d02(minute));
            holder.append(":");
            holder.append(d02(seconds));
        }
        else {
            if (hour != 0) {
                holder.append(Integer.toString(hour));
                holder.append(":");
                holder.append(d02(minute));
                holder.append(":");
                holder.append(d02(seconds));
            }
            else {
                holder.append(d02(minute));
                holder.append(":");
                holder.append(d02(seconds));
            }
        }
        return (holder.toString());
    }

    /**
     * return a string represtneing the hours and minutes
     * of a Data
     *
     * @param in non-null data
     * @returb non-null string i.e. 3:40
     */
    public String formatMinutes(java.util.Date in) {
        GregorianCalendar TheCalendar = new GregorianCalendar();
        TheCalendar.setTime(in);
        int hour = TheCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = TheCalendar.get(Calendar.MINUTE);
        StringBuilder holder = new StringBuilder();
        holder.append(Integer.toString(hour));
        holder.append(":");
        holder.append(d02(minute));
        return (holder.toString());
    }

    /**
     * convert a 2 digit int to 00 , 02 or 12
     * in integer between 0 and 99
     *
     * @return non-null string
     */
    protected static String d02(int in) {
        if (in == 0) {
            return ("00");
        }
        else {
            if (in < 10) {
                return ("0" + in);
            }
            return (Integer.toString(in));
        }
    }

    /**
     * return a string represtneing the hours and minutes and seconds
     * of a Data
     *
     * @param in non-null data
     * @returb non-null string i.e. 3:40
     */
    public static String formatSeconds(java.util.Date in) {
        GregorianCalendar TheCalendar = new GregorianCalendar();
        TheCalendar.setTime(in);
        int hour = TheCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = TheCalendar.get(Calendar.MINUTE);
        int seconds = TheCalendar.get(Calendar.SECOND);
        StringBuilder holder = new StringBuilder();
        holder.append(Integer.toString(hour));
        holder.append(":");
        holder.append(d02(minute));
        holder.append(":");
        holder.append(d02(seconds));
        return (holder.toString());
    }

    /**
     * prints all strings one per line
     *
     * @param out   non-null writer
     * @param items non-null array of strings
     */
    public static void printItems(PrintWriter out, String[] items) {
        for (int i = 0; i < items.length; i++)
            out.println(items[i]);
    }

    /**
     * prints all strings one per line
     *
     * @param out   non-null writer
     * @param items non-null array of strings
     */
    public static void printItems(PrintStream out, String[] items) {
        for (int i = 0; i < items.length; i++)
            out.println(items[i]);
    }

    /**
     * return true if ignoring white space and non-printing characters
     * the two arguments are equivalent strings
     *
     * @param s1 non-null string
     * @param s2 non-null string
     * @return as above
     */
    public static boolean equivalentForHtml(String s1, String s2) {
        return (stringWithoutHTMLComments(s1).equals(stringWithoutHTMLComments(s2)));
    }

    /**
     * return true if equal or empty
     *
     * @param s1 possibly null string
     * @param s2 possibly null string
     * @return as above
     */
    public static boolean equivalentString(String s1, String s2) {
        if (isEmptyString(s1))
            return isEmptyString(s2);
        return s1.equals(s2);
    }

    /**
     * return true if ignoring white space and non-printing characters
     * the two arguments are equivalent strings
     *
     * @param s1 non-null string
     * @param s2 non-null string
     * @return as above
     */
    public static boolean equivalentIgnoreWhite(String s1, String s2) {
        return (stringWithoutWhite(s1).equals(stringWithoutWhite(s2)));
    }

    /**
     * Make a string dropping all white and non-printing characters
     *
     * @param s1 non-null input string
     * @return non-null string as above
     */
    public static String stringWithoutWhite(String s1) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s1.length(); i++) {
            char c = s1.charAt(i);
            if (c > ' ') {
                sb.append(c);
            }
        }
        return (sb.toString());
    }

    /**
     * Make a string dropping all but letters and numbers
     *
     * @param s1 non-null input string
     * @return non-null string as above
     */
    public static String stringLetterOrDigit(String s1) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s1.length(); i++) {
            char c = s1.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return (sb.toString());
    }

    /**
     * Make a string dropping all white and non-printing characters and HTML Comments
     *
     * @param s1 non-null input string
     * @return non-null string as above
     */
    public static String stringWithoutHTMLComments(String s1) {
        int index = s1.indexOf("<!--");
        String temp = s1;
        while (index > -1) {
            if (index > 0)
                s1 = s1.substring(0, index);
            else
                s1 = "";
            int endIndex = temp.indexOf("-->", index);
            if (endIndex > index) {
                if (endIndex + 3 < temp.length())
                    s1 += temp.substring(endIndex + 3);
            }
            temp = s1;
            index = s1.indexOf("<!--");
        }
        return (stringWithoutWhite(s1));
    }

    /**
     * return true if the character is not part of a word
     * used primarily in replaceWordInString
     *
     * @param c test character
     * @return as above
     */
    public static boolean isWordTerminator(char c) {
        if (Character.isWhitespace(c))
            return (true);
        if (Character.isJavaIdentifierPart(c))
            return (false);
        return (true);
    }

    /**
     * Replace All  instances of ReplaceText starting at start
     * this differs from replaceInString in that the text must be followed by a wordTerminator
     *
     * @param s1          non-null input string
     * @param ReplaceText non-null TextToReplace
     * @param NewText     non-null TextTo Insert
     * @return non-null string as above
     */
    public static String replaceWordInString(String s1, String ReplaceText, String NewText) {
        return (replaceWordInString(s1, ReplaceText, NewText, 0));
    }

    /**
     * Replace All  instances of ReplaceText starting at start
     * this differs from replaceInString in that the text must be followed by a wordTerminator
     *
     * @param s1          non-null input string
     * @param ReplaceText non-null TextToReplace
     * @param NewText     non-null TextTo Insert
     * @return non-null string as above
     */
    public static String replaceWordInString(String s1, String ReplaceText, String NewText, int start) {
        int index = s1.indexOf(ReplaceText, start);
        int ReplaceLength = ReplaceText.length();
        if (index == -1)
            return (s1.substring(start));
        StringBuilder sb = new StringBuilder(s1.length());
        if (s1.length() == index + ReplaceLength || isWordTerminator(s1.charAt(index + ReplaceLength))) { // match is word
            sb.append(s1.substring(start, index));
            sb.append(NewText);
            sb.append(replaceWordInString(s1, ReplaceText, NewText, index + ReplaceLength));
        }
        else { // match is not word
            sb.append(s1.substring(start, index + ReplaceLength));
            sb.append(replaceWordInString(s1, ReplaceText, NewText, index + ReplaceLength));
        }
        return (sb.toString());
    }

    /**
     * Replace All  instances of ReplaceText starting at start
     *
     * @param s1          non-null input string
     * @param ReplaceText non-null TextToReplace
     * @param NewText     non-null TextTo Insert
     * @return non-null string as above
     */
    public static String replaceInString(String s1, String ReplaceText, String NewText) {
        return (replaceInString(s1, ReplaceText, NewText, 0));
    }

    /**
     * Replace All  instances of ReplaceText starting at start
     *
     * @param s1          non-null input string
     * @param ReplaceText non-null TextToReplace
     * @param NewText     non-null TextTo Insert
     * @return non-null string as above
     */
    public static String replaceInString(String s1, String ReplaceText, String NewText, int start) {
        int index = s1.indexOf(ReplaceText, start);
        if (index == -1)
            return (s1.substring(start));
        StringBuilder sb = new StringBuilder(s1.length());
        sb.append(s1.substring(start, index));
        sb.append(NewText);
        sb.append(replaceInString(s1, ReplaceText, NewText, index + ReplaceText.length()));
        return (sb.toString());
    }

    /**
     * Replace first instance of ReplaceText starting at start -
     *
     * @param s1          non-null input string must contain the replace string
     * @param ReplaceText non-null TextToReplace
     * @param NewText     non-null TextTo Insert
     * @return non-null string as above
     * @throws IllegalArgumentException if the substring is not found
     */
    public static String replaceOnceInStringRequired(String s1, String ReplaceText, String NewText) {
        int index = s1.indexOf(ReplaceText);
        if (index == -1) // not found
            throw new IllegalArgumentException("String '" + ReplaceText +
                    "' not found in string '" + s1 + "'");
        return (replaceOnceInString(s1, ReplaceText, NewText));
    }

    /**
     * Replace first instance of ReplaceText starting at start - if replacetext not found
     * return the original string
     *
     * @param s1          non-null input string may contain the replace string
     * @param ReplaceText non-null TextToReplace
     * @param NewText     non-null TextTo Insert
     * @return non-null string as above
     */
    public static String replaceOnceInString(String s1, String ReplaceText, String NewText) {
        int index = s1.indexOf(ReplaceText);
        if (index == -1) // not found
            return (s1);
        StringBuilder sb = new StringBuilder(s1.length());
        if (index > 0)
            sb.append(s1.substring(0, index));
        sb.append(NewText);
        if (index < s1.length() - ReplaceText.length())
            sb.append(s1.substring(index + ReplaceText.length()));
        return (sb.toString());
    }

    /**
     * return the number of times the substring occurs in the text -
     * where there are overlaps the count tells how many times the substring could be replaced
     *
     * @param s1        non-null input string to test
     * @param SubString non-null Substring to cound
     * @return non-negative number of occurrances of the substring
     */
    public static int countSubstringOccurrances(String s1, String SubString) {
        int count = 0;
        int index = s1.indexOf(SubString);
        int sublength = SubString.length();
        int start;
        while (index > -1) {
            count++;
            start = index + sublength;
            if (start > s1.length() - sublength)
                return (count);
            index = s1.indexOf(SubString, start);
        }
        return (count);
    }

    /**
     * return the keys in a Map as a StringArray
     *
     * @return non-null array of keys as strings
     * @in non-null map
     */
    public static String[] getKeyStrings(Map in) {
        String[] ret = new String[in.size()];
        SortedSet keys = new TreeSet(in.keySet());
        Iterator it = keys.iterator();
        int count = 0;
        while (it.hasNext())
            ret[count++] = it.next().toString();
        return (ret);
    }

    /**
     * return the keys in a Dictionary as a StringArray
     * some code still uses this obsolete interface
     *
     * @return non-null array of keys as strings
     * @in non-null dictionary
     */
    public static String[] getDictionaryKeyStrings(Dictionary in) {
        String[] ret = new String[in.size()];
        Enumeration enumVal = in.keys();

        SortedSet keys = new TreeSet();
        while (enumVal.hasMoreElements()) {
            keys.add(enumVal.nextElement().toString());
        }
        Iterator it = keys.iterator();
        int count = 0;
        while (it.hasNext())
            ret[count++] = it.next().toString();
        return (ret);
    }


//    /**
//     * return the values in an array of NameValues
//     *
//     * @return non-null array of NameValues where name is the key valye the value
//     * @in non-null map
//     */
//    public static NameValue[] mapToNameValues(Map in) {
//        String[] keys = getKeyStrings(in);
//        NameValue[] ret = new NameValue[keys.length];
//        for (int i = 0; i < keys.length; i++) {
//            ret[i] = new NameValue(keys[i], in.get(keys[i]));
//        }
//        return (ret);
//    }

//    /**
//     * convert array of NameValues to a Map
//     *
//     * @return non-null Map with names as keys values as values
//     * @in non-null non-null array of NameValues
//     */
//    public static Map nameValuesToMap(NameValue[] in) {
//        Map ret = new HashMap();
//        for (int i = 0; i < in.length; i++) {
//            ret.put(in[i].m_Name, in[i].m_Value);
//        }
//        return (ret);
//    }

    /**
     * return the values in a Map as a StringArray
     *
     * @return non-null array of keys as strings
     * @in non-null map
     */
    public static String[] getValueStrings(Map in) {
        String[] ret = new String[in.size()];
        SortedSet keys = new TreeSet(in.keySet());
        Iterator it = keys.iterator();
        int count = 0;
        while (it.hasNext())
            ret[count++] = it.next().toString();
        return (ret);
    }

    /**
     * builde a well formatted multi-line list of items wrapping every
     * 8 items
     *
     * @param items - non-null array of items to add
     * @return non-null string with all iwtms
     * @parem indent non-negative indent level
     */
    public static String buildListString(Object[] items, int indent) {
        return (buildListString(items, indent, 8));
    }

    /**
     * builde a well formatted multi-line list of items wrapping every
     * 8 items
     *
     * @param items - non-null array of items to add
     * @return non-null string with all iwtms
     * @parem indent non-negative indent level
     * @parem itemsPerLine positive number of items per line
     */
    public static String buildListString(Object[] items, int indent, int itemsPerLine) {
        StringBuilder Holder = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if ((i % itemsPerLine) == 0) {
                Holder.append("\n");
                Holder.append(indentString(indent));
            }
            else {
                Holder.append(", ");
            }
            Holder.append(items[i].toString());
        }

        return (Holder.toString());
    }

    /**
     * Build a stringf excaping C characters
     *
     * @param in non-null string
     * @return string with ",',\ escaped
     */
    public static String buildEscapedString(String in) {
        StringBuilder sb = new StringBuilder(in.length() + 20);
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '\\':
                    if (i == in.length() - 1) {
                        sb.append("\\");
                        break; // at end  of string
                    }
                    char next = in.charAt(++i); // get teh NEXT character
                    switch (next) {   /// le
                        case '\"':
                            sb.append("\\\"");
                            break;
                        case '\'':
                            sb.append("\\\'");
                            break;
                        case '\\':
                            sb.append("\\\\");
                            break;
                        default:
                            sb.append("\\\\"); // escape teh \
                            sb.append(next);
                    }
                    break;
                case '\"':
                case '\'':
                    sb.append('\\');

                default:
                    sb.append(c);
            }
        }
        return (sb.toString());
    }

    /**
     * return the first value in a map - this is most useful where only one item
     * exists in th emap
     *
     * @param in non-null Map with at least one value
     * @return non-null object
     * @throws IllegalArgumentException of the map is empty
     */
    public static Object firstValue(Map in) {
        return (firstElement(in.values()));
    }

    /**
     * return the first element in a collection - this is most useful where only one item
     * exists in the collection
     *
     * @param in non-null collection with at least one value
     * @return non-null object
     * @throws IllegalArgumentException of the collection is empty
     */
    public static Object firstElement(Collection in) {
        if (in.size() == 0)
            throw new IllegalArgumentException("firstElement requies at least one entry");
        return (in.iterator().next());
    }

    /**
     * convert an array of objects to a List
     *
     * @param non-null array
     * @return non-null list
     */
    public static List makeList(Object[] items) {
        return (Arrays.asList(items));
    }

    /**
     * convert an array of objects to a List
     *
     * @param non-null array
     * @return non-null list
     */
    public static Set makeSet(Object[] items) {
        Set ret = new HashSet(Math.max(items.length, 1));
        Collections.addAll(ret, items);
        return (ret);
    }

    /**
     * Make a differnce set - only things in items not in remove
     *
     * @param items  non-null array to remove from
     * @param remove non-null array to remove from items
     * @return non-null array of the same type as items
     */
    public static Set makeDifferenceSet(Object[] items, Object[] remove) {
        Set holder = makeSet(items);
        for (int i = 0; i < remove.length; i++)
            holder.remove(remove[i]);
        return (holder);
    }

    /**
     * Make a differnce Array - only things in items not in remove
     *
     * @param items  non-null array to remove from
     * @param remove non-null array to remove from items
     * @return non-null array of the same type as items
     */
    public static Object[] makeDifferenceArray(Object[] items, Object[] remove) {
        if (items == null)
            return (EMPTY_OBJECT_ARRAY);
        if (remove == null)
            return (items);
        Set holder = makeDifferenceSet(items, remove);
        Object[] ret = (Object[]) Array.newInstance(items.getClass().getComponentType(), holder.size());
        holder.toArray(ret);
        return (ret);
    }

    /**
     * Make an array from the contents of an Enumeration
     *
     * @param enumVal non-null Enumeration
     * @return non-null List holding the elements of enumVal
     */
    public static List enumValToList(Enumeration enumVal) {
        List holder = new ArrayList(256);
        while (enumVal.hasMoreElements())
            holder.add(enumVal.nextElement());
        return (holder);
    }

    /**
     * Make an array from the contents of an Enumeration
     *
     * @param enumVal non-null Enumeration
     * @return non-null array holding the elements of enumVal
     */
    public static Object[] enumValToArray(Enumeration enumVal) {
        List holder = enumValToList(enumVal);
        Object[] ret = new Object[holder.size()];
        holder.toArray(ret);
        return (ret);
    }

    /**
     * Make an array from the contents of an Enumeration
     *
     * @param enumVal non-null Enumeration
     * @return non-null array holding the elements of enumVal
     */
    public static String[] enumValToStrings(Enumeration enumVal) {
        List holder = enumValToList(enumVal);
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return (ret);
    }

    /**
     * Make an array from the contents of an Enumeration
     *
     * @param enumVal non-null Iterator
     * @return non-null List holding the elements of enumVal
     */
    public static List iteratorToList(Iterator it) {
        List holder = new ArrayList(256);
        while (it.hasNext())
            holder.add(it.next());
        return (holder);
    }

    /**
     * order on identityHashCode
     *
     * @param o1 non-null object
     * @param o2 non-null object
     * @return as above - last resort os a compare function
     */
    public static int compareObjects(Object o1, Object o2) {
        if (o1 == o2)
            return 0;
        if (System.identityHashCode(o1) < System.identityHashCode(o2))
            return 1;
        else
            return -1;
    }

    /**
     * Make an array from the contents of an Enumeration
     *
     * @param enumVal non-null Iterator
     * @return non-null array holding the elements of enumVal
     */
    public static Object[] iteratorToArray(Iterator it) {
        List holder = iteratorToList(it);
        Object[] ret = new Object[holder.size()];
        holder.toArray(ret);
        return (ret);
    }

    /**
     * Make an array from the contents of an Enumeration
     *
     * @param enumVal non-null Iterator
     * @return non-null array holding the elements of enumVal
     */
    public static String[] iteratorToStrings(Iterator it) {
        List holder = iteratorToList(it);
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return (ret);
    }

    /**
     * find all strings in a string array with a specific enf string
     *
     * @param data non-null array of string
     * @param test possibly null test string null returns all
     * @return non-null array holding the matching strings
     */
    public static String[] getStringsEndingWith(String[] data, String test) {
        if (test == null || test.length() == 0)
            return (data);
        List holder = new ArrayList(data.length);
        for (int i = 0; i < data.length; i++) {
            if (data[i].endsWith(test))
                holder.add(data[i]);
        }
        return ((String[]) Util.collectionToArray(holder, String.class));
    }

    /**
     * find all strings in a string array with a specific start string
     *
     * @param data non-null array of string
     * @param test possibly null test string null returns all
     * @return non-null array holding the matching strings
     */
    public static String[] getStringsStartingWith(String[] data, String test) {
        if (test == null || test.length() == 0)
            return (data);
        List holder = new ArrayList(data.length);
        for (int i = 0; i < data.length; i++) {
            if (data[i].startsWith(test))
                holder.add(data[i]);
        }
        return ((String[]) Util.collectionToArray(holder, String.class));
    }

    /**
     * convets value into en even percentage of
     * total i.e. 0,10,20 ...
     *
     * @param value positive int <= total
     * @param total positive int
     * @return valus as an even percentage of total
     */
    public static int decile(int value, int total) {
        if (value <= 0 || total <= 0)
            return (0);
        int test = (100 * value) / total; // percent
        int decile = (test + 4) / 10;
        int ret = Math.max(0, Math.min(100, decile * 10));
        return (ret);
    }

    /**
     * convert a Calendar into a readable string
     *
     * @param in non-null Calandar
     * @return non-null String
     */
    public static String formatDate(java.util.Calendar in) {
        return (formatDate(in.getTime()));
    }

    /**
     * convert a Date into a readable string
     *
     * @param in non-null Date
     * @return non-null String
     */
    public static String formatDate(java.util.Date in) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        return (fmt.format(in));
    }

    /**
     * convert a Date into a readable string
     *
     * @param in non-null Date
     * @return non-null String
     */
    public static String formatDay(java.util.Date in) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");
        return (fmt.format(in));
    }

    /**
     * write a string representing Now
     *
     * @return non-null String
     */
    public static String nowString() {
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        return (fmt.format(new Date()));
    }

    /**
     * write a string representing Now
     *
     * @return non-null String
     */
    public static String timeString() {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        return (fmt.format(new Date()));
    }

    /**
     * return a random lighter color
     *
     * @return
     */
    public static Color randomLightColor() {
        return new Color(128 + RND.nextInt(128),
                128 + RND.nextInt(128),
                128 + RND.nextInt(128));
    }

    /**
     * return a random lighter color
     *
     * @return
     */
    public static Color randomDarkColor() {
        int del = 0;
        return new Color(del + RND.nextInt(128),
                del + RND.nextInt(128),
                del + RND.nextInt(128));
    }


    /**
     * Compare 2 possibly null objects
     * current they musdt implement Comparable but that could change
     *
     * @param o1 possibly null object
     * @param o2 possibly null object
     * @return 1, 0,-1 as on compareTo
     * @see compareNonNull
     */
    public static int compare(Object o1, Object o2) {
        int answer = comparenull(o1, o2);
        if (answer != 0)
            return (answer);
        if (o1 == null)
            return (0); // o2 must also be null
        return (compareNonNull(o1, o2));
    }

    /**
     * Compare 2 possibly null objects
     * current they musdt implement Comparable but that could change
     *
     * @param o1 possibly null object
     * @param o2 possibly null object
     * @return 1, 0,-1 as on compareTo
     * @see compareNonNull
     */
    public static int compareCaseless(String o1, String o2) {
        int answer = comparenull(o1, o2);
        if (answer != 0)
            return (answer);
        if (o1 == null)
            return (0); // o2 must also be null
        int n = Math.min(o1.length(), o2.length());
        for (int i = 0; i < n; i++) {
            char c1 = Character.toLowerCase(o1.charAt(i));
            char c2 = Character.toLowerCase(o2.charAt(i));
            if (c1 != c2)
                return (c1 < c2 ? -1 : 1);
        }
        if (o1.length() < o2.length())
            return (-1);
        if (o1.length() > o2.length())
            return (1);
        return (0);
    }

    /**
     * Compare 2 possibly null objects
     * return non-
     *
     * @param o1 possibly null object
     * @param o2 possibly null object
     * @return 1, 0,-1 as on compareTo
     * @see compareNonNull
     */
    public static int comparenull(Object o1, Object o2) {
        if (o1 == null)
            return (o2 == null ? 0 : -1);
        if (o2 == null)
            return (1);
        return (0);
    }

    /**
     * Compare 2 non null objects
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see comparenull
     */
    protected static int compareNonNull(Object o1, Object o2) {
        if (o1 instanceof java.lang.Comparable)
            return (((java.lang.Comparable) o1).compareTo(o2));
        //noinspection SimplifiableIfStatement,ConstantIfStatement
        if (o1 instanceof Date) {
            long t1 = ((Date) o1).getTime();
            if (!(o2 instanceof Date))
                throw new IllegalArgumentException("Cannot compare a Date with a " + o2.getClass().getName());
            long t2 = ((Date) o2).getTime();
            if (t1 == t2)
                return (0);
            return (t1 < t2 ? -1 : 1);
        }
        if (o1.getClass().isArray() && o2.getClass().isArray())
            return (compareArrays(o1, o2));
        throw new IllegalArgumentException("Cannot compare non-comparable objects " + o1
                + " and " + o2);
    }

    /**
     * return the first n characters of a string if present
     *
     * @param s non-null string
     * @param n desdired number of chars positive
     * @return non-null string as above
     */
    public static String firstChars(String s, int n) {
        return (s.substring(0, Math.min(s.length(), n)));
    }

    /**
     * return the last n characters of a string if present
     *
     * @param s non-null string
     * @param n desdired number of chars positive
     * @return non-null string as above
     */
    public static String lastChars(String s, int n) {
        return (s.substring(s.length() - Math.min(s.length(), n)));
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    @SuppressWarnings("ConstantConditions")
    protected static int compareArrays(Object o1, Object o2) {
        if (o1 instanceof Object[] && o2 instanceof Object[])
            return (compareObjectArrays((Object[]) o1, (Object[]) o2));
        Class c1 = o1.getClass().getComponentType();
        Class c2 = o2.getClass().getComponentType();
        if (c1 != c2) {
            throw new IllegalArgumentException("Cannot compare arrays of types " +
                    c1.getName() + " and " + c2.getName());
        }
        if (c1 == Integer.TYPE)
            return (compareIntArrays((int[]) o1, (int[]) o2));
        if (c1 == Double.TYPE)
            return (compareDoubleArrays((double[]) o1, (double[]) o2));
        if (c1 == Float.TYPE)
            return (compareFloatArrays((float[]) o1, (float[]) o2));
        if (c1 == Short.TYPE)
            return (compareShortArrays((short[]) o1, (short[]) o2));
        if (c1 == Character.TYPE)
            return (compareCharArrays((char[]) o1, (char[]) o2));
        if (c1 == Byte.TYPE)
            return (compareByteArrays((byte[]) o1, (byte[]) o2));
        if (c1 == Long.TYPE)
            return (compareLongArrays((long[]) o1, (long[]) o2));
        if (c1 == Boolean.TYPE)
            return (compareBooleanArrays((boolean[]) o1, (boolean[]) o2));
        throw new IllegalStateException("Huh???"); // should havew covered all cases
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareObjectArrays(Object[] o1, Object[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            ret = compare(o1[i], o2[i]);
            if (ret != 0)
                return (ret);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareIntArrays(int[] o1, int[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o1[i] < o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareShortArrays(short[] o1, short[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o1[i] < o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareLongArrays(long[] o1, long[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o1[i] < o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareCharArrays(char[] o1, char[] o2) {
        int n = Math.min(o1.length, o2.length);
        //noinspection UnusedDeclaration
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o1[i] < o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareByteArrays(byte[] o1, byte[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o1[i] < o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareBooleanArrays(boolean[] o1, boolean[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareDoubleArrays(double[] o1, double[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o1[i] < o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    /**
     * Compare 2 non null arrays
     * current they musdt implement Comparable but that could change
     *
     * @param o1 non null object
     * @param o2 non null object
     * @return 1, 0,-1 as on compareTo
     * @see compare
     */
    protected static int compareFloatArrays(float[] o1, float[] o2) {
        int n = Math.min(o1.length, o2.length);
        int ret = 0;
        for (int i = 0; i < n; i++) {
            if (o1[i] != o2[i])
                return (o1[i] < o2[i] ? -1 : 1);
        }
        if (o1.length == o2.length)
            return (0);
        return (o1.length < o2.length ? -1 : 1);
    }

    public static class StringComparator implements Comparator<String>, Serializable {
        private StringComparator() {
        }

        public int compare(String o1, String o2) {
            return ((o1).compareTo(o2));
        }

        public boolean equals(Object o1, Object o2) {
            return ((o1).equals(o2));
        }
    }

    /**
     * compare a number of strings all of which represent numbers
     */
    public static class StringAsNumberComparator implements Comparator<String>, Serializable {
        private StringAsNumberComparator() {
        }

        public int compare(String o1, String o2) {
            //noinspection StringEquality
            if (o1 == o2)
                return 0;
            long l1 = Long.parseLong(o1);
            long l2 = Long.parseLong(o2);
            if (l1 > l2)
                return 1;
            if (l1 < l2)
                return -1;
            return o1.compareTo(o2);
        }

    }

    /**
     * compare a number of strings all of which represent numbers
     */
    public static class StringAsDescendingNumberComparator implements Comparator<String>, Serializable {
        private StringAsDescendingNumberComparator() {
        }

        public int compare(String o1, String o2) {
            //noinspection StringEquality
            if (o1 == o2)
                return 0;
            long l1 = Long.parseLong(o1);
            long l2 = Long.parseLong(o2);
            if (l1 > l2)
                return -1;
            if (l1 < l2)
                return 1;
            return o1.compareTo(o2);
        }

    }

    public static class DoubleComparator implements Comparator, Serializable {
        public int compare(Object o1, Object o2) {
            return (((Double) o1).compareTo((Double) o2));
        }

        public boolean equals(Object o1, Object o2) {
            return (o1.equals(o2));
        }
    }

    public static class IntegerComparator implements Comparator, Serializable {
        public int compare(Object o1, Object o2) {
            return (((Integer) o1).compareTo((Integer) o2));
        }

        public boolean equals(Object o1, Object o2) {
            return ((o1).equals(o2));
        }
    }

    public static class DateComparator implements Comparator, Serializable {
        public int compare(Object o1, Object o2) {
            return (((Date) o1).compareTo((Date) o2));
        }

        public boolean equals(Object o1, Object o2) {
            return (o1.equals(o2));
        }
    }

    /**
     * format a double into a price
     *
     * @param in non=null string
     * @return
     */
    public static String formatPrice(double in) {
        return (formatPrice(Double.toString(in)));
    }

    /**
     * format a string which may or may not be a number into a price
     *
     * @param in non=null string
     * @return
     */
    public static String formatPrice(String in) {
        // Numbers starting with "$" retrin the $ and are treated correctly
        String dollarSign = "";
        String realText = in;
        if (in.startsWith("$")) {
            in = in.substring(1);
            dollarSign = "$";
        }
        if (!Util.isEmptyString(in)) {
            if (Util.isStringNumber(in)) {
                double realVal = Double.parseDouble(in);
                realText = Util.formatDouble(realVal, 2);
                if (!realText.contains("."))
                    realText += ".";
                while (realText.indexOf(".") > (realText.length() - 3))
                    realText += "0";
            }
        }
        else {
            realText = "0.00";
        }
        if (dollarSign.length() > 0)
            realText = dollarSign + realText;
        return (realText);
    }

    /**
     * build a strig listing what is in a Map
     * Works best of all entried are string or have
     * good toString methods
     *
     * @param Values non-null Map
     * @return non-nul string
     */
    public static String describeMap(Map Values) {
        SortedSet keys = new TreeSet(Values.keySet());
        Iterator it = keys.iterator();
        StringBuilder sb = new StringBuilder();
        int lineLength = 0;
        while (it.hasNext()) {
            Object key = it.next();
            String keyStr = key.toString();
            String value = Values.get(key).toString();
            if (value.length() == 0)
                value = "\'\'";
            sb.append("key = ");
            sb.append(keyStr);
            sb.append(" value = ");
            sb.append(value);
            lineLength += 14 + keyStr.length() + value.length();
            if (lineLength > 80) {
                lineLength = 0;
                sb.append("\n");
            }
            else {
                sb.append("  ");
            }


        }
        return (sb.toString());
    }

    public static boolean[] clone(boolean[] in) {
        boolean[] ret = new boolean[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static int[] clone(int[] in) {
        int[] ret = new int[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static double[] clone(double[] in) {
        double[] ret = new double[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static float[] clone(float[] in) {
        float[] ret = new float[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static short[] clone(short[] in) {
        short[] ret = new short[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static char[] clone(char[] in) {
        char[] ret = new char[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static long[] clone(long[] in) {
        long[] ret = new long[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static byte[] clone(byte[] in) {
        byte[] ret = new byte[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }

    public static Object[] clone(Object[] in) {
        Object[] ret = new Object[in.length];
        System.arraycopy(in, 0, ret, 0, in.length);
        return ret;
    }


    /**
     * convert a collection into an array fo the given type
     *
     * @param col     non-null collection
     * @param example non-null instance of type held in collection
     * @return non-null array of example.class
     */
    public static Object[] arrayFromCollection(Collection col, Object example) {
        return (arrayFromCollection(col, example.getClass()));
    }

    /**
     * convert a collection into an array fo the given type
     *
     * @param col     non-null collection
     * @param example non-null class held in collection
     * @return non-null array of example
     */
    public static Object[] arrayFromCollection(Collection col, Class example) {
        Object[] ret = (Object[]) Array.newInstance(example, col.size());
        col.toArray(ret);
        return (ret);
    }

    /**
     * print a string listing what is in a Map
     * Works best of all entried are string or have
     * good toString methods
     *
     * @param out    non-null printstream
     * @param Values non-null Map
     */
    public static void dumpMap(PrintStream out, Map Values) {
        out.println(describeMap(Values));
    }

    public static boolean hasWholeWord(String test, String searchWord, boolean caseSensitive) {
        if (Util.isEmptyString(test))
            return (false);
        if (Util.isEmptyString(searchWord))
            return (true);
        if (!caseSensitive) {
            searchWord = searchWord.toLowerCase();
            test = test.toLowerCase();
        }
        int searchLen = searchWord.length();
        int testLen = test.length();
        int start = 0;
        int index = test.indexOf(searchWord, start);
        while (index != -1) {
            if (index == 0 ||
                    !Character.isLetterOrDigit(test.charAt(index - 1))) {
                if ((index + searchLen) == testLen ||
                        !Character.isLetterOrDigit(test.charAt(index + searchLen))) {
                    return (true);
                }
            }
            start = index + 1;
            if (start > testLen - searchLen)
                break;
            index = test.indexOf(searchWord, start);
        }
        return (false); // not there;
    }

    public static synchronized void setIPAddress(String in) {
        gLocalIPAddress = in;
    }

    public static synchronized String getIPAddress() {
        if (gLocalIPAddress != null)
            return (gLocalIPAddress);
        try {
            InetAddress address = InetAddress.getLocalHost();
            return (address.getHostAddress());
        }
        catch (UnknownHostException e) {
            return ("");
        }
    }

    public static int roundToMultiple(int value, int divisor) {
        int rem = value % divisor;
        if (rem == 0)
            return (value);
        return (value + divisor - rem);
    }

    /**
     * this can be used by a comparator to force special items to
     * be sorted in a particular order
     *
     * @param o1      non-null object to test
     * @param o2      non-null object to test
     * @param special non-null list of objects to force to the top of the search
     *                * @return as for comparble with 0 saying o1 and o2 are not special cases
     */
    public static int compareSpecialCase(Object o1, Object o2, Object[] special) {
        for (int i = 0; i < special.length; i++) {
            if (o1.equals(special[i]))
                return (-1);
            if (o2.equals(special[i]))
                return (1);
        }
        return (0); // no special cases
    }

    /**
     * sort an array using the comparator
     * return a new sorted array of the same type
     *
     * @param non-null array
     * @param c        non-null comparator on the elements of in
     * @return non-null array
     */
    public static Object[] sortArray(Object[] in, Comparator c) {
        if (in.length < 2)
            return (in);
        SortedSet holder = new TreeSet(c);
        for (int i = 0; i < in.length; i++)
            holder.add(in[i]);
        Object[] out = (Object[]) Array.newInstance(in.getClass().getComponentType(), holder.size());
        holder.toArray(out);
        return (out);
    }

    /**
     * compare for equality wher one or both object may be null
     *
     * @param o1 possibly null object
     * @param o2 possibly null object
     * @return true if both null or o1.equals(o2)
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == null)
            return o2 == null;
        //noinspection SimplifiableIfStatement
        if (o2 == null)
            return false;
        return o1.equals(o2);
    }


    /**
     * test if an item is in a list
     *
     * @param test  test ite,
     * @param items non-null list
     * @return true if test is in the array
     */
    public static boolean contains(int test, int[] items) {
        for (int i = 0; i < items.length; i++) {
            int item = items[i];
            if (item == test)
                return (true);
        }
        return (false);
    }

    /**
     * test a date represents todays date
     *
     * @param d1 non-null date
     * @return as above
     */
    public static boolean isToday(Date d1) {
        return sameDay(d1, new Date());
    }

    /**
     * time now
     *
     * @return
     */
    public static long now() {
        return System.currentTimeMillis();
    }


    /**
     * today's date
     * with time set at midnight
     *
     * @return
     */
    public static Date today() {
        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.HOUR, 0);
        calNow.set(Calendar.MINUTE, 0);
        calNow.set(Calendar.SECOND, 0);
        calNow.set(Calendar.MILLISECOND, 0);
        return calNow.getTime();
    }

    /**
     * a date set at midnight
     *
     * @return
     */
    public static java.sql.Date todaySQL() {
        return new java.sql.Date(today().getTime());
    }

    /**
     * test two dates have the same day
     *
     * @param d1 non-null date
     * @param d2 non-null date
     * @return as above
     */
    @SuppressWarnings("deprecation")
    public static boolean sameDay(Date d1, Date d2) {
        if (d1.getYear() != d2.getYear())
            return false;
        if (d1.getMonth() != d2.getMonth())
            return false;
        if (d1.getDay() != d2.getDay())
            return false;
        return true;
    }

//    /**
//     * finds the first entry in a collection of nameables by name
//     *
//     * @param test non-null test collection
//     * @param name non-null name
//     * @return possibly null item as above
//     */
//    public static INameable getByName(INameable[] test, String name) {
//        for (int i = 0; i < test.length; i++) {
//            INameable prop = test[i];
//            if (name.equals(prop.getName()))
//                return prop;
//        }
//        return null;
//
//    }

    /**
     * test if an item is in a list
     *
     * @param test  test ite,
     * @param items non-null list
     * @return true if test is in the array
     */
    public static boolean contains(double test, double[] items) {
        for (int i = 0; i < items.length; i++) {
            double item = items[i];
            if (item == test)
                return (true);
        }
        return (false);
    }

    /**
     * test if an item is in a list
     *
     * @param test  test ite,
     * @param items non-null list
     * @return true if test is in the array
     */
    public static boolean contains(short test, short[] items) {
        for (int i = 0; i < items.length; i++) {
            short item = items[i];
            if (item == test)
                return (true);
        }
        return (false);
    }

    /**
     * test if an item is in a list
     *
     * @param test  test ite,
     * @param items non-null list
     * @return true if test is in the array
     */
    public static boolean contains(char test, char[] items) {
        for (int i = 0; i < items.length; i++) {
            char item = items[i];
            if (item == test)
                return (true);
        }
        return (false);
    }

    public static String[] splitStringOnFields(String in, int[] cuts) {
        char[] data = in.toCharArray();
        List holder = new ArrayList();

        for (int i = 0; i < cuts.length; i++) {
            int start = cuts[i];
            int count = data.length - start;
            if (i < cuts.length - 1) {
                count = cuts[i + 1] - start;
            }
            String item = new String(data, start, count);
            holder.add(item);
        }
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return (ret);
    }

    /**
     * test if an item is in a list
     *
     * @param test  test ite,
     * @param items non-null list
     * @return true if test is in the array
     */
    public static boolean contains(float test, float[] items) {
        for (int i = 0; i < items.length; i++) {
            float item = items[i];
            if (item == test)
                return (true);
        }
        return (false);
    }

    /**
     * test if an item is in a list
     *
     * @param test  test ite,
     * @param items non-null list
     * @return true if test is in the array
     */
    public static boolean contains(Object test, Object[] items) {
        for (int i = 0; i < items.length; i++) {
            Object item = items[i];
            if (item.equals(test))
                return (true);
        }
        return (false);
    }

    /**
     * convet an int into an array of 32 booleans
     * 1 per bit
     *
     * @param in
     * @return
     */
    public static boolean[] intToBooleanArray(int in) {
        boolean[] ret = new boolean[32];
        int index = 1;
        for (int i = 0; i < ret.length; i++) {
            if ((in & index) != 0)
                ret[i] = true;
            index <<= 1;
        }
        return ret;
    }

    /**
     * convert an array of booleans - one ber bit
     * to an int
     *
     * @param in
     * @return
     */
    public static int booleanArrayToInt(boolean[] in) {
        if (in.length > 32)
            throw new IllegalArgumentException("no more than 32 bits in an int");
        int index = 1;
        int ret = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i])
                ret |= index;
            index <<= 1;
        }
        return ret;
    }


    public static java.util.Date parseDate(String s) {
        // todo try several formats
        return parseDate(s, DEFAULT_DATE_FORMAT);
    }

    public static java.util.Date parseDate(String s, DateFormat format) {
        if (Util.isEmptyString(s))
            throw new IllegalArgumentException("Cannot parse empty date string");
        try {
            return (format.parse(s));
        }
        catch (ParseException ex) {
            throw new IllegalArgumentException("Cannot parse  date string \'" + s + " with parser " + format);
        }
    }

    /*
    * return the class executing the code
    * @return non-null class
    */

    public Class executingClass() {
        return (gRevealer.doGetExecutingClass());
    }

    /*
    * true if this method is called from the named clazss
    */

    public boolean calledFromClass(Class Test) {
        Class[] TheStack = getStackClasses();
        for (int i = 0; i < TheStack.length; i++) {
            if (Test == TheStack[i])
                return (true);
        }
        return (false);
    }

    /**
     * hack to expose SecurityManager getClassContext method
     * to find what classes are executing
     * can tell what class static methos is called
     * return non-null stack  class array
     */
    public Class[] getStackClasses() {
        return (gRevealer.doGetClassContext());
    }

    // Use StackTrace.getStackTrace
    /**
     * print a stack trace
     */
    /* public static String getStackTrace() {
        return(getStackTrace(new Throwable()));
    }
    */
    /**
     * print a stack trace
     */
    public static final int MINIMUM_IP_LENGTH = 8;
    public static final int MAXIMUM_IP_LENGTH = 16;

    /**
     * is the string of the form ddd.d.dd.d
     *
     * @param in non-null test string
     * @return as above
     */
    public static boolean isRawIPAddress(String in) {
        in = in.trim();
        int len = in.length();
        if (len < MINIMUM_IP_LENGTH || len < MAXIMUM_IP_LENGTH) {
            return false;
        }
        int numberPeriods = 0;
        int numberGroups = 1;
        int numberDigits = 0;
        for (int i = 0; i < len; i++) {
            char c = in.charAt(i);
            if (c == '.') {
                if (numberDigits == 0)
                    return false;
                numberPeriods++;
                numberDigits = 0;
                numberGroups++;
                continue;
            }
            if (Character.isDigit(c)) {
                numberDigits++;
            }
            else {
                return false;
            }

        }
        if (numberDigits == 0 || numberDigits > 3)
            return false;
        return true;
    }

    public static int mode(int[] values) {
        if (values.length == 0)
            return 0;
        if (values.length == 1)
            return values[0];
        //noinspection MismatchedReadAndWriteOfArray,MismatchedReadAndWriteOfArray
        int[] copy = new int[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        Arrays.sort(values);
        int halfLength = values.length / 2;
        if (values.length % 2 == 1) {
            return values[halfLength];
        }
        else {
            return (values[halfLength - 1] + values[halfLength]) / 2;
        }
    }

    /**
     * internal method to extract bytes from a string which is
     * a raw IP address
     *
     * @param in
     * @return
     */
    protected static byte[] parseRawIPAddress(String in) {
        byte[] ret = new byte[4];
        String[] items = in.split("\\.");
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Byte.parseByte(items[i]);
        }
        return ret;
    }

    /**
     * break the string into lines no longer than lineLength assuming
     * no word exceeds line length -
     * <p/>
     * words end with any non  JavaIdentifierPart
     *
     * @param s          non-null string
     * @param lineLength max line langht
     * @return String with added CRs
     */
    public static String insertNeededReturns(String s, int lineLength) {
        if (s == null)
            return "";
        if (s.length() < lineLength)
            return s;
        StringBuilder sb = new StringBuilder();
        StringBuilder wordBuf = new StringBuilder();
        int wordEnd = getWord(s, wordBuf, 0);
        String word = wordBuf.toString();
        wordBuf.setLength(0);
        int currentLine = 0;
        while (wordEnd < s.length()) {
            int wordLength = word.length();
            if (wordLength > 0) {
                if ((currentLine + wordLength) < lineLength) {
                    currentLine += wordLength;
                    sb.append(word);
                }
                else {
                    sb.append("\n");
                    currentLine = 0;
                    currentLine += wordLength;
                    sb.append(word);
                }
            }
            wordEnd = getWord(s, wordBuf, wordEnd);
            word = wordBuf.toString();
            wordBuf.setLength(0);
        }
        return sb.toString();
    }

    public static int getWord(String s, StringBuilder sb, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c);
            if (!Character.isJavaIdentifierPart(c)) {
                return i + 1;
            }
        }
        return s.length();
    }

    public static Enum[] getEnumValues(Class cls) {
        if (!Enum.class.isAssignableFrom(cls))
            throw new IllegalArgumentException("Class " + cls +
                    " is not an enum");
        try {
            Method values = cls.getMethod("values");
            return (Enum[]) values.invoke(null);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + cls +
                    " is not acting link an enum");
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class " + cls +
                    " is not acting link an enum");
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Class " + cls +
                    " is not acting link an enum");
        }
    }

    /**
     * there is a but in the JDK 1.5 socket class which
     * does a DNS lookup in raw IP Addresses - this
     * forces that not to happen
     *
     * @param in non-null host string
     * @return possibly null INetAddress - null says the address in not
     *         properly formed
     */
    public static InetAddress getINetAddress(String in) {
        if (isRawIPAddress(in)) {
            byte[] bytes = parseRawIPAddress(in);
            try {
                return InetAddress.getByAddress(in, bytes);
            }
            catch (UnknownHostException ex) {
                return null;
            }

        }
        else {
            try {
                return InetAddress.getByName(in);
            }
            catch (UnknownHostException ex) {
                return null;
            }
        }
    }


    public static double differenceInDays(final Date dateA, final Date dateB) {
        final long diffInMilliseconds = dateB.getTime() - dateA.getTime();
        final double diffInDays = (double) diffInMilliseconds / (double) (dayInMs);
        return diffInDays;
    }

    public static Date addDays(final Date dateA, final int numDays) {
        final long diffInMilliseconds = numDays * dayInMs;
        final Date newDate = new Date(dateA.getTime() + diffInMilliseconds);
        return newDate;
    }


    public static boolean isTodayAfterExclusive(final Date referenceDate) {
        return isAfterExclusive(referenceDate, new Date(System.currentTimeMillis()));
    }

    public static boolean isAfterExclusive(final Date referenceDate, final Date date) {
        GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
        c.setTime(date);

        GregorianCalendar referenceCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        referenceCalendar.setTime(referenceDate);
        return c.after(referenceCalendar);
    }

    public static int getYear(Date date) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        long time = date.getTime();
        calendar.setTimeInMillis(time);
        return calendar.get(calendar.YEAR);
    }

    /**
     * @param date Date
     * @return int month number 0-based
     */
    public static int getMonth(Date date) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        long time = date.getTime();
        calendar.setTimeInMillis(time);
        return calendar.get(calendar.MONTH);
    }

    public static int getHours(Date date) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        long time = date.getTime();
        calendar.setTimeInMillis(time);
        return calendar.get(calendar.HOUR);
    }

    public static int getMinutes(Date date) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        long time = date.getTime();
        calendar.setTimeInMillis(time);
        return calendar.get(calendar.MINUTE);
    }

    public static String getAMPM(Date date) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        long time = date.getTime();
        calendar.setTimeInMillis(time);
        int ampm = calendar.get(calendar.AM_PM);

        return ampm == 0 ? "AM" : "PM";
    }

    public static boolean isBeforeExclusive(final Date referenceDate, final Date date) {
        GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
        c.setTime(date);

        GregorianCalendar referenceCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        referenceCalendar.setTime(referenceDate);

        final boolean isBefore = c.before(referenceCalendar);
        if (!isBefore) {
            return false;
        }

        return true;
    }


    public static boolean isInRangeExclusive(final Date date, final Date dateStart,
                                             final Date dateEnd) {
        GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
        c.setTime(date);

        GregorianCalendar cStart = (GregorianCalendar) GregorianCalendar.getInstance();
        cStart.setTime(dateStart);

        final boolean isAfter = c.after(cStart);
        if (!isAfter) {
            return false;
        }

        GregorianCalendar cEnd = (GregorianCalendar) GregorianCalendar.getInstance();
        cEnd.setTime(dateEnd);

        final boolean isBefore = c.before(cEnd);
        if (!isBefore) {
            return false;
        }

        return true;
    }

    /**
     * @param dateText for example, "01/02/2003"
     * @return
     */
    public static Date getDate(final String dateText) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        try {
            final Date date = sdf.parse(dateText);
            return date;
        }
        catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static String getDateTimeText(final Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "MM/dd/yyyy hh:mm:ss a"); // -> 1996.07.10 AD at 05:08:56 AM
        final String dateText = sdf.format(date);
        return dateText;
    }

    public static String getDateText(final Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy"); // ->  07/20/2004
        final String dateText = sdf.format(date);
        return dateText;
    }

    public static String getTimeText(final Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a"); // ->  05:08:56 AM
        final String dateText = sdf.format(date);
        return dateText;
    }

    /**
     * return a string of todays date suitable for use in a file name
     *
     * @return
     */
    public static String getTodayString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MMM_yyyy"); // ->  05:08:56 AM
        final String dateText = sdf.format(new Date());
        return dateText;
    }


    /**
     * return a string of todays date time suitable
     * this is in more international form that the version above
     *
     * @return
     */
    public static String getNowString() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy HH:mm:ss"); // ->  23 Jan 2006 16:08:56
        final String dateText = sdf.format(new Date());
        return dateText;
    }

    /**
     * @param source     non-null source text
     * @param beforeText non-null text before regoin of interest - if not found
     *                   return is empry string
     * @param afterText  non-null text after regoin of interest - if not found after
     *                   before text
     *                   return is empty string
     * @return non-null enclosed text - may be empty
     */
    public static String getTextBetween(String source, String beforeText, String afterText) {
        try {
            int start = source.indexOf(beforeText);
            if (start == -1)
                return "";
            start += beforeText.length();
            int end = source.indexOf(afterText, start);
            if (end == -1)
                return "";
            return source.substring(start, end);
        }
        catch (RuntimeException e) {
            return ""; // any erros says not found
        }
    }

    //*******************************
//- *******************
//- End Class Util
    protected static final StackRevealer gRevealer = new StackRevealer();

    /**
     * hack to expose SecurityManager getClassContext method
     * to find what classes are executing
     * can tell what class static methos is called
     */
    protected static class StackRevealer extends SecurityManager {
        public Class doGetExecutingClass() {
            Class[] allClasses = getClassContext();
            return (allClasses[1]);
        }

        public Class[] doGetClassContext() {
            Class[] allClasses = getClassContext();
            Class[] ret = new Class[allClasses.length - 1];
            System.arraycopy(allClasses, 1, ret, 0, allClasses.length - 1);
            return (ret);
        }

    }

//    public static class NameComparator implements Comparator, Serializable {
//        public int compare(Object o1, Object o2) {
//            String n1 = ((INameable) o1).getName();
//            String n2 = ((INameable) o2).getName();
//            if (n1 == null) {
//                return (n2 == null ? 0 : 1);
//            }
//            return (n1.compareTo(n2));
//        }
//    }  // end class


    /**
     * { method
     *
     * @param in - string to capitalize
     * @return - non-null capitailzed string
     *         }
     * @name capitalize
     * @function - test of the object test is a member of the list data
     */
    public static String capitalize(String in) {
        if (in.length() == 0)
            return ("");
        StringBuilder s = new StringBuilder(in.length());
        int i = 0;
        char c = in.charAt(i++);
        while (c <= ' ') {
            c = in.charAt(i++);
            if (i >= in.length())
                return ("");
        }

        s.append(Character.toUpperCase(c));
        boolean atSpace = false;
        for (; i < in.length(); i++) {
            c = Character.toLowerCase(in.charAt(i));
            if (c == ' ') {
                atSpace = true;
            }
            else {
                if (atSpace) {
                    if (c > ' ') {
                        c = Character.toUpperCase(c);
                        atSpace = false;
                    }
                }
            }
            s.append(c);
        }
        return (s.toString());
    }


    /**
     * @param command non-null command to execute
     * @return non-null output string
     * @throws RuntimeException on error
     */
    public static String captureExecOutput(String command) throws RuntimeException {
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            String buf;
            BufferedReader se = new BufferedReader
                    (new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String aline = buf = se.readLine();
            while (aline != null) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(buf);
                aline = buf = se.readLine();
            }

            return sb.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * original string which may contain characters either reserved in XML or with different representation
     * in different encodings (like 8859-1 and UFT-8)
     *
     * @return
     */
    public static String xmlEscape(String originalUnprotectedString) {
        if (originalUnprotectedString == null) {
            return null;
        }
        boolean anyCharactersProtected = false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < originalUnprotectedString.length(); i++) {
            char ch = originalUnprotectedString.charAt(i);

            boolean controlCharacter = ch < 32;
            boolean unicodeButNotAscii = ch > 126;
            boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

            if (unicodeButNotAscii || controlCharacter) {
                //noinspection StringConcatenationInsideStringBufferAppend
                sb.append("&#" + (int) ch + ";");
                anyCharactersProtected = true;
            }
            else {
                switch (ch) {
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '\"':
                        sb.append("&guot;");
                        break;
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '\'':
                        sb.append("&apos;");
                        break;
                    default:
                        sb.append(ch);

                }
            }
        }

        return sb.toString();
    }


    public static final int INTEGER_SIZE = 4; // bytes per integer
    public static final int FLOAT_SIZE = 4; // bytes per float
    public static final int FLOAT64_SIZE = 8; // bytes per float 64


    public static final String[] RIGHT_OF_DECIMAL_FORMATS = {
            "###################",
            "##################.#",
            "#################.##",
            "################.###",
            "###############.####",
            "##############.#####",
            "#############.######",
            "############.#######",
            "###########.########",
            "##########.#########",
            "#########.##########",
    };

    public static final String[] SCIENTIFIC_FORMATS = {
            "0E+000",
            "0.0E000",
            "0.00E000",
            "0.000E000",
            "0.0000E+000",
            "0.00000E+000",
            "0.000000E+000",
            "0.0000000E+000",
            "0.00000000E+000",
            "0.00000000E+000",
            "0.000000000E+000",
    };

    protected static DecimalFormat getRightOfDecimal(int rightOfDecimal) {
        if (rightOfDecimal < 0 || rightOfDecimal >= RIGHT_OF_DECIMAL_FORMATS.length)
            throw new IllegalArgumentException(
                    "0 .. " + RIGHT_OF_DECIMAL_FORMATS.length + " are supported");
        return new DecimalFormat(RIGHT_OF_DECIMAL_FORMATS[rightOfDecimal]);
    }


    protected static DecimalFormat getScientific(int rightOfDecimal) {
        if (rightOfDecimal < 0 || rightOfDecimal >= SCIENTIFIC_FORMATS.length)
            throw new IllegalArgumentException(
                    "0 .. " + SCIENTIFIC_FORMATS.length + " are supported");
        return new DecimalFormat(SCIENTIFIC_FORMATS[rightOfDecimal]);
    }


    public static String formatScientific(double f, int rightOfDecimal) {
        return getScientific(rightOfDecimal).format(f);
    }


    public static String formatFloat(float f, int rightOfDecimal) {
        return getRightOfDecimal(rightOfDecimal).format(f);
    }


    public static <K, T> void insertIntoArrayMap(Map<K, T[]> map, K key, T value) {
        T[] item = map.get(key);
        if (item == null) {
            T[] newValue = (T[]) Array.newInstance(value.getClass(), 1);
            newValue[0] = value;
            map.put(key, newValue);
        }
        else {  // something is there
            T[] newValue = (T[]) Array.newInstance(value.getClass(), item.length + 1);
            System.arraycopy(item, 0, newValue, 0, item.length);
            newValue[item.length] = value;
            map.put(key, newValue);

        }
    }


    public static double[] convertToValueType(Double[] inp) {
        double[] ret = new double[inp.length];
        for (int i = 0; i < inp.length; i++) {
            ret[i] = inp[i];

        }
        return ret;
    }


    public static int[] convertToValueType(Integer[] inp) {
        int[] ret = new int[inp.length];
        for (int i = 0; i < inp.length; i++) {
            ret[i] = inp[i];

        }
        return ret;
    }


    public static float[] convertToValueType(Float[] inp) {
        float[] ret = new float[inp.length];
        for (int i = 0; i < inp.length; i++) {
            ret[i] = inp[i];

        }
        return ret;
    }


    public static InputStream getResourceStream(String resourceStr) {
        return getResourceStream(Util.class, resourceStr);
    }

    public static InputStream getResourceStream(Class theClass, String resourceStr) {
        String resource = resourceStr.replace("res://", "");
        final InputStream stream = theClass.getResourceAsStream(resource);

        if (stream == null)
            throw new IllegalArgumentException("Cannot open resource " + resourceStr);
        return stream;
    }


    public static InputStream getDescribedStream(String name) {
        if (name.startsWith("res://"))
            return getResourceStream(name);
        try {
            File test = new File(name);
            if (!test.exists())
                return null;
            if (name.endsWith(".gz"))
                return new GZIPInputStream(new FileInputStream(name));
            return new FileInputStream(name);
        }
        catch (IOException e) {

            throw new RuntimeException("the file " + name + " was not found", e);
        }
    }


    /**
     * return a line number reader for a String
     * @param s inpout string
     * @return reader
     */
    public static
    @Nonnull
    InputStream asInputStream(@Nonnull String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    /**
     * return a line number reader for a String
     * @param s inpout string
     * @return reader
     */
    public  static
    @Nonnull
    LineNumberReader asLineReader(@Nonnull String s) {
        try {
            return new LineNumberReader(new InputStreamReader(asInputStream(s)));
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }
    }


}
