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

import net.opengis.wcs11.DomainSubsetType;
import net.opengis.wcs11.RangeSubsetType;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Request information from a CoverageSource.
 */
public class CoverageReadRequest extends CoverageRequest {
	private RangeSubsetType range;
	private DomainSubsetType domain;
	private CoordinateReferenceSystem targetCRS;
	private CoordinateReferenceSystem dataCRS;


	// 
    public CoordinateReferenceSystem getRequestCoordinateSystem(){
    	return dataCRS; // domain.getBoundingBox(); // this should be a ReferencedEnvelope
    }
    public void getRequestCoordinateSystem(CoordinateReferenceSystem dataCRS) {
		this.dataCRS = dataCRS;
	}

    public CoordinateReferenceSystem getResponseCoordinateSystem(){
    	return targetCRS;
    }
    public void getResponseCoordinateSystem(CoordinateReferenceSystem targetCRS) {
		this.targetCRS = targetCRS;
	}


	/**
	 * @return
	 * @uml.property  name="domainSubset"
	 */
	public DomainSubsetType getDomainSubset(){
		return domain;		
	}

	/**
	 * @return
	 * @uml.property  name="rangeSubset"
	 */
	RangeSubsetType getRangeSubset(){
		return range;
	}



	/**
	 * @param  value
	 * @uml.property  name="domainSubset"
	 */
	void setDomainSubset(DomainSubsetType value){
		domain = value;
	}


	/**
	 * @param  value
	 * @uml.property  name="rangeSubset"
	 */
	public void setRangeSubset(RangeSubsetType value) {
		range = value;
	}
}
