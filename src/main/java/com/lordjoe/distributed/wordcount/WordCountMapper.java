package com.lordjoe.distributed.wordcount;

import com.lordjoe.distributed.*;

import javax.annotation.*;
import java.util.*;
import java.util.regex.*;

/**
 * com.lordjoe.distributed.wordcount.WordCountMapper
 * User: Steve
 * Date: 8/28/2014
 */
public class WordCountMapper implements IMapperFunction<String,String, String, Integer> {

    private static final Pattern SPACE = Pattern.compile(" ");

    public WordCountMapper() {
    }

    /**
     * this is what a Mapper does
     *
     * @param keyin
     * @param valuein
     * @return iterator over mapped key values
     */
    @Nonnull @Override public Iterable<KeyValueObject<String, Integer>> mapValues(@Nonnull final String location,@Nonnull final String line) {
        String[] split = SPACE.split(line);
        for (int i = 0; i < split.length; i++) {
            split[i] = regularizeString(split[i]);
        }
        List<KeyValueObject<String, Integer>> holder = new ArrayList<KeyValueObject<String, Integer>>();
        for (String s : split) {
           if(s.length() > 0)
               holder.add(new KeyValueObject<String, Integer>(s,1));
        }
          return holder;
    }

//
//    public Iterable<String> call(String s) {
//        String[] split = SPACE.split(s);
//        for (int i = 0; i < split.length; i++) {
//            split[i] = regularizeString(split[i]);
//        }
//        return Arrays.asList(split);
//    }

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
