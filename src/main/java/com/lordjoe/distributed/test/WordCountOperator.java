package com.lordjoe.distributed.test;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.util.*;
import com.lordjoe.distributed.wordcount.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.WordCountOperator
 * User: Steve
 * Date: 9/18/2014
 */
public class WordCountOperator {

    public static final String MY_BOOK = "/war_and_peace.txt";
    public static final String BOOK_COUNTS = "/war_and_peace_counts.txt";

    public static Iterable<KeyValueObject<String, String>> getFileLines() {
        final InputStream is = WordCountOperator.class.getResourceAsStream(MY_BOOK);
        return JavaMapReduceUtilities.fromPath(MY_BOOK, is);
    }

    public static List<KeyValueObject<String, Integer>> getRealCounts() {
        final InputStream is = WordCountOperator.class.getResourceAsStream(BOOK_COUNTS);
        String[] strings = JavaMapReduceUtilities.readStreamLines(is);
        List<KeyValueObject<String, Integer>> holder = new ArrayList<KeyValueObject<String, Integer>>();
        for (String string : strings) {
            String[] items = string.split(":");
            holder.add(new KeyValueObject<String, Integer>(items[0], Integer.parseInt(items[1])));
        }

        return holder;
    }


    public static void validateWordCount(MapReduceEngineFactory factory) {
        IMapReduce handler = factory.buildMapReduceEngine("WordCount", new WordCountMapper(), new WordCountReducer());

        Iterable<KeyValueObject<String, String>> lines = getFileLines();
        handler.mapReduceSource(lines);

        Iterable<KeyValueObject<String, Integer>> results = handler.collect();

        List<KeyValueObject<String, Integer>> real = getRealCounts();
        Collections.sort(real);

        Map<String, Integer> countMap = new HashMap<String, Integer>();
        for (KeyValueObject s : real) {
            countMap.put((String) s.key, (Integer) s.value);
        }

        int count = 0;
        int badCount = 0;
        Iterator<KeyValueObject<String, Integer>> iterator = results.iterator();
        while (iterator.hasNext()) {
            KeyValueObject<String, Integer> next = iterator.next();
            Integer realValue = countMap.get(next.key);
            count++;
            if (realValue != null) {
                if (!realValue.toString().equals(next.value.toString())) {
                    badCount++;
                }
            }
            else {
                badCount++;
            }

        }
        badCount += Math.abs(count - countMap.size());
        double badRatio = (double) badCount / countMap.size();
        if (badRatio > 0.003)
            throw new IllegalStateException("problem"); // ToDo change;

    }
}
