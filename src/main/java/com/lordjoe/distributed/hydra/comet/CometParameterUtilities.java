package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.utilities.*;
import org.systemsbiology.hadoop.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometParameterUtilities
 * User: steven
 * Date: 4/2/13
 */
public class CometParameterUtilities {
    public static final CometParameterUtilities[] EMPTY_ARRAY = {};

    /**
     * read a comet.params file
     * @param hdr
     * @param is
     */
    public static void parseParameters(ISetableParameterHolder hdr,InputStream is) {
        String[] strings = FileUtilities.readInLines(new InputStreamReader(is));
        parseParameters(hdr, strings);
    }

    /**
     * read a comet.params file
      * @param hdr
     * @param strings
     */
    public static void parseParameters(ISetableParameterHolder hdr, String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            String line = strings[i];
            handleParameterLine(hdr,line);
        }
    }

    /**
     * handle a line which may be of the form name =  value
     * @param hdr - holder for parameters
     * @param line input line
     */
    public static void handleParameterLine(ISetableParameterHolder hdr, String line) {
        line = line.trim();
        if(line.length() == 0)
            return;
        if(line.startsWith("#"))
            return; // this is a comment
        int index = line.indexOf("#");
        if(index > 0)
            line = line.substring(0,index).trim();
        if(!line.contains("="))
            return; // not a parameter

        String[] items = line.split("=");
        if(items.length < 2)
            return; // huh???
        String paramName = items[0].trim();
        String value = items[1].trim();
        if(paramName.length() == 0)
            return;
        if(value.length() == 0)
            return;
        hdr.setParameter(paramName,value);
    }

}
