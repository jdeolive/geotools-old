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
 * PostgisDataSourceFactory.java
 *
 * Created on March 5, 2003, 10:59 AM
 */
package org.geotools.data.postgis;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.ConnectionPool;
import java.sql.SQLException;
import java.util.Map;


/**
 * Creates a PostgisDataSource with the correct params.  This factory should be
 * registered in the META-INF/ folder, under services/ in the
 * DataSourceFactorySpi file.  This will allow DataSourceFinder to create
 * PostgisDataSources with the correct params.
 *
 * @author James Macgill, PSU
 * @version $Id: PostgisDataSourceFactory.java,v 1.5 2003/10/31 18:53:53 cholmesny Exp $
 */
public class PostgisDataSourceFactory
    implements org.geotools.data.DataSourceFactorySpi {

     /** Creates PostGIS-specific JDBC driver class. */
    private static final String DRIVER_CLASS = "org.postgresql.Driver";

    /**
     * Creates a new instance of GMLDataSourceFactory
     */
    public PostgisDataSourceFactory() {
	//don't do this here - not sure if it's necessary.
	// Class.forName(DRIVER_CLASS);
    }

    /**
     * Checks to see if all the postgis params are there.  Should have  dbtype
     * equal to postgis, as well as host, user, passwd, database, and table.
     *
     * @param params The full set of information needed to construct a live
     *        data source.
     *
     * @return <code>true</code> if dbtype equals postgis, and contains keys
     *         for host, user, passwd, database, and table.
     */
    public boolean canProcess(Map params) {
        if (!params.containsKey("dbtype")) {
            return false;
        }

        if (!((String) params.get("dbtype")).equalsIgnoreCase("postgis")) {
            return false;
        }

        if (!params.containsKey("host")) {
            return false;
        }

        if (!params.containsKey("user")) {
            return false;
        }

        if (!params.containsKey("passwd")) {
            return false;
        }

        if (!params.containsKey("database")) {
            return false;
        }

        if (!params.containsKey("table")) {
            return false;
        }

        return true;
    }

    /**
     * Construct a live data source using the params specifed.
     *
     * @param params The full set of information needed to construct a live
     *        data source.  Should have  dbtype equal to postgis, as well as
     *        host, user, passwd, database, and table.
     *
     * @return The created DataSource, this may be null if the required
     *         resource was not found or if insufficent parameters were given.
     *         Note that canProcess() should have returned false if the
     *         problem is to do with insuficent parameters.
     *
     * @throws DataSourceException Thrown if there were any problems creating
     *         or connecting the datasource.
     */
    public DataSource createDataSource(Map params) throws DataSourceException {
        if (!canProcess(params)) {
            return null;
        }

        String host = (String) params.get("host");
        String user = (String) params.get("user");
        String passwd = (String) params.get("passwd");
        String port = (String) params.get("port");
        String database = (String) params.get("database");
        String table = (String) params.get("table");
        String charSet = (String) params.get("charset");
	String useStrict = (String) params.get("strictbbox");
	String useGeos = (String) params.get("usegeos");

        PostgisConnectionFactory connFact = new PostgisConnectionFactory(host,
                port, database);

        try {
            connFact.setLogin(user, passwd);
            connFact.setCharSet(charSet);
	    
	    ConnectionPool pool = connFact.getConnectionPool();
            PostgisDataSource pgds = new PostgisDataSource(pool, table);
            if (useStrict != null && useStrict.equals("true")){
		pgds.setEncodeBbox(false);
	    }
	    if (useGeos != null && useGeos.equals("true")){
		pgds.setUseGeosEncoder(true);
	    }
	    return pgds;
        } catch (SQLException sqle) {
            throw new DataSourceException("Unable to connect to database", sqle);
        }
    }

    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    public String getDescription() {
        return "PostGIS spatial database";
    }
}
