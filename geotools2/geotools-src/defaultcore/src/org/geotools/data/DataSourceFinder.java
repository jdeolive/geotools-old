/*
 * GeometryFactory.java
 *
 * Created on February 12, 2003, 2:36 PM
 */

package org.geotools.data;

import java.util.HashMap;
import java.util.Iterator;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFactorySpi;
import sun.misc.Service;


/**
 *
 * @author  jamesm
 */
public class DataSourceFinder {
    
    /** Creates a new instance of GeometryFactory */
    public DataSourceFinder() {
    }
    
     public static DataSource getDataSource(HashMap params) throws DataSourceException
    {
        Iterator ps = Service.providers(DataSourceFactorySpi.class); 
        System.out.println("Available Data Sources");
        while(ps.hasNext()){
            DataSourceFactorySpi fac = (DataSourceFactorySpi)ps.next();
            System.out.println(fac.getDescription());
            if(fac.canProcess(params)){
                return fac.createDataSource(params);
            }
        }
        return null;
    }
     
     public static Iterator getAvailableDataSources()
    {
        return Service.providers(DataSourceFactorySpi.class); 
    } 
}

