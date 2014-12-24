package com.lordjoe.distributed.chapter_and_verse;

import com.lordjoe.distributed.*;

import javax.annotation.*;
import java.util.*;
import java.util.regex.*;

/**
 * com.lordjoe.distributed.wordcount.WordCountMapper
 * User: Steve
 * Date: 8/28/2014
 */
public class ChapterLinesMapper implements IMapperFunction<String,String, ChapterKeyClass, LineAndLocationMatch> {

    private static final Pattern SPACE = Pattern.compile("\n");

    public ChapterLinesMapper() {
    }

    /**
     * this is what a Mapper does
     *
     * @param keyin
     * @param valuein
     * @return iterator over mapped key values
     */
    @Nonnull @Override public Iterable<KeyValueObject<ChapterKeyClass, LineAndLocationMatch>> mapValues(String fileName,String wholeFile) {
        String chapter = fileName;
        String[] split = SPACE.split(wholeFile);
        List<KeyValueObject<ChapterKeyClass, LineAndLocationMatch>> holder = new ArrayList<KeyValueObject<ChapterKeyClass, LineAndLocationMatch>>();
        for (int i = 0; i < split.length; i++) {
            String s  = split[i].trim();
            if(s.length() == 0)
                continue;
            ChapterKeyClass key = new ChapterKeyClass(chapter ,i);
            LineAndLocation value = new LineAndLocation(key.chapter,key.lineNumber,s); // one line by chapter and text
            LineAndLocationMatch match = new LineAndLocationMatch(value);
            holder.add(new KeyValueObject<ChapterKeyClass, LineAndLocationMatch>(key,match)) ;
        }
        return holder;
    }





}
