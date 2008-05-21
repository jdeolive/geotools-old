/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Management Committee (PMC)
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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsqlparser.statement.select.PlainSelect;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.data.versioning.AutoCommitDefaultVersionHandler;
import org.geotools.arcsde.data.view.QueryInfoParser;
import org.geotools.arcsde.data.view.SelectQualifier;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.Session;
import org.geotools.arcsde.pool.Command;
import org.geotools.data.DataAccess;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.LockingManager;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQueryInfo;

/**
 * DataStore implementation to work upon an ArcSDE spatial database gateway.
 * 
 * @author Gabriel Roldan (TOPP)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcSDEDataStore.java $
 * @version $Id$
 */
public class ArcSDEDataStore implements DataStore {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    /**
     * Manages listener lists for FeatureSource<SimpleFeatureType, SimpleFeature> implementation
     */
    final FeatureListenerManager listenerManager = new FeatureListenerManager();

    private final ArcSDEConnectionPool connectionPool;

    /**
     * ArcSDE registered layers definitions
     */
    private final Map<String, FeatureTypeInfo> featureTypeInfos;

    /**
     * In process view definitions. This map is populated through
     * {@link #registerView(String, PlainSelect)}
     */
    private final Map<String, FeatureTypeInfo> inProcessFeatureTypeInfos;

    /**
     * Namespace URI to construct FeatureTypes and AttributeTypes with
     */
    private String namespace;

    /**
     * Creates a new ArcSDE DataStore working over the given connection pool
     * 
     * @param connPool pool of {@link Session} this datastore works upon.
     */
    public ArcSDEDataStore(final ArcSDEConnectionPool connPool) {
        this(connPool, null);
    }

    /**
     * Creates a new ArcSDE DataStore working over the given connection pool
     * 
     * @param connPool pool of {@link Session} this datastore works upon.
     * @param namespaceUri namespace URI for the {@link SimpleFeatureType}s, {@link AttributeType}s,
     *            and {@link AttributeDescriptor}s created by this datastore. May be
     *            <code>null</code>.
     */
    public ArcSDEDataStore(final ArcSDEConnectionPool connPool, final String namespaceUri) {
        this.connectionPool = connPool;
        this.namespace = namespaceUri;
        this.featureTypeInfos = new HashMap<String, FeatureTypeInfo>();
        this.inProcessFeatureTypeInfos = new HashMap<String, FeatureTypeInfo>();
    }

    /**
     * @see DataStore#createSchema(SimpleFeatureType)
     * @see #createSchema(SimpleFeatureType, Map)
     */
    public void createSchema(final SimpleFeatureType featureType) throws IOException {
        createSchema(featureType, null);
    }

    /**
     * Obtains the schema for the given featuretype name.
     * 
     * @see DataStore#getSchema(String)
     */
    public synchronized SimpleFeatureType getSchema(final String typeName)
            throws java.io.IOException {
        FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName);
        SimpleFeatureType schema = typeInfo.getFeatureType();
        return schema;
    }

    /**
     * List of type names; should be a list of all feature classes.
     * 
     * @return the list of full qualified feature class names on the ArcSDE database this DataStore
     *         works on. An ArcSDE full qualified class name is composed of three dot separated
     *         strings: "DATABASE.USER.CLASSNAME", wich is usefull enough to use it as namespace
     * @throws RuntimeException if an exception occurs while retrieving the list of registeres
     *             feature classes on the backend, or while obtaining the full qualified name of one
     *             of them
     */
    public String[] getTypeNames() throws IOException {
        List<String> layerNames = new ArrayList<String>(connectionPool.getAvailableLayerNames());
        layerNames.addAll(inProcessFeatureTypeInfos.keySet());
        return layerNames.toArray(new String[layerNames.size()]);
    }

    public ServiceInfo getInfo() {
        DefaultServiceInfo info = new DefaultServiceInfo();
        info.setDescription("Features from ArcSDE");
        info.setSchema(FeatureTypes.DEFAULT_NAMESPACE);
        return info;
    }

    /**
     * TODO: implement dispose()!
     */
    public void dispose() {
        LOGGER.info("dispose not yet implemented for ArcSDE, don't forget to do that!");
    }

    /**
     * Returns an {@link ArcSDEFeatureReader}
     * <p>
     * Preconditions:
     * <ul>
     * <li><code>query != null</code>
     * <li><code>query.getTypeName() != null</code>
     * <li><code>query.getFilter != null</code>
     * <li><code>transaction != null</code>
     * </ul>
     * </p>
     * 
     * @see DataStore#getFeatureReader(Query, Transaction)
     * @return {@link ArcSDEFeatureReader} aware of the transaction state
     */
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(final Query query,
            final Transaction transaction) throws IOException {
        assert query != null;
        final String typeName = query.getTypeName();
        assert typeName != null;
        assert query.getFilter() != null;
        assert transaction != null;

        Session session;
        ArcSdeVersionHandler versionHandler = ArcSdeVersionHandler.NONVERSIONED_HANDLER;
        {
            final FeatureTypeInfo featureTypeInfo = getFeatureTypeInfo(typeName);
            final boolean versioned = featureTypeInfo.isVersioned();

            if (Transaction.AUTO_COMMIT.equals(transaction)) {
                session = connectionPool.getSession();
            } else {
                ArcTransactionState state = ArcTransactionState.getState(this,transaction,
                        listenerManager, versioned);
                versionHandler = state.getVersionHandler();
                session = state.getConnection();
            }
        }

        // indicates the feature reader should close the connection when done
        // if it's not inside a transaction.
        final boolean handleConnection = true;
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = getFeatureReader(query, session,
                handleConnection, versionHandler);

        return reader;
    }

    /**
     * Returns an {@link ArcSDEFeatureReader} for the given query that works against the given
     * connection.
     * <p>
     * Explicitly stating the connection to use allows for the feature reader to fetch the
     * differences (additions/modifications/deletions) made while a transaction is in progress.
     * </p>
     * 
     * @param query the Query containing the request criteria
     * @param session the session to use to retrieve content. It'll be closed by the returned
     *            FeatureReader<SimpleFeatureType, SimpleFeature> only if the connection does not
     *            has a {@link Session#isTransactionActive() transaction in progress}.
     * @param readerClosesConnection flag indicating whether the reader should auto-close the
     *            connection when exhausted/closed. <code>false</code> indicates never close it as
     *            its being used as the streamed content of a feature writer.
     * @return
     * @throws IOException
     */
    FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(final Query query,
            final Session session,
            final boolean readerClosesConnection,
            final ArcSdeVersionHandler versionHandler) throws IOException {
        final String typeName = query.getTypeName();
        final String propertyNames[] = query.getPropertyNames();

        final FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName, session );
        final SimpleFeatureType completeSchema = typeInfo.getFeatureType();
        final ArcSDEQuery sdeQuery;

        Filter filter = query.getFilter();
        SimpleFeatureType featureType = completeSchema;

        if (propertyNames != null || query.getCoordinateSystem() != null) {
            try {
                featureType = DataUtilities.createSubType(featureType, propertyNames, query
                        .getCoordinateSystem());
            } catch (SchemaException e) {
                LOGGER.log(Level.FINEST, e.getMessage(), e);
                throw new DataSourceException("Could not create Feature Type for query", e);

            }
        }
        if (filter == Filter.EXCLUDE || filter.equals(Filter.EXCLUDE)) {
            return new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(featureType);
        }

        if (typeInfo.isInProcessView()) {
            SeQueryInfo definitionQuery = typeInfo.getSdeDefinitionQuery();
            PlainSelect viewSelectStatement = typeInfo.getDefinitionQuery();
            sdeQuery = ArcSDEQuery.createInprocessViewQuery(session, completeSchema, query,
                    definitionQuery, viewSelectStatement);
        } else {
            final FIDReader fidStrategy = typeInfo.getFidStrategy();
            sdeQuery = ArcSDEQuery.createQuery(session, completeSchema, query, fidStrategy,
                    versionHandler);
        }

        // /sdeQuery.execute();

        // this is the one which's gonna close the connection when done
        final ArcSDEAttributeReader attReader;
        attReader = new ArcSDEAttributeReader(sdeQuery, session, readerClosesConnection);
        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        try {
            reader = new ArcSDEFeatureReader(attReader);
        } catch (SchemaException e) {
            throw new RuntimeException("Schema missmatch, should never happen!: " + e.getMessage(),
                    e);
        }

        filter = getUnsupportedFilter(typeInfo, filter, session);
        if (!filter.equals(Filter.INCLUDE)) {
            reader = new FilteringFeatureReader<SimpleFeatureType, SimpleFeature>(reader, filter);
        }

        if (!featureType.equals(reader.getFeatureType())) {
            LOGGER.fine("Recasting feature type to subtype by using a ReTypeFeatureReader");
            reader = new ReTypeFeatureReader(reader, featureType, false);
        }

        if (query.getMaxFeatures() != Query.DEFAULT_MAX) {
            reader = new MaxFeatureReader<SimpleFeatureType, SimpleFeature>(reader, query
                    .getMaxFeatures());
        }

        return reader;
    }

    /**
     * @see DataStore#getFeatureSource(String)
     * @return {@link FeatureSource} or {@link FeatureStore} depending on if the user has write
     *         permissions over <code>typeName</code>
     */
    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(final String typeName)
            throws IOException {
        final FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName);
        final ArcSdeVersionHandler versionHandler = getVersionHandler(typeName,
                Transaction.AUTO_COMMIT);
        FeatureSource<SimpleFeatureType, SimpleFeature> fsource;
        if (typeInfo.isWritable()) {
            fsource = new ArcSdeFeatureStore(typeInfo, this, versionHandler);
        } else {
            fsource = new ArcSdeFeatureSource(typeInfo, this, versionHandler);
        }
        return fsource;
    }

    /**
     * Delegates to
     * {@link #getFeatureWriter(String, Filter, Transaction) getFeatureWriter(typeName, Filter.INCLUDE, transaction)}
     * 
     * @see DataStore#getFeatureWriter(String, Transaction)
     */
    public ArcSdeFeatureWriter getFeatureWriter(final String typeName, final Transaction transaction)
            throws IOException {
        return getFeatureWriter(typeName, Filter.INCLUDE, transaction);
    }

    /**
     * @param typeName
     * @param transaction
     * @return
     * @throws IOException
     */
    ArcSdeVersionHandler getVersionHandler(final String typeName, final Transaction transaction)
            throws IOException {
        ArcSdeVersionHandler versionHandler = ArcSdeVersionHandler.NONVERSIONED_HANDLER;
        {
            final FeatureTypeInfo featureTypeInfo = getFeatureTypeInfo(typeName);
            final boolean versioned = featureTypeInfo.isVersioned();

            if (Transaction.AUTO_COMMIT.equals(transaction)) {
                if (versioned) {
                    versionHandler = new AutoCommitDefaultVersionHandler();
                }
            } else {
                ArcTransactionState state;
                state = ArcTransactionState.getState(this,transaction, listenerManager,
                        versioned);
                versionHandler = state.getVersionHandler();
            }
        }
        return versionHandler;
    }

    /**
     * Execute code that requires an ArcSDEConnection for a read-only activity.
     * <p>
     * This method should be used when performing a read-only task such as fetching metadata about a
     * table. Because this is a read-only activity it does not matter what Transaction is used for
     * this work.
     * <p>
     * 
     * @param runnable Code to be executed with an ArcSDEConnection
     */
    void getConnection(Command runnable) throws IOException {
        // for now we will just make use of Transaction.AUTO_COMMIT
        getConnection(runnable, Transaction.AUTO_COMMIT);
    }

    /**
     * Execute code that requires an ArcSDEConnection for a read/write activity.
     * 
     * @param runnable
     * @param transaction
     */
    void getConnection(Command command, Transaction transaction) throws IOException {
        final Session session;
        final ArcTransactionState state;

        if (Transaction.AUTO_COMMIT.equals(transaction)) {
            session = connectionPool.getSession();
            try {
                session.issue(command);
            } finally {
                session.close(); // return to pool
            }
            state = null;
        } else {
            state = ArcTransactionState.getState(this,transaction, listenerManager,
                    false);
            session = state.getConnection();
            session.issue(command);
        }
    }

    /**
     * @see DataStore#getFeatureWriter(String, Filter, Transaction)
     */
    public ArcSdeFeatureWriter getFeatureWriter(final String typeName,
            final Filter filter,
            final Transaction transaction) throws IOException {
        // get the connection the streamed writer content has to work over
        // so the reader and writer share it
        final Session session;
        final ArcTransactionState state;
        final boolean versioned;
        final ArcSdeVersionHandler versionHandler = getVersionHandler(typeName, transaction);
        {
            final FeatureTypeInfo featureTypeInfo = getFeatureTypeInfo(typeName);
            versioned = featureTypeInfo.isVersioned();

            if (Transaction.AUTO_COMMIT.equals(transaction)) {
                session = connectionPool.getSession();
                state = null;
            } else {
                state = ArcTransactionState.getState(this,transaction, listenerManager,
                        versioned);
                session = state.getConnection();
            }
        }

        try {
            final FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName, session);
            if (!typeInfo.isWritable()) {
                throw new DataSourceException(typeName + " is not writable");
            }
            final SimpleFeatureType featureType = typeInfo.getFeatureType();

            final DefaultQuery query = new DefaultQuery(typeName, filter);
            // don't let the reader close the connection as the writer needs it
            final boolean closeConnection = false;
            final FeatureReader<SimpleFeatureType, SimpleFeature> reader;
            reader = getFeatureReader(query, session, closeConnection, versionHandler);

            final ArcSdeFeatureWriter writer;

            final FIDReader fidReader = typeInfo.getFidStrategy();

            if (Transaction.AUTO_COMMIT == transaction) {
                writer = new AutoCommitFeatureWriter(fidReader, featureType, reader, session,
                        listenerManager, versionHandler);
            } else {
                // if there's a transaction, the reader and the writer will
                // share the connection held in the transaction state
                writer = new TransactionFeatureWriter(fidReader, featureType, reader, state,
                        listenerManager);
            }
            return writer;
        } catch (IOException e) {
            try {
                session.rollbackTransaction();
            } finally {
                session.close();
            }
            throw e;
        } catch (RuntimeException e) {
            try {
                session.rollbackTransaction();
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction on " + session, e);
            } finally {
                session.close();
            }
            throw e;
        }
    }

    /**
     * Delegates to
     * {@link #getFeatureWriter(String, Filter, Transaction) getFeatureWriter(typeName, Filter.EXCLUDE, transaction)}
     * 
     * @see DataStore#getFeatureWriterAppend(String, Transaction)
     */
    public ArcSdeFeatureWriter getFeatureWriterAppend(final String typeName,
            final Transaction transaction) throws IOException {
        return getFeatureWriter(typeName, Filter.EXCLUDE, transaction);
    }

    /**
     * @return <code>null</code>, no locking yet
     * @see DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return null;
    }

    /**
     * @see DataStore#getView(Query)
     */
    public FeatureSource<SimpleFeatureType, SimpleFeature> getView(final Query query)
            throws IOException, SchemaException {
        return new DefaultView(this.getFeatureSource(query.getTypeName()), query);
    }

    /**
     * This operation is not supported at this version of the GeoTools ArcSDE plugin.
     * 
     * @see DataStore#updateSchema(String, SimpleFeatureType)
     */
    public void updateSchema(final String typeName, final SimpleFeatureType featureType)
            throws IOException {
        throw new UnsupportedOperationException("Schema modification not supported");
    }

    /**
     * Delegates to {@link #getFeatureSource(String)} with {@code name.getLocalPart()}
     * 
     * @since 2.5
     * @see DataAccess#getFeatureSource(Name)
     */
    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(Name typeName)
            throws IOException {
        return getFeatureSource(typeName.getLocalPart());
    }

    /**
     * Returns the same list of names than {@link #getTypeNames()} meaning the returned Names have
     * no namespace set.
     * 
     * @since 2.5
     * @see DataAccess#getNames()
     */
    public List<Name> getNames() throws IOException {
        String[] typeNames = getTypeNames();
        List<Name> names = new ArrayList<Name>(typeNames.length);
        for (String typeName : typeNames) {
            names.add(new NameImpl(typeName));
        }
        return names;
    }

    /**
     * Delegates to {@link #getSchema(String)} with {@code name.getLocalPart()}
     * 
     * @since 2.5
     * @see DataAccess#getSchema(Name)
     */
    public SimpleFeatureType getSchema(Name name) throws IOException {
        return getSchema(name.getLocalPart());
    }

    /**
     * Delegates to {@link #updateSchema(String, SimpleFeatureType)} with
     * {@code name.getLocalPart()}
     * 
     * @since 2.5
     * @see DataAccess#getFeatureSource(Name)
     */
    public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
        updateSchema(typeName.getLocalPart(), featureType);
    }

    // ////// NON API Methods /////////

    /**
     * Returns the unsupported part of the passed filter, so a FilteringFeatureReader will be
     * constructed upon it. Otherwise it will just return the same filter.
     * <p>
     * If the complete filter is supported, returns <code>Filter.INCLUDE</code>
     * </p>
     */
    private org.opengis.filter.Filter getUnsupportedFilter(final FeatureTypeInfo typeInfo,
            final Filter filter,
            final Session session) {
        try {
            SeLayer layer;
            SeQueryInfo qInfo;

            if (typeInfo.isInProcessView()) {
                qInfo = typeInfo.getSdeDefinitionQuery();
                String mainLayerName;
                try {
                    mainLayerName = qInfo.getConstruct().getTables()[0];
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
                layer = Session.issueGetLayer(session, mainLayerName);
            } else {
                layer = Session.issueGetLayer(session, typeInfo.getFeatureTypeName());
                qInfo = null;
            }

            FIDReader fidReader = typeInfo.getFidStrategy();

            SimpleFeatureType schema = typeInfo.getFeatureType();
            PlainSelect viewSelectStatement = typeInfo.getDefinitionQuery();

            ArcSDEQuery.FilterSet filters = ArcSDEQuery.createFilters(layer, schema, filter, qInfo,
                    viewSelectStatement, fidReader);

            Filter result = filters.getUnsupportedFilter();

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Supported filters: " + filters.getSqlFilter() + " --- "
                        + filters.getGeometryFilter());
                LOGGER.fine("Unsupported filter: " + result.toString());
            }

            return result;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        return filter;
    }

    /**
     * Connection pool as provided during construction.
     * 
     * @return Connection Pool (as provided during construction)
     */
    ArcSDEConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * Check inProcessFeatureTypeInfos and featureTypeInfos for the provided typeName, checking the
     * ArcSDE server as a last resort.
     * 
     * @param typeName
     * @return FeatureTypeInfo
     * @throws java.io.IOException
     */
    FeatureTypeInfo getFeatureTypeInfo(final String typeName) throws java.io.IOException {
        assert typeName != null;

        // Check if we have a view
        FeatureTypeInfo typeInfo = inProcessFeatureTypeInfos.get(typeName);
        if (typeInfo != null) {
            return typeInfo;
        }
        // Check if this is a known featureType
        typeInfo = featureTypeInfos.get(typeName);
        if (typeInfo != null) {
            return typeInfo;
        }
        return getFeatureTypeInfo(typeName, getConnectionPool());
    }

    /**
     * Obtain a connection used to retrieve the user name if a non qualified type name was passed
     * in.
     * <p>
     * This method is responsible for leasing a connection from the provided pool and calling
     * getFeatureTypeInfo( typeName, connection ) to populate inProcessFeatureTypeInfos.
     * 
     * @param typeName
     * @param pool
     * @return Generated FeatureTypeInfo for typeName
     */
    protected synchronized FeatureTypeInfo getFeatureTypeInfo(final String typeName, ArcSDEConnectionPool pool)
            throws IOException {

        FeatureTypeInfo ftInfo = inProcessFeatureTypeInfos.get(typeName);
        if (ftInfo == null) {
            synchronized (featureTypeInfos) {
                ftInfo = featureTypeInfos.get(typeName);
                if (ftInfo == null) {
                    ftInfo = ArcSDEAdapter.fetchSchema(typeName, this.namespace, pool);
                    featureTypeInfos.put(typeName, ftInfo);
                }
            }
        }
        return ftInfo;
    }
    /**
     * Used by feature reader and writer to get the schema information.
     * <p>
     * They are making use of this function because they already have their own Session
     * to request the ftInfo if needed.
     * </p>
     * @param typeName
     * @param session
     * @return
     * @throws IOException
     */
    synchronized FeatureTypeInfo getFeatureTypeInfo(final String typeName, Session session)
    throws IOException {

FeatureTypeInfo ftInfo = inProcessFeatureTypeInfos.get(typeName);
if (ftInfo == null) {
    synchronized (featureTypeInfos) {
        ftInfo = featureTypeInfos.get(typeName);
        if (ftInfo == null) {
            ftInfo = ArcSDEAdapter.fetchSchema(typeName, this.namespace, session );
            featureTypeInfos.put(typeName, ftInfo);
        }
    }
}
return ftInfo;
}
    /**
     * Creates a given FeatureType on the ArcSDE instance this DataStore is running over.
     * <p>
     * This deviation from the {@link DataStore#createSchema(SimpleFeatureType)} API is to allow the
     * specification of ArcSDE specific hints for the "Feature Class" to create:
     * <ul>
     * At this time the following hints may be passed:
     * <li><b>configuration.keywords</b>: database configuration keyword to use for the newly
     * create feature type. In not present, <code>"DEFAULTS"</code> will be used.</li>
     * <li><b>rowid.column.name</b>: indicates the name of the table column to set up as the
     * unique identifier, and thus to be used as feature id.</li>
     * <li><b>rowid.column.type</b>: The row id column type. Must be one of the following allowed
     * values: <code>"NONE"</code>, <code>"USER"</code>, <code>"SDE"</code> in order to set
     * up the row id column name to not be managed at all, to be user managed or to be managed by
     * ArcSDE, respectively. Refer to the ArcSDE documentation for an explanation of the meanings of
     * those terms.</li>
     * </ul>
     * </p>
     * 
     * @param featureType
     * @param hints
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void createSchema(final SimpleFeatureType featureType, final Map<String, String> hints)
            throws IOException, IllegalArgumentException {
        final Session session = connectionPool.getSession();
        try {
            ArcSDEAdapter.createSchema(featureType, hints, session);
        } finally {
            session.close();
        }
    }

    /**
     * Supported constructs:
     * <ul>
     * <li>FromItems
     * <li>SelectItems
     * <li>Top (as in SELECT TOP 10 * FROM...)
     * <li>Where
     * </ul>
     * 
     * @param typeName
     * @param select
     * @throws IOException
     */
    void registerView(final String typeName, final PlainSelect select) throws IOException {
        if (typeName == null)
            throw new NullPointerException("typeName");
        if (select == null)
            throw new NullPointerException("select");
        if (Arrays.asList(getTypeNames()).contains(typeName)) {
            throw new IllegalArgumentException(typeName + " already exists as a FeatureType");
        }

        verifyQueryIsSupported(select);

        final Session session = connectionPool.getSession();

        try {
            final PlainSelect qualifiedSelect = SelectQualifier.qualify(session, select);
            // System.out.println(qualifiedSelect);

            final SeQueryInfo queryInfo;
            try {
                LOGGER.fine("creating definition query info");
                queryInfo = QueryInfoParser.parse(session, qualifiedSelect);
            } catch (SeException e) {
                throw new ArcSdeException("Error Parsing select: " + qualifiedSelect, e);
            }
            FeatureTypeInfo typeInfo = ArcSDEAdapter.createInprocessViewSchema(session, typeName,
                    namespace, qualifiedSelect, queryInfo);

            inProcessFeatureTypeInfos.put(typeName, typeInfo);
        } finally {
            session.close();
        }
    }

    /**
     * Unsupported constructs:
     * <ul>
     * <li>GroupByColumnReferences
     * <li>Joins
     * <li>Into
     * <li>Limit
     * </ul>
     * Not yet verified to work:
     * <ul>
     * <li>Distinct
     * <li>Having
     * <li>
     * </ul>
     * 
     * @param select
     * @throws UnsupportedOperationException if any of the unsupported constructs are found on
     *             <code>select</code>
     */
    private void verifyQueryIsSupported(PlainSelect select) throws UnsupportedOperationException {
        List<Object> errors = new LinkedList<Object>();
        // @TODO errors.add(select.getDistinct());
        // @TODO errors.add(select.getHaving());
        verifyUnsupportedSqlConstruct(errors, select.getGroupByColumnReferences());
        verifyUnsupportedSqlConstruct(errors, select.getInto());
        verifyUnsupportedSqlConstruct(errors, select.getJoins());
        verifyUnsupportedSqlConstruct(errors, select.getLimit());
        if (errors.size() > 0) {
            throw new UnsupportedOperationException("The following constructs are not supported: "
                    + errors);
        }
    }

    /**
     * If construct is not null or an empty list, adds it to the list of errors.
     * 
     * @param errors
     * @param construct
     */
    private void verifyUnsupportedSqlConstruct(List<Object> errors, Object construct) {
        if (construct instanceof List) {
            List constructsList = (List) construct;
            if (constructsList.size() > 0) {
                errors.add(constructsList);
            }
        } else if (construct != null) {
            errors.add(construct);
        }
    }
}
