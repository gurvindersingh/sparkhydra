package com.lordjoe.distributed.wordcount;

import com.lordjoe.distributed.spark.accumulators.*;
import com.lordjoe.distributed.util.*;
import org.apache.spark.spillable.*;
import scala.*;

import java.util.*;

/**
* com.lordjoe.distributed.wordcount.WordCountFlatMapFinction
* User: Steve
* Date: 9/4/2014
*/
public class WordCountFlatMapFunction extends AbstractLoggingFlatMapFunction<Iterator<Tuple2<String, Integer>>, WordNumber> {


    private String word;
    private SpillableList<WordNumber> ret = new SpillableList<WordNumber>(500);

    @Override public Iterable<WordNumber> doCall(final Iterator<Tuple2<String, Integer>> pTuple2Iterator) throws Exception {
        int sum = 0;
        while (pTuple2Iterator.hasNext()) {

            Tuple2<String, Integer> next = pTuple2Iterator.next();
            String s = next._1();
            if (s.length() == 0)
                continue;
            if (word == null) {
                word = s;
            }
            else {
                if (!word.equals(s)) {
                    ret.add(new WordNumber(word, sum));
                    sum = 1;
                    word = s;
                }
            }
            sum += next._2();
        }
        ret.add(new WordNumber(word, sum));
        return ret;

    }
}
