/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.gml;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.*;


/**
 * LEVEL2 saxGML4j GML handler: Gets basic alerts from GMLFilterDocument.
 *
 * <p>This filter simply seperates and passes GML events to a GMLGeometryFilter.  The main
 * simplification that it performs is to pass along coordinates as an abstracted CoordType,
 * regardless of their notation in the GML (Coord vs. Coordinates).</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 *
 */
public interface GMLHandlerGeometry extends XMLFilter {


		/**
		 * Sets content handler and passes along start warning.
		 */
		public void geometryStart(String localName, Attributes atts) throws SAXException;


		/**
		 * Sets content handler and passes along start warning.
		 */
		public void geometryEnd(String localName) throws SAXException;


		/**
		 * Sets content handler and passes along start warning.
		 */
		public void geometrySub(String localName) throws SAXException;


		/**
		 * Sets content handler and passes along start warning.
		 */
		public void gmlCoordinates( double x, double y )	throws SAXException;

		/**
		 * Sets content handler and passes along start warning.
		 */
		public void gmlCoordinates( double x, double y, double z ) throws SAXException;



}
