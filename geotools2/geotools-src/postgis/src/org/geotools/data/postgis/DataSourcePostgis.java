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
import org.geotools.filter.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.EnvelopeExtent;

/**
 * Connects to a Postgis database and returns properly formatted GML.
 *
 * <p>This standard class must exist for every supported datastore.</p>
 *
 * @version $Id: DataSourcePostgis.java,v 1.4 2002/07/30 22:32:22 jmacgill Exp $
 * @author Rob Hranac, Vision for New York
 */
public class DataSourcePostgis implements org.geotools.data.DataSource {


    private static Map sqlTypeMap = new HashMap();

    /** Standard logging instance. */
    private static Category _log = Category.getInstance(DataSourcePostgis.class.getName());
    
    /** Initializes to clean Postgis database requests of SRID part. */
    
    
    /**
     * GID.  Since all layers may contain only one primary geometry
     * (i.e. geometry or geom collection), this is the same as a row ID
     * @task HACK: GID NEEDS TO BE GENERALIZED, JUST TEMPORARY
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
    private int maxFeatures = 500;

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
    public DataSourcePostgis (javax.sql.DataSource db, String tableName) {
        // create the return response type
        this.db = db;
        this.tableName = tableName;

        sqlTypeMap.put("varchar", String.class);
        sqlTypeMap.put("int4", Integer.class);
        sqlTypeMap.put("float4", Float.class);
        sqlTypeMap.put("float8", Double.class);
        sqlTypeMap.put("geometry", Geometry.class);
    }


    /**
     * Returns the full GML GetFeature response for each query and bounding box
     *
     * <p>Note that bounding box parameters are included because get feature
     * requests may contain more than 1 generic query with a single unified 
     * bounding box for each request.</p>
     *
     * @param genericQuery The query from the request object.
     */ 
    private static FeatureType makeSchema(ResultSetMetaData metaData) 
        throws SQLException, SchemaException {
        
        // initialize some local convenience variables
        
        AttributeType[] attributes = new AttributeType[metaData.getColumnCount()];

        //_log.info("about to loop through cols");

        // loop through all columns
        for( int i = 1, n = metaData.getColumnCount(); i <= n; i++) {
            //_log.info("reading col: " + i);
            //_log.info("reading col: " + metaData.getColumnTypeName(i));

            // set column name and type from database
            attributes[i - 1] = 
                new AttributeTypeDefault ( metaData.getColumnName(i),
                                           (Class) sqlTypeMap.get( metaData.getColumnTypeName(i)));
        }        

        return FeatureTypeFactory.create(attributes);
    }

    /**
     * Creates a new geometry object from the PostGIS database.
     *
     * @param geometry The PostGIS WKT + SRID string representation of the
     * geometry.
     */ 
    private static Geometry createGeometry(String geometry) {
        
        Geometry returnGeometry = null;
        //_log.info("geom string: " + geometryString);
        try {
            // clean geometry of SRID
            String cleanGeometry;
            StringTokenizer geometryCleaner = new StringTokenizer( geometry, ";");
            geometryCleaner.nextToken();
            cleanGeometry = geometryCleaner.nextToken();
            
            // create geometry
            returnGeometry = geometryReader.read( cleanGeometry );
        }
        catch (ParseException e) {
            _log.info("Failed to parse the geometry from PostGIS: " + e.getMessage());
        }
        

        // return the geometry
        return returnGeometry;

    }


    /**
     * Creates a new geometry object from the PostGIS database.
     *
     * @param geometry The PostGIS WKT + SRID string representation of the
     * geometry.
     */ 
    private static Object createOther(Object other) {        
        return other;
    }


    /**
     * Creates a new geometry object from the PostGIS database.
     *
     * @param geometry The PostGIS WKT + SRID string representation of the
     * geometry.
     * @task: TODO, regardless of query, makeSql fetches everything at the moment
     */ 
    private String makeSql(Filter query) {        
        return "SELECT * FROM " + tableName + ";";
    }


    /**
     * Returns the full GML GetFeature response for each query and bounding box
     *
     * <p>Note that bounding box parameters are included because get feature
     * requests may contain more than 1 generic query with a single unified 
     * bounding box for each request.</p>
     *
     * @param genericQuery The query from the request object.
     */ 
    public void importFeatures(org.geotools.feature.FeatureCollection collection, 
                               org.geotools.data.Extent query) 
        throws DataSourceException {
       

    }



    // TODO 2:
    // Implement these functions
    public void exportFeatures(FeatureCollection features, Extent query) 
        throws DataSourceException {
    }
    
    public void stopLoading() {
    }
    




    /**
     * Closes the result set.  Child class must remember to call.
     *
     * @param result The servlet request object.
     */ 
    protected static void closeResultSet(ResultSet result) {
        
        try {
            result.close();			
            result.getStatement().close();			
            result.getStatement().getConnection().close();			
        }
        catch (SQLException e) {
            _log.info("Error closing result set.");
        } 
    }

    /** Stops this DataSource from loading.
     */
    public void abortLoading() {
    }    
    
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     */
    public void addFeatures(FeatureCollection collection) throws DataSourceException {
    }    
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Envelope getBbox() {
        return null;
    }
    
    /** Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Envelope getBbox(boolean speed) {
        return null;
    }
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
         
       FeatureCollectionDefault col = new FeatureCollectionDefault();
       getFeatures(col,filter);
       return col;
       
    }
    
    /** Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter) throws DataSourceException {
         List features = new ArrayList(maxFeatures);

        try {
            // retrieve the result set
            _log.info("about to make connection");

            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery( makeSql(filter));


            //_log.info("about to make schema");

            // if no schema has been passed to the datasource, roll our own
            if( schema == null) {
                schema = makeSchema( result.getMetaData());
            }

            //_log.info("about to prepare feature reading");

            // set up a factory, attributes, and a counter for feature creation
            FeatureFactory factory = new FeatureFactory(schema);
            Object[] attributes = new Object[schema.attributeTotal()];
            int geometryPosition = schema.getDefaultGeometry().getPosition();
            int resultCounter = 0;

            // loop through entire result set or until maxFeatures are reached
            while( result.next() && ( resultCounter < maxFeatures)) {
                
                //_log.info("reading feature: " + resultCounter);
            
                // create an individual attribute by looping through columns
                for( int col = 0, n = schema.attributeTotal(); col < n; col++) {
                //_log.info("reading attribute: " + col + "it appears to be "+result.getObject(col+1));
                    attributes[col] = (col == geometryPosition) ? 
                        createGeometry( result.getString( col + 1)) :
                        createOther( result.getObject( col + 1));
                }

                // add a feature to the collection and increment result counter
                features.add( factory.create(attributes));
                resultCounter++;
            }								
            
            // add features to collection and close the result set
            collection.addFeatures((Feature[]) features.
                                   toArray(new Feature[features.size()]));				
            closeResultSet(result);
        }
        catch(SQLException e) {
            _log.info("Some sort of database connection error: " + e.getMessage());
        }
        catch(SchemaException e) {
            _log.info("Had problems creating the feature type..." + e.getMessage());
        }
        catch(Exception e) {
            _log.info("Error from the result set: " + e.getMessage());
            _log.info( e.toString() );
            e.printStackTrace();
        }
       
    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws DataSourceException {
    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter) throws DataSourceException {
    }
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
    }
    
}
