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
 * @version $Id: ShapefileDataStoreFactory.java,v 1.2 2004/01/07 16:10:17 ianschneider Exp $
 */
public class ShapefileDataStoreFactory
    implements org.geotools.data.DataStoreFactorySpi {

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

        if (params.containsKey("url")) {
            String url = (String) params.get("url");
            accept = url.toUpperCase().endsWith("SHP");
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
     * @throws DataSourceException Thrown if the datastore which is created
     *         cannot be attached to the restore specified in params.
     */
    public DataStore createDataStore(Map params) throws DataSourceException {
        DataStore ds = null;

        if (canProcess(params)) {
            String location = (String) params.get("url");

            try {
                ds = new ShapefileDataStore(new URL(location));
            } catch (MalformedURLException mue) {
                throw new DataSourceException("Unable to attatch datastore to "
                    + location, mue);
            }
        }

        return ds;
    }

    /**
     * Postgis cannot create a new database.
     *
     * @param params
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     * @throws UnsupportedOperationException Cannot create new database
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
     * Describe parameters.
     *
     * @return
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { new Param("url", String.class, "url to a .shp file") };
    }
}
