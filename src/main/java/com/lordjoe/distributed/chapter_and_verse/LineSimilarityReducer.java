package com.lordjoe.distributed.chapter_and_verse;

import com.lordjoe.distributed.*;
import net.ricecode.similarity.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.chapter_and_verse.LineSimilarityReducer
 * User: Steve
 * Date: 8/28/2014
 */
public class LineSimilarityReducer implements IReducerFunction<ChapterKeyClass, LineAndLocationMatch,ChapterKeyClass, LineAndLocationMatch  >,Serializable {

    public static final int LINES_TO_CONSIDER = 200;
    private final SimilarityStrategy strategy;
    private final Queue<LineAndLocationMatch> heldValues = new ArrayDeque<LineAndLocationMatch>();
    private String currentChapter;

    public LineSimilarityReducer(final SimilarityStrategy pStrategy) {
        strategy = pStrategy;
    }

    public LineSimilarityReducer( ) {
        this(new LevenshteinDistanceStrategy());
    }

    /**
     * this is what a reducer does
     *
     * @param key
     * @param values
     * @param consumer @return iterator over mapped key values
     */
    @Nonnull @Override public void handleValues(@Nonnull final ChapterKeyClass key, @Nonnull final Iterable<LineAndLocationMatch> values, final IKeyValueConsumer<ChapterKeyClass, LineAndLocationMatch>... consumer) {
        if (key.chapter != currentChapter) {
            while (!heldValues.isEmpty()) {
        //        LineAndLocationMatch first = heldValues.remove();
                handleTopLine(consumer);
            }
            currentChapter = key.chapter;
        }


        for (LineAndLocationMatch value : values) {
            int lastLine = value.thisLine.lineNumber - LINES_TO_CONSIDER;
            LineAndLocationMatch top = heldValues.peek();
            while (top != null && top.thisLine.lineNumber < lastLine) {
                handleTopLine(consumer);
                top = heldValues.peek();
            }

            for (LineAndLocationMatch heldValue : heldValues) {
                double thisSimilarity = strategy.score(value.thisLine.line, heldValue.thisLine.line);
                if (thisSimilarity > heldValue.similarity) {
                    heldValue.similarity = thisSimilarity;
                    heldValue.bestFit = value.thisLine;
                }
                if (thisSimilarity > value.similarity) {
                    value.similarity = thisSimilarity;
                    value.bestFit = heldValue.thisLine;
                }

            }
            heldValues.offer(value);

        }

    }

    protected void handleTopLine(final IKeyValueConsumer<ChapterKeyClass, LineAndLocationMatch>[] consumer) {
        LineAndLocationMatch first = heldValues.remove();
        for (int i = 0; i < consumer.length; i++) {
            ChapterKeyClass key = new ChapterKeyClass(first.thisLine.chapter,first.thisLine.lineNumber);
            consumer[i].consume(new KeyValueObject<ChapterKeyClass, LineAndLocationMatch>(key,first));

        }
    }


}
