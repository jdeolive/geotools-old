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
package org.geotools.data;

import java.io.IOException;


/**
 * This class describes a featureID based locking service.
 * 
 * <p>
 * AbstractFeatureLocking, and others, may use this API to request locks on the
 * basis of FeatureID.
 * </p>
 * 
 * <p>
 * This class is also used as a public api to manage locks.
 * </p> 
 *
 * @author Jody Garnett, Refractions Research
 */
public interface LockingManager {
    /**
     * Release lock locks held by authID
     *
     * @param authID
     * @param transaction
     */
    void releaseLock(String authID, Transaction transaction)
        throws IOException;

    /**
     * Refresh locks held by authID
     *
     * @param authID
     * @param transaction
     */
    void refreshLock(String authID, Transaction transaction)
        throws IOException;

    /**
     * FeatureID based unlocking.
     *
     * @param typeName
     * @param authID
     * @param transaction
     * @param featureLock
     */
    void unLockFeatureID(String typeName, String authID,
        Transaction transaction, FeatureLock featureLock)
        throws IOException;

    /**
     * FeatureID based locking.
     *
     * @param typeName
     * @param autID
     * @param transaction
     * @param featureLock
     */
    void lockFeatureID(String typeName, String authID, Transaction transaction,
        FeatureLock featureLock) throws IOException;
        
    /**
     * Check if lock exists on this DataStore.
     * <p>
     * Remember that the lock may have expired.
     * </p>
     * @param authID
     * @return
     */
    boolean lockExists( String authID );
}
