package org.geotools.data.mysql;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;

/**
 * Creates ConnectionPool objects for a certain MySQL database instance.
 * @author Gary Sheppard garysheppard@psu.edu
 */
public class MySQLConnectionFactory {

    private static final String MYSQL_URL_PREFIX = "jdbc:mysql://";
    private static Map _dataSources = new HashMap();
    private String _dbURL;
    private String _username = "";
    private String _password = "";
    
    /**
     * Creates a new MySQLConnectionFactory object from a MySQL database URL.  This
     * is normally of the following format:<br>
     * <br>
     * jdbc:mysql://<host>:<port>/<instance>
     * @param url the MySQL database URL
     */
    public MySQLConnectionFactory(String url) {
        _dbURL = url;
    }

    /**
     * Creates a new MySQLConnectionFactory object from a host name, port number,
     * and instance name.
     * @param host the MySQL database host
     * @param port the port number for the MySQL database
     * @param instance the MySQL database instance name
     */
    public MySQLConnectionFactory(String host, Integer port, String instance) {
        this(MYSQL_URL_PREFIX + host + ":" + port.toString() + "/" + instance);
    }
    
    /**
     * Creates a new MySQLConnectionFactory object from a host name and an instance
     * name, using the normal MySQL port number of 3306.
     * @param host the MySQL database host
     * @param instance the MySQL database instance name
     */
    public MySQLConnectionFactory(String host, String instance) {
        this(host, new Integer(3306), instance);
    }

    /**
     * Creates and returns a MySQL ConnectionPool, or gets an existing ConnectionPool
     * if one exists, based upon the username and password parameters passed to this
     * method.  This is shorthand for the following two calls:<br>
     * <br>
     * connPool.setLogin(username, password);<br>
     * connPool.getConnectionPool();<br>
     * @param username the MySQL username
     * @param password the password corresponding to <code>username</code>
     * @return a MySQL ConnectionPool object
     * @throws SQLException if an error occurs connecting to the MySQL database
     */
    public ConnectionPool getConnectionPool(String username, String password) throws SQLException {
        setLogin(username, password);
        return getConnectionPool();
    }

    /**
     * Creates and returns a MySQL ConnectionPool, or gets an existing ConnectionPool
     * if one exists, based upon the username and password set in this MySQLConnectionFactory
     * object.  Please call setLogin before calling this method, or use getConnectionPool(String, String)
     * instead.
     * @return a MySQL ConnectionPool object
     * @throws SQLException if an error occurs connecting to the DB
     */
    public ConnectionPool getConnectionPool() throws SQLException {
        String poolKey = _dbURL + _username + _password;
        MysqlConnectionPoolDataSource poolDataSource = (MysqlConnectionPoolDataSource) _dataSources.get(poolKey);

        if (poolDataSource == null) {
            poolDataSource = new MysqlConnectionPoolDataSource();
            
            poolDataSource.setURL(_dbURL);
            poolDataSource.setUser(_username);
            poolDataSource.setPassword(_password);

            _dataSources.put(poolKey, poolDataSource);
        }

        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        ConnectionPool connectionPool = manager.getConnectionPool(poolDataSource);

        return connectionPool;
    }

    /**
     * Sets the MySQL database login credentials.
     * @param username the username
     * @param password the password
     */
    public void setLogin(String username, String password) {
        _username = username;
        _password = password;
    }
    
}
