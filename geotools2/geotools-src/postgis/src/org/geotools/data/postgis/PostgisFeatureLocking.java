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
package org.geotools.data.postgis;


//JTS imports

//geotools imports
import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLockException;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;


/**
 * Extends PostgisFeatureLocking with support for Locking.
 * <p>
 * This class will be, horror, modey. While the are plenty of Object Oriented
 * ways to fix this I have a deadline right now.
 * </p>
 * <p>
 * When the DataStore is constructed it will create a LockingManager only if
 * the Postgis implementation does not support database locking. If the
 * lockingManger is present it will be used.
 * </p>
 * <p>
 * If the lockingManger is not present, the this class will
 * use Database locking
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc
 * @version $Id: PostgisFeatureLocking.java,v 1.5 2003/11/22 00:20:54 jive Exp $
 *
 */
public class PostgisFeatureLocking extends PostgisFeatureStore implements FeatureLocking {
    FeatureLock featureLock = FeatureLock.TRANSACTION;
    
    public PostgisFeatureLocking(PostgisDataStore postgisDataStore,
        FeatureType featureType) throws IOException {
        super(postgisDataStore, featureType);
    }
    
    /**
     * Provide a FeatureLock for locking opperations to opperate against.
     * <p>
     * Initial Transactional duration locks can be restored with
     * setFeatureLock( FetaureLock.TRANSACTION )
     * </p>
     *
     * @param lock FeatureLock (or FeatureLock.TRANSACTION );
     *
     * @throws NullPointerException If lock was <code>null</code>
     *
     * @see org.geotools.data.FeatureLocking#setFeatureLock(org.geotools.data.FeatureLock)
     */
    public void setFeatureLock(FeatureLock lock) {
        if (lock == null) {
            throw new NullPointerException(
                "A FeatureLock is required - did you mean FeatureLock.TRANSACTION?");
        }

        featureLock = lock;
    }

    /**
     * Lock all Features
     *
     * @return Number of Locked features
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureLocking#lockFeatures()
     */
    public int lockFeatures() throws IOException {
        return lockFeatures(Filter.NONE);
    }

    /**
     * Lock features matching <code>filter</code>.
     *
     * @param filter
     *
     * @return Number of locked Features
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureLocking#lockFeatures(org.geotools.filter.Filter)
     */
    public int lockFeatures(Filter filter) throws IOException {
        return lockFeatures(new DefaultQuery( getSchema().getTypeName(), filter));
    }

    /**
     * Lock features matching Query.
     * 
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.
     * </p>
     *
     * @param query
     *
     * @return Number of locked Features
     *
     * @throws IOException If we could not determine which feature to lock
     *         based on Query
     * @throws UnsupportedOperationException When DataStore does not provide a
     *         LockingManager
     * @throws DataSourceException If feature to be locked does not exist
     *
     * @see org.geotools.data.FeatureLocking#lockFeatures(org.geotools.data.Query)
     */
    public int lockFeatures(Query query) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();
        
        if (lockingManager == null) {
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation");
        }
        String typeName = getSchema().getTypeName();        
                
        if( query.getTypeName() != null &&
            !typeName.equals( query.getTypeName() )){
            throw new IOException("Query typeName does not match "+getSchema().getTypeName()+":"+query);   
        }
        //
        // WILD HACK FOR SPEED
        //
        boolean SPEED_HACK = false;         
        if( SPEED_HACK && query.getPropertyNames() == Query.NO_NAMES && query.getFilter() instanceof FidFilter ){
            Transaction transaction = getTransaction();
            FidFilter fidFilter = (FidFilter) query.getFilter();
            String fids[] = fidFilter.getFids();
            int count = 0;
            for ( int i=0; i<fids.length;i++){
                try {
                    lockingManager.lockFeatureID( typeName, fids[i], transaction, featureLock );
                    count++;
                }
                catch( IOException io){
                    // could not aquire - don't increment count
                }
            }                 
            return count;
        }
        
        // Reduce the Query to only return the FetureID here?
        //
        Query optimizedQuery = new DefaultQuery( typeName, query.getFilter(), query.getMaxFeatures(), Query.NO_NAMES, query.getHandle() ); 
        FeatureReader reader = getFeatures(optimizedQuery).reader();
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                try {
                    feature = reader.next();
                    lockingManager.lockFeatureID(typeName, feature.getID(),
                    transaction, featureLock);
                    count++;
                } catch (FeatureLockException locked) {
                    // could not aquire - don't increment count                
                } catch (NoSuchElementException nosuch) {
                    throw new DataSourceException("Problem with "
                        + query.getHandle() + " while locking", nosuch);
                } catch (IllegalAttributeException e) {
                    throw new DataSourceException("Problem with "
                        + query.getHandle() + " while locking", e);
                }
            }
        } finally {
            reader.close();
        }

        return count;
    }

    /**
     * Unlock all Features.
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureLocking#unLockFeatures()
     */
    public void unLockFeatures() throws IOException {
        unLockFeatures(Filter.NONE);
    }

    /**
     * Unlock Features specified by <code>filter</code>.
     *
     * @param filter
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureLocking#unLockFeatures(org.geotools.filter.Filter)
     */
    public void unLockFeatures(Filter filter) throws IOException {
        unLockFeatures(new DefaultQuery( getSchema().getTypeName(), filter));
    }

    /**
     * Unlock features specified by the <code>query</code>.
     * 
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.
     * </p>
     *
     * @param query
     *
     * @throws IOException
     * @throws UnsupportedOperationException If lockingManager is not provided
     *         by DataStore subclass
     * @throws DataSourceException Filter describes an unlocked Feature, or
     *         authorization not held
     *
     * @see org.geotools.data.FeatureLocking#unLockFeatures(org.geotools.data.Query)
     */
    public void unLockFeatures(Query query) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();

        if (lockingManager == null) {
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation");
        }

        // Could we reduce the Query to only return the FetureID here?
        //
        FeatureReader reader = getFeatures(query).reader();
        String typeName = reader.getFeatureType().getTypeName();
        Feature feature;

        try {
            while (reader.hasNext()) {
                try {
                    feature = reader.next();
                    lockingManager.unLockFeatureID(typeName, feature.getID(),
                        getTransaction(), featureLock);
                } catch (NoSuchElementException nosuch) {
                    throw new DataSourceException("Problem with "
                        + query.getHandle() + " while locking", nosuch);
                } catch (IllegalAttributeException e) {
                    throw new DataSourceException("Problem with "
                        + query.getHandle() + " while locking", e);
                }
            }
        } finally {
            reader.close();
        }
    }

    /**
     * Refresh locks held by <code>authID</code>.
     * 
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.
     * </p>
     *
     * @param authID
     *
     * @throws IOException If authorization was not held to refresh lock
     * @throws UnsupportedOperationException When DataStore does not  use
     *         lockingManger, and has not overriden this method
     *
     * @see org.geotools.data.FeatureLocking#refreshLock(java.lang.String)
     */
    public void refreshLock(String authID) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();

        if (lockingManager == null) {
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation");
        }

        lockingManager.refreshLock(authID, getTransaction());
    }

    /**
     * Release locks held by <code>authID</code>.
     * 
     * <p>
     * FeatureStores that have provided their own locking to will need to
     * override this method.
     * </p>
     *
     * @param authID
     *
     * @throws IOException If authorization was not held for lock
     * @throws UnsupportedOperationException When a DataStore subclass does not
     *         provide a lockingManger
     *
     * @see org.geotools.data.FeatureLocking#releaseLock(java.lang.String)
     */
    public void releaseLock(String authID) throws IOException {
        LockingManager lockingManager = getDataStore().getLockingManager();

        if (lockingManager == null) {
            throw new UnsupportedOperationException(
                "DataStore not using lockingManager, must provide alternate implementation");
        }

        lockingManager.releaseLock(authID, getTransaction());
    }    
}