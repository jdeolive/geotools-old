/* $Id: JDBCUtils.java,v 1.1 2003/11/28 08:49:51 seangeo Exp $
 * 
 * Created on 28/11/2003
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.Transaction;

/**
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: JDBCUtils.java,v 1.1 2003/11/28 08:49:51 seangeo Exp $
 * Last Modified: $Date: 2003/11/28 08:49:51 $ 
 */
public class JDBCUtils {

    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");
    
    /** Non Constructable.
     * 
     */
    private JDBCUtils() {
        super();
    }

    /**
     * A utility method for closing a Statement. Wraps and logs any exceptions
     * thrown by the close method.
     *
     * @param statement The statement to close. This can be null since it makes
     *        it easy to close statements in a finally block.
     */
    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                String msg = "Error closing JDBC Statement";
                LOGGER.log(Level.WARNING, msg, e);
            }
        }
    }

    /**
     * A utility method for closing a ResultSet. Wraps and logs any exceptions
     * thrown by the close method.
     *
     * @param rs The ResultSet to close. This can be null since it makes it
     *        easy to close result sets in a finally block.
     */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                String msg = "Error closing JDBC ResultSet";
                LOGGER.log(Level.WARNING, msg, e);
            } catch (Exception e) { // oracle drivers are crapping out
    
                String msg = "Error closing JDBC ResultSet";
    
                //LOGGER.log(Level.WARNING, msg, e);
            }
        }
    }

    /**
     * A utility method for closing a Connection. Wraps and logs any exceptions
     * thrown by the close method.
     * 
     * <p>
     * Connections are maintained by a Transaction and we will need to manage
     * them with respect to their Transaction.
     * </p>
     * 
     * <p>
     * Jody here - I am forcing this to be explicit, by requiring you give the
     * Transaction context when you close a connection seems to be the only
     * way to hunt all the cases down. AttributeReaders based on QueryData
     * rely on
     * </p>
     * 
     * <p>
     * I considered accepting an error flag to control Transaction rollback,
     * but I really only want to capture SQLException that force transaction
     * rollback.
     * </p>
     *
     * @param conn The Connection to close. This can be null since it makes it
     *        easy to close connections in a finally block.
     * @param transaction Context for the connection, we will only close the
     *        connection for Transaction.AUTO_COMMIT
     * @param sqlException Error status, <code>null</code> for no error
     */
    public static void close(Connection conn, Transaction transaction,
        SQLException sqlException) {
        if (conn == null) {
            // Assume we have already closed the connection
            // (allows use of method in a finally block)
            return;
        }
    
        if (transaction != Transaction.AUTO_COMMIT) {
            // we should not close Transaction connections
            // they will do this themselves when they are finished
            // with the connection.
            if (sqlException != null) {
                // we are closing due to an SQLException                
                try {
                    transaction.rollback();
                } catch (IOException e) {
                    String msg = "Error rolling back transaction in response"
                        + "to connection error. We are in an inconsistent state";
                    LOGGER.log(Level.SEVERE, msg, e);
    
                    // TODO: this is a bad place to be should we completely gut the transaction 
                    // to prevent damage                                                            
                    // transaction.close();
                }
            }
    
            return;
        }
    
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            String msg = "Error closing JDBC Connection";
            LOGGER.log(Level.WARNING, msg, e);
        }
    }
    
    

}
