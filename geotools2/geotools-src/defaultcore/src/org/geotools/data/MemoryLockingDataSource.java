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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Quick & Dirty implementation used to test LockingDataSource API. When
 * suitable for public consumption it will escape the bounds of package
 * visability.
 * 
 * <p>
 * Known Bugs:
 * </p>
 * 
 * <ul>
 * <li>
 * Assumes a consistent FeatureType
 * </li>
 * <li>
 * Does not share Locking between two MemoryLockingDataSources serving up the
 * same FeatureCollection.
 * </li>
 * </ul>
 * 
 *
 * @author jgarnett, Refractions Reasearch Inc.
 * @version CVS Version
 *
 * @see org.geotools.data
 */
class MemoryLockingDataSource extends MemoryDataSource
    implements LockingDataSource {
    /** Holds our locking information. */
    protected Map locks = new HashMap();

    /** Holds feature information over the course of a transaction. */
    protected Map transactionFeatures;

    /** Holds our locking information over the course of a transaction. */
    protected Map transactionLocks;

    //
    // The Gory Details
    //

    /** Current authroization available (contains String) */
    protected Set authorization = new HashSet();

    /**
     * Current Lock for use by lockFeatures requests.
     * 
     * <p>
     * Require all fids be unlocked
     * </p>
     */
    protected FeatureLock lock = FeatureLock.CURRENT_TRANSACTION;

    //J-
    /**
     * Simple constructor.   Example: <code> <pre>LockingDataSource ds = new
     * MemoryLockingDataSource(); </pre></code>
     */
    //J+
    public MemoryLockingDataSource() {
        super();
    }

    /**
     * Changes The FeatureList based on setAutoCommitMode and current
     * Transaction status.
     *
     * @see org.geotools.data.MemoryDataSource#getFeaturesList()
     */
    protected Map getFeaturesMap() {
        if (transactionFeatures != null) {
            return transactionFeatures;
        } else {
            return super.getFeaturesMap();
        }
    }

    /**
     * Changes the LockMap based on the setAutoCommitMode and the current
     * Tranasaction status.
     *
     * @return
     */
    protected Map getLocks() {
        return (transactionLocks != null) ? transactionLocks : locks;
    }

    /**
     * One line description of action.
     * 
     * <p>
     * Detailed Description of setAutoCommit.
     * </p>
     *
     * @param autoCommit
     *
     * @throws DataSourceException
     * @throws UnsupportedOperationException
     *
     * @see org.geotools.data.DataSource#setAutoCommit(boolean)
     */
    public void setAutoCommit(boolean autoCommit)
        throws DataSourceException, UnsupportedOperationException {
        // cannot tell if I need to call super before my
        // NOP check or after?
        // Before - will break super's metadata check
        // After - will break if super implements getAutoCommit
        super.setAutoCommit(autoCommit);

        // NOP check - no change requried    
        if (autoCommit == getAutoCommit()) {
            return;
        }

        if (autoCommit) {
            // We have unsaved commits
            //
            // Since we are autoCommiting keep changes
            // exit transaction mode
            endTransaction();
            features = transactionFeatures;
            locks = transactionLocks;
            transactionFeatures = null;
            transactionLocks = null;
        } else {
            // enter autocommit mode
            // 
            transactionFeatures = new HashMap( features );
            transactionLocks = new HashMap( locks );
        }
    }

    /**
     * True if we are in autoCommit mode.
     * 
     * <p></p>
     *
     * @return Auto commit status
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.DataSource#getAutoCommit()
     */
    public boolean getAutoCommit() throws DataSourceException {
        // Hope I am calling super correctly?        
        return super.getAutoCommit() && (transactionFeatures == null);
    }

    /**
     * Commit current transaction.
     * 
     * <p>
     * Will update features with (possibly modified) transactionFeatures.
     * </p>
     * 
     * <p>
     * All existing TransactionLocks will be relesaed.
     * </p>
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.DataSource#commit()
     */
    public void commit() throws DataSourceException {
        super.commit(); // expecting AbstractDataSource to check metadata

        if (getAutoCommit()) {
            throw new DataSourceException("Commit() requries a transaction");
        }

        endTransaction(); // need to remove MemoryLocks first :-)
        features = new HashMap( transactionFeatures );
        locks = new HashMap( transactionLocks );
    }

    /**
     * Rollback current transaction.
     * 
     * <p>
     * Will throw out current (possibly modified) transactionFeatures.
     * </p>
     * 
     * <p>
     * All existing TransactionLocks will be relesaed.
     * </p>
     *
     * @throws DataSourceException
     * @throws UnsupportedOperationException
     *
     * @see org.geotools.data.DataSource#rollback()
     */
    public void rollback()
        throws DataSourceException, UnsupportedOperationException {
        super.rollback();

        if (getAutoCommit()) {
            throw new DataSourceException("Rollback requries a transaction");
        }

        transactionFeatures = new HashMap( features );
        transactionLocks = new HashMap( locks );
        endTransaction();
    }

    /**
     * Called to cleanup after a transaction.
     * 
     * <p>
     * Removes current FeatureLock, AuthorizationSet and TransactionLocks.
     * </p>
     */
    protected void endTransaction() {
        releaseTransactionLock();
        authorization = new HashSet();
        lock = FeatureLock.CURRENT_TRANSACTION;
    }

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     *
     * @throws DataSourceException If anything goes wrong.
     * @throws UnsupportedOperationException if the removeFeatures method is
     *         not supported by this datasource.
     */
    public void removeFeatures(Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        super.removeFeatures(filter); // takes care of metadata check

        Set fids = getFidSet(makeDefaultQuery(filter));
        removeFids(fids);
        resetBBox();
    }

    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     *
     * @throws DataSourceException If modificaton is not supported, if the
     *         attribute and object arrays are not eqaul length, or if the
     *         object types do not match the attribute types.
     * @throws UnsupportedOperationException if the modifyFeatures method is
     *         not supported by this datasource
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter)
        throws DataSourceException, UnsupportedOperationException {
        super.modifyFeatures(type, value, filter); // takes care of metadata check

        Set fids = getFidSet(makeDefaultQuery(filter));
        ensureAuthorization(fids);

        try {
            modifyFids(fids, type, value);
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not modify.", e);
        }

        resetBBox();
    }

    protected void modifyFids(Set fids, AttributeType[] type, Object[] value)
        throws DataSourceException, ArrayIndexOutOfBoundsException, 
            IllegalAttributeException {
        ensureAuthorization(fids);

        Feature feature;
        String fid;
        
        for( Iterator i = fids.iterator(); i.hasNext(); ){
            fid = (String) i.next();
            if( getFeaturesMap().containsKey( fid ) ){
                feature = (Feature) getFeaturesMap().get( fid );
                
                modifyFeature(feature, type, value);                    
            }
        }        
    }

    protected void modifyFeature(Feature feature, AttributeType[] type,
        Object[] value) throws IllegalAttributeException {
        FeatureType featureType = feature.getFeatureType();
        int[] index = new int[type.length];
        int i;

        for (i = 0; i < type.length; i++) {
            index[i] = featureType.find(type[i]);

            if (index[i] == -1) {
                throw new IllegalAttributeException("Could not find "
                    + type[i].getName());
            }

            // do all this early so we don't half do a modification
            type[i].validate(value[i]);
        }

        for (i = 0; i < type.length; i++) {
            feature.setAttribute(index[i], value[i]);
        }
    }

    /**
     * Replace contents of datasource with a new FeatureCollection.
     *
     * @param collection
     *
     * @throws DataSourceException
     * @throws UnsupportedOperationException
     *
     * @see org.geotools.data.DataSource#setFeatures(org.geotools.feature.FeatureCollection)
     */
    public void setFeatures(FeatureCollection collection)
        throws DataSourceException, UnsupportedOperationException {
        super.setFeatures(collection); // check metadata
        ensureAuthorization();

        getFeaturesMap().clear();
        
        addFeatures(collection);
    }

    //
    // MemoryDataSource Overrides
    //

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
        MetaDataSupport memMeta = new MetaDataSupport();
        memMeta.setSupportsAdd(true);
        memMeta.setFastBbox(true);
        memMeta.setSupportsRemove(true);
        memMeta.setSupportsModify(true);
        memMeta.setSupportsGetBbox(true);
        memMeta.setSupportsSetFeatures(true);
        memMeta.setSupportsRollbacks(true);

        return memMeta;
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws DataSourceException if bounds could not be calculated
     * @throws UnsupportedOperationException if the datasource can't get
     *         bounds.
     */
    public Envelope getBounds()
        throws DataSourceException, UnsupportedOperationException {
        super.getBounds(); // metadata check

        return calculateBBox();
    }

    //
    // Manage MemoryDataSource protected Varaible
    //
    protected Envelope calculateBBox() {
        Envelope newBBox = new Envelope();
        Envelope internal;
        Feature feature;

        for (Iterator i = getFeaturesMap().values().iterator(); i.hasNext();) {
            feature = (Feature) i.next();
            internal = feature.getDefaultGeometry().getEnvelopeInternal();
            newBBox.expandToInclude(internal);
        }

        return newBBox;
    }

    protected void resetBBox() {
        bbox = calculateBBox();
    }

    /**
     * Removes set of Features and name any locks held by that feature.
     * 
     * <p></p>
     *
     * @param fids
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected void removeFids(Set fids) throws DataSourceException {
        ensureAuthorization(fids);

        Feature feature;
        String fid;

        for( Iterator i = fids.iterator(); i.hasNext(); ){
            fid = (String) i.next();
            
            getFeaturesMap().remove( fid );
            getLocks().remove( fid );
        }        
    }

    //
    // LockingDataSource Implementation
    //

    /**
     * Grabs FeatureLock for subsequence locking operations.
     *
     * @param featureLock Set FeatureLock.TRANSACTION for per transaction
     *        locking.
     *
     * @throws NullPointerException When a featureLock is not provided
     *
     * @see org.geotools.data.LockingDataSource#setCurrentLock(org.geotools.data.FeatureLock)
     */
    public void setFeatureLock(FeatureLock featureLock) {
        if (featureLock == null) {
            throw new NullPointerException("FeatureLock required");
        }

        lock = featureLock;
    }

    /**
     * Locks features specified by query.
     * 
     * <p>
     * Requires either a transaction or a FeatureLock
     * </p>
     *
     * @param query
     *
     * @return Number of Features locked.
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#lockFeatures(org.geotools.data.Query)
     */
    public int lockFeatures(Query query) throws DataSourceException {
        return lockFidsSet(getFidSet(query));
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
     * Record LockAuthorization.
     *
     * @see org.geotools.data.LockingDataSource#setAuthorization(java.lang.String[])
     */
    public void setAuthorization(Set authIDs) {
        authorization = new HashSet();

        if ((authIDs == null) || (authIDs.size() == 0)) {
            return;
        }

        authorization.addAll(authIDs);
    }

    /**
     * Record LockAuthorization.
     *
     * @see org.geotools.data.LockingDataSource#setAuthorization(java.lang.String[])
     */
    public void setAuthorization(String authID) {
        setAuthorization(Collections.singleton(authID));
    }

    /**
     * Unlock all features.
     * 
     * <p>
     * Requires authorization.
     * </p>
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#unLockFeatures()
     */
    public void unLockFeatures() throws DataSourceException {
        unLockFeatures(Query.ALL);
    }

    /**
     * Unlock filtered features.
     * 
     * <p>
     * Requires authorization.
     * </p>
     *
     * @param filter
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#unLockFeatures(org.geotools.filter.Filter)
     */
    public void unLockFeatures(Filter filter) throws DataSourceException {
        unLockFeatures(makeDefaultQuery(filter));
    }

    /**
     * Unlock features specified in query.
     *
     * @param query
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#unLockFeatures(org.geotools.data.Query)
     */
    public void unLockFeatures(Query query) throws DataSourceException {
        unLockFidSet(getFidSet(query));
    }

    /**
     * Reset locked, given authID.
     * 
     * <p>
     * Used to reset lock on unmodifed features in a WFS transaction.
     * </p>
     *
     * @param authID
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#refreshLock(java.lang.String)
     */
    public void refreshLock(String authID) throws DataSourceException {
        if (!authorization.contains(authID)) {
            throw new DataSourceException("Not authorized to refresh " + authID);
        }

        Set locks = new HashSet(getLocks().values());
        MemoryLock memoryLock;

        for (Iterator i = locks.iterator(); i.hasNext();) {
            memoryLock = (MemoryLock) i.next();

            if (authID.equals(memoryLock.authID)) {
                memoryLock.refresh();
            }
        }
    }

    /**
     * Release a locks, given authorization.
     * 
     * <p>
     * Several locks may be listed under one authorization ID.
     * </p>
     *
     * @param authID
     *
     * @throws DataSourceException
     *
     * @see org.geotools.data.LockingDataSource#releaseLock(java.lang.String)
     */
    public void releaseLock(String authID) throws DataSourceException {
        if (!authorization.contains(authID)) {
            throw new DataSourceException("Not authorized to release " + authID);
        }

        Set locks = new HashSet(getLocks().values());
        MemoryLock memoryLock;

        for (Iterator i = locks.iterator(); i.hasNext();) {
            memoryLock = (MemoryLock) i.next();

            if (!authID.equals(memoryLock.authID)) {
                i.remove();
            }
        }

        releaseLocks(locks);
    }

    /**
     * Release Transaction locks.
     * 
     * <p>
     * No authorization is required. We could have separate TransactionLocks
     * based on ThreadID or somesuch though.
     * </p>
     *
     * @see org.geotools.data.LockingDataSource#releaseLock(java.lang.String)
     */
    public void releaseTransactionLock() {
        releaseLock(MemoryLock.CURRENT_TRANSACTION);
    }

    /**
     * Implements all lockFeature() methods
     * 
     * <p></p>
     *
     * @param fids The ids of the features.
     *
     * @return Number of FIDs locked by operation.
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected int lockFidsSet(Set fids) throws DataSourceException {
        if ((lock == FeatureLock.CURRENT_TRANSACTION) && getAutoCommit()) {
            throw new DataSourceException(
                "Cannot lock features without either a transaction or FeatureLock.");
        }

        if (fids.isEmpty()) {
            return 0;
        }

        int count = 0;

        MemoryLock memoryLock = createMemoryLock(lock);
        String fid;

        for (Iterator i = fids.iterator(); i.hasNext();) {
            fid = (String) i.next();

            if (isLocked(fid)) {
                continue;
            }

            getLocks().put(fid, memoryLock);
            count++;
        }

        return count;
    }

    /**
     * Remove the the locks on the provied Fids.
     * 
     * <p>
     * An authroization check is performed - the entire operations will fail
     * and nothing will be unlocked if this check fails.
     * </p>
     * 
     * <p>
     * You will need to request checks individually if you are trying to do
     * SOME unlocking in the WFS sense.
     * </p>
     *
     * @param fids
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected void unLockFidSet(Set fids) throws DataSourceException {
        ensureAuthorization(fids);
        getLocks().keySet().removeAll(fids);
    }

    /**
     * Will fail with if any fids are locked
     *
     * @param fids DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected void ensureUnLocked(Set fids) throws DataSourceException {
        for (Iterator i = fids.iterator(); i.hasNext();) {
            ensureUnLocked((String) i.next());
        }
    }

    /**
     * Will fail with if fid is locked
     *
     * @param fid DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected void ensureUnLocked(String fid) throws DataSourceException {
        MemoryLock memoryLock = getMemoryLock(fid);

        if (memoryLock == null) {
            return;
        }

        throw new DataSourceException("Feature (FID " + fid + ") "
            + "was Locked with " + memoryLock.authID);
    }

    /**
     * Will fail if not authorized to modify the entire table
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected void ensureAuthorization() throws DataSourceException {
        Set locks = new HashSet(getLocks().values());
        MemoryLock memoryLock;

        for (Iterator i = locks.iterator(); i.hasNext();) {
            memoryLock = (MemoryLock) i.next();

            if (!memoryLock.isAuthorized(authorization)) {
                throw new DataSourceException("Not authorized with "
                    + memoryLock.authID);
            }
        }
    }

    /**
     * Will fail with if not authorized
     *
     * @param fids DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected void ensureAuthorization(Set fids) throws DataSourceException {
        for (Iterator i = fids.iterator(); i.hasNext();) {
            ensureAuthorization((String) i.next());
        }
    }

    /**
     * Will fail with if not authorized
     *
     * @param fid DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected void ensureAuthorization(String fid) throws DataSourceException {
        MemoryLock memoryLock = getMemoryLock(fid);

        if (memoryLock == null) {
            return; // was not locked;
        }

        if (memoryLock == MemoryLock.CURRENT_TRANSACTION) {
            return;
        }

        if (memoryLock.isAuthorized(authorization)) {
            return;
        }

        throw new DataSourceException(
            "Authorization Required for Feature (FID " + fid + ") "
            + "require " + memoryLock.authID);
    }

    /**
     * Returns the subset of the provided fids that we have authorization for.
     *
     * @param fids
     *
     * @return
     */
    protected Set authorizedSet(Set fids) {
        Set authorized = new HashSet();
        String fid;

        for (Iterator i = fids.iterator(); i.hasNext();) {
            fid = (String) i.next();

            if (isAuthorized(fid)) {
                authorized.add(fid);
            }
        }

        return authorized;
    }

    /**
     * FID based authorization check
     *
     * @param fid
     *
     * @return True if we have authorization for fid.
     */
    protected boolean isAuthorized(String fid) {
        MemoryLock memoryLock = getMemoryLock(fid);

        if (memoryLock == null) {
            return true; // was not locked;
        }

        if (memoryLock == MemoryLock.CURRENT_TRANSACTION) {
            return true;
        }

        return memoryLock.isAuthorized(authorization);
    }

    /**
     * Tests to see if an fid is locked.
     * 
     * <p>
     * (This will release expired locks)
     * </p>
     *
     * @param fid
     *
     * @return
     */
    protected boolean isLocked(String fid) {
        return getMemoryLock(fid) != null;
    }

    /**
     * Used for debuging locking expirery process
     *
     * @param fid DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected String lockStatus(String fid) {
        MemoryLock memoryLock = null;

        if (getLocks().containsKey(fid)) {
            memoryLock = (MemoryLock) getLocks().get(fid);

            if (memoryLock == MemoryLock.CURRENT_TRANSACTION) {
                return "Transaction Lock";
            }

            return memoryLock.toString();
        }

        return "Not Locked";
    }

    protected String locksStatus() {
        if (getLocks().isEmpty()) {
            return "No Locks Held";
        }

        StringBuffer buffer = new StringBuffer(getLocks().size()
                + " Locks Held");

        for (Iterator i = getLocks().entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            buffer.append("\n");
            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(entry.getValue());
        }

        return buffer.toString();
    }

    /**
     * Tests to see if an fid is locked.
     * 
     * <p>
     * (This will release expired locks)
     * </p>
     *
     * @param fid
     *
     * @return
     */
    protected MemoryLock getMemoryLock(String fid) {
        if (!getLocks().containsKey(fid)) {
            return null;
        }

        MemoryLock memoryLock = (MemoryLock) getLocks().get(fid);

        if (memoryLock.isExpired()) {
            releaseLock(memoryLock);

            return null;
        }

        return memoryLock;
    }

    /**
     * Release set of expirered Locks
     */
    protected void releaseExpiredLocks() {
        Set locks = new HashSet(getLocks().values());
        MemoryLock memoryLock;

        for (Iterator i = locks.iterator(); i.hasNext();) {
            memoryLock = (MemoryLock) i.next();

            if (!memoryLock.isExpired()) {
                i.remove();
            }
        }

        releaseLocks(locks);
    }

    /**
     * release set of MemoryLocks
     *
     * @param memoryLocks DOCUMENT ME!
     */
    protected void releaseLocks(Set memoryLocks) {
        getLocks().values().removeAll(memoryLocks);
    }

    /**
     * Release single MemoryLock - may be used by more than one FID though
     *
     * @param memoryLock DOCUMENT ME!
     */
    protected void releaseLock(MemoryLock memoryLock) {
        releaseLocks(Collections.singleton(memoryLock));
    }

    /**
     * Grabs Fids for Query.
     *
     * @param query
     *
     * @return Set of fids (String)
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    protected Set getFidSet(Query query) throws DataSourceException {
        HashSet set = new HashSet();

        FeatureCollection features = getFeatures(query);
        Feature feature;

        for (FeatureIterator iterator = features.features();
                iterator.hasNext();) {
            feature = iterator.next();
            set.add(feature.getID());
        }

        return set;
    }

    protected MemoryLock createMemoryLock(FeatureLock featureLock) {
        if (featureLock == FeatureLock.CURRENT_TRANSACTION) {
            // per transaction lock
            return MemoryLock.CURRENT_TRANSACTION;
        }

        return new MemoryLock(featureLock);
    }
}


/**
 * Class referenced by fid in MemoryDataSource.
 * 
 * <p>
 * FeatureLock is the request - MemoryLock is the result.
 * </p>
 *
 * @author Refractions Reasearch Inc.
 * @author jgarnett
 * @version CVS Version
 *
 * @see org.geotools.data
 */
class MemoryLock {
    static final MemoryLock CURRENT_TRANSACTION = new MemoryLock("CURRENT_TRANSACTION",
            0);
    String authID;
    long duration;
    long expiry;

    public MemoryLock(FeatureLock featureLock) {
        this(featureLock.getAuthorization(), featureLock.getDuration());
    }

    protected MemoryLock(String id, long length) {
        authID = id;
        this.duration = length;
        expiry = System.currentTimeMillis() + length;
    }

    public void refresh() {
        expiry = System.currentTimeMillis() + duration;
    }

    public boolean isExpired() {
        if (duration == 0) {
            return false; // perma lock
        }

        long now = System.currentTimeMillis();

        return now >= expiry;
    }

    public boolean isAuthorized(Set authorization) {
        return authorization.contains(authID);
    }

    public String toString() {
        if (duration == 0) {
            return "MemoryLock(" + authID + "|PermaLock)";
        }

        long now = System.currentTimeMillis();
        long delta = (expiry - now);
        long dur = duration;

        return "MemoryLock(" + authID + "|" + delta + "ms|" + dur + "ms)";
    }
}
