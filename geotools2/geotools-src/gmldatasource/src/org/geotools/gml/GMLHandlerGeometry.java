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
 * <p>This handler is required for any parent of a GMLFilterDocument filter.  It recieves
 * basic element notifications and coordinates.</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public interface GMLHandlerGeometry extends ContentHandler {


		/**
		 * Recieves a geometry start element from the parent.
		 */
		public abstract void geometryStart(String localName, Attributes atts) throws SAXException;


		/**
		 * Recieves a geometry end element from the parent.
		 */
		public abstract void geometryEnd(String localName) throws SAXException;


		/**
		 * Recieves a geometry sub element from the parent.
		 */
		public abstract void geometrySub(String localName) throws SAXException;


		/**
		 * Recieves a finished coordinate from the parent (2-valued).
		 */
		public abstract void gmlCoordinates( double x, double y )	throws SAXException;

		/**
		 * Recieves a finished coordinate from the parent (3-valued).
		 */
		public abstract void gmlCoordinates( double x, double y, double z ) throws SAXException;


}
