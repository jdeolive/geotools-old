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
import org.geotools.data.Transaction.State;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * A Transaction.State that keeps a difference table for use with
 * AbstractDataStore.
 *
 * @author Jody Garnett, Refractions Research
 */
public class TransactionStateDiff implements State {
    /**
     * DataStore used to commit() results of this transaction.
     *
     * @see TransactionStateDiff.commit();
     */
    AbstractDataStore store;

    /** Tranasction this State is opperating against. */
    Transaction transaction;

    /**
     * Map of differences by typeName.
     * 
     * <p>
     * Differences are stored as a Map of Feature by fid, and are reset during
     * a commit() or rollback().
     * </p>
     */
    Map typeNameDiff = new HashMap();

    public TransactionStateDiff(AbstractDataStore dataStore) {
        store = dataStore;
    }

    public synchronized void setTransaction(Transaction transaction) {
        if (transaction != null) {
            // configure
            this.transaction = transaction;
        } else {
            this.transaction = null;

            if (typeNameDiff != null) {
                for (Iterator i = typeNameDiff.values().iterator();
                        i.hasNext();) {
                    Map diff = (Map) i.next();
                    diff.clear();
                }

                typeNameDiff.clear();
            }

            store = null;
        }
    }

    public synchronized Map diff(String typeName) throws IOException {
        if (!exists(typeName)) {
            throw new IOException(typeName + " not defined");
        }

        if (typeNameDiff.containsKey(typeName)) {
            return (Map) typeNameDiff.get(typeName);
        } else {
            Map diff = Collections.synchronizedMap(new HashMap());
            typeNameDiff.put(typeName, diff);

            return diff;
        }
    }

    boolean exists(String typeName) {
        String[] types = store.getTypeNames();
        Arrays.sort(types);

        return Arrays.binarySearch(types, typeName) != -1;
    }

    /**
     * @see org.geotools.data.Transaction.State#addAuthorization(java.lang.String)
     */
    public synchronized void addAuthorization(String AuthID)
        throws IOException {
        // not required for TransactionStateDiff
    }

    /**
     * Will apply differences to store.
     *
     * @see org.geotools.data.Transaction.State#commit()
     */
    public synchronized void commit() throws IOException {
        Map.Entry entry;

        for (Iterator i = typeNameDiff.entrySet().iterator(); i.hasNext();) {
            entry = (Entry) i.next();

            String typeName = (String) entry.getKey();
            Map diff = (Map) entry.getValue();
            applyDiff(typeName, diff);
        }
    }

    /**
     * Called by commit() to apply one set of diff
     * 
     * <p>
     * diff will be modified as the differneces are applied, If the opperations
     * is successful diff will be empty at the end of this process.
     * </p>
     * 
     * <p>
     * diff can be used to represent the following operations:
     * </p>
     * 
     * <ul>
     * <li>
     * fid|null: represents a fid being removed
     * </li>
     * <li>
     * fid|feature: where fid exists, represents feature modification
     * </li>
     * <li>
     * fid|feature: where fid does not exist, represents feature being modified
     * </li>
     * </ul>
     * 
     *
     * @param typeName typeName being updated
     * @param diff differences to apply to FeatureWriter
     *
     * @throws IOException If the entire diff cannot be writen out
     * @throws DataSourceException If the entire diff cannot be writen out
     */
    void applyDiff(String typeName, Map diff) throws IOException {
        if (diff.isEmpty()) {
            return;
        }

        FeatureWriter writer = store.getFeatureWriter(typeName);
        Feature feature;
        Feature update;
        String fid;

        try {
            while (writer.hasNext()) {
                feature = writer.next();
                fid = feature.getID();

                if (diff.containsKey(fid)) {
                    update = (Feature) diff.remove(fid);

                    if (update == null) {
                        writer.remove();

                        // notify
                        store.listenerManager.fireFeaturesRemoved(typeName,
                            transaction, feature.getBounds());
                    } else {
                        try {
                            feature.setAttributes(update.getAttributes(null));
                            writer.write();

                            // notify                        
                            Envelope bounds = new Envelope();
                            bounds.expandToInclude(feature.getBounds());
                            bounds.expandToInclude(update.getBounds());
                            store.listenerManager.fireFeaturesChanged(typeName,
                                transaction, bounds);
                        } catch (IllegalAttributeException e) {
                            throw new DataSourceException("Could update " + fid,
                                e);
                        }
                    }
                }
            }

            Feature addedFeature;
            Feature nextFeature;

            for (Iterator i = diff.values().iterator(); i.hasNext();) {
                addedFeature = (Feature) i.next();
                i.remove();

                if (addedFeature != null) {
                    fid = addedFeature.getID();

                    nextFeature = writer.next();

                    if (nextFeature == null) {
                        throw new DataSourceException("Could not add " + fid);
                    } else {
                        try {
                            nextFeature.setAttributes(addedFeature
                                .getAttributes(null));
                            writer.write();

                            // notify                        
                            store.listenerManager.fireFeaturesAdded(typeName,
                                transaction, nextFeature.getBounds());
                        } catch (IllegalAttributeException e) {
                            throw new DataSourceException("Could update " + fid,
                                e);
                        }
                    }
                }
            }
        } finally {
            writer.close();
            store.listenerManager.fireChanged(typeName, transaction, true);
        }
    }

    /**
     * @see org.geotools.data.Transaction.State#rollback()
     */
    public synchronized void rollback() throws IOException {
        Map.Entry entry;

        for (Iterator i = typeNameDiff.entrySet().iterator(); i.hasNext();) {
            entry = (Entry) i.next();

            String typeName = (String) entry.getKey();
            Map diff = (Map) entry.getValue();

            diff.clear(); // rollback differences
            store.listenerManager.fireChanged(typeName, transaction, false);
        }
    }

    /**
     * Convience Method for a Transaction based FeatureReader.
     * 
     * <p>
     * Constructs a DiffFeatureReader that works against this Transaction.
     * </p>
     *
     * @param typeName TypeName to aquire a Reader on
     *
     * @return FeatureReader the mask orgional contents with against the
     *         current Differences recorded by the Tansasction State
     *
     * @throws IOException If typeName is not Manged by this Tansaction State
     */
    public synchronized FeatureReader reader(String typeName)
        throws IOException {
        Map diff = diff(typeName);
        FeatureReader reader = store.getFeatureReader(typeName);

        return new DiffFeatureReader(reader, diff);
    }

    /**
     * Convience Method for a Transaction based FeatureWriter
     * 
     * <p>
     * Constructs a DiffFeatureWriter that works against this Transaction.
     * </p>
     *
     * @param typeName Type Name to record differences against
     *
     * @return A FeatureWriter that records Differences against a FeatureReader
     *
     * @throws IOException If a FeatureRader could not be constucted to record
     *         differences against
     */
    public synchronized FeatureWriter writer(final String typeName)
        throws IOException {
        Map diff = diff(typeName);
        FeatureReader reader = store.getFeatureReader(typeName);

        return new DiffFeatureWriter(reader, diff) {
                void fireNotification(int eventType, Envelope bounds) {
                    switch (eventType) {
                    case FeatureEvent.FEATURES_ADDED:
                        store.listenerManager.fireFeaturesAdded(typeName,
                            transaction, bounds);

                        break;

                    case FeatureEvent.FEATURES_CHANGED:
                        store.listenerManager.fireFeaturesChanged(typeName,
                            transaction, bounds);

                        break;

                    case FeatureEvent.FEATURES_REMOVED:
                        store.listenerManager.fireFeaturesRemoved(typeName,
                            transaction, bounds);

                        break;
                    }
                }
            };
    }
}
