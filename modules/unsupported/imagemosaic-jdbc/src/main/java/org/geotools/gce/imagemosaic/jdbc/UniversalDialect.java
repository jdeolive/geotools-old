package org.geotools.gce.imagemosaic.jdbc;

public class UniversalDialect extends DBDialect {
    public UniversalDialect(Config config) {
        super(config);
    }

    protected String getDriverClassName() {
        return "org.h2.Driver";
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        return "jdbc:h2:target/h2/testdata";
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
    protected String getCreateIndexStatement(String tn)
        throws Exception {
        return "CREATE  INDEX IX_" + tn + " ON " + tn + "(" +
        getConfig().getTileMinXAttribute() + "," +
        getConfig().getTileMinYAttribute() + ")";
    }

    protected String getXMLConnectFragmentName() {
        return "connect.h2.xml.inc";
    }
}
