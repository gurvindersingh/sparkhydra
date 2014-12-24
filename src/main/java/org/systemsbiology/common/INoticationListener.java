package org.systemsbiology.common;

import java.io.*;

/**
 * org.systemsbiology.common.INoticationListener
 *
 * User: steven
 * Date: 11/17/11
 */
public interface INoticationListener  extends Serializable {
    public static final INoticationListener[] EMPTY_ARRAY = {};

    /**
     * notification of something
     * @param data
     */
    public void onNotification(Object... data );
}
