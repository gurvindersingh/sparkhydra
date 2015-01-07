package org.systemsbiology.xtandem.hadoop;

import org.systemsbiology.xml.*;

import java.sql.*;

/**
 * org.systemsbiology.hadoop.HBaseTests
 * User: steven
 * Date: 4/18/11
 */
public class HBaseTests {
    public static final HBaseTests[] EMPTY_ARRAY = {};

  //  @Test
    public void testHBaseJDBC() throws Exception
    {
  //      Class foo = org.apache.hadoop.hbase.jdbc.Driver.class;

        Class.forName("org.apache.hadoop.hbase.jdbc.Driver");

        // Get a connection with an HTablePool size of 10
    //    Connection conn = DriverManager.getConnection("jdbc:hbql;maxtablerefs=10");

        // or
        Connection conn = DriverManager.getConnection("jdbc:hbql;maxtablerefs=10;hbase.master=Glados:60000");


        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE table12 (f1(), f3()) IF NOT tableexists('table12')");

        stmt.execute("CREATE TEMP MAPPING sch9 FOR TABLE table12"
                     + "("
                     + "keyval key, "
                     + "f1 ("
                     + "    val1 string alias val1, "
                     + "    val2 string alias val2 "
                     + "), "
                     + "f3 ("
                     + "    val1 int alias val5, "
                     + "    val2 int alias val6 "
                     + "))");

        ResultSet rs = stmt.executeQuery("select * from sch9");

        while (rs.next()) {
            int val5 = rs.getInt("val5");
            int val6 = rs.getInt("val6");
            String val1 = rs.getString("val1");
            String val2 = rs.getString("val2");

            XMLUtilities.outputText("val5: " + val5);
            XMLUtilities.outputText(", val6: " + val6);
            XMLUtilities.outputText(", val1: " + val1);
            System.out.println(", val2: " + val2);
        }

        rs.close();

        stmt.execute("DISABLE TABLE table12");
        stmt.execute("DROP TABLE table12");
        stmt.close();

        conn.close();

    }

}
