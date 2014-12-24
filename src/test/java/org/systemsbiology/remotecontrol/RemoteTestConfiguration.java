package org.systemsbiology.remotecontrol;

import java.util.*;

/**
 * org.systemsbiology.remotecontrol.RemoteTestConfiguration
 * User: Steve
 * Date: 8/25/11
 */
public class RemoteTestConfiguration {
    public static final RemoteTestConfiguration[] EMPTY_ARRAY = {};

    private static Map<String, Boolean> gConditionToAvailability = new HashMap<String, Boolean>();

    public static boolean isHDFSAccessible() {

        String connStr = RemoteUtilities.getHost() + ":" + RemoteUtilities.getUser() + ":" + RemoteUtilities.getPassword();
        Boolean ret = gConditionToAvailability.get(connStr);
        if (ret == null) {
            try {
                new FTPWrapper(RemoteUtilities.getUser(), RemoteUtilities.getPassword(), RemoteUtilities.getHost());
                ret = Boolean.TRUE;
            } catch (Exception e) {
                ret = Boolean.FALSE;

            }
            gConditionToAvailability.put(connStr, ret);
        }
        return ret;
    }

}
