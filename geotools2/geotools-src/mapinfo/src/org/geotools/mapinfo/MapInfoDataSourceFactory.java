package org.geotools.mapinfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;

/**
 *
 * @author  jamesm
 */
public class MapInfoDataSourceFactory implements org.geotools.data.DataSourceFactorySpi {
    
    /** Creates a new instance of GMLDataSourceFactory */
    public MapInfoDataSourceFactory() {
    }
    
    public boolean canProcess(HashMap params) {
        if(!params.containsKey("url")){
            return false;
        }
        String url =  (String)params.get("url");
        if(!url.toUpperCase().endsWith("MIF")){
            return false;
        }
        return true;
    }
    
    public DataSource createDataSource(HashMap params)  throws DataSourceException {      
        if(!canProcess(params)){
            return null;
        }
        String location = (String)params.get("url");
        try{
            MapInfoDataSource ds = new MapInfoDataSource(new URL(location));
            return ds;
        }
        catch(MalformedURLException mue){
            throw new DataSourceException("Unable to attatch datasource to " + location, mue);
        }
    }
    
    public String getDescription() {
        return "MapInfo(tm) Interchange files MIF/MID (*.mif)";
    }
    
}

