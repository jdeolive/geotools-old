/* $Id: QueryData.java,v 1.5 2004/01/20 05:17:12 jive Exp $
 * 
 * Created on 28/11/2003
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.JDBCDataStore.FeatureTypeInfo;


/**
 * Provides an encapsulation of the connection, statement and result set of
 * a JDBC query.  This class solves the problem of "Where do we close JDBC
 * resources when they are being used by multiple AttributeReaders?".
 * 
 * <p>
 * An alternative solution would be to have the FeatureReader manage the
 * resources, however this will pose problems when combining JDBC
 * Attribute Readers with other AttributeReaders and FeatureReaders.  The
 * QueryData solution works by holding all the needed resources and
 * providing a close method that closes all the resources.  When the close
 * method is called any readers that are registered as QueryDataListeners
 * for the query data will be notified.  This will allow one
 * AttributeReader to close the resources and any other AttributeReaders
 * using the same resources will find out about it.
 * </p>
 * 
 * <h3>About QueryDataObservers</h3>
 * <p>QueryDataObservers are required for classes wishing to read the data
 * contained within the QueryData object.  If an object needs to read the 
 * contained data it needs to register as an Observer using the attachObserver
 * method.  This allows it to call next on the query data object to advance the
 * row and read to return the current row data.</p>
 * 
 * <p>Observers are required so we can use FORWARD_ONLY result sets and obtain
 * better performance and memory usage.  The Observer allows an object to call
 * next, once between calls to next for each other observer.  It prevents
 * skipping rows from multiple next calls.  QueryData enforces this by ensuring that
 * the observers are always in a consistant state.  When next is called by an
 * observer all other observers must also call next before reads can take place.
 * If an Observer tries to read before all other observers have called next an
 * IllegalStateException will be thrown.  If an Observer tries to call next again,
 * before all other observers have called next and IllegalStateException will be 
 * thrown.</p>
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public final class QueryData {    
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");
    
    private Connection conn;
    private FeatureTypeInfo featureTypeInfo;
    private boolean lastNextValue = false;
    private boolean waitForNextCalls = false;
    private List observers = new ArrayList();
    private ResultSet resultSet;
    private Statement statement;
    private Transaction transaction;
    
    /**
     * The constructor for the QueryData object.
     *
     * @param featureTypeInfo DOCUMENT ME!
     * @param conn The connection to the DB.
     * @param statement The statement used to execute the query.
     * @param resultSet The result set.
     * @param transaction DOCUMENT ME!
     */
    public QueryData(FeatureTypeInfo featureTypeInfo, Connection conn,
        Statement statement, ResultSet resultSet, Transaction transaction) {
        this.featureTypeInfo = featureTypeInfo;
        this.conn = conn;
        this.resultSet = resultSet;
        this.statement = statement;
        this.transaction = transaction;
    }

    /** Attaches a QueryDataObserver to this query data.
     *  This is required if an observer wants to be able to read
     *  and move the result set contained within.
     * 
     * @param o The observer to attach.
     * @throws IllegalStateException If the QueryDataObserver has already
     * been attach.
     */
    public void attachObserver(QueryDataObserver o) {            
        QueryDataObserverHolder holder = new QueryDataObserverHolder(o);
        if (observers.contains(holder)) {
            throw new IllegalStateException("A QueryDataObserver can " +
                    "only observe a QueryData once.");
        }
        observers.add(holder);
    }

    /**
     * A convience method that ensures we handle rollback on error
     * correctly.
     * 
     * <p>
     * Returns an IOException encapsulating the sqlException after
     * correctly rolling back the current Transaction. Rollback only
     * occurs if we are not using Transacstion.AUTO_COMMIT.
     * </p>
     * TODO: chris is this a good idea?
     *
     * @param message DOCUMENT ME!
     * @param sqlException DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public IOException cast(String message, SQLException sqlException) {
        if (transaction != Transaction.AUTO_COMMIT) {
            try {
                transaction.rollback();
            } catch (IOException e) {
                // problem with rollback
            }

            return new DataSourceException(message
                + "(transaction rolled back)", sqlException);
        } else {
            return new DataSourceException(message, sqlException);
        }
    }

    /**
     * Closes all the resources.
     * 
     * <p>
     * All resources are closed the QueryDataListener.queryDataClosed() is
     * called on all QueryDataListeners.
     * </p>
     * 
     * <p>
     * The Connection is handled differently depending on if this is an
     * AUTO_COMMIT Transaction or not.
     * 
     * <ul>
     * <li>
     * AUTO_COMMIT connections are closed
     * </li>
     * <li>
     * Transaction connection are left open if <code>error</code> is false
     * </li>
     * <li>
     * Transaction connectino are left open and the Transaction is rolled
     * back if error is <code>true</code>.
     * </li>
     * </ul>
     * </p>
     * 
     * <p>
     * Jody Here: I have forced this method to handle maintaining
     * conneciton status as it knows about Transactions and
     * AttributeReaders do not.
     * </p>
     * 
     * <p>
     * <b>USEAGE GUIDELINES:</b>
     * </p>
     * 
     * <ul>
     * <li>
     * sqlException != null: When ever we have an SQLException we will need
     * to force the any Transaction associated with this conneciton to
     * rollback.
     * </li>
     * <li>
     * sqlException == null: When we are finished with resources we will
     * call close( null) which will return AUTO_COMMIT connections to the
     * pool and leave Transaction connections open.
     * </li>
     * </ul>
     *
     * @param sqlException DOCUMENT ME!
     */
    public void close(SQLException sqlException, QueryDataObserver obs) {
        if (obs == null) {
            throw new NullPointerException("Cannot pass null Observer to close");
        }
        
        QueryDataObserverHolder holder = getHolder(obs);
        if (holder == null) {
            throw new IllegalArgumentException("Can not close query data unless you are" +
                    " an attached observer.");
        }
        
        observers.remove(holder);
        
        if (observers.isEmpty()) {
            JDBCUtils.close(resultSet);
            JDBCUtils.close(statement);
            JDBCUtils.close(conn, transaction, sqlException);
            resultSet = null;
            statement = null;
            conn = null;
        }
    }
    
    /**
     * 
     */
    public void deleteCurrentRow() throws SQLException {
        this.resultSet.deleteRow();
    }
            
    /**
     *
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public FeatureTypeInfo getFeatureTypeInfo() {
        return featureTypeInfo;
    }
    
    private QueryDataObserverHolder getHolder(QueryDataObserver o) {
        for (Iterator iter = observers.iterator(); iter.hasNext();) {
            QueryDataObserverHolder holder = (QueryDataObserverHolder) iter.next();
            if (holder.observer == o) {
                return holder;
            }
        }
        return null;
    }

    /**
     * Returns transaction, this Query data is opperating against.
     * 
     * <p>
     * Please note that if transacstion is not Transaction.AUTO_COMMIT you
     * will need to call transaction.rollback() in the event of an
     * SQLException.
     * </p>
     *
     * @return The current Transaction
     */
    public Transaction getTransaction() {
        return transaction;
    }
    
    private boolean hasOneObserverCalledNext() {
        for (Iterator iter = observers.iterator(); iter.hasNext();) {
            QueryDataObserverHolder holder = (QueryDataObserverHolder) iter.next();
            if (holder.nextCalled) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean haveAllObserversCalledNext() {
        for (Iterator iter = observers.iterator(); iter.hasNext();) {
            QueryDataObserverHolder holder = (QueryDataObserverHolder) iter.next();
            if (!holder.nextCalled) {
                return false;
            }
        }
        return true;
    }
    
    /** Advances the underlying data to the next row.
     * 
     * @param obs
     * @return
     */
    public void next(QueryDataObserver obs) {
        if (obs == null) {
            throw new NullPointerException("Can not pass null Observer to next()");
        }
        
        QueryDataObserverHolder holder = getHolder(obs);
        if (holder == null) {
            throw new IllegalArgumentException("An Observer cannot call next" +
                    " if it is not attached to the QueryData");
        }
        
        if (!waitForNextCalls) {
            throw new IllegalStateException("Can not call next before calling hasNext()");
        }
        
        if (holder.nextCalled) {
            throw new IllegalStateException("Can not call next again until all other " +
                    "Observers have called next");
        }
        
        holder.nextCalled = true;
        
        if (haveAllObserversCalledNext()) {
            resetObservers();
            waitForNextCalls = false;
        }        
    }

    public boolean hasNext(QueryDataObserver obs) throws SQLException {
        if (obs == null) {
            throw new NullPointerException("Can not pass null Observer to next()");
        }
        
        QueryDataObserverHolder holder = getHolder(obs);
        if (holder == null) {
            throw new IllegalArgumentException("An Observer cannot call hasNext" +
                    " if it is not attached to the QueryData");
        }
                
        // If we are not waiting for nextCalls then advance the
        // row and wait for next calls.
        if (!waitForNextCalls) {
            LOGGER.finest("Advancing to next row");
            lastNextValue = resultSet.next();
            waitForNextCalls = true;
        }
        
        return lastNextValue;
    }
    
    public RowData getRowData(QueryDataObserver obs) {
        if (obs == null) {
            throw new NullPointerException("Can not pass null Observer to read()");
        }
        
        QueryDataObserverHolder holder = getHolder(obs);
        if (holder == null) {
            throw new IllegalArgumentException("An Observer cannot call read" +
                    " if it is not attached to the QueryData");
        }
        
        if (hasOneObserverCalledNext()) {
            throw new IllegalStateException("Can not call read while in row index " +
                    "transition state.");
        }
        
        return new RowData(resultSet);
    }    
    
    /** Removes the QueryDataObserver from the list of Observers.
     * 
     * @param o The observer to remove.
     */
    public void removeObserver(QueryDataObserver o) {
        observers.remove(o);
    }

    /**
     * 
     */
    private void resetObservers() {
        for (Iterator iter = observers.iterator(); iter.hasNext();) {
            QueryDataObserverHolder holder = (QueryDataObserverHolder) iter.next();
            holder.nextCalled = false;
        }        
    }
    
    public void updateRow() throws SQLException {
        resultSet.updateRow();
    }
    
    public void startInsert() throws SQLException {
        resultSet.moveToInsertRow();
    }
    
    public void doInsert() throws SQLException {
        resultSet.insertRow();
    }
    
    public void endInsert() throws SQLException {
        resultSet.moveToCurrentRow();
    }
    
    public static class RowData {
        private ResultSet rs;
        
        public RowData(ResultSet rs) {
            this.rs = rs;
        }
        
        public Object read(int position) throws SQLException {            
            return rs.getObject(position);            
        }
        
        public void write(Object o, int position) throws SQLException {
            if (o == null) {
                rs.updateNull(position);
            } else {
                rs.updateObject(position, o);
            }
        }
        
        public void refreshRow() throws SQLException {
            rs.refreshRow();
        }
    }
}

class QueryDataObserverHolder {
    public boolean nextCalled = false;
    public boolean closeCalled = false;
    public QueryDataObserver observer;
    
    public QueryDataObserverHolder(QueryDataObserver observer) {
        this.observer = observer;
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o instanceof QueryDataObserverHolder) {
            QueryDataObserverHolder other = (QueryDataObserverHolder) o;
            return this.observer == other.observer;
        }
        
        return false;
    }
}