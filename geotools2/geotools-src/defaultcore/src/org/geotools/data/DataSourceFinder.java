/*
 * GeometryFactory.java
 *
 * Created on February 12, 2003, 2:36 PM
 */

package org.geotools.data;

import java.util.logging.Logger;
import java.util.Map;
import java.util.Iterator;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFactorySpi;
import sun.misc.Service;


/**
 *
 * @author  jamesm
 */
public class DataSourceFinder {
    
    /** The logger for the data module.  */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    /** Creates a new instance of DataSourceFactory */
    public DataSourceFinder() {
    }
    
     public static DataSource getDataSource(Map params) throws DataSourceException
    {
        Iterator ps = Service.providers(DataSourceFactorySpi.class); 
        LOGGER.finer("Available Data Sources:");
        while(ps.hasNext()){
            DataSourceFactorySpi fac = (DataSourceFactorySpi)ps.next();
            LOGGER.finer(fac.getDescription());
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

