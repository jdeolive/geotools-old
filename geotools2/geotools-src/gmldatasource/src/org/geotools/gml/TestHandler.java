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
 * This very simple handler just prints every JTS geometry that it gets to the standard output.
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
