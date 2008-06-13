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
import org.geotools.caching.firstdraft.InternalStore;
import org.geotools.feature.Feature;


/** Simplest implementation of InternalStore, using a HashMap as storage.
 * Does implement cache size limit.
 * Does not handle oveflow.
 *
 * Used only for testing purpose.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class SimpleHashMapInternalStore implements InternalStore {
    private final HashMap buffer = new HashMap();

    public void clear() {
        buffer.clear();
    }

    public boolean contains(final Feature f) {
        return contains(f.getID());
    }

    public Feature get(final String featureId) {
        return (Feature) buffer.get(featureId);
    }

    public Collection getAll() {
        return buffer.values();
    }

    public void put(final Feature f) {
        buffer.put(f.getID(), f);
    }

    public void remove(final String featureId) {
        buffer.remove(featureId);
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#contains(java.lang.String)
     */
    public boolean contains(String featureId) {
        return buffer.containsKey(featureId);
    }
}
