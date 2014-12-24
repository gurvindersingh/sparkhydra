package com.lordjoe.distributed.util;

import java.util.*;

/**
 * com.lordjoe.distributed.util.XYPoint
 * User: Steve
 * Date: 8/25/2014
 */
public class XYPoint {
    public final int x;
    public final int y;

    public XYPoint(final int pX, final int pY) {
        x = pX;
        y = pY;
    }
    public XYPoint(String s) {
        StringTokenizer stk = new StringTokenizer(s,",");
        x = Integer.parseInt(stk.nextToken());
        y = Integer.parseInt(stk.nextToken());
    }

    @Override public String toString() {
        return  Integer.toString(x) + ","  + y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final XYPoint xyPoint = (XYPoint) o;

        if (x != xyPoint.x) return false;
        if (y != xyPoint.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
