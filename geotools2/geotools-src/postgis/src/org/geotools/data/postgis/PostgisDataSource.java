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
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.filter.SQLUnpacker;
import org.geotools.filter.SQLEncoderPostgis;
import org.geotools.filter.SQLEncoderException;
import org.geotools.resources.Geotools;
import org.geotools.datasource.extents.EnvelopeExtent;

//Logging system
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Connects to a Postgis database and returns properly formatted GML.
 *
 * <p>This standard class must exist for every supported datastore.</p>
 *
 * @version $Id: PostgisDataSource.java,v 1.6 2002/10/16 17:56:15 cholmesny Exp $
 * @author Rob Hranac, Vision for New York
 */
public class PostgisDataSource implements org.geotools.data.DataSource {


    private static Map sqlTypeMap = new HashMap();

    private static Map geometryTypeMap = new HashMap();

  /**
     * The logger for the filter module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.postgis");
    
    
    /** Initializes to clean Postgis database requests of SRID part. */
    
    // NEEDS TO BE GENERALIZED, JUST TEMPORARY
    /**
     * GID.  Since all layers may contain only one primary geometry
     * (i.e. geometry or geom collection), this is the same as a row ID
     */
    private static final String GID_NAME = "gid";
    
    /**
     * FID. Since all layers may contain only one primary geometry
     * (i.e. geometry or geom collection), this is the same as a row ID
     */
    private static final String FID_NAME = "gid";
    
    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();
    
    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);
    
        /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();

    /** The maximum features allowed by the server for any given response. */
    private static int maxFeatures = 1000;

    /** The srid of the data in the table.  HACK: This won't
	work if a schema is passed.  Add srid to schema? */
    private static int srid;

 /** To create the sql where statement */
    private static SQLEncoderPostgis encoder = new SQLEncoderPostgis();


    /** The maximum features allowed by the server for any given response. */
    private FeatureType schema = null;

    /** A postgis connection. */
    private javax.sql.DataSource db;

    /** A tablename. */
    private String tableName;


    /** To get the part of the filter incorporated into the sql where statement */
    private SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());


    static {
	Geotools.init("Log4JFormatter", Level.FINER);
    }


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
    public static FeatureType makeSchema(String tableName, 
                                          javax.sql.DataSource db) 
        throws Exception {
        
        Connection dbConnection = db.getConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM " + tableName + " LIMIT 1;");
        ResultSetMetaData metaData = result.getMetaData();
        
        // initialize some local convenience variables        
        String columnName;
        String columnTypeName;
        AttributeType[] attributes = new AttributeType[metaData.getColumnCount() -1];

        int offset = 1;
        //_log.debug("about to loop through cols");
        // loop through all columns
        for( int i = 1, n = metaData.getColumnCount(); i <= n; i++) {
            LOGGER.finer("reading col: " + i);
            LOGGER.finer("reading col: " + metaData.getColumnTypeName(i));
            LOGGER.finer("reading col: " + metaData.getColumnName(i));

            columnTypeName = metaData.getColumnTypeName(i);
            columnName = metaData.getColumnName(i);

            // geometry is treated specially
            if( columnTypeName.equals("geometry")) {
                attributes[i - offset] = 
                    getGeometryAttribute(db, tableName, columnName);
            }

            // object id is ignored in the schema, since it is treated as a 
            //  feature id
            else if (columnName.equals("gid")) {
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
            LOGGER.fine("Error closing result set.");
        } 

        //LOGGER.fine("the postgis-created schema is: " + FeatureTypeFactory.create(attributes).toString());
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
        
        String sqlStatement = "SELECT type, srid FROM GEOMETRY_COLUMNS WHERE " + 
            "f_table_name='" + tableName + "' AND f_geometry_column='" + 
            columnName + "';";
        String geometryType = null;

        // retrieve the result set from the JDBC driver
        //LOGGER.fine("about to make connection, SQL: " + sqlStatement);
        Connection dbConnection = db.getConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery(sqlStatement);
        
        // loop through entire result set or until maxFeatures are reached
        //LOGGER.fine("about to read geometry type");
        if( result.next()) {
            geometryType = result.getString("type");
	    srid = result.getInt("srid");

            LOGGER.fine("geometry type is: " + geometryType);
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
    public static String makeSql(Filter filter, String tableName, FeatureType schema) {
        StringBuffer sqlStatement = new StringBuffer("SELECT gid,");
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
	    encoder.setSRID(srid);
	    String where = "";
	    LOGGER.finer("about to encode");
	    if (filter != null) {
		try {    
		    where = encoder.encode((AbstractFilter)filter);   
		} catch (SQLEncoderException e) { 
		    LOGGER.fine("Encoder error" + e.getMessage());
		}
		
	    }
	    sqlStatement.append(" FROM " + tableName +" "+ where + " LIMIT " + maxFeatures + ";").toString();
	    LOGGER.finer("sql statement is " + sqlStatement);
	    return sqlStatement.toString();            
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
            LOGGER.warning("Error closing result set.");
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
            //LOGGER.fine("about to make connection");
            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();

            // if no schema has been passed to the datasource, roll our own
            if( schema == null) {
                schema = makeSchema(tableName, db);
            }
            //LOGGER.fine("just made schema: " + schema.toString());

            //LOGGER.fine("about to run a query: " + makeSql(filter, tableName, schema));
	    unpacker.unPackAND(filter);
            ResultSet result = statement.executeQuery( makeSql(unpacker.getSupported(), tableName, schema));

            // set up a factory, attributes, and a counter for feature creation
            //LOGGER.fine("about to prepare feature reading");
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
                LOGGER.fine("reading feature: " + resultCounter);
                //LOGGER.fine("geometry position: " + geometryPosition);
                for( col = 0; col < totalAttributes; col++) {
                    //LOGGER.fine("reading attribute: " + col);
                    if( col == geometryPosition) {
                        //LOGGER.fine("found geometry: " + geometryReader.read( result.getString(col + 2)));
                        attributes[col] = geometryReader.read( result.getString(col + 2));
                    }
                    else {
                        //LOGGER.fine("found attribute: " + result.getString(col + 2));
                        attributes[col] = result.getObject(col + 2);
                    }
                }
        
                // add a feature to the collection and increment result counter
                features.add( factory.create(attributes, featureId));
                resultCounter++;
            }								
            
	    //get rid of features that the encoder couldn't handle.
	    filterFeatures(features, unpacker.getUnSupported());


            // add features to collection and close the result set
            collection.addFeatures((Feature[]) features.
                                   toArray(new Feature[features.size()]));				
            closeResultSet(result);
        }
        catch(SQLException e) {
            LOGGER.warning("Some sort of database connection error: " + e.getMessage());
        }
        catch(SchemaException e) {
            LOGGER.warning("Had problems creating the feature type..." + e.getMessage());
        }
        catch(Exception e) {
            LOGGER.warning("Error from the result set: " + e.getMessage());
            LOGGER.warning( e.toString() );
            e.printStackTrace();
        }

    }


    /**
     * Runs through a list of features and returns a list of the
     * ones that are contained by the filter.  Hopefully when
     * unpacking filters has more support this will do relatively
     * little, as the SQL statement should do the majority of the filtering
     *
     * @param features The list to be tested.
     * @param filter The filter to test with.
     */
    private void filterFeatures(List features, Filter filter){
	if (filter != null) {
	    List filteredFeatures = new ArrayList(maxFeatures);
	    for (int i = 0; i < features.size(); i++){
		if (!filter.contains((Feature) features.get(i))){
		    features.remove(i);
                i--; //remove shifts index, so we must compensate
		}
	    }
	}
    }

    /**
     * Returns a feature collection, based on the passed filter.
     *
     * @param collection Add features to the PostGIS database.
     */ 
    public void addFeatures(FeatureCollection collection)
        throws DataSourceException {
        Feature[] featureArr;
        AttributeType[] attributeTypes;
        int geomPos;
        int curFeature;
        Object[] curAttributes;
        String sql = "";
        String featureID;
        String geomSql = "";
        int numAttributes;
        String attrValue = "";
	String geoText = "";
        //int gid;
        //Geometry curGeom = null;
       
	
	featureArr = collection.getFeatures();
        if (featureArr.length > 0) {
	    schema = featureArr[0].getSchema();
	    //TODO: check to make sure schema is same for feature collection
	    attributeTypes = schema.getAttributeTypes(); 
	    numAttributes = attributeTypes.length;     
	    geomPos = schema.getDefaultGeometry().getPosition();
	    try { 
		Connection dbConnection = db.getConnection();
		Statement statement = dbConnection.createStatement();
		for (int i = 0; i < featureArr.length; i++){
		    curAttributes = featureArr[i].getAttributes();
		    
		//need to change this...get names of cols from schema
		    sql = "INSERT INTO " + tableName + 
			" VALUES(";
		    
		    featureID = featureArr[i].getId(); 
		    sql += addQuotes(featureID) + ", ";
		    for (int j = 0; j < curAttributes.length; j++){
			if (j == geomPos) {
			    geoText =  geometryWriter.write((Geometry)curAttributes[j]);
			    sql += "GeometryFromText('" + geoText + "', " + srid + ")"; 
			} else {
			    attrValue = addQuotes(curAttributes[j]);
			    sql += attrValue;
			}			
			
			if (j < curAttributes.length - 1){
			    sql += ", ";
			}
		    }
		    sql += ");";
		    //_log.info("this sql statement = " + sql);
		    statement.executeUpdate(sql);
		}
		statement.close();
		dbConnection.close();
	    } catch (SQLException e) {
		//_log.info("Some sort of database connection error: " + e.getMessage());
	    }    
	}


    }



    /**
     * Adds quotes to an object for storage in postgis.  The object should
     * be a string or a number.  To perform an insert strings need quotes
     * around them, and numbers work fine with quotes, so this method can
     * be called on unknown objects.
     *
     * @param value The object to add quotes to.
     * @return a string representation of the object with quotes.
     */
    private String addQuotes(Object value){
        String retString;
        retString = "'" + value.toString() + "'";
        return retString;
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
	Feature[] featureArr;
        Object[] curAttributes;
        String sql = "";
        Object featureID;
        String geomSql = "";
        String attrValue = "";
        Object gidValue;
        String gid = null;
        AttributeType fidType;
        String gidName;

	unpacker.unPackOR(filter);
	String whereStmt = null;
	Filter encodableFilter = unpacker.getSupported();
	Filter unEncodableFilter = unpacker.getUnSupported();
	
	try {
	    Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
	    
	    if (encodableFilter != null) {
		whereStmt = encoder.encode((AbstractFilter)encodableFilter);
		sql = "DELETE from " + tableName + " " + whereStmt + ";";
		//do actual delete
		//_log.info("sql statment is " + sql);
		statement.executeUpdate(sql);

	    }
	    
	    if (unEncodableFilter != null) {
		
		featureArr = getFeatures(unEncodableFilter).getFeatures();
		if (featureArr.length > 0) {
		    sql = "DELETE FROM "  + tableName + " WHERE "; 
		    for (int i = 0; i < featureArr.length; i++){
			gidValue = featureArr[i].getId();
			gid = addQuotes(gidValue);
			sql += GID_NAME + " = " + gid;
			//is there always going to be a field called gid?
			if (i < featureArr.length - 1) {
			    sql += " OR ";
			} else {
			    sql += ";";
			}
			
		    }
		    //_log.info("our delete says : " + sql);
		    statement.executeUpdate(sql);
		}
            }

	    statement.close();
	    dbConnection.close();    
        } catch (SQLException e) {
	    LOGGER.fine("Error with sql " + e.getMessage());
        } catch (SQLEncoderException e) {
	    LOGGER.fine("error encoding sql from filter " + e.getMessage());
	}

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
	Feature[] featureArr;
        Object[] curAttributes;
        String sql = "";
        Object featureID;
        String geomSql = "";
        String attrValue = "";
        Object gidValue;
        String gid = null;
        AttributeType fidType;
        String gidName;
	//        int gid = 0;
        //int gidPos;


	//check schema has filter???

	unpacker.unPackOR(filter);
	String whereStmt = null;
	Filter encodableFilter = unpacker.getSupported();
	Filter unEncodableFilter = unpacker.getUnSupported();
	
	try {
	    Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
	    
	    if (encodableFilter != null) {
		whereStmt = encoder.encode((AbstractFilter)encodableFilter);
		sql = makeModifySql(type, value, whereStmt);
		//_log.info("encoded modify is " + sql);
		statement.executeUpdate(sql);
	    }
	    
	    if (unEncodableFilter != null) {
		
		featureArr = getFeatures(unEncodableFilter).getFeatures();
		if (featureArr.length > 0) {
		   whereStmt = " WHERE "; 
		    for (int i = 0; i < featureArr.length; i++){
			gidValue = featureArr[i].getId();
			gid = addQuotes(gidValue);
			whereStmt += GID_NAME + " = " + gid;
			//is there always going to be a field called gid?
			if (i < featureArr.length - 1) {
			    whereStmt += " OR ";
			}	
		    }
		    sql = makeModifySql(type, value, whereStmt);
		    //_log.info("unencoded modify is : " + sql);
		    statement.executeUpdate(sql);
		}
            }

	    statement.close();
	    dbConnection.close();    
        } catch (SQLException e) {
              LOGGER.fine("Error with sql " + e.getMessage());
        } catch (SQLEncoderException e) {
	    LOGGER.fine("error encoding sql from filter " + e.getMessage());
	}


    }

        /**
     * Creates a sql update statement.
     *
     * @param type the attribute to be changed.
     * @param value the value to change it to.
     * @param feature the feature to update.
     * @return an update sql statement.
     */ 
    private String makeModifySql(AttributeType type, Object value, String whereStmt){
        String sql;
        String newValue;  
	if (Geometry.class.isAssignableFrom(type.getType())) {
	    //create the text to add geometry
	    String geoText =  geometryWriter.write((Geometry)value);
	    newValue = "GeometryFromText('" + geoText + "', " + srid + ")"; 
	} else {
	    //or add quotes, covers rest of cases
	    newValue = addQuotes(value); 
	}
        //TODO error checking to make sure type matches schema
        sql = "UPDATE " + tableName + " SET " + 
            type.getName() + " = " + newValue + " " + whereStmt + ";";
	
        return sql;
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
