/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
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

import net.opengis.wcs11.DomainSubsetType;
import net.opengis.wcs11.RangeSubsetType;

import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Encapsulates a data read request.
 */
public interface CoverageReadRequest extends CoverageRequest {


    CoordinateReferenceSystem getRequestCoordinateSystem();


    CoordinateReferenceSystem getResponseCoordinateSystem();


	/**
	 * @return
	 * @uml.property  name="domainSubset"
	 */
	DomainSubsetType getDomainSubset();



	/**
	 * @return
	 * @uml.property  name="rangeSubset"
	 */
	RangeSubsetType getRangeSubset();



	/**
	 * @param  value
	 * @uml.property  name="domainSubset"
	 */
	void setDomainSubset(DomainSubsetType value);


	/**
	 * @param  value
	 * @uml.property  name="rangeSubset"
	 */
	public abstract void setRangeSubset(RangeSubsetType value);
}
