/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.ObjectPool;
import org.geotools.arcsde.ArcSdeException;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeDoesNotExistException;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRelease;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Provides thread safe access to an SeConnection.
 * <p>
 * This class has become more and more magic over time! It no longer represents a Connection but
 * provides "safe" access to a connection.
 * <p>
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.3.x
 */
public class Session {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /**
     * Lock used to protect the connection
     */
    private Lock lock;

    /** Actual SeConnection being protected */
    SeConnection connection;

    /**
     * ObjectPool used to manage open connections (shared).
     */
    private ObjectPool pool;

    private ArcSDEConnectionConfig config;

    private static int connectionCounter;

    private int connectionId;

    private boolean transactionInProgress;

    private boolean isPassivated;

    private Map<String, SeTable> cachedTables = new WeakHashMap<String, SeTable>();

    private Map<String, SeLayer> cachedLayers = new WeakHashMap<String, SeLayer>();

    private Map<String, SeRasterColumn> cachedRasters = new HashMap<String, SeRasterColumn>();

    /**
     * Provides safe access to an SeConnection.
     * 
     * @param pool ObjectPool used to manage SeConnection
     * @param config Used to set up a SeConnection
     * @throws SeException If we cannot connect
     */
    Session(ObjectPool pool, ArcSDEConnectionConfig config) throws SeException {
        this.connection = new SeConnection(config.getServerName(), config.getPortNumber()
                .intValue(), config.getDatabaseName(), config.getUserName(), config
                .getUserPassword());
        this.config = config;
        this.pool = pool;
        this.lock = new ReentrantLock(false);
        this.connection.setConcurrency(SeConnection.SE_UNPROTECTED_POLICY);

        synchronized (Session.class) {
            connectionCounter++;
            connectionId = connectionCounter;
        }
    }

    public final boolean isClosed() {
        return this.connection.isClosed();
    }

    /**
     * Marks the connection as being active (i.e. its out of the pool and ready to be used).
     * <p>
     * Shall be called just before being returned from the connection pool
     * </p>
     * 
     * @see #markInactive()
     * @see #isPassivated
     * @see #checkActive()
     */
    void markActive() {
        this.isPassivated = false;
    }

    /**
     * Marks the connection as being inactive (i.e. laying on the connection pool)
     * <p>
     * Shall be callled just before sending it back to the pool
     * </p>
     * 
     * @see #markActive()
     * @see #isPassivated
     * @see #checkActive()
     */
    void markInactive() {
        this.isPassivated = true;
    }

    /**
     * Returns whether this connection is on the connection pool domain or not.
     * 
     * @return <code>true</code> if this connection has beed returned to the pool and thus cannot
     *         be used, <code>false</code> if its safe to keep using it.
     */
    public boolean isPassivated() {
        return isPassivated;
    }

    /**
     * Sanity check method called before every public operation delegates to the superclass.
     * 
     * @throws IllegalStateException if {@link #isPassivated() isPassivated() == true} as this is a
     *             serious workflow breackage.
     */
    private void checkActive() {
        if (isPassivated()) {
            throw new IllegalStateException("Unrecoverable error: " + toString()
                    + " is passivated, shall not be used!");
        }
    }

    public synchronized SeLayer getLayer(final String layerName) throws IOException {
        checkActive();
        if (!cachedLayers.containsKey(layerName)) {
            cacheLayers();
        }
        SeLayer seLayer = cachedLayers.get(layerName);
        if (seLayer == null) {
            throw new NoSuchElementException("Layer '" + layerName + "' not found");
        }
        return seLayer;
    }

    public synchronized SeRasterColumn getRasterColumn(final String rasterName) throws IOException {
        checkActive();
        if (!cachedRasters.containsKey(rasterName)) {
            try {
                cacheRasters();
            } catch (SeException e) {
                throw new DataSourceException("Can't obtain raster " + rasterName, e);
            }
        }
        SeRasterColumn raster = cachedRasters.get(rasterName);
        if (raster == null) {
            throw new NoSuchElementException("Raster '" + rasterName + "' not found");
        }
        return raster;
    }

    public synchronized SeTable getTable(final String tableName) throws IOException {
        checkActive();
        if (!cachedTables.containsKey(tableName)) {
            cacheLayers();
        }
        SeTable seTable = (SeTable) cachedTables.get(tableName);
        if (seTable == null) {
            throw new NoSuchElementException("Table '" + tableName + "' not found");
        }
        return seTable;
    }

    /**
     * Caches both tables and layers
     * 
     * @throws SeException
     */
    @SuppressWarnings("unchecked")
    private void cacheLayers() throws IOException {
        Command<Void> cmd = new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException {
                Vector/* <SeLayer> */layers = connection.getLayers();
                String qualifiedName;
                SeLayer layer;
                SeTable table;
                cachedTables.clear();
                cachedLayers.clear();
                for (Iterator it = layers.iterator(); it.hasNext();) {
                    layer = (SeLayer) it.next();
                    qualifiedName = layer.getQualifiedName();
                    table = new SeTable(connection, qualifiedName);
                    cachedLayers.put(qualifiedName, layer);
                    cachedTables.put(qualifiedName, table);
                }
                return null;
            }
        };
        execute(cmd);
    }

    @SuppressWarnings("unchecked")
    private void cacheRasters() throws SeException {
        Vector<SeRasterColumn> rasters = this.connection.getRasterColumns();
        cachedRasters.clear();
        for (SeRasterColumn raster : rasters) {
            cachedRasters.put(raster.getQualifiedTableName(), raster);
        }
    }

    public void startTransaction() throws IOException {
        checkActive();
        execute(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                connection.startTransaction();
                transactionInProgress = true;
                return null;
            }
        });
    }

    public void commitTransaction() throws IOException {
        checkActive();
        execute(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                connection.commitTransaction();
                transactionInProgress = false;
                return null;
            }
        });
    }

    /**
     * Returns whether a transaction is in progress over this connection
     * <p>
     * As for any other public method, this one can't be called if {@link #isPassivated()} is true.
     * </p>
     * 
     * @return
     */
    public boolean isTransactionActive() {
        checkActive();
        return transactionInProgress;
    }

    public void rollbackTransaction() throws IOException {
        checkActive();
        try {
            this.connection.rollbackTransaction();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        } finally {
            transactionInProgress = false;
        }
    }

    /**
     * Return to the pool (may not close the internal connection, depends on pool settings).
     * 
     * @throws IllegalStateException if close() is called while a transaction is in progress
     * @see #destroy()
     */
    public void close() throws IllegalStateException {
        checkActive();
        if (transactionInProgress) {
            throw new IllegalStateException(
                    "Transaction is in progress, should commit or rollback before closing");
        }

        try {
            if (LOGGER.isLoggable(Level.FINER)) {
                // StackTraceElement[] stackTrace =
                // Thread.currentThread().getStackTrace();
                // String caller = stackTrace[3].getClassName() + "." +
                // stackTrace[3].getMethodName();
                // System.err.println("<- " + caller + " returning " +
                // toString() + " to pool");
                LOGGER.finer("<- returning " + toString() + " to pool");
            }
            this.pool.returnObject(this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "ArcSDEPooledConnection[" + connectionId + "]";
    }

    /**
     * Actually closes the connection
     */
    void destroy() {
        try {
            this.connection.close();
        } catch (SeException e) {
            LOGGER.info("closing connection: " + e.getMessage());
        }
    }

    /**
     * Compares for reference equality
     */
    @Override
    public boolean equals(Object other) {
        return other == this;
    }

    @Override
    public int hashCode() {
        return 17 ^ this.config.hashCode();
    }

    //
    // Helper method that delgates to internal connection
    //
    @SuppressWarnings("unchecked")
    public List<SeLayer> getLayers() throws IOException {
        try {
            return connection.getLayers();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public String getUser() throws IOException {
        try {
            return connection.getUser();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeRelease getRelease() {
        return connection.getRelease();
    }

    public String getDatabaseName() throws IOException {
        try {
            return connection.getDatabaseName();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public void setConcurrency(int policy) throws IOException {
        try {
            connection.setConcurrency(policy);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public void setTransactionAutoCommit(int auto) throws IOException {
        try {
            connection.setTransactionAutoCommit(auto);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    //
    // Factory methods that make use of internal connection
    // Q: How "long" are these objects good for? until the connection closes - or longer...
    //
    public SeLayer createSeLayer() throws IOException {
        return new SeLayer(connection);
    }

    public SeLayer createSeLayer(String tableName, String shape) throws IOException {
        try {
            return new SeLayer(connection, tableName, shape);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeQuery createSeQuery() throws IOException {
        try {
            return new SeQuery(connection);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeQuery createSeQuery(String[] propertyNames, SeSqlConstruct sql) throws IOException {
        try {
            return new SeQuery(connection, propertyNames, sql);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeRegistration createSeRegistration(String typeName) throws IOException {
        try {
            return new SeRegistration(connection, typeName);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    /**
     * Creates an SeTable named
     * <code>qualifiedName<code>; the layer does not need to exist on the server.
     * 
     * @param qualifiedName
     * @return
     * @throws IOException
     */
    public SeTable createSeTable(String qualifiedName) throws IOException {
        try {
            return new SeTable(connection, qualifiedName);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeInsert createSeInsert() throws IOException {
        try {
            return new SeInsert(connection);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeUpdate createSeUpdate() throws IOException {
        try {
            return new SeUpdate(connection);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeDelete createSeDelete() throws IOException {
        try {
            return new SeDelete(connection);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeVersion createSeVersion(String versionName) throws IOException {
        try {
            return new SeVersion(connection, versionName);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    /**
     * Create an SeState for the provided id.
     * 
     * @param stateId stateId to use, or null
     * @return SeState
     * @throws IOException
     */
    public SeState createSeState(SeObjectId stateId) throws IOException {
        try {
            return new SeState(connection, stateId);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeState createSeState() throws IOException {
        try {
            return new SeState(connection);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeRasterColumn createSeRasterColumn() throws IOException {
        try {
            return new SeRasterColumn(connection);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public SeRasterColumn createSeRasterColumn(SeObjectId rasterColumnId) throws IOException {
        try {
            return new SeRasterColumn(connection, rasterColumnId);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    /**
     * Schedule the provided Command for execution.
     * 
     * @param command
     * @throws IOException if an exception occurs handling any ArcSDE resource while executing the
     *             command
     */
    public <T> T execute(Command<T> command) throws IOException {
        try {
            lock.lock();
            try {
                return command.execute(this, connection);
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    public SeColumnDefinition[] describe(String tableName) throws IOException {
        SeTable table = getTable(tableName);
        try {
            return table.describe();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }
}