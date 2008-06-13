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

import org.opengis.filter.Filter;
import org.geotools.data.Query;


/** Records information about queries,
 *  in order to be able to tell to a DataCache if the data requested
 *  are already known, or else what data should be asked for to the source DataStore.
 *
 *  Actual implementation should allow query-specific optimization.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public interface QueryTracker {
    /** Take notice that the data for the query q have been retrieved once.
     * So they should be in the cache.
     *
     * @param q the Query to register
     */
    public abstract void register(Query q);

    /** Take notice that the data for the Filter f have been retrieved once.
     * So they should be in the cache.
     *
     * @param f the Filter to register
     */
    public abstract void register(Filter f);

    /** Restrict the Filter f to a new filter
     * that will yield only the complementary set of data the cache doesn't hold.
     * If all data are known, implementation should return
     * Filter.EXCLUDE which is a filter that yields nothing.
     *
     * @param f the filter to restrict
     * @return a restricted filter, or otherwise the input filter
     */
    public abstract Filter match(Filter f);

    /** Restrict the Query q to a new query
     * that will yield only the complementary set of data the cache doesn't hold.
     * If all data are known, implementation should return
     * new DefaultQuery(q.getTypeName(), Filter.EXCLUDE) which is a query that yields nothing.
     *
     * @param q the query to restrict
     * @return a restricted query, or otherwise the input query
     */
    public abstract Query match(Query q);

    /** Forget about the query q.
     * When this query will be issued again, or a related query, the cache will have to get data from the source DataStore.
     * This is used when the cache has reached its maximum capacity,
     * and needs to make room for new features.
     * For example, the input query can be the extent (bbox) of the deleted feature in the cache.
     *
     * @param q the query to forget about.
     */
    public abstract void unregister(Query q);

    /** Forget about the filter f.
     * When this filter will be used again, or a related query, the cache will have to get data from the source DataStore.
     * This is used when the cache has reached its maximum capacity,
     * and needs to make room for new features.
     * For example, the input filter can be the extent (bbox) of the deleted feature in the cache.
     *
     * @param q the query to forget about.
     */
    public abstract void unregister(Filter f);

    /** Forget every query ever registered.
     * Blank mind for new days !
     *
     */
    public abstract void clear();
}
