package org.geotools.data.jdbc.datasource;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingStatement;

/**
 * Unwraps DBCP managed connections
 * 
 * @author Andrea Aime - TOPP
 * 
 */
public class DBCPUnWrapper implements UnWrapper {

    public boolean canUnwrap(Connection conn) {
        return conn instanceof DelegatingConnection;
    }

    public Connection unwrap(Connection conn) {
        if (!canUnwrap(conn))
            throw new IllegalArgumentException("This unwrapper can only handle instances of "
                    + DelegatingConnection.class);
        Connection unwrapped = ((DelegatingConnection) conn).getInnermostDelegate();
        if (unwrapped == null)
            throw new RuntimeException("Could not unwrap connection. Is the DBCP pool configured "
                    + "to allow access to underlying connections?");
        return unwrapped;
    }

    public boolean canUnwrap(Statement st) {
        return st instanceof DelegatingStatement;
    }

    public Statement unwrap(Statement statement) {
        if(!canUnwrap(statement))
            throw new IllegalArgumentException("This unwrapper can only handle instances of "
                    + DelegatingStatement.class);
        Statement unwrapped = ((DelegatingStatement) statement).getInnermostDelegate();
        if (unwrapped == null)
            throw new RuntimeException("Could not unwrap connection. Is the DBCP pool configured "
                    + "to allow access to underlying connections?");
        return unwrapped;
    }

}
