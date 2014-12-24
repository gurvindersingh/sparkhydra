package com.lordjoe.distributed;

import javax.annotation.*;

/**
 * com.lordjoe.distributed.NullStringReducer
 * simple sends keys and values to consumers - effectively doing nothing
 * User: Steve
 * Date: 9/24/2014
 */
public class NullStringReducer implements IReducerFunction<String, String,String, String> {

    public NullStringReducer() {
       }

    /**
     * this is what a reducer does
     *
     * @param key
     * @param values
     * @param consumer @return iterator over mapped key values
     */
    @Nonnull
    @Override
    public void handleValues(@Nonnull final String key, @Nonnull final Iterable<String> values, final IKeyValueConsumer<String, String>... consumer) {


        for (int i = 0; i < consumer.length; i++) {
            for (String value : values) {
                consumer[i].consume(new KeyValueObject<String, String>(key, value));
              }
          }


    }


}
