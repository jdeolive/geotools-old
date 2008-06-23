package org.geotools.gce.imagemosaic.jdbc;

public class DB2Dialect extends DBDialect {
    public DB2Dialect(Config config) {
        super(config);
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        if (host == null) {
            return "jdbc:db2:" + dbName;
        } else {
            return "jdbc:db2://" + host + ":" + port + "/" + dbName;
        }
    }

    protected String getDriverClassName() {
        return "com.ibm.db2.jcc.DB2Driver";
    }

    protected String getXMLConnectFragmentName() {
        return "connect.db2.xml.inc";
    }

    @Override
    protected String getRegisterSpatialStatement(String tn, String srs) {
        return "call db2gse.ST_register_spatial_column(null,'" + tn + "','" +
        config.getGeomAttributeNameInSpatialTable() + "','" + srs + "',?,?)";
    }

    @Override
    protected String getUnregisterSpatialStatement(String tn) {
        return "call db2gse.ST_unregister_spatial_column(null,'" + tn + "','" +
        config.getGeomAttributeNameInSpatialTable() + "',?,?)";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BLOB";
    }

    @Override
    protected String getMulitPolygonSQLType() {
        return "db2gse.st_multipolygon";
    }

    @Override
    protected String getCreateIndexStatement(String tn)
        throws Exception {
        return "CREATE  INDEX IX_" + tn + " ON " + tn + "(" +
        getConfig().getGeomAttributeNameInSpatialTable() + ") " +
        " EXTEND USING db2gse.spatial_index (10000.0, 100000.0, 1000000.0)";
    }
}
