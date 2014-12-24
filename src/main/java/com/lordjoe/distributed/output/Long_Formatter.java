package com.lordjoe.distributed.output;

/**
 * com.lordjoe.distributed.output.Number_Formatter
 * User: Steve
 * Date: 12/13/2014
 */
public class Long_Formatter {
    public static final long ONE_K = 1024;
    public static final long ONE_MEG = ONE_K * 1024;
    public static final long ONE_GIG = ONE_MEG * 1024;
    public static final long ONE_TERA = ONE_GIG * 1024;

    public static final int TOSHOW = 30;

    public static String format(long n) {
        if(n < TOSHOW * ONE_K)
            return Long.toString(n);
        n /= ONE_K;
        if(n < TOSHOW * ONE_K)
            return Long.toString(n) + "K";
        n /= ONE_K;
        if(n < TOSHOW * ONE_K)
            return Long.toString(n) + "M";
        n /= ONE_K;
        if(n < TOSHOW * ONE_K)
            return Long.toString(n) + "G";
        n /= ONE_K;
        return Long.toString(n) + "T";
    }

}
