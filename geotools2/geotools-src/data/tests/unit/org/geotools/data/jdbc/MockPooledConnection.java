/*
 * Created on 20/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

/**
 * @author geoghegs
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MockPooledConnection implements PooledConnection {
    private Connection conn;

    /**
     * 
     */
    public MockPooledConnection(Connection conn) {
        this.conn = conn;
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return conn;
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#close()
     */
    public void close() throws SQLException {
        conn.close();
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#addConnectionEventListener(javax.sql.ConnectionEventListener)
     */
    public void addConnectionEventListener(ConnectionEventListener listener) {
        
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#removeConnectionEventListener(javax.sql.ConnectionEventListener)
     */
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        
    }

}
