/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.GmlObjectStore;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.filter.FilterCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.GmlObjectId;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Datastore implementation for jdbc based relational databases.
 * <p>
 * This class is not intended to be subclassed on a per database basis. Instead
 * the notion of a "dialect" is used.
 * </p>
 * <p>
 *   <h3>Dialects</h3>
 * A dialect ({@link SQLDialect}) encapsulates all the operations that are database
 * specific. Therefore to implement a jdbc based datastore one must extend SQLDialect.
 * The specific dialect to use is set using {@link #setSQLDialect(SQLDialect)}.
 * </p>
 * <p>
 *   <h3>Database Connections</h3>
 *   Connections to the underlying database are obtained through a {@link DataSource}.
 *   A datastore must be specified using {@link #setDataSource(DataSource)}.
 * </p>
 * <p>
 *   <h3>Schemas</h3>
 * This datastore supports the notion of database schemas, which is more or less
 * just a grouping of tables. When a schema is specified, only those tables which
 * are part of the schema are provided by the datastore. The schema is specified
 * using {@link #setDatabaseSchema(String)}.
 * </p>
 * <p>
 *   <h3>Spatial Functions</h3>
 * The set of spatial operations or functions that are supported by the
 * specific database are reported with a {@link FilterCapabilities} instance.
 * This is specified using {@link #setFilterCapabilities(FilterCapabilities)}.
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public final class JDBCDataStore extends ContentDataStore
    implements GmlObjectStore {
    
    /**
     * name of table to use to store geometries when {@link #associations}
     * is set.
     */
    protected static final String GEOMETRY_TABLE = "geometry";

    /**
     * name of table to use to store multi geometries made up of non-multi
     * geometries when {@link #associations} is set.
     */
    protected static final String MULTI_GEOMETRY_TABLE = "multi_geometry";

    /**
     * name of table to use to store geometry associations when {@link #associations}
     * is set.
     */
    protected static final String GEOMETRY_ASSOCIATION_TABLE = "geometry_associations";

    /**
     * name of table to use to store feature relationships (information about
     * associations) when {@link #associations} is set.
     */
    protected static final String FEATURE_RELATIONSHIP_TABLE = "feature_relationships";

    /**
     * name of table to use to store feature associations when {@link #associations}
     * is set.
     */
    protected static final String FEATURE_ASSOCIATION_TABLE = "feature_associations";

    /**
     * data source
     */
    protected DataSource dataSource;

    /**
     * the dialect of sql
     */
    protected SQLDialect dialect;

    /**
     * The database schema.
     */
    protected String databaseSchema;

    /**
     * sql type to java class mappings
     */
    protected HashMap<Integer, Class<?>> sqlTypeToClassMappings;

    /**
     * sql type name to java class mappings
     */
    protected HashMap<String, Class<?>> sqlTypeNameToClassMappings;

    /**
     * java class to sql type mappings;
     */
    protected HashMap<Class<?>, Integer> classToSqlTypeMappings;

    /**
     * filter capabilities of the datastore
     */
    protected FilterCapabilities filterCapabilities;

    /**
     * flag controlling if the datastore is supporting feature and geometry
     * relationships with associations
     */
    protected boolean associations = false;

    /**
     * The dialect the datastore uses to generate sql statements in order to
     * communicate with the underlying database.
     *
     * @return The dialect, never <code>null</code>.
     */
    public SQLDialect getSQLDialect() {
        return dialect;
    }

    /**
     * Sets the dialect the datastore uses to generate sql statements in order to
     * communicate with the underlying database.
     *
     * @param dialect The dialect, never <code>null</code>.
     */
    public void setSQLDialect(SQLDialect dialect) {
        if (dialect == null) {
            throw new NullPointerException();
        }

        this.dialect = dialect;
    }

    /**
     * The data source the datastore uses to obtain connections to the underlying
     * database.
     *
     * @return The data source, never <code>null</code>.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the data source the datastore uses to obtain connections to the underlying
     * database.
     *
     * @param dataSource The data source, never <code>null</code>.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * The schema from which this datastore is serving tables from.
     *
     * @return the schema, or <code>null</code> if non specified.
     */
    public String getDatabaseSchema() {
        return databaseSchema;
    }

    /**
     * Set the database schema for the datastore.
     * <p>
     * When this value is set only those tables which are part of the schema are
     * served through the datastore. This value can be set to <code>null</code>
     * to specify no particular schema.
     * </p>
     * @param databaseSchema The schema, may be <code>null</code>.
     */
    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }

    /**
     * The filter capabilities which reports which spatial operations the
     * underlying database can handle natively.
     *
     * @return The filter capabilities, never <code>null</code>.
     */
    public FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }

    /**
     * Sets the filter capabilities which reports which spatial operations the
     * underlying database can handle natively.
     *
     * @param filterCapabilities The filter capabilities.
     */
    public void setFilterCapabilities(FilterCapabilities filterCapabilities) {
        this.filterCapabilities = filterCapabilities;
    }

    /**
     * Flag controlling if the datastore is supporting feature and geometry
     * relationships with associations
     */
    public boolean isAssociations() {
        return associations;
    }

    /**
     * Sets the flag controlling if the datastore is supporting feature and geometry
     * relationships with associations
     */
    public void setAssociations(boolean foreignKeyGeometries) {
        this.associations = foreignKeyGeometries;
    }

    /**
     * The sql type to java type mappings that the datastore uses when reading
     * and writing objects to and from the database.
     * <p>
     * These mappings are derived from {@link SQLDialect#registerSqlTypeToClassMappings(java.util.Map)}
     * </p>
     * @return The mappings, never <code>null</code>.
     */
    public Map<Integer, Class<?>> getSqlTypeToClassMappings() {
        if (sqlTypeToClassMappings == null) {
            sqlTypeToClassMappings = new HashMap<Integer, Class<?>>();
            dialect.registerSqlTypeToClassMappings(sqlTypeToClassMappings);
        }

        return sqlTypeToClassMappings;
    }

    /**
     * The sql type name to java type mappings that the dialect uses when
     * reading and writing objects to and from the database.
     * <p>
     * These mappings are derived from {@link SQLDialect#registerSqlTypeNameToClassMappings(Map)}
     * </p>
     *
     * @return The mappings, never <code>null<code>.
     */
    public Map<String, Class<?>> getSqlTypeNameToClassMappings() {
        if (sqlTypeNameToClassMappings == null) {
            sqlTypeNameToClassMappings = new HashMap<String, Class<?>>();
            dialect.registerSqlTypeNameToClassMappings(sqlTypeNameToClassMappings);
        }

        return sqlTypeNameToClassMappings;
    }

    /**
     * The java type to sql type mappings that the datastore uses when reading
     * and writing objects to and from the database.
     * <p>
     * These mappings are derived from {@link SQLDialect#registerClassToSqlMappings(Map)}
     * </p>
     * @return The mappings, never <code>null</code>.
     */
    public Map<Class<?>, Integer> getClassToSqlTypeMappings() {
        if (classToSqlTypeMappings == null) {
            classToSqlTypeMappings = new HashMap<Class<?>, Integer>();
            dialect.registerClassToSqlMappings(classToSqlTypeMappings);
        }

        return classToSqlTypeMappings;
    }

    /**
     * Returns the java type mapped to the specified sql type.
     * <p>
     * If there is no such type mapped to <tt>sqlType</tt>, <code>null</code>
     * is returned.
     * </p>
     * @param sqlType The integer constant for the sql type from {@link Types}.
     *
     * @return The mapped java class, or <code>null</code>. if no such mapping exists.
     */
    public Class<?> getMapping(int sqlType) {
        return getSqlTypeToClassMappings().get(new Integer(sqlType));
    }

    /**
     * Returns the java type mapped to the specified sql type name.
     * <p>
     * If there is no such type mapped to <tt>sqlTypeName</tt>, <code>null</code>
     * is returned.
     * </p>
     * @param sqlTypeName The name of the sql type.
     *
     * @return The mapped java class, or <code>null</code>. if no such mapping exists.
     */
    public Class<?> getMapping(String sqlTypeName) {
        return getSqlTypeNameToClassMappings().get(sqlTypeName);
    }

    /**
     * Returns the sql type mapped to the specified java type.
     * <p>
     * If there is no such type mapped to <tt>clazz</tt>, <code>Types.OTHER</code>
     * is returned.
     * </p>
     * @param clazz The java class.
     *
     * @return The mapped sql type from {@link Types}, Types.OTHER if no such
     * mapping exists.
     */
    public Integer getMapping(Class<?> clazz) {
        Integer mapping = getClassToSqlTypeMappings().get(clazz);

        if (mapping == null) {
            mapping = Types.OTHER;
            LOGGER.warning("No mapping for " + clazz.getName());
        }

        return mapping;
    }

    /**
     * Creates a table in the underlying database from the specified table.
     * <p>
     * This method will map the classes of the attributes of <tt>featureType</tt>
     * to sql types and generate a 'CREATE TABLE' statement against the underlying
     * database.
     * </p>
     * @see DataStore#createSchema(SimpleFeatureType)
     *
     * @throws IllegalArgumentException If the table already exists.
     * @throws IOException If the table cannot be created due to an error.
     */
    public void createSchema(final SimpleFeatureType featureType)
        throws IOException {
        if (entry(featureType.getName()) != null) {
            String msg = "Schema '" + featureType.getName() + "' already exists";
            throw new IllegalArgumentException(msg);
        }

        //execute the create table statement
        //TODO: create a primary key and a spatial index
        Connection cx = createConnection();

        try {
            String sql = createTableSQL(featureType, cx);
            LOGGER.fine(sql);

            Statement st = cx.createStatement();

            try {
                st.execute(sql);
            } finally {
                closeSafe(st);
            }

            dialect.postCreateTable(databaseSchema, featureType.getTypeName(), cx);
        } catch (Exception e) {
            String msg = "Error occurred creating table";
            throw (IOException) new IOException(msg).initCause(e);
        } finally {
            closeSafe(cx);
        }

        //create a content entry for the type
        ContentEntry entry = new ContentEntry(this, featureType.getName());

        //cache the feature type
        entry.getState(Transaction.AUTO_COMMIT).setFeatureType(featureType);
        entries.put(entry.getName(), entry);
    }

    /**
     * 
     */
    public Object getGmlObject(GmlObjectId id, Hints hints) throws IOException {
        //geometry?
        if ( isAssociations() ) {
        
            Connection cx = createConnection();
            try {
                String sql = selectGeometrySQL(id.getID());
                LOGGER.fine( sql );
                
                try {
                    Statement st = cx.createStatement();
                    try {
                        ResultSet rs = st.executeQuery( sql );
                        try {
                            if ( rs.next() ) {
                                //read the geometry
                                Geometry g = getSQLDialect().decodeGeometryValue(
                                    null, rs, "geometry", getGeometryFactory(), cx );
                                
                                //read the metadata
                                String name = rs.getString( "name" );
                                String desc = rs.getString( "description" );
                                setGmlProperties(g, id.getID(),name,desc);
                                
                                return g;
                            }
                        }
                        finally {
                            closeSafe( rs );
                        }
                    }
                    finally {
                        closeSafe( st );
                    }
                }
                catch( SQLException e ) {
                    throw (IOException) new IOException().initCause( e );
                }
            }
            finally {
                closeSafe( cx );
            }
        }
        
        //regular feature, first feature out the feature type
        int i = id.getID().indexOf('.');
        if ( i == -1 ) {
            LOGGER.info( "Unable to determine feature type for GmlObjectId:" + id );
            return null; 
        }
        
        //figure out the type name from the id
        String featureTypeName = id.getID().substring( 0, i );
        SimpleFeatureType featureType = getSchema( featureTypeName );
        if ( featureType == null ) {
            throw new IllegalArgumentException( "No such feature type: " + featureTypeName );
        }
        
        //load the feature
        Id filter = getFilterFactory().id(Collections.singleton(id));
        DefaultQuery query = new DefaultQuery( featureTypeName );
        query.setFilter( filter );
        query.setHints( hints );
        
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = 
            getFeatureSource( featureTypeName ).getFeatures( query );  
        if ( !features.isEmpty() ) {
            FeatureIterator<SimpleFeature> fi = features.features();
            try {
                if ( fi.hasNext() ) {
                    return fi.next();
                }
            }
            finally {
                features.close( fi );
            }
        }
        
        return null;
    }
    
    /**
     * Creates a new instance of {@link JDBCFeatureStore}.
     *
     * @see ContentDataStore#createFeatureSource(ContentEntry)
     */
    protected ContentFeatureSource createFeatureSource(ContentEntry entry)
        throws IOException {
        //TODO: read only access
        return new JDBCFeatureStore(entry,null);
    }

//    /**
//     * Creates a new instance of {@link JDBCTransactionState}.
//     */
//    protected State createTransactionState(ContentFeatureSource<SimpleFeatureType, SimpleFeature> featureSource)
//        throws IOException {
//        return new JDBCTransactionState((JDBCFeatureStore) featureSource);
//    }

    /**
     * Creates an instanceof {@link JDBCState}.
     *
     * @see ContentDataStore#createContentState(ContentEntry)
     */
    protected ContentState createContentState(ContentEntry entry) {
        JDBCState state = new JDBCState(entry);

        return state;
    }

    /**
     * Generates the list of type names provided by the database.
     * <p>
     * The list is generated from the underlying database metadata.
     * </p>
     */
    protected List createTypeNames() throws IOException {
        Connection cx = createConnection();

        /*
         *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
         *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
         *        <LI><B>TABLE_NAME</B> String => table name
         *        <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
         *                        "VIEW",        "SYSTEM TABLE", "GLOBAL TEMPORARY",
         *                        "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
         *        <LI><B>REMARKS</B> String => explanatory comment on the table
         *  <LI><B>TYPE_CAT</B> String => the types catalog (may be <code>null</code>)
         *  <LI><B>TYPE_SCHEM</B> String => the types schema (may be <code>null</code>)
         *  <LI><B>TYPE_NAME</B> String => type name (may be <code>null</code>)
         *  <LI><B>SELF_REFERENCING_COL_NAME</B> String => name of the designated
         *                  "identifier" column of a typed table (may be <code>null</code>)
         *        <LI><B>REF_GENERATION</B> String => specifies how values in
         *                  SELF_REFERENCING_COL_NAME are created. Values are
         *                  "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
         */
        List typeNames = new ArrayList();

        try {
            DatabaseMetaData metaData = cx.getMetaData();
            ResultSet tables = metaData.getTables(null, databaseSchema, "%",
                    new String[] { "TABLE", "VIEW" });

            try {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");

                    //use the dialect to filter
                    if (!dialect.includeTable(tableName, cx)) {
                        continue;
                    }

                    typeNames.add(new NameImpl(namespaceURI, tableName));
                }
            } finally {
                closeSafe(tables);
            }
        } catch (SQLException e) {
            throw (IOException) new IOException("Error occurred getting table name list.").initCause(e);
        } finally {
            closeSafe(cx);
        }

        return typeNames;
    }

    /**
     * Returns the primary key object for a particular entry, deriving it from
     * the underlying database metadata.
     *
     */
    protected PrimaryKey getPrimaryKey(ContentEntry entry)
        throws IOException {
        JDBCState state = (JDBCState) entry.getState(Transaction.AUTO_COMMIT);

        if (state.getPrimaryKey() == null) {
            synchronized (this) {
                if (state.getPrimaryKey() == null) {
                    //get metadata from database
                    Connection cx = createConnection();

                    try {
                        String tableName = entry.getName().getLocalPart();
                        DatabaseMetaData metaData = cx.getMetaData();
                        ResultSet primaryKey = metaData.getPrimaryKeys(null, databaseSchema,
                                tableName);

                        try {
                            /*
                             *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
                             *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
                             *        <LI><B>TABLE_NAME</B> String => table name
                             *        <LI><B>COLUMN_NAME</B> String => column name
                             *        <LI><B>KEY_SEQ</B> short => sequence number within primary key
                             *        <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
                             */
                            ArrayList keys = new ArrayList();

                            while (primaryKey.next()) {
                                String columnName = primaryKey.getString("COLUMN_NAME");

                                //look up the type ( should only be one row )
                                ResultSet columns = metaData.getColumns(null, databaseSchema,
                                        tableName, columnName);
                                columns.next();

                                int binding = columns.getInt("DATA_TYPE");
                                Class columnType = getMapping(binding);

                                if (columnType == null) {
                                    LOGGER.warning("No class for sql type " + binding);
                                    columnType = Object.class;
                                }

                                //determine which type of primary key we have
                                PrimaryKey key = null;

                                //1. Auto Incrementing?
                                Statement st = cx.createStatement();

                                try {
                                    //not actually going to get data
                                    st.setFetchSize(1);

                                    StringBuffer sql = new StringBuffer();
                                    sql.append("SELECT ");
                                    dialect.encodeColumnName(columnName, sql);
                                    sql.append(" FROM ");
                                    encodeTableName(tableName, sql);

                                    sql.append(" WHERE 0=1");

                                    LOGGER.fine(sql.toString());

                                    ResultSet rs = st.executeQuery(sql.toString());

                                    try {
                                        if (rs.getMetaData().isAutoIncrement(1)) {
                                            key = new AutoGeneratedPrimaryKey(tableName,
                                                    columnName, columnType);
                                        }
                                    } finally {
                                        closeSafe(rs);
                                    }
                                } finally {
                                    closeSafe(st);
                                }

                                //2. Has a sequence?
                                if (key == null) {
                                    //TODO: look for a sequence
                                }

                                if (key == null) {
                                    String msg = "Could not determine how to map "
                                        + "primary key values for (" + tableName + "," + columnName
                                        + "), restorting to null mapping.";
                                    LOGGER.warning(msg);

                                    key = new NullPrimaryKey(tableName, columnName, columnType);
                                }

                                keys.add(key);
                            }

                            if (keys.isEmpty()) {
                                String msg = "No primary key found for " + tableName + ".";
                                LOGGER.warning(msg);

                                state.setPrimaryKey(new NullPrimaryKey(tableName, null, null));
                            } else if (keys.size() > 1) {
                                //TODO: create a composite key
                            } else {
                                state.setPrimaryKey((PrimaryKey) keys.get(0));
                            }
                        } finally {
                            closeSafe(primaryKey);
                        }
                    } catch (SQLException e) {
                        String msg = "Error looking up primary key";
                        throw (IOException) new IOException(msg).initCause(e);
                    } finally {
                        closeSafe(cx);
                    }
                }
            }
        }

        return state.getPrimaryKey();
    }

    /**
     * Returns the primary key object for a particular feature type / table,
     * deriving it from the underlying database metadata.
     *
     */
    protected PrimaryKey getPrimaryKey(SimpleFeatureType featureType)
        throws IOException {
        return getPrimaryKey(ensureEntry(featureType.getName()));
    }

    /**
     * Returns the bounds of the features for a particular feature type / table.
     * 
     * @param featureType The feature type / table.
     //* @param types The columns to include in the bounds calculation, may be <code>null<code>.
     * @param filter Filter specifying rows to include in bounds calculation.
     */
    protected ReferencedEnvelope getBounds(SimpleFeatureType featureType, /*Set types,*/ Filter filter,
        Connection cx) throws IOException {
        String sql = selectBoundsSQL(featureType,/* types,*/ filter);
        LOGGER.fine(sql);

        try {
            Statement st = cx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            try {
                rs.next();

                ReferencedEnvelope bounds = null;
                Envelope e = dialect.decodeGeometryEnvelope(rs, 1, st.getConnection());

                if (e instanceof ReferencedEnvelope) {
                    bounds = (ReferencedEnvelope) e;
                } else {
                    //set the crs to be the crs of the feature type
                    bounds = new ReferencedEnvelope(e, featureType.getCoordinateReferenceSystem());
                }

                //keep going to handle case where envelope is not calculated
                // as aggregate function
                while (rs.next()) {
                    bounds.expandToInclude(dialect.decodeGeometryEnvelope(rs, 1, st.getConnection()));
                }

                return bounds;
            }
            finally {
                closeSafe(rs);
                closeSafe(st);
            }
        } catch (SQLException e) {
            String msg = "Error occured calculating bounds";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    /**
     * Returns the count of the features for a particular feature type / table.
     */
    protected int getCount(SimpleFeatureType featureType, Filter filter, Connection cx)
        throws IOException {
        String sql = selectCountSQL(featureType, filter);
        LOGGER.fine(sql);

        try {
            Statement st = cx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            try {
                rs.next();

                return rs.getInt(1);
            }
            finally {
                closeSafe(rs);
                closeSafe(st);
            }
        } catch (SQLException e) {
            String msg = "Error occured calculating count";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    /**
     * Inserts a new feature into the database for a particular feature type / table.
     */
    protected void insert(SimpleFeature feature, SimpleFeatureType featureType, Connection cx)
        throws IOException {
        insert(Collections.singletonList(feature), featureType, cx);
    }

    /**
     * Inserts a collection of new features into the database for a particular
     * feature type / table.
     */
    protected void insert(Collection features, SimpleFeatureType featureType, Connection cx)
        throws IOException {
        PrimaryKey key = getPrimaryKey(featureType);

        // we do this in a synchronized block because we need to do two queries,
        // first to figure out what the id will be, then the insert statement
        synchronized (this) {
            Statement st = null;

            try {
                st = cx.createStatement();

                for (Iterator f = features.iterator(); f.hasNext();) {
                    SimpleFeature feature = (SimpleFeature) f.next();

                    //figure out what the next fid will be
                    String fid = null;

                    try {
                        Object value = dialect.getNextPrimaryKeyValue(databaseSchema,
                                key.getTableName(), key.getColumnName(), cx);
                        fid = featureType.getTypeName() + "." + value.toString();
                    } catch (SQLException e) {
                        String msg = "Error obtaining next feature id";
                        throw (IOException) new IOException(msg).initCause(e);
                    }

                    String sql = insertSQL(featureType, feature);
                    LOGGER.fine(sql);

                    //TODO: execute in batch to improve performance?
                    st.execute(sql);

                    //report the feature id as user data since we cant set the fid
                    feature.getUserData().put("fid", fid);
                }

                //st.executeBatch();
            } catch (SQLException e) {
                String msg = "Error inserting features";
                throw (IOException) new IOException(msg).initCause(e);
            } finally {
                closeSafe(st);
            }
        }
    }

    /**
     * Updates an existing feature(s) in the database for a particular feature type / table.
     */
    protected void update(SimpleFeatureType featureType, List<AttributeDescriptor> attributes,
        List<Object> values, Filter filter, Connection cx)
        throws IOException {
        update(featureType, attributes.toArray(new AttributeDescriptor[attributes.size()]),
            values.toArray(new Object[values.size()]), filter, cx);
    }

    /**
     * Updates an existing feature(s) in the database for a particular feature type / table.
     */
    protected void update(SimpleFeatureType featureType, AttributeDescriptor[] attributes,
        Object[] values, Filter filter, Connection cx)
        throws IOException {
        if ((attributes == null) || (attributes.length == 0)) {
            LOGGER.warning("Update called with no attributes, doing nothing.");

            return;
        }

        String sql = updateSQL(featureType, attributes, values, filter);
        LOGGER.fine(sql);

        try {
            Statement st = cx.createStatement();

            try {
                st.execute(sql);
            }
            finally {
                closeSafe(st);
            }
        } catch (SQLException e) {
            String msg = "Error occured updating features";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    /**
     * Deletes an existing feature in the database for a particular feature type / fid.
     */
    protected void delete(SimpleFeatureType featureType, String fid, Connection cx)
        throws IOException {
        Filter filter = filterFactory.id(Collections.singleton(filterFactory.featureId(fid)));
        delete(featureType, filter, cx);
    }

    /**
     * Deletes an existing feature(s) in the database for a particular feature type / table.
     */
    protected void delete(SimpleFeatureType featureType, Filter filter, Connection cx)
        throws IOException {
        String sql = deleteSQL(featureType, filter);
        LOGGER.fine(sql);

        try {
            Statement st = cx.createStatement();

            try {
                st.execute(sql);
            }
            finally {
                closeSafe(st);
            }
        } catch (SQLException e) {
            String msg = "Error occured calculating bounds";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    /**
     * Gets a database connection for the specified feature store.
     */
    protected final Connection getConnection(JDBCState state) {
        Connection cx = state.getConnection();

        if (cx == null) {
            synchronized (state) {
                //create a new connection
                cx = createConnection();

                //set auto commit to false if tx != auto commit
                try {
                    cx.setAutoCommit(state.getTransaction() == Transaction.AUTO_COMMIT);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                state.setConnection(cx);
                
                if ( state.getTransaction() != Transaction.AUTO_COMMIT ) {
                    //TODO: what abotu when it is auto commmit... i beleive this
                    // is leaking connection
                    //add connection state to the transaction
                    state.getTransaction().putState(state, new JDBCTransactionState( cx, this ) );    
                }
}
        }

        return cx;
    }

    /**
     * Creates a new connection.
     * <p>
     * Callers of this method should close the connection when done with it.
     * </p>.
     *
     */
    protected final Connection createConnection() {
        try {
            Connection cx = getDataSource().getConnection();

            //TODO: make this configurable
            //cx.setTransactionIsolation( Connection.TRANSACTION_SERIALIZABLE );
            //cx.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
            cx.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            return cx;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to obtain connection", e);
        }
    }

    //
    // SQL generation
    //
    /**
     * Generates a 'CREATE TABLE' sql statement.
     */
    protected String createTableSQL(SimpleFeatureType featureType, Connection cx)
        throws Exception {
        //figure out the names of the columns
        String[] columnNames = new String[featureType.getAttributeCount()];
        String[] sqlTypeNames = null;
        Class[] classes = new Class[featureType.getAttributeCount()];

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeDescriptor attributeType = featureType.getDescriptor(i);

            //column name
            columnNames[i] = attributeType.getLocalName();

            //column type 
            classes[i] = attributeType.getType().getBinding();
        }

        sqlTypeNames = getSQLTypeNames(classes, cx);

        return createTableSQL(featureType.getTypeName(), columnNames, sqlTypeNames, "fid");
    }

    /**
     * Helper method for creating geometry association table if it does not
     * exist.
     */
    protected void ensureAssociationTablesExist(Connection cx)
        throws IOException, SQLException {
        // look for feature relationship table
        ResultSet tables = cx.getMetaData()
                             .getTables(null, databaseSchema, FEATURE_RELATIONSHIP_TABLE, null);

        try {
            if (!tables.next()) {
                // does not exist, create it
                String sql = createRelationshipTableSQL(cx);
                LOGGER.fine(sql);

                Statement st = cx.createStatement();

                try {
                    st.execute(sql);
                } finally {
                    closeSafe(st);
                }
            }
        } finally {
            closeSafe(tables);
        }

        // look for feature association table
        tables = cx.getMetaData().getTables(null, databaseSchema, FEATURE_ASSOCIATION_TABLE, null);

        try {
            if (!tables.next()) {
                // does not exist, create it
                String sql = createAssociationTableSQL(cx);
                LOGGER.fine(sql);

                Statement st = cx.createStatement();

                try {
                    st.execute(sql);
                } finally {
                    closeSafe(st);
                }
            }
        } finally {
            closeSafe(tables);
        }

        // look up for geometry table
        tables = cx.getMetaData().getTables(null, databaseSchema, GEOMETRY_TABLE, null);

        try {
            if (!tables.next()) {
                // does not exist, create it
                String sql = createGeometryTableSQL(cx);
                LOGGER.fine(sql);

                Statement st = cx.createStatement();

                try {
                    st.execute(sql);
                } finally {
                    closeSafe(st);
                }
            }
        } finally {
            closeSafe(tables);
        }

        // look up for multi geometry table
        tables = cx.getMetaData().getTables(null, databaseSchema, MULTI_GEOMETRY_TABLE, null);

        try {
            if (!tables.next()) {
                // does not exist, create it
                String sql = createMultiGeometryTableSQL(cx);
                LOGGER.fine(sql);

                Statement st = cx.createStatement();

                try {
                    st.execute(sql);
                } finally {
                    closeSafe(st);
                }
            }
        } finally {
            closeSafe(tables);
        }

        // look up for metadata for geometry association table
        tables = cx.getMetaData().getTables(null, databaseSchema, GEOMETRY_ASSOCIATION_TABLE, null);

        try {
            if (!tables.next()) {
                // does not exist, create it
                String sql = createGeometryAssociationTableSQL(cx);
                LOGGER.fine(sql);

                Statement st = cx.createStatement();

                try {
                    st.execute(sql);
                } finally {
                    closeSafe(st);
                }
            }
        } finally {
            closeSafe(tables);
        }
    }

    /**
     * Creates the sql for the relationship table.
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     */
    protected String createRelationshipTableSQL(Connection cx)
        throws SQLException {
        String[] sqlTypeNames = getSQLTypeNames(new Class[] { String.class, String.class }, cx);
        String[] columnNames = new String[] { "table", "col" };

        return createTableSQL(FEATURE_RELATIONSHIP_TABLE, columnNames, sqlTypeNames, null);
    }

    /**
     * Creates the sql for the association table.
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     */
    protected String createAssociationTableSQL(Connection cx)
        throws SQLException {
        String[] sqlTypeNames = getSQLTypeNames(new Class[] {
                    String.class, String.class, String.class, String.class
                }, cx);
        String[] columnNames = new String[] { "fid", "rtable", "rcol", "rfid" };

        return createTableSQL(FEATURE_ASSOCIATION_TABLE, columnNames, sqlTypeNames, null);
    }

    /**
     * Creates the sql for the geometry table.
     *
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     */
    protected String createGeometryTableSQL(Connection cx)
        throws SQLException {
        String[] sqlTypeNames = getSQLTypeNames(new Class[] {
                    String.class, String.class, String.class, String.class, Geometry.class
                }, cx);
        String[] columnNames = new String[] { "id", "name", "description", "type", "geometry" };

        return createTableSQL(GEOMETRY_TABLE, columnNames, sqlTypeNames, null);
    }

    /**
     * Creates the sql for the multi_geometry table.
     *  <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     */
    protected String createMultiGeometryTableSQL(Connection cx)
        throws SQLException {
        String[] sqlTypeNames = getSQLTypeNames(new Class[] { String.class, String.class, Boolean.class }, cx);
        String[] columnNames = new String[] { "id", "mgid", "ref" };

        return createTableSQL(MULTI_GEOMETRY_TABLE, columnNames, sqlTypeNames, null);
    }

    /**
     * Creates the sql for the relationship table.
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     * @param table The table of the association
     * @param column The column of the association
     */
    protected String selectRelationshipSQL(String table, String column) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        dialect.encodeColumnName("table", sql);
        sql.append(",");
        dialect.encodeColumnName("col", sql);

        sql.append(" FROM ");
        encodeTableName(FEATURE_RELATIONSHIP_TABLE, sql);

        if (table != null) {
            sql.append(" WHERE ");

            dialect.encodeColumnName("table", sql);
            sql.append(" = ");
            dialect.encodeValue(table, String.class, sql);
        }

        if (column != null) {
            if (table == null) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }

            dialect.encodeColumnName("col", sql);
            sql.append(" = ");
            dialect.encodeValue(column, String.class, sql);
        }

        return sql.toString();
    }

    /**
     * Creates the sql for the association table.
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     * @param fid The feature id of the association
     */
    protected String selectAssociationSQL(String fid) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        dialect.encodeColumnName("fid", sql);
        sql.append(",");
        dialect.encodeColumnName("rtable", sql);
        sql.append(",");
        dialect.encodeColumnName("rcol", sql);
        sql.append(", ");
        dialect.encodeColumnName("rfid", sql);

        sql.append(" FROM ");
        encodeTableName(FEATURE_ASSOCIATION_TABLE, sql);
        
        if (fid != null) {
            sql.append(" WHERE ");

            dialect.encodeColumnName("fid", sql);
            sql.append(" = ");
            dialect.encodeValue(fid, String.class, sql);
        }

        return sql.toString();
    }

    /**
     * Creates the sql for a select from the geometry table.
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     * @param gid The geometry id to select for, may be <code>null</code>
     *
     */
    protected String selectGeometrySQL(String gid) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        dialect.encodeColumnName("id", sql);
        sql.append(",");
        dialect.encodeColumnName("name", sql);
        sql.append(",");
        dialect.encodeColumnName("description", sql);
        sql.append(",");
        dialect.encodeColumnName("type", sql);
        sql.append(",");
        dialect.encodeColumnName("geometry", sql);
        sql.append(" FROM ");
        encodeTableName( GEOMETRY_TABLE, sql );
        
        if (gid != null) {
            sql.append(" WHERE ");

            dialect.encodeColumnName("id", sql);
            sql.append(" = ");
            dialect.encodeValue(gid, String.class, sql);
        }

        return sql.toString();
    }

    /**
     * Creates the sql for a select from the multi geometry table.
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     * @param gid The geometry id to select for, may be <code>null</code>.
     */
    protected String selectMultiGeometrySQL(String gid) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        dialect.encodeColumnName("id", sql);
        sql.append(",");
        dialect.encodeColumnName("mgid", sql);
        sql.append(",");
        dialect.encodeColumnName("ref", sql);

        sql.append(" FROM ");
        encodeTableName(MULTI_GEOMETRY_TABLE, sql);

        if (gid != null) {
            sql.append(" WHERE ");

            dialect.encodeColumnName("id", sql);
            sql.append(" = ");
            dialect.encodeValue(gid, String.class, sql);
        }

        return sql.toString();
    }

    /**
     * Creates the sql for the geometry association table.
     * <p>
     * This method is only called when {@link JDBCDataStore#isAssociations()}
     * is true.
     * </p>
     */
    protected String createGeometryAssociationTableSQL(Connection cx)
        throws SQLException {
        String[] sqlTypeNames = getSQLTypeNames(new Class[] {
                    String.class, String.class, String.class, Boolean.class
                }, cx);
        String[] columnNames = new String[] { "fid", "gname", "gid", "ref" };

        return createTableSQL(GEOMETRY_ASSOCIATION_TABLE, columnNames, sqlTypeNames, null);
    }

    /**
     * Creates the sql for a select from the geometry association table.
     * <p>
     * </p>
     * @param fid The fid to select for, may be <code>null</code>
     * @param gid The geometry id to select for, may be <code>null</code>
     * @param gname The geometry name to select for, may be <code>null</code>
     */
    protected String selectGeometryAssociationSQL(String fid, String gid, String gname) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        dialect.encodeColumnName("fid", sql);
        sql.append(",");
        dialect.encodeColumnName("gid", sql);
        sql.append(",");
        dialect.encodeColumnName("gname", sql);
        sql.append(",");
        dialect.encodeColumnName("ref", sql);

        sql.append(" FROM ");
        encodeTableName(GEOMETRY_ASSOCIATION_TABLE, sql);

        if (fid != null) {
            sql.append(" WHERE ");
            dialect.encodeColumnName("fid", sql);
            sql.append(" = ");
            dialect.encodeValue(fid, String.class, sql);
        }

        if (gid != null) {
            if (fid == null) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }

            dialect.encodeColumnName("gid", sql);
            sql.append(" = ");
            dialect.encodeValue(gid, String.class, sql);
        }

        if (gname != null) {
            if ((fid == null) && (gid == null)) {
                sql.append(" WHERE ");
            } else {
                sql.append(" AND ");
            }

            dialect.encodeColumnName("gname", sql);
            sql.append(" = ");
            dialect.encodeValue(gname, String.class, sql);
        }

        return sql.toString();
    }

    //    /**
    //     * Creates the sql for the multi geometry table.
    //     <p>
    //     * This method is only called when {@link JDBCDataStore#isForeignKeyGeometries()}
    //     * is true.
    //     * </p>
    //     */
    //    protected String createMultiGeometryTableSQL( Connection cx ) throws SQLException {
    //        String[] sqlTypeNames = getSQLTypeNames(
    //            new Class[]{ String.class, String.class, }, cx
    //        );
    //        String[] columnNames = new String[]{ "gid", "rgid" };
    //        
    //        return createTableSQL( MULTI_GEOMETRY_TABLE, columnNames, sqlTypeNames, null );
    //    }
    //    
    //    /**
    //     * Creates the sql for a select from the multi geometry table.
    //     * <p>
    //     * 
    //     * </p>
    //     * @param gid The id of the multi geometry, not <code>null</code>.
    //     */
    //    protected String selectMultiGeometrySQL( String gid ) {
    //        StringBuffer sql = new StringBuffer();
    //        
    //        sql.append( "SELECT " );
    //        dialect.encodeColumnName( "gid", sql ); sql.append( ",");
    //        dialect.encodeColumnName( "rgid", sql ); 
    //        
    //        sql.append( " FROM ");
    //        dialect.encodeTableName( MULTI_GEOMETRY_TABLE, sql );
    //        
    //        sql.append( " WHERE " );
    //        dialect.encodeColumnName( "gid", sql );
    //        sql.append( " = " );
    //        dialect.encodeValue( gid, String.class, sql );
    //        
    //        return sql.toString();    
    //    }

    /**
     * Helper method for building a 'CREATE TABLE' sql statement.
     */
    private String createTableSQL(String tableName, String[] columnNames, String[] sqlTypeNames,
        String pkeyColumn) {
        //build the create table sql
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE ");

        encodeTableName(tableName, sql);
        sql.append(" ( ");

        //primary key column
        if (pkeyColumn != null) {
            dialect.encodePrimaryKey(pkeyColumn, sql);
            sql.append(", ");
        }

        //normal attributes
        for (int i = 0; i < columnNames.length; i++) {
            //the column name
            dialect.encodeColumnName(columnNames[i], sql);
            sql.append(" ");

            //sql type name
            //JD: some sql dialects require strings / varchars to have an 
            // associated size with them
            if ( sqlTypeNames[i].startsWith( "VARCHAR" ) ) {
                dialect.encodeColumnType(sqlTypeNames[i] + "(255)", sql);
            }
            else {
                dialect.encodeColumnType(sqlTypeNames[i], sql);    
            }
            

            //sql.append(sqlTypeNames[i]);
            if (i < (sqlTypeNames.length - 1)) {
                sql.append(", ");
            }
        }

        sql.append(" ) ");

        //encode anything post create table
        dialect.encodePostCreateTable(tableName, sql);

        return sql.toString();
    }

    /**
     * Helper method for determining what the sql type names are for a set of
     * classes.
     * <p>
     * This method uses a combination of dialect mappings and database metadata
     * to determine which sql types map to the specified classes.
     * </p>
     */
    private String[] getSQLTypeNames(Class[] classes, Connection cx)
        throws SQLException {
        //figure out what the sql types are corresponding to the feature type
        // attributes
        int[] sqlTypes = new int[classes.length];
        String[] sqlTypeNames = new String[sqlTypes.length];

        for (int i = 0; i < classes.length; i++) {
            Class clazz = classes[i];
            Integer sqlType = getMapping(clazz);

            if (sqlType == null) {
                LOGGER.warning("No sql type mapping for: " + clazz);
                sqlType = Types.OTHER;
            }

            sqlTypes[i] = sqlType;

            //if this a geometric type, get the name from teh dialect
            //if ( attributeType instanceof GeometryDescriptor ) {
            if (Geometry.class.isAssignableFrom(clazz)) {
                String sqlTypeName = dialect.getGeometryTypeName(sqlType);

                if (sqlTypeName != null) {
                    sqlTypeNames[i] = sqlTypeName;
                }
            }
        }

        //figure out the type names that correspond to the sql types from 
        // the database metadata
        DatabaseMetaData metaData = cx.getMetaData();

        /*
         *      <LI><B>TYPE_NAME</B> String => Type name
         *        <LI><B>DATA_TYPE</B> int => SQL data type from java.sql.Types
         *        <LI><B>PRECISION</B> int => maximum precision
         *        <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal
         *      (may be <code>null</code>)
         *        <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal
             (may be <code>null</code>)
         *        <LI><B>CREATE_PARAMS</B> String => parameters used in creating
         *      the type (may be <code>null</code>)
         *        <LI><B>NULLABLE</B> short => can you use NULL for this type.
         *      <UL>
         *      <LI> typeNoNulls - does not allow NULL values
         *      <LI> typeNullable - allows NULL values
         *      <LI> typeNullableUnknown - nullability unknown
         *      </UL>
         *        <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive.
         *        <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
         *      <UL>
         *      <LI> typePredNone - No support
         *      <LI> typePredChar - Only supported with WHERE .. LIKE
         *      <LI> typePredBasic - Supported except for WHERE .. LIKE
         *      <LI> typeSearchable - Supported for all WHERE ..
         *      </UL>
         *        <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned.
         *        <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value.
         *        <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an
         *      auto-increment value.
         *        <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name
         *      (may be <code>null</code>)
         *        <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
         *        <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
         *        <LI><B>SQL_DATA_TYPE</B> int => unused
         *        <LI><B>SQL_DATETIME_SUB</B> int => unused
         *        <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
         */
        ResultSet types = metaData.getTypeInfo();

        try {
            while (types.next()) {
                int sqlType = types.getInt("DATA_TYPE");
                String sqlTypeName = types.getString("TYPE_NAME");

                for (int i = 0; i < sqlTypes.length; i++) {
                    //check if we already have the type name from the dialect
                    if (sqlTypeNames[i] != null) {
                        continue;
                    }

                    if (sqlType == sqlTypes[i]) {
                        sqlTypeNames[i] = sqlTypeName;
                    }
                }
            }
        } finally {
            closeSafe(types);
        }

        return sqlTypeNames;
    }

    /**
     * Generates a 'SELECT * FROM' sql statement.
     */
    protected String selectSQL(SimpleFeatureType featureType, Filter filter, SortBy[] sort) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");

        //column names

        //primary key
        PrimaryKey key = null;

        try {
            key = getPrimaryKey(featureType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dialect.encodeColumnName(key.getColumnName(), sql);
        sql.append(",");

        //other columns
        for (AttributeDescriptor att : featureType.getAttributeDescriptors()) {
            if (att instanceof GeometryDescriptor) {
                //encode as geometry
                dialect.encodeGeometryColumn((GeometryDescriptor) att, sql);

                //alias it to be the name of the original geometry
                dialect.encodeColumnAlias(att.getLocalName(), sql);
            } else {
                dialect.encodeColumnName(att.getLocalName(), sql);
            }

            sql.append(",");
        }

        sql.setLength(sql.length() - 1);

        sql.append(" FROM ");
        encodeTableName(featureType.getTypeName(), sql);

        //filtering
        if (filter != null && !Filter.INCLUDE.equals(filter)) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL(featureType);
                sql.append(" ").append(toSQL.encodeToString(filter));
            } catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }

        //sorting
        if ((sort != null) && (sort.length > 0)) {
            sql.append(" ORDER BY ");

            for (int i = 0; i < sort.length; i++) {
                dialect.encodeColumnName(getPropertyName(featureType, sort[i].getPropertyName()),
                    sql);

                if (sort[i].getSortOrder() == SortOrder.DESCENDING) {
                    sql.append(" DESC");
                } else {
                    sql.append(" ASC");
                }

                sql.append(",");
            }

            sql.setLength(sql.length() - 1);
        }

        return sql.toString();
    }

    /**
     * Helper method for executing a property name against a feature type.
     * <p>
     * This method will fall back on {@link PropertyName#getPropertyName()} if
     * it does not evaulate against the feature type.
     * </p>
     */
    protected String getPropertyName(SimpleFeatureType featureType, PropertyName propertyName) {
        AttributeDescriptor att = (AttributeDescriptor) propertyName.evaluate(featureType);

        if (att != null) {
            return att.getLocalName();
        }

        return propertyName.getPropertyName();
    }

    /**
     * Generates a 'SELECT' sql statement which selects bounds.
     * 
     * @param featureType The feature type / table.
     //* @param types The columns to include in the bounds calculation, may be <code>null<code>.
     * @param filter Filter specifying rows to include in bounds calculation.
     */
    protected String selectBoundsSQL(SimpleFeatureType featureType, /*Set types,*/ Filter filter) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT ");

        //walk through all geometry attributes and build the query
        for (Iterator a = featureType.getAttributeDescriptors().iterator(); a.hasNext();) {
            AttributeDescriptor attribute = (AttributeDescriptor) a.next();
            //if (types != null && !types.contains( attribute.getLocalName() ) ) {
            //    continue;
            //}
            if (attribute instanceof GeometryDescriptor) {
                String geometryColumn = featureType.getGeometryDescriptor().getLocalName();
                dialect.encodeGeometryEnvelope(geometryColumn, sql);
                sql.append(",");
            }
        }

        sql.setLength(sql.length() - 1);

        sql.append(" FROM ");
        encodeTableName(featureType.getTypeName(), sql);

        if (filter != null  && !Filter.INCLUDE.equals(filter)) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL(featureType);
                sql.append(" ").append(toSQL.encodeToString(filter));
            } catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }

        return sql.toString();
    }

    /**
     * Generates a 'SELECT count(*) FROM' sql statement.
     */
    protected String selectCountSQL(SimpleFeatureType featureType, Filter filter) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT count(*) FROM ");
        encodeTableName(featureType.getTypeName(), sql);

        if (filter != null && !Filter.INCLUDE.equals(filter)) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL(featureType);
                sql.append(" ").append(toSQL.encodeToString(filter));
            } catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }

        return sql.toString();
    }

    /**
     * Generates a 'DELETE FROM' sql statement.
     */
    protected String deleteSQL(SimpleFeatureType featureType, Filter filter) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM ");
        encodeTableName(featureType.getTypeName(), sql);

        if (filter != null && !Filter.INCLUDE.equals(filter)) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL(featureType);
                sql.append(" ").append(toSQL.encodeToString(filter));
            } catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }

        return sql.toString();
    }

    /**
     * Generates a 'INSERT INFO' sql statement.
     * @throws IOException 
     */
    protected String insertSQL(SimpleFeatureType featureType, SimpleFeature feature) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO ");
        encodeTableName(featureType.getTypeName(), sql);

        //column names
        sql.append(" ( ");

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            dialect.encodeColumnName(featureType.getDescriptor(i).getLocalName(), sql);
            sql.append(",");
        }

        //TODO: non auto incrementing primary keys
        //PrimaryKey key = null; 
        //try {
        //    key = getPrimaryKey(featureType);
        //} 
        //catch (IOException e) {
        //    throw new RuntimeException( e );
        //}
        //
        ////if primary key is not auto-incrementing we need to include it
        //if ( !( key instanceof AutoGeneratedPrimaryKey ) ) {
        //    dialect.encodeColumnName(key.getColumnName(),sql);
        //    sql.append(", ");
        //}
        //        
        sql.setLength(sql.length() - 1);

        //values
        sql.append(" ) VALUES ( ");

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeDescriptor att = featureType.getDescriptor(i);
            Class binding = att.getType().getBinding();

            Object value = feature.getAttribute(att.getLocalName());

            if (value == null) {
                if (!att.isNillable()) {
                    //TODO: throw an exception    
                }

                sql.append("null");
            } else {
                if (Geometry.class.isAssignableFrom(binding)) {
                    try {
                        Geometry g = (Geometry) value;
                        encodeGeometryValue( g, sql );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    dialect.encodeValue(value, binding, sql);
                }
            }

            sql.append(",");
        }
        sql.setLength(sql.length() - 1);

        sql.append(")");

        return sql.toString();
    }

    /**
     * Helper method for encoding a geometry, pulls out the crs, turns it to 
     * a code and then delegates to the dialecte.
     */
    protected void encodeGeometryValue( Geometry g, StringBuffer sql ) throws IOException {

        int srid = 0;

        // check for srid
        if (g.getSRID() > 0) {
            srid = g.getSRID();
        }

        if (srid == 0) {
            //check for crs object
            CoordinateReferenceSystem crs = (CoordinateReferenceSystem) g
                .getUserData();

            if (crs != null) {
                //pull out the epsg code
            }
        }

        dialect.encodeGeometryValue(g, srid, sql);
    }
    
    /**
     * Generates an 'UPDATE' sql statement.
     */
    protected String updateSQL(SimpleFeatureType featureType, AttributeDescriptor[] attributes,
        Object[] values, Filter filter) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE ");
        encodeTableName(featureType.getTypeName(), sql);

        sql.append(" SET ");

        for (int i = 0; i < attributes.length; i++) {
            dialect.encodeColumnName(attributes[i].getLocalName(), sql);
            sql.append(" = ");
            
            if ( Geometry.class.isAssignableFrom( attributes[i].getType().getBinding() ) ) {
                try {
                    encodeGeometryValue((Geometry)values[i],sql); 
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                dialect.encodeValue(values[i], attributes[i].getType().getBinding(), sql);    
            }
            
            sql.append(",");
        }

        sql.setLength(sql.length() - 1);
        sql.append(" ");

        if (filter != null  && !Filter.INCLUDE.equals(filter)) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL(featureType);
                sql.append(" ").append(toSQL.encodeToString(filter));
            } catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }

        return sql.toString();
    }

    /**
     * Creates a new instance of a filter to sql encoder.
     * <p>
     * The <tt>featureType</tt> may be null but it is not recommended. Such a 
     * case where this may neccessary is when a literal needs to be encoded in 
     * isolation.
     * </p>
     */
    protected FilterToSQL createFilterToSQL(final SimpleFeatureType featureType) {
        FilterToSQL toSQL = dialect.createFilterToSQL();
        
        toSQL.setSqlNameEscape(dialect.getNameEscape());
        toSQL.setCapabilities(filterCapabilities);
        
        if ( featureType != null ) {
            //set up a fid mapper
            //TODO: remove this
            final PrimaryKey key;

            try {
                key = getPrimaryKey(featureType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            FIDMapper mapper = new FIDMapper() {
                    public String createID(Connection conn, SimpleFeature feature, Statement statement)
                        throws IOException {
                        return null;
                    }

                    public int getColumnCount() {
                        return 1;
                    }

                    public int getColumnDecimalDigits(int colIndex) {
                        return 0;
                    }

                    public String getColumnName(int colIndex) {
                        return key.getColumnName();
                    }

                    public int getColumnSize(int colIndex) {
                        return 0;
                    }

                    public int getColumnType(int colIndex) {
                        return 0;
                    }

                    public String getID(Object[] attributes) {
                        return null;
                    }

                    public Object[] getPKAttributes(String FID)
                        throws IOException {
                        //strip off the feature type name
                        if (FID.startsWith(featureType.getTypeName() + ".")) {
                            FID = FID.substring(featureType.getTypeName().length() + 1);
                        }

                        try {
                            return new Object[] { key.decode(FID) };
                        } catch (Exception e) {
                            throw (IOException) new IOException().initCause(e);
                        }
                    }

                    public boolean hasAutoIncrementColumns() {
                        return false;
                    }

                    public void initSupportStructures() {
                    }

                    public boolean isAutoIncrement(int colIndex) {
                        return false;
                    }

                    public boolean isVolatile() {
                        return false;
                    }

                    public boolean returnFIDColumnsAsAttributes() {
                        return false;
                    }
                };
            toSQL.setFeatureType(featureType);    
            toSQL.setFIDMapper(mapper);
        }
        
        return toSQL;
    }

    /**
     * Helper method to encode table name which checks if a schema is set and
     * prefixes the table name with it.
     */
    protected void encodeTableName(String tableName, StringBuffer sql) {
        if (databaseSchema != null) {
            dialect.encodeTableName(databaseSchema, sql);
            sql.append(".");
        }

        dialect.encodeSchemaName(tableName, sql);
    }

    /**
     * Helper method for setting the gml:id of a geometry as user data.
     */
    protected void setGmlProperties(Geometry g, String gid, String name, String description) {
        // set up the user data
        Map userData = null;

        if (g.getUserData() != null) {
            if (g.getUserData() instanceof Map) {
                userData = (Map) g.getUserData();
            } else {
                userData = new HashMap();
                userData.put(g.getUserData().getClass(), g.getUserData());
            }
        } else {
            userData = new HashMap();
        }

        if (gid != null) {
            userData.put("gml:id", gid);
        }

        if (name != null) {
            userData.put("gml:name", name);
        }

        if (description != null) {
            userData.put("gml:description", description);
        }

        g.setUserData(userData);
    }
    
    /**
     * Utility method for closing a result set.
     * <p>
     * This method closed the result set "safely" in that it never throws an
     * exception. Any exceptions that do occur are logged at {@link Level#FINER}.
     * </p>
     * @param rs The result set to close.
     */
    public void closeSafe(ResultSet rs) {
        if (rs == null) {
            return;
        }

        try {
            rs.close();
        } catch (SQLException e) {
            String msg = "Error occurred closing result set";
            LOGGER.warning(msg);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, msg, e);
            }
        }
    }

    /**
     * Utility method for closing a statement.
     * <p>
     * This method closed the statement"safely" in that it never throws an
     * exception. Any exceptions that do occur are logged at {@link Level#FINER}.
     * </p>
     * @param st The statement to close.
     */
    public void closeSafe(Statement st) {
        if (st == null) {
            return;
        }

        try {
            st.close();
        } catch (SQLException e) {
            String msg = "Error occurred closing statement";
            LOGGER.warning(msg);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, msg, e);
            }
        }
    }

    /**
     * Utility method for closing a connection.
     * <p>
     * This method closed the connection "safely" in that it never throws an
     * exception. Any exceptions that do occur are logged at {@link Level#FINER}.
     * </p>
     * @param cx The connection to close.
     */
    public void closeSafe(Connection cx) {
        if (cx == null) {
            return;
        }

        try {
            cx.close();
        } catch (SQLException e) {
            String msg = "Error occurred closing connection";
            LOGGER.warning(msg);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, msg, e);
            }
        }
    }
}
