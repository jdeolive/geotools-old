package org.geotools.gce.imagemosaic.jdbc;

import java.sql.Connection;


public class H2Setup extends JDBCSetup {
    public static H2Setup Singleton = new H2Setup();

    public H2Setup() {
        // //spatially enable the database
        //        
        // try {
        // run(getClass().getResourceAsStream("h2-drop.sql"));
        // } catch (Exception e) {}
        //        
        // try {
        // run(getClass().getResourceAsStream("h2.sql"));
        // } catch (Exception e) {
        // throw new RuntimeException(e);
        // }
    }

    protected String getDriverClassName() {
        return "org.h2.Driver";
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        return "jdbc:h2:target/h2/testdata";
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.h2.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BLOB";
    }

    @Override
    protected String getMulitPolygonSQLType() {
        // return "blob";
        return null;
    }

    @Override
    protected String getCreateSpatialTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL,");
        statement += (getConfig().getTileMinXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMinYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxYAttribute() + " DOUBLE NOT NULL,");
        statement += ("CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    protected String getCreateSpatialTableStatementJoined(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL,");
        statement += (getConfig().getTileMinXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMinYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getBlobAttributeNameInTileTable() + " " +
        getBLOBSQLType() + ",");
        statement += ("CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    protected void createIndex(String tn, Connection con)
        throws Exception {
        String stmt = "CREATE  INDEX IX_" + tn + " ON " + tn + "(" +
            getConfig().getTileMinXAttribute() + "," +
            getConfig().getTileMinYAttribute() + ")";

        con.prepareStatement(stmt).execute();
    }

    protected String getXMLConnectFragmentName() {
        return "connect.h2.xml.inc";
    }
}
