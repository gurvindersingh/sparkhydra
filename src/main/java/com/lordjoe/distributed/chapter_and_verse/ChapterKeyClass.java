package com.lordjoe.distributed.chapter_and_verse;

import java.io.*;

/**
* com.lordjoe.distributed.chapter_and_verse.ChapterKeyClass
* User: Steve
* Date: 9/14/2014
*/ // location - acts as a key
public class ChapterKeyClass implements Serializable,Comparable<ChapterKeyClass> {
    public final String chapter;
    public final int lineNumber;

    public ChapterKeyClass(final String pChapter, final int pLineNumber) {
        chapter = pChapter;
        lineNumber = pLineNumber;
    }

    public String toString() {
        return chapter + ":" + lineNumber;
    }


    @Override public int compareTo(final ChapterKeyClass o) {
        int ret = chapter.compareTo(o.chapter);
        if(ret != 0) return ret;
         return Integer.compare(lineNumber,o.lineNumber);
    }
}
