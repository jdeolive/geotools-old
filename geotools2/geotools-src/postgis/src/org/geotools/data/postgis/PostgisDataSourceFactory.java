/*
 * PostgisDataSourceFactory.java
 *
 * Created on March 5, 2003, 10:59 AM
 */

package org.geotools.data.postgis;


import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;

/**
 *
 * @author  jamesm
 */
public class PostgisDataSourceFactory implements org.geotools.data.DataSourceFactorySpi {
    
    /** Creates a new instance of GMLDataSourceFactory */
    public PostgisDataSourceFactory() {
    }
    
    public boolean canProcess(Map params) {
        if(!params.containsKey("dbtype")){
            return false;
        }
        if(!((String)params.get("dbtype")).equalsIgnoreCase("postgis")){
            return false;
        }
        if(!params.containsKey("host")){
            return false;
        }
        if(!params.containsKey("user")){
            return false;
        }
        if(!params.containsKey("passwd")){
            return false;
        }
        if(!params.containsKey("database")){
            return false;
        }
        if(!params.containsKey("table")){
            return false;
        }
        return true;
    }
    
    public DataSource createDataSource(Map params)  throws DataSourceException {
        if(!canProcess(params)){
            return null;
        }
        String host = (String)params.get("host");
        String user = (String)params.get("user");
        String passwd = (String)params.get("passwd");
        String port = (String)params.get("port");
        String database = (String)params.get("database");
        String table = (String)params.get("table");
        
        
        PostgisConnectionFactory db =
        new PostgisConnectionFactory(host, port, database);
        try{
            db.setLogin(user, passwd);            
            PostgisDataSource ds = new PostgisDataSource(db.getConnection(), table);
            return ds;
        }
        catch(SQLException sqle){
            throw new DataSourceException("Unable to connect to database",sqle);
        }
        
    }
    
    public String getDescription() {
        return "PostGIS spatial database";
    }
    
}

