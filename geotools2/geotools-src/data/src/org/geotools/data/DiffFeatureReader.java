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

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * A FeatureReader that considers differences.
 * 
 * <p>
 * Used to implement In-Process Transaction support. This implementation will
 * need to peek ahead in order to check for deletetions.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class DiffFeatureReader implements FeatureReader {
    FeatureReader reader;
    Map diff;

    /** Next value as peeked by hasNext() */
    Feature next = null;

    /**
     * This constructor grabs a "copy" of the current diff.
     * 
     * <p>
     * This reader is not "live" to changes over the course of the Transaction.
     * (Iterators are not always stable of the course of modifications)
     * </p>
     *
     * @param reader
     * @param diff Differences of Feature by FID
     */
    public DiffFeatureReader(FeatureReader reader, Map diff) {
        this.reader = reader;
        this.diff = new HashMap(diff);
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return reader.getFeatureType();
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (hasNext()) {
            Feature live = next;
            next = null;

            return live;
        }

        throw new NoSuchElementException("No more Feature exists");
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            // We found it already
            return true;
        }

        if ((reader != null) && reader.hasNext()) {
            Feature peek;

            try {
                peek = reader.next();
            } catch (NoSuchElementException e) {
                throw new DataSourceException("Could not aquire the next Feature",
                    e);
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Could not aquire the next Feature",
                    e);
            }

            String fid = peek.getID();

            if (diff.containsKey(fid)) {
                Feature changed = (Feature) diff.remove(fid);

                if (changed == null) {
                    return hasNext(); // feature removed try again   
                } else {
                    next = changed;

                    return true; // found modified feature                    
                }
            } else {
                next = peek; // found feature

                return true;
            }
        }

        if ((diff != null) && !diff.isEmpty()) {
            Iterator i = diff.values().iterator();
            next = (Feature) i.next();
            i.remove();

            return true;
        }

        return false;
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }

        if (diff != null) {
            diff.clear();
            diff = null;
        }
    }
}
