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
/*
 * GMLDataSourceFactory.java
 *
 * Created on March 4, 2003, 3:44 PM
 */
package org.geotools.data.gml;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 */
public class GMLDataSourceFactory
    implements org.geotools.data.DataSourceFactorySpi {
    /**
     * Creates a new instance of GMLDataSourceFactory
     */
    public GMLDataSourceFactory() {
    }

    /**
     * Checks to see the url ends with gml.
     *
     * @param params The full set of information needed to construct a live
     *        data source.
     *
     * @return <code>true</code> if the url param is there and it ends with
     *         gml.
     */
    public boolean canProcess(Map params) {
        if (!params.containsKey("url")) {
            return false;
        }

        String url = (String) params.get("url");

        if (!url.toUpperCase().endsWith("GML")) {
            return false;
        }

        return true;
    }

    /**
     * Construct a live data source using the params specifed.
     *
     * @param params The full set of information needed to construct a live
     *        data source. Typical key values for the map include: url -
     *        location of a resource, used by file reading datasources. dbtype
     *        - the type of the database to connect to, e.g. postgis, mysql
     *
     * @return The created DataSource, this may be null if the required
     *         resource was not found or if insufficent parameters were given.
     *         Note that canProcess() should have returned false if the
     *         problem is to do with insuficent parameters.
     *
     * @throws DataSourceException Thrown if there were any problems creating
     *         or connecting the datasource.
     *
     * @task REVISIT: make this comment relevant to gml.
     */
    public DataSource createDataSource(Map params) throws DataSourceException {
        if (!canProcess(params)) {
            return null;
        }

        String location = (String) params.get("url");
        GMLDataSource ds = new GMLDataSource(location);

        return ds;
    }

    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    public String getDescription() {
        return "Geographic Markup Language (GML) files version 2.x";
    }
}
