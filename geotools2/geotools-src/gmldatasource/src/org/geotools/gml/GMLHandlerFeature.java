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

import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * LEVEL4 saxGML4j GML handler: Gets features.
 *
 * <p>This handler must be implemented by the parent of a GMLFilterFeature
 * filter in order to handle the features passed to it from the child.</p>
 * 
 * @version $Id: GMLHandlerFeature.java,v 1.3 2002/06/05 11:05:56 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public interface GMLHandlerFeature {


		/**
		 * Receives OGC simple feature from parent.
		 */
		public void feature(Feature feature);

}
