/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.gml;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.vividsolutions.jts.geom.*;


/**
 * LEVEL2 saxGML4j GML handler: Gets basic alerts from GMLFilterDocument.
 *
 * <p>This handler is required for any parent of a GMLFilterDocument filter.
 * It receives basic element notifications and coordinates.</p>
 * 
 * @version $Id: GMLHandlerGeometry.java,v 1.4 2002/06/05 11:09:06 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public interface GMLHandlerGeometry extends ContentHandler {


		/**
		 * Receives a geometry start element from the parent.
		 */
		public abstract void geometryStart(String localName, Attributes atts) throws SAXException;


		/**
		 * Receives a geometry end element from the parent.
		 */
		public abstract void geometryEnd(String localName) throws SAXException;


		/**
		 * Receives a geometry sub element from the parent.
		 */
		public abstract void geometrySub(String localName) throws SAXException;


		/**
		 * Receives a finished coordinate from the parent (2-valued).
		 */
		public abstract void gmlCoordinates( double x, double y )	throws SAXException;

		/**
		 * Receives a finished coordinate from the parent (3-valued).
		 */
		public abstract void gmlCoordinates( double x, double y, double z ) throws SAXException;


}
