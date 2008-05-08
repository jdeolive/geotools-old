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

import org.opengis.filter.Filter;
import org.geotools.data.FeatureStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;


public interface FeatureCache extends FeatureStore {
    public static final int SPATIAL_RESTRICTION_CACHED = 0;
    public static final int SPATIAL_RESTRICTION_MISSING = 1;
    public static final int OTHER_RESTRICTIONS = 2;

    public Filter[] splitFilter(Filter f);

    public void clear();

    public void evict();

    public Feature get(String id) throws FeatureCacheException;

    public void put(Feature f);

    public void putAll(FeatureCollection fc, Filter f);

    public int size();

    public Feature remove(String id);
}
