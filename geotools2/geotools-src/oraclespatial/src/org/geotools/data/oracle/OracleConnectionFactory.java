/* *    Geotools2 - OpenSource mapping toolkit *    http://geotools.org *    (C) 2002, Geotools Project Managment Committee (PMC) * *    This library is free software; you can redistribute it and/or *    modify it under the terms of the GNU Lesser General Public *    License as published by the Free Software Foundation; *    version 2.1 of the License. * *    This library is distributed in the hope that it will be useful, *    but WITHOUT ANY WARRANTY; without even the implied warranty of *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU *    Lesser General Public License for more details. * */
package org.geotools.data.oracle;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import org.geotools.data.jdbc.ConnectionPool;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides javax.sql.DataSource wrapper around an OracleConnection object.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: OracleConnectionFactory.java,v 1.4 2003/08/08 07:37:18 seangeo Exp $
 */
public class OracleConnectionFactory {
    /** The prefix of an Oracle JDBC url */
    private static final String JDBC_PATH = "jdbc:oracle:thin:@";
    private static Map connectionPools = Collections.synchronizedMap(new HashMap());

    /** The url to the DB */
    private String dbUrl;

    /** The username to login with */
    private String username = "";

    /** The password to login with */
    private String passwd = "";

    /**
     * Creates a new OracleConnection object that wraps a oracle.jdbc.driver.OracleConnection.
     *
     * @param host The host to connect to.
     * @param port The port number on the host
     * @param instance The instance name on the host
     */
    public OracleConnectionFactory(String host, String port, String instance) {
        dbUrl = JDBC_PATH + host + ":" + port + ":" + instance;
    }

    /**
     * Creates the real OracleConnection. Logs in to the Oracle Database and creates the
     * OracleConnection object.
     *
     * @param user The user name.
     * @param pass The password
     *
     * @return The real OracleConnection object.
     *
     * @throws SQLException If an error occurs connecting to the DB.
     */
    public ConnectionPool getConnectionPool(String user, String pass)
        throws SQLException {
        ConnectionPool pool = null;
        String poolKey = dbUrl + user + pass;

        if ((pool = (ConnectionPool) connectionPools.get(poolKey)) == null) {
            OracleConnectionPoolDataSource cpDataSource = new OracleConnectionPoolDataSource();

            cpDataSource.setURL(dbUrl);
            cpDataSource.setUser(user);
            cpDataSource.setPassword(pass);

            pool = new ConnectionPool(cpDataSource);
            connectionPools.put(poolKey, pool);
        }

        return pool;
    }

    /**
     * Creates the real OracleConnection.  Logs into the database using the credentials provided by
     * setLogin
     *
     * @return The oracle connection to the data base.
     *
     * @throws SQLException If an error occurs connecting to the DB.
     */
    public ConnectionPool getConnectionPool() throws SQLException {
        return getConnectionPool(username, passwd);
    }

    /**
     * Sets the login credentials.
     *
     * @param user The username
     * @param pass The password
     */
    public void setLogin(String user, String pass) {
        this.username = user;

        this.passwd = pass;
    }
}
