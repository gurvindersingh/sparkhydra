package com.lordjoe.distributed.chapter_and_verse;

import java.io.*;

/**
 * com.lordjoe.distributed.chapter_and_verse.LineAndLocation
 * User: Steve
 * Date: 9/14/2014
 */ // one line in the book
public class LineAndLocation implements Serializable {
    public final String chapter;
    public final int lineNumber;
    public final String line;

    public LineAndLocation(final String pChapter, final int pLineNumber, final String pLine) {
        chapter = pChapter;
        lineNumber = pLineNumber;
        line = pLine;
    }

    public String toString() {
        return chapter + ":" + lineNumber + " - " + line;
    }
}
