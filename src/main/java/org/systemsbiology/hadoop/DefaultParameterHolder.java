package org.systemsbiology.hadoop;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.mapreduce.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.hadoop.DefaultParameterHolder
 *
 * @author Steve Lewis
 * @date 8/19/13
 */
public class DefaultParameterHolder implements ISetableParameterHolder {

    public static final String PARAMS_KEY = "org.systemsbiology.xtandem.params";
    public static final String PATH_KEY = "org.systemsbiology.xtandem.hdfs.basepath";
    @SuppressWarnings("UnusedDeclaration")
    public static final String FORCE_PATH_PREFIX_KEY = "org.systemsbiology.xtandem.hdfs.forcePathPrefix";
    @SuppressWarnings("UnusedDeclaration")
     public static final String HOST_KEY = "org.systemsbiology.xtandem.hdfs.host";
    @SuppressWarnings("UnusedDeclaration")
    public static final String HOST_PORT_KEY = "org.systemsbiology.xtandem.hdfs.port";
    @SuppressWarnings("UnusedDeclaration")
    public static final String HOST_PREFIX_KEY = "org.systemsbiology.xtandem.hostprefix";

    private static final List<IStreamOpener> gPreLoadOpeners =
            new ArrayList<IStreamOpener>();

    public static void addPreLoadOpener(IStreamOpener opener) {
        gPreLoadOpeners.add(opener);
    }

    public static IStreamOpener[] getPreloadOpeners() {
        return gPreLoadOpeners.toArray(new IStreamOpener[gPreLoadOpeners.size()]);
    }


    private static DefaultParameterHolder gInstance;

    /**
     * call to find if we need to build one
     *
     * @return possibly null instance
     */
    public static synchronized DefaultParameterHolder getInstance() {
        return gInstance;
    }

    /**
     * guarantee this is a singleton
     *
     * @param is
     * @param url
     * @param ctx
     * @return
     */
    public static synchronized DefaultParameterHolder getInstance(InputStream is, String url, Configuration ctx) {
        if (gInstance == null)
            gInstance = new DefaultParameterHolder(is, url, ctx);
        return gInstance;
    }


    @SuppressWarnings("UnusedDeclaration")
    public static DefaultParameterHolder loadFromContext(final TaskInputOutputContext context) {
        final Configuration configuration = context.getConfiguration();

        return loadFromConfiguration(context, configuration);
    }

    public static DefaultParameterHolder loadFromConfiguration(final TaskInputOutputContext context, final Configuration pConfiguration) {
        DefaultParameterHolder ret = DefaultParameterHolder.getInstance();
        if (ret != null)
            return ret;

        // note we are reading from hdsf
        IStreamOpener opener;
        try {
            opener = new HDFSStreamOpener(pConfiguration);
            addPreLoadOpener(opener);

        } catch (Exception e) {

            //noinspection ConstantConditions
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
            //   opener = new FileStreamOpener();
        }

        //noinspection UnusedDeclaration
        final String basedir = pConfiguration.get(PATH_KEY);
        final String paramsFile = pConfiguration.get(PARAMS_KEY);
        if (context != null) {
            if (context instanceof MapContext) {
                System.err.println("in mapper paramsFile = " + paramsFile);
            } else if (context instanceof ReduceContext) {
                System.err.println("in reducer paramsFile = " + paramsFile);

            } else {
                // Huh - who knows where we are
                System.err.println("in context " + context.getClass().getName() + " paramsFile = " + paramsFile);

            }
        }
        //     File params  =  getDistributedFile(paramsFile,context);
        InputStream is = opener.open(paramsFile);
        if (is == null)
            throw new IllegalStateException(
                    "cannot open parameters file " + ((HDFSStreamOpener) opener).buildFilePath(
                            paramsFile));
        ret = DefaultParameterHolder.getInstance(is, paramsFile, pConfiguration);


        return ret;
    }

    private final Map<String, String> m_PerformanceParameters = new HashMap<String, String>();
    private final Map<String, String> m_Parameters = new HashMap<String, String>();
    private final DelegatingFileStreamOpener m_Openers = new DelegatingFileStreamOpener();

//    public HadoopTandemMain(Configuration ctx )
//    {
//        super( );
//        m_Context = ctx;
//      }

//    public HadoopTandemMain(File pTaskFile,Configuration ctx)
//    {
//        super(pTaskFile);
//        m_Context = ctx;
//      }

    @SuppressWarnings("UnusedParameters")
    private DefaultParameterHolder(InputStream is, String url, Configuration ctx) {
        handleInputs(is);
    }

    @SuppressWarnings("UnusedDeclaration")
    public DefaultParameterHolder() {

    }


    public void handleInputs(final InputStream is) {
        try {
            Properties props = new Properties();
            props.load(new InputStreamReader(is));
            //noinspection unchecked
            for (String key : props.stringPropertyNames()) {
                m_Parameters.put(key, (String) props.get(key));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    private void setPredefinedParameter(String key, String value) {
        setParameter(key, value);
    }


    @SuppressWarnings("UnusedDeclaration")
    public void setPerformanceParameter(String key, String value) {
        m_PerformanceParameters.put(key, value);
    }

    /**
     * return a parameter configured in  default parameters
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getPerformanceParameter(String key) {
        return m_PerformanceParameters.get(key);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getPerformanceKeys() {
        Set<String> stx = m_PerformanceParameters.keySet();
        String[] ret = stx.toArray(new String[stx.size()]);
        Arrays.sort(ret);
        return ret;
    }

    /**
     * add new ways to open files
     */
    @SuppressWarnings("UnusedDeclaration")
    protected void initOpeners() {
        addOpener(new FileStreamOpener());
   //     addOpener(new StreamOpeners.ResourceStreamOpener(DefaultParameterHolder.class));
        for (IStreamOpener opener : getPreloadOpeners())
            addOpener(opener);
    }


    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(String fileName, Object... otherData) {
        return m_Openers.open(fileName, otherData);
    }

    public void addOpener(IStreamOpener opener) {
        m_Openers.addOpener(opener);
    }

    /**
     * set a parameter value
     *
     * @param key   !null key
     * @param value !null value
     */
    @Override
    public void setParameter(String key, String value) {
        m_Parameters.put(key, value);

    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     * @defaultValue what to set when the parameter is null
     */
    @Override
    public String getParameter(String key, String defaultValue) {
        if (m_Parameters.containsKey(key))
            return m_Parameters.get(key);
        else
            return defaultValue;
    }

    @Override
    public String[] getParameterKeys() {
        Set<String> stx = m_Parameters.keySet();
        String[] strings = stx.toArray(new String[stx.size()]);
        Arrays.sort(strings);
        return strings;
    }

    @Override
    public String[] getUnusedKeys() {
        //noinspection ConstantIfStatement
        if (true) throw new UnsupportedOperationException("Fix This");
        return new String[0];
    }


    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public String getParameter(String key) {
        return m_Parameters.get(key);
    }

    /**
     * get all keys of the foro key , key 1, key 2 ...
     *
     * @param key !null key
     * @return non-null array
     */
    @Override
    public String[] getIndexedParameters(String key) {
        //noinspection ConstantIfStatement
        if (true) throw new UnsupportedOperationException("Fix This");
        return new String[0];
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Integer getIntParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return new Integer(val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Boolean getBooleanParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        if ("yes".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("no".equalsIgnoreCase(val))
            return Boolean.FALSE;
        if ("true".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("false".equalsIgnoreCase(val))
            return Boolean.FALSE;
        throw new IllegalArgumentException("value must be yes or no or true or false - not " + val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Float getFloatParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return new Float(val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Double getDoubleParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return new Double(val);
    }

    /**
     * return an enum from the value of the parameter
     *
     * @param key !nulkl key
     * @param cls !null expected class
     * @param <T> type of enum
     * @return possibly null value - null says parameter does not exist
     */
    @Override
    public <T extends Enum<T>> T getEnumParameter(String key, Class<T> cls) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return Enum.valueOf(cls, val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Integer getIntParameter(String key, int defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        return new Integer(val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Boolean getBooleanParameter(String key, boolean defaultValue) {
        String val = getParameter(key);
        if (val == null)
            return defaultValue;
        if ("yes".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("no".equalsIgnoreCase(val))
            return Boolean.FALSE;
        if ("true".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("false".equalsIgnoreCase(val))
            return Boolean.FALSE;
        throw new IllegalArgumentException("value must be yes or no or true or false - not " + val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Float getFloatParameter(String key, float defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        try {
            return new Float(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Double getDoubleParameter(String key, double defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        return new Double(val);
    }

    /**
     * return an enum from the value of the parameter
     *
     * @param key !nulkl key
     * @param cls !null expected class
     * @param <T> type of enum
     * @return possibly null value - null says parameter does not exist
     */
    @Override
    public <T extends Enum<T>> T getEnumParameter(String key, Class<T> cls, T defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        return Enum.valueOf(cls, val);
    }

}
