/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.gml;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.*;


/**
 * LEVEL3 saxGML4j GML handler: Gets JTS objects.
 *
 * <p>This filter simply seperates and passes GML events to a GMLGeometryFilter.  The main
 * simplification that it performs is to pass along coordinates as an abstracted CoordType,
 * regardless of their notation in the GML (Coord vs. Coordinates).</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 *
 */
public interface GMLHandlerJTS extends XMLReader {


		/**
		 * Sets content handler and passes along start warning.
		 */
		public void geometry(Geometry geometry);


}
