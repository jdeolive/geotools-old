package org.geotools.shapefile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;

/**
 *
 * @author  jamesm
 */
public class ShapefileDataSourceFactory implements org.geotools.data.DataSourceFactorySpi {
    
    /** Creates a new instance of GMLDataSourceFactory */
    public ShapefileDataSourceFactory() {
    }
    
    public boolean canProcess(HashMap params) {
        if(!params.containsKey("url")){
            return false;
        }
        String url =  (String)params.get("url");
        if(!url.toUpperCase().endsWith("SHP")){
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
            ShapefileDataSource ds = new ShapefileDataSource(new URL(location));
            return ds;
        }
        catch(MalformedURLException mue){
            throw new DataSourceException("Unable to attatch datasource to " + location, mue);
        }
    }
    
    public String getDescription() {
        return "ESRI(tm) Shapefiles (*.shp)";
    }
    
}

