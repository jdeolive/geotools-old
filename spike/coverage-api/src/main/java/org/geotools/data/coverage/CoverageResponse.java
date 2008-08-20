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

import java.util.Collection;

import org.opengis.coverage.Coverage;
import org.opengis.util.ProgressListener;


/**
 * A coverage response; please check the status before assuming any data is available.
  */
public interface CoverageResponse {
   
    /**
	 * @author      Administrator
	 */
    public enum Status{
    	FAILURE,WARNING,SUCCESS,UNAVAILABLE
    }
    
    /**
     * The handle attribute is included to allow a client to associate  a
     * mnemonic name to the Query request. The purpose of the handle attribute
     * is to provide an error handling mechanism for locating  a statement
     * that might fail.
     *
     * @return the mnemonic name of the query request.
     */
    String getHandle();
    
    
    Status getStatus();

	Collection<? extends Exception> getExceptions();
	// exceptions are fine for reporting problems
	// but a more difficult fit for reporting warnings

	/**
	 *
	 */
	public void getRequest();
	
	Collection<? extends Coverage> getResults(final ProgressListener listener);
	
}

   
