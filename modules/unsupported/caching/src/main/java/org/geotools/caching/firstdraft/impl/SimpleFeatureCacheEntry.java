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

import org.geotools.caching.firstdraft.CacheEntry;
import org.geotools.feature.Feature;


public class SimpleFeatureCacheEntry implements CacheEntry {
    final private Feature feature;
    private int hits;
    private long creationTime;
    private long lastAccessTime;

    public SimpleFeatureCacheEntry(Feature f) {
        this.feature = f;
        hits = 0;
        creationTime = System.currentTimeMillis();
        lastAccessTime = creationTime;
    }

    public long getCost() {
        return -1;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getExpirationTime() {
        return -1;
    }

    public int getHits() {
        return hits;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getLastUpdateTime() {
        return -1;
    }

    public long getVersion() {
        return -1;
    }

    public boolean isValid() {
        return true;
    }

    public Object getKey() {
        return feature.getID();
    }

    public Object getValue() {
        hits++;
        lastAccessTime = System.currentTimeMillis();

        return feature;
    }

    public Object setValue(Object arg0) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o) {
        return feature.equals(o);
    }

    public int hashCode() {
        return feature.hashCode();
    }
}
