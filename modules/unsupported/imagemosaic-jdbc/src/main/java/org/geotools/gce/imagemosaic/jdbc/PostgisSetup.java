package org.geotools.gce.imagemosaic.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class PostgisSetup extends JDBCSetup {

	static protected int SRSID=4326; 
	
    public PostgisSetup(Config config) {
		super(config);
	}


    protected String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    @Override
    protected String[] getTileTableNames() {
        return new String[] {  /* "tiles0" , */"tiles1", "tiles2", "tiles3" };
    }

    @Override
    protected String[] getSpatialTableNames() {
        return new String[] {  /* "spatial0", */"spatial1", "spatial2", "spatial3" };
    }

    @Override
    protected void registerSpatial(String tn, Connection con)
        throws Exception {
        // AddGeometryColumn(varchar, varchar, varchar, integer, varchar,
        // integer)
        PreparedStatement s = con.prepareStatement(
                " select AddGeometryColumn(?,?,?,?,?) ");
        s.setString(1, tn.toLowerCase());
        s.setString(2, getConfig().getGeomAttributeNameInSpatialTable());
        s.setInt(3, SRSID);
        s.setString(4, getMulitPolygonSQLType());
        s.setInt(5, 2);

        ResultSet r = s.executeQuery();

        while (r.next())
            System.out.println(r.getString(1));

        s.close();
    }

    

    
    @Override
    protected void unregisterSpatial(String tn, Connection con)
        throws Exception {
        PreparedStatement s = con.prepareStatement(
                "select DropGeometryColumn(?, ?)");
        s.setString(1, tn.toLowerCase());
        s.setString(2, getConfig().getGeomAttributeNameInSpatialTable());

        try {
            ResultSet r = s.executeQuery();

            while (r.next())
                System.out.println(r.getString(1));
        } catch (Exception e) {
        }

        s.close();
    }

    @Override
    protected String getCreateSpatialTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" (" + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL ");
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    protected String getCreateSpatialTableStatementJoined(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" (" + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL ");
        statement += ("," + getConfig().getBlobAttributeNameInTileTable() +
        " " + getBLOBSQLType());
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.postgis.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BYTEA";
    }

    @Override
    protected String getMulitPolygonSQLType() {
        return "MULTIPOLYGON";
    }

    @Override
    protected String getDoubleSQLType() {
        return "FLOAT8";
    }

    @Override
    protected String getCreateIndexStatement(String tn) throws Exception {
        
        return "CREATE INDEX IX_" + tn + " ON " + tn + " USING gist(" +
            getConfig().getGeomAttributeNameInSpatialTable() + ") ";        
    }

    protected String getXMLConnectFragmentName() {
        return "connect.postgis.xml.inc";
    }
}
