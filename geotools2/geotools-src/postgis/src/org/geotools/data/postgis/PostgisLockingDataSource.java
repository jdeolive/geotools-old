/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.postgis;

//geotools imports
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureLock;
import org.geotools.data.LockingDataSource;
import org.geotools.data.Query;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLUnpacker;
import org.geotools.filter.SQLEncoderException;

//J2SE imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Adds Locking support to Postgis DataSource.
 * <p>
 * The origional class has been modified to capture access to connection
 * </p>
 * <ul>
 * <li></li>
 * </ul>
 * Example:
 * <code><pre>
 * 
 * </pre></code>
 * 
 * @see PostgisDataSource
 * @see http://vwfs.refractions.net/docs/Database_Research.pdf
 * @see http://vwfs.refractions.net/docs/Transactional_WFS_Design.pdf
 * @see http://vwfs.refractions.net/docs/Design_Implications.pdf 
 * @author jgarnett, Refractions Research, Inc.
 */
public class PostgisLockingDataSource extends PostgisDataSource
    implements LockingDataSource {
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.postgislock");
        
    private FeatureLock featureLock;

    /**
     * Sets the table and datasource, rolls a new schema from the db.
     *
     * @param dbConnection The datasource holding the table.
     * @param tableName the name of the table that holds the features.
     * @throws DataSourceException if there were problems constructing the
     *         schema.     
     */
    public PostgisLockingDataSource(ConnectionPool pool, String tableName)
        throws DataSourceException {
        super( pool, tableName );
    }


    /**
     * Lock filtered features.
     * 
     * <p>
     * Requires either a transaction or a FeatureLock
     * </p>
     *
     * @param filter
     *
     * @return number of features locked.
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#lockFeatures(org.geotools.filter.Filter)
     */
    public int lockFeatures(Filter filter) throws DataSourceException {
        return lockFeatures(makeDefaultQuery(filter));
    }

    /**
     * Lock all features.
     *
     * @return Number of features locked.
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#lockFeatures()
     */
    public int lockFeatures() throws DataSourceException {
        return lockFeatures(Query.ALL);
    }

    /**
     * Grabs FeatureLock for subsequence locking operations.
     * @param lock Set to null for per transaction locking.
     * 
     * @see org.geotools.data.LockingDataSource#setCurrentLock(org.geotools.data.FeatureLock)
     */
    public void setFeatureLock(FeatureLock lock) {
        // For now we are just doing locks that
        // last for the duration of the transaction
        //
        if( lock == null ){
            // use Lock for duration of the Transaction
            //            
            featureLock = null;            
        }
        if( lock != null ){
            // currently we do not support long term locks
            // we wil add this later
            //                    
            throw new UnsupportedOperationException(
                "Long term locks not yet supported"
            );
        }
    }

    /**
     * Lock features specifed by the query.
     * <p>
     * This supports lock of:</p>
     * <ul>
     * <li>
     *     </li>
     * </ul>
     * 
     * @param query
     * @return
     * @throws DataSourceException
     * 
     * @see org.geotools.data.LockingDataSource#lockFeatures(org.geotools.data.Query)
     */
    public int lockFeatures(Query query) throws DataSourceException {
        if( featureLock != null ){
            // lock features in the database
            return lockFeaturesDatabase( query, featureLock );
        }
        else {
            // lock features for the transaction
            return lockFeaturesTransaction( query );            
        }
    }
    //
    // Lock Features Implementation
    //
    /**
     * Locks features for the for the duration of the Transaction.
     * <p>
     * SQL:</p>
     * <code>SELECT <i>query</i> FOR UPDATE</code>
     * <p>
     * This code will not return an error if things do not work - it will just
     * return the number of features successfully locked.</p>
     * 
     * @param  query Specify features to be locked
     * @return Number of features locked by the operation
     */
    protected int lockFeaturesTransaction( Query query ) throws DataSourceException{
        if( getAutoCommit() ) return 0; // No transaction - no transaction lock
        
        // this opperation is EVIL and will block
        // (but but should only block for the duration of the Transaction
        //  that currently holds the lock we want)
        // 
        // So when we return we return with everything!
        if( isAllSQL( query )){
            return lockFeaturesTransactionSQL( query );                    
        }
        Set fids;
        try
        {            
            fids = getFidSet(getTransactionConnection(), query);
            return lockFeaturesTransaction( fids );            
        }
        catch (SQLException e)
        {
            throw new DataSourceException( "Could not lock rows", e );
        }                                
    }
    /**
     * Executes query to lock feature Transactions with the understanding
     * that the provided query is completely describale as SQL.
     * <p>
     * You can use isAllSQL( query ) to ensure that this is so.</p>
     * 
     * @param query SQL query
     * @return
     */
    protected int lockFeaturesTransactionSQL( Query query )
        throws DataSourceException {
        SQLUnpacker unpacker = new SQLUnpacker( encoder.getCapabilities() );
        unpacker.unPackAND( query.getFilter() );
        
        // you did check this first - right? (of course you did)
        //
        assert( unpacker.getUnSupported() == null );
        
        Filter filter = unpacker.getSupported();
        assert( filter != null );
        
        LOGGER.fine("Filter in making sql is " + filter );

        
        StringBuffer sql = new StringBuffer();
        
        sql.append( "SELECT ");
        sql.append( tableName );
        sql.append( " " );        
        if (filter != null) {
            try {
                sql.append( encoder.encode( filter ) );
            } catch (SQLEncoderException sqle) {
            //    String message = "Encoder error" + sqle.getMessage();
            //    LOGGER.warning(message);
            //    throw new DataSourceException(message, sqle);
            }
        }
        sql.append(" FOR UPDATE");
        try {
            Connection conn = getTransactionConnection();
            Statement statement = conn.createStatement();
                
            return statement.executeUpdate( sql.toString() );
        }
        catch( SQLException erp ){
            throw new DataSourceException("Problem locking rows", erp );
        }
    }
    /**
     * Constructs a query to lock the provided set of fids.
     * 
     * @param fids
     * @return
     */
    protected int lockFeaturesTransaction( Set fids ) throws DataSourceException {
        try {        
            StringBuffer sql = new StringBuffer();                
            Connection conn = getTransactionConnection();
            
            sql.append( "SELECT ");
            sql.append( tableName );
            sql.append( " WHERE " );
            String fidColumn = getFidColumn( conn, tableName );
            String fid;
                    
            for( Iterator i=fids.iterator(); i.hasNext();){
                fid = (String) i.next();
                
                sql.append( fidColumn );
                sql.append( "=" );
                sql.append( deFid( fid ) ); // this may break if FID is a string :-(
                if( i.hasNext() ){
                    sql.append( " OR " );                
                }            
            }
            sql.append(" FOR UPDATE");
            
            Statement statement = conn.createStatement();
                    
            return statement.executeUpdate( sql.toString() );                 
        }
        catch (SQLException e)
        {
            throw new DataSourceException( "Unable to lock features", e );
        }

    }
    
    /**
     * Tests to see of Query is enterly represented in sql.
     * <p>
     * This Postgis SQLUnpacker will be used to determine if the
     * provided Query can be implemented using just SQL statements.</p>
     * 
     * @param <code>query</code> being examined
     * @return <code>true</code> if unpacker.getUnsupported() is <code>null</code> 
     */
    protected boolean isAllSQL( Query query ){
        SQLUnpacker unpacker = new SQLUnpacker( encoder.getCapabilities() );
        unpacker.unPackAND( query.getFilter() ); 
        
        return unpacker.getUnSupported() == null;               
    }
    /**
     * Retrieve list of FID that satisfy the query.
     * <p>
     * This is used to generate a set of FIDs used for locking
     * operations.</p>
     * 
     * @param query
     * @return Set of FIDs
     */
    protected Set getFidSet( Connection conn, final Query query )        
        throws DataSourceException {
        Set fids = new HashSet();
        
        for( FeatureIterator f=getFeatures( query ).features(); f.hasNext();){
            fids.add( f.next().getID() );          
        }
        return fids;
    }         
    /**
     * Lock features specified by <code>query</code> using <code>lock</code>.
     * <p>
     * This operations has not yet been implemented and allways returns 0.
     * </p>
     * @param query Specify features to be locked
     * @param lock FeatureLock describing id and duration requested
     * @return Number of features locked by the operation
     */
    protected int lockFeaturesDatabase( Query query, FeatureLock lock ){
        // long term locks not supported yet
        return 0;
    }
    /** Inverse of createFid */
    protected String deFid( String featureId ) {
        String newFid;

        if( featureId.startsWith( tableName + "." ) ){
            return featureId.substring( tableName.length() );    
        } else {
            return featureId;
        }
    }
    /**
     * Provide authorization to current transaction.
     * <p>
     * The authorization only lasts until the transaction is complete.</p>
     * 
     * @param authID Authorization for current transaction
     * 
     * @see org.geotools.data.LockingDataSource#setAuthorization(java.util.Set)
     */
    public void setAuthorization(Set authIds) throws DataSourceException {
        if( getAutoCommit() ) {
            throw new DataSourceException( "Cannot accept authorization in auto commit mode");
        }
        String authId = null;        
        try
        {
            Connection conn = getTransactionConnection();
            
            String sql = "haveAuthorization( ? )";
            PreparedStatement statement = conn.prepareStatement( sql );
                
            for( Iterator i=authIds.iterator(); i.hasNext(); ){
                authId = (String) i.next();
                statement.setString( 0, authId );
                statement.execute();
            }                
        }
        catch (SQLException e)
        {
            throw new DataSourceException("Unable to use authorization "+authId, e );
        }        
    }

    /**
     * Provide authorization to current transaction.
     * <p>
     * The authorization only lasts until the transaction is complete.</p>
     * 
     * @param authID Authorization for current transaction
     * 
     * @see org.geotools.data.LockingDataSource#setAuthorization(java.lang.String)
     */
    public void setAuthorization(String authID) throws DataSourceException {
        setAuthorization( Collections.singleton( authID ) );
    }

    /**
     * One line description of action.
     * <p>
     * Detailed Description of releaseLock.
     * </p>
     * @param authID
     * @throws DataSourceException
     * 
     * @see org.geotools.data.LockingDataSource#releaseLock(java.lang.String)
     */
    public void releaseLock(String authID) throws DataSourceException {
        // long term locks are not supported yet
        throw new UnsupportedOperationException(
            "Relesae Lock not supported"
        );
    }

    /**
     * One line description of action.
     * <p>
     * Detailed Description of refreshLock.
     * </p>
     * @param authID
     * @throws DataSourceException
     * 
     * @see org.geotools.data.LockingDataSource#refreshLock(java.lang.String)
     */
    public void refreshLock(String authID) throws DataSourceException {
        // long term locks are not supported yet
        throw new UnsupportedOperationException(
            "Refresh Lock not supported"
        );
    }

    /**
     * One line description of action.
     * <p>
     * Detailed Description of unLockFeatures.
     * </p>
     * @throws DataSourceException
     * 
     * @see org.geotools.data.LockingDataSource#unLockFeatures()
     */
    public void unLockFeatures() throws DataSourceException {
        unLockFeatures( Query.ALL );        
    }

    /**
     * One line description of action.
     * <p>
     * Detailed Description of unLockFeatures.
     * </p>
     * @param filter
     * @throws DataSourceException
     * 
     * @see org.geotools.data.LockingDataSource#unLockFeatures(org.geotools.filter.Filter)
     */
    public void unLockFeatures(Filter filter) throws DataSourceException {
        unLockFeatures(makeDefaultQuery(filter));        
    }

    /**
     * One line description of action.
     * <p>
     * Detailed Description of unLockFeatures.
     * </p>
     * @param query
     * @throws DataSourceException
     * 
     * @see org.geotools.data.LockingDataSource#unLockFeatures(org.geotools.data.Query)
     */
    public void unLockFeatures(Query query) throws DataSourceException {
        // long term locks are not supported yet
        throw new UnsupportedOperationException(
            "Unlock Features not supported"
        );
    }    
}
