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

/**
 * @author     Simone Giannecchini, GeoSolutions 	
 */
public class CoverageWriteRequest extends CoverageRequest{
	// do you have something specific you intend or this can be
	// handled by getAdditionalParameters() ?
	private Object metadata;
	private Collection<? extends Coverage> data;

	/**
	 * @param  metadata
	 * @uml.property  name="metadata"
	 */
	public void setAdditionalMetadata(Object metadata){
		this.metadata = metadata;
	}
	
	/**
	 * @return
	 * @uml.property  name="metadata"
	 */
	public Object getAdditionalMetadata(){
		return metadata;
	}
	
	
	
	/**
	 * @param  metadata
	 * @uml.property  name="metadata"
	 */
	public void setData(Collection<? extends Coverage> data){
		this.data = data;
	}
	
	/**
	 * @return
	 * @uml.property  name="metadata"
	 */
	public Collection<? extends Coverage> getData(){
		return data;
	}
	
}
