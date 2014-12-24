package com.lordjoe.distributed.wordcount;

import com.lordjoe.distributed.*;

import javax.annotation.*;

/**
 * com.lordjoe.distributed.wordcount.WordCountReducer
 * User: Steve
 * Date: 8/28/2014
 */
public class WordCountReducer implements IReducerFunction<String,Integer,String,Integer > {


    /**
     * this is what a reducer does
     *
     * @param key
     * @param values
     * @param consumer @return iterator over mapped key values
     */
    @Nonnull @Override public void handleValues(@Nonnull final String key, @Nonnull final Iterable<Integer> values, final IKeyValueConsumer<String, Integer>... consumer) {
        int count = 0;
        for (Integer value : values) {
          count += value;
          }
        for (int i = 0; i < consumer.length; i++) {
            IKeyValueConsumer<String, Integer> cnsm = consumer[i];
            cnsm.consume(new KeyValueObject<String, Integer>(key, count));

        }

    }


}
