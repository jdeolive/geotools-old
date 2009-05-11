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

import org.geotools.data.DataStore;

/**
 * @author Christian Müller Finder for datastores stored by name and workspace
 */
public interface DataStoreLookup {

    /**
     * @param source
     *            , any info needed for initializing
     * 
     */
    public void initialize(Object source);

    /**
     * @param name
     *            of the datastore
     * @return the datastore object or null
     */
    public DataStore getDataStoreFor(String name);

    /**
     * @param namespace
     *            namespace of datastore
     * @param name
     *            name of the datastore
     * @return the datastore object or null
     */
    public DataStore getDataStoreFor(String namespace, String name);
}
