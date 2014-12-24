package com.lordjoe.utilities;

import java.io.*;

/**
 * com.lordjoe.utilities.ElapsedTimer
 *
 * org.systemsbiology.ElapsedTimer
  *   Helper class that can time operations
  *   Usege
  *    ElapsedTimer et = new ElapsedTimer()
  *    < Operation to time></>
  *    String timerMessage = et.formatElapsed("Operation 1 "); // adds time as a string
  *    et.reset(); // back to 0
  *    < Operation2 to time></>
  *    String timerMessage2 = et.formatElapsed("Operation 2"); // adds time as a string
  * @author Steve Lewis
   */


 public class ElapsedTimer implements Serializable {

        private long m_Start;

        public ElapsedTimer()
        {
             m_Start = System.currentTimeMillis();
        }

        /**
         * get when the operation started
         * @return  as above
         */
        public long getStart()
        {
            return m_Start;
        }

        /**
         * return start time to current
         */
        public void reset()
        {
             m_Start = System.currentTimeMillis();
        }

        /**
         * get time elapsed since start
         * @return  as above
          */
        public long getElapsedMillisec()
        {
            return  System.currentTimeMillis() - m_Start;
        }

        /**
         * return elapsed time as a string
         * @return  as above
         */
        public String formatElapsed( )
        {
            return formatElapsed("");
        }

        /**
         * return elapsed time as a string  with message included
          * @param message  !null message
         * @return  as above
         */
        public String formatElapsed(String message)
        {
             long elapsed = getElapsedMillisec();
            double elapsedSec = elapsed / 1000.0;
            if(elapsedSec < 1000 )
                 return message+ " in " + Util.formatDouble(elapsedSec,3 ) + " sec";
            if(elapsedSec < 10000 )
                 return message+ " in " + Util.formatDouble(elapsedSec / 60,3 ) + " min";
            return message + " in " +Util.formatDouble(elapsedSec / (60 * 60),3 ) + " hour";
         }

        /**
         * print elapsed time as a string  with message included on SYstem.out
         * @param message  !null message
         */
        public void showElapsed(String message)
        {
            showElapsed(message,System.out);

        }

        /**
         * print elapsed time as a string  with message included on out
           * @param out  !null PrintStream
         */
        public void showElapsed(PrintStream out)    {
            out.println(formatElapsed());
        }

        /**
         * print elapsed time as a string  with message included on  out
           * @param message  !null message
         * @param out  !null print stream
           */
        public void showElapsed(String message,PrintStream out)    {
            out.println(formatElapsed(message));
        }

        /**
          * print elapsed time as a string  with message included on out
            * @param out  !null PrintWriter
          */
        public void showElapsed(PrintWriter out)    {
            out.println(formatElapsed());
        }

        /**
         * print elapsed time as a string  with message included on  out
           * @param message  !null message
         * @param out  !null PrintWriter
           */
        public void showElapsed(String message,PrintWriter out)    {
            out.println(formatElapsed(message));
        }

}
