package com.lordjoe.distributed.util;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.util.WordNumber
 * NOTE the most obvious use of this class is for WordCount -
 *    it is in production code at this level so much test code in many
 *    implementation roots can use it
 * User: Steve
 * Date: 8/25/2014
 */
public class WordNumber implements Comparable<WordNumber>,Serializable {

    public static WordNumber sum(WordNumber... in)
    {
        int count = 0;
        for (WordNumber wordNumber : in) {
            count += wordNumber.count;
        }
        return new WordNumber(in[0].word,count);
    }

    public final String word;
    public final int count;

    public WordNumber(final String pX, final int pY) {
        word = pX;
        count = pY;
    }

    public WordNumber(String s) {
        StringTokenizer stk = new StringTokenizer(s,":");
        word =  stk.nextToken().trim();
        count = Integer.parseInt(stk.nextToken().trim());
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    @Override public String toString() {
        return  word + ":"  + count;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final WordNumber xyPoint = (WordNumber) o;

        if (!word.equals(xyPoint.word)) return false;
        if (count != xyPoint.count) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = word.hashCode();
        result = 31 * result + count;
        return result;
    }


    @Override public int compareTo(final WordNumber o) {
        int ret = word.compareTo(o.word);
        if(ret != 0)
            return ret;
        if(count == o.count)
            return 0;
        return count > o.count ? -1 : 1;
    }
}
