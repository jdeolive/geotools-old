/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences Köln (Fachhochschule Köln)
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


package org.geotools.geometry.iso.complex;

import org.geotools.geometry.iso.primitive.BoundaryImpl;
import org.opengis.geometry.complex.ComplexBoundary;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 *
 */
public abstract class ComplexBoundaryImpl extends BoundaryImpl implements ComplexBoundary {

	/**
	 * @param crs
	 */
	public ComplexBoundaryImpl(CoordinateReferenceSystem crs) {
		super(crs);
		// TODO Auto-generated constructor stub
	}
}
