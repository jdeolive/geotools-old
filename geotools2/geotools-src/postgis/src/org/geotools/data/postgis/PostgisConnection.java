/* Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root application directory.
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
 *@author Rob Hranac, Vision for New York
 *@version $0.9 alpha, 11/01/01$
 *
 */
public class PostgisConnection implements javax.sql.DataSource {


    private static Category _log = Category.getInstance(PostgisConnection.class.getName());
    
    /** Creates PostGIS-specific JDBC driver class */ 
    private static final String POSTGIS_DRIVER_CLASS = "org.postgresql.Driver";
    
    /** Creates PostGIS-specific JDBC driver path */ 
    private static final String POSTGIS_DRIVER_PATH = "jdbc:postgresql";


    private String host;

    private String port;

    private String dbName;

    private String user = "test";

    private String password = "test";
    
    /**
     * Constructor with all internal database driver classes, driver paths, and database types.
     *
     * @param host The driver class; should be passed from the database-specific subclass.
     * @param port The driver path; should be passed from the database-specific subclass..
     * @param dbName The database type; should be passed from the database-specific subclass.
     */ 
    public PostgisConnection ( String host, String port, String dbName) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
    }
    
    
    /**
     * Creates a database connection method to initialize a given database for feature extraction.
     *
     * @param featureType A complete description of the feature type metadata.
     */ 
    public void setLogin( String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Creates a database connection method to initialize a given database for feature extraction.
     *
     * @param featureType A complete description of the feature type metadata.
     */ 
    public Connection getConnection()
        throws SQLException {
        return getConnection(user, password);
    }


    /**
     * Creates a database connection method to initialize a given database for feature extraction.
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
