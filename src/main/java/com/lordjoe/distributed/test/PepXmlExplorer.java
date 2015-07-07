package com.lordjoe.distributed.test;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.test.PepXmlExplorer
 * User: Steve
 * Date: 4/15/2015
 */
public class PepXmlExplorer {

    public static void getSpectraQueries(LineNumberReader rdr,List<String> queries) {
        try {
            String line = rdr.readLine();
            while(line != null)  {
                if(line.contains("<spectrum_query spectrum=")) {
                    addQuery(rdr,line,queries) ;
                }
                line = rdr.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private static void addQuery(final LineNumberReader rdr,  String line, final List<String> pQueries) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(line) ;
            sb.append("\n") ;
            while(line != null)  {
                   line = rdr.readLine();
                if(line.contains("</spectrum_query>"))
                    break;
                sb.append(line) ;
              sb.append("\n") ;
             }

            sb.append(line) ;
              sb.append("\n") ;

            pQueries.add(sb.toString());
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    private static void writeGoodQuery(final String pGoodquery) {
        System.out.println(pGoodquery);

     }

    public static void main(String[] args) throws Exception {
        File inp = new File(args[0]);
        LineNumberReader rdr = new LineNumberReader(new FileReader(inp));
        List<String>   queries = new ArrayList<String>();
        List<String>   goodqueries = new ArrayList<String>();
        getSpectraQueries(  rdr, queries);
        for (String query : queries) {
            if(query.contains("E-005"))
                goodqueries.add(query) ;
            if(query.contains("E-004"))
                goodqueries.add(query) ;
            if(query.contains("E-003"))
                goodqueries.add(query) ;
         }

        for (String goodquery : goodqueries) {
               writeGoodQuery(goodquery);
        }
    }


}
