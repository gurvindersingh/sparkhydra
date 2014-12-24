package com.lordjoe.distributed.util;

import java.util.*;
import java.util.regex.*;

/**
 * com.lordjoe.distributed.util.LineToWords
 * User: Steve
 * NOTE - this really supports word count tests
 * Date: 8/25/2014
 */
public class LineToWords   {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static Iterable<String>  fromLine(String line)
    {
        String[] split = splitLine(line);
          return Arrays.asList(split);

    }

    public static String[] splitLine(final String line) {
        String[] split = SPACE.split(line);
        for (int i = 0; i < split.length; i++) {
            split[i] = regularizeString(split[i]);
        }
        return split;
    }

    public static String dropNonLetters(String s) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if (Character.isLetter(c))
                 sb.append(c);
         }

         return sb.toString();
     }


     public static String regularizeString(String inp) {
         inp = inp.trim();
         inp = inp.toUpperCase();
         return dropNonLetters(inp);
     }
}
