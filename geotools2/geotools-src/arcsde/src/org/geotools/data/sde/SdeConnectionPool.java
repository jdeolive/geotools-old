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

import com.esri.sde.sdk.client.*;
import org.geotools.data.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;


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
 * @author Gabriel Roldán
 * @version 0.1
 *
 * @task TODO: make it read a properties file to get connection pool
 *       information, such as min/max connections, expire time, etc.
 */
public class SdeConnectionPool
{
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
    private static final long DEFAULT_MAX_WAIT_TIME = 10000;

    /** minimun number of SDE connections this pool will hold */
    private int minConnections = DEFAULT_CONNECTIONS;

    /**
     * maximun number of connections, both used and available, this pool is
     * allowed to hold
     */
    private int maxConnections = DEFAULT_MAX_CONNECTIONS;

    /**
     * amount of SDE connections to be created in each repopulation of this
     * pool's connections
     */
    private int incrementStep = DEFAULT_INCREMENT;

    /**
     * how many milliseconds to wait between each attempt to obtain an
     * available connection
     */
    private long waitTime = DEFAULT_WAIT_TIME;

    /**
     * maximun number of millisecons to wait for an available connection before
     * throwing an <code>UnavailableConnectionException</code>
     */
    private long maxWaitTime = DEFAULT_MAX_WAIT_TIME;

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
     * Vector of <code>SeLayer</code> objects available in the SDE database, to
     * optimize SDE layers lookup time due to
     */
    private Vector databaseLayers;

    /**
     * Creates a new SdeConnectionPool object.
     *
     * @param config DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    protected SdeConnectionPool(SdeConnectionConfig config)
        throws DataSourceException
    {
        if (config == null)
        {
            throw new NullPointerException("parameter config can't be null");
        }

        this.config = config;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void populate() throws DataSourceException
    {
        synchronized (mutex)
        {
            int increment = getIncrementStep();

            int actual = 0;

            while ((actual++ < increment)
                    && (getNumConnections() < getMaxConnections()))
            {
                try
                {
                    availableConnections.add(newConnection());
                }
                catch (SeException ex)
                {
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
    private int getNumConnections()
    {
        return usedConnections.size() + availableConnections.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param seConnection DOCUMENT ME!
     */
    public void release(SeConnection seConnection)
    {
        if (seConnection == null)
        {
            return;
        }

        synchronized (mutex)
        {
            usedConnections.remove(seConnection);

            if (availableConnections.contains(seConnection))
                LOGGER.fine("trying to free an already freed connection...");

            else
                availableConnections.add(seConnection);

            LOGGER.fine(seConnection + " freed");
        }
    }

    /**
     * closes all connections in this pool
     */
    public void close()
    {
        synchronized (mutex)
        {
            databaseLayers = null;

            int used = usedConnections.size();

            int available = availableConnections.size();

            for (int i = 0; i < used; i++)
            {
                SeConnection mPool = (SeConnection) usedConnections.removeFirst();

                try
                {
                    mPool.close();
                }
                catch (SeException e)
                {
                    LOGGER.warning("Failed to close in use PooledConnection: "
                        + e);
                }
            }

            for (int i = 0; i < available; i++)
            {
                SeConnection mPool = (SeConnection) availableConnections
                    .removeFirst();

                try
                {
                    mPool.close();
                }
                catch (SeException e)
                {
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
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public SeConnection getConnection()
        throws DataSourceException
    {
        SeConnection conn = null;

        if (closed)
        {
            throw new IllegalStateException("The ConnectionPool has been closed.");
        }

        //if there are no available connections
        if (availableConnections.size() == 0)
        {
            //first, try to create new ones
            populate();
        }

        //and check again
        synchronized (mutex)
        {
            if (availableConnections.size() > 0)
            {
                return getAvailable();
            }
        }

        long timeWaited = 0;

        //then, wait until a connection be freed or the time out has been reached
        while (timeWaited <= getMaxWaitTime())
        {
            LOGGER.finer("waiting for connection...");

            try
            {
                Thread.sleep(getWaitTime());
            }
            catch (InterruptedException ex)
            {
                throw new DataSourceException(
                    "Interrupted while waiting for an available connection");
            }

            timeWaited += getWaitTime();

            synchronized (mutex)
            {
                if (availableConnections.size() > 0)
                {
                    return getAvailable();
                }
            }
        }

        throw new UnavailableConnectionException(usedConnections.size(),
            getConfig());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private SeConnection getAvailable()
    {
        LOGGER.finest("Getting available connection.");

        SeConnection conn = (SeConnection) availableConnections.removeFirst();
        usedConnections.add(conn);
        LOGGER.finer(conn + " now in use");

        return conn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeConnection newPooledConnection() throws DataSourceException
    {
        int existents = availableConnections.size() + usedConnections.size();

        //one never knows...
        if (existents >= maxConnections)
        {
            throw new DataSourceException(
                "Maximun number of connections reached");
        }

        SeConnection connection = null;

        try
        {
            connection = newConnection();

            usedConnections.add(connection);
        }
        catch (SeException ex)
        {
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
    private SeConnection newConnection() throws SeException
    {
        SeConnection seConn = new SeConnection(config.getServerName(),
                config.getPortNumber().intValue(), config.getDatabaseName(),
                config.getUserName(), config.getUserPassword());

        LOGGER.fine("***************\ncreated new connection " + seConn
            + "\n*****************");

        SeConnection.SeStreamSpec stSpec = seConn.getStreamSpec();

        /*
           System.out.println("getMinBufSize=" + stSpec.getMinBufSize());
           System.out.println("getMaxBufSize=" + stSpec.getMaxBufSize());
           System.out.println("getMaxArraySize=" + stSpec.getMaxArraySize());
           System.out.println("getMinObjects=" + stSpec.getMinObjects());
           System.out.println("getAttributeArraySize=" + stSpec.getAttributeArraySize());
           System.out.println("getShapePointArraySize=" + stSpec.getShapePointArraySize());
           System.out.println("getStreamPoolSize=" + stSpec.getStreamPoolSize());
         */
        stSpec.setMinBufSize(1024 * 1024);

        stSpec.setMaxBufSize(10 * 1024 * 1024);

        stSpec.setMaxArraySize(10000);

        stSpec.setMinObjects(1024);

        stSpec.setAttributeArraySize(1024 * 1024);

        stSpec.setShapePointArraySize(1024 * 1024);

        stSpec.setStreamPoolSize(10);

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
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SeLayer getSdeLayer(String typeName) throws DataSourceException
    {
        Vector layers = getAvailableSdeLayers();
        SeLayer layer = null;

        try
        {
            for (Iterator it = layers.iterator(); it.hasNext();)
            {
                layer = (SeLayer) it.next();

                if (layer.getQualifiedName().equals(typeName)
                        || layer.getName().equals(typeName))
                {
                    break;
                }

                layer = null;
            }
        }
        catch (SeException ex)
        {
            throw new DataSourceException(ex.getMessage(), ex);
        }

        return layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    SeTable getSdeTable(String tableName) throws DataSourceException
    {
        SeTable table = null;
        SeConnection sdeConn = null;

        try
        {
            sdeConn = getConnection();
            table = new SeTable(sdeConn, tableName);
        }
        catch (SeException ex)
        {
            throw new DataSourceException(ex.getMessage(), ex);
        }
        finally
        {
            release(sdeConn);
        }

        return table;
    }

    /**
     * gets the list of available SeLayers on the database
     *
     * @return a <code>Vector&lt;SeLayer</code> with the registered
     *         featureclasses on the ArcSDE database
     *
     * @throws DataSourceException
     */
    public Vector getAvailableSdeLayers() throws DataSourceException
    {
        if (databaseLayers == null)
        {
            SeConnection sdeConn = getConnection();

            try
            {
                databaseLayers = sdeConn.getLayers();
            }
            catch (SeException ex)
            {
                throw new DataSourceException("Error getting table list:"
                    + ex.getMessage(), ex);
            }
            finally
            {
                release(sdeConn);
            }
        }

        return databaseLayers;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SdeConnectionConfig getConfig()
    {
        return config;
    }


    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getIncrementStep()
    {
        return incrementStep;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMaxConnections()
    {
        return maxConnections;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getMaxWaitTime()
    {
        return maxWaitTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMinConnections()
    {
        return minConnections;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getWaitTime()
    {
        return waitTime;
    }

    //
}


///:/
