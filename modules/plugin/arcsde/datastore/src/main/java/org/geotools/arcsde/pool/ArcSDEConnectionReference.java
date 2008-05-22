package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;

/**
 * This ArcSDEConnectionPool makes a maximum of *one* Connection available to the calling
 * application.
 * <p>
 * Why? ArcSDEConnections are both expensive to set up and expensive in cache. We still use an
 * ObjectCache internally in order to control time out behavior.
 * <p>
 * The trick is this time that Transaction.AUTO_COMMIT is only treated as suggestion for read only
 * code. The system will allow one transaction to be underway at any point, and will hand out that
 * connection to code that knows how to ask.
 * <p>
 * This is an aggressive experiment designed to cut down the number of connections needed.
 * <p>
 * 
 * @author Jody Garnett
 * @since 2.5
 */
public class ArcSDEConnectionReference extends ArcSDEConnectionPool {
    /** package's logger */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /**
     * Our "cached" session used to issue read only commands.
     */
    ISession cached;

    /**
     * Current Transaction used to track what our single connection is up to.
     */
    Transaction transaction = Transaction.AUTO_COMMIT;

    protected ArcSDEConnectionReference(ArcSDEConnectionConfig config) throws DataSourceException {
        super(config);
        if (config.maxConnections.intValue() != 1) {
            throw new IllegalArgumentException(
                    "ConnectionReference is only allowed to manage one connection.");
        }
    }

    @Override
    public <T> T issueReadOnly(Command<T> command) throws IOException {
        if (cached != null && !cached.isDisposed() && !cached.isClosed()) {
            return cached.issue(command);
        } else {
            return super.issueReadOnly(command);
        }
    }

    @Override
    public ISession getSession() throws DataSourceException, UnavailableArcSDEConnectionException {
        this.cached = super.getSession(); // this will block if session is already in use
        return cached;
    }
}