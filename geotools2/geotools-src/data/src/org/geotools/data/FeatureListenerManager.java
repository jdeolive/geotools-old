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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.EventListenerList;


/**
 * This class is used by DataStore implementations to provide FeatureListener
 * support for the FeatureSources they create.
 * 
 * <p>
 * FeatureWriters created by the DataStore will need to make use of this class
 * to provide the required FeatureEvents.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class FeatureListenerManager {
    /** EvenListenerLists by FeatureSource */
    Map listenerMap = new HashMap();

    /**
     * Used by FeaureSource implementations to provide listener support.
     *
     * @param featureSource
     * @param featureListener
     */
    public void addFeatureListener(FeatureSource featureSource,
        FeatureListener featureListener) {
        eventListenerList(featureSource).add(FeatureListener.class,
            featureListener);
    }

    /**
     * Used by FeatureSource implementations to provide listener support.
     *
     * @param featureSource
     * @param featureListener
     */
    public void removeFeatureListener(FeatureSource featureSource,
        FeatureListener featureListener) {
        eventListenerList(featureSource).remove(FeatureListener.class,
            featureListener);
    }

    public EventListenerList eventListenerList(FeatureSource featureSource) {
        synchronized (listenerMap) {
            if (listenerMap.containsKey(featureSource)) {
                return (EventListenerList) listenerMap.get(featureSource);
            } else {
                EventListenerList listenerList = new EventListenerList();
                listenerMap.put(featureSource, listenerMap);

                return listenerList;
            }
        }
    }

    /**
     * Returns a Map of FeatureListener[] by FeatureSource for all matches with
     * featureType and transaction.
     * 
     * <p>
     * A FeatureSource is considered a match when typeName and Transaction
     * agree.  Transaction.AUTO_COMMIT will match with any change.
     * </p>
     *
     * @param typeName typeName to match against
     * @param transaction Transaction to match against (may be AUTO_COMMIT)
     *
     * @return
     */
    Map getListeners(String typeName, Transaction transaction) {
        Map map = new HashMap();
        Map.Entry entry;
        FeatureSource featureSource;
        EventListenerList listenerList;
        FeatureListener[] listeners;

        synchronized (listenerMap) {
            for (Iterator i = listenerMap.entrySet().iterator(); i.hasNext();) {
                entry = (Map.Entry) i.next();
                featureSource = (FeatureSource) entry.getKey();

                if (!featureSource.getSchema().getTypeName().equals(typeName)) {
                    continue; // skip as typeName does not match
                }

                if ((transaction != Transaction.AUTO_COMMIT)
                        && hasTransaction(featureSource)) {
                    // need to ensure Transactions match
                    if (transaction != getTransaction(featureSource)) {
                        continue; // skip as transactions do not match        
                    }
                }

                listenerList = (EventListenerList) entry.getValue();
                listeners = (FeatureListener[]) listenerList.getListeners(FeatureListener.class);

                if (listeners.length != 0) {
                    map.put(featureSource, listeners);
                }
            }
        }

        return map;
    }

    private static boolean hasTransaction(FeatureSource featureSource) {
        return featureSource instanceof FeatureStore
        && (((FeatureStore) featureSource).getTransaction() != null);
    }

    private static Transaction getTransaction(FeatureSource featureSource) {
        if (hasTransaction(featureSource)) {
            return ((FeatureStore) featureSource).getTransaction();
        }

        return Transaction.AUTO_COMMIT;
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type.
     * 
     * <p>
     * This method is called by:
     * </p>
     * 
     * <ul>
     * <li>
     * FeatureWriter.next() with FeatureWriter.hasNext() == false<br>
     * - when an existing Feature is removed with Tranasaction.AUTO_COMMIT all
     * listeners registered with FeatureSource of typeName will be notified.
     * </li>
     * <li>
     * FeatureWriter.next()with FeatureWriter.hasNext() == false<br>
     * - when an existing Feature is removed with a Transaction all listeners
     * registered with FeatureSource of typeName and with the same Transaction
     * will be notified.
     * </li>
     * </ul>
     * 
     *
     * @param typeName typeName being modified
     * @param transaction Transaction used for change
     * @param bounds BoundingBox of changes (may be <code>null</code> if
     *        unknown)
     */
    public void fireFeaturesAdded(String typeName, Transaction transaction,
        Envelope bounds) {
        Map.Entry entry;
        FeatureSource featureSource;
        FeatureListener[] listeners;
        FeatureEvent event;

        for (Iterator i = getListeners(typeName, transaction).values().iterator();
                i.hasNext();) {
            entry = (Map.Entry) i.next();
            featureSource = (FeatureSource) entry.getKey();
            listeners = (FeatureListener[]) entry.getValue();

            event = new FeatureEvent(featureSource,
                    FeatureEvent.FEATURES_ADDED, bounds);

            for (int l = 0; l < listeners.length; l++) {
                listeners[l].changed(event);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type.
     * 
     * <p>
     * This method is called by:
     * </p>
     * 
     * <ul>
     * <li>
     * FeatureWriter.next() with FeatureWriter.hasNext() == true <br>
     * - when an existing Feature is modified with Tranasaction.AUTO_COMMIT
     * all listeners registered with FeatureSource of typeName will be
     * notified.
     * </li>
     * <li>
     * FeatureWriter.next()with FeatureWriter.hasNext() == true <br>
     * - when an existing Feature is modified, with a Transaction all
     * listeners registered with FeatureSource of typeName and with the same
     * Transaction will be notified.
     * </li>
     * </ul>
     * 
     *
     * @param typeName typeName being modified
     * @param transaction Transaction used for change
     * @param bounds BoundingBox of changes (may be <code>null</code> if
     *        unknown)
     */
    public void fireFeaturesChanged(String typeName, Transaction transaction,
        Envelope bounds) {
        Map.Entry entry;
        FeatureSource featureSource;
        FeatureListener[] listeners;
        FeatureEvent event;

        for (Iterator i = getListeners(typeName, transaction).values().iterator();
                i.hasNext();) {
            entry = (Map.Entry) i.next();
            featureSource = (FeatureSource) entry.getKey();
            listeners = (FeatureListener[]) entry.getValue();

            event = new FeatureEvent(featureSource,
                    FeatureEvent.FEATURES_CHANGED, bounds);

            for (int l = 0; l < listeners.length; l++) {
                listeners[l].changed(event);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type.
     * 
     * <p>
     * This method is called by:
     * </p>
     * 
     * <ul>
     * <li>
     * Transaction.commit()<br> - when changes have occured on a Transaction
     * all listeners registered with FeatureSource of typeName will be
     * notified except those with the Same Transaction
     * </li>
     * <li>
     * Transaction.rollback()<br> - when changes have been reverted only those
     * listeners registered with FeatureSource of typeName and with the same
     * Transaction will be notified.
     * </li>
     * </ul>
     * 
     *
     * @param typeName typeName being modified
     * @param transaction Transaction used for change
     * @param commit <code>true</code> for <code>commit</code>,
     *        <code>false</code> for <code>rollback</code>
     */
    public void fireChanged(String typeName, Transaction transaction,
        boolean commit) {
        Map.Entry entry;
        FeatureSource featureSource;
        FeatureListener[] listeners;
        FeatureEvent event;

        if (commit) {
            Map map = getListeners(typeName, Transaction.AUTO_COMMIT);

            for (Iterator i = map.values().iterator(); i.hasNext();) {
                entry = (Map.Entry) i.next();
                featureSource = (FeatureSource) entry.getKey();
                listeners = (FeatureListener[]) entry.getValue();

                if (hasTransaction(featureSource)
                        && (getTransaction(featureSource) == transaction)) {
                    continue; // skip notify members of the same transaction
                }

                event = new FeatureEvent(featureSource,
                        FeatureEvent.FEATURES_CHANGED, null);

                for (int l = 0; l < listeners.length; l++) {
                    listeners[l].changed(event);
                }
            }
        } else {
            fireFeaturesChanged(typeName, transaction, null);
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type.
     * 
     * <p>
     * This method is called by:
     * </p>
     * 
     * <ul>
     * <li>
     * FeatureWrtier.remove() - when an existing Feature is removed with
     * Tranasaction.AUTO_COMMIT all listeners registered with FeatureSource of
     * typeName will be notified.
     * </li>
     * <li>
     * FeatureWrtier.remove() - when an existing Feature is removed with a
     * Transaction all listeners registered with FeatureSource of typeName and
     * with the same Transaction will be notified.
     * </li>
     * </ul>
     * 
     *
     * @param typeName typeName being modified
     * @param transaction Transaction used for change
     * @param bounds BoundingBox of changes (may be <code>null</code> if
     *        unknown)
     */
    public void fireFeaturesRemoved(String typeName, Transaction transaction,
        Envelope bounds) {
        Map.Entry entry;
        FeatureSource featureSource;
        FeatureListener[] listeners;
        FeatureEvent event;

        for (Iterator i = getListeners(typeName, transaction).values().iterator();
                i.hasNext();) {
            entry = (Map.Entry) i.next();
            featureSource = (FeatureSource) entry.getKey();
            listeners = (FeatureListener[]) entry.getValue();

            event = new FeatureEvent(featureSource,
                    FeatureEvent.FEATURES_REMOVED, bounds);

            for (int l = 0; l < listeners.length; l++) {
                listeners[l].changed(event);
            }
        }
    }
}
