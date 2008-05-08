package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;

import com.esri.sde.sdk.client.SeException;

/**
 * This ArcSDEConnectionPool makes a maximum of *one* Connection available to
 * the calling application.
 * <p>
 * Why? ArcSDEConnections are both expensive to set up and expensive in cache. We still
 * use an ObjectCache internally in order to control time out behavior.
 * <p>
 * The trick is this time that Transaction.AUTO_COMMIT is only treated as suggestion
 * for read only code. The system will allow one transaction to be underway at any
 * point, and will hand out that connection to code that knows how to ask.
 * <p>
 * This is an aggressive experiment designed to cut down the number of connections
 * needed.
 * <p>
 * @author Jody Garnett
 * @since 2.5
 */
public class ArcSDEConnectionReference extends ArcSDEConnectionPool {
    /** package's logger */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /**
     * Current Transaction used to track what our single connection is up to.
     */
    Transaction transaction = Transaction.AUTO_COMMIT;
    
    protected ArcSDEConnectionReference( ArcSDEConnectionConfig config ) throws DataSourceException {
        super(config);
        if( config.maxConnections.intValue() != 1 ){
            throw new IllegalArgumentException("ConnectionReference is only allowed to manage one connection." );
        }
    }

    /**
     * Code that just blindly asks for a connection will be stuck
     * in read-only mode (that said they will not block!).
     */
    public Session getConnection() throws DataSourceException,
            UnavailableArcSDEConnectionException {
        return getConnection( Transaction.AUTO_COMMIT );
    }
    
    /**
     * Grab a connection from the pool.
     * 
     * @return ArcSDEPooledConnection so that close() will return it to the pool
     * @throws DataSourceException If we could not get a connection
     * @throws UnavailableArcSDEConnectionException If we are out of connections
     * @throws IllegalStateException If pool has been closed.
     */
    public Session getConnection( Transaction transaction ) throws DataSourceException,
            UnavailableArcSDEConnectionException {
        
        if (pool == null) {
            throw new IllegalStateException("The ConnectionPool has been closed.");
        }

        try {
            // String caller = null;
            // if (LOGGER.isLoggable(Level.FINER)) {
            // StackTraceElement[] stackTrace =
            // Thread.currentThread().getStackTrace();
            // caller = stackTrace[3].getClassName() + "." +
            // stackTrace[3].getMethodName();
            // }

            Session session = (Session) this.pool.borrowObject();

            if (LOGGER.isLoggable(Level.FINER)) {
                // System.err.println("-> " + caller + " got " + connection);
                LOGGER.finer(session + " out of connection pool");
            }

            session.markActive();
            // TODO: Push the transaction into the connection to make the connection modal
            return session;
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.WARNING, "Out of connections: " + e.getMessage(), e);
            throw new UnavailableArcSDEConnectionException(this.pool.getNumActive(), this.config);
        } catch (SeException se) {
            LOGGER.log(Level.WARNING, "ArcSDE error getting connection: "
                    + se.getSeError().getErrDesc(), se);
            throw new DataSourceException("ArcSDE Error Message: " + se.getSeError().getErrDesc(),
                    se);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unknown problem getting connection: " + e.getMessage(), e);
            throw new DataSourceException(
                    "Unknown problem fetching connection from connection pool", e);
        }
    }
    
    /**
     * SeConnectionFactory used to create ArcSDEPooledConnection instances
     * for the pool.
     * <p>
     * Subclass may overide to customize this behaviour.
     * </p>
     * @return SeConnectionFactory.
     */
    protected SeConnectionFactory createConnectionFactory() {
        return new SingleSeConnectionFactory(this.config);
    }
    
    /**
     * SeConnectionFactory customized to return specific
     * ArcSDEPooledConnection instances based on transaction.
     * <p>
     * With a transaction:
     * <ul>
     * <li>AUTO_COMMIT: the connection is read only!
     * <li>instance: only a single instance is allowed out
     * </ul>
     * @author Jody
     *
     */
    protected class SingleSeConnectionFactory extends SeConnectionFactory {

        public SingleSeConnectionFactory( ArcSDEConnectionConfig config ) {
            super(config);
        }

        /**
         * Called whenever a new instance is needed.
         * 
         * @return a newly created <code>SeConnection</code>
         * 
         * @throws SeException
         *             if the connection can't be created
         */
        @Override
        public Object makeObject() throws IOException {
            NegativeArraySizeException cause = null;
            for (int i = 0; i < 3; i++) {
                try {
                    Session session = new Session(
                            ArcSDEConnectionReference.this.pool, config);
                    return session;
                } catch (NegativeArraySizeException nase) {
                    LOGGER.warning("Strange failed ArcSDE connection error.  Trying again (try "
                            + (i + 1) + " of 3)");
                    cause = nase;
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
            }
            throw new DataSourceException(
                    "Couldn't create ArcSDEPooledConnection because of strange SDE internal exception.  Tried 3 times, giving up.",
                    cause);
        }
    }
}