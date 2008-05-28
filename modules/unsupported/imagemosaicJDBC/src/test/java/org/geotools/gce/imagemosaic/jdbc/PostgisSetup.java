package org.geotools.gce.imagemosaic.jdbc;

import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class PostgisSetup extends JDBCSetup {
    public static PostgisSetup Singleton = new PostgisSetup();

    public PostgisSetup() {
    }

    @Override
    protected String[] getTileTableNames() {
        return new String[] { "tiles0", "tiles1", "tiles2", "tiles3" };
    }

    @Override
    protected String[] getSpatialTableNames() {
        return new String[] { "spatial0", "spatial1", "spatial2", "spatial3" };
    }

    @Override
    protected void registerSpatial(String tn, Connection con)
        throws Exception {
        //AddGeometryColumn(varchar, varchar, varchar, integer, varchar, integer)
        PreparedStatement s = con.prepareStatement(
                " select AddGeometryColumn(?,?,?,?,?) ");
        s.setString(1, tn.toLowerCase());
        s.setString(2, getConfig().getGeomAttributeNameInSpatialTable());
        s.setInt(3, 31287);
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
        return "file:src/test/resources/oek.postgis.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BYTEA";
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
    protected String getDoubleSQLType() {
        return "FLOAT8";
    }

    @Override
    protected void createIndex(String tn, Connection con)
        throws Exception {
        String stmt = "CREATE INDEX IX_" + tn + " ON " + tn + " USING gist(" +
            getConfig().getGeomAttributeNameInSpatialTable() + ") ";
        con.prepareStatement(stmt).execute();
    }
}
