package org.geotools.gce.imagemosaic.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;


public class OracleSetup extends JDBCSetup {
    static final int SRSID = 31287;
    public static OracleSetup Singleton = new OracleSetup();

    public OracleSetup() {
    }

    protected String getDriverClassName() {
        return "oracle.jdbc.OracleDriver";
    }

    protected String getJDBCUrl(String host, Integer port, String dbName) {
        return "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
    }

    @Override
    protected String getDoubleSQLType() {
        return "DOUBLE PRECISION";
    }

    @Override
    protected void registerSpatial(String tn, Connection con)
        throws Exception {
        String geomAttr = getConfig()
                              .getGeomAttributeNameInSpatialTable();
        String statementString = "INSERT INTO user_sdo_geom_metadata (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID )" +
            "VALUES('" + tn + "','" + geomAttr + "'," +
            "MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',0,1000000,0.1),MDSYS.SDO_DIM_ELEMENT('Y',0,1000000,0.1))," +
            SRSID + ")";

        PreparedStatement s = con.prepareStatement(statementString);
        s.execute();
        s.close();
    }

    @Override
    protected void unregisterSpatial(String tn, Connection con)
        throws Exception {
        String geomAttr = getConfig().getGeomAttributeNameInSpatialTable();
        String statementString = "DELETE FROM user_sdo_geom_metadata WHERE TABLE_NAME='" +
            tn + "' AND COLUMN_NAME='" + geomAttr + "'";
        PreparedStatement s = con.prepareStatement(statementString);
        s.execute();
        s.close();
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.oracle.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BLOB";
    }

    @Override
    protected String getMulitPolygonSQLType() {
        return "MDSYS.SDO_GEOMETRY";
    }

    @Override
    protected void createIndex(String tn, Connection con)
        throws Exception {
        String stmt = "CREATE INDEX IX_" + tn + " ON " + tn + "(" +
            getConfig().getGeomAttributeNameInSpatialTable() +
            ") INDEXTYPE IS MDSYS.SPATIAL_INDEX";
        con.prepareStatement(stmt).execute();
    }

    protected String getXMLConnectFragmentName() {
        return "connect.oracle.xml.inc";
    }
}
