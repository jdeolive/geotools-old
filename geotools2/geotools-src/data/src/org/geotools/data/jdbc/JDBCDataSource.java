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
package org.geotools.data.jdbc;

import java.util.Set;

import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.DataSourceException;
import java.sql.SQLException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Envelope;
import java.sql.Connection;
/**
 * This class provides a skeletal implementation of the DataSource interface to
 * minimize the effort of required to implement this interface.
 * 
 * <p>
 * </p>
 * 
 * <p>
 * </p>
 * 
 * @author Chris Holmes, TOPP
 * @author Sean Geoghegan
 * @version $Id: JDBCDataSource.java,v 1.2 2003/11/03 23:08:24 cholmesny Exp $
 */
public abstract class JDBCDataSource extends AbstractDataSource {

      /** The logger for the data package. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data");

    private ConnectionPool connectionPool;

    /** A postgis connection. */
    protected Connection transConn;

    public JDBCDataSource(ConnectionPool pool){
	this.connectionPool = pool;
    }

/**
     * Makes all transactions made since the previous commit/rollback
     * permanent.  This method should be used only when auto-commit mode has
     * been disabled.   If autoCommit is true then this method does nothing.
     *
     * @throws DataSourceException if there are any datasource errors.
     *
     * @see #setAutoCommit(boolean)
     * @task  REVISIT: to abstract class, same as oracle.
     */
    public void commit() throws DataSourceException {
        try {
	    LOGGER.fine("commit called");
	    getTransactionConnection().commit();
            closeTransactionConnection();
        } catch (SQLException sqle) {
            String message = "problem committing";
            LOGGER.info(message + ": " + sqle.getMessage());
            throw new DataSourceException(message, sqle);
        }
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that clients
     * recieve the proper information about the datasource's capabilities.
     *
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
        MetaDataSupport jdbcMeta = new MetaDataSupport();
        jdbcMeta.setSupportsRollbacks(true);
        return jdbcMeta;
    }
    
      /**
     * Performs the setFeautres operation by removing all and then adding the
     * full collection.  This is not efficient, the add, modify and  remove
     * operations should be used instead, this is just to follow the
     * interface.  Extensions of this class should set supportsSetFeatures
     * to true if remove and add are supported, and this class will do
     * the work for them.
     *
     * @param features the features to set for this table.
     *
     * @throws DataSourceException if there are problems removing or adding.
     * @task REVISIT: 
     */
    public void setFeatures(FeatureCollection features)
        throws DataSourceException {
	if (!getMetaData().supportsSetFeatures()) {
            throw new UnsupportedOperationException("Does not support setFeatures");
        }
	boolean originalAutoCommit = getAutoCommit();
        setAutoCommit(false);
        removeFeatures(null);
        addFeatures(features);
        commit();
        setAutoCommit(originalAutoCommit);
    }

    /**
     * Undoes all transactions made since the last commit or rollback. This
     * method should be used only when auto-commit mode has been disabled.
     * This method should only be implemented if
     * <tt>setAutoCommit(boolean)</tt>  is also implemented.
     *
     * @throws DataSourceException if there are problems with the datasource.
     *
     * @see #setAutoCommit(boolean)
     * @task REVISIT: to abstract class, same as oracle.
     */
    public void rollback() throws DataSourceException {
        try {
	    getTransactionConnection().rollback();
            closeTransactionConnection();
        } catch (SQLException sqle) {
            String message = "problem with rollbacks";
            LOGGER.info(message + ": " + sqle.getMessage());
            throw new DataSourceException(message, sqle);
        }
    }

    /**
     * Retrieves the current autoCommit mode for the current DataSource.  If
     * the datasource does not implement setAutoCommit, then this method
     * should always return true.
     *
     * @return the current state of this datasource's autoCommit mode.
     *
     * @throws DataSourceException if a datasource access error occurs.
     *
     * @see #setAutoCommit(boolean)
     * @task REVISIT: to abstract class, same as oracle.
     */
    public boolean getAutoCommit() throws DataSourceException {
        try {
	    if (transConn == null) {
		return true;
	    } else {
		return getTransactionConnection().getAutoCommit();
	    }
        } catch (SQLException sqle) {
            String message = "problem setting auto commit";
            LOGGER.info(message + ": " + sqle.getMessage());
            throw new DataSourceException(message, sqle);
        }
    }

    //all the following methods should go in abstract jdbc datasource.
    //Could the oracle methods just call super andcast the connection 
    //returned to OracleConnections?  

    /**
     * Gets a connection.
     * 
     * <p>
     * The connection returned by this method is suitable for a single use.
     * Once a method has finish with the connection it should call the
     * connections close method.
     * </p>
     * 
     * <p>
     * Methods wishing to use a connection for transactions or methods who use
     * of the connection involves commits or rollbacks should use
     * getTransactionConnection instead of this method.
     * </p>
     *
     * @return A single use connection.
     *
     * @throws DataSourceException If the connection is not an
     *         OracleConnection.
     * @throws SQLException If there is a problem with the connection.
     */
    protected Connection getConnection() throws DataSourceException {
	try {
	    return connectionPool.getConnection();
	} catch (SQLException sqle) {
	    throw new DataSourceException("could not get connection", sqle);
	}
    }

       /**
     * This is called my any transaction method in its finally block. If the
     * transaction failed it is rolled back, if it succeeded and we are
     * previously set to autocommit, it is committed and if  it succeed and we
     * are set to manual commit, no action is taken.
     * 
     * <p>
     * In all cases the autocommit status of the data source is set to
     * previousAutoCommit and the closeTransactionConnection is called.
     * </p>
     *
     * @param previousAutoCommit The status of autoCommit prior to the
     *        beginning of a transaction method.  This tells us whether we
     *        should commit or wiat for the user to perform the commit.
     * @param fail The fail status of the transaction.  If true, the
     *        transaction is rolled back.
     *
     * @throws DataSourceException If errors occur performing any of the
     *         actions.
     */
    protected void finalizeTransactionMethod(boolean previousAutoCommit,
        boolean fail) throws DataSourceException {

	LOGGER.finer("finalizing transaction, prevac: " + previousAutoCommit +
		    ", fail is " + fail);
        if (fail) {
            rollback();
        } else {
            // only commit if this transaction was atomic
            // ie if the user had previously set autoCommit to false
            // we leave commiting up to them.
            if (previousAutoCommit) {
		LOGGER.finer("committing in finalize");
                commit();
            }
        }
        setAutoCommit(previousAutoCommit);
        closeTransactionConnection();
    }



    /**
     * This method should be called when a connection is required for
     * transactions. After completion of the use of the connection the caller
     * should call  closeTransactionConnection which will either close the
     * conn if we are in auto  commit, or maintain the connection if we are in
     * manual commit.  Successive calls to this method after setting
     * autoCommit to false will return the same connection object.
     *
     * @return A connection object suitable for multiple transactional calls.
     *
     * @throws DataSourceException IF an error occurs getting the connection.
     * @throws SQLException If there is something wrong with the connection.
     */
    protected Connection getTransactionConnection()
        throws DataSourceException, SQLException {
        if (transConn == null) {
            transConn = getConnection();
        }

        return transConn;
    }

    /**
     * This method should be called when a connection retrieved using
     * getTransactionConnection is to be closed.
     * 
     * <p>
     * This method only closes the connection if it is set to auto commit.
     * Otherwise the connection is kept open and held in the
     * transactionConnection instance variable.
     * </p>
     */
    protected void closeTransactionConnection() {
        try {
            // we only close if the transaction is set to auto commit
            // otherwise we wait until auto commit is turned off before closing.
	    if ((transConn != null)
                    && transConn.getAutoCommit()) {
                LOGGER.finer("Closing Transaction Connection");
                transConn.close();
                transConn = null;
            } else {
                LOGGER.finer(
                    "Transaction connection not open or set to manual commit");
            }
        } catch (SQLException e) {
            LOGGER.warning("Error closing transaction connection: " + e);
        }
    }
}
