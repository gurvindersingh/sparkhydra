package com.lordjoe.distributed.wordcount;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.util.*;

/**
 * com.lordjoe.distributed.wordcount.FirstLetterPartition
 * Silly test partition function for WordCount partitioning on first letter
 * User: Steve
 * Date: 9/2/2014
 */
public class FirstLetterPartition implements IPartitionFunction<WordNumber> {



    @Override public int getPartition(final WordNumber inp) {
        if(inp.word.length() > 0)
            return Character.toUpperCase(inp.word.charAt(0)) - 'A';
        return 0;
    }
}
