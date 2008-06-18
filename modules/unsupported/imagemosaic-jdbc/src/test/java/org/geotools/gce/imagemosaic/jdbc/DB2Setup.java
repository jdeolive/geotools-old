package org.geotools.gce.imagemosaic.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;


public class DB2Setup extends JDBCSetup {
    static final String SRSID = "EPSG:31297";

    // preparing db

    // db2 create db mosaic
    // db2se enable_db mosaic
    //
    public static DB2Setup Singleton = new DB2Setup();

    public DB2Setup() {
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

    private void preCreateActions() throws Exception {
        Connection con = getConnection();

        CallableStatement s = con.prepareCall(
                " {call db2gse.ST_enable_db(?,?,?) }");
        s.registerOutParameter(2, Types.INTEGER);
        s.registerOutParameter(3, Types.CHAR);
        s.setNull(1, Types.CHAR);
        s.executeUpdate();
        System.out.println(s.getInt(2) + "|" + s.getString(3));
        s.close();

        s = con.prepareCall(
                " {call db2gse.ST_create_srs(?,?,?,?,?,?,0,1,0,1,?,null,?,?) }");
        s.setString(1, SRSID);
        s.setInt(2, 31297);
        s.setDouble(3, -10000000.0);
        s.setDouble(4, 100.0);
        s.setDouble(5, -10000000.0);
        s.setDouble(6, 100.0);
        s.setString(7, "MGI_AUSTRIA_LAMBERT");
        s.registerOutParameter(8, Types.INTEGER);
        s.registerOutParameter(9, Types.CHAR);
        s.executeUpdate();
        System.out.println(s.getInt(8) + "|" + s.getString(9));
        s.close();

        con.commit();
        con.close();
    }

    @Override
    public void createAll() throws Exception {
        preCreateActions();
        super.createAll();
    }

    @Override
    public void createAllJoined() throws Exception {
        preCreateActions();
        super.createAllJoined();
    }

    @Override
    public void dropAll() throws Exception {
        super.dropAll();

        Connection con = getConnection();
        CallableStatement s = con.prepareCall(
                " {call db2gse.ST_drop_srs(?,?,?) }");
        s.registerOutParameter(2, Types.INTEGER);
        s.registerOutParameter(3, Types.CHAR);
        s.setString(1, SRSID);
        s.executeUpdate();
        System.out.println(s.getInt(2) + "|" + s.getString(3));
        s.close();
        con.commit();
        con.close();
    }

    @Override
    protected void registerSpatial(String tn, Connection con)
        throws Exception {
        CallableStatement s = con.prepareCall(
                " {call db2gse.ST_register_spatial_column(?,?,?,?,?,?) }");
        s.setNull(1, Types.CHAR);
        s.setString(2, tn);
        s.setString(3, getConfig().getGeomAttributeNameInSpatialTable());
        s.setString(4, SRSID);
        s.registerOutParameter(5, Types.INTEGER);
        s.registerOutParameter(6, Types.CHAR);
        s.executeUpdate();
        System.out.println(s.getInt(5) + "|" + s.getString(6));
        s.close();
    }

    @Override
    protected void unregisterSpatial(String tn, Connection con)
        throws Exception {
        CallableStatement s = con.prepareCall(
                " {call db2gse.ST_unregister_spatial_column(?,?,?,?,?) }");
        s.setNull(1, Types.CHAR);
        s.setString(2, tn);
        s.setString(3, getConfig().getGeomAttributeNameInSpatialTable());
        s.registerOutParameter(4, Types.INTEGER);
        s.registerOutParameter(5, Types.CHAR);
        s.executeUpdate();
        System.out.println(s.getInt(4) + "|" + s.getString(5));
        s.close();
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.db2.xml";
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
    protected void createIndex(String tn, Connection con)
        throws Exception {
        String stmt = "CREATE  INDEX IX_" + tn + " ON " + tn + "(" +
            getConfig().getGeomAttributeNameInSpatialTable() + ") " +
            " EXTEND USING db2gse.spatial_index (10000.0, 100000.0, 1000000.0)";
        con.prepareStatement(stmt).execute();
    }
}
