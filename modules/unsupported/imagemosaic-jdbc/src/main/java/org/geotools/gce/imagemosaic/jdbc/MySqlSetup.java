package org.geotools.gce.imagemosaic.jdbc;



public class MySqlSetup extends JDBCSetup {

    public MySqlSetup(Config config) {
		super(config);
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
    protected String getCreateIndexStatement(String tn) throws Exception {            	
        // String stmt = "ALTER TABLE "+tn + " MODIFY
        // "+getConfig().getGeomAttributeNameInSpatialTable() + " "
        // + getMulitPolygonSQLType() + " NOT NULL";
        // con.prepareStatement(stmt).execute();
        return "CREATE SPATIAL INDEX IX_" + tn + " ON " + tn + "(" +
            getConfig().getGeomAttributeNameInSpatialTable() + ") ";
        
    }

    protected String getXMLConnectFragmentName() {
        return "connect.mysql.xml.inc";
    }
    

}
