/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Simple Catalog so we can try out the api.
 * 
 * <p>
 * This class intends to track the Catalog API as it provides more metadata
 * information. It is intended to be an In Memory data structure.
 * </p>
 * 
 * <p>
 * Other projectswill produce more persistent Catalog implementations.
 * GeoServer for instance will back it's Catalog implementation with XML
 * files.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class DefaultCatalog implements Catalog {
    protected Map registration = new HashMap();

    public String[] getNameSpaces() {
        return (String[]) registration.keySet().toArray( new String[ registration.size() ] );
    }

    /**
     * Registrers datastore with the provided namespace.
     *
     * @param namespace
     * @param dataStore
     *
     * @see org.geotools.data.Catalog#registerDataStore(java.lang.String,
     *      org.geotools.data.DataStore)
     */
    public synchronized void registerDataStore(String namespace,
        DataStore dataStore) {
        registration.put(namespace, dataStore);
    }

    /**
     * Finds and registers DataStore indicated by the parameters.
     * 
     * <p>
     * The provided parameters must specify a <code>namespace</code> for the
     * resulting DataStore.
     * </p>
     *
     * @param params
     *
     * @throws IOException
     */
    public void register(Map params) throws IOException {
        if (params.containsKey("namespace")) {
            throw new IOException(
                "Could not locate namespace in provided prameters");
        }

        registerDataStore((String) params.get("namespace"),
            DataStoreFinder.getDataStore(params));
    }

    /**
     * Retrieve DataStore managed by this Catalog.
     *
     * @param namespace
     *
     * @return
     *
     * @see org.geotools.data.Catalog#getDataStore(java.lang.String)
     */
    public synchronized DataStore getDataStore(String namespace) {
        return (DataStore) registration.get(namespace);
    }
}
