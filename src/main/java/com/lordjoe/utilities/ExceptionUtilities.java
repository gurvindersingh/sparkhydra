package com.lordjoe.utilities;

import java.io.*;

/**
 * com.lordjoe.utilities.ExceptionUtilities
 * User: Steve
 * Date: 12/13/13
 */
public class ExceptionUtilities {

    /**
     * create a RuntimeException to throw
     * @param ex
     * @return
     */
    public static RuntimeException buildException(Exception ex)  {
       if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
         }
        return new RuntimeException(getUltimateCause(ex));
    }
    /**
     * unwind cause to find what really caused an exception
     * @param in
     * @return
     */
    public static  Throwable getUltimateCause(Throwable in)
    {
        final Throwable cause = in.getCause();
        if(cause == null || in == cause)
            return in;
        return getUltimateCause(cause);
    }

    public static void printUltimateCause(Throwable ex)
    {
        getUltimateCause(ex).printStackTrace();
    }

    public static void printAllStacks(Throwable t) {
        printAllStacks(t, System.out);
    }


    public static void printAllStacks(Throwable t, Appendable out) {

        try {
            StackTraceElement[] stackTrace = t.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                StackTraceElement trace = stackTrace[i];
                out.append("\tat " + trace + "\n");

            }

            Throwable next = t.getCause();
            if(next != null && next != t)
                printAllStacks(next,   out) ;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void printCausalStacks(Throwable t) {
        printCausalStacks(t, System.out);
    }


    public static void printCausalStacks(Throwable t, Appendable out) {
        Throwable next = t.getCause();
        if(next == null || next == t)
            printAllStacks(t,out);
        printCausalStacks(next,out);
    }

}
