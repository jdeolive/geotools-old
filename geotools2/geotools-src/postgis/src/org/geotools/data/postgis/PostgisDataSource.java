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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

//geotools imports
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderPostgis;
import org.geotools.filter.SQLUnpacker;

//J2SE imports
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Connects to a Postgis database and returns properly formatted GML.
 * 
 * <p>
 * This standard class must exist for every supported datastore.
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 * @author Chris Holmes, TOPP
 * @version $Id: PostgisDataSource.java,v 1.33 2003/08/14 15:28:17 cholmesny Exp $
 */
public class PostgisDataSource extends AbstractDataSource
    implements org.geotools.data.DataSource {
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

    /** The invisible column to use as the fid if no primary key is set */
    public static final String DEFAULT_FID_COLUMN = "oid";

    /** Error message prefix for sql connection errors */
    private static final String CONN_ERROR = 
                          "Some sort of database connection error: ";
    /** The sql column that contains the name of primary keys */
    private static final int PK_COLUMN_NAME_COL = 4;

    /** Map of sql primitives to java primitives */
    private static Map sqlTypeMap = new HashMap();

    /** Map of postgis geometries to jts geometries */
    private static Map geometryTypeMap = new HashMap();

    static {
        initMaps();
    }

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
     * Sets the table and datasource, rolls a new schema from the db.
     *
     * @param dbConnection The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     *
     * @throws DataSourceException if there were problems constructing the
     *         schema.
     *
     * @task REVISIT: get rid of tableName?  Would need to specify tableName in
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

        if (schema.getDefaultGeometry() != null) {
            this.srid = querySRID(dbConnection, tableName);
            encoder.setDefaultGeometry(schema.getDefaultGeometry().getName());
            encoder.setSRID(srid);
        }
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
        sqlTypeMap.put("date", java.util.Date.class);

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
     * @throws DataSourceException If there are problems making the schema.
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
     * @throws DataSourceException if there were problems reading sql or making
     *         the schema.
     */
    public static FeatureType makeSchema(String tableName,
        java.sql.Connection dbConnection, String fidColumnName)
        throws DataSourceException {
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM \""
                    + tableName + "\" LIMIT 1;");
            ResultSetMetaData metaData = result.getMetaData();

            // initialize some local convenience variables        
            String columnName;
            String columnTypeName;
            int attributeCount = metaData.getColumnCount();

            if (!fidColumnName.equals(DEFAULT_FID_COLUMN)) {
                attributeCount--;
            }

            AttributeType[] attributes = new AttributeType[attributeCount];
            int offset = 1;

            // loop through all columns
            for (int i = 1, n = metaData.getColumnCount(); i <= n; i++) {
                LOGGER.finer("reading col: " + i);
                LOGGER.finest("reading col: " + metaData.getColumnTypeName(i));
                LOGGER.finest("reading col: " + metaData.getColumnName(i));
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
                    LOGGER.finer("setting attribute to " + columnName);
                    LOGGER.finer("with class "
                        + (Class) sqlTypeMap.get(columnTypeName));

                    Class type = (Class) sqlTypeMap.get(columnTypeName);
                    attributes[i - offset] = AttributeTypeFactory
                        .newAttributeType(columnName, type);

                    LOGGER.finer("new att-type is " + attributes[i - offset]);
                }
            }

            closeResultSet(result);

            FeatureType retSchema = 
                 FeatureTypeFactory.newFeatureType(attributes, tableName);

            return retSchema;
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
        } catch (SchemaException sche) {
            String message = "Had problems creating the feature type: ";
            LOGGER.warning(message);
            throw new DataSourceException(message, sche);
        }

        //catch (Exception e) {
    }

    /**
     * Gets the srid from the geometry_columns table of the datasource.
     *
     * @param dbConnection The connection to the database.
     * @param tableName the name of the table to find the srid.
     *
     * @return the srid of the first geometry column of the table.
     *
     * @throws DataSourceException if there are problems getting the srid.
     *
     * @task REVISIT: only handles one geometry column, should take the column
     *       name if we have more than one srid per feature.
     */
    public static int querySRID(Connection dbConnection, String tableName)
        throws DataSourceException {
        try {
            String sqlStatement = "SELECT srid FROM GEOMETRY_COLUMNS WHERE "
                + "f_table_name='" + tableName + "';";
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
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
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
                retString = pkeys.getString(PK_COLUMN_NAME_COL);

                //REVISIT: Figure out what to do if there are multiple pks
            }
        } catch (SQLException sqle) {
            retString = DEFAULT_FID_COLUMN;
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
     * @throws DataSourceException if the geometry_columns table of postgis can
     *         not be found, if this typename could not be found in the table,
     *         or if there are sql problems.
     *
     * @task REVISIT: combine with querySRID, as they use the same select
     *       statement.
     */
    private static AttributeType getGeometryAttribute(Connection dbConnection,
        String tableName, String columnName) throws DataSourceException {
        try {
            String sqlStatement = "SELECT type FROM GEOMETRY_COLUMNS WHERE "
                + "f_table_name='" + tableName + "' AND f_geometry_column='"
                + columnName + "';";
            String geometryType = null;

            // retrieve the result set from the JDBC driver
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                geometryType = result.getString("type");
                LOGGER.fine("geometry type is: " + geometryType);
            }

            closeResultSet(result);

            if (geometryType == null) {
                String msg = " no geometry found in the GEOMETRY_COLUMNS table "
                    + " for " + tableName + " of the postgis install.  A row "
                    + "for " + columnName + " is required  "
                    + " for geotools to work correctly";
                throw new DataSourceException(msg);
            }

            Class type = (Class) geometryTypeMap.get(geometryType);

            return AttributeTypeFactory.newAttributeType(columnName, type);
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
        }
    }

    /**
     * Creates a SQL statement for the PostGIS database.
     *
     * @param unpacker the object to get the encodable filter.
     * @param query the getFeature query - for the tableName, properties and
     *        maxFeatures.
     *
     * @return Full SQL statement.
     *
     * @throws DataSourceException if there are problems encoding the sql.
     *
     * @task REVISIT: put all the sql construction in a helper class?
     * @task REVISIT: rewrite the unpacker.
     */
    public String makeSql(SQLUnpacker unpacker, Query query)
        throws DataSourceException {
        //one to one relationship for now, so typeName is not used.
        //String tableName = query.getTypeName();
        tableName = this.tableName;

        boolean useLimit = (unpacker.getUnSupported() == null);
        Filter filter = unpacker.getSupported();
        LOGGER.fine("Filter in making sql is " + filter);

        StringBuffer sqlStatement = new StringBuffer("SELECT ");
        sqlStatement.append(fidColumn);

        AttributeType[] attributeTypes = getAttTypes(query);
        int numAttributes = attributeTypes.length;
        LOGGER.finer("making sql for " + numAttributes + " attributes");

        for (int i = 0; i < numAttributes; i++) {
            String curAttName = attributeTypes[i].getName();

            if (Geometry.class.isAssignableFrom(attributeTypes[i].getType())) {
                sqlStatement.append(", AsText(force_2d(\"" + curAttName
                    + "\"))");

                //REVISIT, see getIdColumn note.
            } else if (fidColumn.equals(curAttName)) {
                LOGGER.finest("skipping fid column");
                //do nothing, already covered by fid
            } else {
                sqlStatement.append(", \"" + curAttName + "\"");
            }
        }

        String where = "";

        if (filter != null) {
            try {
                where = encoder.encode(filter);
            } catch (SQLEncoderException sqle) {
                String message = "Encoder error" + sqle.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, sqle);
            }
        }

        int limit = HARD_MAX_FEATURES;

        if (useLimit) {
            limit = query.getMaxFeatures();
        }

        sqlStatement.append(" FROM \"" + tableName + "\" " + where + " LIMIT "
            + limit + ";").toString();
        LOGGER.fine("sql statement is " + sqlStatement);

        return sqlStatement.toString();
    }

    /**
     * Gets the attribute types from the query.  If all are requested then
     * returns all attribute types of this query.  If only certain
     * propertyNames are requested then this returns the correct attribute
     * types, throwing an exception is they can not be found.
     *
     * @param query contains the propertyNames.
     *
     * @return the array of attribute types to be returned by getFeature.
     *
     * @throws DataSourceException if query contains a propertyName that is not
     *         a part of this type's schema.
     */
    private AttributeType[] getAttTypes(Query query) 
                           throws DataSourceException {
        AttributeType[] schemaTypes = schema.getAttributeTypes();

        if (query.retrieveAllProperties()) {
            return schemaTypes;
        } else {
            List attNames = Arrays.asList(query.getPropertyNames());
            AttributeType[] retAttTypes = new AttributeType[attNames.size()];
            int retPos = 0;

            for (int i = 0, n = schemaTypes.length; i < n; i++) {
                String schemaTypeName = schemaTypes[i].getName();

                if (attNames.contains(schemaTypeName)) {
                    retAttTypes[retPos++] = schemaTypes[i];

                }
            }

            //TODO: better error reporting, and completely test this method.
            if (attNames.size() != retPos) {
                String msg = "attempted to request a property, "
                    + attNames.get(0) + " that is not part of the schema ";
                throw new DataSourceException(msg);
            }

            return retAttTypes;
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
        } catch (SQLException sqle) {
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

        //REVISIT: there's a good amount of code getting called twice
        //needlessly so figure out ways to just have it run once...
        AttributeType[] attTypes = getAttTypes(query);

        //FeatureCollection features = FeatureCollections.newCollection(); 
        //initial capacity of maxFeauters?
        //Would be good when maxFeatures is reached, but default of 10000000?
        try {
            FeatureType schema = FeatureTypeFactory.newFeatureType(attTypes,
                    tableName);

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
            Object[] attributes = new Object[schema.getAttributeCount()];
            String featureId;

            //AttributeType[] attTypes = schema.getAttributeTypes();
            int resultCounter = 0;
            int totalAttributes = schema.getAttributeCount();
            int col;
            Filter featureFilter = unpacker.getUnSupported();

            // loop through entire result set or until maxFeatures are reached
            while (result.next() && (resultCounter < maxFeatures)) {
                // grab featureId, which always appears first 
                featureId = result.getString(1);

                //create a featureId that has the typeName prepended.
                featureId = createFid(featureId);

                // create an individual attribute by looping through columns
                for (col = 0; col < totalAttributes; col++) {
                    if (attTypes[col].isGeometry()) {
                        String wkt = result.getString(col + 2);

                        if (wkt == null) {
                            attributes[col] = null;
                        } else {
                            attributes[col] = geometryReader.read(wkt);
                        }
                    } else {
                        attributes[col] = result.getObject(col + 2);
                    }
                }

                Feature curFeature = schema.create(attributes, featureId);

                if ((featureFilter == null)
                        || featureFilter.contains(curFeature)) {
                    LOGGER.finest("adding feature: " + curFeature);
                    collection.add(curFeature);
                    resultCounter++;
                }
            }

            closeResultSet(result);
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
        } catch (SchemaException sche) {
            String message = "Problem creating FeatureType: "
                + sche.getMessage();
            throw new DataSourceException(message, sche);
        } catch (ParseException parseE) {
            String message = "Could not read geometry: " + parseE.getMessage();
            throw new DataSourceException(message, parseE);
        } catch (IllegalAttributeException ilae) {
            String message = "Problem creating Feature: " + ilae.getMessage();
            throw new DataSourceException(message, ilae);
        }
    }

    /**
     * Prepends the tablename (featureType) on to featureIds that start with
     * digits.
     *
     * @param featureId A featureId string to be prepended with tablename if
     *        needed.
     *
     * @return the prepended feautre Id.
     */
    private String createFid(String featureId) {
        String newFid;

        if (Character.isDigit(featureId.charAt(0))) {
            //so prepend the table name.
            newFid = tableName + "." + featureId;
        } else {
            newFid = featureId;
        }

        return newFid;
    }

    /**
     * Returns a feature collection, based on the passed filter.  The schema of
     * the features passed in must match the schema of the datasource.
     *
     * @param collection Add features to the PostGIS database.
     *
     * @return A set of featureIds of the features added.
     *
     * @throws DataSourceException if anything went wrong.
     *
     * @task REVISIT: Check to make sure features passed in match schema.
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

        //Feature[] featureArr = collection.getFeatures();
        if (collection.size() > 0) {
            try {
                Statement statement = dbConnection.createStatement();

                for (FeatureIterator i = collection.features(); i.hasNext();) {
                    String sql = makeInsertSql(tableName, i.next());
                    LOGGER.finer("this sql statement = " + sql);
                    statement.executeUpdate(sql);
                }

                statement.close();
            } catch (SQLException sqle) {
                String message = CONN_ERROR + sqle.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, sqle);
            }
        }

        Set newFids = getFidSet();
        newFids.removeAll(curFids);

        //Set retFids = new HashSet(newFids.size());
        //for (Iterator i = newFids.iterator(); i.hasNext;){
        return newFids;
    }

    /**
     * Gets the set of fids for all features in this datasource .  Used by
     * insert to  figure out which features it added.  There should be a more
     * efficient way of doing this, I'm just not sure what.
     *
     * @return a set of strings of the featureIds
     *
     * @throws DataSourceException if there were problems connecting to the db
     *         backend.
     */
    private Set getFidSet() throws DataSourceException {
        Set fids = new HashSet();

        try {
            LOGGER.fine("entering fid set");

            Statement statement = dbConnection.createStatement();
            DefaultQuery query = new DefaultQuery();
            query.setPropertyNames(new String[0]);

            SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());

            //REVISIT: redo unpacker-this has to be called first, or it breaks.
            unpacker.unPackAND(null);

            String sql = makeSql(unpacker, (Query) query);
            ResultSet result = statement.executeQuery(sql);

            while (result.next()) {
                //REVISIT: this formatting could be done after the remove,
                //would speed things up, but also would make that code ugly.
                fids.add(createFid(result.getString(1)));
            }

            result.close();
            statement.close();
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
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
        String sql = "";
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
                FeatureCollection features = getFeatures(unEncodableFilter);
                FeatureIterator iter = features.features();

                if (iter.hasNext()) {
                    sql = "DELETE FROM \"" + tableName + "\" WHERE ";

                    for (int i = 0; iter.hasNext(); i++) {
                        fid = formatFid(iter.next());
                        sql += (fidColumn + " = " + fid);

                        if (iter.hasNext()) {
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
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
        } catch (SQLEncoderException ence) {
            String message = "error encoding sql from filter "
                + ence.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, ence);
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
     *
     * @task REVISIT: validate values with types.  Database does this a bit
     *       now, but should be more fully implemented.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter) throws DataSourceException {
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
                FeatureCollection coll = getFeatures(unEncodableFilter);

                if (coll.size() > 0) {
                    whereStmt = " WHERE ";

                    for (FeatureIterator iter = coll.features();
                            iter.hasNext();) {
                        fid = formatFid(iter.next());
                        whereStmt += (fidColumn + " = " + fid);

                        if (iter.hasNext()) {
                            whereStmt += " OR ";
                        }
                    }

                    sql = makeModifySql(type, value, whereStmt);
                    LOGGER.fine("unencoded modify is " + sql);
                    statement.executeUpdate(sql);
                }
            }

            statement.close();
        } catch (SQLException sqle) {
            String message = CONN_ERROR + sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
        } catch (SQLEncoderException ence) {
            String message = "error encoding sql from filter "
                + ence.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, ence);
        }
    }

    /**
     * strips the tableName from the fid for those in the format
     * featureName.3534 should maybe just strip out all alpha-numeric
     * characters.
     *
     * @param feature The feature for which the fid number should be stripped.
     *
     * @return The fid without the leading tablename.
     */
    private String formatFid(Feature feature) {
        String fid = feature.getID();

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
        AttributeType[] singleType = {type};
        Object[] singleVal = {value};
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
     * @throws DataSourceException if the lengths of types and values don't
     *         match.
     */
    private String makeModifySql(AttributeType[] types, Object[] values,
        String whereStmt) throws DataSourceException {
        int arrLength = types.length;

        if (arrLength == values.length) {
            StringBuffer sqlStatement = new StringBuffer("UPDATE ");
            sqlStatement.append("\"" + tableName + "\" SET ");

            for (int i = 0; i < arrLength; i++) {
                AttributeType curType = types[i];
                Object curValue = values[i];
                String newValue;

                //check her to make sure object matches attribute type.
                if (curType.isGeometry()) {
                    //create the text to add geometry
                    String geoText = geometryWriter.write((Geometry) curValue);
                    newValue = "GeometryFromText('" + geoText + "', " + srid
                        + ")";
                } else {
                    //or add quotes, covers rest of cases
                    newValue = addQuotes(curValue);
                }

                sqlStatement.append("\"" + curType.getName() + "\" = "
                    + newValue);

                //sqlStatement.append(curType.getName() + " = " + newValue);
                sqlStatement.append((i < (arrLength - 1)) ? ", " : " ");
            }

            sqlStatement.append(whereStmt + ";");

            return sqlStatement.toString();
        } else {
            throw new DataSourceException("length of value array is not "
                + "same length as type array");
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

    /**
     * Performs the setFeautres operation by removing all and then adding the
     * full collection.  This is not efficient, the add, modify and  remove
     * operations should be used instead, this is just to follow the
     * interface.
     *
     * @param features the features to set for this table.
     *
     * @throws DataSourceException if there are problems removing or adding.
     */
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
        } catch (SQLException sqle) {
            String message = "problem committing";
            LOGGER.info(message + ": " + sqle.getMessage());
            throw new DataSourceException(message, sqle);
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
        } catch (SQLException sqle) {
            String message = "problem with rollbacks";
            LOGGER.info(message + ": " + sqle.getMessage());
            throw new DataSourceException(message, sqle);
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
     * @throws DataSourceException if there is a sql problem setting the auto
     *         commit.
     *
     * @see #setAutoCommit(boolean)
     */
    public void setAutoCommit(boolean autoCommit) throws DataSourceException {
        try {
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            String message = "problem setting auto commit";
            LOGGER.info(message + ": " + sqle.getMessage());
            throw new DataSourceException(message, sqle);
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
        } catch (SQLException sqle) {
            String message = "problem setting auto commit";
            LOGGER.info(message + ": " + sqle.getMessage());
            throw new DataSourceException(message, sqle);
        }
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that clients
     * recieve the proper information about the datasource's capabilities.
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

        return pgMeta;
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     *
     * @task REVISIT: Consider changing return of getBbox to Filter once
     *       Filters can be unpacked
     */

    //Implement with an aggregrate sql function.
    //public Envelope getBbox() {
    //   return new Envelope();
    //}
}
