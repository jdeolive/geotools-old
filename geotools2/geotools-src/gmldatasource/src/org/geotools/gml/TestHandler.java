/*
 * Copyright (c) 2001 Vision for New York - www.vfny.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root application directory.
 */
package org.geotools.gml;

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.*;

/**
 * Simple test implementation of <code>GMLHandlerJTS</code>.
 *
 * <p>This filter simply seperates and passes GML events to a GMLGeometryFilter.  The main
 * simplification that it performs is to pass along coordinates as an abstracted CoordType,
 * regardless of their notation in the GML (Coord vs. Coordinates).</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 *
 */
public class TestHandler extends XMLFilterImpl implements GMLHandlerJTS {


		public void geometry(Geometry geometry) {
				System.out.println( "here is the geometry: " + geometry.toString());
		}

}
