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
package org.geotools.data.mysql;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.jdbc.ConnectionPool;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Map;


/**
 * Creates a PostgisDataStore baed on the correct params.
 * 
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/
 * in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class MySQLDataStoreFactory implements org.geotools.data.DataStoreFactorySpi {
    /** Creates PostGIS-specific JDBC driver class. */
    private static final String DRIVER_CLASS = "org.postgresql.Driver";

    /** Param, package visibiity for JUnit tests */
    static final Param DBTYPE = new Param("dbtype", String.class,
            "must be 'mysql'", true, "mysql");

    /** Param, package visibiity for JUnit tests */
    static final Param HOST = new Param("host", String.class,
            "mysql host machine", true, "localhost");

    /** Param, package visibiity for JUnit tests */
    static final Param PORT = new Param("port", Integer.class,
            "mysql connection port", true, new Integer(3306));

    /** Param, package visibiity for JUnit tests */
    static final Param DATABASE = new Param("database", String.class,
            "mysql database");

    /** Param, package visibiity for JUnit tests */
    static final Param USER = new Param("user", String.class,
            "user name to login as");

    /** Param, package visibiity for JUnit tests */
    static final Param PASSWD = new Param("passwd", String.class,
            "password used to login", false);

    

    /**
     * Creates a new instance of PostgisDataStoreFactory
     */
    public MySQLDataStoreFactory() {
    }

    /**
     * Checks to see if all the postgis params are there.
     * 
     * <p>
     * Should have:
     * </p>
     * 
     * <ul>
     * <li>
     * dbtype: equal to postgis
     * </li>
     * <li>
     * host
     * </li>
     * <li>
     * user
     * </li>
     * <li>
     * passwd
     * </li>
     * <li>
     * database
     * </li>
     * <li>
     * charset
     * </li>
     * </ul>
     * 
     *
     * @param params Set of parameters needed for a postgis data store.
     *
     * @return <code>true</code> if dbtype equals postgis, and contains keys
     *         for host, user, passwd, and database.
     */
   public boolean canProcess(Map params) {
        Object value;

        if (!params.containsKey("dbtype")) {
            return false;
        }

        if (!((String) params.get("dbtype")).equalsIgnoreCase("mysql")) {
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
     * @throws IOException See DataSourceException
     * @throws DataSourceException Thrown if there were any problems creating
     *         or connecting the datasource.
     */
   public DataStore createDataStore(Map params) throws IOException {
        String host = (String) HOST.lookUp(params);
        String user = (String) USER.lookUp(params);
        String passwd = (String) PASSWD.lookUp(params);
        Integer port = (Integer) PORT.lookUp(params);
        String database = (String) DATABASE.lookUp(params);
        //Charset charSet = (Charset) CHARSET.lookUp(params);
        //String namespace = (String) NAMESPACE.lookUp(params);
        
        if (!canProcess(params)) {
            return null;
        }
        try{
            return MySQLDataStore.getInstance(host, port, database, user, passwd);
        } catch (SQLException e) {
            throw new DataSourceException("Could not create connection", e);
        }
    }

    /**
     * Postgis cannot create a new database.
     *
     * @param params
     *
     * @return
     *
     * @throws IOException See UnsupportedOperationException
     * @throws UnsupportedOperationException Cannot create new database
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException(
            "MySQL cannot create a new Database");
    }

    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    public String getDescription() {
        return "MySQL (spatial) database";
    }

    /**
     * Describe parameters.
     *
     * @return
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] {
            DBTYPE, HOST, PORT, DATABASE, USER, PASSWD
        };
    }
}
