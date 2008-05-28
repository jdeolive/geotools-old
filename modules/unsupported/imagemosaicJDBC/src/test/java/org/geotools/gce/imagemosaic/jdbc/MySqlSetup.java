package org.geotools.gce.imagemosaic.jdbc;

import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;


public class MySqlSetup extends JDBCSetup {
    public static MySqlSetup Singleton = new MySqlSetup();

    public MySqlSetup() {
    }

    @Override
    public String getConfigUrl() {
        return "file:src/test/resources/oek.mysql.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "LONGBLOB";
    }

    @Override
    protected String getToGemoetryClause(Geometry g) {
        return "GeomFromText('" + g.toText() + "',31287)";
    }

    @Override
    protected String getMulitPolygonSQLType() {
        return "MULTIPOLYGON";
    }

    @Override
    protected void createIndex(String tn, Connection con)
        throws Exception {
//    	
//    	String stmt = "ALTER TABLE "+tn + " MODIFY  "+getConfig().getGeomAttributeNameInSpatialTable() + " "
//		+ getMulitPolygonSQLType() + " NOT NULL"; 
//    	con.prepareStatement(stmt).execute();   

    	String stmt = "CREATE SPATIAL INDEX IX_" + tn + " ON " + tn + "(" +
            getConfig().getGeomAttributeNameInSpatialTable() + ") ";
        con.prepareStatement(stmt).execute();
    }
}
