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

import com.vividsolutions.jts.geom.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.vividsolutions.jts.io.*;
import org.geotools.data.*;
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderPostgis;
import org.geotools.filter.SQLUnpacker;
import org.geotools.resources.Geotools;



/**
 * Connects to a Postgis database and returns properly formatted GML.
 * 
 * <p>
 * This standard class must exist for every supported datastore.
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 * @author Chris Holmes, TOPP
 * @version $Id: PostgisDataSource.java,v 1.24 2003/05/13 19:17:03 cholmesny Exp $
 */
public class PostgisDataSource extends AbstractDataSource
    implements org.geotools.data.DataSource {
    private static Map sqlTypeMap = new HashMap();
    private static Map geometryTypeMap = new HashMap();

    static {
        initMaps();
    }

    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.postgis");

    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();

    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);

    /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();

    /** The limit on a select statement. */
    private static final int HARD_MAX_FEATURES = 1000000;
    public static final String DEFAULT_FID_COLUMN = "oid";

    /** The maximum features allowed by the server for any given response. */
    private int maxFeatures = HARD_MAX_FEATURES;

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

    /**
     * To get the part of the filter incorporated into the sql where statement
     * @task TODO: this is not safe, if two getFeatures happen at once.  The 
     * unpacker should be rewritten, but for now, just use locally.
     */
    //private SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());

    /**
     * Sets the table and datasource, rolls a new schema from the db.
     *
     * @param dbConnection The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     *
     * @throws DataSourceException if there were problems constructing the
     * schema.
     *
     * @task TODO: get rid of tableName?  Would need to specify tableName in
     *       transactions...  But it would be nice to access the full db with
     *       one postgis data source.
     */
    public PostgisDataSource(Connection dbConnection, String tableName)
        throws DataSourceException {
        // create the return response type
        this.dbConnection = dbConnection;
        this.tableName = tableName;
        this.fidColumn = getFidColumn(dbConnection, tableName);

        try {
            this.schema = makeSchema(tableName, dbConnection, fidColumn);
        } catch (DataSourceException e) {
            throw new DataSourceException("Couldn't make schema: " + e, e);
        }

        this.srid = querySRID(dbConnection, tableName);

        if (schema.getDefaultGeometry() != null) {
            encoder.setDefaultGeometry(schema.getDefaultGeometry().getName());
        }
        encoder.setSRID(srid);
    }

    /**
     * Sets the table and datasource, rolls a new schema from the db.
     *
     * @param dbConnection The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param maxFeatures The maximum numbers of features to return.
     *
     * @throws DataSourceException if there were problems making schema.
     *
     * @deprecated the maxFeatures here was a hack. Use {@link
     *             #getFeatures(Query)} instead.
     */
    public PostgisDataSource(Connection dbConnection, String tableName,
        int maxFeatures) throws DataSourceException {
        // create the return response type
        this(dbConnection, tableName);
        this.maxFeatures = maxFeatures;
    }

    /**
     * Sets the table, datasource and schema.  This is a convenience method for
     * greater speed.  It does no type-checking on the schema, so  things will
     * break if the schema passed in and that held by the  datasource don't
     * match up.
     *
     * @param dbConnection The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param schema the attributes and id held by this table of features.
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @task REVISIT: type-check the schema?  Would sacrifice the speed gained
     *       by passing in schema, so might not be worth it.
     * @deprecated the passed in schema was a hack to get the right properties.
     *             Use {@link #getFeatures(Query)} instead.
     */
    public PostgisDataSource(Connection dbConnection, String tableName,
        FeatureType schema) throws DataSourceException {
        this.dbConnection = dbConnection;
        this.tableName = tableName;
        this.schema = schema;
        this.srid = querySRID(dbConnection, tableName);

        if (schema.getDefaultGeometry() != null) {
            encoder.setDefaultGeometry(schema.getDefaultGeometry().getName());
        }

        encoder.setSRID(srid);
        this.fidColumn = getFidColumn(dbConnection, tableName);
    }

    /**
     * Sets the table, datasource, schema and maxFeature.
     *
     * @param dbConnection The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @param schema the attributes and id held by this table of features.
     * @param maxFeatures The maximum numbers of features to return.
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @deprecated the maxFeatures and passed in schema here was a hack. Use
     *             {@link #getFeatures(Query)} instead.
     */
    public PostgisDataSource(Connection dbConnection, String tableName,
        FeatureType schema, int maxFeatures) throws DataSourceException {
        this(dbConnection, tableName, schema);
        this.maxFeatures = maxFeatures;
    }

    /**
     * Initializes the mappings for mapping from sql columns to classes for
     * attributes
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

    /**
     * Creates a schema from the information in the tablename.
     *
     * @param tableName The name of the table that holds the features.
     * @param dbConnection The connection to the database holding the table.
     *
     * @return the schema reflecting features held in the table.
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public static FeatureType makeSchema(String tableName,
        java.sql.Connection dbConnection) throws DataSourceException {
        return makeSchema(tableName, dbConnection,
            getFidColumn(dbConnection, tableName));
    }

    /**
     * Creates a schema from the information in the tablename.
     *
     * @param tableName The name of the table that holds the features.
     * @param dbConnection The connection to the database holding the table.
     * @param fidColumnName the name of the column to use as the fid.
     *
     * @return the schema reflecting features held in the table.
     *
     * @throws DataSourceException if there were problems reading sql or
     * making the schema.
     */
    public static FeatureType makeSchema(String tableName,
        java.sql.Connection dbConnection, String fidColumnName)
        throws DataSourceException {
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " +
                    tableName + " LIMIT 1;");
            ResultSetMetaData metaData = result.getMetaData();
            // initialize some local convenience variables        
            String columnName;
            String columnTypeName;
            int attributeCount = metaData.getColumnCount();
            if (!fidColumnName.equals(DEFAULT_FID_COLUMN)) {
                attributeCount--;
            }
            AttributeType[] attributes = new AttributeType[attributeCount]; //-1];
            int offset = 1;
            // loop through all columns
            for (int i = 1, n = metaData.getColumnCount(); i <= n; i++) {
                //LOGGER.finer("reading col: " + i);
                //LOGGER.finer("reading col: " + metaData.getColumnTypeName(i));
                LOGGER.finer("reading col: " + metaData.getColumnName(i));
                columnTypeName = metaData.getColumnTypeName(i);
                columnName = metaData.getColumnName(i);
                // geometry is treated specially
                if (columnTypeName.equals("geometry")) {
                    attributes[i - offset] = getGeometryAttribute(dbConnection,
                            tableName, columnName);
                } else if (columnName.equals(fidColumnName)) {
                    //do nothing, fid does not have a proper attribute type.
                    offset++;
                } else {
                    // set column name and type from database
                    attributes[i - offset] = new AttributeTypeDefault(columnName,
                            (Class) sqlTypeMap.get(columnTypeName));
                }
            }
            closeResultSet(result);
            FeatureType retSchema = FeatureTypeFactory.create(attributes)
                                                      .setTypeName(tableName);
            return retSchema;
        } catch (SQLException e) {
            String message = "Some sort of database connection error";
            LOGGER.warning(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        } catch (SchemaException e) {
            String message = "Had problems creating the feature type...";
	     LOGGER.warning(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        } catch (Exception e) {
            String message = "Error from the result set: " + e.getMessage();
            LOGGER.warning(message);
            LOGGER.warning(e.toString());
            e.printStackTrace();
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Gets the srid from the geometry_columns table of the datasource.
     *
     * @param dbConnection The connection to the database.
     * @param tableName the name of the table to find the srid.
     *
     * @return the srid of the first geometry column of the table.
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @task REVISIT: only handles one geometry column, should take the column
     *       name if we have more than one srid per feature.
     */
    public static int querySRID(Connection dbConnection, String tableName)
        throws DataSourceException {
        try {
            String sqlStatement = "SELECT srid FROM GEOMETRY_COLUMNS WHERE " +
                "f_table_name='" + tableName + "';";
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);
            if (result.next()) {
                int retSrid = result.getInt("srid");
                closeResultSet(result);
                return retSrid;
            } else {
                throw new DataSourceException(
                    "problem querying the db for srid " + "of " + tableName);
            }
        } catch (SQLException e) {
            String message = "Some sort of database connection error: " +
                e.getMessage();
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
     *
     * @return the name of the column to use as the fid.
     *
     * @task REVISIT: right now most postgis datasources probably won't have
     *       primary keys declared, but that should start to change if the
     *       next  shp2pgsql declares primary keys.  getFeatures now works
     *       good with  primary keys, it returns properly.  But insert does
     *       not work, and will be tricky.
     */
    public static String getFidColumn(Connection dbConnection, String tableName) {
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
     *
     * @return Geometric attribute.
     *
     * @throws Exception DOCUMENT ME!
     *
     * @task REVISIT: combine with querySRID, as they use the same select
     *       statement.
     */
    private static AttributeType getGeometryAttribute(Connection dbConnection,
        String tableName, String columnName) throws DataSourceException {
	try {
	    String sqlStatement = "SELECT type FROM GEOMETRY_COLUMNS WHERE " +
		"f_table_name='" + tableName + "' AND f_geometry_column='" +
		columnName + "';";
	    String geometryType = null;
	    
	    // retrieve the result set from the JDBC driver
	    Statement statement = dbConnection.createStatement();
	    ResultSet result = statement.executeQuery(sqlStatement);
	    
	    if (result.next()) {
		geometryType = result.getString("type");
		LOGGER.fine("geometry type is: " + geometryType);
	    }
	    
	    closeResultSet(result);
	    
	    return new AttributeTypeDefault(columnName,
					    (Class) geometryTypeMap.get(geometryType));
	} catch (SQLException e) {
	    String message = "Some sort of database connection error: " +
		e.getMessage();
	    LOGGER.warning(message);
	    throw new DataSourceException(message, e);
	}
    }

    /**
     * Creates a SQL statement for the PostGIS database.
     *
     * @param filter the filter that can be fully encoded to a sql statement.
     * @param query the getFeature query - for the tableName, properties and
     * maxFeatures.
     * @param useLimit 
     *
     * @return Full SQL statement.
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @task REVISIT: put all the sql construction in a helper class?
     * @task TODO: don't use filter/useLimit, just pass the query in, 
     * unpack here and get the supported, and also unpack in getFeatures
     * and getUnsupported there.
     */
    public String makeSql(SQLUnpacker unpacker, Query query)
        throws DataSourceException {
	//one to one relationship for now, so typeName is not used.
        //String tableName = query.getTypeName();
	//if (tableName == null) {
	    tableName = this.tableName;
	    //}
	boolean useLimit = (unpacker.getUnSupported() == null);
	Filter filter = unpacker.getSupported();
	LOGGER.fine("Filter in making sql is " + filter);
        StringBuffer sqlStatement = new StringBuffer("SELECT ");
        sqlStatement.append(fidColumn);
        AttributeType[] attributeTypes = getAttTypes(query);
        int numAttributes = attributeTypes.length;
        LOGGER.finer("making sql for " + numAttributes + " attributes");
        //TODO: implement loading of null features.  Supports null loads in metadata?
        for (int i = 0; i < numAttributes; i++) {
            String curAttName = attributeTypes[i].getName();
            if (Geometry.class.isAssignableFrom(attributeTypes[i].getType())) {
                sqlStatement.append(", AsText(" + curAttName + ")");
            } //REVISIT, see getIdColumn note.
            else if (fidColumn.equals(curAttName)) {
                //do nothing, already covered by fid
            } else {
                sqlStatement.append(", " + curAttName);
            }
        }

        String where = "";
        if (filter != null) {
            try {
                where = encoder.encode(filter);
            } catch (SQLEncoderException e) {
                String message = "Encoder error" + e.getMessage();
                LOGGER.warning(message);
                LOGGER.warning(e.toString());
                throw new DataSourceException(message, e);
            }
        }
        int limit = HARD_MAX_FEATURES;
        if (useLimit) {
            limit = query.getMaxFeatures();
        }
        sqlStatement.append(" FROM " + tableName + " " + where + " LIMIT " +
            limit + ";").toString();
        LOGGER.fine("sql statement is " + sqlStatement);
        return sqlStatement.toString();
    }

    private AttributeType[] getAttTypes(Query query) {
	if (query.retrieveAllProperties()) {
            return schema.getAllAttributeTypes();
        } else {
            return query.getProperties();
        }
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
        } catch (SQLException e) {
            LOGGER.warning("Error closing result set.");
        }
    }

   /**
     * Loads features from the datasource into the passed collection, based on
     * the passed query.  Note that all data sources must support this method
     * at a minimum.  
     *
     * @param collection The collection to put the features into.
     * @param query a datasource query object.  It encapsulates requested
     *        information, such as typeName, maxFeatures and filter.  
     *
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException {
        Filter filter = query.getFilter();
        int maxFeatures = query.getMaxFeatures();
        LOGGER.finer("query is " + query);
	//HACK: check to make sure these match, if they don't fill in nulls.
	AttributeType[] attTypes = getAttTypes(query);
	
        List features = new ArrayList(); //initial capacity of maxFeauters?
        //Would be good when maxFeatures is reached, but default of 10000000?
        try {
	    FeatureType schema = FeatureTypeFactory.create(attTypes)
		.setTypeName(tableName);
            // retrieve the result set from the JDBC driver
            LOGGER.finer("using schema " + schema);
            Statement statement = dbConnection.createStatement();
            LOGGER.finer("made statement");

	    SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());
            //figure out which of the filter we can use.
            unpacker.unPackAND(filter);

            //if there is no filter applied after the sql select statement then
            //we can use the maxFeatures in the statement.  If not we have to 
            //filter after (which is a huge memory hit with large datasets)
            String sql = makeSql(unpacker, query);
            ResultSet result = statement.executeQuery(sql);

            // set up a factory, attributes, and a counter for feature creation
            //LOGGER.fine("about to prepare feature reading");
            FeatureFactory factory = new FeatureFactory(schema);
            Object[] attributes = new Object[schema.attributeTotal()];
            String featureId;
            //AttributeType[] attTypes = schema.getAttributeTypes();
            int resultCounter = 0;
            int totalAttributes = schema.attributeTotal();
            int col;
            Filter featureFilter = unpacker.getUnSupported();

            // loop through entire result set or until maxFeatures are reached
            while (result.next() && (resultCounter < maxFeatures)) {
                // grab featureId, which always appears first 
                featureId = result.getString(1);

                //featureId's can't start with numbers.
                featureId = createFid(featureId);

                // create an individual attribute by looping through columns
                //LOGGER.finer("reading feature: " + resultCounter);
                for (col = 0; col < totalAttributes; col++) {
                    if (attTypes[col].isGeometry()) {
                        attributes[col] = geometryReader.read(result.getString(col +
                                    2));
                    } else {
                        attributes[col] = result.getObject(col + 2);
                    }
                }
                Feature curFeature = factory.create(attributes, featureId);
                if ((featureFilter == null) ||
                        featureFilter.contains(curFeature)) {
                    LOGGER.finest("adding feature: " + curFeature);
                    features.add(curFeature);
                    resultCounter++;
                }
            }
            // add features to collection and close the result set
            collection.addFeatures((Feature[]) features.toArray(
                    new Feature[features.size()]));
            closeResultSet(result);
        } catch (SQLException e) {
            String message = "Some sort of database connection error: " +
                e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (Exception e) {
            String message = "Error from the result set: " + e.getMessage();
            LOGGER.warning(message);
            LOGGER.warning(e.toString());
            e.printStackTrace();
            throw new DataSourceException(message, e);
        }
    }

    private String createFid(String featureId) {
        if (Character.isDigit(featureId.charAt(0))) {
            //so prepend the table name.
            featureId = tableName + "." + featureId;
        }

        return featureId;
    }

    /**
     * Returns a feature collection, based on the passed filter.  The schema of
     * the features passed in must match the schema of the datasource.
     *
     * @param collection Add features to the PostGIS database.
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @task TODO: Check to make sure features passed in match schema.
     * @task TODO: get working with the primary key fid column.  This will
     *       currently just insert nulls for the fids if oid is not being used
     *       as the column.  We probably need a sequence to generate the fids.
     *       Or if the fid is supposed to be part of the insert (which doesn't
     *       make sense if we return fids), then we should check for
     *       uniqueness.
     */
    public Set addFeatures(FeatureCollection collection)
        throws DataSourceException {
        Set curFids = getFidSet();
        Feature[] featureArr = collection.getFeatures();

        if (featureArr.length > 0) {
            try {
                Statement statement = dbConnection.createStatement();

                for (int i = 0; i < featureArr.length; i++) {
                    String sql = makeInsertSql(tableName, featureArr[i]);
                    LOGGER.finer("this sql statement = " + sql);
                    statement.executeUpdate(sql);
                }

                statement.close();
            } catch (SQLException e) {
                String message = "Some sort of database connection error: " +
                    e.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, e);
            }
        }

        Set newFids = getFidSet();
        newFids.removeAll(curFids);

        //Set retFids = new HashSet(newFids.size());
        //for (Iterator i = newFids.iterator(); i.hasNext;){
        return newFids;
    }

    private Set getFidSet() throws DataSourceException {
        HashSet fids = new HashSet();

        try {
	    LOGGER.fine("entering fid set");
            Statement statement = dbConnection.createStatement();
            //FeatureType fidSchema = FeatureTypeFactory.create(new AttributeType[0]);
            QueryImpl query = new QueryImpl();
            query.setProperties(new AttributeType[0]);
            SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());
	    //TODO: redo unpacker - this has to be called first, or it breaks.
	    unpacker.unPackAND(null);
	    String sql = makeSql(unpacker, (Query)query);
	    ResultSet result = statement.executeQuery(sql);
	    while (result.next()) {
		
                //REVISIT: this formatting could be done after the remove,
                //would speed things up, but also would make that code ugly.
                fids.add(createFid(result.getString(1)));
            }

            result.close();
            statement.close();
        } catch (SQLException e) {
            String message = "Some sort of database connection error: " +
                e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } 
	LOGGER.finest("returning fids " + fids);

        return fids;
    }

    /**
     * Creates a sql insert statement.  Uses each feature's schema, which makes
     * it possible to insert out of order, as well as inserting less than all
     * features.
     *
     * @param tableName the name of the feature table being inserted into.
     * @param feature the feature to add.
     *
     * @return an insert sql statement.
     */
    private String makeInsertSql(String tableName, Feature feature) {
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
            sql.append((i < (types.length - 1)) ? ", " : ") ");
        }

        sql.append("VALUES (");

        Object[] attributes = feature.getAttributes();

        for (int j = 0; j < attributes.length; j++) {
            if (j == geomPos) {
                String geoText = geometryWriter.write((Geometry) attributes[j]);
                sql.append("GeometryFromText('" + geoText + "', " + srid + ")");
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
        retString = "'" + value.toString() + "'";

        return retString;
    }

    /**
     * Removes the features specified by the passed filter from the PostGIS
     * database.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     *
     * @throws DataSourceException If anything goes wrong or if deleting is not
     *         supported.
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        Feature[] featureArr;
        String sql = "";
        Object featureID;
        String attrValue = "";
        String fid = null;
        String whereStmt = null;

	SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());
        unpacker.unPackOR(filter);
        Filter encodableFilter = unpacker.getSupported();
        Filter unEncodableFilter = unpacker.getUnSupported();

        try {
            //Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();

            if (encodableFilter != null) {
                whereStmt = encoder.encode((AbstractFilter) encodableFilter);
                sql = "DELETE from " + tableName + " " + whereStmt + ";";

                //do actual delete
                LOGGER.fine("sql statment is " + sql);
                statement.executeUpdate(sql);
            }

            if (unEncodableFilter != null) {
                featureArr = getFeatures(unEncodableFilter).getFeatures();

                if (featureArr.length > 0) {
                    sql = "DELETE FROM " + tableName + " WHERE ";

                    for (int i = 0; i < featureArr.length; i++) {
                        fid = formatFid(featureArr[i]);
                        sql += (fidColumn + " = " + fid);

                        if (i < (featureArr.length - 1)) {
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
            String message = "Some sort of database connection error: " +
                e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLEncoderException e) {
            String message = "error encoding sql from filter " +
                e.getMessage();
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
     *
     * @throws DataSourceException If modificaton is not supported, if the
     *         attribute and object arrays are not eqaul length, or if the
     *         object types do not match the attribute types.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter) throws DataSourceException {
        //TODO: throw dse if obect types don't match attribute types.  The postgis
        //database does this a bit now, but should be more fully implemented.
        Feature[] featureArr;
        Object[] curAttributes;
        String sql = "";
        String fid = null;

        //check schema with filter???
	SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());
        unpacker.unPackOR(filter);

        String whereStmt = null;
        Filter encodableFilter = unpacker.getSupported();
        Filter unEncodableFilter = unpacker.getUnSupported();

        try {
            Statement statement = dbConnection.createStatement();

            if (encodableFilter != null) {
                whereStmt = encoder.encode((AbstractFilter) encodableFilter);
                sql = makeModifySql(type, value, whereStmt);
                LOGGER.finer("encoded modify is " + sql);
                statement.executeUpdate(sql);
            }

            if (unEncodableFilter != null) {
                featureArr = getFeatures(unEncodableFilter).getFeatures();

                if (featureArr.length > 0) {
                    whereStmt = " WHERE ";

                    for (int i = 0; i < featureArr.length; i++) {
                        fid = formatFid(featureArr[i]);
                        whereStmt += (fidColumn + " = " + fid);

                        if (i < (featureArr.length - 1)) {
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
            String message = "Some sort of database error: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLEncoderException e) {
            String message = "error encoding sql from filter " +
                e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
    }

    /**
     * strips the tableName from the fid for those in the format
     * featureName.3534 should maybe just strip out all alpha-numeric
     * characters.
     *
     * @param feature DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String formatFid(Feature feature) {
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
     *
     * @throws DataSourceException If modificaton is not supported, if the
     *         object type do not match the attribute type.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws DataSourceException {
        AttributeType[] singleType = { type };
        Object[] singleVal = { value };
        modifyFeatures(singleType, singleVal, filter);
    }

    /**
     * Creates a sql update statement.
     *
     * @param types the attribute to be changed.
     * @param values the value to change it to.
     * @param whereStmt the feature to update.
     *
     * @return an update sql statement.
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private String makeModifySql(AttributeType[] types, Object[] values,
        String whereStmt) throws DataSourceException {
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
                    String geoText = geometryWriter.write((Geometry) curValue);
                    newValue = "GeometryFromText('" + geoText + "', " + srid +
                        ")";
                } else {
                    //or add quotes, covers rest of cases
                    newValue = addQuotes(curValue);
                }

                sqlStatement.append(curType.getName() + " = " + newValue);
                sqlStatement.append((i < (arrLength - 1)) ? ", " : " ");
            }

            sqlStatement.append(whereStmt + ";");

            return sqlStatement.toString();
        } else {
            throw new DataSourceException("length of value array is not " +
                "same length as type array");
        }
    }

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     *
     * @return the schema of features created by this datasource.
     *
     * @throws DataSourceException if there are any problems getting the
     *         schema.
     *
     * @task REVISIT: Our current FeatureType model is not yet advanced enough
     *       to handle multiple featureTypes.  Should getSchema take a
     *       typeName now that  a query takes a typeName, and thus DataSources
     *       can now support multiple types? Or just wait until we can
     *       programmatically make powerful enough schemas?
     */
    public FeatureType getSchema() throws DataSourceException {
        return schema;
    }

    public void setFeatures(FeatureCollection features)
        throws DataSourceException {
        removeFeatures(null);
        addFeatures(features);
    }

    /**
     * Makes all transactions made since the previous commit/rollback
     * permanent.  This method should be used only when auto-commit mode has
     * been disabled.   If autoCommit is true then this method does nothing.
     *
     * @throws DataSourceException if there are any datasource errors.
     *
     * @see #setAutoCommit(boolean)
     */
    public void commit() throws DataSourceException {
        try {
            dbConnection.commit();
        } catch (SQLException e) {
            String message = "problem committing";
            LOGGER.info(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Undoes all transactions made since the last commit or rollback. This
     * method should be used only when auto-commit mode has been disabled.
     * This method should only be implemented if
     * <tt>setAutoCommit(boolean)</tt>  is also implemented.
     *
     * @throws DataSourceException if there are problems with the datasource.
     *
     * @see #setAutoCommit(boolean)
     */
    public void rollback() throws DataSourceException {
        try {
            dbConnection.rollback();
        } catch (SQLException e) {
            String message = "problem with rollbacks";
            LOGGER.info(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Sets this datasources auto-commit mode to the given state. If a
     * datasource is in auto-commit mode, then all its add, remove and modify
     * calls will be executed  and committed as individual transactions.
     * Otherwise, those calls are grouped into a single transaction  that is
     * terminated by a call to either the method commit or the method
     * rollback.  By default, new datasources are in auto-commit mode.
     *
     * @param autoCommit <tt>true</tt> to enable auto-commit mode,
     *        <tt>false</tt> to disable it.
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @see #setAutoCommit(boolean)
     */
    public void setAutoCommit(boolean autoCommit) throws DataSourceException {
        try {
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            String message = "problem setting auto commit";
            LOGGER.info(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Retrieves the current autoCommit mode for the current DataSource.  If
     * the datasource does not implement setAutoCommit, then this method
     * should always return true.
     *
     * @return the current state of this datasource's autoCommit mode.
     *
     * @throws DataSourceException if a datasource access error occurs.
     *
     * @see #setAutoCommit(boolean)
     */
    public boolean getAutoCommit() throws DataSourceException {
        try {
            return dbConnection.getAutoCommit();
        } catch (SQLException e) {
            String message = "problem setting auto commit";
            LOGGER.info(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @deprecated, replaced by {@link #setAutoCommit(boolean)}
     */
    public void startMultiTransaction() throws DataSourceException {
        try {
            dbConnection.setAutoCommit(false);
        } catch (SQLException e) {
            String message = "Some sort of database error: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @deprecated, replaced by {@link #commit()}
     */
    public void endMultiTransaction() throws DataSourceException {
        try {
            dbConnection.commit();
        } catch (SQLException e) {
            String message = "Some sort of database error: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that clients
     * recieve the proper information about the datasource's capabilities.
     * 
     * <p></p>
     *
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
        MetaDataSupport pgMeta = new MetaDataSupport();
        pgMeta.setSupportsAdd(true);
        pgMeta.setSupportsRemove(true);
        pgMeta.setSupportsModify(true);
        pgMeta.setSupportsRollbacks(true);

        //pgMeta.setSupportsAdd(true);
        return pgMeta;

        //return new MetaDataSupport();
    }

    /**
     * Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @task REVISIT: Consider changing return of getBbox to Filter once
     *       Filters can be unpacked
     */

    //public Envelope getBbox() {
    //   return new Envelope();
    //}

    /**
     * Gets the bounding box of this datasource using the speed of  this
     * datasource as set by the parameter.
     *
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters
     *       can be unpacked
     */

    //public Envelope getBbox(boolean speed) {
    //   return new Envelope();
    //}    
}
