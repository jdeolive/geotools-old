/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.data.mysql;

import java.io.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Category;


/**
 * Shell for JDBC transactions of all types.
 *
 * This provides a base class to the database transactional classes.  
 *
 * @version $Id: MysqlConnection.java,v 1.1 2002/08/04 12:57:40 jmacgill Exp $
 * @author Rob Hranac, Vision for New York
 * @author Chris Holmes, Vision for New York
 */
public class MysqlConnection implements javax.sql.DataSource {


    private static Category _log = Category.getInstance(MysqlConnection.class.getName());
    
    /** The Mysql-specific JDBC driver class using mm.mysql */ 
    private static final String MYSQL_DRIVER_CLASS = "org.gjt.mm.mysql.Driver";
    
    /** The Mysql-specific JDBC driver path. */ 
    private static final String MYSQL_DRIVER_PATH = "jdbc:mysql";


    private String host;

    private String port;

    private String dbName;

    private String user = null;

    private String password = null;
    
    /**
     * Constructor with all internal database driver classes, driver paths
     * and database types.
     *
     * @param host The driver class; should be passed from the
     * database-specific subclass.
     * @param port The driver path; should be passed from the
     * database-specific subclass.
     * @param dbName The database type; should be passed from the
     * database-specific subclass.
     */ 
    public MysqlConnection ( String host, String port, String dbName) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
    }
    
    
    /**
     * Sets the user and password strings of the login to be used when
     * connecting to the Mysql database.
     *
     * @param user The string of the user to connect to the database with.
     * @param password The string of the password of user.
     */ 
    public void setLogin( String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getLoginUser(){
	return user;
    }

    public String getLoginPassword(){
	return password;
    }

    /**
     * Retrieves a connection to the Mysql database, using the current
     * user and password;
     * 
     */ 
    public Connection getConnection()
        throws SQLException {
        return getConnection(user, password);
    }


    /**
     * Retrieves a connection to the Mysql database, specifying a user
     * and password.
     *
     * @param user The string of the user to connect to the database with.
     * @param password The string of the corresponding password of user.
     */ 
    public Connection getConnection(String user, String password)
        throws SQLException {
        
        // creates the string to connect with
        String connectionPath = MYSQL_DRIVER_PATH + "://" 
   	                        + host + ":" + port + "/" + dbName;

        Connection dbConnection = null;
        
        // Instantiate the driver classes
        try {
            Class.forName(MYSQL_DRIVER_CLASS);
            dbConnection = DriverManager.getConnection(connectionPath, 
							user, password);
        }
        catch (ClassNotFoundException e) {
            throw new SQLException("Mysql driver was not found.");
        }

        return dbConnection;

    }
    
    

    public int getLoginTimeout() {
        return 10;
    }

    public void setLoginTimeout(int seconds) {
    }

    public PrintWriter getLogWriter() {
        return new PrintWriter(System.out);
    }

    public void setLogWriter(PrintWriter out) {
    }



}
