package com.lordjoe.distributed.tandem;

import com.lordjoe.distributed.*;
import org.systemsbiology.xtandem.*;

import javax.annotation.*;
import java.util.*;

/**
 * com.lordjoe.distributed.tandem.NullTandemMapper
 * User: Steve
 * Date: 10/1/2014
 */
public class NullTandemMapper extends AbstractTandemFunction  implements IMapperFunction<String, String, String, String> {
    public NullTandemMapper(final XTandemMain pMain) {
        super(pMain);
    }

    /**
     * this is what a Mapper does
     *
     * @param keyin
     * @param valuein
     * @return iterator over mapped key values
     */
    @Nonnull
    @Override
    public Iterable<KeyValueObject<String, String>> mapValues(@Nonnull final String key, @Nonnull final String valuein) {
        List<KeyValueObject<String, String>> holder = new ArrayList<KeyValueObject<String, String>>();
          holder.add(new KeyValueObject<String, String>(key,valuein));
            return holder;
      }
}
