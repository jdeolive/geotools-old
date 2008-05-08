/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import org.geotools.data.DataStore;


/** a DataCache is a DataStore that takes its features from another DataStore,
 * and tries to remember them in order to leverage subsequent related queries.
 * This is more of a marker interface, as a DataCache should behave in the same manner
 * as the source DataStore, but hopefully faster ...
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public interface DataCache extends DataStore {
    /** Reset the cache,
     *  so it does not remember any of the features that were stored.
     */
    public abstract void clear();

    /** Cause the cache to write back any of dirty features it may contain.
     *  Could be better in a subinterface for async read-write caches.
     */
    public abstract void flush() throws IllegalStateException;

    /** Provide a simple statistic of how many times
     * the cache was queried in place of the source DataStore.
     * Probably not useful.
     *
     * @task remove this method.
     */
    public abstract long getHits();
}
