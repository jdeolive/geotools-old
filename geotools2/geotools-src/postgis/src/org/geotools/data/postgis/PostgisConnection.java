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

package org.geotools.data.postgis;

import java.io.*;
import java.util.*;
import java.sql.*;
import org.apache.log4j.Category;


/**
 * Shell for JDBC transactions of all types.
 *
 * This provides a base class to the database transactional classes.  
 *
 * @version $Id: PostgisConnection.java,v 1.2 2002/06/05 12:05:16 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public class PostgisConnection implements javax.sql.DataSource {


    private static Category _log = Category.getInstance(PostgisConnection.class.getName());
    
    /** Creates PostGIS-specific JDBC driver class. */ 
    private static final String POSTGIS_DRIVER_CLASS = "org.postgresql.Driver";
    
    /** Creates PostGIS-specific JDBC driver path. */ 
    private static final String POSTGIS_DRIVER_PATH = "jdbc:postgresql";


    private String host;

    private String port;

    private String dbName;

    private String user = "test";

    private String password = "test";
    
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
    public PostgisConnection ( String host, String port, String dbName) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
    }
    
    
    /**
     * Creates a database connection method to initialize a given database
     * for feature extraction.
     *
     * @param featureType A complete description of the feature type metadata.
     */ 
    public void setLogin( String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Creates a database connection method to initialize a given database
     * for feature extraction.
     *
     * @param featureType A complete description of the feature type metadata.
     */ 
    public Connection getConnection()
        throws SQLException {
        return getConnection(user, password);
    }


    /**
     * Creates a database connection method to initialize a given database
     * for feature extraction.
     *
     * @param featureType A complete description of the feature type metadata.
     */ 
    public Connection getConnection(String user, String password)
        throws SQLException {
        
        // makes a new feature type bean to deal with incoming
        String connectionPath = POSTGIS_DRIVER_PATH + "://" + host + ":" + port + "/" + dbName;
        Connection dbConnection = null;
        
        // Instantiate the driver classes
        try {
            Class.forName(POSTGIS_DRIVER_CLASS);
            dbConnection = DriverManager.getConnection( connectionPath , user, password);
        }
        catch (ClassNotFoundException e) {
            throw new SQLException("Postgis driver was not found.");
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
