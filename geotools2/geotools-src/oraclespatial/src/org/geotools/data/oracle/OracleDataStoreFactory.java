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
package org.geotools.data.oracle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.jdbc.ConnectionPool;

/**
 * Creates a PostgisDataStore baed on the correct params.
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/
 * in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 */
public class OracleDataStoreFactory
    implements DataStoreFactorySpi {
     
    /**
     * Creates a new instance of OracleDataStoreFactory
     */
    public OracleDataStoreFactory() {
    }

    /**
     * Determines whether DataStore created by this factory can process the
     * parameters.
     * <p>
     * Required Parameters are:
     * </p>
     * <ul>
     * <li>
     * <code>dbtype</code> - must equal "oracle"
     * </li>
     * <li>
     * <code>host</code>
     * </li>
     * <li>
     * <code>port</code>
     * </li>
     * <li>
     * <code>user</code>
     * </li>
     * <li>
     * <code>passwd</code>
     * </li>
     * <li>
     * <code>instance</code>
     * </li>     
     * </ul>
     * 
     * <p>
     * There are no defaults since each parameter must be explicitly defined by the user, or
     * another DataSourceFactorySpi should be used. This behaviour is defined in the
     * DataStoreFactorySpi contract.
     * </p>
     *
     * @param params The parameter to check.
     *
     * @return True if all the required parameters are supplied.
     */
    public boolean canProcess(Map params) {
        //J-
        return params.containsKey("dbtype") 
            && params.get("dbtype").equals("oracle") 
            && params.containsKey("host") 
            && params.containsKey("port") 
            && params.containsKey("user")
            && params.containsKey("passwd") 
            && params.containsKey("instance");
        //J+
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

        /* There are no defaults here. Calling canProcess verifies that
         * all these variables exist.
         */
        String host = (String) params.get("host");
        String port = (String) params.get("port");
        String instance = (String) params.get("instance");
        String user = (String) params.get("user");
        String passwd = (String) params.get("passwd");
        String schema = (String) params.get("schema");
        String namespace = (String) params.get("namespace");

        try {
            OracleConnectionFactory ocFactory = new OracleConnectionFactory(host, port, instance);
            ocFactory.setLogin(user, passwd);
            ConnectionPool pool = ocFactory.getConnectionPool();
            
            
            OracleDataStore dataStore = new OracleDataStore(pool, schema, new HashMap());

            return dataStore;
        } catch (SQLException ex) {
            throw new DataSourceException("Error creating oracle DataSource", ex);
        }                                                
    }
    /**
     * Oracle cannot create a new database.
     * @param params
     * @return
     * @throws UnsupportedOperationException Cannot create new database
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException("Oracle cannot create a new Database");
    }

    /**
     * Describe the nature of the datastore constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    public String getDescription() {
        return "Oracle Spatial Database";
    }

    /**
     * Describe parameters.
     * 
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     * @return
     */
    public Param[] getParametersInfo() {
        return new Param[]{
            new Param("oracle", String.class, "This must be 'oracle'.", true),            
            new Param("host", String.class, "The host name of the server.", true),
            new Param("port", String.class, "The port oracle is running on.", true),
            new Param("user", String.class, "The user name to log in with.", true),
            new Param("passwd", String.class, "The password.", true),
            new Param("instance", String.class, "The name of the Oracle instance to connect to.", true),   
            new Param("schema", String.class, "The schema name to narrow down the exposed tables.", false),
            new Param("namespace", String.class, "The namespace to give the DataStore.", false)
        };                
    }    
}