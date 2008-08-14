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

import java.util.Map;

import org.geotools.factory.Hints;

/**
 * 
 * @author     Administrator
 */
public class CoverageRequest {

	private String identifier;
	private String handle;
	private Hints hints;
	private Map<String, Object> additionalParameters;

	/**
	 * @return
	 * @uml.property  name="coverageIdentifier"
	 */
	public String getCoverageIdentifier(){
		return identifier;
	}

	/**
	 * @param  coverageIdentifier
	 * @uml.property  name="coverageIdentifier"
	 */
	public void setCoverageIdentifier(final String coverageIdentifier){
		this.identifier = coverageIdentifier;
	}

	/**
	 * The handle attribute is included to allow a client to associate  a mnemonic name to the Query request. The purpose of the handle attribute is to provide an error handling mechanism for locating  a statement that might fail.
	 * @return     the mnemonic name of the query request.
	 * @uml.property  name="handle"
	 */
	public String getHandle(){
		return handle;
	}
	
	/**
	 * @param  handle
	 * @uml.property  name="handle"
	 */
	public void setHandle(String handle){
		this.handle = handle;
	}

	/**
	 * Specifies some hints to drive the query execution and results build-up.
	 * <p>
	 * Hints examples can be the GeometryFactory to be used, a generalization distance to be applied right in the data store, to data store specific things such as the fetch size to be used in JDBC queries. The set of hints supported can be fetched by calling  {@links   FeatureSource#getSupportedHints()}  . Depending on the actual values of the hints, the data store is free to ignore them. No mechanism is in place, at the moment, to figure out which hints where actually used during the query execution.
	 * @return   the Hints the data store should try to use when executing the query  (eventually empty but never null).
	 * @uml.property  name="hints"
	 */
	public Hints getHints(){
		return hints;
	}

	/**
	 * @param  hints
	 * @uml.property  name="hints"
	 */
	public void setHints(Hints hints){
		this.hints = hints;
	}

	/**
	 * @param  additionalParameters
	 * @uml.property  name="additionalParameters"
	 */
	public void setAdditionalParameters( Map<String,Object> additionalParameters){
		this.additionalParameters = additionalParameters;
	}

	/**
	 * @uml.property  name="additionalParameters"
	 * @uml.associationEnd  multiplicity="(0 -1)" container="org.geotools.data.coverage.Param"
	 */
	public Map<String,Object> getAdditionalParameters(){
		return additionalParameters;
	}
	
	// This is not generally how we use a listener
	// public void setListener (final ProgressListener listener);
	//
	// public ProgressListener getListener();

}
