/*
 */
package org.geotools.gml;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.*;


/**
 * LEVEL3 saxGML4j GML handler: Gets JTS objects.
 *
 * <p>This handler must be implemented by the parent of a GMLFilterGeometry filter, in order to handle the 
 * JTS objects passed to it from the child.</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public interface GMLHandlerJTS extends XMLReader {


		/**
		 * Recieves OGC simple feature type geometry from parent.
		 */
		public void geometry(Geometry geometry);


}
