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
 * An Index is built up around a FeatureTable, using one of the columns in
 * FeatureTable as a comparable reference. An object in a column can be any
 * object, but must either be a java base-type Object (Integer, String,
 * Character, etc.) or implement Comparable. An Index built on such a column
 * will sort its array of object references using FeatureComparator. Implement
 * this to perform more complex Index building.
 *
 * @author Ray Gallagher
 * @version $Id: FeatureIndex.java,v 1.5 2003/05/07 16:53:04 jmacgill Exp $
 */
public interface FeatureIndex extends CollectionListener {
    /**
     * Gets an array of references to the rows currently held by this Index.
     *
     * @return all the features referenced by this Index
     */
    Feature[] getFeatures();
}
