/*
 */
package org.geotools.gml;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.*;

import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * LEVEL4 saxGML4j GML handler: Gets features.
 *
 * <p>This handler must be implemented by the parent of a GMLFilterFeature filter, in order to handle the 
 * features passed to it from the child.</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public interface GMLHandlerFeature {


		/**
		 * Recieves OGC simple feature from parent.
		 */
		public void feature(Feature feature);

}
