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
 * An Index is built up around a FeatureCollection, using one of the 
 * attributes in the FeatureCollection as a comparable reference. 
 * An object in a column can be any
 * object, but must either be a java base-type Object (Integer, String,
 * Character, etc.) or implement Comparable. An Index built on such a column
 * will sort its array of object references using FeatureComparator. Implement
 * this to perform more complex Index building.
 *
 * @author Ray Gallagher
 * @author Ian Schneider
 * @version $Id: FeatureIndex.java,v 1.6 2003/07/17 07:09:52 ianschneider Exp $
 */
public interface FeatureIndex extends CollectionListener {
    /** Gets an "in order" Iterator of the Features as indexed.
     * @return An Iterator of the Features within this index.
     */
    java.util.Iterator getFeatures();
    
    /** Find all the Features within this index using a key.
     * @return A FeatureCollection containing the matches. May be empty.
     * @throws IllegalArgumentException If the key is incompatable with this index.
     * @param key A key to look up the Features with.
     */    
    FeatureCollection find(Object key) throws IllegalArgumentException;
    
    /** Find the first Feature using the given key.
     * @return A Feature, or null if none is found.
     * @throws IllegalArgumentException If the key is incompatable with this index.
     * @param key A key to look up the Feature with.
     */    
    Feature findFirst(Object key) throws IllegalArgumentException;
}
