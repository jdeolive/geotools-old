/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gml;

import com.vividsolutions.jts.geom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;


/**
 * Simple test implementation of <code>GMLHandlerJTS</code>. This very simple
 * handler just prints every JTS geometry that it gets to the standard output.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: TestHandler.java,v 1.5 2003/08/14 18:36:47 cholmesny Exp $
 */
public class TestHandler extends XMLFilterImpl implements GMLHandlerJTS {
    public void geometry(Geometry geometry) {
        System.out.println("here is the geometry: " + geometry.toString());
    }
}
