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
/*
 * DefaultFeatureCollections.java
 *
 * Created on July 17, 2003, 9:25 AM
 */
package org.geotools.feature;

/**
 * Concrete extension to FeatureCollections to create
 * DefaultFeatureCollections.
 *
 * @author Ian Schneider
 */
public class DefaultFeatureCollections extends FeatureCollections {
    /**
     * Creates a new instance of DefaultFeatureCollections
     */
    public DefaultFeatureCollections() {
    }

    /**
     * Creates a new DefaultFeatureCollection.
     *
     * @return A new, empty DefaultFeatureCollection.
     */
    protected FeatureCollection createCollection() {
        return new DefaultFeatureCollection();
    }
}
