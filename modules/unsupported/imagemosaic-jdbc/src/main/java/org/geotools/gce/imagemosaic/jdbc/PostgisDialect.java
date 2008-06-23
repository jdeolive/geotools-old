package org.geotools.gce.imagemosaic.jdbc;

public class PostgisDialect extends DBDialect {
    public PostgisDialect(Config config) {
        super(config);
    }

    protected String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    @Override
    protected String getRegisterSpatialStatement(String tn, String srs) {
        return " select AddGeometryColumn('" + tn + "','" +
        config.getGeomAttributeNameInSpatialTable() + "'," + srs + ",'" +
        getMulitPolygonSQLType() + "',2)";
    }

    @Override
    protected String getUnregisterSpatialStatement(String tn) {
        return "select DropGeometryColumn('" + tn + "','" +
        getConfig().getGeomAttributeNameInSpatialTable() + "')";
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
    protected String getCreateIndexStatement(String tn)
        throws Exception {
        return "CREATE INDEX IX_" + tn + " ON " + tn + " USING gist(" +
        getConfig().getGeomAttributeNameInSpatialTable() + ") ";
    }

    protected String getXMLConnectFragmentName() {
        return "connect.postgis.xml.inc";
    }
}
