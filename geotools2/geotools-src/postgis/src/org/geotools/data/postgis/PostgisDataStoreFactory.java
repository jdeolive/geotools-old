/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.jdbc.ConnectionPool;

/**
 * Creates a PostgisDataStore baed on the correct params.
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/
 * in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class PostgisDataStoreFactory
    implements org.geotools.data.DataStoreFactorySpi {

     /** Creates PostGIS-specific JDBC driver class. */
    private static final String DRIVER_CLASS = "org.postgresql.Driver";

    /**
     * Creates a new instance of PostgisDataStoreFactory
     */
    public PostgisDataStoreFactory() {
    }

    /**
     * Checks to see if all the postgis params are there.
     * <p>
     * Should have:
     * </p>
     * <ul>
     * <li>dbtype: equal to postgis</li>
     * <li>host</li>
     * <li>user</li>
     * <li>passwd</li>
     * <li>database</li>
     * <li>charset</li>
     * </ul>
     * @param params Set of parameters needed for a postgis data store.
     * @return <code>true</code> if dbtype equals postgis, and contains keys
     *         for host, user, passwd, and database.
     */
    public boolean canProcess(Map params) {
        Object value;
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

        if (!params.containsKey("database")) {
            return false;
        }
        
        return true;
    }

    /**
     * Construct a postgis data store using the params.
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
    public DataStore createDataStore(Map params) throws IOException {
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
        String namespace = (String) params.get("namespace");

        PostgisConnectionFactory connFact = new PostgisConnectionFactory(host,
                port, database);

        connFact.setLogin(user, passwd);
        connFact.setCharSet(charSet);

        ConnectionPool pool;
        try {
            pool = connFact.getConnectionPool();
        } catch (SQLException e) {
            throw new DataSourceException("Could not create connection", e );
        }
        if( namespace != null){
            return new PostgisDataStore(pool, namespace );                
        }
        else {
            return new PostgisDataStore(pool);                
        }                        
    }
    /**
     * Postgis cannot create a new database.
     * @param params
     * @return
     * @throws UnsupportedOperationException Cannot create new database
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException("Postgis cannot create a new Database");
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

    /**
     * Describe parameters.
     * 
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     * @return
     */
    public Param[] getParametersInfo() {
        return new Param[]{
            new Param("dbtype"),
            new Param("host"),
            new Param("port",String.class,"database connection port"),
            new Param("database"),                        
            new Param("user"),
            new Param("passwd", String.class,"passwd for user (optional)", false),                      
            new Param("charset",String.class,"character set (optional)", false),
            new Param("namespace",String.class,"namespace (optional)",false)            
        };
    }    
}
