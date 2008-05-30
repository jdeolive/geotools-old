package org.geotools.data.coverage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class CoverageIO {
    

	/**
     * Test to see if this factory is suitable for processing the data pointed
     * to by the params map.
     *
     * <p>
     * If this datasource requires a number of parameters then this mehtod
     * should check that they are all present and that they are all valid. If
     * the datasource is a file reading data source then the extentions or
     * mime types of any files specified should be checked. For example, a
     * Shapefile datasource should check that the url param ends with shp,
     * such tests should be case insensative.
     * </p>
     *
     * @param params The full set of information needed to construct a live
     *        data source.
     *
     * @return booean true if and only if this factory can process the resource
     *         indicated by the param set and all the required params are
     *         pressent.
     */
    public static boolean canCreateCoverageDataSetProcess(java.util.Map<String, Serializable> params){
		return false;
    	
    }
    
    public static boolean canOpenCoverageDataSetProcess(java.util.Map<String, Serializable> params){
		return false;
    	
    }
    public static CoverageDataStore createCoverageDataSet(Map<String, Serializable> params) throws IOException{
    	return null;
    }

    public static CoverageDataStore openCoverageDataSet(Map<String, Serializable> params) throws IOException{
    	return null;
    }
    
    public static Set<? extends CoverageService> getAvailableCoverageService(){
    	return null;
    }
    
}
