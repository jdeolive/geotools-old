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
package org.geotools.data.sde;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeTable;


/**
 * Maintains <code>SeConnection</code>'s for a single set of connection
 * properties (for instance: by server, port, user and password) in a pooled
 * way
 *
 * <p>
 * Since sde connections are not jdbc connections, I can't use Sean's excellent
 * connection pool. So I'll borrow most of it.
 * </p>
 *
 * <p>
 * This connection pool is configurable in the sense that some parameters can
 * be passed to establish the pool policy. To pass parameters to the
 * connection pool, you should set some properties in the parameters Map
 * passed to SdeDataStoreFactory.createDataStore, wich will invoke
 * SdeConnectionPoolFactory to get the SDE instance's pool singleton. That
 * instance singleton will be created with the preferences passed the first
 * time createDataStore is called for a given SDE instance/user, if subsecuent
 * calls change that preferences, they will be ignored.
 * </p>
 *
 * <p>
 * The expected optional parameters that you can set up in the argument Map for
 * createDataStore are:
 *
 * <ul>
 * <li>
 * pool.minConnections Integer, tells the minimun number of open connections
 * the pool will maintain opened
 * </li>
 * <li>
 * pool.maxConnections Integer, tells the maximun number of open connections
 * the pool will create and maintain opened
 * </li>
 * <li>
 * pool.increment Integer, tells how many connections will be created at once
 * every time an available connection is not present and the maximin number of
 * allowed connections has not been reached
 * </li>
 * <li>
 * pool.timeOut Integer, tells how many milliseconds a calling thread is
 * guaranteed to wait before getConnection() throws an
 * UnavailableConnectionException
 * </li>
 * </ul>
 * </p>
 *
 * @author Gabriel Roldán
 * @version $Id: SdeConnectionPool.java,v 1.11 2004/01/09 16:58:24 aaime Exp $
 */
public class SdeConnectionPool {
    /** package's logger */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.sde");

    /** default number of connections a pool creates at first population */
    public static final int DEFAULT_CONNECTIONS = 1;

    /** default number of maximun allowable connections a pool can hold */
    public static final int DEFAULT_MAX_CONNECTIONS = 1;

    /** default number of connections a pool increments by */
    public static final int DEFAULT_INCREMENT = 1;

    /**
     * default interval in milliseconds a calling thread waits for an available
     * connection
     */
    private static final long DEFAULT_WAIT_TIME = 1000;

    /**
     * default number of milliseconds a calling thread waits before
     * <code>getConnection</code> throws an <code>UnavailableException</code>
     */
    public static final int DEFAULT_MAX_WAIT_TIME = 10000;

    /**
     * number of milliseconds to wait in the wait loop until reach the timeout
     * period
     */
    private long waitTime = DEFAULT_WAIT_TIME;

    /** this connection pool connection's parameters */
    private SdeConnectionConfig config;

    /** list of SDE connections ready to use */
    private LinkedList availableConnections = new LinkedList();

    /** list of SDE connections actually in use */
    private LinkedList usedConnections = new LinkedList();

    /** A mutex for synchronizing */
    private Object mutex = new Object();

    /**
     * Indicates that this Connection Pool is closed and it should not return
     * connections on calls to getConnection()
     */
    private boolean closed = false;

    /**
     * Map of <code>SeLayer/SeColumnDefinition[]</code> objects available in
     * the SDE database, to optimize SDE layers lookup time due to
     */
    private Map databaseLayers;

    /**
     * Creates a new SdeConnectionPool object.
     *
     * @param config DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    protected SdeConnectionPool(SdeConnectionConfig config)
        throws DataSourceException {
        if (config == null) {
            throw new NullPointerException("parameter config can't be null");
        }

        this.config = config;
        LOGGER.info("just created SDE connection pool: " + config);
        LOGGER.fine("populating ArcSDE connection pool");

        synchronized (mutex) {
            populate();
        }

        LOGGER.fine("connection pool populated, added "
            + availableConnections.size() + " connections");
        refresh();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void refresh() throws DataSourceException {
        synchronized (mutex) {
            LOGGER.info("updating Schemas cache");
            databaseLayers = null;

            if (databaseLayers == null) {
                SeConnection sdeConn = null;

                try {
                  sdeConn = getConnection();
                }
                catch (UnavailableConnectionException ex) {
                  throw new DataSourceException("No available connection", ex);
                }

                LOGGER.finer("using " + sdeConn
                    + " to update datastore schemas cache");

                try {
                    Vector dbLayers = sdeConn.getLayers();
                    LOGGER.fine("loaded " + dbLayers.size() + " SDE Layers");
                    databaseLayers = new HashMap(2 * dbLayers.size());
                    LOGGER.fine("loading table schemas");

                    SeTable sdeTable;
                    SeLayer sdeLayer;
                    SeColumnDefinition[] tableSchema = null;

                    for (Iterator it = dbLayers.iterator(); it.hasNext();) {
                        sdeLayer = (SeLayer) it.next();

                        String name = sdeLayer.getQualifiedName();
                        LOGGER.finer("looking for table shcema of " + name);

                        try {
                            sdeTable = new SeTable(sdeConn, name);
                            tableSchema = sdeTable.describe();
                            sdeTable = null;
                        } catch (SeException tableError) {
                            LOGGER.warning("can't load schema for " + name);

                            continue;
                        }

                        databaseLayers.put(sdeLayer, tableSchema);
                        LOGGER.finer("cached table schema for " + name);
                    }
                } catch (Exception ex) {
                    throw new DataSourceException("Error getting table list:"
                        + ex.getMessage(), ex);
                } finally {
                    release(sdeConn);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private void populate() throws DataSourceException {
        synchronized (mutex) {
            int increment = config.getIncrement().intValue();
            LOGGER.info("creating " + increment + " new SDE connections");

            int actual = 0;

            while ((actual++ < increment)
                    && (getNumConnections() < config.getMaxConnections()
                                                        .intValue())) {
                try {
                    SeConnection conn = newConnection();
                    availableConnections.add(conn);
                    LOGGER.fine("added connection to pool: " + conn);
                } catch (SeException ex) {
                    throw new DataSourceException("Can't create connection to "
                        + config.getServerName() + ": " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private int getNumConnections() {
        return usedConnections.size() + availableConnections.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param seConnection DOCUMENT ME!
     */
    public void release(SeConnection seConnection) {
        if (seConnection == null) {
            return;
        }

        synchronized (mutex) {
            usedConnections.remove(seConnection);

            if (availableConnections.contains(seConnection)) {
                LOGGER.fine("trying to free an already freed connection...");
            } else {
                availableConnections.add(seConnection);
            }

            LOGGER.fine(seConnection + " freed");
        }
    }

    /**
     * closes all connections in this pool
     */
    public void close() {
        synchronized (mutex) {
            databaseLayers = null;

            int used = usedConnections.size();
            int available = availableConnections.size();

            for (int i = 0; i < used; i++) {
                SeConnection mPool = (SeConnection) usedConnections.removeFirst();

                try {
                    mPool.close();
                } catch (SeException e) {
                    LOGGER.warning("Failed to close in use PooledConnection: "
                        + e);
                }
            }

            for (int i = 0; i < available; i++) {
                SeConnection mPool = (SeConnection) availableConnections
                    .removeFirst();

                try {
                    mPool.close();
                } catch (SeException e) {
                    LOGGER.warning("Failed to close free PooledConnection: "
                        + e);
                }
            }

            closed = true;
            LOGGER.info("SDE connection pool closed. " + (used + available)
                + " connections freed");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException
     * @throws IllegalStateException DOCUMENT ME!
     */
    public SeConnection getConnection()
        throws DataSourceException, UnavailableConnectionException
    {
        if (closed) {
            throw new IllegalStateException(
                "The ConnectionPool has been closed.");
        }

        long timeWaited = 0;
        long timeOut = config.getConnTimeOut().intValue();

        synchronized (mutex) {
            try {
                if (availableConnections.size() == 0) {
                    populate();
                }

                while ((availableConnections.size() == 0)
                        && (timeWaited < timeOut)) {
                    LOGGER.finer("waiting for connection...");
                    mutex.wait(waitTime);
                    timeWaited += waitTime;
                }
                if(timeWaited > 0)
                  LOGGER.fine("waited for connection for " + timeWaited + "ms");

                if (availableConnections.size() > 0) {
                    return getAvailable();
                } else {
                    UnavailableConnectionException uce = new UnavailableConnectionException(usedConnections
                            .size(), getConfig());
                    Throwable trace = uce.fillInStackTrace();
                    uce.setStackTrace(trace.getStackTrace());
                    uce.printStackTrace();
                    throw uce;
                }
            } catch (InterruptedException ex) {
                throw new DataSourceException(
                    "Interrupted while waiting for an available connection");
            } finally {
                mutex.notifyAll();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private SeConnection getAvailable() {
        LOGGER.finest("Getting available connection.");

        SeConnection conn = (SeConnection) availableConnections.removeFirst();
        usedConnections.add(conn);
        LOGGER.fine(conn + " now in use");

        return conn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeConnection newPooledConnection() throws DataSourceException {
        int existents = availableConnections.size() + usedConnections.size();

        //one never knows...
        if (existents >= config.getMaxConnections().intValue()) {
            throw new DataSourceException(
                "Maximun number of connections reached");
        }

        SeConnection connection = null;

        try {
            connection = newConnection();
            usedConnections.add(connection);
        } catch (SeException ex) {
            throw new DataSourceException(
                "can't create a sde pooled connection: "
                + ex.getSeError().getSdeErrMsg(), ex);
        }

        return connection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    private SeConnection newConnection() throws SeException {
        SeConnection seConn = new SeConnection(config.getServerName(),
                config.getPortNumber().intValue(), config.getDatabaseName(),
                config.getUserName(), config.getUserPassword());
        LOGGER.fine("***************\ncreated new connection " + seConn
            + "\n*****************");
        seConn.setConcurrency(SeConnection.SE_LOCK_POLICY);

        // SeConnection.SeStreamSpec stSpec = seConn.getStreamSpec();

        /*
           System.out.println("getMinBufSize=" + stSpec.getMinBufSize());
           System.out.println("getMaxBufSize=" + stSpec.getMaxBufSize());
           System.out.println("getMaxArraySize=" + stSpec.getMaxArraySize());
           System.out.println("getMinObjects=" + stSpec.getMinObjects());
           System.out.println("getAttributeArraySize=" + stSpec.getAttributeArraySize());
           System.out.println("getShapePointArraySize=" + stSpec.getShapePointArraySize());
           System.out.println("getStreamPoolSize=" + stSpec.getStreamPoolSize());
         */
        /*
           stSpec.setMinBufSize(1024 * 1024);
           stSpec.setMaxBufSize(10 * 1024 * 1024);
           stSpec.setMaxArraySize(10000);
           stSpec.setMinObjects(1024);
           stSpec.setAttributeArraySize(1024 * 1024);
           stSpec.setShapePointArraySize(1024 * 1024);
           stSpec.setStreamPoolSize(10);
         */
        /*
           System.out.println("********************************************");
           System.out.println("getMinBufSize=" + stSpec.getMinBufSize());
           System.out.println("getMaxBufSize=" + stSpec.getMaxBufSize());
           System.out.println("getMaxArraySize=" + stSpec.getMaxArraySize());
           System.out.println("getMinObjects=" + stSpec.getMinObjects());
           System.out.println("getAttributeArraySize=" + stSpec.getAttributeArraySize());
           System.out.println("getShapePointArraySize=" + stSpec.getShapePointArraySize());
           System.out.println("getStreamPoolSize=" + stSpec.getStreamPoolSize());
         */
        return seConn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     */
    public SeColumnDefinition[] getTableSchema(String tableName)
        throws NoSuchElementException {
        SeLayer layer = getSdeLayer(tableName);

        if (layer == null) {
            throw new NoSuchElementException("no FeatureType named "
                + tableName
                + " found. May be you should refresh the connection.");
        }

        return getTableSchema(layer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param layer DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    public SeColumnDefinition[] getTableSchema(SeLayer layer)
        throws NoSuchElementException, NullPointerException {
        if (layer == null) {
            throw new NullPointerException(
                "you must pass a SeLayer to look its schema for. Passed null");
        }

        SeColumnDefinition[] sdeSchema = (SeColumnDefinition[]) databaseLayers
            .get(layer);

        if (sdeSchema == null) {
            throw new NoSuchElementException("No shcema found for "
                + layer.getName()
                + ". May be you should refresh the connection");
        }

        return sdeSchema;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     */
    public SeLayer getSdeLayer(String typeName) throws NoSuchElementException {
        Collection layers = databaseLayers.keySet();
        SeLayer layer = null;

        try {
            for (Iterator it = layers.iterator(); it.hasNext();) {
                layer = (SeLayer) it.next();

                if (layer.getQualifiedName().equals(typeName)
                        || layer.getName().equals(typeName)) {
                    break;
                }

                layer = null;
            }
        } catch (SeException ex) {
            throw new NoSuchElementException(ex.getMessage());
        }

        return layer;
    }

    /**
     * gets the list of available SeLayers on the database
     *
     * @return a <code>Vector&lt;SeLayer</code> with the registered
     *         featureclasses on the ArcSDE database
     *
     * @throws DataSourceException
     */
    public List getAvailableSdeLayers() throws DataSourceException {
        Set layers = databaseLayers.keySet();
        List copy = new ArrayList(layers);

        return copy;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SdeConnectionConfig getConfig() {
        return config;
    }
}
