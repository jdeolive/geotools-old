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
package org.geotools.data.gtopo30;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


/**
 * Implementation of the DataSource service provider interface for GTopo30 files.
 *
 * @author aaime
 */
public class GTopo30DataSourceFactory implements org.geotools.data.DataSourceFactorySpi {
    /**
     * Takes a list of params which describes how to access a resource and determins if it can be
     * read by the ArgGrid Datasource.
     *
     * @param params A set of params describing the location of a resource. Files should be pointed
     *        to by a 'url' param.
     *
     * @return true iff params contains a url param which points to a file ending in asc
     */
    public boolean canProcess(Map params) {
        boolean accept = false;

        try {
            // Use the datasource itself to see if the url is supported
            if (params.containsKey("url")) {
                String url = (String) params.get("url");
                GTopo30DataSource ds = new GTopo30DataSource(new URL(url));
                accept = true;
            }
        } catch (Exception e) {
            accept = false;
        }

        return accept;
    }

    /**
     * Returns an instance of a ArcGridDataSource iff the resource pointed to by params actualy is
     * a GTOPO30 file.
     *
     * @param params A param list with information on the location of a resource.  For GTOPO30
     *        files this should contain a 'url' param which points to a file which ends in dem, stx, hdr, src, shc.
     *
     * @return DataSource An ArcGridDataSource
     *
     * @throws DataSourceException Thrown if the datasource which is created cannot be attached to
     *         the resource specified in params.
     */
    public DataSource createDataSource(Map params) throws DataSourceException {
        DataSource ds = null;

        if (canProcess(params)) {
            String location = (String) params.get("url");

            try {
                ds = new GTopo30DataSource(new URL(location));
            } catch (MalformedURLException mue) {
                throw new DataSourceException("Unable to attatch datasource to " + location, mue);
            }
        }

        return ds;
    }

    /**
     * Describes the type of data the datasource returned by this factory works with.
     *
     * @return String a human readable description of the type of resource supported by this
     *         datasource.
     */
    public String getDescription() {
        return "GTOPO30 global 30' DEM (*.dem)";
    }
}
