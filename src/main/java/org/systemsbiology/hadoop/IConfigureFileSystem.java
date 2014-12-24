package org.systemsbiology.hadoop;

import org.apache.hadoop.conf.*;

/**
 * org.systemsbiology.hadoop.IConfigureFileSystem
 * designed to set up a FileSystem in a hadoop configuration
 *  specifically to allow jobs to use a non-default file systen such as hdfs or amazon s3
 * User: steven
 * Date: 12/12/11
 */
public interface IConfigureFileSystem {
    public static final IConfigureFileSystem[] EMPTY_ARRAY = {};

    /**
     * default does nothing
     */
    public static final IConfigureFileSystem NULL_CONFIGURE_FILE_SYSTEM = new NullConfigureFileSystem();
    /**
     * set up a proper file system in the configuration
     * @param conf !null  configuration
     * @param otherData algorithm specific other data
     */
    public void configureFileSystem( Configuration conf, Object... otherData);

    public static class NullConfigureFileSystem implements IConfigureFileSystem {
        private NullConfigureFileSystem() {
        }

        /**
         * do nothing - use the default
         * @param conf !null  configuration
         * @param otherData algorithm specific other data
         */
        @Override
        public void configureFileSystem(final Configuration conf, final Object... otherData) {

        }
    }

}
