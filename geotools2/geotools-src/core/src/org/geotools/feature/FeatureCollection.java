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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Extent;


/** Represents a collection of features. Implementations and client code should
 * adhere to the rules set forth by java.util.Collection. That is, some methods are
 * optional to implement, and may throw an UnsupportedOperationException.
 * @see java.util.Collection
 * @author Ian Turton, CCG
 * @author Rob Hranac, VFNY
 * @author Ian Schneider, USDA-ARS
 * @version $Id: FeatureCollection.java,v 1.11 2003/07/17 07:09:52 ianschneider Exp $
 */
public interface FeatureCollection extends java.util.Collection {
    /**
     * Gets the bounding box for the features in this feature collection.
     *
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    Envelope getBounds();
    
    /** Obtain a FeatureIterator of the Feature Objects contained within this
     * collection. The implementation of Collection must adhere to the rules of
     * fail-fast concurrent modification.
     * @return A FeatureIterator.
     */    
    FeatureIterator features();

    /** Adds a listener for collection events.
     * @param listener The listener to add
     * @throws NullPointerException If the listener is null.
     */
    void addListener(CollectionListener listener) throws NullPointerException;

    /** Removes a listener for collection events.
     * @param listener The listener to remove
     * @throws NullPointerException If the listener is null.
     */
    void removeListener(CollectionListener listener) throws NullPointerException;
}
