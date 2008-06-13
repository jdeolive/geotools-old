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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.geotools.caching.firstdraft.InternalStore;
import org.geotools.feature.Feature;


/** In-memory implementation of InternalStore, using a HashMap storage.
 *
 * Overflow is directed to another InternalStore, so we can chain InternalStore.
 * (eg. Memory -> Disk -> ...)
 *
 * When maximum capacity is reached, randomly make room for new features,
 * and write removed features to overflow store.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class HashMapInternalStore implements InternalStore {
    private final InternalStore overflow;
    private final int capacity;
    private final HashMap buffer;
    private int count = 0;
    private final Random rand = new Random();

    /**
     * @param capacity
     * @param overflow
     */
    public HashMapInternalStore(int capacity, InternalStore overflow) {
        this.overflow = overflow;
        this.capacity = capacity;
        this.buffer = new HashMap();
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#clear()
     */
    public void clear() {
        buffer.clear();
        count = 0;
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#contains(org.geotools.feature.Feature)
     */
    public boolean contains(final Feature f) {
        return buffer.containsKey(f.getID());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.caching.InternalStore#contains(java.lang.String)
     */
    public boolean contains(String featureId) {
        return buffer.containsKey(featureId);
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#get(java.lang.String)
     *
     * TODO don't return null.
     *
     */
    public Feature get(final String featureId) {
        Feature ret = null;

        if (buffer.containsKey(featureId)) {
            ret = (Feature) buffer.get(featureId);
        } else {
            if (overflow != null) {
                ret = overflow.get(featureId);

                if (ret != null) {
                    put(ret);
                }
            }
        }

        return ret;
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#getAll()
     */
    public Collection getAll() {
        return buffer.values();
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#put(org.geotools.feature.Feature)
     */
    public void put(final Feature f) {
        // assert capacity > count ;
        if (count == capacity) {
            evict();
        }

        buffer.put(f.getID(), f);
        count++;
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#remove(java.lang.String)
     */
    public void remove(final String featureId) {
        buffer.remove(featureId);
        count--;
    }

    /** Random eviction strategy.
     * Remove an arbitrary feature from store,
     * and put removed in features in overflow InternalStore
     *
     */
    protected void evict() {
        int entry = rand.nextInt(buffer.size());
        Iterator it = buffer.keySet().iterator();

        for (int i = 0; i < (entry - 1); i++) {
            it.next();
        }

        String id = (String) it.next();

        if (overflow != null) {
            overflow.put(get(id));
        }

        remove(id);
    }

    /* class Entry {
       static final short DIRTY = 0;
       static final short FROM_SOURCE = 1;
       static final short FROM_CACHE = 2;
       Feature f;
       short state = 1;
       public Entry(Feature f) {
           this.f = f;
       }
       } */
}
