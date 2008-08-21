/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.data.coverage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * A class containing static convenience methods for locating
 * <code>CoverageStore</code>s and specific <code>CoverageSource</code>s,
 * and performing simple encoding and decoding.
 * 
 * 
 * @author Simone Giannecchini, GeoSolusions
 * @author Jody Garnett
 */
public class CoverageIO {
    

	/**
     * Test to see if this factory is suitable for processing the data pointed
     * to by the params map.
     *
     * <p>
     * If this datasource requires a number of parameters then this method
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
     *         present.
     */
    public static boolean canCreateCoverageDataSetProcess(java.util.Map<String, Serializable> params){
		return false;
    	
    }
    
    public static boolean canOpenCoverageDataSetProcess(java.util.Map<String, Serializable> params){
		return false;
    	
    }
    public static CoverageAccess createCoverageDataSet(Map<String, Serializable> params) throws IOException{
    	return null;
    }

    public static CoverageAccess openCoverageDataSet(Map<String, Serializable> params) throws IOException{
    	return null;
    }
    
    public static Set<? extends CoverageAccessFactory> getAvailableCoverageService(){
    	return null;
    }
    
}
