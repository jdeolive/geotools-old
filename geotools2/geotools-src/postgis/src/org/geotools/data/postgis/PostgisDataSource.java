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
 * @version $Id: PostgisDataSource.java,v 1.14 2003/01/16 21:36:54 cholmesny Exp $
 * @author Rob Hranac, Vision for New York
 * @author Chris Holmes, TOPP
 */
public class PostgisDataSource implements org.geotools.data.DataSource {


    private static Map sqlTypeMap = new HashMap();

    private static Map geometryTypeMap = new HashMap();

    static {
	initMaps();
    }

  /**
     * The logger for the filter module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.postgis");
    
    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();
    
    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);
    
        /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();

    /** The limit on a select statement. */
    private static final int HARD_MAX_FEATURES = 10000;

    /** The maximum features allowed by the server for any given response. */
    private int maxFeatures = HARD_MAX_FEATURES;

    /** The srid of the data in the table. */
    private int srid;

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

    /**
     * Sets the table and datasource, rolls a new schema from the db.
     *
     * @param db The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     */
    public PostgisDataSource (javax.sql.DataSource db, String tableName) 
	throws DataSourceException{
        // create the return response type
        this.db = db;
        this.tableName = tableName;
	try {
	    this.schema = makeSchema(tableName, db);
	} catch (Exception e) {
	    throw new DataSourceException("Couldn't make schema: " + e);
	}
	this.srid = getSrid();
    }

    /**
     * Sets the table and datasource, rolls a new schema from the db.
     *
     * @param db The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param maxFeatures The maximum numbers of features to return.
     */

    public PostgisDataSource (javax.sql.DataSource db, String tableName, 
			      int maxFeatures) throws DataSourceException {
        // create the return response type
	this(db, tableName);
        this.maxFeatures = maxFeatures;
    }

   /**
     * Sets the table, datasource and schema.  This is a convenience method
     * for greater speed.  It does no type-checking on the schema, so 
     * things will break if the schema passed in and that held by the 
     * datasource don't match up.  
     *
     * @param db The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param schema the attributes and id held by this table of features.
     * @tasks REVISIT: type-check the schema?  Would sacrifice the speed
     * gained by passing in schema, so might not be worth it.
     */
    public PostgisDataSource(javax.sql.DataSource db, String tableName, 
			     FeatureType schema) throws DataSourceException {
	this.db = db;
	this.tableName = tableName;
	this.schema = schema;
	this.srid = getSrid();
    }

    /**
     * Sets the table, datasource, schema and maxFeature.
     *
     * @param db The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param schema the attributes and id held by this table of features.
     * @param maxFeatures The maximum numbers of features to return.
     */
    public PostgisDataSource(javax.sql.DataSource db, String tableName, 
			     FeatureType schema, int maxFeatures) 
	throws DataSourceException {
	this(db, tableName, schema);
	this.maxFeatures = maxFeatures;
    }

    /**
     * Initializes the mappings for mapping from sql columns to classes
     * for attributes
     */
    private static void initMaps() {
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
     * Creates a schema from the information in the tablename.
     *
     * @param tableName The name of the table that holds the features.
     * @param db The datasource to generate a connection to the database 
     * holding the table.
     * @return the schema reflecting features held in the table.
     */ 
    public static FeatureType makeSchema(String tableName, 
                                          javax.sql.DataSource db) 
        throws DataSourceException {

        try {
        Connection dbConnection = db.getConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM " 
						  + tableName + " LIMIT 1;");
        ResultSetMetaData metaData = result.getMetaData();
        
        // initialize some local convenience variables        
        String columnName;
        String columnTypeName;
        AttributeType[] attributes = 
	    new AttributeType[metaData.getColumnCount()]; //-1];

        int offset = 1;
        //_log.debug("about to loop through cols");
        // loop through all columns
        for( int i = 1, n = metaData.getColumnCount(); i <= n; i++) {
            LOGGER.fine("reading col: " + i);
            LOGGER.fine("reading col: " + metaData.getColumnTypeName(i));
            LOGGER.finer("reading col: " + metaData.getColumnName(i));

            columnTypeName = metaData.getColumnTypeName(i);
            columnName = metaData.getColumnName(i);

            // geometry is treated specially
            if( columnTypeName.equals("geometry")) {
                attributes[i - offset] = 
                    getGeometryAttribute(db, tableName, columnName);
            } else  {
                // set column name and type from database
                attributes[i - offset] = 
                    new AttributeTypeDefault ( columnName,
                                               (Class) sqlTypeMap.get
					       (columnTypeName));
            }
        }
	closeResultSet(result);

        //LOGGER.fine("the postgis-created schema is: " 
	//+ FeatureTypeFactory.create(attributes).toString());
	FeatureType retSchema =  
	    FeatureTypeFactory.create(attributes).setTypeName(tableName);
	if (retSchema.getClass().isAssignableFrom(FeatureTypeFlat.class)) {
	    //((FeatureTypeFlat)retFeature).setSRID(srid); 
	    // first way depends on static srid, which could change if another
	    //object calls the static method.  querySRID is slower, as 
	    //another connection must be made, but will be sure to get it right
	  int srid = querySRID(db, tableName);
	((FeatureTypeFlat)retSchema).setSRID(srid);
	}
	return retSchema;
	}
	catch(SQLException e) {
	    String message = "Some sort of database connection error: " 
		+ e.getMessage();
		LOGGER.warning(message);
	    throw new DataSourceException(message, e);
	}
	catch(SchemaException e) {
	    String message = "Had problems creating the feature type..." 
			   + e.getMessage();
	    LOGGER.warning(message);
	    throw new DataSourceException(message, e);
	}
        catch(Exception e) {
	    String message = "Error from the result set: " + e.getMessage();
            LOGGER.warning(message);
            LOGGER.warning( e.toString() );
            e.printStackTrace();
	    throw new DataSourceException(message, e);
        }


    }

    /**
     * Convenience method to get the srid.  Grabs it from the schema if
     * possible, if not then it queries the datasource.
     *
     * @return the srid of this schema.
     */
    private int getSrid() {
	int srid = 0;
	if (schema.getClass().isAssignableFrom(FeatureTypeFlat.class)) {
	    srid = ((FeatureTypeFlat)schema).getSRID();
	    if (srid != 0) return srid; //if 0 then it was not initialized, 
	}    //so it should be found with querySRID.
	try {
	    srid = querySRID(db, tableName);  //this will slow things considerably
	} catch (Exception e) {     //srid should be set in schema.
	    //TODO: error checking here.
	}
	return srid;
    }

    /**
     * Gets the srid from the geometry_columns table of the datasource.
     *
     * @param db The datasource used to generate the connection.
     * @param tableName the name of the table to find the srid.
     * @return the srid of the first geometry column of the table.
     * @tasks REVISIT: only handles one geometry column, should take
     * the column name if we have more than one srid per feature.
     */
    public static int querySRID(javax.sql.DataSource db, String tableName) 
	throws Exception {
	 String sqlStatement = "SELECT srid FROM GEOMETRY_COLUMNS WHERE " + 
            "f_table_name='" + tableName + "';";

        // retrieve the result set from the JDBC driver
        //LOGGER.fine("about to make connection, SQL: " + sqlStatement);
        Connection dbConnection = db.getConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery(sqlStatement);
	if( result.next()) {
	    int retSrid = result.getInt("srid");
	    closeResultSet(result);
	    return retSrid;
        } else {
	    throw new DataSourceException("problem querying the db for srid " +
					  "of " + tableName);
	}

    }

    /**
     * Returns an attribute type for a geometry column in a feature table.
     *
     * @param db The JDBC data source.
     * @param tableName The feature table name.
     * @param columnName The geometry column name.
     * @return Geometric attribute.
     * @tasks REVISIT: combine with querySRID, as they use the same select 
     * statement.
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
        //LOGGER.fine("about to make connection, SQL: " + sqlStatement);
        Connection dbConnection = db.getConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery(sqlStatement);
                
        //LOGGER.fine("about to read geometry type");
        if( result.next()) {
            geometryType = result.getString("type");
	    LOGGER.fine("geometry type is: " + geometryType);
        }
        
        closeResultSet(result);

        return new AttributeTypeDefault ( columnName,
                                          (Class) geometryTypeMap.get
					  (geometryType));

    }


    /**
     * Creates a SQL statement for the PostGIS database.
     *
     * @return Full SQL statement.
     */ 
    public String makeSql(Filter filter, String tableName,
			  FeatureType schema, boolean useLimit) 
	throws DataSourceException{
        StringBuffer sqlStatement = new StringBuffer("SELECT");
        AttributeType[] attributeTypes = schema.getAttributeTypes();

        for( int i = 0; i < attributeTypes.length; i++) {
            if( Geometry.class.isAssignableFrom( attributeTypes[i].getType())) {
                sqlStatement.append(" AsText(" + 
				    attributeTypes[i].getName() + ")");
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
		where = encoder.encode(filter);   
	    } catch (SQLEncoderException e) { 
		String message = "Encoder error" + e.getMessage();
		LOGGER.warning(message);
		LOGGER.warning( e.toString() );
		throw new DataSourceException(message, e);
	    }
	    
	}
	int limit = HARD_MAX_FEATURES;
	if (useLimit) {
	    limit = maxFeatures;
	}
	sqlStatement.append(" FROM " + tableName +" "+ where + " LIMIT " 
			    + limit + ";").toString();
	LOGGER.fine("sql statement is " + sqlStatement);
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
            //LOGGER.info("schema = " + schema);
	    if( schema == null) {
                schema = makeSchema(tableName, db);
            }
            //LOGGER.fine("just made schema: " + schema.toString());

            //LOGGER.fine("about to run a query: " + makeSql(filter, tableName, schema));
	    unpacker.unPackAND(filter);
	    //if there is no filter applied after the sql select statement then
	    //we can use the maxFeatures in the statement.  If not we have to 
	    //filter after (which is a huge memory hit with large datasets)
	    boolean useLimit = (unpacker.getUnSupported() == null);
	    LOGGER.finest("passed in " + unpacker.getSupported() + tableName + schema + useLimit);
	    String sql = makeSql(unpacker.getSupported(), tableName, schema, useLimit);
	    LOGGER.finest("sql is " + sql);
            ResultSet result = statement.executeQuery( sql);

            // set up a factory, attributes, and a counter for feature creation
            //LOGGER.fine("about to prepare feature reading");
            FeatureFactory factory = new FeatureFactory(schema);
            Object[] attributes = new Object[schema.attributeTotal()];
            String featureId;
	    AttributeType geometryAttr = schema.getDefaultGeometry();
	    int geometryPosition = -1;
	    if (geometryAttr != null) {
		geometryPosition = geometryAttr.getPosition();
	    }
            int resultCounter = 0;
            int totalAttributes = schema.attributeTotal();
            int col;                
	    Filter featureFilter = unpacker.getUnSupported();
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
			attributes[col] = geometryReader.read
			    ( result.getString(col + 1));
                    }
                    else {
                        //LOGGER.fine("found attribute: " + result.getString(col + 2));
                        attributes[col] = result.getObject(col + 1);
                    }
                }
		Feature curFeature = factory.create(attributes, featureId);
		//LOGGER.finest("testing feature " + curFeature + " with filter: " + featureFilter);
		if ((featureFilter == null) || featureFilter.contains(curFeature)) {
		    //  LOGGER.finest("adding feature.....");
		    features.add( curFeature);
		    resultCounter++;
		}
            }								
            
	   
            // add features to collection and close the result set
            collection.addFeatures((Feature[]) features.
                                   toArray(new Feature[features.size()]));				
            closeResultSet(result);
        }
        catch(SQLException e) {
	    String message = "Some sort of database connection error: " 
		+ e.getMessage();
            LOGGER.warning(message);
	    throw new DataSourceException(message, e);
        }
        catch(Exception e) {
	   String message = "Error from the result set: " + e.getMessage();
            LOGGER.warning(message);
            LOGGER.warning( e.toString() );
            e.printStackTrace();
	    throw new DataSourceException(message, e);

        }

    }


    /**
     * Returns a feature collection, based on the passed filter.  The
     * schema of the features passed in must match the schema
     * of the datasource.  
     *
     * @param collection Add features to the PostGIS database.
     * @tasks TODO: Check to make sure features passed in match schema.
     */ 
    public void addFeatures(FeatureCollection collection)
        throws DataSourceException {
        Feature[] featureArr;
        AttributeType[] attributeTypes;
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
	    AttributeType geometryAttr = schema.getDefaultGeometry();
	    int geomPos = -1;
	    if (geometryAttr != null) {
		geomPos = geometryAttr.getPosition();
	    }
	    try { 
		Connection dbConnection = db.getConnection();
		Statement statement = dbConnection.createStatement();
		for (int i = 0; i < featureArr.length; i++){
		    curAttributes = featureArr[i].getAttributes();
		    //need to change this...get names of cols from schema
		    sql = "INSERT INTO " + tableName + 
			" VALUES(";
		    //featureID = featureArr[i].getId(); 
		    //sql += addQuotes(featureID) + ", ";
		    for (int j = 0; j < curAttributes.length; j++){
			if (j == geomPos) {
			    geoText = geometryWriter.write((Geometry)curAttributes[j]);
			    sql += "GeometryFromText('" + geoText + 
				"', " + srid + ")"; 
			} else {
			    attrValue = addQuotes(curAttributes[j]);
			    sql += attrValue;
			}			
			
			if (j < curAttributes.length - 1){
			    sql += ", ";
			}
		    }
		    sql += ");";
		    LOGGER.finer("this sql statement = " + sql);
		    statement.executeUpdate(sql);
		}
		statement.close();
		dbConnection.close();
	    } catch (SQLException e) {
		String message = "Some sort of database connection error: " 
		+ e.getMessage();
		LOGGER.warning(message);
		throw new DataSourceException(message, e);
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
        //Object[] curAttributes;
        String sql = "";
        Object featureID;
        //String geomSql = "";
        String attrValue = "";
        Object fidValue;
        String fid = null;
        String fidName = schema.getAttributeTypes()[0].getName();

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
		LOGGER.finer("sql statment is " + sql);
		statement.executeUpdate(sql);
	    }
	    
	    if (unEncodableFilter != null) {
		featureArr = getFeatures(unEncodableFilter).getFeatures();
		if (featureArr.length > 0) {
		    sql = "DELETE FROM "  + tableName + " WHERE "; 
		    for (int i = 0; i < featureArr.length; i++){

			fidValue = featureArr[i].getId();
			fid = addQuotes(fidValue);
			sql += fidName + " = " + fid;
			if (i < featureArr.length - 1) {
			    sql += " OR ";
			} else {
			    sql += ";";
			}
			
		    }
		    //LOGGER.info("our delete says : " + sql);
		    statement.executeUpdate(sql);
		}
            }

	    statement.close();
	    dbConnection.close();    
        } catch (SQLException e) {
	    String message = "Some sort of database connection error: " 
		+ e.getMessage();
		LOGGER.warning(message);
		throw new DataSourceException(message, e);
        } catch (SQLEncoderException e) {
	    String message = "error encoding sql from filter " 
		+ e.getMessage();
		LOGGER.warning(message);
		throw new DataSourceException(message, e);
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
	//TODO: throw dse if obect types don't match attribute types.  The postgis
	//database does this a bit now, but should be more fully implemented.
	Feature[] featureArr;
	Object[] curAttributes;
	String sql = "";
	Object fidValue;
	String fid = null;
	String fidName = schema.getAttributeTypes()[0].getName();
	//check schema with filter???
	
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
		LOGGER.finer("encoded modify is " + sql);
		statement.executeUpdate(sql);
	    }
	    
	    if (unEncodableFilter != null) {
		
		featureArr = getFeatures(unEncodableFilter).getFeatures();
		if (featureArr.length > 0) {
		    whereStmt = " WHERE "; 
		    for (int i = 0; i < featureArr.length; i++){
			//REVISIT: do away with id, 
			// and just query first attribute?
			fidValue = featureArr[i].getId();
			fid = addQuotes(fidValue);
			whereStmt += fidName + " = " + fid;
			
			if (i < featureArr.length - 1) {
			    whereStmt += " OR ";
			}	
		    }
		    sql = makeModifySql(type, value, whereStmt);
		    
		    LOGGER.info("unencoded modify is " + sql);
		    statement.executeUpdate(sql);
		}
            }
	    
       
	    statement.close();
	    dbConnection.close();    
	} catch (SQLException e) {
	    String message = "Some sort of database error: " 
		+ e.getMessage();
	    LOGGER.warning(message);
	    throw new DataSourceException(message, e);
	} catch (SQLEncoderException e) {
	    String message = "error encoding sql from filter " 
		+ e.getMessage();
	    LOGGER.warning(message);
		throw new DataSourceException(message, e);
	}

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
	AttributeType[] singleType = {type};
	Object[] singleVal = {value};
	modifyFeatures(singleType, singleVal, filter);
	
    }

        /**
     * Creates a sql update statement.
     *
     * @param type the attribute to be changed.
     * @param value the value to change it to.
     * @param feature the feature to update.
     * @return an update sql statement.
     */ 
    private String makeModifySql(AttributeType[] types, 
				 Object[] values, String whereStmt) 
	throws DataSourceException {
	int arrLength = types.length;
	if (arrLength == values.length) {
	    StringBuffer sqlStatement = new StringBuffer("UPDATE ");
	    sqlStatement.append(tableName + " SET ");
	    for (int i = 0; i < arrLength; i++) {
		AttributeType curType = types[i];
		Object curValue = values[i];
		String newValue;  
		//check her to make sure object matches attribute type.
		if (Geometry.class.isAssignableFrom(curType.getType())) {
		    //create the text to add geometry
		    String geoText =  geometryWriter.write((Geometry)curValue);
		    newValue = "GeometryFromText('" + geoText + "', " + srid + ")"; 
		} else {
	    //or add quotes, covers rest of cases
		    newValue = addQuotes(curValue); 
		}
		sqlStatement.append(curType.getName() + " = " + newValue);
		sqlStatement.append((i < arrLength - 1) ? ", " : " ");
	        
	    }
	    sqlStatement.append(whereStmt + ";");
        return sqlStatement.toString();
	} else {
	    throw new DataSourceException("length of value array is not " + 
					  "same length as type array");
	}
    }


    public FeatureType getSchema() {
	return schema;
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
