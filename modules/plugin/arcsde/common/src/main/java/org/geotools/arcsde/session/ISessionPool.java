package org.geotools.arcsde.session;

import java.io.IOException;

/**
 * Maintains <code>SeConnection</code>'s for a single set of connection properties (for instance: by
 * server, port, user and password) in a pool to recycle used connections.
 * <p>
 * The expected optional parameters that you can set up in the argument Map for createDataStore are:
 * <ul>
 * <li>pool.minConnections Integer, tells the minimum number of open connections the pool will
 * maintain opened</li>
 * <li>pool.maxConnections Integer, tells the maximum number of open connections the pool will
 * create and maintain opened</li>
 * <li>pool.timeOut Integer, tells how many milliseconds a calling thread is guaranteed to wait
 * before getConnection() throws an UnavailableArcSDEConnectionException</li>
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id$
 */
public interface ISessionPool {

    /**
     * returns the number of actual connections held by this connection pool. In other words, the
     * sum of used and available connections, regardless
     * 
     */
    int getPoolSize();

    /**
     * closes all connections in this pool. The first call closes all SeConnections, further calls
     * have no effect.
     */
    void close();

    /**
     * Returns whether this pool is closed
     * 
     * @return
     */
    boolean isClosed();

    /**
     * Returns the number of idle connections
     */
    int getAvailableCount();

    /**
     * Number of active sessions.
     * 
     * @return Number of active session; used to monitor the live pool.
     */
    int getInUseCount();

    /**
     * Grab a session from the pool, this session is the responsibility of the calling code and must
     * be closed after use.
     * 
     * @return A Session, when close() is called it will be recycled into the pool
     * @throws IOException
     *             If we could not get a connection
     * @throws UnavailableArcSDEConnectionException
     *             If we are out of connections
     * @throws IllegalStateException
     *             If pool has been closed.
     */
    ISession getSession() throws IOException, UnavailableArcSDEConnectionException;

    ArcSDEConnectionConfig getConfig();

}