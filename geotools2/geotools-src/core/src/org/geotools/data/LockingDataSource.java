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
package org.geotools.data;

import java.util.Set;

import org.geotools.filter.Filter;
/**
 * Extends geotools2 DataSource to account for Locking support.
 * <p>
 * The goal of this class is to support "strong transactions", that is
 * expose a locking mechanism that can be backed by a Database.</p>
 * <p>
 * The driver for this locking extension is the GeoServer application
 * and the WFS Specification.</p>
 * <p>
 * The WFS Locking Specification Represents a compromize between
 * row locks and row versioning.</p>
 * <ul>
 * <li><b>row-locks:</b><br>
 *     Non persistent, short-term, per row lock that last for the duration of a transaction.
 *     The locking process blocks transactions that.
 *     </li>
 * <li><b>feature-locks:</b>(As per WFS Specification)<br>
 *     Persistent, per feature locks that last for a specified duration.
 *     The locking process does not block transactions. A getLock conflict will
 *     not aquire a lock (it reports the number of locks aquired), a modification
 *     request will simply fail. 
 *     </li>
 * <li><b>row-versioning:</b>(Long Tansaction Support)<br>
 *     Rows are versioned in a manner similar to CVS, branches are managed through
 *     a 'workspace' construct. The blocking process does not block transactions,
 *     conflicts are handled when merging branches back into the main database.
 *     The drawback is an often manual process for resolving merge conflicts.
 *     </li>
 * </ul>
 * <p>
 * This API supports both row-locks and feature-locks.</p>
 * 
 * <p>
 * The Plan for this refactoring is:</p>
 * <ol>
 * <li>Construct this LockingDataSource and AbstractLockingDataSource
 *     </li>
 * <li>Implment PostgisLockingDataSource:<br>
 *     using:
 *     <ul>
 *     <li><code>SELECT FOR UPDATE:</code> for per transaction locking</li>
 *     <li>Use of a 'FeatureLock Table' for long term feature based locking</li>
 *     </ul>
 * <li>Modify GeoServer Locking code to make use of LockingDataSource
 *     locking abilities
 *     </li>
 * <li>Port GeoServer In-Process Locking Code into a AbstractLockingDataSource
 *     with tests against GeoServer codebase to maintain functionality</li>
 * </ol>
 * Example use of per transaction locking:
 * <table border=1, bgcolor="lightgray", width="100%"><tr><td><code><pre>
 * HashMap params = new HashMap();
 * 
 * params.put("dbtype", "postgis");
 * params.put("host","feathers.leeds.ac.uk");
 * params.put("port", "5432");
 * params.put("database","postgis_test");
 * params.put("user","postgis_ro");
 * params.put("passwd","postgis_ro");
 * params.put("table","testset");
 * params.put("locking","true");* 
 * 
 * DataSource ds = DataSourceFinder.getDataSource( params );
 * LockingDataSource lds = null;
 * if ( ds instanceof LockingDataSource ){
 *    lds = (LockingDataSource) ds;
 *    lds.setFeatureLock( null );
 * }
 * ds.setAutoCommit( false ); // start transaction
 * if (lds != null){
 *   lock = lds.lockFeatures( Filter );
 * )
 * ds.removeFeatures( fc );
 * ds.commit();
 * </pre></code></td></tr></table>
 * 
 * Aquire FeatureLock for Long Transaction:
 * <table border=1, bgcolor="lightgray", width="100%"><tr><td><code><pre>
 * public FeatureLock lockSomething( Filter filter){
 *   LockingDataSource tds =
 *       (LockingDataSource) DataSourceFinder.getDataSource( params );
 *  
 *   FeatureLock lock = FeatureLockFactory.generate( 30*60*60 ); // 30 min
 *   lds.setFeatureLock( lock );
 *   lds.lockFeatures( Filter );
 *   lds.commit();
 *   return FeatureLock;
 * }
 * </pre></code></td></tr></table>
 * 
 * Using previously aquired FeatureLock:
 * <table border=1, bgcolor="lightgray", width="100%"><tr><td><code><pre>
 * public FeatureLock deleteSomething( String authId, Filter filter){
 *   LockingDataSource tds =
 *      (LockingDataSource) DataSourceFinder.getDataSource( params );
 *   lds.setAuthorization( new String[]{ lockId,} );
 *   lds.removeFeatures( filter );
 *   lds.commit();
 * }
 * </pre></code></td></tr></table>
 * 
 * A 'generalized' JUnit @link org.geotools.data.LockingDataSourceTest 
 * provides a detailed example of the requirements for this locking
 * process.
 * 
 * @see <a href="http://vwfs.refractions.net/docs/Database_Research.pdf">Database Reseach</a>
 * @see <a href="http://vwfs.refractions.net/docs/Transactional_WFS_Design.pdf">Transactional WFS Design</a>
 * @see <a href="http://vwfs.refractions.net/docs/Design_Implications.pdf">Design Implications</a>
 * @author jgarnett, Refractions Research, Inc.
 * @version CVS Version  
 */
public interface LockingDataSource extends DataSource {
            
    /**
     * All locking operations will operate against the provided <code>lock</code>.
     * <p>
     * This in in keeping with the stateful spirit of DataSource in which operations are against the
     * "current" transaction. If a FeatureLock is not provided lock operations will only
     * be applicable for the current transaction (they will expire on the next commit or rollback).</p>
     * <p>
     * That is lockFeatures() operations will:</p>
     * <ul>
     * <li>Be recorded against the provided FeatureLock.
     * <li>Be recorded against the current transaction if no FeatureLock is provided.
     * </ul>
     * <p>
     * Calling this method with <code>setFeatureLock( null )<code> will revert to per transaction operation.</p>
     * <p>
     * This design allows for the following:</p>
     * <ul>
     * <li>cross DataSource FeatureLock usage</li>
     * <li>not having pass in the same FeatureLock object multiple times</li>
     * </ul>
     */
    void setFeatureLock(FeatureLock lock);

    /**
     * FeatureLock features described by Query.
     * <p>
     * To implement parcial Locking retrieve your features with a query operation
     * first before trying to lock them individually.</p>
     * 
     * @param query Query describing the features to lock
     * @throws DataSourceException Thrown if anything goes wrong
     * @return Number of features locked
     */
    int lockFeatures(Query query) throws DataSourceException;
    
    /**
     * FeatureLock features described by Filter.
     * <p>
     * To implement parcial Locking retrieve your features with a query operation
     * first before trying to lock them individually.</p>
     *  
     * @param filter Filter describing the features to lock
     * @throws DataSourceException Thrown if anything goes wrong
     * @return Number of features locked
     */
    int lockFeatures(Filter filter) throws DataSourceException;
    
    /**
     * FeatureLock all Features.
     * <p>
     * The method does not prevent addFeatures() from being used (we could add
     * a lockDataSource() method if this functionality is required.</p>
     * 
     * @throws DataSourceException
     */
    int lockFeatures() throws DataSourceException;
    
    /**
     * Provides a set of Authoirization IDs for this Transaction.     
     * <p>
     * All proceeding modifyFeatures,removeFeature, unLockFeatures, refreshLock and ReleaseLock
     * operations will make use of the provided authorization.</p>
     * <p>
     * That is operations will only succeed if affected features either:</p>
     * <ul>
     * <li>not locked</li> 
     * <li>locked with one of the provided lockids</li>
     * </ul>
     * <p>
     * Authorization IDs are provided as Strings, rather than FeatureLock objects,
     * to account for across process lock use.</p>
     * 
     * @param locksIds LockIds for Long Transaction operations
     */
    void setAuthorization(Set authIds);
    /**
     * Provides an Authorization ID for this Transaction.     
     * <p>
     * All proceeding modifyFeatures,removeFeature, unLockFeatures, refreshLock and ReleaseLock
     * operations will make use of the provided authorization.</p>
     * <p>
     * That is operations will only succeed if affected features either:</p>
     * <ul>
     * <li>not locked</li> 
     * <li>locked with the provided authID</li>
     * </ul>
     * <p>
     * Authorization ID is provided as a String, rather than a FeatureLock,
     * to account for across process lock use.</p>
     * 
     * @param authID
     */
    void setAuthorization(String authID);
    
    /**
     * Used to complete release a lock.
     * <p>
     * Authorization must be provided prior before calling this method.</p>
     * <p>
     * All featurs locked with the provided authID will
     * be unlocked.</p>
     * <p>
     * @param authID Idetification of Lock to release
     */
    void releaseLock(String authID) throws DataSourceException;
    
    /**
     * Used to refresh a lock.
     * <p>
     * Authorization must be provided prior before calling this method.</p>
     * <p>
     * All features locked with the provied authID will
     * be locked for additional time (the origional duration
     * request).</p>
     * <p>
     * @param authID Idetification of Lock to refresh
     */    
    void refreshLock(String authID) throws DataSourceException;
    
    /**
     * Unlocks all Features.
     * <p>
     * Authorization must be provided prior before calling this method.</p>
     * 
     * <table border=1, bgcolor="lightgray", width="100%"><tr><td><code><pre>
     * <b>void</b> releaseLock( String lockId, LockingDataSource ds ){
     *    ds.setAuthorization( "LOCK534" );
     *    ds.unLockFeatures(); 
     * }
     * </pre></code></td></tr></table>
     * 
     * @throws DataSourceException
     */
    void unLockFeatures() throws DataSourceException;
    
    /**
     * Unlock Features denoted by provided filter.
     * <p>
     * Authorization must be provided prior before calling this method.</p>
     * 
     * @param filter
     * @throws DataSourceException
     */
    void unLockFeatures(Filter filter) throws DataSourceException;
    
    /**
     * Unlock Features denoted by provided query.
     * <p>
     * Authorization must be provided prior before calling this method.</p>
     * 
     * @param query Specifies fatures to unlock
     * @throws DataSourceException
     */
    void unLockFeatures(Query query) throws DataSourceException;    
}
