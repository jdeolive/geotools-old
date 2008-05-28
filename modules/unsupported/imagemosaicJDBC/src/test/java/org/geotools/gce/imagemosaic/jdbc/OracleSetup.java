package org.geotools.gce.imagemosaic.jdbc;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.text.MessageFormat;
import java.text.NumberFormat;

import java.util.Locale;


public class OracleSetup extends JDBCSetup {
    static final int SRSID = 31287;
    public static OracleSetup Singleton = new OracleSetup();

    public OracleSetup() {
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
        return "file:src/test/resources/oek.oracle.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BLOB";
    }

    @Override
    protected String getToGemoetryClause(Geometry g) {
        //return "SDO_UTIL.FROM_WKTGEOMETRY('"+g.toText()+"')";
        String pattern = "sdo_geometry (2003, " + SRSID +
            ", null, sdo_elem_info_array (1,1003,1)," +
            "sdo_ordinate_array ({0},{1}, {2},{3}, {4},{5}, {6},{7}, {8},{9}))";
        Envelope env = g.getEnvelopeInternal();
        NumberFormat f = NumberFormat.getNumberInstance(Locale.ENGLISH);
        f.setGroupingUsed(false);

        Object[] points = {
                f.format(env.getMinX()), f.format(env.getMinY()),
                f.format(env.getMaxX()), f.format(env.getMinY()),
                f.format(env.getMaxX()), f.format(env.getMaxY()),
                f.format(env.getMinX()), f.format(env.getMaxY()),
                f.format(env.getMinX()), f.format(env.getMinY())
            };

        return MessageFormat.format(pattern, points);
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
}
