/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.caching.firstdraft;

import java.util.Collection;
import org.geotools.feature.Feature;


/** This is the interface for the FeatureIndex backend,
 * when FeatureIndex uses non-clustered index (ie data and data index are separate).
 *
 *  It offers a generic contract,
 *  in order to allow different implementation
 *  on how the data is actually stored
 *  (eg. in memory, in another DataStore, on disk, in a database).
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public interface InternalStore {
    /** Test if a feature is already in the store.
     *
     * @param feature a feature
     * @return true if this feature is already stored.
     */
    public abstract boolean contains(Feature feature);

    /** Test if a feature is already in the store.
     *
     * @param featureId the Id of a feature
     * @return true if this feature is already stored.
     */
    public abstract boolean contains(String featureId);

    /** Store a feature.
     *
     * @param f a feature
     */
    public abstract void put(Feature f);

    /** Returns the feature identified by id.
     *
     * @param featureId the id of the feature to return.
     * @return the feature, or null if the feature does not exist in store.
     *
     * @task should we not return null ?
     * what to return if feature does not existe in store ?
     */
    public abstract Feature get(String featureId);

    /** Returns all the features in the store as a Collection.
     *
     * @return all the features in the store
     *
     * @task should we rather return a FeatureCollection ?
     */
    public abstract Collection getAll();

    /** Empties the store.
     *
     */
    public abstract void clear();

    /** Removes the feature identified by id.
     *
     * @param featureId the id of the feature to remove from store.
     *
     */
    public abstract void remove(String featureId);
}
