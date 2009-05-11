/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.gen;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;

/**
 * Simple Implementation of
 * 
 * @see DataStoreLookup Intended to be used by java developers
 * @author Christian Mueller
 * 
 */
public class DataStoreLookupImpl implements DataStoreLookup {

    Map<String, DataStore> map = new HashMap<String, DataStore>();

    public void clear() {
        map = new HashMap<String, DataStore>();
    }

    public void register(String name, DataStore ds) {
        register(null, name, ds);
    }

    public void register(String namespace, String name, DataStore ds) {
        map.put(keyFor(namespace, name), ds);
    }

    public DataStore getDataStoreFor(String name) {

        return getDataStoreFor(null, name);
    }

    public DataStore getDataStoreFor(String namespace, String name) {

        return map.get(keyFor(namespace, name));
    }

    private String keyFor(String workspace, String name) {

        if (workspace == null)
            return name;
        return workspace + "|" + name;
    }

    public void initialize(Object source) {

    }
}
