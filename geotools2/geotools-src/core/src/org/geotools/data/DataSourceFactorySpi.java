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

/**
 * Constructs a live datasource from a set of parameters. An instance of this
 * interface should exist for all data sources which want to take advantage of
 * the dynamic datasource plugin system. In addition to implementing this
 * interface datasouces should have a services file:
 * META-INF/services/org.geotools.data.DataSourceFactorySpi The file should
 * contain a single line which gives the full name of the implementing class.
 * e.g. org.geotools.data.mytype.MyTypeDataSourceFacotry The factories are
 * never called directly by users, instead the DataSourceFinder class is used.
 * The following example shows how a user might connect to a PostGIS
 * database: HashMap params = new HashMap(); params.put("dbtype", "postgis");
 * params.put("host","feathers.leeds.ac.uk"); params.put("port", "5432");
 * params.put("database","postgis_test"); params.put("user","postgis_ro");
 * params.put("passwd","postgis_ro"); params.put("table","testset");
 * DataSource ds = DataSourceFinder.getDataSource(params);
 *
 * @author jmacgill
 */
public interface DataSourceFactorySpi extends org.geotools.factory.Factory {
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
     */
    DataSource createDataSource(java.util.Map params)
        throws DataSourceException;

    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    String getDescription();

    /**
     * Test to see if this factory is suitable for processing the data pointed
     * to by the param tags. If this datasource requires a number of
     * parameters then this mehtod should check that they are all present and
     * that they are all valid. If the datasource is a file reading data
     * source then the extentions or mime types of any files specified should
     * be checked. For example, a Shapefile datasource should check that the
     * url param ends with shp, such tests should be case insensative.
     *
     * @param params The full set of information needed to construct a live
     *        data source.
     *
     * @return booean true if and only if this factory can process the resource
     *         indicated by the param set and all the required params are
     *         pressent.
     */
    boolean canProcess(java.util.Map params);
}
