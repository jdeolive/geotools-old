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
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.LockingManager;
import org.geotools.data.FeatureLockException;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;

/**
 * A Starting point for your own FeatureLocking implementations.
 * <p>
 * This class extends JDBCFeatureSource and depends on
 * getDataStore().
 * </p>
 * The implementation of the following functions depends on 
 * getDataStore().getLockingManger() not being <code>null</code>:
 * <ul>
 * <li>
 * lockFeatures( Query )
 * </li>
 * <li>
 * unLockFeatures( Query )
 * </li>
 * <li>
 * releaseLock( AuthorizationID )
 * </li>
 * <li>
 * refreshLock( AuthorizationID )
 * </li>
 * </ul> 
 * <p>
 * JDBCFeatureLocking that have provided their own locking to will need to
 * override the above methods, or provide a custom LockingManger.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class JDBCFeatureLocking extends JDBCFeatureStore implements FeatureLocking {
    FeatureLock featureLock = FeatureLock.TRANSACTION;
    
    public JDBCFeatureLocking(JDBCDataStore jdbcDataStore, FeatureType featureType){
        super( jdbcDataStore, featureType );        
    }
    /**
     * Provide a FeatureLock for locking opperations to opperate against.
     * <p>
     * Initial Transactional duration locks can be restored with
     * setFeatureLock( FetaureLock.TRANSACTION ) 
     * </p>
     * @see org.geotools.data.FeatureLocking#setFeatureLock(org.geotools.data.FeatureLock)
     * @param lock FeatureLock (or FeatureLock.TRANSACTION );
     */
    public void setFeatureLock(FeatureLock lock) {
        if( lock == null ){
            throw new NullPointerException( "A FeatureLock is required - did you mean FeatureLock.TRANSACTION?");
        }
        featureLock = lock;
    }
    /**
     * Lock all Features
     * @see org.geotools.data.FeatureLocking#lockFeatures()
     * @return Number of Locked features
     * @throws DataSourceException
     */
    public int lockFeatures() throws IOException {
        return lockFeatures( Filter.NONE );
    }
    /**
     * Lock features matching <code>filter</code>.
     * 
     * @see org.geotools.data.FeatureLocking#lockFeatures(org.geotools.filter.Filter)
     * @param filter
     * @return Number of locked Features
     * @throws DataSourceException
     */
    public int lockFeatures(Filter filter) throws IOException {
        return lockFeatures( new DefaultQuery( filter ) );    
    }
    /**
     * Lock features matching Query.
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.     
     * </p>
     * 
     * @see org.geotools.data.FeatureLocking#lockFeatures(org.geotools.data.Query)
     * @param query
     * @return Number of locked Features
     * @throws DataSourceException If we have any lock conflicts
     */
    public int lockFeatures(Query query) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();        
        if( lockingManager == null){
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation"
            );
        }
        // Could we reduce the Query to only return the FetureID here?
        //
        FeatureReader reader = getFeatures( query ).reader();
        String typeName = reader.getFeatureType().getTypeName();
        Feature feature;
        int count=0;
        try {
            while( reader.hasNext() ){
                try {
                    feature = reader.next();
                    lockingManager.lockFeatureID( typeName, feature.getID(), getTransaction(), featureLock );
                    count++;
                }
                catch( FeatureLockException locked){
                    // could not aquire - don't increment count                
                } catch (NoSuchElementException nosuch) {
                    throw new DataSourceException( "Problem with "+query.getHandle()+" while locking", nosuch  );  
                } catch(IllegalAttributeException e){
                    throw new DataSourceException( "Problem with "+query.getHandle()+" while locking", e );
                }                                
            }
        }
        finally {
            reader.close();
        }
        return count;
    }
    /**
     * Unlock all Features.
     * 
     * @see org.geotools.data.FeatureLocking#unLockFeatures()
     * @throws IOException
     */
    public void unLockFeatures() throws IOException {
        unLockFeatures( Filter.NONE );
    }
    /**
     * Unlock Features specified by <code>filter</code>.
     *  
     * @see org.geotools.data.FeatureLocking#unLockFeatures(org.geotools.filter.Filter)
     * @param filter
     * @throws DataSourceException
     */
    public void unLockFeatures(Filter filter) throws IOException {
        unLockFeatures( new DefaultQuery( filter ) ); 
    }
    /**
     * Unlock features specified by the <code>query</code>.
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.     
     * </p>
     * @see org.geotools.data.FeatureLocking#unLockFeatures(org.geotools.data.Query)
     * @param query
     * @throws IOException
     */
    public void unLockFeatures(Query query) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();        
        if( lockingManager == null){
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation"
            );
        }
        // Could we reduce the Query to only return the FetureID here?
        //
        FeatureReader reader = getFeatures( query ).reader();
        String typeName = reader.getFeatureType().getTypeName();
        Feature feature;
        try {
            while( reader.hasNext() ){
                try {
                    feature = reader.next();
                    lockingManager.unLockFeatureID( typeName, feature.getID(), getTransaction(), featureLock );                    
                } catch (NoSuchElementException nosuch) {
                    throw new DataSourceException( "Problem with "+query.getHandle()+" while locking", nosuch  );  
                } catch(IllegalAttributeException e){
                    throw new DataSourceException( "Problem with "+query.getHandle()+" while locking", e );
                }                
            }
        }
        finally {
            reader.close();
        }    
    }

    /**
     * Refresh locks held by <code>authID</code>.
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.     
     * </p>
     * @see org.geotools.data.FeatureLocking#refreshLock(java.lang.String)
     * @param authID
     * @throws DataSourceException
     */
    public void refreshLock(String authID) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();        
        if( lockingManager == null){
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation"
            );
        }
        lockingManager.refreshLock( authID, getTransaction() );
    }
    /**
     * Release locks held by <code>authID</code>.
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.     
     * </p>
     * @see org.geotools.data.FeatureLocking#releaseLock(java.lang.String)
     * @param authID
     * @throws DataSourceException
     */
    public void releaseLock(String authID) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();        
        if( lockingManager == null){
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation"
            );
        }
        lockingManager.releaseLock( authID, getTransaction() );
    }
}
