package org.geotools.data.jdbc.datasource;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Generic {@link Connection} unwrapper. A user can test if the unwrapper is able to unwrap a
 * certain connection, on positive answer he can call {@link #unwrap(Connection)} to get the native
 * connection
 * 
 * @author Andrea Aime - TOPP
 * 
 */
public interface UnWrapper {
    /**
     * Returns true if this unwrapper knows how to unwrap the specified connection
     * 
     * @param conn
     * @return
     */
    boolean canUnwrap(Connection conn);
    
    /**
     * Returns tru if this unwrapper knows how to unwrap the specified statement
     * @param st
     * @return
     */
    boolean canUnwrap(Statement st);

    /**
     * Returns the unwrapped connection, of throws {@link IllegalArgumentException} if the passed
     * {@link Connection} is not supported ({@link #canUnwrap(Connection)} returns false}.
     * 
     * @param conn
     * @return
     */
    Connection unwrap(Connection conn);
    
    /**
     * Returns the unwrapped statement, of throws {@link IllegalArgumentException} if the passed
     * {@link java.sql.Statement} is not supported ({@link #canUnwrap(Statement)} returns false}.
     * @param statement
     * @return
     */
    Statement unwrap(Statement statement);
}
