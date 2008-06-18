package org.geotools.gce.imagemosaic.jdbc;

import java.sql.Connection;


public class MySqlSetup extends JDBCSetup {
    public static MySqlSetup Singleton = new MySqlSetup();

    public MySqlSetup() {
    }

    protected String getDriverClassName() {
        return "com.mysql.jdbc.Driver";
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName;
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.mysql.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "LONGBLOB";
    }

    @Override
    protected String getMulitPolygonSQLType() {
        return "MULTIPOLYGON";
    }

    @Override
    protected void createIndex(String tn, Connection con)
        throws Exception {
        //    	
        // String stmt = "ALTER TABLE "+tn + " MODIFY
        // "+getConfig().getGeomAttributeNameInSpatialTable() + " "
        // + getMulitPolygonSQLType() + " NOT NULL";
        // con.prepareStatement(stmt).execute();
        String stmt = "CREATE SPATIAL INDEX IX_" + tn + " ON " + tn + "(" +
            getConfig().getGeomAttributeNameInSpatialTable() + ") ";
        con.prepareStatement(stmt).execute();
    }

    protected String getXMLConnectFragmentName() {
        return "connect.mysql.xml.inc";
    }
}
