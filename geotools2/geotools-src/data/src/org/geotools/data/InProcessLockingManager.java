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

import org.geotools.data.Transaction.State;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.Set;


/**
 * Provides In-Process FeatureLocking support for DataStore implementations.
 * 
 * <p>
 * If at all possible DataStore implementations should provide a real Feature
 * Locking support that is persisted to disk or database and resepected by
 * other processes.
 * </p>
 * 
 * <p>
 * This class provides a stop gap solution that implementations may use for
 * GeoServer compatability.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class InProcessLockingManager implements LockingManager {
    /** lockTable access by typeName stores Transactions or MemoryLocks */
    protected Map lockTables = new HashMap();

    /**
     * Aquire lock on featureID.
     * 
     * <p>
     * This method will fail if Lock is already held by another.
     * </p>
     *
     * @param typeName TypeName storing feature
     * @param featureID FeatureID to lock
     * @param transaction Transaction to lock against
     * @param featureLock FeatureLock describing lock request
     *
     * @throws FeatureLockException Indicates a problem with the lock request
     */
    public synchronized void lockFeatureID(String typeName, String featureID,
        Transaction transaction, FeatureLock featureLock)
        throws FeatureLockException {
        Lock lock = getLock(typeName, featureID);

        // This is a loop so we can wait on Transaction Locks
        //
        while (lock != null) {
            // we have a conflict
            if (lock instanceof TransactionLock) {
                TransactionLock tlock = (TransactionLock) lock;

                if (transaction == tlock.transaction) {
                    // lock already held by this transacstion
                    // we could just consider returning here
                    //
                    throw new FeatureLockException("Transaction Lock is already held by this Transaction",
                        featureID);
                } else {
                    // we should wait till it is available and then grab
                    // the lock
                    try {
                        synchronized (tlock) {
                            tlock.wait();
                        }

                        lock = getLock(typeName, featureID);
                    } catch (InterruptedException interupted) {
                        throw new FeatureLockException("Interupted while waiting for Transaction Lock",
                            featureID, interupted);
                    }
                }
            } else if (lock instanceof MemoryLock) {
                MemoryLock mlock = (MemoryLock) lock;
                throw new FeatureLockException(
                    "Feature Lock is held by Authorization " + mlock.authID,
                    featureID);
            } else {
                throw new FeatureLockException("Lock is already held " + lock,
                    featureID);
            }
        }

        // Lock is Available
        //
        lock = createLock(transaction, featureLock);
        locks(typeName).put(featureID, lock);
    }

    /**
     * Lock for typeName & featureID if it exists.
     * 
     * <p>
     * This method will not return expired locks.
     * </p>
     *
     * @param typeName
     * @param featureID
     *
     * @return Lock if exists, or null
     */
    protected Lock getLock(String typeName, String featureID) {
        Map locks = locks(typeName);

        synchronized (locks) {
            if (locks.containsKey(featureID)) {
                Lock lock = (Lock) locks.get(featureID);

                if (lock.isExpired()) {
                    locks.remove(featureID);

                    return null;
                } else {
                    return lock;
                }
            } else {
                // not found
                return null;
            }
        }
    }

    /**
     * Creates the right sort of In-Process Lock.
     *
     * @param transaction
     * @param featureLock
     *
     * @return In-Process Lock
     *
     * @throws FeatureLockException When a Transaction  lock is requested
     *         against Transaction.AUTO_COMMIT
     */
    protected synchronized Lock createLock(Transaction transaction,
        FeatureLock featureLock) throws FeatureLockException {
        if (featureLock == FeatureLock.TRANSACTION) {
            // we need a Transacstion Lock
            if (transaction == Transaction.AUTO_COMMIT) {
                throw new FeatureLockException(
                    "We cannot issue a Transaction lock against AUTO_COMMIT");
            }

            TransactionLock lock = (TransactionLock) transaction.getState(this);

            if (lock == null) {
                lock = new TransactionLock();
                transaction.putState(this, lock);

                return lock;
            } else {
                return lock;
            }
        } else {
            return new MemoryLock(featureLock);
        }
    }

    /**
     * Access to a Map of locks for typeName
     *
     * @param typeName typeName
     *
     * @return Map of Transaction or MemoryLock by featureID
     */
    protected Map locks(String typeName) {
        synchronized (lockTables) {
            if (lockTables.containsKey(typeName)) {
                return (Map) lockTables.get(typeName);
            } else {
                Map locks = new HashMap();
                lockTables.put(typeName, locks);

                return locks;
            }
        }
    }

    /**
     * Set of all locks.
     *
     * @return Set of all locks
     */
    protected Set allLocks() {
        synchronized (lockTables) {
            Set set = new HashSet();
            Map fidLocks;

            for (Iterator i = lockTables.values().iterator(); i.hasNext();) {
                fidLocks = (Map) i.next();
                set.addAll(fidLocks.values()); 
            }

            return set;
        }
    }

    /**
     * Checks mutability of featureID for this transaction.
     * 
     * <p>
     * Two behaviors are defined by FeatureLocking:
     * </p>
     * 
     * <ul>
     * <li>
     * TransactionLock (Blocking): lock held by a Transaction<br>
     * Authorization is granted to the Transaction holding the Lock. Conflict
     * will result in a block until the Transaction holding the lock
     * completes. (This behavior is equivalent to a Database row-lock, or a
     * java synchronized statement)
     * </li>
     * <li>
     * FeatureLock (Error): lock held by a FeatureLock<br>
     * Authorization is based on the set of Authorization IDs held by the
     * provided Transaction. Conflict will result in an error.  (This behavior
     * is equivalent to the WFS locking specification)
     * </li>
     * </ul>
     * <p>
     * Right now we are just going to error out with an exception
     * </p>
     *
     * @param typeName Feature type to check against
     * @param featureID FeatureID to check
     * @param transaction Provides Authorization
     *
     * @throws IOException If transaction does not have sufficient
     *         authroization
     */
    public void assertAccess(String typeName, String featureID, Transaction transaction)
        throws FeatureLockException {
        Lock lock = getLock(typeName, featureID);

        if ((lock != null) && !lock.isAuthorized(transaction)) {
            throw new FeatureLockException(
                "Transaction does not have authorization for " + typeName + ":"
                + featureID);
        }
    }

    /**
     * Provides a wrapper on the provided writer that checks locks.
     *
     * @param writer FeatureWriter requiring access control
     * @param transaction Transaction being used
     *
     * @return FeatureWriter with lock checking
     */
    public FeatureWriter checkedWriter(final FeatureWriter writer,
        final Transaction transaction) {
        FeatureType featureType = writer.getFeatureType();
        final String typeName = featureType.getTypeName();

        return new FeatureWriter() {
                Feature live = null;

                public FeatureType getFeatureType() {
                    return writer.getFeatureType();
                }

                public Feature next() throws IOException {
                    live = writer.next();

                    return live;
                }

                public void remove() throws IOException {
                    if (live != null) {
                        assertAccess(typeName, live.getID(), transaction);
                    }

                    writer.remove();
                    live = null;
                }

                public void write() throws IOException {
                    if (live != null) {
                        assertAccess(typeName, live.getID(), transaction);
                    }

                    writer.write();
                    live = null;
                }

                public boolean hasNext() throws IOException {
                    live = null;

                    return writer.hasNext();
                }

                public void close() throws IOException {
                    live = null;
       	            writer.close();
                }
            };
    }

    /**
     * Release indicated featureID, must have correct authroization.
     *
     * @param typeName
     * @param featureID
     * @param transaction
     * @param featureLock
     *
     * @throws IOException If lock could not be released
     */
    public synchronized void unLockFeatureID(String typeName, String featureID,
        Transaction transaction, FeatureLock featureLock)
        throws IOException {
        assertAccess(typeName, featureID, transaction);
        locks(typeName).remove(featureID);
    }

    /**
     * Refresh lock indicated by authID, must have correct authorization.
     *
     * @param authID
     * @param transaction
     */
    public synchronized void refreshLock(String authID, Transaction transaction) {
        if( authID == null ) return;
        
        Lock lock;
        for (Iterator i = allLocks().iterator(); i.hasNext();) {
    	    lock = (Lock) i.next();
    	    
    	    if (lock.isExpired()) {
    		    i.remove();
            } else if (lock.isMatch(authID)) {
                lock.refresh();
    	    }
        }
    }

    /**
     * Release lock indicated by authID, must have correct authroization.
     *
     * @param authID
     * @param transaction
     */
    public void releaseLock(String authID, Transaction transaction) {        
        if( authID == null ) return;
        
        Lock lock;
        for (Iterator i = allLocks().iterator(); i.hasNext();) {
	       lock = (Lock) i.next();

            if (lock.isExpired()) {
                i.remove();
            } else if (lock.isMatch(authID)) {
                i.remove();
            }
        }
    }

    /**
     * Implment lockExists.
     * <p>
     * Remeber lock may have expired.
     * </p>
     * @see org.geotools.data.LockingManager#lockExists(java.lang.String)
     * 
     * @param authID
     * @return true if lock exists for authID
     */
    public boolean lockExists(String authID) {
        Lock lock;
        for (Iterator i = allLocks().iterator(); i.hasNext();) {
            lock = (Lock) i.next();

            if (lock.isExpired()) {
                i.remove();
            } else if (authID == null || lock.isMatch(authID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used by test cases
     *
     * @param typeName
     * @param featureID
     *
     * @return Return if feature is currently locked
     */
    public boolean isLocked(String typeName, String featureID) {
        return getLock(typeName, featureID) != null;
    }

    /**
     * Represents In-Process locks for Transactions or FeatureLocks.
     *
     * @author Jody Garnett, Refractions Research
     */
    interface Lock {
        /**
         * Check if lock has expired, it will be removed if so
         *
         * @return <code>true</code> if Lock has gone stale
         */
        boolean isExpired();

        /**
         * Check if authID matches this lock
         *
         * @return <code>true</code> if authID matches
         */
        boolean isMatch(String authID);

        /**
         * Check if transaction is authorized for this lock
         *
         * @return <code>true</code> if transaction is authorized
         */
        boolean isAuthorized(Transaction transaction);

        /**
         * Refresh lock
         */
        void refresh();

        /**
         * Release lock
         */
        void release();
    }

    /**
     * Class representing TransactionDuration locks.
     * 
     * <p>
     * Implements Transasction.State so it can remomve itself when commit() or
     * rollback() is called.
     * </p>
     * 
     * <p>
     * Threads may wait on this object, it will notify when it releases the
     * lock due to a commit or rollback opperation
     * </p>
     *
     * @author Jody Garnett, Refractions Research
     */
    class TransactionLock implements Lock, State {
        /** This will be non-null while lock is fresh */
        Transaction transaction;

        /**
         * A new TranasctionLock for use.
         * 
         * <p>
         * The lock will be stale until added to Tranasction.putState( key,
         * Lock )
         * </p>
         */
        TransactionLock() {
        }

        /**
         * Transaction locks do not match authIDs
         *
         * @param authID Authorization ID being checked
         *
         * @return <code>false</code>
         */
        public boolean isMatch(String authID) {
            return false;
        }

        /**
         * <code>true</code> if Lock has gone stale
         *
         * @return <code>true</code> if lock is stale
         */
        public boolean isExpired() {
            return transaction != null;
        }

        /**
         * Release lock - notify those who are waiting
         */
        public void release() {
            transaction = null;
            notifyAll();
        }

        /**
         * TransactionLocks do not need to be refreshed
         */
        public void refresh() {
            // do not need to implement   
        }

        /**
         * <code>true </code> if tranasction is the same one that provided this
         * lock
         *
         * @param transaction Transaction to check authorization against
         *
         * @return true if transaction is authorized
         */
        public boolean isAuthorized(Transaction transaction) {
            return this.transaction == transaction;
        }

        /**
         * Call back from Transaction.State
         *
         * @param AuthID AuthoID being added to transaction
         *
         * @throws IOException Not used
         */
        public void addAuthorization(String AuthID) throws IOException {
            // we don't need this callback
        }

        /**
         * Will make lock stale on commit
         *
         * @throws IOException If anything goes wrong
         */
        public void commit() throws IOException {
            release();
        }

        /**
         * Will make lock stale on rollback
         *
         * @throws IOException If anything goes wrong
         */
        public void rollback() throws IOException {
            release();
        }

        /**
         * Will make lock stale if removed from Transaction
         *
         * @param transaction Transaction on putState, or null on removeState
         */
        public void setTransaction(Transaction transaction) {
            if (transaction == null) {
                release();
            }

            this.transaction = transaction;
        }

        public String toString() {
            return "TranasctionLock(" + !isExpired() + ")";
        }
    }

    /**
     * Class referenced by featureID in locks( typeName).
     * 
     * <p>
     * FeatureLock is the request - MemoryLock is the result.
     * </p>
     *
     * @author Jody Garnett, Refractions Reasearch Inc.
     */
    class MemoryLock implements Lock {
        String authID;
        long duration;
        long expiry;

        MemoryLock(FeatureLock lock) {
            this(lock.getAuthorization(), lock.getDuration());
        }

        MemoryLock(String id, long length) {
            authID = id;
            this.duration = length;
            expiry = System.currentTimeMillis() + length;
        }

        public boolean isMatch(String id) {
            return authID.equals(id);
        }

        public void refresh() {
            expiry = System.currentTimeMillis() + duration;
        }

        public void release() {
        }

        public boolean isExpired() {
            if (duration == 0) {
                return false; // perma lock
            }

            long now = System.currentTimeMillis();

            return now >= expiry;
        }

        public boolean isAuthorized(Transaction transaction) {
            return (transaction != Transaction.AUTO_COMMIT)
            && transaction.getAuthorizations().contains(authID);
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
    
}
