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
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * A FeatureWriter that captures modifications against a FeatureReader.
 * 
 * <p>
 * You will eventually need to write out the differences, later.
 * </p>
 * 
 * <p>
 * The api has been implemented in terms of FeatureReader to make explicit that
 * no Features are writen out by this Class.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 *
 * @see TransactionStateDiff
 */
public abstract class DiffFeatureWriter implements FeatureWriter {
    protected FeatureReader reader;
    protected Map diff;
    Feature next; // next value aquired by hasNext()
    Feature live; // live value supplied by FeatureReader
    Feature current; // duplicate provided to user

    /**
     * DiffFeatureWriter construction.
     *
     * @param reader
     * @param diff
     */
    public DiffFeatureWriter(FeatureReader reader, Map diff) {
        this.reader = reader;
        this.diff = diff;
    }

    /**
     * Supplys FeatureTypeFrom reader
     *
     * @see org.geotools.data.FeatureWriter#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return reader.getFeatureType();
    }

    /**
     * Next Feature from reader or new content.
     *
     * @see org.geotools.data.FeatureWriter#next()
     */
    public Feature next() throws IOException {
        FeatureType type = getFeatureType();

        if (hasNext()) {
            // hasNext() will take care recording
            // any modifications to current
            try {
                live = next; // update live value
                next = null; // hasNext will need to search again            
                current = type.duplicate(live);

                return current;
            } catch (IllegalAttributeException e) {
                throw new IOException("Could not modify content");
            }
        } else {
            // Create new content
            // created with an empty ID
            // (The real writer will supply a FID later) 
            try {
                live = null;
                next = null;
                current = type.create(new Object[type.getAttributeCount()],
                        "new");

                return current;
            } catch (IllegalAttributeException e) {
                throw new IOException("Could not create new content");
            }
        }
    }

    /**
     * @see org.geotools.data.FeatureWriter#remove()
     */
    public void remove() throws IOException {
        if (live != null) {
            // mark live as removed
            diff.put(live.getID(), null);
            fireNotification(FeatureEvent.FEATURES_REMOVED, live.getBounds());
            live = null;
            current = null;
        } else if (current != null) {
            // cancel additional content
            current = null;
        }
    }

    /**
     * Writes out the current feature.
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureWriter#write()
     */
    public void write() throws IOException {
        if ((live != null) && !live.equals(current)) {
            // We have a modification to record!
            //
            diff.put(live.getID(), current);

            Envelope bounds = new Envelope();
            bounds.expandToInclude(live.getBounds());
            bounds.expandToInclude(current.getBounds());
            fireNotification(FeatureEvent.FEATURES_CHANGED, bounds);
            live = null;
            current = null;
        } else if ((live == null) && (current != null)) {
            // We have new content to record
            //
            diff.put(current.getID(), current);
            fireNotification(FeatureEvent.FEATURES_ADDED, current.getBounds());
            current = null;
        } else {
            throw new IOException("No feature available to write");
        }
    }

    /**
     * Query for more content.
     *
     * @see org.geotools.data.FeatureWriter#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            // we found next already
            return true;
        }

        live = null;
        current = null;

        if (reader.hasNext()) {
            try {
                next = reader.next();
            } catch (NoSuchElementException e) {
                throw new DataSourceException("No more content", e);
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("No more content", e);
            }

            return true;
        }

        return false;
    }

    /**
     * Clean up resources associated with this writer.
     * 
     * <p>
     * Diff is not clear()ed as it is assumed that it belongs to a
     * Transaction.State object and may yet be written out.
     * </p>
     *
     * @see org.geotools.data.FeatureWriter#close()
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }

        current = null;
        live = null;
        next = null;
        diff = null;
    }

    /**
     * Subclass must provide the notification.
     * 
     * <p>
     * Notification requirements for modifications against a Transaction should
     * only be issued to FeatureSource instances that opperate against the
     * same typeName and Transaction.
     * </p>
     * 
     * <p>
     * Other FeatureSource instances with the same typeName will be notified
     * when the Transaction is committed.
     * </p>
     *
     * @param eventType One of FeatureType.FEATURES_ADDED, FeatureType.CHANGED,
     *        FeatureType.FEATURES_REMOVED
     * @param bounds
     */
    abstract void fireNotification(int eventType, Envelope bounds);
}
