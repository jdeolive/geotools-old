package org.geotools.data.mysql;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;
import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

/**
 * An implementation of the GeoTools Data Store API for the MySQL database platform.
 * The plan is to support traditional MySQL datatypes, as well as the new geometric
 * datatypes provided with MySQL 4.1 and higher.<br>
 * <br>
 * TODO:  MySQLDataStore is not yet tested for MySQL 4.1's geometric datatypes.<br>
 * <br>
 * Please see {@link org.geotools.data.jdbc.JDBCDataStore class JDBCDataStore} and
 * {@link org.geotools.data.DataStore interface DataStore} for DataStore usage details.
 * @author Gary Sheppard garysheppard@psu.edu
 */

public class MySQLDataStore extends JDBCDataStore {
    
    private static WKTWriter geometryWriter = new WKTWriter();
    
    /**
     * Basic constructor for MySQLDataStore.  Requires creation of a
     * {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}, which could
     * be done similar to the following:<br>
     * <br>
     * <code>MySQLConnectionFactory connectionFactory = new MySQLConnectionFactory("mysqldb.geotools.org", "3306", "myCoolSchema");</code><br>
     * <code>ConnectionPool connectionPool = connectionFactory.getConnectionPool("omcnoleg", "myTrickyPassword123");</code><br>
     * <code>DataStore dataStore = new MySQLDataStore(connectionPool);</code><br>
     * @param connectionPool a MySQL {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}
     * @throws IOException if the database cannot be properly accessed
     * @see org.geotools.data.jdbc.ConnectionPool
     * @see org.geotools.data.mysql.MySQLConnectionFactory
     */
    public MySQLDataStore(ConnectionPool connectionPool) throws IOException {
        super(connectionPool);
    }
    
    /**
     * Constructor for MySQLDataStore where the database schema name is provided.
     * @param connectionPool a MySQL {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @throws IOException if the database cannot be properly accessed
     */
    public MySQLDataStore(ConnectionPool connectionPool, String databaseSchemaName) throws IOException {
        super(connectionPool, databaseSchemaName);
    }
    
    /**
     * Constructor for MySQLDataStore where the database schema name is provided.
     * @param connectionPool a MySQL {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @param namespace the namespace for this data store.  Can be null, in which case the namespace will simply be the schema name.
     * @throws IOException if the database cannot be properly accessed
     */
    public MySQLDataStore(ConnectionPool connectionPool, String databaseSchemaName, String namespace) throws IOException {
        super(connectionPool, databaseSchemaName, namespace);
    }
    
    /**
     * A utility method for creating a MySQLDataStore from database connection parameters,
     * using the default port (3306) for MySQL.
     * @param host the host name or IP address of the database server
     * @param schema the name of the database instance
     * @param username the database username
     * @param password the password corresponding to <code>username</code>
     * @return a MySQLDataStore for the specified parameters
     */
    public static MySQLDataStore getInstance(String host, String schema, String username, String password) throws IOException, SQLException {
        return getInstance(host, "3306", schema, username, password);
    }
    
    /**
     * Utility method for creating a MySQLDataStore from database connection parameters.
     * @param host the host name or IP address of the database server
     * @param port the port number of the database
     * @param schema the name of the database instance
     * @param username the database username
     * @param password the password corresponding to <code>username</code>
     * @throws IOException if the MySQLDataStore cannot be created because the database cannot be properly accessed
     * @throws SQLException if a MySQL connection pool cannot be established
     */
    public static MySQLDataStore getInstance(String host, String port, String schema, String username, String password) throws IOException, SQLException {
        return new MySQLDataStore(new MySQLConnectionFactory(host, port, schema).getConnectionPool(username, password));
    }
    
    protected AttributeReader createGeometryReader(AttributeType attrType, QueryData queryData, int index) throws IOException {
        return new MySQLAttributeReader(attrType, queryData, index);
    }
    
    protected AttributeWriter createGeometryWriter(AttributeType attrType, QueryData queryData, int index) throws IOException {
        return new MySQLAttributeWriter(attrType, queryData, index);
    }
    
    protected JDBCFeatureWriter createFeatureWriter(FeatureReader fReader, AttributeWriter writer, QueryData queryData) throws IOException {
        return new MySQLFeatureWriter(fReader, writer, queryData);
    }
    
    private String makeInsertSql(Feature feature, FeatureTypeInfo ftInfo) {
        String tableName = ftInfo.getFeatureTypeName();
        String attrValue;
        StringBuffer sql = new StringBuffer("INSERT INTO " + tableName + " (");
        FeatureType featureSchema = feature.getFeatureType();
        
        AttributeType[] types = featureSchema.getAttributeTypes();
        
        boolean isAutoIncrement = (ftInfo.getFidColumnName() != null);
        
        if (!isAutoIncrement) {
            sql.append(ftInfo.getFidColumnName());
            sql.append(", ");
        }
        
        for (int i = 0; i < types.length; i++) {
            sql.append(types[i].getName());
            sql.append((i < (types.length - 1)) ? ", " : ") ");
        }
        
        sql.append("VALUES (");
        
        Object[] attributes = feature.getAttributes(null);
        
        if (!isAutoIncrement) {
            String fid = feature.getID();
            int split = fid.indexOf('.');
            if(split != -1 && fid.substring(0, split).equals(tableName)) {
                fid = fid.substring(split+1);
            }
            char ch = fid.charAt(0);
            
            if (Character.isLetter(ch) || ch == '_') {
                sql.append("'");
                sql.append(fid);
                sql.append("'");
            } else if (Character.isDigit(ch)) {
                try {
                    long number = Long.parseLong(fid);
                    sql.append(number);
                }
                catch (NumberFormatException badNumber){
                    sql.append(fid);
                }
            } else {
                sql.append(fid);
            }
            sql.append(", ");
        }
        for (int j = 0; j < attributes.length; j++) {
            if (types[j].isGeometry()) {
                String geomName = types[j].getName();
                int srid = ftInfo.getSRID(geomName);
                String geoText = getGeometryText((Geometry) attributes[j], srid);
                sql.append(geoText);
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
    
    private String addQuotes(Object value) {
        String retString;
        
        if (value != null) {
            //first, change single single quotes (') to double single quotes ('')
            //this escapes those single quotes in MySQL
            StringBuffer buf = new StringBuffer();
            String str = value.toString();
            for (int i = 0; i < str.length(); i++) {
                char thisChar = str.charAt(i);
                buf.append(thisChar);
                if (thisChar == '\'') {
                    buf.append('\'');
                }
            }
            
            retString = "'" + buf.toString() + "'";
        } else {
            retString = "NULL";
        }
        
        return retString;
    }
    
    private String getGeometryText(Geometry geom, int srid) {
        if (geom == null) {
            return "NULL";
        }
        
        String geoText = geometryWriter.write(geom);
        String sql = null;
        if (GeometryCollection.class.isAssignableFrom(geom.getClass())) {
            if (MultiPoint.class.isAssignableFrom(geom.getClass())) {
                sql = "MultiPointFromText";
            } else if (MultiLineString.class.isAssignableFrom(geom.getClass())) {
                sql = "MultiLineStringFromText";
            } else if (MultiPolygon.class.isAssignableFrom(geom.getClass())) {
                sql = "MultiPolygonFromText";
            } else {
                sql = "GeometryCollectionFromText";
            }
        } else {
            if (Point.class.isAssignableFrom(geom.getClass())) {
                sql = "PointFromText";
            } else if (LineString.class.isAssignableFrom(geom.getClass())) {
                sql = "LineStringFromText";
            } else if (Polygon.class.isAssignableFrom(geom.getClass())) {
                sql = "PolygonFromText";
            } else {
                sql = "GeometryFromText";
            }
        }
        
        sql += "('" + geoText + "', " + srid + ")";
        
        return sql;
    }
    
    /**
     * Utility method for getting a FeatureWriter for modifying existing features,
     * using no feature filtering and auto-committing.  Not used for adding new
     * features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for modifying existing features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriter(String typeName) throws IOException {
        return getFeatureWriter(typeName, Filter.NONE, Transaction.AUTO_COMMIT);
    }
    
    /**
     * Utility method for getting a FeatureWriter for adding new features, using
     * auto-committing.  Not used for modifying existing features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for adding new features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriterAppend(String typeName) throws IOException {
        return getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);
    }
    
    protected class MySQLFeatureWriter extends JDBCFeatureWriter {
        
        public MySQLFeatureWriter(FeatureReader fReader, AttributeWriter writer, QueryData queryData) throws IOException {
            super(fReader, writer, queryData);
        }
        
        protected void doInsert(Feature current) throws IOException, SQLException {
            Statement statement = null;
            
            try {
                statement = queryData.getConnection().createStatement();
                
                String sql = makeInsertSql(current, queryData.getFeatureTypeInfo());
                System.out.println("going to insert:");
                System.out.println(sql);
                statement.executeUpdate(sql);
                
                //} catch (IllegalAttributeException e) {
                //throw new DataSourceException("Unable to do insert", e);
            } catch (SQLException sqle) {
                String msg = "SQL Exception writing geometry column";
                throw new DataSourceException(msg, sqle);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        //this is sad, but not the end of the world
                    }
                }
            }
        }
        
    }
    
    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet
     * contains the information retrieved by a call to  getColumns() on the
     * DatabaseMetaData object.  This information  can be used to construct an
     * Attribute Type.
     * 
     * <p>
     * In addition to standard SQL types, this method identifies MySQL 4.1's geometric
     * datatypes and creates attribute types accordingly.  This happens when the
     * datatype, identified by column 5 of the ResultSet parameter, is equal to
     * java.sql.Types.OTHER.  If a Types.OTHER ends up not being geometric, this
     * method simply calls the parent class's buildAttributeType method to do something
     * with it.
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
     * @throws DataSourceException Provided for overriding classes to wrap
     *         exceptions caused by other operations they may perform to
     *         determine additional types.  This will only be thrown by the
     *         default implementation if a type is present that is not present
     *         in the TYPE_MAPPINGS.
     */
    protected AttributeType buildAttributeType(ResultSet rs) throws SQLException, DataSourceException {
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        final int TYPE_NAME = 6;

        int dataType = rs.getInt(DATA_TYPE);
        if (dataType == Types.OTHER) {
            //this is MySQL-specific; handle it
            String typeName = rs.getString(TYPE_NAME);
            String typeNameLower = typeName.toLowerCase();
            
            if ("geometry".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), Geometry.class);
            } else if ("point".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), Point.class);
            } else if ("linestring".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), LineString.class);
            } else if ("polygon".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), Polygon.class);
            } else if ("multipoint".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), MultiPoint.class);
            } else if ("multilinestring".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), MultiLineString.class);
            } else if ("multipolygon".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), MultiPolygon.class);
            } else if ("geometrycollection".equals(typeNameLower)) {
                return AttributeTypeFactory.newAttributeType(rs.getString(COLUMN_NAME), GeometryCollection.class);
            } else {
                //nothing else we can do
                return super.buildAttributeType(rs);
            }
        } else {
            return super.buildAttributeType(rs);
        }
    }
    
    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
        return new MySQLSQLBuilder();
    }
    
}