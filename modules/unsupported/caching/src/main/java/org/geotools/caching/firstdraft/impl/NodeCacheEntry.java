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
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;


public class NodeCacheEntry implements CacheEntry {
    private final INode node;
    private final Integer key;
    private int hits;
    private long creationTime;
    private long lastAccessTime;

    public NodeCacheEntry(INode node) {
        this.node = node;
        key = new Integer(node.getIdentifier());
        hits = 0;
        creationTime = System.currentTimeMillis();
        lastAccessTime = creationTime;
    }

    public long getCost() {
        // TODO Auto-generated method stub
        return -1;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getExpirationTime() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return -1;
    }

    public boolean isValid() {
        // TODO Auto-generated method stub
        return true;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return node;
    }

    public Object setValue(Object arg0) {
        throw new UnsupportedOperationException();
    }

    public void hit() {
        hits++;
        lastAccessTime = System.currentTimeMillis();
    }
}
