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
package org.geotools.caching.firstdraft.impl;

import org.geotools.caching.firstdraft.DataCache;
import org.geotools.caching.firstdraft.FeatureIndex;
import org.geotools.caching.firstdraft.QueryTracker;
import org.geotools.data.DataStore;
import org.geotools.feature.FeatureType;


public class CacheInternalEngine {
    private final FeatureType type;
    private final QueryTracker tracker;
    private final FeatureIndex index;

    public CacheInternalEngine(DataCache parent, FeatureType t) {
        this.type = t;
        this.tracker = new SpatialQueryTracker();
        this.index = new MemoryFeatureIndex(parent, t, 100);
    }

    public FeatureType getType() {
        return type;
    }

    public QueryTracker getTracker() {
        return tracker;
    }

    public FeatureIndex getIndex() {
        return index;
    }
}
