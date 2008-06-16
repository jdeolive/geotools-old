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
public interface CoverageWriteRequest extends CoverageRequest{
	
	/**
	 * @param  metadata
	 * @uml.property  name="metadata"
	 */
	public void setAdditionalMetadata(Object metadata);
	
	/**
	 * @return
	 * @uml.property  name="metadata"
	 */
	public Object getAdditionalMetadata();
	
	
	
	/**
	 * @param  metadata
	 * @uml.property  name="metadata"
	 */
	public void setData(Collection<? extends Coverage> data);
	
	/**
	 * @return
	 * @uml.property  name="metadata"
	 */
	public Collection<? extends Coverage> getData();
	
}
