package org.systemsbiology.xtandem.hadoop;

import org.junit.*;

import java.io.*;

/**
 * org.systemsbiology.hadoop.TestHadoopParser
 * User: steven
 * Date: 5/31/11
 */
public class TestHadoopParser {
    public static final TestHadoopParser[] EMPTY_ARRAY = {};

    public static final String CORE_SITE =
            "<?xml version=\"1.0\"?>\n" +
                    "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>\n" +
                    "\n" +
                    "<!-- Put site-specific property overrides in this file. -->\n" +
                    "\n" +
                    "<configuration>\n" +
                    "<property>\n" +
                    "   <name>fs.default.name</name>\n" +
                    "   <value>hdfs://glados:9000</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "<property>\n" +
                    "   <name>fs.inmemory.size.mb</name>\n" +
                    "   <value>200</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "<!--<property>\n" +
                    "   <name>mapreduce.task.io.sort.factor</name>\n" +
                    "   <value>50</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "<property>\n" +
                    "   <name>mapreduce.task.io.sort.mb</name>\n" +
                    "   <value>100</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>-->\n" +
                    "<property>\n" +
                    "   <name>io.file.buffer.size</name>\n" +
                    "   <value>131072</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "</configuration>";

    public static final String MAP_RED_SITE =
            "<?xml version=\"1.0\"?>\n" +
                    "<?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>\n" +
                    "\n" +
                    "<!-- Put site-specific property overrides in this file. -->\n" +
                    "\n" +
                    "<configuration>\n" +
                    "<property>\n" +
                    "   <name>mapred.job.tracker</name>\n" +
                    "   <value>glados:9001</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "<property>\n" +
                    "   <name>mapred.child.jvm.opts</name>\n" +
                    "   <value>-Xmx768m</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "<property>\n" +
                    "   <name>mapred.tasktracker.map.tasks.maximum</name>\n" +
                    "   <value>8</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "<property>\n" +
                    "   <name>mapred.tasktracker.reduce.tasks.maximum</name>\n" +
                    "   <value>8</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "<property>\n" +
                    "   <name>mapred.local.dir</name>\n" +
                    "   <value>/hdfs1/tmp,/hdfs2/tmp,/hdfs3/tmp,/hdfs4/tmp</value>\n" +
                    "   <final>true</final>\n" +
                    "</property>\n" +
                    "</configuration>";

    @Test
      public void testMapRedParse() throws Exception {
          HadoopConfigurationPropertySet ps = parseHadoopXML(MAP_RED_SITE);

          Assert.assertEquals(5, ps.getHadoopProperties().length);


          testPropertiesMapRedSite(ps);

      }
    @Test
      public void testCoreParse() throws Exception {
          HadoopConfigurationPropertySet ps = parseHadoopXML(CORE_SITE);

          Assert.assertEquals(3, ps.getHadoopProperties().length);


          testPropertiesCoreSite(ps);

      }

    @Test
    public void testMultiFileParse() throws Exception {
        HadoopConfigurationPropertySet ps = new HadoopConfigurationPropertySet();
        HadoopConfigurationPropertySet ps1 = parseHadoopXML(MAP_RED_SITE);
        ps.addProperties(ps1);

        ps1 = parseHadoopXML(CORE_SITE);
        ps.addProperties(ps1);

        Assert.assertEquals(8, ps.getHadoopProperties().length);


        testPropertiesMapRedSite(ps);
        testPropertiesCoreSite(ps);

    }

    private HadoopConfigurationPropertySet parseHadoopXML(String xml) {
        InputStream is = new ByteArrayInputStream(xml.getBytes());

        HadoopConfigurationPropertySet ps = XTandemHadoopUtilities.parseHadoopProperties(is);
        Assert.assertNotNull(ps);
        return ps;
    }



    public static void testPropertiesCoreSite(final HadoopConfigurationPropertySet pPs) {
          HadoopConfigurationProperty prop;
        prop = pPs.getHadoopProperty("fs.default.name");
        Assert.assertEquals("hdfs://glados:9000", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

        prop = pPs.getHadoopProperty("fs.inmemory.size.mb");
        Assert.assertEquals("200", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

        prop = pPs.getHadoopProperty("io.file.buffer.size");
        Assert.assertEquals("131072", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

       }


     public static void testPropertiesMapRedSite(final HadoopConfigurationPropertySet pPs) {
          HadoopConfigurationProperty prop;
        prop = pPs.getHadoopProperty("mapred.local.dir");
        Assert.assertEquals("/hdfs1/tmp,/hdfs2/tmp,/hdfs3/tmp,/hdfs4/tmp", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

        prop = pPs.getHadoopProperty("mapred.tasktracker.reduce.tasks.maximum");
        Assert.assertEquals("8", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

        prop = pPs.getHadoopProperty("mapred.tasktracker.map.tasks.maximum");
        Assert.assertEquals("8", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

        prop = pPs.getHadoopProperty("mapred.child.jvm.opts");
        Assert.assertEquals("-Xmx768m", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

        prop = pPs.getHadoopProperty("mapred.job.tracker");
        Assert.assertEquals("glados:9001", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());

        prop = pPs.getHadoopProperty("mapred.job.tracker");
        Assert.assertEquals("glados:9001", prop.getValue());
        Assert.assertEquals(true, prop.isFinal());
    }
}
