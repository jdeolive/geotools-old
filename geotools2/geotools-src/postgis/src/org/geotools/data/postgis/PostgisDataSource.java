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
//import java.sql.SQLException;
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
 * @version $Id: PostgisDataSource.java,v 1.18 2003/03/15 00:00:20 cholmesny Exp $
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
    private static final int HARD_MAX_FEATURES = 100000;

    /** The maximum features allowed by the server for any given response. */
    private int maxFeatures = HARD_MAX_FEATURES;

    public static final String DEFAULT_FID_COLUMN = "oid";

    /** The srid of the data in the table. */
    private int srid;

 /** To create the sql where statement */
    private SQLEncoderPostgis encoder = new SQLEncoderPostgis();

    /** the name of the column to use for the featureId */
    private String fidColumn;

    /** The maximum features allowed by the server for any given response. */
    private FeatureType schema = null;

    /** A postgis connection. */
    private Connection dbConnection;

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
    public PostgisDataSource (Connection dbConnection, String tableName) 
	throws DataSourceException{
        // create the return response type
        this.dbConnection = dbConnection;
        this.tableName = tableName;
	this.fidColumn = getFidColumn(dbConnection, tableName);
	try {
	    this.schema = makeSchema(tableName, dbConnection, fidColumn);
	} catch (Exception e) {
	    throw new DataSourceException("Couldn't make schema: " + e);
	}
	this.srid = getSrid();
	encoder.setSRID(srid);

    }

    /**
     * Sets the table and datasource, rolls a new schema from the db.
     *
     * @param db The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param maxFeatures The maximum numbers of features to return.
     */

    public PostgisDataSource (Connection dbConnection, String tableName, 
			      int maxFeatures) throws DataSourceException {
        // create the return response type
	this(dbConnection, tableName);
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
    public PostgisDataSource(Connection dbConnection, String tableName, 
			     FeatureType schema) throws DataSourceException {
	this.dbConnection = dbConnection;
	this.tableName = tableName;
	this.schema = schema;
	this.srid = getSrid();
	encoder.setSRID(srid);
	this.fidColumn = getFidColumn(dbConnection, tableName);
    }

    /**
     * Sets the table, datasource, schema and maxFeature.
     *
     * @param db The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param schema the attributes and id held by this table of features.
     * @param maxFeatures The maximum numbers of features to return.
     */
    public PostgisDataSource(Connection dbConnection, String tableName, 
			     FeatureType schema, int maxFeatures) 
	throws DataSourceException {
	this(dbConnection, tableName, schema);
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
     * @param dbConnection The connection to the database holding the table.
     * @return the schema reflecting features held in the table.
     */ 
   public static FeatureType makeSchema(String tableName, 
					 java.sql.Connection dbConnection) 
	throws DataSourceException {
	return makeSchema(tableName, dbConnection, 
			  getFidColumn(dbConnection, tableName));
    }

     /**
     * Creates a schema from the information in the tablename.
     *
     * @param tableName The name of the table that holds the features.
     * @param dbConnection The connection to the database holding the table.
     * @param fidColumnName the name of the column to use as the fid.
     * @return the schema reflecting features held in the table.
     */ 
    public static FeatureType makeSchema(String tableName, 
					 java.sql.Connection dbConnection,
					 String fidColumnName) 
        throws DataSourceException {

        try {
        Statement statement = dbConnection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM " 
						  + tableName + " LIMIT 1;");
        ResultSetMetaData metaData = result.getMetaData();
        // initialize some local convenience variables        
        String columnName;
        String columnTypeName;
	int attributeCount = metaData.getColumnCount();
	if (!fidColumnName.equals(DEFAULT_FID_COLUMN)) {
	    attributeCount--;
	}
        AttributeType[] attributes = 
	    new AttributeType[attributeCount]; //-1];

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
                    getGeometryAttribute(dbConnection, tableName, columnName);
            } else if (columnName.equals(fidColumnName)){ 
		//do nothing, fid does not have a proper attribute type.
		offset++;
	    }
	    else  {
                // set column name and type from database
                attributes[i - offset] = 
                    new AttributeTypeDefault ( columnName,
                                               (Class) sqlTypeMap.get
					       (columnTypeName));
            }
        }
	closeResultSet(result);
	FeatureType retSchema =  
	    FeatureTypeFactory.create(attributes).setTypeName(tableName);
	if (retSchema.getClass().isAssignableFrom(FeatureTypeFlat.class)) {
	    //((FeatureTypeFlat)retFeature).setSRID(srid); 
	    // first way depends on static srid, which could change if another
	    //object calls the static method.  querySRID is slower, as 
	    //another connection must be made, but will be sure to get it right
	  int srid = querySRID(dbConnection, tableName);
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
     * @tasks: REVISIT: consider doing away with assigning srid from schema.
     * Just ignore the srid of the schema coming in?  Because it always should
     * be the same as the postgis backend.
     */
    private int getSrid() {
	int srid = 0;
	if (schema.getClass().isAssignableFrom(FeatureTypeFlat.class)) {
	    srid = ((FeatureTypeFlat)schema).getSRID();
	    if (srid != 0) return srid; //if 0 then it was not initialized, 
	}    //so it should be found with querySRID.
	try {
	    srid = querySRID(dbConnection, tableName);  //this will slow things 
	} catch (Exception e) {     //srid should be set in schema.
	    //TODO: error checking here.
	}
	return srid;
    }

    /**
     * Gets the srid from the geometry_columns table of the datasource.
     *
     * @param dbConnection The connection to the database.
     * @param tableName the name of the table to find the srid.
     * @return the srid of the first geometry column of the table.
     * @tasks REVISIT: only handles one geometry column, should take
     * the column name if we have more than one srid per feature.
     */
    public static int querySRID(Connection dbConnection, String tableName) 
	throws DataSourceException {
	try {
	String sqlStatement = "SELECT srid FROM GEOMETRY_COLUMNS WHERE " + 
            "f_table_name='" + tableName + "';";

        // retrieve the result set from the JDBC driver
        //LOGGER.fine("about to make connection, SQL: " + sqlStatement);
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
	catch(SQLException e) {
	    String message = "Some sort of database connection error: " 
		+ e.getMessage();
		LOGGER.warning(message);
	    throw new DataSourceException(message, e);
	}

    }

    /**
     * Figures out what database column to use as the identifier for the 
     * feature.  For now it first tries to use the primary key (which should
     * be the fid according to sfs for sql), and if there are none, then it
     * uses the postgres specific invisible oid column.
     *
     * @param dbConnection The connection to the database.
     * @param tableName The name of the table to get the id for.
     * @return the name of the column to use as the fid.
     * @tasks REVISIT: right now most postgis datasources probably won't have
     * primary keys declared, but that should start to change if the next 
     * shp2pgsql declares primary keys.  getFeatures now works good with 
     * primary keys, it returns properly.  But insert does not work, and will
     * be tricky.  
     */
   public static String getFidColumn(Connection dbConnection, 
				      String tableName) {
       String retString = DEFAULT_FID_COLUMN;
       try {
	   DatabaseMetaData dbMeta = dbConnection.getMetaData();
 	   //TODO: get values for catalog and schema?  this could mess up
	   //if there are tables of same name and different catalog.
	   ResultSet pkeys = dbMeta.getPrimaryKeys(null, null, tableName);
	   if (pkeys.next()) {
 	       //get the name of the primary key column
 	       retString = pkeys.getString(4);
 	       //TODO: Figure out what to do if there are multiple pks
 	   }
        } catch (SQLException e) {
 	   //do nothing, just use OID
 	   }
       return retString;
   }

    /**
     * Returns an attribute type for a geometry column in a feature table.
     *
     * @param dbConnection The JDBC connection.
     * @param tableName The feature table name.
     * @param columnName The geometry column name.
     * @return Geometric attribute.
     * @tasks REVISIT: combine with querySRID, as they use the same select 
     * statement.
     */ 
    private static AttributeType getGeometryAttribute(Connection dbConnection, 
                                                      String tableName, 
                                                      String columnName) 
        throws Exception {

        String sqlStatement = "SELECT type FROM GEOMETRY_COLUMNS WHERE " + 
            "f_table_name='" + tableName + "' AND f_geometry_column='" + 
            columnName + "';";
        String geometryType = null;

        // retrieve the result set from the JDBC driver
        //LOGGER.fine("about to make connection, SQL: " + sqlStatement);
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
	StringBuffer sqlStatement = new StringBuffer("SELECT ");
	sqlStatement.append(fidColumn);
        AttributeType[] attributeTypes = schema.getAttributeTypes();
	int numAttributes = attributeTypes.length;
 
        for( int i = 0; i < numAttributes; i++) {
	    String curAttName = attributeTypes[i].getName();
            if( Geometry.class.isAssignableFrom( attributeTypes[i].getType())) {
                sqlStatement.append(", AsText(" + curAttName + ")");
	    } //REVISIT, see getIdColumn note.
            else if (fidColumn.equals(curAttName)) {
		//do nothing, already covered by fid
	    } else {
                sqlStatement.append(", " + curAttName);
            }
	}
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
            //result.getStatement().getConnection().close();			
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
            Statement statement = dbConnection.createStatement();

            // if no schema has been passed to the datasource, roll our own
            //LOGGER.info("schema = " + schema);
	    unpacker.unPackAND(filter);
	    //if there is no filter applied after the sql select statement then
	    //we can use the maxFeatures in the statement.  If not we have to 
	    //filter after (which is a huge memory hit with large datasets)
	    boolean useLimit = (unpacker.getUnSupported() == null);
	    LOGGER.finest("passed in " + unpacker.getSupported() + 
			  tableName + schema + useLimit);
	    String sql = makeSql(unpacker.getSupported(), tableName, schema, useLimit);
            ResultSet result = statement.executeQuery( sql);

            // set up a factory, attributes, and a counter for feature creation
            //LOGGER.fine("about to prepare feature reading");
            FeatureFactory factory = new FeatureFactory(schema);
            Object[] attributes = new Object[schema.attributeTotal()];
            String featureId;
	    AttributeType[] attTypes = schema.getAttributeTypes();
            int resultCounter = 0;
            int totalAttributes = schema.attributeTotal();
            int col;                
	    Filter featureFilter = unpacker.getUnSupported();
            // loop through entire result set or until maxFeatures are reached
	    
            while( result.next() && ( resultCounter < maxFeatures)) {

                // grab featureId, which always appears first 
                featureId = result.getString(1);
		//featureId's can't start with numbers.
		if (Character.isDigit(featureId.charAt(0))){
		    //so prepend the table name.
		    featureId = tableName + "." + featureId;
		}

                // create an individual attribute by looping through columns
                //LOGGER.finer("reading feature: " + resultCounter);
                for( col = 0; col < totalAttributes; col++) {
                    if( attTypes[col].isGeometry()) {
			attributes[col] = geometryReader.read
			    ( result.getString(col + 2));
                    }
                    else {
                        attributes[col] = result.getObject(col + 2);
                    }
                }
		Feature curFeature = factory.create(attributes, featureId);
		//LOGGER.finest("testing feature " + curFeature + " with filter: " + featureFilter);
		if ((featureFilter == null) || featureFilter.contains(curFeature)) {
		    LOGGER.finest("adding feature: " + curFeature);
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
     * @tasks TODO: change to return an array of featureIds that were inserted.
     * @tasks TODO: get working with the primary key fid column.  This will
     * currently just insert nulls for the fids if oid is not being used
     * as the column.  We probably need a sequence to generate the fids.
     * Or if the fid is supposed to be part of the insert (which doesn't make
     * sense if we return fids), then we should check for uniqueness.
     */ 
    public void addFeatures(FeatureCollection collection)
        throws DataSourceException {
	Feature[] featureArr = collection.getFeatures();
        if (featureArr.length > 0) {
	    try { 
		Statement statement = dbConnection.createStatement();
		for (int i = 0; i < featureArr.length; i++){
		    String sql = makeInsertSql(tableName, featureArr[i]);
		    LOGGER.finer("this sql statement = " + sql);
		    statement.executeUpdate(sql);
		}
		statement.close();
	    } catch (SQLException e) {
		String message = "Some sort of database connection error: " 
		+ e.getMessage();
		LOGGER.warning(message);
		throw new DataSourceException(message, e);
	    }    
	}
    }

    /**
     * Creates a sql insert statement.  Uses each feature's schema, which
     * makes it possible to insert out of order, as well as inserting
     * less than all features.
     *
     * @param tableName the name of the feature table being inserted into.
     * @param feature the feature to add.
     * @return an insert sql statement.
     */ 
    private String makeInsertSql(String tableName, Feature feature){
	String attrValue = new String();
	StringBuffer sql = new StringBuffer("INSERT INTO " + tableName + "(");
	FeatureType featureSchema = feature.getSchema();
	AttributeType geometryAttr = featureSchema.getDefaultGeometry();
	int geomPos = -1;
	if (geometryAttr != null) {
	    geomPos = geometryAttr.getPosition();
	}
	AttributeType[] types = featureSchema.getAttributeTypes();
	for (int i = 0; i < types.length; i++) {
	    sql.append(types[i].getName());
	    sql.append((i < types.length - 1) ? ", " : ") ");
	} 
	sql.append("VALUES (");
	Object[] attributes = feature.getAttributes();
	for (int j = 0; j < attributes.length; j++){
	    if (j == geomPos) {
		String geoText = geometryWriter.write((Geometry)attributes[j]);
		sql.append("GeometryFromText('" + geoText + 
		    "', " + srid + ")"); 
	    } else {
		attrValue = addQuotes(attributes[j]);
		sql.append(attrValue);
	    }			
	    
	    if (j < attributes.length - 1){
		sql.append(", ");
	    }
	}
	sql.append(");");
	return sql.toString();
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
        String sql = "";
        Object featureID;
        String attrValue = "";
        String fid = null;
	String whereStmt = null;

	unpacker.unPackOR(filter);
	Filter encodableFilter = unpacker.getSupported();
	Filter unEncodableFilter = unpacker.getUnSupported();
	
	try {
	    //Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
	    if (encodableFilter != null) {
		whereStmt = encoder.encode((AbstractFilter)encodableFilter);
		sql = "DELETE from " + tableName + " " + whereStmt + ";";
		//do actual delete
		LOGGER.fine("sql statment is " + sql);
		statement.executeUpdate(sql);
	    }
	    
	    if (unEncodableFilter != null) {
		featureArr = getFeatures(unEncodableFilter).getFeatures();
		if (featureArr.length > 0) {
		    sql = "DELETE FROM "  + tableName + " WHERE "; 
		    for (int i = 0; i < featureArr.length; i++){
			fid = formatFid(featureArr[i]);
			sql += fidColumn + " = " + fid;
			if (i < featureArr.length - 1) {
			    sql += " OR ";
			} else {
			    sql += ";";
			}
		    }
		    LOGGER.fine("our delete says : " + sql);
		    statement.executeUpdate(sql);
		}
            }
	    statement.close();
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
	String fid = null;
	//check schema with filter???
	
	unpacker.unPackOR(filter);
	String whereStmt = null;
	Filter encodableFilter = unpacker.getSupported();
	Filter unEncodableFilter = unpacker.getUnSupported();
	
	try {
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
			fid = formatFid(featureArr[i]);
			whereStmt += fidColumn + " = " + fid;
			if (i < featureArr.length - 1) {
			    whereStmt += " OR ";
			}	
		    }
		    sql = makeModifySql(type, value, whereStmt);
		    LOGGER.finer("unencoded modify is " + sql);
		    statement.executeUpdate(sql);
		}
            }
	    statement.close();
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

    /** strips the tableName from the fid for those in the format featureName.3534
     * should maybe just strip out all alpha-numeric characters.
     */
    private String formatFid(Feature feature){
	String fid = feature.getId();
	if (fid.startsWith(tableName)) {
	    //take out the tableName and the .
	    fid = fid.substring(tableName.length() + 1);
	}
	return addQuotes(fid);
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
