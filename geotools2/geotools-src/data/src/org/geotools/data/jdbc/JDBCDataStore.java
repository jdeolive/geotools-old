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
package org.geotools.data.jdbc;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FIDFeatureReader;
import org.geotools.data.FIDReader;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.JoiningAttributeReader;
import org.geotools.data.JoiningAttributeWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.QueryData.RowData;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderException;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Abstract class for JDBC based DataStore implementations.
 *
 * <p>
 * This class provides a default implementation of a JDBC data store. Support for vendor specific
 * JDBC data stores can be easily added to Geotools by subclassing this class and overriding the
 * hooks provided.
 * </p>
 *
 * <p>
 * At a minimum subclasses should implement the following methods:
 *
 * <ul>
 * <li>
 * {@link #buildAttributeType(ResultSet) buildAttributeType(ResultSet)} - This should be overriden
 * to construct an attribute type that represents any column types not supported by the default
 * implementation, such as geometry columns.
 * </li>
 * <li>
 * {@link #createGeometryAttributeReader(AttributeType,QueryData,int)
 * createGeometryAttributeReader} - Should be overriden to provide an AttributeReader that is
 * capable of reading geometries in the format of the database.
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * Additionally subclasses can optionally override the following:
 *
 * <ul>
 * <li>
 * {@link #determineFidColumnName(String) determindeFidColumnName} - Used to determine the FID
 * column name.  The default uses the primary key, but subclasses can alter this as needed.
 * </li>
 * <li>
 * {@link #allowTable(String) allowTable} - Used to determine whether a table name should be
 * exposed asa  feature type.
 * </li>
 * <li>
 * {@link #determineSRID(String,String) determineSRID} - Used to determine the SpatialReference ID
 * of a geometry column in a table.
 * </li>
 * <li>
 * {@link #buildSQLQuery(String,AttributeType[],Filter,boolean) buildSQLQuery()} - Sub classes can
 * override this to build a custom SQL query.
 * </li>
 * </ul>
 * </p>
 *
 * <h2>FID Handling</h2>
 * <p>FID handling needs to be considered for two features of the DataStore API. These are
 * generation of new FIDs for insertion and determining table columns to use for FIDs.</p>
 * <h3>FID Generation for inserts</h3>
 * <p>The way the JDBCFeatureWriter generates FIDs is defined for each feature type in the 
 * fidGenerationTypeMap. This maps feature type names to the FID generation strategy names.
 * If no Map is provided all featureTypes will be given the FID_GEN_AUTO strategy.</p>
 * <p>The FID Generation strategies supported by JDBCDataStore are:
 * <ul>
 *  <li>FID_GEN_INSERT_NULL - the underlying data store will generate the FID automatically and data store 
 *      implementations should do the equivalent of inserting null into the fid.</li>
 *  <li>FID_GEN_MANUAL_INC - the data store implementation must handle generation of new valid fids. 
 *      This is done by incrementing the max current fid in the table.</li>
 * </ul>
 * </p>
 * <p>Sub classes can add new FID Generation strategies by implementing FIDGenerationStrategy
 * and overriding getFIDGenerationStrategy.</p>
 * 
 * @author Sean  Geoghegan, Defence Science and Technology Organisation
 * @author Chris Holmes, TOPP
 *
 * $Id: JDBCDataStore.java,v 1.20 2004/01/13 00:53:21 seangeo Exp $
 */
public abstract class JDBCDataStore implements DataStore {
    
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");

    /**
     * Maps SQL types to Java classes. This might need to be fleshed out more later, Ive ignored
     * complex types such as ARRAY, BLOB and CLOB. It is protected so subclasses can override it I
     * guess.
     *
     * <p>
     * These mappings were taken from
     * http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html#997737
     * </p>
     */
    protected static final Map TYPE_MAPPINGS = new HashMap();

    static {
        TYPE_MAPPINGS.put(new Integer(Types.VARCHAR), String.class);
        TYPE_MAPPINGS.put(new Integer(Types.CHAR), String.class);
        TYPE_MAPPINGS.put(new Integer(Types.LONGVARCHAR), String.class);

        TYPE_MAPPINGS.put(new Integer(Types.BIT), Boolean.class);
        TYPE_MAPPINGS.put(new Integer(Types.BOOLEAN), Boolean.class);

        TYPE_MAPPINGS.put(new Integer(Types.TINYINT), Short.class);
        TYPE_MAPPINGS.put(new Integer(Types.SMALLINT), Short.class);

        TYPE_MAPPINGS.put(new Integer(Types.INTEGER), Integer.class);
        TYPE_MAPPINGS.put(new Integer(Types.BIGINT), Long.class);

        TYPE_MAPPINGS.put(new Integer(Types.REAL), Float.class);
        TYPE_MAPPINGS.put(new Integer(Types.FLOAT), Double.class);
        TYPE_MAPPINGS.put(new Integer(Types.DOUBLE), Double.class);

        TYPE_MAPPINGS.put(new Integer(Types.DECIMAL), BigDecimal.class);
        TYPE_MAPPINGS.put(new Integer(Types.NUMERIC), BigDecimal.class);

        TYPE_MAPPINGS.put(new Integer(Types.DATE), java.sql.Date.class);
        TYPE_MAPPINGS.put(new Integer(Types.TIME), java.sql.Time.class);
        TYPE_MAPPINGS.put(new Integer(Types.TIMESTAMP), java.sql.Timestamp.class);
    }

    /** Manages listener lists for FeatureSource implementations */
    public FeatureListenerManager listenerManager = new FeatureListenerManager();
    private LockingManager lockingManager = createLockingManager();

    /** A map of FeatureTypes with their names as the key. */
    private Map featureTypeMap = null;

    /** The ConnectionPool */
    private ConnectionPool connectionPool;

    protected final JDBCDataStoreConfig config;
    
    /**
     * 
     * @param connectionPool
     * @throws IOException
     * @deprecated This is deprecated in favour of the JDBCDataStoreConfig object.
     */
    public JDBCDataStore(ConnectionPool connectionPool) throws IOException {
        this(connectionPool, null, new HashMap(), "");
    }

    /**
     * 
     * @param connectionPool
     * @param databaseSchemaName
     * @throws IOException
     * @deprecated This is deprecated in favour of the JDBCDataStoreConfig object.
     */
    public JDBCDataStore(ConnectionPool connectionPool, String databaseSchemaName)
        throws IOException {
        this(connectionPool, databaseSchemaName,  new HashMap(), databaseSchemaName);
    }
    
    /**
     * 
     * @param connectionPool
     * @param databaseSchemaName
     * @param fidGenerationTypes
     * @throws IOException
     * @deprecated This is deprecated in favour of the JDBCDataStoreConfig object.
     */
    public JDBCDataStore(ConnectionPool connectionPool, String databaseSchemaName, Map fidGenerationTypes)
        throws IOException {
        this(connectionPool, databaseSchemaName, fidGenerationTypes, databaseSchemaName);
    }

    /**
     * 
     * @param connectionPool
     * @param databaseSchemaName
     * @param fidGenerationTypes
     * @param namespace
     * @throws IOException
     * @deprecated This is deprecated in favour of the JDBCDataStoreConfig object.
     */
    public JDBCDataStore(ConnectionPool connectionPool, String databaseSchemaName, Map fidGenerationTypes, String namespace)
        throws IOException {
        this(connectionPool,new JDBCDataStoreConfig(namespace, databaseSchemaName, new HashMap(), fidGenerationTypes));        
    }

    public JDBCDataStore(ConnectionPool connectionPool, JDBCDataStoreConfig config) throws IOException {
        this.connectionPool = connectionPool;
        this.config = config;
		this.featureTypeMap = createFeatureTypeMap();
    }
    
    /**
     * Allows subclass to create LockingManager to support their needs.
     *
     * @return
     */
    protected LockingManager createLockingManager() {
        return new InProcessLockingManager();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureTypes()
     */
    public String[] getTypeNames() {
        return (String[]) featureTypeMap.keySet().toArray(new String[featureTypeMap.keySet().size()]);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        if (featureTypeMap.containsKey(typeName)) {
            FeatureTypeInfo info = getFeatureTypeInfo(typeName);
            FeatureTypeInfo holder = (FeatureTypeInfo) featureTypeMap.get(typeName);
            return holder.schema;
        } else {
            throw new SchemaNotFoundException(typeName);
        }
    }

    /**
     * Create a new featureType.
     *
     * <p>
     * Not currently supported - subclass may implement.
     * </p>
     *
     * @param featureType
     *
     * @throws IOException
     * @throws UnsupportedOperationException Creating new schemas is not supported.
     *
     * @see org.geotools.data.DataStore#createSchema(org.geotools.feature.FeatureType)
     */
    public void createSchema(FeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("Table creation not implemented");
    }

    /**
     * Used to provide support for changing the DataStore Schema.
     *
     * <p>
     * Specifically this is intended to address updating the metadata Coordinate System
     * information.
     * </p>
     *
     * <p>
     * If we can figure out the Catalog API for metadata we will not have to use such a heavy
     * handed approach.
     * </p>
     *
     * <p>
     * Subclasses are free to implement various levels of support:
     * </p>
     *
     * <ul>
     * <li>
     * None - table modification is not supported
     * </li>
     * <li>
     * CS change - ensure that the attribtue types match and only update metadata but not table
     * structure.
     * </li>
     * <li>
     * Allow table change opperations
     * </li>
     * </ul>
     *
     *
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.geotools.feature.FeatureType)
     */
    public void updateSchema(String typeName, FeatureType featureType)
        throws IOException {
        throw new UnsupportedOperationException("Table modification not supported");
    }

    // Jody - This is my recomendation for DataStore
    // in order to support CS reprojection and override
    public FeatureSource getView(final Query query) throws IOException, SchemaException {
        String typeName = query.getTypeName();
        FeatureType origionalType = getSchema(typeName);

        //CoordinateSystem cs = query.getCoordinateSystem();
        //final FeatureType featureType = DataUtilities.createSubType( origionalType, query.getPropertyNames(), cs );
        final FeatureType featureType = DataUtilities.createSubType(origionalType,
                query.getPropertyNames());

        return new AbstractFeatureSource() {
                public DataStore getDataStore() {
                    return JDBCDataStore.this;
                }

                public void addFeatureListener(FeatureListener listener) {
                    listenerManager.addFeatureListener(this, listener);
                }

                public void removeFeatureListener(FeatureListener listener) {
                    listenerManager.removeFeatureListener(this, listener);
                }

                public FeatureType getSchema() {
                    return featureType;
                }
            };
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
    public FeatureSource getFeatureSource(String typeName) throws IOException {
        if (getLockingManager() != null) {
            // Use default JDBCFeatureLocking that delegates all locking
            // the getLockingManager
            //
            return new JDBCFeatureLocking(this, getSchema(typeName));
        } else {
            // subclass should provide a FeatureLocking implementation
            // but for now we will simply forgo all locking
            return new JDBCFeatureStore(this, getSchema(typeName));
        }
    }

    /**
     * This is a public entry point to the DataStore.
     *
     * <p>
     * We have given some though to changing this api to be based on query.
     * </p>
     *
     * <p>
     * Currently the is is the only way to retype your features to different name spaces.
     * </p>
     * (non-Javadoc)
     *
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.feature.FeatureType,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(final FeatureType requestType, final Filter filter,
        final Transaction transaction) throws IOException {
        String typeName = requestType.getTypeName();
        FeatureType schemaType = getSchema(typeName);

        int compare = DataUtilities.compare(requestType, schemaType);

        Query query;

        if (compare == 0) {
            // they are the same type
            //
            query = new DefaultQuery(typeName, filter);
        } else if (compare == 1) {
            // featureType is a proper subset and will require reTyping
            //
            String[] names = attributeNames(requestType, filter);
            query = new DefaultQuery(typeName, filter, Query.DEFAULT_MAX, names, "getFeatureReader");
        } else {
            // featureType is not compatiable
            //
            throw new IOException("Type " + typeName + " does match request");
        }

        if ((filter == Filter.ALL) || filter.equals(Filter.ALL)) {
            return new EmptyFeatureReader(requestType);
        }

        FeatureReader reader = getFeatureReader(query, transaction);

        if (compare == 1) {
            reader = new ReTypeFeatureReader(reader, requestType);
        }

        return reader;
    }

    /**
     * Gets the list of attribute names required for both featureType and filter
     *
     * @param featureType The FeatureType to get attribute names for.
     * @param filter The filter which needs attributes to filter.
     *
     * @return The list of attribute names required by a filter.
     *
     * @throws IOException If we can't get the schema.
     */
    protected String[] attributeNames(FeatureType featureType, Filter filter)
        throws IOException {
        String typeName = featureType.getTypeName();
        FeatureType origional = getSchema(typeName);
        SQLBuilder sqlBuilder = getSqlBuilder(typeName);

        if (featureType.getAttributeCount() == origional.getAttributeCount()) {
            // featureType is complete (so filter must require subset
            return DataUtilities.attributeNames(featureType);
        }

        String[] typeAttributes = DataUtilities.attributeNames(featureType);
        String[] filterAttributes = DataUtilities.attributeNames(sqlBuilder.getPostQueryFilter(
                    filter));

        if ((filterAttributes == null) || (filterAttributes.length == 0)) {
            // no filter attributes required
            return typeAttributes;
        }

        Set set = new HashSet();
        set.addAll(Arrays.asList(typeAttributes));
        set.addAll(Arrays.asList(filterAttributes));

        if (set.size() == typeAttributes.length) {
            // filter required a subset of featureType attributes
            return typeAttributes;
        } else {
            return (String[]) set.toArray(new String[set.size()]);
        }
    }

    /**
     * The top level method for getting a FeatureReader.
     *
     * <p>
     * Chris- I've gone with the Query object aswell.  It just seems to make more sense.  This is
     * pretty well split up across methods. The hooks for DB specific AttributeReaders are
     * createResultSetReader and createGeometryReader.
     * </p>
     *
     * <p>
     * JG- I have implemented getFeatureReader( FeatureType, Filter, Transasction) ontop of this
     * method, it will Retype as required
     * </p>
     *
     * @param query The Query to get a FeatureReader for.
     * @param trans The transaction this read operation is being performed in.
     *
     * @return A FeatureReader that contains features defined by the query.
     *
     * @throws IOException If an error occurs executing the query. 
     */
    public FeatureReader getFeatureReader(Query query, Transaction trans)
        throws IOException {
        String typeName = query.getTypeName();
        FeatureType featureType = getSchema(typeName);

        SQLBuilder sqlBuilder = getSqlBuilder(typeName);
        Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
        Filter postFilter = sqlBuilder.getPostQueryFilter(query.getFilter());

        String[] requestedNames = propertyNames(query);
        String[] propertyNames;

        if (requestedNames.length == featureType.getAttributeCount()) {
            // because we have everything, the filter can run
            propertyNames = requestedNames;
        } else if (requestedNames.length < featureType.getAttributeCount()) {
            // we will need to reType this :-)
            //
            // check to make sure we have enough for the filter
            //
            String[] filterNames = DataUtilities.attributeNames(postFilter);

            Set set = new HashSet();
            set.addAll(Arrays.asList(requestedNames));
            set.addAll(Arrays.asList(filterNames));

            if (set.size() == requestedNames.length) {
                propertyNames = requestedNames;
            } else {
                propertyNames = (String[]) set.toArray(new String[set.size()]);
            }
        } else {
            throw new DataSourceException(typeName + " does not contain requested proeprties:" +
                query);
        }

        AttributeType[] attrTypes = null;

        try {
            attrTypes = getAttributeTypes(typeName, propertyNames);
        } catch (SchemaException schemaException) {
            throw new DataSourceException("Some Attribute Names were specified that" +
                    " do not exist in the FeatureType " + typeName +". " +
                    "Requested names: " + Arrays.asList(propertyNames) + ", " +
                    "FeatureType: " + featureType, schemaException);
        }

        String sqlQuery = constructQuery(query, attrTypes);

        QueryData queryData = executeQuery(typeName, sqlQuery, trans, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);

        FeatureType schema;

        try {
            schema = FeatureTypeFactory.newFeatureType(attrTypes, typeName, getNameSpace());
        } catch (FactoryConfigurationError e) {
            throw new DataSourceException("Schema Factory Error when creating schema for FeatureReader",
                e);
        } catch (SchemaException e) {
            throw new DataSourceException("Schema Error when creating schema for FeatureReader", e);
        }

        FeatureReader reader;
        reader = createFeatureReader(schema, postFilter, queryData);

        if (requestedNames.length < propertyNames.length) {
            // need to scale back to what the user asked for
            // (remove the attribtues only used for postFilter)
            //
            try {
                FeatureType requestType = DataUtilities.createSubType(schema, requestedNames);
                reader = new ReTypeFeatureReader(reader, requestType);
            } catch (SchemaException schemaException) {
                throw new DataSourceException("Could not handle query", schemaException);
            }
        }

        return reader;
    }

    /**
     * Used internally to call the subclass hooks that construct the SQL query.
     *
     * @param query
     * @param attrTypes
     *
     * @return
     *
     * @throws IOException
     * @throws DataSourceException
     */
    private String constructQuery(Query query, AttributeType[] attrTypes)
        throws IOException, DataSourceException {
        String typeName = query.getTypeName();
        SQLBuilder sqlBuilder = getSqlBuilder(query.getTypeName());
        Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
        Filter postFilter = sqlBuilder.getPostQueryFilter(query.getFilter());

        String sqlQuery;
        FeatureTypeInfo info = getFeatureTypeInfo(typeName);
        boolean useMax = (postFilter == null); // not used yet

        try {
            LOGGER.fine("calling sql builder with filter " + preFilter);

            if (query.getFilter() == Filter.ALL) {
                StringBuffer buf = new StringBuffer("SELECT ");
                sqlBuilder.sqlColumns(buf, info.fidColumnName, attrTypes);
                sqlBuilder.sqlFrom(buf, typeName);
                buf.append(" WHERE '1' = '0'"); // NO-OP it
                sqlQuery = buf.toString();
            } else {
                sqlQuery = sqlBuilder.buildSQLQuery(typeName, info.fidColumnName, attrTypes,
                        preFilter);
            }

            LOGGER.info("sql is " + sqlQuery);
        } catch (SQLEncoderException e) {
            throw new DataSourceException("Error building SQL Query", e);
        }

        return sqlQuery;
    }

    /**
     * Create a new FeatureReader based on attributeReaders.
     *
     * <p>
     * The provided <code>schema</code> describes the attributes in the queryData ResultSet. This
     * schema should cover the requirements of <code>filter</code>.
     * </p>
     *
     * <p>
     * Retyping to the users requested Schema will not happen in this method.
     * </p>
     *
     * @param schema
     * @param postFilter Filter for post processing, or <code>null</code> if not requried.
     * @param queryData Holds a ResultSet for attribute Readers
     *
     * @return
     *
     * @throws IOException
     */
    protected FeatureReader createFeatureReader(FeatureType schema, Filter postFilter,
        QueryData queryData) throws IOException {
        AttributeReader[] attrReaders = buildAttributeReaders(schema.getAttributeTypes(), queryData);
        AttributeReader aReader = new JoiningAttributeReader(attrReaders);

        FeatureReader fReader;

        try {
            FIDReader fidReader = new ResultSetFIDReader(queryData, schema.getTypeName(), 1);
            fReader = new FIDFeatureReader(aReader, fidReader, schema);
        } catch (SchemaException e) {
            throw new DataSourceException("Error creating schema", e);
        }

        if ((postFilter != null) && (postFilter != Filter.ALL)) {
            fReader = new FilteringFeatureReader(fReader, postFilter);
        }

        return fReader;
    }

    /**
     * Builds the AttributeReaders from the QueryData and the array of attribute types. This is
     * private since the default implementation shoud not need to be overriden.
     *
     * <p>
     * Subclasses can provide custom attribute readers for  the basic types by overriding
     * createResultSetReader().  createResultSetReader has parameters that define a range of
     * columns in the Result to read. The default implementation of this method creates a
     * RangedResultSetAttributeReader instance.
     * </p>
     *
     * <p>
     * Subclasses must provide a geometry attribute reader by implementing createGeometryReader().
     * This must provide an AttributeReader capable of  reading a geometry at a single column.
     * This will be called when isGeometry() on a attribute returns true.
     * </p>
     * TODO: Should this be final??
     *
     * @param attrTypes The attribute types to create a list of AttributeReaders for.
     * @param queryData The Query result resources for the readers.
     *
     * @return The list of attribute readers.
     *
     * @throws IOException If an error occurs building the readers.  It seems to make sense that if
     *         we can't get readers for all the attribute we shoudl bomb out. (??)
     */
    protected final AttributeReader[] buildAttributeReaders(AttributeType[] attrTypes,
        QueryData queryData) throws IOException {
        List attrReaders = new ArrayList();
        List basicAttrTypes = new ArrayList();

        for (int i = 0; i < attrTypes.length; i++) {
            if (attrTypes[i].isGeometry()) {
                // create a reader for any previous attribute types
                if (basicAttrTypes.size() > 0) {
                    AttributeType[] basicTypes = (AttributeType[]) basicAttrTypes.toArray(new AttributeType[basicAttrTypes.size()]);

                    // startIndex is 1 based and need to add 1 to get past the fid column.
                    int startIndex = i - basicAttrTypes.size() + 2;
                    attrReaders.add(createResultSetReader(basicTypes, queryData, startIndex, i + 2));
                    basicAttrTypes.clear();
                }

                attrReaders.add(createGeometryReader(attrTypes[i], queryData, i + 2));
            } else {
                basicAttrTypes.add(attrTypes[i]);
            }
        }

        // check for left over columns
        if (basicAttrTypes.size() > 0) {
            AttributeType[] basicTypes = (AttributeType[]) basicAttrTypes.toArray(new AttributeType[basicAttrTypes.size()]);
            int startIndex = attrTypes.length - basicAttrTypes.size() + 2;

            // + 2 to get past fid and 1 based index
            attrReaders.add(createResultSetReader(basicTypes, queryData, startIndex,
                    attrTypes.length + 2));
        }

        return (AttributeReader[]) attrReaders.toArray(new AttributeReader[attrReaders.size()]);
    }

    /**
     * Executes the SQL Query.
     *
     * <p>
     * This is private in the expectation that subclasses should not need to change this behaviour.
     * </p>
     *
     * <p>
     * Jody with a question here - I have stopped this method from closing connection shared by a
     * Transaction. It sill seems like we are leaving connections open by using this method. I
     * have also stopped QueryData from doing the same thing.
     * </p>
     *
     * <p>
     * Answer from Sean:  Resources for successful queries are closed when close is called on the
     * AttributeReaders constructed with the QueryData. We can't close them here since they need
     * to be open to read from the ResultSet.
     * </p>
     *
     * <p>
     * Jody AttributeReader question: I looked at the code and Attribute Readers do not close with
     * respect to Transactions (they need to as we can issue a Reader against a Transaction. I
     * have changed the JDBCDataStore.close method to force us to keep track of these things.
     * </p>
     *
     * <p>
     * SG: I've marked this as final since I don't think it shoudl be overriden, but Im not sure
     * </p>
     *
     * @param tableName DOCUMENT ME!
     * @param sqlQuery The SQL query to execute.
     * @param transaction The Transaction is included here for handling transaction connections at
     *        a later stage.  It is not currently used.
     * @param resultSetType DOCUMENT ME!
     * @param concurrency DOCUMENT ME!
     *
     * @return The QueryData object that contains the resources for the query.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException If an error occurs performing the query.
     *
     * @task HACK: This is just protected for postgis FeatureWriter purposes. Should move back to
     *       private when that stuff moves more abstract here.
     */
    protected final QueryData executeQuery(String tableName, String sqlQuery,
        Transaction transaction, int resultSetType, int concurrency) throws IOException {
        LOGGER.info("About to execure query: " + sqlQuery);

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = getConnection(transaction);
            statement = conn.createStatement(resultSetType, concurrency);
            rs = statement.executeQuery(sqlQuery);

            FeatureTypeInfo info = getFeatureTypeInfo(tableName);
            QueryData queryData = new QueryData(info, conn, statement, rs, transaction);

            return queryData;
        } catch (SQLException e) {
            // if an error occurred we close the resources
            String msg = "Error Performing SQL query";
            LOGGER.log(Level.SEVERE, msg, e);
            JDBCUtils.close(rs);
            JDBCUtils.close(statement);
            JDBCUtils.close(conn, transaction, e);
            throw new DataSourceException(msg, e);
        }
    }

    /**
     * Hook for subclass to return a different sql builder.
     *
     * @param typeName The typename for the sql builder.
     *
     * @return A new sql builder.
     *
     * @throws IOException if anything goes wrong.
     */
    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
        return new DefaultSQLBuilder();
    }

    protected AttributeReader createResultSetReader(AttributeType[] attrType, QueryData queryData,
        int startIndex, int endIndex) {
        return new ResultSetAttributeIO(attrType, queryData, startIndex, endIndex);
    }

    protected AttributeWriter createResultSetWriter(AttributeType[] attrType, QueryData queryData,
        int startIndex, int endIndex) {
        return new ResultSetAttributeIO(attrType, queryData, startIndex, endIndex);
    }

    /**
     * Hook to create the geometry reader for a vendor specific data source.
     *
     * @param attrType The AttributeType to read.
     * @param queryData The data containing the result of the query.
     * @param index The index within the result set to read the data from.
     *
     * @return The AttributeReader that will read the geometry from the results.
     *
     * @throws DataSourceException If an error occurs building the AttributeReader.
     */
    protected abstract AttributeReader createGeometryReader(AttributeType attrType,
        QueryData queryData, int index) throws IOException;

    protected abstract AttributeWriter createGeometryWriter(AttributeType attrType,
        QueryData queryData, int index) throws IOException;

    /**
     * Creates a map of feature types names to feature types.
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException
     */
    private final Map createFeatureTypeMap() throws IOException {
        final int TABLE_NAME_COL = 3;

        Connection conn = null;

        try {
            Map featureTypeMap = new HashMap();
            conn = getConnection(Transaction.AUTO_COMMIT);

            DatabaseMetaData meta = conn.getMetaData();
            String[] tableType = { "TABLE" };
            ResultSet tables = meta.getTables(null, config.getDatabaseSchemaName(), "%", tableType);

            while (tables.next()) {
                String tableName = tables.getString(TABLE_NAME_COL);

                if (allowTable(tableName)) {
                    featureTypeMap.put(tableName, null);
                }
            }

            return featureTypeMap;
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null;

            String message = "Error querying database for list of tables:" +
                sqlException.getMessage();
            throw new DataSourceException(message, sqlException);
        } finally {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * Gets a connection from the connection pool.
     *
     * @param transaction DOCUMENT ME!
     *
     * @return A single use connection.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException If the connection is not an OracleConnection.
     */
    protected final Connection getConnection(Transaction transaction)
        throws IOException {
        if (transaction != Transaction.AUTO_COMMIT) {
            // we will need to save a JDBC connenction is
            // transaction.putState( connectionPool, JDBCState )
            //throw new UnsupportedOperationException("Transactions not supported yet");
            JDBCTransactionState state = (JDBCTransactionState) transaction.getState(connectionPool);

            if (state == null) {
                state = new JDBCTransactionState(connectionPool);
                transaction.putState(connectionPool, state);
            }

            return state.getConnection();
        }

        try {
            return connectionPool.getConnection();
        } catch (SQLException sqle) {
            throw new DataSourceException("Could not get connection", sqle);
        }
    }

    /**
     * Provides a hook for sub classes to filter out specific tables in the data store that are not
     * to be used as geospatial tables.  The default implementation of this method is to allow all
     * tables.
     *
     * @param tablename A table name to check.
     *
     * @return True if the table should be exposed as a FeatureType, false if it should be ignored.
     */
    protected boolean allowTable(String tablename) {
        return true;
    }

    /**
     * Builds the schema for a table in the database.
     *
     * <p>
     * This works by retrieving the column information for the table from the DatabaseMetaData
     * object.  It then iterates over the information for each column, calling
     * buildAttributeType(ResultSet) to construct an AttributeType for each column.  The list of
     * attribute types is then turned into a FeatureType that defines the schema.
     * </p>
     *
     * <p>
     * It is not intended that this method is overriden.  It should provide the required
     * functionality for most sub-classes.  To add AttributeType construction for vendor specific
     * SQL types, such as geometries, override the buildAttributeType(ResultSet) method.
     * </p>
     *
     * <p>
     * This may become final later.  In fact Ill make it private because I don't think It will need
     * to be overriden.
     * </p>
     *
     * @param typeName The name of the table to construct a feature type for.
     * @param fidColumn The name of the column holding the fid.
     *
     * @return The FeatureType for the table.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException This can occur if there is an SQL error or an error constructing
     *         the FeatureType.
     *
     * @see JDBCDataStore#buildAttributeType(ResultSet)
     */
    private FeatureType buildSchema(String typeName, String fidColumn)
        throws IOException {
        final int NAME_COLUMN = 4;
        final int TYPE_NAME = 6;
        Connection conn = null;
        ResultSet tableInfo = null;

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            DatabaseMetaData dbMetaData = conn.getMetaData();
            tableInfo = dbMetaData.getColumns(null, null, typeName, "%");

            List attributeTypes = new ArrayList();

            while (tableInfo.next()) {
                try {
                    // If an FID column is provided and this is it, don't make an AttributeType for it
                    if ((fidColumn != null) && fidColumn.equals(tableInfo.getString(NAME_COLUMN))) {
                        continue;
                    }

                    AttributeType attributeType = buildAttributeType(tableInfo);
                    if (attributeType != null) {
	                    attributeTypes.add(attributeType);
					} else {
						LOGGER.finest("Unknown SQL Type: " + tableInfo.getString(TYPE_NAME));
					}
                } catch (DataSourceException dse) {
                    String msg = "Error building attribute type. The column will be ignored";
                    LOGGER.log(Level.WARNING, msg, dse);
                }
            }

            AttributeType[] types = (AttributeType[]) attributeTypes.toArray(new AttributeType[0]);

            return FeatureTypeFactory.newFeatureType(types, typeName, getNameSpace());
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null; // prevent finally block from reclosing
            throw new DataSourceException("SQL Error building FeatureType for " + typeName,
                sqlException);
        } catch (FactoryConfigurationError e) {
            throw new DataSourceException("Error creating FeatureType " + typeName, e);
        } catch (SchemaException e) {
            throw new DataSourceException("Error creating FeatureType for " + typeName, e);
        } finally {
            JDBCUtils.close(tableInfo);
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet contains the
     * information retrieved by a call to  getColumns() on the DatabaseMetaData object.  This
     * information  can be used to construct an Attribute Type.
     *
     * <p>
     * The default implementation constructs an AttributeType using the default JDBC type mappings
     * defined in JDBCDataStore.  These type mappings only handle native Java classes and SQL
     * standard column types, so to handle Geometry columns, sub classes should override this to
     * check if a column is a geometry column, if it is a geometry column the appropriate
     * determination of the geometry type can be performed. Otherwise, overriding methods should
     * call super.buildAttributeType.
     * </p>
     *
     * <p>
     * Note: Overriding methods must never move the current row pointer in the result set.
     * </p>
     *
     * @param rs The ResultSet containing the result of a DatabaseMetaData.getColumns call.
     *
     * @return The AttributeType built from the ResultSet or null if the column should be excluded
     *         from the schema.
     *
     * @throws SQLException If an error occurs processing the ResultSet.
     * @throws DataSourceException Provided for overriding classes to wrap exceptions caused by
     *         other operations they may perform to determine additional types.  This will only be
     *         thrown by the default implementation if a type is present that is not present in
     *         the TYPE_MAPPINGS.
     */
    protected AttributeType buildAttributeType(ResultSet rs)
        throws SQLException, DataSourceException {
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;

        String columnName = rs.getString(COLUMN_NAME);
        int dataType = rs.getInt(DATA_TYPE);
        Class type = (Class) TYPE_MAPPINGS.get(new Integer(dataType));

        if (type == null) {
            return null;
        } else {
	        return AttributeTypeFactory.newAttributeType(columnName, type);
		}
    }

    /**
     * Provides a hook for subclasses to determine the SRID of a geometry column.
     *
     * <p>
     * This allows SRIDs to be determined in a Vendor specific way and to be cached by the default
     * implementation.  To retreive these srids, get the FeatureTypeInfo object for the table and
     * call getSRID(geometryColumnName).  This will allow storage of SRIDs for multiple geometry
     * columns in each table.
     * </p>
     *
     * <p>
     * If no SRID can be found, subclasses should return -1.  The default implementation always
     * returns -1.
     * </p>
     *
     * @param tableName The name of the table to get the SRID for.
     * @param geometryColumnName The name of the geometry column within the table to get SRID for.
     *
     * @return The SRID for the geometry column in the table or -1.
     *
     * @throws IOException DOCUMENT ME!
     */
    protected int determineSRID(String tableName, String geometryColumnName)
        throws IOException {
        return -1;
    }

    /**
     * Provides the default implementation of determining the FID column.
     *
     * <p>
     * The default implementation of determining the FID column name is to use the primary key as
     * the FID column.  If no primary key is present, null will be returned.  Sub classes can
     * override this behaviour to define primary keys for vendor specific cases.
     * </p>
     *
     * <p>
     * There is an unresolved issue as to what to do when there are multiple primary keys.  Maybe a
     * restriction that table much have a single column primary key is appropriate.
     * </p>
     *
     * <p>
     * This should not be called by subclasses to retreive the FID column name. Instead, subclasses
     * should call getFeatureTypeInfo(String) to get the FeatureTypeInfo for a feature type and
     * get the fidColumn name from the fidColumn name memeber.
     * </p>
     *
     * @param typeName The name of the table to get a primary key for.
     *
     * @return The name of the primay key column or null if one does not exist.
     *
     * @throws IOException This will only occur if there is an error getting a connection to the
     *         Database.
     */
    protected String determineFidColumnName(String typeName) throws IOException {
        final int NAME_COLUMN = 4;
        String fidColumnName = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            DatabaseMetaData dbMetadata = conn.getMetaData();
            rs = dbMetadata.getPrimaryKeys(null, null, typeName);

            if (rs.next()) {
                fidColumnName = rs.getString(NAME_COLUMN);
            }
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null; // prevent finally block from reclosing
            LOGGER.warning("Could not find the primary key - using the default");
        } finally {
            JDBCUtils.close(rs);
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }

        return fidColumnName;
    }

    /**
     * Gets the namespace of the data store.
     *
     * @return The namespace.
     */
    public String getNameSpace() {
        return config.getNamespace();
    }

    /**
     * Retreives the FeatureTypeInfo object for a FeatureType.
     *
     * <p>
     * This allows subclasses to get access to the information about a feature type, this includes
     * the schema and the fidColumnName.
     * </p>
     *
     * @param featureTypeName The name of the feature type to get the info for.
     *
     * @return The FeatureTypeInfo object for the named feature type or null if the feature type
     *         does not exist.
     *
     * @throws IOException If an error occurs creating the FeatureTypeInfo.
     */
    protected final FeatureTypeInfo getFeatureTypeInfo(String featureTypeName)
        throws IOException {
        FeatureTypeInfo info = (FeatureTypeInfo) featureTypeMap.get(featureTypeName);

        if (info == null) {
            String fidColumnName = determineFidColumnName(featureTypeName);
            FeatureType schema = buildSchema(featureTypeName, fidColumnName);
            info = new FeatureTypeInfo(featureTypeName, fidColumnName, schema);

            AttributeType[] types = schema.getAttributeTypes();

            for (int i = 0; i < types.length; i++) {
                if (types[i].isGeometry()) {
                    int srid = determineSRID(featureTypeName, types[i].getName());
                    info.putSRID(types[i].getName(), srid);
                }
            }

            featureTypeMap.put(featureTypeName, info);
        }

        return info;
    }

    /**
     * Retrieve a FeatureWriter over entire dataset.
     *
     * <p>
     * Quick notes: This FeatureWriter is often used to add new content, or perform summary
     * calculations over the entire dataset.
     * </p>
     *
     * <p>
     * Subclass may wish to implement an optimized featureWriter for these operations.
     * </p>
     *
     * <p>
     * It should provide Feature for next() even when hasNext() is <code>false</code>.
     * </p>
     *
     * <p>
     * Subclasses are responsible for checking with the lockingManger unless they are providing
     * their own locking support.
     * </p>
     *
     * @param typeName
     * @param transaction
     *
     * @return
     *
     * @throws IOException
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, boolean,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
        throws IOException {
        return getFeatureWriter(typeName, Filter.NONE, transaction);
    }

    /**
     * Retrieve a FeatureWriter for creating new content.
     *
     * <p>
     * Subclass may wish to implement an optimized featureWriter for this operation. One based on
     * prepaired statemnts is a possibility, as we do not require a ResultSet.
     * </p>
     *
     * <p>
     * To allow new content the FeatureWriter should provide Feature for next() even when hasNext()
     * is <code>false</code>.
     * </p>
     *
     * <p>
     * Subclasses are responsible for checking with the lockingManger unless they are providing
     * their own locking support.
     * </p>
     *
     * @param typeName
     * @param transaction
     *
     * @return
     *
     * @throws IOException
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, boolean,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
        throws IOException {
        FeatureWriter writer = getFeatureWriter(typeName, Filter.ALL, transaction);

        while (writer.hasNext()) {
            writer.next(); // this would be a use for skip then :-)
        }

        return writer;
    }

    /**
     * Aquire FetureWriter for modification of contents specifed by filter.
     *
     * <p>
     * Quick notes: This FeatureWriter is often used to remove contents specified by the provided
     * filter, or perform summary calculations.
     * </p>
     *
     * <p>
     * It is not used to provide new content and should return <code>null</code> for next() when
     * hasNext() returns <code>false</code>.
     * </p>
     *
     * <p>
     * Subclasses are responsible for checking with the lockingManger unless they are providing
     * their own locking support.
     * </p>
     *
     * @param typeName
     * @param filter
     * @param transaction
     *
     * @return
     *
     * @throws IOException If typeName could not be located
     * @throws NullPointerException If the provided filter is null
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
        throws IOException {
        if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: " +
                "did you mean Filter.NONE?");
        }

        if (transaction == null) {
            throw new NullPointerException("getFeatureReader requires Transaction: " +
                "did you mean Transaction.AUTO_COMMIT");
        }

        FeatureType featureType = getSchema(typeName);
        FeatureTypeInfo info = getFeatureTypeInfo(typeName);
        LOGGER.fine("getting feature writer for " + typeName + ": " + info);

        SQLBuilder sqlBuilder = getSqlBuilder(typeName);
        Filter preFilter = sqlBuilder.getPreQueryFilter(filter);
        Filter postFilter = sqlBuilder.getPostQueryFilter(filter);
        Query query = new DefaultQuery(typeName, filter);
        String sqlQuery;
        try {
            sqlQuery = constructQuery(query, getAttributeTypes(typeName, propertyNames(query)));
        } catch (SchemaException e) {
            throw new DataSourceException("Some Attribute Names were specified that" +
                    " do not exist in the FeatureType " + typeName +". " +
                    "Requested names: " + Arrays.asList(query.getPropertyNames()) + ", " +
                    "FeatureType: " + featureType, e);
        }
        
        // TODO: This is a hack to workaround Oracle problem with inserting
        // into FORWARD_ONLY result sets.
        QueryData queryData = executeQuery(typeName, sqlQuery, transaction,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        FeatureReader reader = createFeatureReader(info.getSchema(), postFilter, queryData);
        AttributeWriter[] writers = buildAttributeWriters(info.getSchema().getAttributeTypes(),
                queryData);

        AttributeWriter joinedW = new JoiningAttributeWriter(writers);
        FeatureWriter writer = createFeatureWriter(reader, joinedW, queryData);

        if ((getLockingManager() != null) &&
                getLockingManager() instanceof InProcessLockingManager) {
            InProcessLockingManager inProcess = (InProcessLockingManager) getLockingManager();
            writer = inProcess.checkedWriter(writer, transaction);
        }

        if ((postFilter != null) && (postFilter != Filter.NONE)) {
            writer = new FilteringFeatureWriter(writer, postFilter);
        }

        return writer;
    }

    protected JDBCFeatureWriter createFeatureWriter(FeatureReader fReader, AttributeWriter writer,
        QueryData queryData) throws IOException {
        LOGGER.fine("returning jdbc feature writer");

        return new JDBCFeatureWriter(fReader, writer, queryData);
    }

    protected final AttributeWriter[] buildAttributeWriters(AttributeType[] attrTypes,
        QueryData queryData) throws IOException {
        List attrWriters = new ArrayList();
        List basicAttrTypes = new ArrayList();

        for (int i = 0; i < attrTypes.length; i++) {
            if (attrTypes[i].isGeometry()) {
                // create a reader for any previous attribute types
                if (basicAttrTypes.size() > 0) {
                    AttributeType[] basicTypes = (AttributeType[]) basicAttrTypes.toArray(new AttributeType[basicAttrTypes.size()]);

                    // startIndex is 1 based and need to add 1 to get past the fid column.
                    int startIndex = i - basicAttrTypes.size() + 2;
                    attrWriters.add(createResultSetWriter(basicTypes, queryData, startIndex, i + 2));
                    basicAttrTypes.clear();
                }

                attrWriters.add(createGeometryWriter(attrTypes[i], queryData, i + 2));
            } else {
                basicAttrTypes.add(attrTypes[i]);
            }
        }

        // check for left over columns
        if (basicAttrTypes.size() > 0) {
            AttributeType[] basicTypes = (AttributeType[]) basicAttrTypes.toArray(new AttributeType[basicAttrTypes.size()]);
            int startIndex = attrTypes.length - basicAttrTypes.size() + 2;

            // + 2 to get past fid and 1 based index
            attrWriters.add(createResultSetWriter(basicTypes, queryData, startIndex,
                    attrTypes.length + 2));
        }

        return (AttributeWriter[]) attrWriters.toArray(new AttributeWriter[attrWriters.size()]);
    }

    /**
     * Get propertyNames in a safe manner.
     *
     * <p>
     * Method wil figure out names from the schema for query.getTypeName(), if query
     * getPropertyNames() is <code>null</code>, or query.retrieveAllProperties is
     * <code>true</code>.
     * </p>
     *
     * @param query
     *
     * @return
     *
     * @throws IOException
     */
    private String[] propertyNames(Query query) throws IOException {
        String[] names = query.getPropertyNames();

        if ((names == null) || query.retrieveAllProperties()) {
            String typeName = query.getTypeName();
            FeatureType schema = getSchema(typeName);

            names = new String[schema.getAttributeCount()];

            for (int i = 0; i < schema.getAttributeCount(); i++) {
                names[i] = schema.getAttributeType(i).getName();
            }
        }

        return names;
    }

    /**
     * Gets the attribute types from from a given type.  
     *     
     * @param typeName The name of the feature type to get the AttributeTypes for.
     * @param propertyNames The list of propertyNames to get AttributeTypes for.
     *
     * @return the array of attribute types from the schema which match propertyNames.
     *
     * @throws IOException If we can't get the schema.
     * @throws SchemaException if query contains a propertyName that is not a part of this
     *         type's schema.
     *
     */
    protected final AttributeType[] getAttributeTypes(String typeName, String[] propertyNames)
        throws IOException, SchemaException {
        FeatureType schema = getSchema(typeName);
        AttributeType[] types = new AttributeType[propertyNames.length];

        for (int i = 0; i < propertyNames.length; i++) {
            types[i] = schema.getAttributeType(propertyNames[i]);

            if (types[i] == null) {
                throw new SchemaException(typeName + " does not contain requested " +
                    propertyNames[i] + " attribute");
            }
        }

        return types;
    }

    /**
     * Locking manager used for this DataStore.
     *
     * <p>
     * By default AbstractDataStore makes use of InProcessLockingManager.
     * </p>
     *
     * @return
     *
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return lockingManager;
    }

    /** Gets the FIDGenerationStrategy for the FeatureType contained within the QueryData.
     * 
     *  <p>Sub classes can override this to provide new Strategies for generating FIDs.
     * @param queryData The QueryData to get a FIDGenerationStrategy for.  We use QueryData
     * here because it is required by the FID_GEN_MANUAL. It also adds a nice restriction that
     * you can only get the strategy when you are performing a read/write.
     * @return The FIDGenerationStrategy for the feature type in the query data.
     * @throws DataSourceException
     */
    protected FIDGenerationStrategy getFIDGenerationStrategyFor(final QueryData queryData) throws DataSourceException {
        final FeatureTypeInfo info = queryData.getFeatureTypeInfo();
        Object strategy = config.getFidGenerationIdFor(info.featureTypeName);        
        
        LOGGER.info("FID Generation strategy for " + info.featureTypeName + " is " + strategy);
        if (strategy == null || JDBCDataStoreConfig.FID_GEN_INSERT_NULL.equalsIgnoreCase(strategy.toString())) {
            return new InsertNullFIDGenerationStrategy();
        } else if (JDBCDataStoreConfig.FID_GEN_MANUAL_INC.equalsIgnoreCase(strategy.toString())) {
            return new MaxIncFIDGenerationStrategy(queryData);
        } else {
            throw new DataSourceException("No valid fid generation strategy defined: " + strategy);
        }
    }
    
    /**
     * Stores information about known FeatureTypes.
     *
     * @author Sean Geoghegan, Defence Science and Technology Organisation.
     */
    public static class FeatureTypeInfo {
        private String featureTypeName;
        private String fidColumnName;
        private FeatureType schema;
        private Map sridMap = new HashMap();

        public FeatureTypeInfo(String typeName, String fidColumn, FeatureType schema) {
            this.featureTypeName = typeName;
            this.fidColumnName = fidColumn;
            this.schema = schema;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         */
        public String getFeatureTypeName() {
            return featureTypeName;
        }

        /**
         * Returns the name of FidColumn if we are using one.
         *
         * <p>
         * This is used when we are using a Primary Key for our Feature ID. If this value is
         * <code>null</code> we are letting a sequence or OID do the work.
         * </p>
         *
         * @return
         */
        public String getFidColumnName() {
            return fidColumnName;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         */
        public FeatureType getSchema() {
            return schema;
        }

        /**
         * Get the DataStore specific SRID for a geometry column
         *
         * @param geometryAttributeName The name of the Geometry column to get the srid for.
         *
         * @return The srid of the geometry column.  This will only be present if
         *         determineSRID(String) of JDBCDataStore has been overridden.  If there is no
         *         SRID registered -1 will be returned.
         */
        public int getSRID(String geometryAttributeName) {
            int srid = -1;

            Integer integer = (Integer) sridMap.get(geometryAttributeName);

            if (integer != null) {
                srid = integer.intValue();
            }

            return srid;
        }

        public Map getSRIDs() {
            return Collections.unmodifiableMap(sridMap);
        }

        /**
         * Puts the srid for a geometry column in the internal map.
         *
         * @param geometryColumnName The geometry column name.
         * @param srid The SRID of the geometry column.
         */
        void putSRID(String geometryColumnName, int srid) {
            sridMap.put(geometryColumnName, new Integer(srid));
        }

        public String toString() {
            return "typeName = " + featureTypeName + ", fidCol = " + fidColumnName + ", schema: " +
            schema + "srids: " + sridMap;
        }
    }

    protected class JDBCFeatureWriter implements FeatureWriter, QueryDataObserver {
        protected QueryData queryData;
        protected AttributeWriter writer;
        protected Feature live = null; // current for FeatureWriter
        protected Feature current = null; // copy of live returned to user
        protected FeatureReader fReader;

        /**
         * <p>
         * Details
         * </p>
         *
         * @param fReader DOCUMENT ME!
         * @param writer DOCUMENT ME!
         * @param queryData
         *
         * @throws IOException
         */
        public JDBCFeatureWriter(FeatureReader fReader, AttributeWriter writer, QueryData queryData)
            throws IOException {
            this.queryData = queryData;
            queryData.attachObserver(this);
            this.fReader = fReader;
            this.writer = writer;
        }

        public FeatureType getFeatureType() {
            return queryData.getFeatureTypeInfo().getSchema();
        }

        /**
         * <p>Notes on adding features.  If we are in the adding feature state,
         * i.e. there are no rows left in the ResultSet, then we return features
         * with default attributes and null fids. After write has been called and 
         * the new feature added to the database then the null fid will be replaced
         * with the real fid.</p> 
         * 
         * @see org.geotools.data.FeatureWriter#next()
         * @return
         * @throws IOException
         */
        public Feature next() throws IOException {
            if (queryData == null) {
                throw new IOException("FeatureWriter has been closed");
            }

            FeatureType featureType = queryData.getFeatureTypeInfo().getSchema();

            if (hasNext()) {
                try {
                    queryData.next(this); // move the FeatureWriter position
                    writer.next(); // move the attribute writer
                    live = fReader.next(); // get existing content
                    current = featureType.duplicate(live);
                    LOGGER.finer("Calling next on writer");
                } catch (IllegalAttributeException e) {
                    throw new DataSourceException("Unable to edit " + live.getID() + " of " +
                        featureType.getTypeName(), e);
                }
            } else {
                // new content
                live = null;

                if (fReader != null) {
                    fReader.close();
                    fReader = null;
                }

                try {
                    Feature temp = DataUtilities.template(featureType);
                    
                    /* Here we create a Feature with a Mutable FID.
                     * We use data utilities to create a default set of attributes
                     * for the feature and these are copied into the a new 
                     * MutableFIDFeature.  Thsi can probably be improved later,
                     * there is also a dependency on DefaultFeatureType here since
                     * DefaultFeature depends on it and MutableFIDFeature extends default
                     * feature.  This may be an issue if someone reimplements the Feature
                     * interfaces.  It could address by providing a full implementation
                     * of Feature in MutableFIDFeature at a later date.
                     * 
                     */
                    current = new MutableFIDFeature((DefaultFeatureType) featureType, 
                                    temp.getAttributes(new Object[temp.getNumberOfAttributes()]), null);
                    
                    queryData.startInsert();
                    queryData.next(this);
                    writer.next();
                } catch (IllegalAttributeException e) {
                    throw new DataSourceException("Unable to add additional Features of " +
                        featureType.getTypeName(), e);
                } catch (SQLException e) {
                    throw new DataSourceException("Unable to move to insert row.", e);
                }
            }

            return current;
        }

        public void remove() throws IOException {
            if (queryData == null) {
                throw new IOException("FeatureWriter has been closed");
            }

            if (current == null) {
                throw new IOException("No feature available to remove");
            }

            if (live != null) {
                LOGGER.fine("Removing " + live);

                Envelope bounds = live.getBounds();
                live = null;
                current = null;

                try {
                    queryData.deleteCurrentRow();
                    listenerManager.fireFeaturesRemoved(queryData.getFeatureTypeInfo()
                                                                 .getFeatureTypeName(),
                        queryData.getTransaction(), bounds);
                } catch (SQLException sqle) {
                    String message = "problem deleting row";

                    if (queryData.getTransaction() != Transaction.AUTO_COMMIT) {
                        queryData.getTransaction().rollback();
                        message += "(transaction canceled)";
                    }

                    throw new DataSourceException(message, sqle);
                }
            } else {
                // cancel add new content
                current = null;
            }
        }

        /**
         * What to do with inserts and FIDS???
         *
         * @throws IOException DOCUMENT ME!
         * @throws DataSourceException DOCUMENT ME!
         */
        public void write() throws IOException {
            if (queryData == null) {
                throw new IOException("FeatureWriter has been closed");
            }

            if (current == null) {
                throw new IOException("No feature available to write");
            }

            LOGGER.fine("write called, live is " + live + " and cur is " + current);

            if (live != null) {
                if (live.equals(current)) {
                    // no modifications made to current
                    live = null;
                    current = null;
                } else {
                    doUpdate(live, current);

                    try {
                        queryData.updateRow();
                    } catch (SQLException sqlException) {
                        // This is a serious problem when working against
                        // a transaction connection, queryData knows how to
                        // handle it though
                        queryData.close(sqlException, this);
                        throw new DataSourceException("Error updating row", sqlException);
                    }

                    Envelope bounds = new Envelope();
                    bounds.expandToInclude(live.getBounds());
                    bounds.expandToInclude(current.getBounds());
                    listenerManager.fireFeaturesChanged(queryData.getFeatureTypeInfo()
                                                                 .getFeatureTypeName(),
                        queryData.getTransaction(), bounds);
                    live = null;
                    current = null;
                }
            } else {
                // Do an insert - TODO not yet sure how to handle new FIDs, any ideas???
                LOGGER.fine("doing insert in jdbc featurewriter");

                try {
                    doInsert((MutableFIDFeature) current);
                } catch (SQLException e) {
                    throw new DataSourceException("Row adding failed.", e);
                }

                listenerManager.fireFeaturesAdded(queryData.getFeatureTypeInfo().getFeatureTypeName(),
                    queryData.getTransaction(), current.getBounds());
                current = null;
            }
        }

        /**
         * Protected method to perform an insert. Postgis needs to do this seperately.  With
         * updates it can just override the geometry stuff, using a direct sql update statement,
         * but for inserts it can't update a row that doesn't exist yet.
         *
         * @param current DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         * @throws SQLException DOCUMENT ME!
         * @throws DataSourceException DOCUMENT ME!
         */
        protected void doInsert(Feature current) throws IOException, SQLException {
            try {
                queryData.startInsert();
                FIDGenerationStrategy fidGen = getFIDGenerationStrategyFor(queryData);
                RowData rd = queryData.getRowData(this);
                rd.write(fidGen.generateFidFor(current), 1);
                doUpdate(DataUtilities.template(current.getFeatureType()), current);
                queryData.doInsert();
                
                // read the new fid into the Feature. 
                MutableFIDFeature mutable = (MutableFIDFeature) current;
                mutable.setID(rd.read(1).toString());
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Unable to do insert", e);
            }
        }
        
        private void doUpdate(Feature live, Feature current) throws IOException {
            try {
                //Can we create for array getAttributes more efficiently?
                for (int i = 0; i < current.getNumberOfAttributes(); i++) {
                    Object curAtt = current.getAttribute(i);
                    Object liveAtt = live.getAttribute(i);

                    if ((live == null) || !DataUtilities.attributesEqual(curAtt, liveAtt)) {
                        LOGGER.info("modifying att# " + i + " to " + curAtt);
                        writer.write(i, curAtt);
                    }
                }
            } catch (IOException ioe) {
                String message = "problem modifying row";

                if (queryData.getTransaction() != Transaction.AUTO_COMMIT) {
                    queryData.getTransaction().rollback();
                    message += "(transaction canceled)";
                }

                throw ioe;
            }
        }

        public boolean hasNext() throws IOException {
            if (queryData == null) {
                throw new IOException("FeatureWriter has been closed");
            }

            // I think this is right, some should sanity check me though.
            // Not sure - having || highlighted another problem for me,
            // with resultsetfidreader, that I fixed.  But if fReader says
            // it doesn't have next, but the writer does, then the JDBCWriter
            // will return true and choke when next is called, since it will
            // call featureReader.next().  Of course if the writer actually
            // does have next, then that's wrong too, since it will be
            // modifying a feature when the user thinks he's making a new one.
            // Perhaps we should throw an exception if they're not the same
            // length?
            return (fReader != null) && fReader.hasNext() && writer.hasNext();
        }

        public void close() throws IOException {
            if (queryData == null) {
                throw new IOException("FeatureWriter has been closed");
            }

            if (fReader != null) {
                fReader.close();
            }

            if (writer != null) {
                writer.close();
                writer = null;
            }

            if (queryData != null) {
                queryData.close(null, this);
                queryData = null;
            }

            current = null;
            live = null;
        }
    }
}



