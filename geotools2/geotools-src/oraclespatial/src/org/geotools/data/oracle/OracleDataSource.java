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

package org.geotools.data.oracle;

import com.vividsolutions.jts.geom.Envelope;
import oracle.jdbc.OracleConnection;
import oracle.sdoapi.OraSpatialManager;
import oracle.sdoapi.adapter.AdapterSDO;
import oracle.sdoapi.adapter.GeometryInputTypeNotSupportedException;
import oracle.sdoapi.adapter.GeometryOutputTypeNotSupportedException;
import oracle.sdoapi.geom.InvalidGeometryException;
import oracle.sdoapi.sref.SRException;
import oracle.sdoapi.sref.SRManager;
import oracle.sdoapi.sref.SpatialReference;
import oracle.sdoapi.util.GeometryMetaData;
import oracle.sql.STRUCT;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeDefault;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.FeatureTypeFlat;
import org.geotools.feature.FlatFeatureFactory;
import org.geotools.feature.IllegalFeatureException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderOracle;
import org.geotools.filter.SQLUnpacker;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * Provides a DataSource implementation for Oracle Spatial Database.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: cholmesny $
 * @version $Id: OracleDataSource.java,v 1.1 2003/07/08 15:50:48 cholmesny Exp $
 */
public class OracleDataSource extends AbstractDataSource {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");

    /** The default column to use as the Feature ID */
    private static final String DEFAULT_FID_COLUMN = "OBJECTID";

    /** MetaData ResultSet column index for the column name */
    private static final int NAME_COLUMN = 4;

    /** MetaData ResultSet column index for the column type */
    private static final int TYPE_STRING_COLUMN = 6;

    /** Maps SQL types to Java classes */
    private static final Map typeMappings = new HashMap();

    static {
        typeMappings.put("NUMBER", java.math.BigDecimal.class);
        typeMappings.put("VARCHAR", String.class);
        typeMappings.put("VARCHAR2", String.class);
        typeMappings.put("INT4", Integer.class);
        typeMappings.put("FLOAT4", Float.class);
        typeMappings.put("FLOAT8", Double.class);
    }

    /** The Connection to the Database */
    private OracleConnection conn = null;

    /** The name of the feature table */
    private String tableName = null;
    private String oraSchemaName = null;

    /** Schema of the features in the feature table */
    private FeatureType schema = null;

    /** The FID column in the feature table. Will be the primary key or DEFAULT_FID_COLUMN */
    private String fidColumn;

    // Oracle SDOAPI objects - these objects are created by 
    // the initOra method after we have found the geometry column.

    /** Metadata about the geometry column */
    private GeometryMetaData metaData;

    /** Reference to the Spatial Reference System manager */
    private SRManager manager;

    /** The Spatial Reference of the geometry column */
    private SpatialReference sr;

    /**
     * SDO Adapter for converting between oracle.sql.STRUCT  classes and
     * oracle.sdoapi.geom.Geometry
     */
    private AdapterSDO adaptersdo;

    /**
     * Adapter for converting between oracle.sdoapi.geom.Geometry and JTS geometries for use in
     * Geotools.
     */
    private AdapterJTS adapterJTS;

    //	End Oracle SDOAPI objects.

    /** Encodes Filters into SQL statements */
    private SQLEncoder encoder;

    /** Unpacks filters into those supported by the SQL encoded and those not supported. */
    private SQLUnpacker unpacker;

    /** Sequence for generating new FIDs */
    private FIDSequence fidSequence;

    /**
     * Creates an OracleDataSource object for a specified tableName.
     *
     * @param conn The database connection
     * @param tableName The feature table name
     *
     * @throws DataSourceException
     */
    public OracleDataSource(OracleConnection conn, String tableName) throws DataSourceException {
        int dotIndex = -1;
        this.conn = conn;

        if ((dotIndex = tableName.indexOf(".")) > -1) {
            LOGGER.info("Splitting table name on " + dotIndex);
            this.oraSchemaName = tableName.substring(0, dotIndex);
            this.tableName = tableName.substring(dotIndex + 1);
            LOGGER.info("oraSchemaName = " + oraSchemaName + ", tableName = " + this.tableName);
        } else {
            this.tableName = tableName;
        }

        this.fidColumn = getFidColumn(conn, tableName);
        this.schema = makeSchema();

        this.encoder = new SQLEncoderOracle(metaData.getSpatialReferenceID());
        this.unpacker = new SQLUnpacker(SQLEncoder.getCapabilities());
        this.fidSequence = new FIDSequence(conn, tableName, fidColumn);
    }

    /**
     * Constructs the schema of this feature type.
     *
     * @return The schema of the features in the database table.
     *
     * @throws DataSourceException
     */
    private FeatureType makeSchema() throws DataSourceException {
        FeatureType featureType = null;
        ResultSet tableInfo = null;

        try {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            tableInfo = dbMetaData.getColumns(null, oraSchemaName, tableName, "%");

            Vector attributeTypes = new Vector();
            int srid = -1;
            LOGGER.finest("Checking the metadata of " + tableName);

            while (tableInfo.next()) {
                LOGGER.finest("Column Name = " + tableInfo.getObject(NAME_COLUMN) +
                    "; Column Type = " + tableInfo.getObject(TYPE_STRING_COLUMN));

                if (fidColumn.equals(tableInfo.getString(NAME_COLUMN))) {
                    continue;
                } else if ("SDO_GEOMETRY".equals(tableInfo.getString(TYPE_STRING_COLUMN))) {
                    attributeTypes.add(getGeometryAttribute(conn, tableName,
                            tableInfo.getString(NAME_COLUMN)));
                    srid = getSRID(conn, tableName, tableInfo.getString(NAME_COLUMN));
                } else {
                    Class type = (Class) typeMappings.get(tableInfo.getString(TYPE_STRING_COLUMN));

                    if (type != null) {
                        AttributeType attributeType = new AttributeTypeDefault(tableInfo.getString(
                                    NAME_COLUMN), type);
                        attributeTypes.add(attributeType);
                    } else {
                        LOGGER.info("Ignoring an SQL type that is not known: " +
                            tableInfo.getString(TYPE_STRING_COLUMN));
                    }
                }
            }

            if (attributeTypes.size() < 1) {
                throw new DataSourceException("Error loading schema.  It is likely that the table " +
                    tableName + " does not exist");
            }

            featureType = FeatureTypeFactory.create((AttributeType[]) attributeTypes.toArray(
                        new AttributeType[0]));
            featureType = featureType.setTypeName(getTypeName());

            if (featureType.getClass().isAssignableFrom(FeatureTypeFlat.class) && (srid != -1)) {
                ((FeatureTypeFlat) featureType).setSRID(srid);
            }
        } catch (SQLException e) {
            String error = "Database error in schema construction: " + e.getMessage();
            LOGGER.warning(error);
            throw new DataSourceException(error, e);
        } catch (SchemaException e) {
            String error = "Error in schema construction: " + e.getMessage();
            LOGGER.warning(error);
            throw new DataSourceException(error, e);
        } finally {
            close(tableInfo);
        }

        return featureType;
    }

    /**
     * Gets the FID column name.  Determines the FID column name which is either the primary key or
     * the default - DEFAULT_FID_COLUMN.
     *
     * @param conn The Oracle database connection.
     * @param tableName The table name to get the fid for.
     *
     * @return The fid column name.
     */
    private String getFidColumn(OracleConnection conn, String tableName) {
        String pkString = DEFAULT_FID_COLUMN;
        ResultSet rs = null;

        try {
            DatabaseMetaData dbMetadata = conn.getMetaData();
            rs = dbMetadata.getPrimaryKeys(null, null, tableName);

            if (rs.next()) {
                // @task REVIST: Need to work out what to do when there is more than 1 PK
                pkString = rs.getString(NAME_COLUMN);
            }
        } catch (SQLException e) {
            LOGGER.warning("Could not find the primary key - using the default");
        } finally {
            close(rs);
        }

        LOGGER.finest("FID=" + pkString);

        return pkString;
    }

    /**
     * Constructs the AttributeType for a geometry column.
     *
     * @param conn The database connection
     * @param tableName The table name.
     * @param columnName The geometry column name.
     *
     * @return The AttributeType for the geometry column.
     *
     * @throws DataSourceException
     */
    private AttributeType getGeometryAttribute(OracleConnection conn, String tableName,
        String columnName) throws DataSourceException {
        AttributeType attributeType = null;
        ResultSet rs = null;
        Statement statement = null;

        try {
            initOra(conn, tableName, columnName);

            StringBuffer queryBuffer = new StringBuffer("SELECT ");
            queryBuffer.append(columnName);
            queryBuffer.append(" FROM ");
            queryBuffer.append(tableName);
            queryBuffer.append(" WHERE ROWNUM = 1");

            String query = queryBuffer.toString();
            LOGGER.info("Checking geometry using: " + query);

            statement = conn.createStatement();
            rs = statement.executeQuery(query);

            if (rs.next()) {
                try {
                    com.vividsolutions.jts.geom.Geometry geometry = (com.vividsolutions.jts.geom.Geometry) adapterJTS.exportGeometry(com.vividsolutions.jts.geom.Geometry.class,
                            adaptersdo.importGeometry(rs.getObject(columnName)));
                    attributeType = new AttributeTypeDefault(columnName, geometry.getClass());
                } catch (Exception e) {
                    String message = "Could not import geometry: " + e.getMessage();
                    LOGGER.warning(message);
                    throw new DataSourceException(message, e);
                }
            } else {
                throw new DataSourceException(
                    "Could not get any features to determine geometry type");
            }
        } catch (SQLException e) {
            String message = "Database error: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } finally {
            close(rs);
            close(statement);
        }

        return attributeType;
    }

    /**
     * Convenience method that initialises the SDO API objects if they haven't been initialised.
     * 
     * <p>
     * This is set up so that the SDO objects are instance variables and only need to be
     * initialised once because each call to the SDO api involves a call to the database. The
     * side-effect of this is that we only get one SRID for each feature, so if a feature has
     * multiple geometries with different spatial reference systems, only the first encountered
     * geometry's SR will be used.
     * </p>
     * 
     * <p>
     * This will probably need to be addressed at some stage, ideally in a way that features with
     * only one geometry can take advantage of only needing to call the SDO api methods once and
     * features with multiple geometries can have multiple geometry factories and metadata objects
     * associated with them.
     * </p>
     *
     * @param conn The OracleConnection to the database
     * @param tableName The table name to initialise the SDO object for.
     * @param columnName The geometry column to initialise geometry metadata for.
     *
     * @throws DataSourceException
     */
    private void initOra(OracleConnection conn, String tableName, String columnName)
        throws DataSourceException {
        if (adapterJTS == null) {
            try {
                metaData = OraSpatialManager.getGeometryMetaData(conn, tableName, columnName);

                if (metaData == null) {
                    String message = "Could not get Geometry metadata for " + tableName + "." +
                        columnName;
                    LOGGER.warning(message);
                    throw new DataSourceException(message);
                }

                manager = OraSpatialManager.getSpatialReferenceManager(conn);
                sr = manager.retrieve(metaData.getSpatialReferenceID());

                oracle.sdoapi.geom.GeometryFactory gFact = OraSpatialManager.getGeometryFactory(sr);
                adaptersdo = new AdapterSDO(gFact, conn);
                adapterJTS = new AdapterJTS(gFact);
            } catch (SRException e) {
                String message = e.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, e);
            } catch (SQLException e) {
                String message = "Database error: " + e.getMessage();
                LOGGER.warning(message);
                throw new DataSourceException(message, e);
            }
        }
    }

    /**
     * Gets the SRID of a geometry column from the geometry metadata object.
     *
     * @param conn The oracle connection.
     * @param tableName The table to get the SRID for.
     * @param columnName The geometry column.
     *
     * @return The SRID.
     *
     * @throws DataSourceException
     */
    private int getSRID(OracleConnection conn, String tableName, String columnName)
        throws DataSourceException {
        initOra(conn, tableName, columnName);

        return metaData.getSpatialReferenceID();
    }

    /**
     * Makes an SQL statement for getFeatures.  Constructs an SQL statement that will select the
     * features from the table based on the filter.
     *
     * @param query The query to construct a where clause for
     * @param filter The filter to convert to a where statement.
     * @param maxFeatures The max amount of features to return.
     * @param useMax True if we are to use the maxFeature as the max.
     *
     * @return An SQL statement.
     *
     * @throws DataSourceException
     */
    private String makeSQL(Query query, Filter filter, int maxFeatures, boolean useMax)
        throws DataSourceException {
        LOGGER.info("Creating sql for Query: mf=" + query.getMaxFeatures() + " filter=" +
            query.getFilter() + " useMax=" + useMax);
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT ");
        sqlBuffer.append(fidColumn);

        AttributeType[] attrTypes = getAttTypes(query);

        for (int i = 0; i < attrTypes.length; i++) {
            sqlBuffer.append(", ");
            sqlBuffer.append(attrTypes[i].getName());
        }

        sqlBuffer.append(" FROM ");
        sqlBuffer.append(tableName);

        if (filter != null) {
            try {
                String where = encoder.encode(filter);
                sqlBuffer.append(" ");
                sqlBuffer.append(where);
            } catch (SQLEncoderException e) {
                String message = "Error encoding where clause " + e.getMessage();
                LOGGER.warning(message);
                LOGGER.warning(e.toString());
                throw new DataSourceException(message, e);
            }

            if (useMax && (maxFeatures > 0)) {
                sqlBuffer.append(" and ROWNUM <= ");
                sqlBuffer.append(maxFeatures);
            }
        } else if (useMax && (maxFeatures > 0)) {
            sqlBuffer.append(" WHERE ROWNUM <= ");
            sqlBuffer.append(maxFeatures);
        }

        String sqlStmt = sqlBuffer.toString();
        LOGGER.info("sqlString = " + sqlStmt);

        return sqlStmt;
    }

    /* The public instance methods of DataSource */

    /**
     * Places the features from this data source, filtered by the filter, into the feature
     * collection.
     *
     * @param collection The collection in which to place the features.
     * @param query The query to execute.
     *
     * @throws DataSourceException If error occurs when getting the features.
     */
    public void getFeatures(FeatureCollection collection, Query query)
        throws DataSourceException {
        LOGGER.info("Entering getFeatures");
        int maxFeatures = query.getMaxFeatures();
        Filter filter = query.getFilter();
        ArrayList features = new ArrayList();
        ResultSet result = null;
        Statement statement = null;

        // ensure we have a schema
        if (schema == null) {
            if ((schema = makeSchema()) == null) {
                throw new DataSourceException("Could not create schema.");
            }
        }

        try {
            Filter manualFilter = null;
            Filter supportedFilters = null;
            statement = conn.createStatement();

            if (filter != null) {
                LOGGER.info("Unpacking filter");
                unpacker.unPackAND(filter);
                manualFilter = unpacker.getUnSupported();
                supportedFilters = unpacker.getSupported();
            }

            // we use the max features limit if there are no filters or
            // no filters we have to manually apply to the result.
            boolean useMax = ((filter == Filter.NONE) || (manualFilter == null));
            String sqlQuery = makeSQL(query, supportedFilters, maxFeatures, useMax);
            result = statement.executeQuery(sqlQuery);

            FlatFeatureFactory fFactory = new FlatFeatureFactory(schema);
            Object[] attributes = new Object[schema.attributeTotal()];
            AttributeType[] attrTypes = getAttTypes(query);

            while (result.next()) {
                // Feature ID always appears in row 1 because of the way the sql is created
                String fid = getTypeName() + "." + result.getString(1);

                // loop through the attributes starting at 0, but
                // we have to add 2 to i because of the fid column being 1
                for (int i = 0; i < attributes.length; i++) {
                    if (attrTypes[i].isGeometry()) {
                        attributes[i] = adapterJTS.exportGeometry(com.vividsolutions.jts.geom.Geometry.class,
                                adaptersdo.importGeometry(result.getObject(i + 2)));
                    } else {
                        attributes[i] = result.getObject(i + 2);
                    }
                }

                Feature newFeature = fFactory.create(attributes, fid);

                if (manualFilter == null) {
                    LOGGER.fine("Adding feature: " + fid);
                    features.add(newFeature);
                } else if (manualFilter.contains(newFeature)) {
                    LOGGER.info("Adding Manually filtered features");
                    features.add(newFeature);
                }
            }

            collection.addFeatures((Feature[]) features.toArray(new Feature[0]));
        } catch (SQLException e) {
            String message = "SQL Error when loading features: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (InvalidGeometryException e) {
            String message = "Error parsing geometry: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            String message = "Geometry Conversion type error: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            String message = "Geometry Conversion type error: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (IllegalFeatureException e) {
            String message = "Error instantiating feature: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } finally {
            close(result);
            close(statement);
        }
    }

    /**
     * Adds a FeatureCollection to the data source.  All the features in the FeatureCollection are
     * added to the data source. If an error occurs the state of the datasource before this method
     * call is restored.
     *
     * @param fc The collection of features to add to the datasource.
     *
     * @return The fids of all the added features.
     *
     * @throws DataSourceException If error occurs when adding features. If this is thrown A
     *         rollback has occurred.
     */
    public Set addFeatures(FeatureCollection fc) throws DataSourceException {
        initOra(conn, tableName, schema.getDefaultGeometry().getName());
        boolean previousAutoCommit = getAutoCommit();
        setAutoCommit(false);

        boolean fail = false;
        Set fids = new TreeSet();
        Feature[] features = fc.getFeatures();
        String sql = makeInsertSQL(tableName, schema);
        PreparedStatement statement = null;

        try {
            statement = conn.prepareStatement(sql);

            for (int i = 0; i < features.length; i++) {
                if (!tableName.equals(features[i].getSchema().getTypeName())) {
                    LOGGER.warning("Got a feature that is not of the correct type");
                    continue;
                }

                int fidInt = fidSequence.getNext();
                String fid = tableName + "." + fidInt;
                AttributeType[] attributeTypes = schema.getAllAttributeTypes();

                // add the fid first
                statement.setInt(1, fidInt);

                for (int j = 0; j < attributeTypes.length; j++) {
                    if (attributeTypes[j].isGeometry()) {
                        // handle the geometry						
                        oracle.sdoapi.geom.Geometry geometry = adapterJTS.importGeometry((
                                    features[i].getAttributes()
                                )[j]);
                        statement.setObject(j + 2, adaptersdo.exportGeometry(STRUCT.class, geometry));
                    } else {
                        statement.setObject(j + 2, (features[i].getAttributes())[j]);
                    }
                }

                statement.executeUpdate();
                fids.add(fid);
            }
        } catch (SQLException e) {
            fail = true;
            String message = "Database error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (InvalidGeometryException e) {
            fail = true;
            String message = "Geometry Conversion error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry input type error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry output type error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } finally {
            close(statement);

            if (fail) {
                rollback();
            } else {
                //only commit if this transaction was atomic
                // ie if the user had previously set autoCommit to false
                // we leave commiting up to them.
                if (previousAutoCommit) {
                    commit();
                }
            }

            setAutoCommit(previousAutoCommit);
        }

        return fids;
    }

    /**
     * Constructs an Insert SQL statement template for this feature type.
     *
     * @param tableName The table name to insert to.
     * @param featureType The feature type to construct the statement for.
     *
     * @return The SQL insert template.  The FID column will always be first, followed by each
     *         feature attribute.  The VALUES section will contain ?'s for each attribute of the
     *         feature type.
     */
    private String makeInsertSQL(String tableName, FeatureType featureType) {
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(tableName);
        sql.append("(");
        sql.append(fidColumn);
        sql.append(",");

        AttributeType[] attributeTypes = featureType.getAllAttributeTypes();

        for (int i = 0; i < attributeTypes.length; i++) {
            sql.append(attributeTypes[i].getName());
            sql.append((i < (attributeTypes.length - 1)) ? "," : ")");
        }

        sql.append(" VALUES (?,"); // fid column		

        for (int i = 0; i < attributeTypes.length; i++) {
            sql.append("?");
            sql.append((i < (attributeTypes.length - 1)) ? "," : ")");
        }

        String sqlString = sql.toString();
        LOGGER.info("Insert statement is: " + sqlString);

        return sqlString;
    }

    /**
     * Removes all of the features specificed by the passed filter from the data source.
     * 
     * <p>
     * If an error occurs in the execution of this method, the state of the data source is restored
     * to the previous state.
     * </p>
     *
     * @param filter The filter of features to remove.
     *
     * @throws DataSourceException If an error occurs when removing features.
     *
     * @see org.geotools.data.DataSource#removeFeatures(org.geotools.filter.Filter)
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        boolean previousAutoCommit = getAutoCommit();
        setAutoCommit(false);

        String sql = "DELETE FROM " + tableName;
        boolean fail = false;
        unpacker.unPackOR(filter);

        Filter supported = unpacker.getSupported();
        Filter unsupported = unpacker.getUnSupported();
        Statement statement = null;

        try {
            statement = conn.createStatement();

            if (supported != null) {
                sql += (" " + encoder.encode(supported));
                LOGGER.info("sql is " + sql);
                statement.executeUpdate(sql);
            }

            // Now see if there is anything that needs to be done manually
            if (unsupported != null) {
                Feature[] features = getFeatures(unsupported).getFeatures();

                if (features.length > 0) {
                    String manualDelete = "DELETE FROM " + tableName + " WHERE ";

                    for (int i = 0; i < features.length; i++) {
                        manualDelete += (fidColumn + " = " + formatFid(features[i]));
                        manualDelete += ((i < (features.length - 1)) ? " OR " : " ");
                    }

                    LOGGER.info("Manual Delete is: " + manualDelete);
                    statement.executeUpdate(manualDelete);
                }
            }
        } catch (SQLException e) {
            fail = true;
            String message = "An SQL Error occured: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLEncoderException e) {
            fail = true;
            String message = "Failed to encode filter: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } finally {
            close(statement);

            if (fail) {
                rollback();
            } else {
                // only commit if this transaction was atomic
                // ie if the user had previously set autoCommit to false
                // we leave commiting up to them.
                if (previousAutoCommit) {
                    commit();
                }
            }

            setAutoCommit(previousAutoCommit);
        }
    }

    /**
     * Modifies the passed attribute types with the passed objects in all features that correspond
     * to the passed OGS filter.
     * 
     * <p>
     * If an error occurs in this operation, the state of the data source will be rolled back to
     * the previous state.
     * </p>
     * 
     * <p>
     * The types array and the values array have a 1-1 relationship, meaning that the ith value in
     * values is used to set the ith attribute in types.
     * </p>
     *
     * @param types The Attributes of the Feature to modify.
     * @param values The values to set the attributes in types to.
     * @param filter The filter to determine which features need to be modified.
     *
     * @throws DataSourceException If an error occured when modifying the features. If this is
     *         thrown then a rollback occurs.
     *
     * @see org.geotools.data.DataSource#modifyFeatures(org.geotools.feature.AttributeType[],
     *      java.lang.Object[], org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeType[] types, Object[] values, Filter filter)
        throws DataSourceException {
        boolean previousAutoCommit = getAutoCommit();
        setAutoCommit(false);

        boolean fail = false;
        PreparedStatement pStatement = null;
        Statement statement = null;

        // Just some constraint checking first
        if (types.length != values.length) {
            throw new DataSourceException(
                "The number of AttributeTypes must match the number of values");
        }

        // Should probably check that the attributes are valid in the schema
        // Unpack on OR conditions so we can treat them separately.
        unpacker.unPackOR(filter);

        String sqlTemplate = makeModifyTemplate(types);
        Filter supported = unpacker.getSupported();
        Filter unsupported = unpacker.getUnSupported();

        try {
            // update all the features that found in the supported filter
            if (supported != null) {
                String update = sqlTemplate + encoder.encode(supported);
                LOGGER.info("Update STMT: " + update);
                pStatement = conn.prepareStatement(update);

                for (int i = 0; i < values.length; i++) {
                    if (types[i].isGeometry()) {
                        oracle.sdoapi.geom.Geometry geometry = adapterJTS.importGeometry(values[i]);
                        pStatement.setObject(i + 1,
                            adaptersdo.exportGeometry(STRUCT.class, geometry));
                    } else {
                        pStatement.setObject(i + 1, values[i]);
                    }
                }

                pStatement.executeUpdate();
            }

            // now do the ones that are in the unsupported filters.
            if (unsupported != null) {
                // we need to retreive these manually
                Feature[] features = getFeatures(unsupported).getFeatures();

                /* Then update them manually, using their fid
                 * as the where clause.
                 */
                if (features.length > 0) {
                    StringBuffer updateBuffer = new StringBuffer(sqlTemplate + " WHERE ");

                    // Make the WHERE clause
                    for (int i = 0; i < features.length; i++) {
                        updateBuffer.append(fidColumn);
                        updateBuffer.append(" = ");
                        updateBuffer.append(formatFid(features[i]));
                        updateBuffer.append((i < (features.length - 1)) ? " OR " : " ");
                    }

                    String update = updateBuffer.toString();
                    LOGGER.info("Manual Update STMT: " + update);
                    pStatement = conn.prepareStatement(update);

                    // Fill in the prepared statement
                    for (int i = 0; i < values.length; i++) {
                        if (types[i].isGeometry()) {
                            oracle.sdoapi.geom.Geometry geometry = adapterJTS.importGeometry(values[i]);
                            pStatement.setObject(i + 1,
                                adaptersdo.exportGeometry(STRUCT.class, geometry));
                        } else {
                            pStatement.setObject(i + 1, values[i]);
                        }
                    }

                    pStatement.executeUpdate();
                }
            }
        } catch (SQLEncoderException e) {
            fail = true;
            String message = "Failed to encode filter: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLException e) {
            fail = true;
            String message = "An SQL Error occured: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry input type error when updating features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry output type error when updating features: " +
                e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (InvalidGeometryException e) {
            fail = true;
            String message = "Geometry Conversion error when updating features: " + e.getMessage();
            LOGGER.warning(message);
        } finally {
            close(pStatement);
            close(statement);

            if (fail) {
                rollback();
            } else {
                // only commit if this transaction was atomic
                // ie if the user had previously set autoCommit to false
                // we leave commiting up to them.
                if (previousAutoCommit) {
                    commit();
                }
            }

            setAutoCommit(previousAutoCommit);
        }
    }

    /**
     * Makes a template SQL statement for use in an update prepared statement. The template will
     * have the form:  <code>UPDATE &lt;tablename&gt; SET &lt;type&gt = ?</code>
     *
     * @param attributeTypes The feature attributes that are being updated.
     *
     * @return An SQL template.
     */
    private String makeModifyTemplate(AttributeType[] attributeTypes) {
        StringBuffer buffer = new StringBuffer("UPDATE ");
        buffer.append(tableName);
        buffer.append(" SET ");

        for (int i = 0; i < attributeTypes.length; i++) {
            buffer.append(attributeTypes[i].getName());
            buffer.append(" = ? ");
            buffer.append((i < (attributeTypes.length - 1)) ? ", " : " ");
        }

        return buffer.toString();
    }

    /**
     * Sets the feature table of this data source to the features contained in the feature
     * collection.  All features are removed and replaced with the contents of the
     * FeatureCollection.
     *
     * @param features The FeatureCollection to set as the contents of this DataSource.
     *
     * @throws DataSourceException If an error occured when setting the features. If this is thrown
     *         then a rollback occurs.
     *
     * @see org.geotools.data.DataSource#setFeatures(org.geotools.feature.FeatureCollection)
     */
    public void setFeatures(FeatureCollection features) throws DataSourceException {
        boolean originalAutoCommit = getAutoCommit();
        setAutoCommit(false);
        removeFeatures(null);
        addFeatures(features);
        commit();
        setAutoCommit(originalAutoCommit);
    }

    /**
     * Starts a MultiTransaction.
     * 
     * <p>
     * Begins a transaction(add, remove or modify) that does not commit as each modification call
     * is made.  If an error occurs during a transaction after this method has been called then
     * the datasource should rollback: none of the transactions performed after this method was
     * called should go through.
     * </p>
     * 
     * <p>
     * This transaction state will persist until endMultiTransaction or abortMultiTransaction is
     * called.
     * </p>
     *
     * @see org.geotools.data.DataSource#startMultiTransaction()
     */
    public void setAutoCommit(boolean b) throws DataSourceException {
        try {
            conn.setAutoCommit(b);
        } catch (SQLException e) {
            String message = "Error beginning Multi Transaction: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Undoes all transactions made since the last commit or rollback. This method should be used
     * only when auto-commit mode has been disabled. This method should only be implemented if
     * <tt>setAutoCommit(boolean)</tt>  is also implemented.
     *
     * @throws DataSourceException if there are problems with the datasource.
     *
     * @see #setAutoCommit(boolean)
     */
    public void rollback() throws DataSourceException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            String message = "problem with rollbacks";
            LOGGER.info(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Makes all transactions made since the previous commit/rollback permanent.  This method
     * should be used only when auto-commit mode has been disabled.   If autoCommit is true then
     * this method does nothing.
     *
     * @throws DataSourceException if there are any datasource errors.
     *
     * @see #setAutoCommit(boolean)
     */
    public void commit() throws DataSourceException {
        try {
            conn.commit();
        } catch (SQLException e) {
            String message = "problem committing";
            LOGGER.info(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Retrieves the current autoCommit mode for the current DataSource.  If the datasource does
     * not implement setAutoCommit, then this method should always return true.
     *
     * @return the current state of this datasource's autoCommit mode.
     *
     * @throws DataSourceException if a datasource access error occurs.
     *
     * @see #setAutoCommit(boolean)
     */
    public boolean getAutoCommit() throws DataSourceException {
        try {
            return conn.getAutoCommit();
        } catch (SQLException e) {
            String message = "problem getting auto commit";
            LOGGER.info(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Gets the schema of the features in the DataSource
     *
     * @return The schema of the Datasource.
     *
     * @see org.geotools.data.DataSource#getSchema()
     */
    public FeatureType getSchema() {
        return schema;
    }

    /**
     * Gets the bounding box of this Data source.
     *
     * @return The bounding box of the data source.  This is extracted from the geometry metadata.
     *
     * @see org.geotools.data.DataSource#getBbox()
     */
    public Envelope getBbox() {
        Envelope bbox = null;

        if (metaData != null) {
            oracle.sdoapi.geom.Envelope oraEnv = metaData.getExtent();
            bbox = new Envelope(oraEnv.getMinX(), oraEnv.getMaxX(), oraEnv.getMinY(),
                    oraEnv.getMaxY());
        }

        return bbox;
    }

    /**
     * Closes a result set and catches any errors.
     *
     * @param rs The result set to close.
     */
    private void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            LOGGER.warning("Failed to close result set - " + e.getMessage());
        }
    }

    /**
     * Closes a statement and catches any errors.
     *
     * @param s The statement to close.
     */
    private void close(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException e) {
            LOGGER.warning("Failed to close PreparedStatement - " + e.getMessage());
        }
    }

    /**
     * strips the tableName from the fid for those in the format featureName.3534 should maybe just
     * strip out all alpha-numeric characters.
     *
     * @param feature The feature format the fid for.
     *
     * @return The formated feature id.
     */
    private String formatFid(Feature feature) {
        String fid = feature.getId();

        if (fid.startsWith(tableName)) {
            //take out the tableName and the .
            fid = fid.substring(tableName.length() + 1);
        }

        return fid;
    }

    private String getTypeName() {
        String typeName = "";

        if (oraSchemaName != null) {
            typeName += (oraSchemaName + ".");
        }

        typeName += tableName;
        return typeName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @see org.geotools.data.AbstractDataSource#createMetaData()
     */
    protected DataSourceMetaData createMetaData() {
        MetaDataSupport support = new MetaDataSupport();
        support.setFastBbox(true);
        support.setSupportsRollbacks(true);
        support.setSupportsAdd(true);
        support.setSupportsModify(true);
        support.setSupportsRemove(true);
        support.setSupportsSetFeatures(true);
        support.setSupportsGetBbox(true);
        return support;
    }

    private AttributeType[] getAttTypes(Query query) {
        if (query.retrieveAllProperties()) {
            return schema.getAllAttributeTypes();
        } else {
            return query.getProperties();
        }
    }
}
