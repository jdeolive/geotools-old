/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.data.postgis;

import java.io.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Category;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.datasource.extents.EnvelopeExtent;

/**
 * Connects to a Postgis database and returns properly formatted GML.
 *
 * <p>This standard class must exist for every supported datastore.</p>
 *
 * @version $Id: PostgisDataSource.java,v 1.1 2002/08/07 18:25:37 robhranac Exp $
 * @author Rob Hranac, Vision for New York
 */
public class PostgisDataSource implements org.geotools.data.DataSource {


    private static Map sqlTypeMap = new HashMap();

    private static Map geometryTypeMap = new HashMap();

    /** Standard logging instance. */
    private static Category _log = Category.getInstance(PostgisDataSource.class.getName());
    
    /** Initializes to clean Postgis database requests of SRID part. */
    
    // NEEDS TO BE GENERALIZED, JUST TEMPORARY
    /**
     * GID.  Since all layers may contain only one primary geometry
     * (i.e. geometry or geom collection), this is the same as a row ID
     */
    private static final String GID_NAME = "objectid";
    
    /**
     * FID. Since all layers may contain only one primary geometry
     * (i.e. geometry or geom collection), this is the same as a row ID
     */
    private static final String FID_NAME = "objectid";
    
    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();
    
    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);
    
    /** The maximum features allowed by the server for any given response. */
    private static int maxFeatures = 1000;

    /** The maximum features allowed by the server for any given response. */
    private FeatureType schema = null;

    /** A postgis connection. */
    private javax.sql.DataSource db;

    /** A tablename. */
    private String tableName;

    /**
     * Initializes the database and request handler.
     *
     * @param response The query from the request object.
     * @param maxFeatures The query from the request object.
     */
    public PostgisDataSource (javax.sql.DataSource db, String tableName) {
        // create the return response type
        this.db = db;
        this.tableName = tableName;

        sqlTypeMap.put("varchar", String.class);
        sqlTypeMap.put("int4", Integer.class);
        sqlTypeMap.put("float4", Float.class);
        sqlTypeMap.put("float8", Double.class);
        sqlTypeMap.put("geometry", Geometry.class);

        geometryTypeMap.put("GEOMETRY", Geometry.class);
        geometryTypeMap.put("POINT", Point.class);
        geometryTypeMap.put("LINESTRING", LineString.class);
        geometryTypeMap.put("POLYGON", Polygon.class);
        geometryTypeMap.put("MULTIPOINT", MultiPoint.class);
        geometryTypeMap.put("MULTILINESTRING", MultiLineString.class);
        geometryTypeMap.put("MULTIPOLYGON", MultiPolygon.class);
    }

    /**
     * Initializes the database and request handler.
     *
     * @param response The query from the request object.
     * @param maxFeatures The query from the request object.
     */
    public PostgisDataSource (javax.sql.DataSource db, String tableName, int maxFeatures) {
        // create the return response type
        this.db = db;
        this.tableName = tableName;
        this.maxFeatures = maxFeatures;

        sqlTypeMap.put("varchar", String.class);
        sqlTypeMap.put("int4", Integer.class);
        sqlTypeMap.put("float4", Float.class);
        sqlTypeMap.put("float8", Double.class);
        sqlTypeMap.put("geometry", Geometry.class);

        geometryTypeMap.put("GEOMETRY", Geometry.class);
        geometryTypeMap.put("POINT", Point.class);
        geometryTypeMap.put("LINESTRING", LineString.class);
        geometryTypeMap.put("POLYGON", Polygon.class);
        geometryTypeMap.put("MULTIPOINT", MultiPoint.class);
        geometryTypeMap.put("MULTILINESTRING", MultiLineString.class);
        geometryTypeMap.put("MULTIPOLYGON", MultiPolygon.class);
    }


    /* *************************************************************************
     * Some static methods to help with schema construction and SQL statement
     * creation.
     * ************************************************************************/
    /**
     * Returns the full GML GetFeature response for each query and bounding box
     *
     * <p>Note that bounding box parameters are included because get feature
     * requests may contain more than 1 generic query with a single unified 
     * bounding box for each request.</p>
     *
     * @param genericQuery The query from the request object.
     */ 
    private static FeatureType makeSchema(String tableName, 
                                          javax.sql.DataSource db) 
        throws Exception {
        
        Connection dbConnection = db.getConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM " + tableName + " LIMIT 1;");
        ResultSetMetaData metaData = result.getMetaData();
        
        // initialize some local convenience variables        
        String columnName;
        String columnTypeName;
        AttributeType[] attributes = new AttributeType[metaData.getColumnCount() - 1];

        int offset = 1;
        //_log.debug("about to loop through cols");
        // loop through all columns
        for( int i = 1, n = metaData.getColumnCount(); i <= n; i++) {
            //_log.debug("reading col: " + i);
            //_log.debug("reading col: " + metaData.getColumnTypeName(i));
            //_log.debug("reading col: " + metaData.getColumnName(i));

            columnTypeName = metaData.getColumnTypeName(i);
            columnName = metaData.getColumnName(i);

            // geometry is treated specially
            if( columnTypeName.equals("geometry")) {
                attributes[i - offset] = 
                    getGeometryAttribute(db, tableName, columnName);
            }

            // object id is ignored in the schema, since it is treated as a 
            //  feature id
            else if (columnName.equals("objectid")) {
                offset++;
            }

            else  {
                // set column name and type from database
                attributes[i - offset] = 
                    new AttributeTypeDefault ( columnName,
                                               (Class) sqlTypeMap.get(columnTypeName));
            }
        }

        try {
            result.close();			
            result.getStatement().close();			
            result.getStatement().getConnection().close();			
        }
        catch (SQLException e) {
            _log.debug("Error closing result set.");
        } 

        //_log.debug("the postgis-created schema is: " + FeatureTypeFactory.create(attributes).toString());
        return FeatureTypeFactory.create(attributes);
    }


    /**
     * Returns an attribute type for a geometry column in a feature table.
     *
     * @param db The JDBC data source.
     * @param tableName The feature table name.
     * @param columnName The geometry column name.
     * @return Geometric attribute.
     */ 
    private static AttributeType getGeometryAttribute(javax.sql.DataSource db, 
                                                      String tableName, 
                                                      String columnName) 
        throws Exception {
        
        String sqlStatement = "SELECT type FROM GEOMETRY_COLUMNS WHERE " + 
            "f_table_name='" + tableName + "' AND f_geometry_column='" + 
            columnName + "';";
        String geometryType = null;

        // retrieve the result set from the JDBC driver
        //_log.debug("about to make connection, SQL: " + sqlStatement);
        Connection dbConnection = db.getConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery(sqlStatement);
        
        // loop through entire result set or until maxFeatures are reached
        //_log.debug("about to read geometry type");
        if( result.next()) {
            geometryType = result.getString("type");
            //_log.debug("geometry type is: " + geometryType);
        }
        
        closeResultSet(result);
        return new AttributeTypeDefault ( columnName,
                                          (Class) geometryTypeMap.get(geometryType));

    }


    /**
     * Creates a SQL statement for the PostGIS database.
     *
     * @return Full SQL statement.
     */ 
    private static String makeSql(Filter filter, String tableName, FeatureType schema) {
        StringBuffer sqlStatement = new StringBuffer("SELECT objectid,");
        AttributeType[] attributeTypes = schema.getAttributeTypes();

        for( int i = 0; i < attributeTypes.length; i++) {
            if( Geometry.class.isAssignableFrom( attributeTypes[i].getType())) {
                sqlStatement.append(" AsText(" + attributeTypes[i].getName() + ")");
            }
            else {
                sqlStatement.append(" " + attributeTypes[i].getName());
            }
            if( i < attributeTypes.length - 1) {
                sqlStatement.append(",");
            }                
        }
        return sqlStatement.append(" FROM " + tableName + " LIMIT " + maxFeatures + ";").toString();
    }


    /**
     * Closes the result set.  Child class must remember to call.
     *
     * @param result The servlet request object.
     */ 
    private static void closeResultSet(ResultSet result) {
        
        try {
            result.close();			
            result.getStatement().close();			
            result.getStatement().getConnection().close();			
        }
        catch (SQLException e) {
            _log.debug("Error closing result set.");
        } 
    }


    /* *************************************************************************
     * Implement the public instance methods of the DataSource interface.
     * ************************************************************************/
    /**
     * Returns a feature collection, based on the passed filter.
     *
     * @param filter The filter from the requester.
     */ 
    public FeatureCollection getFeatures(Filter filter)
        throws DataSourceException {

        FeatureCollection collection = new FeatureCollectionDefault();
        getFeatures(collection, filter);
        return collection;
    }

    /**
     * Returns the full GML GetFeature response for each query and bounding box
     *
     * @param genericQuery The query from the request object.
     */ 
    public void getFeatures(FeatureCollection collection, Filter filter) 
        throws DataSourceException {
        
        List features = new ArrayList(maxFeatures);
        try {
            // retrieve the result set from the JDBC driver
            //_log.debug("about to make connection");
            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();

            // if no schema has been passed to the datasource, roll our own
            if( schema == null) {
                schema = makeSchema(tableName, db);
            }
            //_log.debug("just made schema: " + schema.toString());

            //_log.debug("about to run a query: " + makeSql(filter, tableName, schema));
            ResultSet result = statement.executeQuery( makeSql(filter, tableName, schema));

            // set up a factory, attributes, and a counter for feature creation
            //_log.debug("about to prepare feature reading");
            FeatureFactory factory = new FeatureFactory(schema);
            Object[] attributes = new Object[schema.attributeTotal()];
            String featureId;
            int geometryPosition = schema.getDefaultGeometry().getPosition();
            int resultCounter = 0;
            int totalAttributes = schema.attributeTotal();
            int col;                

            // loop through entire result set or until maxFeatures are reached
            while( result.next() && ( resultCounter < maxFeatures)) {

                // grab featureId, which is the (hidden) objectid column and
                //  always appears first 
                featureId = result.getString(1);

                // create an individual attribute by looping through columns
                _log.debug("reading feature: " + resultCounter);
                //_log.debug("geometry position: " + geometryPosition);
                for( col = 0; col < totalAttributes; col++) {
                    //_log.debug("reading attribute: " + col);
                    if( col == geometryPosition) {
                        //_log.debug("found geometry: " + geometryReader.read( result.getString(col + 2)));
                        attributes[col] = geometryReader.read( result.getString(col + 2));
                    }
                    else {
                        //_log.debug("found attribute: " + result.getString(col + 2));
                        attributes[col] = result.getObject(col + 2);
                    }
                }
        
                // add a feature to the collection and increment result counter
                features.add( factory.createFlat(attributes, featureId));
                resultCounter++;
            }								
            
            // add features to collection and close the result set
            collection.addFeatures((Feature[]) features.
                                   toArray(new Feature[features.size()]));				
            closeResultSet(result);
        }
        catch(SQLException e) {
            _log.debug("Some sort of database connection error: " + e.getMessage());
        }
        catch(SchemaException e) {
            _log.debug("Had problems creating the feature type..." + e.getMessage());
        }
        catch(Exception e) {
            _log.debug("Error from the result set: " + e.getMessage());
            _log.debug( e.toString() );
            e.printStackTrace();
        }

    }


    /**
     * Returns a feature collection, based on the passed filter.
     *
     * @param collection Add features to the PostGIS database.
     */ 
    public void addFeatures(FeatureCollection collection)
        throws DataSourceException {
        throw new DataSourceException("Operation not supported.");
    }

    /**
     * Removes the features specified by the passed filter from the
     * PostGIS database.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     */
    public void removeFeatures(Filter filter)
        throws DataSourceException {
        throw new DataSourceException("Operation not supported.");
    }
    
    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws DataSourceException {
        throw new DataSourceException("Operation not supported.");
    }
    
    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws DataSourceException {
        throw new DataSourceException("Operation not supported.");
    }
    

    /**
     * Stops this DataSource from loading.
     */
    public void abortLoading() {
    }
    
    /**
     * Gets the bounding box of this datasource using the default speed of 
     * this datasource as set by the implementer. 
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox() {
        return new Envelope();
    }
    
    /**
     * Gets the bounding box of this datasource using the speed of 
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox(boolean speed) {
        return new Envelope();
    }    
    
}
