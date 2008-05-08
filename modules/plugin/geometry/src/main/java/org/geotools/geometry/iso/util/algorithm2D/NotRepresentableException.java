/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences Köln (Fachhochschule Köln)
 *    (C) 2001-2006  Vivid Solutions
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

package org.geotools.geometry.iso.util.algorithm2D;

/**
 * Indicates that a {@link HCoordinate} has been computed which is not
 * representable on the Cartesian plane.
 * 
 * @see HCoordinate
 */
public class NotRepresentableException extends Exception {

	public NotRepresentableException() {
		super("Projective point not representable on the Cartesian plane.");
	}

}
