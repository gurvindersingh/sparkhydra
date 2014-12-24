package com.lordjoe.distributed.chapter_and_verse;

import java.io.*;

/**
* com.lordjoe.distributed.chapter_and_verse.LineAndLocationMatch
* User: Steve
* Date: 9/14/2014
*/ // one line in the book
public class LineAndLocationMatch  implements Serializable {
    public LineAndLocation thisLine;
    public LineAndLocation bestFit;
    public double similarity;

    public LineAndLocationMatch(final LineAndLocation pThisLine ) {
        thisLine = pThisLine;
        similarity = -1;
    }

    public String toString() {
          return thisLine + ":" + similarity;
      }

}
