/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
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
package org.geotools.data.shapefile;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of the DataStore service provider interface for Shapefiles.
 *
 * @author Chris Holmes, TOPP
 * @version $Id: ShapefileDataStoreFactory.java,v 1.7 2004/05/06 21:50:10 ianschneider Exp $
 */
public class ShapefileDataStoreFactory
    implements org.geotools.data.DataStoreFactorySpi {
    private static final Param URLP = new Param("url", URL.class,
        "url to a .shp file");
    private static final Param MEMORY_MAPPED = new Param("memory mapped buffer",
        Boolean.class, "enable/disable the use of memory-mapped io",false);

    /**
     * Takes a list of params which describes how to access a restore and
     * determins if it can be read by the Shapefile Datastore.
     *
     * @param params A set of params describing the location of a restore.
     *        Files should be pointed to by a 'url' param.
     *
     * @return true iff params contains a url param which points to a file
     *         ending in shp
     */
    public boolean canProcess(Map params) {
        boolean accept = false;
        if (params.containsKey(URLP.key)) {
            try {
                URL url = (URL) URLP.lookUp(params);
                accept = url.getFile().toUpperCase().endsWith("SHP");
            } catch (IOException ioe) {
                // yes, I am eating this
            }
        }
        return accept;
    }

    /**
     * Returns an instance of a ShapeFileDataStore iff the restore pointed to
     * by params actualy is a Shapefile.
     *
     * @param params A param list with information on the location of a
     *        restore.  For shapefiles this should contain a 'url' param which
     *        points to a file which ends in shp.
     *
     * @return DataStore A ShapefileDatastore
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException Thrown if the datastore which is created
     *         cannot be attached to the restore specified in params.
     */
    public DataStore createDataStore(Map params) throws IOException {
        DataStore ds = null;

        URL url = null;
        try {
            url = (URL) URLP.lookUp(params);
            Boolean mm = (Boolean) MEMORY_MAPPED.lookUp(params);
            if (mm == null)
                mm = Boolean.TRUE;
            ds = new ShapefileDataStore(url,mm.booleanValue());
        } catch (MalformedURLException mue) {
            throw new DataSourceException("Unable to attatch datastore to "
                + url, mue);
        } 
 
        return ds;
    }

    /**
     * Not implemented yet.
     *
     * @param params
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     * @throws UnsupportedOperationException
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Describes the type of data the datastore returned by this factory works
     * with.
     *
     * @return String a human readable description of the type of restore
     *         supported by this datastore.
     */
    public String getDescription() {
        return "ESRI(tm) Shapefiles (*.shp)";
    }

    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  This datastore just
     * returns true for now.
     *
     * @return <tt>true</tt> if and only if this factory is available to create
     *         DataStores.
     *
     * @task REVISIT: I'm just adding this method to compile, maintainer should
     *       revisit to check for any libraries that may be necessary for
     *       datastore creations.
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * Describe parameters.
     *
     * @return
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { URLP, MEMORY_MAPPED };
    }
}
