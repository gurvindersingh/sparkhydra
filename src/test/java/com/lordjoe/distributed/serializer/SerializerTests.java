package com.lordjoe.distributed.serializer;

import org.junit.*;

import javax.annotation.*;
import java.util.*;

/**
 * com.lordjoe.distributed.serializer.SerializerTests
 * User: Steve
 * Date: 9/19/2014
 */
public class SerializerTests {

    public static final Random RND = new Random();

    /**
     * this class can have a serializer automatically built
     */
    public static class Point {
        public final int x;
        public final int y;

        public Point(final int pX, final int pY) {
            x = pX;
            y = pY;
        }

        public Point(final String s) {
            String[] items = s.split(",");
            x = Integer.parseInt(items[0]);
            y = Integer.parseInt(items[1]);
        }

        public String toString() {
            return x + "," + y;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Point point = (Point) o;

            if (x != point.x) return false;
            if (y != point.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }

    /**
     * no string constructor so we need to register a serializer
     */
    public static class DumbPoint {
        public final int x;
        public final int y;

        public DumbPoint(final int pX, final int pY) {
            x = pX;
            y = pY;
        }

        public String toString() {
             return x + "," + y;
         }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DumbPoint point = (DumbPoint) o;

            if (x != point.x) return false;
            if (y != point.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }


    @Test
    public void testPoint() throws Exception {
        // use reflection to make a serializer for an unknown class
        IStringSerializer serializer = StringSerializers.getSerializer(Point.class);
        for (int i = 0; i < 1000; i++) {
            Point p = buildPoint();
            String s1 = serializer.asString(p);
            Point p2 = (Point)serializer.fromString(s1);
            Assert.assertEquals(p,p2);
            Assert.assertEquals(s1,p2.toString());
           }

    }


    @Test
    public void testDumbPoint() throws Exception {
        IStringSerializer<DumbPoint> ds = new IStringSerializer<DumbPoint>() {
            @Override
            public Class<? extends DumbPoint> getSerializedClass() {
                return DumbPoint.class;
            }

            @Nonnull
            @Override
            public String asString(@Nonnull final DumbPoint t) {
               return t.x + "," + t.y;

            }

            @Override
            public DumbPoint fromString(@Nonnull final String s) {
                String[] items = s.split(",");
               int x = Integer.parseInt(items[0]);
               int y = Integer.parseInt(items[1]);
                return new DumbPoint(x,y);
            }
        };
        // better have no serializer
        try {
            IStringSerializer serializer = StringSerializers.getSerializer(DumbPoint.class);
            Assert.fail("no serializer registered yet");
        }
        catch (Exception e) {  // we expect an exception
         }

         // register a serializer
        StringSerializers.registerSerializer(DumbPoint.class,ds);

        // now look it up
        IStringSerializer serializer = StringSerializers.getSerializer(DumbPoint.class);
        for (int i = 0; i < 1000; i++) {
            DumbPoint p = new DumbPoint(RND.nextInt(1000),RND.nextInt(1000));
            String s1 = serializer.asString(p);
            DumbPoint p2 = (DumbPoint)serializer.fromString(s1);
            Assert.assertEquals(p,p2);
            Assert.assertEquals(s1,p2.toString());
           }

    }

    /**
     * make a random point
     * @return
     */
    private Point buildPoint() {
        return new Point(RND.nextInt(1000),RND.nextInt(1000));
    }
}
