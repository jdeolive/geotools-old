/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.postgis;


//JTS imports
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCFeatureLocking;
import org.geotools.data.jdbc.JDBCFeatureStore;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.WKTAttributeIO;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderPostgis;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Postgis DataStore implementation.
 *
 * @author Chris Holmes
 * @version $Id: PostgisDataStore.java,v 1.8 2003/11/23 05:06:15 jive Exp $
 */
public class PostgisDataStore extends JDBCDataStore implements DataStore {
    /** The logger for the postgis module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.postgis");
    private static final int TABLE_NAME_COL = 3;

    /** The invisible column to use as the fid if no primary key is set */
    public static final String DEFAULT_FID_COLUMN = "oid";

    /** Error message prefix for sql connection errors */
    protected static final String CONN_ERROR = "Some sort of database connection error: ";

    //private ConnectionPool connectionPool;

    /** Map of postgis geometries to jts geometries */
    private static Map GEOM_TYPE_MAP = new HashMap();

    /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();

    static {
        GEOM_TYPE_MAP.put("GEOMETRY", Geometry.class);
        GEOM_TYPE_MAP.put("POINT", Point.class);
        GEOM_TYPE_MAP.put("LINESTRING", LineString.class);
        GEOM_TYPE_MAP.put("POLYGON", Polygon.class);
        GEOM_TYPE_MAP.put("MULTIPOINT", MultiPoint.class);
        GEOM_TYPE_MAP.put("MULTILINESTRING", MultiLineString.class);
        GEOM_TYPE_MAP.put("MULTIPOLYGON", MultiPolygon.class);
    }
    public static final int OPTIMIZE_SAFE = 0;
    public static final int OPTIMIZE_SQL = 1;

    public final int OPTIMIZE_MODE;       
    /** To create the sql where statement */
    protected SQLEncoder encoder = new SQLEncoderPostgis();

    //private String namespace;
    public PostgisDataStore(ConnectionPool connPool) throws IOException {
        this(connPool, null);
    }

    public PostgisDataStore(ConnectionPool connPool, String namespace)
        throws IOException {
        this(connPool, null, namespace);
    }
    
    public PostgisDataStore(ConnectionPool connPool, String schema, String namespace ) throws IOException{
        this( connPool, schema, namespace, OPTIMIZE_SAFE );            
    }
    public PostgisDataStore(ConnectionPool connPool, String schema, String namespace, int optimizeMode )
        throws IOException {
        super( connPool, schema, namespace );
        OPTIMIZE_MODE = optimizeMode;             
    }

    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet
     * contains the information retrieved by a call to  getColumns() on the
     * DatabaseMetaData object.  This information  can be used to construct an
     * Attribute Type.
     * 
     * <p>
     * This implementation construct an AttributeType using the default JDBC
     * type mappings defined in JDBCDataStore.  These type mappings only
     * handle native Java classes and SQL standard column types.  If a
     * geometry type is found then getGeometryAttribute is called.
     * </p>
     * 
     * <p>
     * Note: Overriding methods must never move the current row pointer in the
     * result set.
     * </p>
     *
     * @param rs The ResultSet containing the result of a
     *        DatabaseMetaData.getColumns call.
     *
     * @return The AttributeType built from the ResultSet.
     *
     * @throws SQLException If an error occurs processing the ResultSet.
     * @throws DataSourceException For problems when calling
     *         getGeometryAttribute.  Will be either wrapped SQLExceptions or
     *         because the column could not be found in the geometry_columns
     *         table.
     */
    protected AttributeType buildAttributeType(ResultSet rs)
        throws SQLException, DataSourceException {
        final int TABLE_NAME = 3;
        final int COLUMN_NAME = 4;
        final int TYPE_NAME = 6;
        String typeName = rs.getString(TYPE_NAME);

        if (typeName.equals("geometry")) {
            String tableName = rs.getString(TABLE_NAME);
            String columnName = rs.getString(COLUMN_NAME);

            return getGeometryAttribute(tableName, columnName);
        } else {
            return super.buildAttributeType(rs);
        }
    }

    /**
     * Returns an attribute type for a geometry column in a feature table.
     *
     * @param tableName The feature table name.
     * @param columnName The geometry column name.
     *
     * @return Geometric attribute.
     *
     * @throws SQLException DOCUMENT ME!
     * @throws DataSourceException if the geometry_columns table of postgis can
     *         not be found, if this typename could not be found in the table,
     *         or if there are sql problems.
     *
     * @task REVISIT: combine with querySRID, as they use the same select
     *       statement.
     * @task TODO: This should probably take a Transaction, so if things mess
     *       up then we can rollback.
     */
    AttributeType getGeometryAttribute(String tableName, String columnName)
        throws SQLException, DataSourceException {
        Connection dbConnection = null;

        try {
            dbConnection = getConnection(Transaction.AUTO_COMMIT);

            String sqlStatement = "SELECT type FROM GEOMETRY_COLUMNS WHERE "
                + "f_table_name='" + tableName + "' AND f_geometry_column='"
                + columnName + "';";
            LOGGER.fine("geometry sql statement is " + sqlStatement);

            String geometryType = null;

            // retrieve the result set from the JDBC driver
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                geometryType = result.getString("type");
                LOGGER.fine("geometry type is: " + geometryType);
            }

            if (geometryType == null) {
                String msg = " no geometry found in the GEOMETRY_COLUMNS table "
                    + " for " + tableName + " of the postgis install.  A row "
                    + "for " + columnName + " is required  "
                    + " for geotools to work correctly";
                throw new DataSourceException(msg);
            }

            statement.close();

            Class type = (Class) GEOM_TYPE_MAP.get(geometryType);

            return AttributeTypeFactory.newAttributeType(columnName, type);

            //I have no idea why my compiler is complaining about this.
        } catch (IOException ioe) {
            throw new DataSourceException("getting connection", ioe);
        } finally {
            JDBCDataStore.close(dbConnection, Transaction.AUTO_COMMIT, null);
        }
    }

    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
        FeatureTypeInfo info = getFeatureTypeInfo(typeName);
        int srid = -1;
        SQLEncoderPostgis encoder = new SQLEncoderPostgis();

        if (info.getSchema().getDefaultGeometry() != null) {
            String geom = info.getSchema().getDefaultGeometry().getName();
            srid = info.getSRID(geom);
            encoder.setDefaultGeometry(geom);
        }

        encoder.setSRID(srid);

        return new PostgisSQLBuilder(encoder);
    }

    /**
     * Override to create Well Known Text attribute reader.  Might be nice to
     * have an WKTDataSource, between postgis and jdbc, as any simple feature
     * for sql compliant database should work the same way, it can return wkt
     * with AsText(), and will have a geometry_columns table where the info
     * can be queried, ect.
     *
     * @param attrType The AttributeType to read.
     * @param queryData The data containing the result of the query.
     * @param index The index within the result set to read the data from.
     *
     * @return The AttributeReader that will read the geometry from the
     *         results.
     *
     * @throws DataSourceException If an error occurs building the
     *         AttributeReader.
     *
     * @task TODO: return a WKBAttributeReader, or a native object reader,
     *       something that will be faster.
     */
    protected AttributeReader createGeometryReader(AttributeType attrType,
        QueryData queryData, int index) throws DataSourceException {
        return new WKTAttributeIO(queryData, attrType, index);
    }

    protected AttributeWriter createGeometryWriter(AttributeType attrType,
        QueryData queryData, int index) throws DataSourceException {
        return new WKTAttributeIO(queryData, attrType, index);
    }

    /**
     * Override that works exactly the same except sets the default  fid column
     * as 'oid', which is a reasonable default for postgis.
     *
     * @param typeName The name of the table to get a primary key for.
     *
     * @return The name of the primay key column.
     *
     * @throws IOException This will only occur if there is an error getting a
     *         connection to the Database.
     */
    protected String determineFidColumnName(String typeName)
        throws IOException {
        String fidColumn = super.determineFidColumnName(typeName);

        if (fidColumn == null) {
            fidColumn = DEFAULT_FID_COLUMN;
        }

        return fidColumn;
    }

    protected int determineSRID(String tableName, String geometryColumnName)
        throws IOException {
        Connection dbConnection = null;

        try {
            String sqlStatement = "SELECT srid FROM GEOMETRY_COLUMNS WHERE "
                + "f_table_name='" + tableName + "' AND f_geometry_column='"
                + geometryColumnName + "';";
            dbConnection = getConnection(Transaction.AUTO_COMMIT);

            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                int retSrid = result.getInt("srid");
                close(statement);

                return retSrid;
            } else {
                String mesg = "No geometry column row for srid in table: "
                    + tableName + ", geometry column " + geometryColumnName;
                throw new DataSourceException(mesg);
            }
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
        } finally {
            JDBCDataStore.close(dbConnection, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * Crops non feature type tables.  There are alot of additional tables in a
     * Oracle tablespace. This tries to remove some of them.  If the
     * schemaName is provided in the Constructor then the job of narrowing
     * down tables will be mush easier.  Otherwise there are alot of Meta
     * tables and SDO tables to cull.  This method tries to remove as many as
     * possible.
     *
     * @see org.geotools.data.jdbc.JDBCDataStore#allowTable(java.lang.String)
     */
    protected boolean allowTable(String tablename) {
        if (tablename.equals("geometry_columns")) {
            return false;
        } else if (tablename.startsWith("spatial_ref_sys")) {
            return false;
        }

        //others?
        return true;
    }

    private String getGeometryText(Geometry geom, int srid) {
        String geoText = geometryWriter.write(geom);
        String sql = "GeometryFromText('" + geoText + "', " + srid + ")";

        return sql;
    }

    protected JDBCFeatureWriter createFeatureWriter(FeatureReader fReader,
        AttributeWriter writer, QueryData queryData) throws IOException {
        LOGGER.fine("returning postgis feature writer");

        return new PostgisFeatureWriter(fReader, writer, queryData);
    }

    /**
     * Default implementation based on getFeatureReader and getFeatureWriter.
     * 
     * <p>
     * We should be able to optimize this to only get the RowSet once
     * </p>
     *
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName)
        throws IOException {
            
        if( OPTIMIZE_MODE == OPTIMIZE_SQL ){
            return new PostgisFeatureLocking(this, getSchema(typeName));
        }
        // default 

        if (getLockingManager() != null) {
            // Use default JDBCFeatureLocking that delegates all locking
            // the getLockingManager
            return new JDBCFeatureLocking(this, getSchema(typeName));
            
        } else {
            // subclass should provide a FeatureLocking implementation
            // but for now we will simply forgo all locking
            return new JDBCFeatureStore(this, getSchema(typeName));
            
        }
    }

    //These are going to be needed by PostgisFeatureStore as well... we also
    //could do the update stuff on the insert row, but I've just never used it
    //before.  Want to get things working with something I'm sure of.

    /**
     * Creates a sql insert statement.  Uses each feature's schema, which makes
     * it possible to insert out of order, as well as inserting less than all
     * features.
     *
     * @param feature the feature to add.
     * @param ftInfo the name of the feature table being inserted into.
     *
     * @return an insert sql statement.
     */
    private String makeInsertSql(Feature feature, FeatureTypeInfo ftInfo) {
        String tableName = ftInfo.getFeatureTypeName();
        String attrValue;
        StringBuffer sql = new StringBuffer("INSERT INTO \"" + tableName
                + "\"(");
        FeatureType featureSchema = feature.getFeatureType();

        AttributeType[] types = featureSchema.getAttributeTypes();

        for (int i = 0; i < types.length; i++) {
            sql.append("\"" + types[i].getName() + "\"");
            sql.append((i < (types.length - 1)) ? ", " : ") ");
        }

        sql.append("VALUES (");

        Object[] attributes = feature.getAttributes(null);

        for (int j = 0; j < attributes.length; j++) {
            if (types[j].isGeometry()) {
                String geomName = types[j].getName();
                int srid = ftInfo.getSRID(geomName);
                String geoText = getGeometryText((Geometry) attributes[j], srid);
                sql.append(geoText);

                //String geoText = geometryWriter.write((Geometry) attributes[j]);
                //sql.append("GeometryFromText('" + geoText + "', " + srid + ")");
            } else {
                attrValue = addQuotes(attributes[j]);
                sql.append(attrValue);
            }

            if (j < (attributes.length - 1)) {
                sql.append(", ");
            }
        }

        sql.append(");");

        return sql.toString();
    }

    /**
     * Adds quotes to an object for storage in postgis.  The object should be a
     * string or a number.  To perform an insert strings need quotes around
     * them, and numbers work fine with quotes, so this method can be called
     * on unknown objects.
     *
     * @param value The object to add quotes to.
     *
     * @return a string representation of the object with quotes.
     */
    private String addQuotes(Object value) {
        String retString;

        if (value != null) {
            retString = "'" + value.toString() + "'";
        } else {
            retString = "null";
        }

        return retString;
    }

    String getFidColumn(String typeName) throws IOException {
        return getFeatureTypeInfo(typeName).getFidColumnName();
    }

    int getSRID(String typeName, String geomColName) throws IOException {
        return getFeatureTypeInfo(typeName).getSRID(geomColName);
    }

    protected class PostgisFeatureWriter extends JDBCFeatureWriter {
        public PostgisFeatureWriter(FeatureReader fReader,
            AttributeWriter writer, QueryData queryData)
            throws IOException {
            super(fReader, writer, queryData);
        }

        protected void doInsert(Feature current)
            throws IOException, SQLException {
            LOGGER.fine("inserting into postgis feature " + current);

            ResultSet rs = queryData.getResultSet();

            //rs.moveToInsertRow();
            Statement statement = null;

            try {
                Connection conn = rs.getStatement().getConnection();
                statement = conn.createStatement();

                String sql = makeInsertSql(current,
                        queryData.getFeatureTypeInfo());
                statement.executeUpdate(sql);

                //} catch (IllegalAttributeException e) {
                //throw new DataSourceException("Unable to do insert", e);
            } catch (SQLException sqle) {
                String msg = "SQL Exception writing geometry column";
                LOGGER.log(Level.SEVERE, msg, sqle);
                queryData.close(sqle);
                throw new DataSourceException(msg, sqle);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        String msg = "Error closing JDBC Statement";
                        LOGGER.log(Level.WARNING, msg, e);
                    }
                }
            }
        }
    }

    /**
     * This is just an initial stab at this.  Ideally we could somehow use
     * ResultSet update sequences, but the jdbc driver doesn't handle postgis
     * objects.   Using UpdateString with wkt didn't work with initial tests,
     * it couldn't find an AsText column.  So for now we're just using a
     * ripped  off modify from PostgisDataSource.  Should ideally rewrite
     * stuff for  max code reuse, so the PostgisFeatureStore uses the same
     * one.  And ideally put in JDBCDataStore for subclasses to just
     * over-write the geometry part. private void updateGeometry(Geometry
     * geom, QueryData queryData, int position, String fid)     throws
     * DataSourceException, SQLException {                 FeatureType schema
     * = queryData.getFeatureTypeInfo().getSchema();     AttributeType curType
     * = schema.getAttributeType(position);     if (curType.isGeometry()) {
     * //create the text to add geometry String geomName = curType.getName();
     * int srid = queryData.getFeatureTypeInfo().getSRID(geomName); String
     * geoText = geometryWriter.write((Geometry) geom); String newValue =
     * null;//"SRID=" + srid + ";"+geoText;
     * queryData.getResultSet().updateObject(geomName, newValue);
     * queryData.getResultSet().updateRow();     }     /Statement statement =
     * null; try { Connection conn =
     * queryData.getResultSet().getStatement().getConnection(); statement =
     * conn.createStatement(); String where = getFidWhere(fid, queryData);
     * FeatureType schema = queryData.getFeatureTypeInfo().getSchema();
     * AttributeType[] attType ={ schema.getAttributeType(position) };
     * Object[] att = { geom }; String sql = makeModifySql(attType, att,
     * where, queryData.getFeatureTypeInfo()); LOGGER.finer("this sql
     * statement = " + sql); statement.executeUpdate(sql); } catch
     * (SQLException sqle) { String message = CONN_ERROR + sqle.getMessage();
     * LOGGER.warning(message); throw new DataSourceException(message, sqle);
     * } finally { PostgisDataSource.close(statement); }
     */

    //}
}
