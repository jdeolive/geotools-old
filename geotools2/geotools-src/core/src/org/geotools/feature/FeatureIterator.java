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
package org.geotools.feature;

/**
 * A convenience class for dealing with FeatureCollection Iterators. DOES NOT
 * implement Iterator.
 *
 * @author Ian Schneider
 */
public class FeatureIterator {
    /** The iterator from the FeatureCollection to return features from. */
    private java.util.Iterator iterator;

    /**
     * Create a new FeatureIterator using the Iterator from the given
     * FeatureCollection.
     *
     * @param collection The FeatureCollection to perform the iteration on.
     */
    public FeatureIterator(FeatureCollection collection) {
        this.iterator = collection.iterator();
    }

    /**
     * Does another Feature exist in this Iteration.
     *
     * @return true if more Features exist, false otherwise.
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Get the next Feature in this iteration.
     *
     * @return The next Feature
     *
     * @throws java.util.NoSuchElementException If no more Features exist.
     */
    public Feature next() throws java.util.NoSuchElementException {
        return (Feature) iterator.next();
    }
}
