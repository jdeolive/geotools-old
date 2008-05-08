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

import java.io.IOException;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;


/** Provides an indexed store where to keep features
 * on behalf of a DataCache.
 * Features are likely to be stored by ID, but
 * indexation can be used to speed the lookup of features in the cache.
 * Implementation will choose the indexation method that fits best with the purpose of the cache.
 * An index instance should store only one type of feature.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public interface FeatureIndex extends FeatureSource {
    /** Store a Feature in the index.
     *
     * @param f the feature to store
     */
    public abstract void add(Feature f);

    /** Get a feature by its ID.
     *
     * @param featureID the id of the feature to retrieve.
     * @return
     */
    public abstract Feature get(String featureID);

    /** Delete a feature from the index.
     *
     * @param featureID the id of the feature to remove.
     */
    public abstract void remove(String featureID);

    /** Delete all features from the index.
     *
     */
    public abstract void clear();

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public abstract FeatureCollection getFeatures(Query q)
        throws IOException;

    /** Return a FeatureSource from where to get the features yielded by query q.
     *
     * @param q the query defining the view, ie a selection of the features in the index.
     * @return
     */
    public abstract FeatureSource getView(Query q) throws SchemaException;
}
