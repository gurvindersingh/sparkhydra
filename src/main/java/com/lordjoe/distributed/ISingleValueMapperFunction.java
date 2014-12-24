package com.lordjoe.distributed;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.ISingleValueMapperFunction
 * Guaranteed to return ONE and only One Object for each Key Value pair
 * User: Steve
 * Date: 8/25/2014
 */
public interface ISingleValueMapperFunction<KEYIN extends Serializable,VALUEIN extends Serializable,KOUT extends Serializable,VOUT extends Serializable> extends Serializable {
    /**
       * this is what a Mapper does
       * @param value  input value
       * @return iterator over mapped key values
       */
      public @Nonnull  KeyValueObject<KOUT,VOUT>  mapValues(@Nonnull KEYIN keyin, @Nonnull VALUEIN valuein);




}
