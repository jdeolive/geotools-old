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
import java.util.*;


/**
 * LEVEL2 saxGML4j GML handler: Gets basic alerts from GMLFilterDocument.
 * 
 * <p>
 * This handler is required for any parent of a GMLFilterDocument filter. It
 * receives basic element notifications and coordinates.
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: GMLHandlerGeometry.java,v 1.7 2003/08/14 18:36:47 cholmesny Exp $
 */
public interface GMLHandlerGeometry extends ContentHandler {
    /**
     * Receives a geometry start element from the parent.
     */
    abstract void geometryStart(String localName, Attributes atts)
        throws SAXException;

    /**
     * Receives a geometry end element from the parent.
     */
    abstract void geometryEnd(String localName) throws SAXException;

    /**
     * Receives a geometry sub element from the parent.
     */
    abstract void geometrySub(String localName) throws SAXException;

    /**
     * Receives a finished coordinate from the parent (2-valued).
     */
    abstract void gmlCoordinates(double x, double y) throws SAXException;

    /**
     * Receives a finished coordinate from the parent (3-valued).
     */
    abstract void gmlCoordinates(double x, double y, double z)
        throws SAXException;
}
