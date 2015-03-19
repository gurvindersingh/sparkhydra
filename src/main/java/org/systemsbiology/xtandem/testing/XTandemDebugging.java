package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.XTandemDebugging
 *
 * @author Steve Lewis
 * @date Mar 4, 2011
 */
public class XTandemDebugging
{
    public static XTandemDebugging[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = XTandemDebugging.class;

    private static boolean gDebugging;
    private static XTandemDebugging gInstance;

    public static synchronized XTandemDebugging getInstance()
    {
         return gInstance;
    }

    public static boolean isDebugging()
    {
        return gDebugging;
    }

    public static void loadXTandemValues(String fileName)
    {

        if (!isDebugging())
            return;
        XTandemDebugging td = getInstance();
        final DebugValues dv = td.getXTandemValues();
        dv.loadDebugValues(fileName);
    }

    public static void setDebugging(boolean pDebugging, XTandemMain main)
    {
        gDebugging = pDebugging;
        if (pDebugging && gInstance == null)
            gInstance = new XTandemDebugging(main);
    }

    public static void addIntermediateState(String id, String msg, IMeasuredSpectrum spec)
    {
        if (!isDebugging())
            return;
        final DebugValues dv = getInstance().getValues();
        dv.addMeasuredSpectrums(id, msg, spec);
    }

    public static void addIntermediateState(String id, String msg, ITheoreticalSpectrum spec)
    {
        if (!isDebugging())
            return;
        final DebugValues dv = getInstance().getValues();
        dv.addTheoreticalSpectrums(id, msg, spec);
    }


    public static void addDebugDotProduct(String id, DebugDotProduct spec)
    {
        if (!isDebugging())
            return;
        final DebugValues dv = getInstance().getValues();
        dv.addDebugDotProduct(id, spec);
    }

    public static DebugValues getComparisonValues( )
    {
        if (!isDebugging())
            throw new IllegalStateException("problem");
        final DebugValues dv = getInstance().getXTandemValues();
        return dv;
    }

    public static DebugValues getLocalValues( )
    {
        if (!isDebugging())
            throw new IllegalStateException("problem");
        XTandemDebugging instance = getInstance();
        final DebugValues dv = instance.getValues();
        return dv;
    }

    private Map<String, DebugDotProduct> m_DotProducts = new HashMap<String, DebugDotProduct>();
    private final XTandemMain m_Params;
    private final DebugValues m_Values;
    private final DebugValues m_XTandemValues;

    private XTandemDebugging(XTandemMain main)
    {
        m_Params = main;
        m_Values = new DebugValues(m_Params);
        m_XTandemValues = new DebugValues(m_Params);
    }

    public IMainData getParams()
    {
        return m_Params;
    }

    public DebugValues getValues()
    {
        return m_Values;
    }

    public DebugValues getXTandemValues()
    {
        return m_XTandemValues;
    }

    public void addDotProducts(String key, DebugDotProduct added)
    {
        m_DotProducts.put(key, added);
    }


    public void removeDotProducts(String removed)
    {
        m_DotProducts.remove(removed);
    }

    public DebugDotProduct[] getDotProducts()
    {
        return m_DotProducts.values().toArray(new DebugDotProduct[0]);
    }

    public DebugDotProduct getDotProducts(String key)
    {
        return m_DotProducts.get(key);
    }

}
