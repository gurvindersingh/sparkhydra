package com.lordjoe.distributed.spark;

/**
 * com.lordjoe.distributed.spark.GeneratingPseudoList
 * Version of a pseudolist capable of  creating objects -
 *  Note the existance of a counter means it will NOT parallelize well
 * User: Steve
 * Date: 12/8/2014
 */
public class GeneratingPseudoList<T> extends AbstractPseudoList<T> {

     private final ObjectGenerator<T> generator;

    public GeneratingPseudoList(final int pListSize, final ObjectGenerator<T> pGenerator) {
        super(pListSize);
        generator = pGenerator;
    }

    @Override
    public T generateElement() {
        return generator.generateObject();
    }
}
