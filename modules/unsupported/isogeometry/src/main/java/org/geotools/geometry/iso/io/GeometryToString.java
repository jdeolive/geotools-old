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

package org.geotools.geometry.iso.io;

import org.geotools.geometry.iso.io.wkt.GeometryToWKTString;
import org.opengis.geometry.Geometry;

/**
 * This class manages the toString() outputs for geometry objects
 * 
 * @author sanjay
 * 
 */
public class GeometryToString {

	public static String getString(Geometry geom) {
		GeometryToWKTString wkt = new GeometryToWKTString(true);
		return wkt.getString(geom);
	}
	



}
